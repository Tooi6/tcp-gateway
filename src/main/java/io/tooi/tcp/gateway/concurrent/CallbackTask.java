package io.tooi.tcp.gateway.concurrent;

/**
 * @author Tooi
 * @since 2021-02-14 18:51:47
 */
public interface CallbackTask<R> {
    R execute() throws Exception;

    void onBack(R r);

    void onException(Throwable t);
}
