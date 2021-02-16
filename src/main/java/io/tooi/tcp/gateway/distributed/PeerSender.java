package io.tooi.tcp.gateway.distributed;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GenericFutureListener;
import io.tooi.tcp.gateway.domain.NotifiedData;
import io.tooi.tcp.gateway.message.ProtoMsg;
import io.tooi.tcp.gateway.server.handler.DecoderHandler;
import io.tooi.tcp.gateway.server.handler.EncodeHandler;
import io.tooi.tcp.gateway.utils.CommonUtils;
import io.tooi.tcp.gateway.utils.JsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Tooi
 * @since 2021-02-14 15:05:14
 */
@Slf4j
@Data
public class PeerSender {

    private Channel channel;

    private GateNode node;

    /**
     * 连接标志
     */
    private boolean connectFlag = false;

    private Bootstrap bootstrap;
    private EventLoopGroup group;

    public PeerSender(GateNode gateNode) {
        this.node = gateNode;
        bootstrap = new Bootstrap();
        group = new NioEventLoopGroup();
    }

    GenericFutureListener<ChannelFuture> closeListener = (ChannelFuture f) ->
    {
        log.info("{} 节点连接断开。", node.toString());
        channel = null;
        connectFlag = false;
    };

    private GenericFutureListener<ChannelFuture> connectedListener = (ChannelFuture f) ->
    {
        final EventLoop eventLoop = f.channel().eventLoop();
        if (!f.isSuccess()) {
            log.info("连接失败!在10s之后准备尝试重连!");
            eventLoop.schedule(() -> PeerSender.this.doConnect(), 10, TimeUnit.SECONDS);

            connectFlag = false;
        } else {
            connectFlag = true;

            log.info("节点连接成功：{}", node.toString());

            channel = f.channel();
            channel.closeFuture().addListener(closeListener);

            /**
             * 发送连接成功的通知
             */
            NotifiedData<GateNode> nodeNotifiedData = new NotifiedData<>(NodeWorker.getInstance().getLocalNodeInfo());
            ProtoMsg.Notification notificationMessage = ProtoMsg.Notification.newBuilder()
                    .setType(ProtoMsg.Notification_type.CONNECT_FINISHED)
                    .setJson(JsonUtil.pojoToJson(nodeNotifiedData))
                    .setTimeStamp(System.currentTimeMillis())
                    .build();
            ProtoMsg.Message message = ProtoMsg.Message.newBuilder()
                    .setType(ProtoMsg.MessageType.NOTIFICATION)
                    .setMessageId(CommonUtils.getUUID())
                    .setSessionId("-1")
                    .setNotification(notificationMessage)
                    .build();
            channel.writeAndFlush(message);
        }
    };


    /**
     * 连接
     */
    public void doConnect() {

        // 服务器ip地址
        String host = node.getHost();
        // 服务器端口
        int port = node.getPort();

        try {
            if (bootstrap != null && bootstrap.group() == null) {
                bootstrap.group(group);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
                bootstrap.remoteAddress(host, port);

                // 设置通道初始化
                bootstrap.handler(
                        new ChannelInitializer<SocketChannel>() {
                            public void initChannel(SocketChannel ch) {
                                ch.pipeline().addLast("decoder", new DecoderHandler());
                                ch.pipeline().addLast("encoder", new EncodeHandler());
                            }
                        }
                );
                log.info("开始连接节点：{}", node.toString());

                ChannelFuture f = bootstrap.connect();
                f.addListener(connectedListener);


                // 阻塞
//                 f.channel().closeFuture().sync();
            } else if (bootstrap.group() != null) {
                log.info("再一次连接节点：{}", node);
                ChannelFuture f = bootstrap.connect();
                f.addListener(connectedListener);
            }
        } catch (Exception e) {
            log.info("客户端连接失败!" + e.getMessage());
        }

    }

}
