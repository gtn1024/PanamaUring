package top.dreamlike.async.file;

import io.smallrye.mutiny.Uni;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.dreamlike.access.AccessHelper;
import top.dreamlike.async.PlainAsyncFd;
import top.dreamlike.async.uring.IOUring;
import top.dreamlike.eventloop.IOUringEventLoop;
import top.dreamlike.extension.NotEnoughSqeException;
import top.dreamlike.extension.fp.Result;
import top.dreamlike.helper.NativeCallException;
import top.dreamlike.helper.NativeHelper;
import top.dreamlike.helper.StackValue;
import top.dreamlike.helper.Unsafe;
import top.dreamlike.nativeLib.fcntl.stat;
import top.dreamlike.nativeLib.unistd.unistd_h;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static top.dreamlike.nativeLib.errno.errno_h.EWOULDBLOCK;
import static top.dreamlike.nativeLib.fcntl.fcntl_h.*;
import static top.dreamlike.nativeLib.flock.file_h.flock;

public class AsyncFile extends PlainAsyncFd {
    private static final Logger log = LoggerFactory.getLogger(AsyncFile.class);

    private final IOUring uring;
    private final int fd;

    private final AtomicBoolean hasLocked = new AtomicBoolean(false);


    public AsyncFile(String path, IOUringEventLoop eventLoop, int ops) {
        super(eventLoop);
        try (Arena allocator = Arena.ofConfined()) {
            MemorySegment filePath = allocator.allocateFrom(path);
            fd = open(filePath, ops);
            if (fd < 0) {
                throw new IllegalStateException("res open error:" + NativeHelper.getNowError());
            }

        }
        this.uring = AccessHelper.fetchIOURing.apply(eventLoop);
    }

    public AsyncFile(int fd,IOUringEventLoop eventLoop){
        super(eventLoop);
        this.fd = fd;
        if (fd < 0){
            throw new IllegalStateException("res open error:" + NativeHelper.getNowError());
        }
        this.uring = AccessHelper.fetchIOURing.apply(eventLoop);
    }


    @Unsafe("memory segment要保证有效且为share范围的session")
    public CompletableFuture<Integer> readUnsafe(int offset, MemorySegment memorySegment) {
        return super.readUnsafe(offset, memorySegment);
    }



    public CompletableFuture<byte[]> readSelected(int offset, int length) {
        if (closed.get()) {
            throw new NativeCallException("file has closed");
        }
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        eventLoop.runOnEventLoop(() -> {
            if (!uring.prep_selected_read(fd, offset, length, future)) {
                future.completeExceptionally(new NotEnoughSqeException());
            }
        });
        return future;
    }

    public Uni<byte[]> readSelectedLazy(int offset, int length) {
        if (closed.get()) {
            throw new NativeCallException("file has closed");
        }
        return Uni.createFrom()
                .emitter(ue -> eventLoop.runOnEventLoop(() -> {
                    AtomicBoolean end = new AtomicBoolean(false);
                    long userData = uring.prep_selected_read_and_get_user_data(fd, offset, length, (r) -> {
                        if (!end.compareAndSet(false, true)) {
                            return;
                        }
                        switch (r) {
                            case Result.OK(byte[] res) -> ue.complete(res);
                            case Result.Err(Throwable t) -> ue.fail(t);
                        }
                    });

                    if (userData == IOUring.NO_SQE) {
                        end.set(true);
                        ue.fail(new NotEnoughSqeException());
                        return;
                    }

                    ue.onTermination(() -> onTermination(end, userData));
                }));
    }


    @Override
    public CompletableFuture<Integer> writeUnsafe(int offset, MemorySegment memorySegment) {
        return super.writeUnsafe(offset, memorySegment);
    }

    public CompletableFuture<Integer> fsync() {
        if (closed.get()) {
            throw new NativeCallException("file has closed");
        }
        CompletableFuture<Integer> future = new CompletableFuture<>();
        eventLoop.runOnEventLoop(() -> {
            if (!uring.prep_fsync(fd, 0, future::complete)) {
                future.completeExceptionally(new NotEnoughSqeException());
            }
        });
        return future;
    }

    public Uni<Integer> fsyncLazy(int fsyncFlag) {
        if (closed.get()) {
            throw new NativeCallException("file has closed");
        }

        return Uni.createFrom()
                .emitter(ue -> eventLoop.runOnEventLoop(() -> {
                    AtomicBoolean end = new AtomicBoolean(false);
                    long userData = uring.prep_fsync_and_get_user_data(fd, fsyncFlag, syscallRes -> {
                        if (!end.compareAndSet(false, true)) {
                            return;
                        }
                        ue.complete(syscallRes);
                    });
                    if (userData == IOUring.NO_SQE) {
                        end.set(true);
                        ue.fail(new NotEnoughSqeException());
                        return;
                    }

                    ue.onTermination(() -> onTermination(end, userData));
                }));
    }

    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                unistd_h.close(fd);
            } catch (RuntimeException e) {
                closed.set(false);
                throw e;
            }
        }
    }


    @Override
    public int readFd() {
        return fd;
    }

    static {
        AccessHelper.fetchFileFd = (f) -> f.fd;
    }

    @Override
    public IOUringEventLoop fetchEventLoop() {
        return eventLoop;
    }

    public boolean tryLock() {
        int res = flock(fd, LOCK_EX() | LOCK_NB());
        if (res == 0) {
            hasLocked.set(true);
            return true;
        }
        if (NativeHelper.getErrorNo() == EWOULDBLOCK()) {
            return false;
        }
        throw new NativeCallException(NativeHelper.getNowError());
    }

    public boolean tryUnLock() {
        if (!hasLocked.get()) {
            return false;
        }

        int res = flock(fd, LOCK_NB() | LOCK_UN());

        if (res == 0) {
            hasLocked.set(false);
            return true;
        }
        throw new NativeCallException(NativeHelper.getNowError());
    }


    public CompletableFuture<Boolean> lock() {
        if (hasLocked.get()) {
            return CompletableFuture.completedFuture(true);
        }

        //fast path
        if (tryLock()) {
            return CompletableFuture.completedFuture(true);
        }
        CompletableFuture<Boolean> res = new CompletableFuture<>();
        lock0(res, Duration.ofMillis(1000));
        return res.whenComplete((__, t) -> {
            if (t instanceof CancellationException) {
                tryUnLock();
            }
        });
    }


    private void lock0(CompletableFuture<Boolean> completableFuture, Duration duration) {
        boolean b = tryLock();
        if (b) {
            completableFuture.complete(true);
            return;
        }
        eventLoop.scheduleTask(() -> lock0(completableFuture, duration), duration);
    }

    public long size() {
        try (StackValue stackValue = StackValue.currentStack()) {
            MemorySegment buffer = stat.allocate(stackValue);
            int i = fstat(fd, buffer);
            return stat.st_size$get(buffer);
        } catch (Throwable t) {
            throw new AssertionError("should not reach here", t);
        }
    }

    @Override
    public String toString() {
        return "AsyncFile{" +
                "fd=" + fd +
                ", hasLocked=" + hasLocked +
                '}';
    }
}
