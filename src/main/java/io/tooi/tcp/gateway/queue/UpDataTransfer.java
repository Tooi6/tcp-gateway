package io.tooi.tcp.gateway.queue;

import io.tooi.tcp.gateway.concurrent.ThreadFactoryImpl;
import io.tooi.tcp.gateway.message.ProtoMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 上行数据中转
 *
 * @author Tooi
 * @since 2021-02-08 16:07:47
 */
@Slf4j
public class UpDataTransfer implements Runnable {

    /**
     * 线程池
     */
    private int poolSize;

    private ExecutorService executorService;

    /**
     * 上行消息缓冲
     */
    private BlockingQueue<ProtoMsg.Message> up2MQQueue;

    public UpDataTransfer(BlockingQueue<ProtoMsg.Message> queue, int poolSize) {
        this.poolSize = poolSize;
        this.up2MQQueue = queue;
        executorService = Executors.newFixedThreadPool(poolSize, new ThreadFactoryImpl("upDataTransfer_T", false));
    }

    @Override
    public void run() {
        for (int i = 0; i < poolSize; i++) {
            executorService.execute(() -> {
                while (true) {
                    ProtoMsg.Message message = null;
                    try {
                        message = up2MQQueue.take();
                        if (message == null) {
                            continue;
                        }
                        // TODO 上报消息到MQ
                        log.info("上报消息到MQ：{}", message.getMessageId());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void start() throws Exception {
        new Thread(this).start();
    }
}
