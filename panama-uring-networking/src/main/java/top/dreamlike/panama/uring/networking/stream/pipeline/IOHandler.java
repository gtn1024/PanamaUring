package top.dreamlike.panama.uring.networking.stream.pipeline;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface IOHandler {

    default Executor executor() {
        return null;
    }

    default void onHandleAdded(IOStreamPipeline.IOContext context) {

    }

    default void onHandleRemoved(IOStreamPipeline.IOContext context) {

    }

    default void onHandleInactive(IOStreamPipeline.IOContext context) {

    }

    void onRead(IOStreamPipeline.IOContext context, Object msg);

    default void onError(IOStreamPipeline.IOContext context, Throwable cause) {

    }

    default void onWrite(IOStreamPipeline.IOContext context, Object msg, CompletableFuture<Integer> promise) {
        context.fireNextWrite(msg, promise);
    }
}
