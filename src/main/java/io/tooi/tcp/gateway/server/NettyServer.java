package io.tooi.tcp.gateway.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.tooi.tcp.gateway.concurrent.FutureTaskScheduler;
import io.tooi.tcp.gateway.distributed.NodeWorker;
import io.tooi.tcp.gateway.distributed.WorkerRouter;
import io.tooi.tcp.gateway.server.handler.DataReportHandler;
import io.tooi.tcp.gateway.server.handler.DecoderHandler;
import io.tooi.tcp.gateway.server.handler.EncodeHandler;
import io.tooi.tcp.gateway.server.handler.LoginHandler;
import io.tooi.tcp.gateway.utils.IOUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Netty 网关
 *
 * @author Tooi
 * @since 2021-02-08 16:19:56
 */
@Slf4j
@Service("nettyServer")
public class NettyServer {
    @Value("${netty.port}")
    private int serverPort;
    private String ip = "0.0.0.0";
    private EventLoopGroup boss = new NioEventLoopGroup();
    private EventLoopGroup work = new NioEventLoopGroup();

    @Autowired
    private LoginHandler loginHandler;

    @Autowired
    private DataReportHandler dataReportHandler;

    ServerBootstrap serverBootstrap = new ServerBootstrap();


    /**
     * 启动Netty服务
     */
    public void start() {
        serverBootstrap
                .group(boss, work)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(ip, serverPort))
                // 长连接
                .option(ChannelOption.SO_KEEPALIVE, true)
                // 开启Nagle算法，（尽可能的发送大块数据避免网络中充斥着大量的小数据块）
                .option(ChannelOption.TCP_NODELAY, true)
                // ByteBuf 分配器
                .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        // 心跳检测，超时时间300s
                        ch.pipeline().addLast(new IdleStateHandler(0, 0, 300, TimeUnit.SECONDS));
                        // TODO 粘包临时解决方案
                        ch.pipeline().addLast(new FixedLengthFrameDecoder(63));
                        // 自定义编解码器
                        ch.pipeline().addLast("decoder", new DecoderHandler());
                        ch.pipeline().addLast("encoder", new EncodeHandler());
                        // 业务处理
                        ch.pipeline().addLast("login", loginHandler);
                        ch.pipeline().addLast("dataReport", dataReportHandler);
                    }
                });


        ChannelFuture channelFuture = null;
        boolean isStart = false;
        while (!isStart) {
            try {
                channelFuture = serverBootstrap.bind().sync();
                log.info("Netty 服务启动成功，端口：{}", channelFuture.channel().localAddress());
                isStart = true;
            } catch (Exception e) {
                log.error("启动 Netty 服务时发生异常", e);
                serverPort++;
                log.info("尝试一个新的端口：" + serverPort);
                serverBootstrap.localAddress(new InetSocketAddress(serverPort));
            }
        }

        if (false) {
            // TODO 开启集群模式（待完善）
            // 注册服务到 Zookeeper
            FutureTaskScheduler.add(() -> {
                NodeWorker.getInstance().setLocalNode(ip, serverPort);
                // 注册节点
                NodeWorker.getInstance().init();
                // 初始化路由器（监听节点事件）
                WorkerRouter.getInstance().init();
            });
        }


        // 监听Channel关闭事件
        ChannelFuture closeFuture = channelFuture.channel().closeFuture();
        try {
            closeFuture.sync();
        } catch (InterruptedException e) {
            log.error("关闭 Channel 发生异常", e);
        } finally {
            // 关闭线程组
            work.shutdownGracefully();
            boss.shutdownGracefully();
        }

    }

}
