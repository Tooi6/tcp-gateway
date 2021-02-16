### Tcp-Gateway

基于Netty实现的高并发、高可用的TCP物联网网关

### 核心依赖

| 依赖        | 版本         |
| :---------- | ------------ |
| SpringBoot  | 2.4.0        |
| Netty       | 4.1.25.Final |
| zookeeper   | 3.6.2        |
| ProtocolBuf | 3.14.0       |
| Redis       | 5.0.10       |

### 架构图

![](imgs\QQ截图20210215172842.png)

### 性能测试

> 开源测试工具： [Orion-Stress-Tester](https://github.com/MirrenTools/Orion-Stress-Tester)

- 启动程序

```shell
nohup java -jar  -server \
 -Xmx3000M \
 -Xms3000M \
 -Xmn600M \
 -Xss256K \
 -XX:+DisableExplicitGC \
 -XX:SurvivorRatio=1 \
 -XX:+UseConcMarkSweepGC \
 -XX:+UseParNewGC \
 -XX:+CMSParallelRemarkEnabled \
 -XX:+UseCMSCompactAtFullCollection \
 -XX:CMSFullGCsBeforeCompaction=0 \
 -XX:+CMSClassUnloadingEnabled \
 -XX:LargePageSizeInBytes=128M \
 -XX:+UseFastAccessorMethods \
 -XX:+UseCMSInitiatingOccupancyOnly \
 -XX:CMSInitiatingOccupancyFraction=70 \
 -XX:SoftRefLRUPolicyMSPerMB=0 \
 -XX:+PrintClassHistogram \
 -XX:+PrintGCDetails \
 -XX:+PrintGCTimeStamps \
 -XX:+PrintHeapAtGC \
 -Xloggc:log/gc.log tcp-gateway-1.0-SNAPSHOT.jar \
 > log/out.file 2>&1 &
```

