package io.tooi.tcp.gateway.queue;

import java.util.concurrent.BlockingQueue;

/**
 * 下行消息队列
 *
 * @author Tooi
 * @since 2021-02-08 16:09:03
 */
public class DownDataTransfer implements Runnable {
    /**
     * 下行消息队列
     */
    private BlockingQueue<Object> down2GateQueue;

    @Override
    public void run() {

    }
}
