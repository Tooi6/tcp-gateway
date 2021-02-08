package io.tooi.tcp.gateway.queue;

import java.util.concurrent.BlockingQueue;

/**
 * 上行数据中转
 *
 * @author Tooi
 * @since 2021-02-08 16:07:47
 */
public class UpDataTransfer implements Runnable {

    /**
     * 上行消息队列
     */
    private BlockingQueue<Object> up2MQQueue;

    @Override
    public void run() {

    }
}
