package io.tooi.tcp.gateway.config;

import io.tooi.tcp.gateway.distributed.zk.CuratorZkClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Tooi
 * @since 2021-02-14 17:09:13
 */
@Configuration
public class ZkClientConfig {

//    @Value("${zookeeper.connect.url}")
//    private String zkConnect;
//
//    @Bean(name = "curatorZKClient")
//    public CuratorZkClient curatorZKClient() {
//        return new CuratorZkClient(zkConnect);
//    }
}
