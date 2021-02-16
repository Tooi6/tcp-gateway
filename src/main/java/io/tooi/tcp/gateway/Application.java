package io.tooi.tcp.gateway;

import io.tooi.tcp.gateway.queue.CacheQueue;
import io.tooi.tcp.gateway.queue.DownDataTransfer;
import io.tooi.tcp.gateway.queue.UpDataTransfer;
import io.tooi.tcp.gateway.server.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author Tooi
 * @since 2021-02-08 15:49:58
 */
@Slf4j
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        // 初始化配置
        // 初始化缓冲队列
        try {
            new UpDataTransfer(CacheQueue.up2MQQueue, 1).start();
            new DownDataTransfer(CacheQueue.down2GateQueue, 1).start();
        } catch (Exception e) {
            log.error("缓冲队列初始化失败！", e);
            System.exit(-1);
        }

        // 初始化Netty
        NettyServer nettyServer = context.getBean(NettyServer.class);
        nettyServer.start();
    }

}
