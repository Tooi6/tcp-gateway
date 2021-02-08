package io.tooi.tcp.gateway.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;

/**
 * Netty 网关
 *
 * @author Tooi
 * @since 2021-02-08 16:19:56
 */
public class NettyServer {
    private String serverPort;
    private EventLoopGroup boss;
    private EventLoopGroup work;

    /**
     * 通过配置创建网关服务
     *
     * @return
     */
    public ServerBootstrap config() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        return serverBootstrap;
    }

    /**
     * 启动网关
     *
     * @param serverBootstrap
     */
    public void startServer(ServerBootstrap serverBootstrap) {

    }
}
