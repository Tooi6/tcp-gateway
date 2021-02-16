package simulate;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.tooi.tcp.gateway.server.handler.DecoderHandler;
import io.tooi.tcp.gateway.server.handler.EncodeHandler;
import lombok.extern.slf4j.Slf4j;
import simulate.handler.SimulateHandler;

/**
 * @author Tooi
 * @since 2021-02-13 18:07:03
 */
@Slf4j
public class TcpSimulate {

    private static final String HOST = "192.168.31.68";
    private static final int PORT = 36001;

    public static void main(String[] args) {
        // 通过nio方式来接收连接和处理连接
        EventLoopGroup group = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);

        bootstrap.channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .remoteAddress(HOST, PORT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast("decoder", new DecoderHandler());
                        ch.pipeline().addLast("encoder", new EncodeHandler());
                        ch.pipeline().addLast(new SimulateHandler());
                    }
                });

        log.info("客户端开始连接...");
        ChannelFuture future = null;
        try {
            future = bootstrap.connect().sync();
            log.info("客户端连接成功！");
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("客户端连接失败", e);
        } finally {
            group.shutdownGracefully();
        }


    }
}
