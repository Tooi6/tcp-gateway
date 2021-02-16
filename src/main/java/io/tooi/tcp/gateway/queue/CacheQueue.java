package io.tooi.tcp.gateway.queue;

import io.tooi.tcp.gateway.message.ProtoMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Tooi
 * @since 2021-02-09 10:14:11
 */
@Slf4j
public class CacheQueue {

    /**
     * 下行消息总线
     */
    public static BlockingQueue<ProtoMsg.Message> down2GateQueue;

    /**
     * 上行消息总线
     */
    public static BlockingQueue<ProtoMsg.Message> up2MQQueue;

    static {
        down2GateQueue = new LinkedBlockingDeque<ProtoMsg.Message>();
        up2MQQueue = new LinkedBlockingDeque<ProtoMsg.Message>();
        log.info("缓冲队列初始化完成。");
    }


}
