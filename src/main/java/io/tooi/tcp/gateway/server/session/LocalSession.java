package io.tooi.tcp.gateway.server.session;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

/**
 * @author Tooi
 * @since 2021-02-14 12:17:46
 */
@Slf4j
@Data
public class LocalSession implements ServerSession {

    /**
     * 绑定 Session 与 Channel 的 key
     */
    public static final AttributeKey<LocalSession> SESSION_KEY =
            AttributeKey.valueOf("SESSION_KEY");

    /**
     * Netty Channel
     */
    private Channel channel;

    /**
     * Session唯一ID
     */
    private final String sessionId;

    /**
     * 登录状态
     */
    private boolean isLogin = false;

    public LocalSession(Channel channel) {
        this.channel = channel;
        this.sessionId = UUID.randomUUID().toString().replace("-", "");
    }

    public LocalSession bind() {
        log.info("LocalSession 绑定 Channel：{}", channel.remoteAddress());
        channel.attr(SESSION_KEY).set(this);
        return this;
    }

    @Override
    public void writeAndFlush(Object pkg) {
        if (channel.isWritable()) {
            channel.writeAndFlush(pkg);
        }
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public boolean isValid() {
        return false;
    }


    //关闭连接
    public synchronized void close() {
        //用户下线 通知其他节点

        ChannelFuture future = channel.close();
        future.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    log.error("Channel 关闭异常");
                }
            }
        });
    }
}
