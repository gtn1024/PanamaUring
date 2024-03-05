import org.junit.Assert;
import org.junit.Test;
import top.dreamlike.common.CType;
import top.dreamlike.panama.nativelib.Instance;
import top.dreamlike.panama.nativelib.Libc;
import top.dreamlike.panama.nativelib.helper.DebugHelper;
import top.dreamlike.panama.nativelib.struct.SocketAddrIn;

import java.io.File;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class LibcTest {


    @Test
    public void testFile() throws IOException {
        Libc libc = Instance.LIBC;
        File path = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
        path.deleteOnExit();
        try (Arena arena = Arena.ofConfined()) {
            String absolutePath = path.getAbsolutePath();
            MemorySegment pathname = arena.allocateFrom(absolutePath);
            int fd = libc.open(pathname, Libc.Fcntl.O_RDWR);
            Assert.assertTrue(fd > 0);
            String string = UUID.randomUUID().toString();
            MemorySegment writeBuf = arena.allocateFrom(string);
            int write = libc.write(fd, writeBuf, (int) writeBuf.byteSize());
            Assert.assertTrue(write > 0);

            libc.close(fd);

            fd = libc.open(pathname, Libc.Fcntl.O_RDWR);
            MemorySegment readBuf = arena.allocate(string.length());
            int read = libc.read(fd, readBuf, (int) readBuf.byteSize());

            String readbuf = DebugHelper.bufToString(readBuf, string.length());
            Assert.assertEquals(string, readbuf);
        }
    }

    @Test
    public void test() {
        MemoryLayout layout = Instance.STRUCT_PROXY_GENERATOR.extract(SocketAddrIn.class);
        System.out.println(layout.byteSize());
        System.out.println(layout);
    }

}
