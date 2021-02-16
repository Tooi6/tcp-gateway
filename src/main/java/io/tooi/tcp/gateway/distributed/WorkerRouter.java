package io.tooi.tcp.gateway.distributed;

import io.tooi.tcp.gateway.constans.ServerConstants;
import io.tooi.tcp.gateway.distributed.zk.CuratorZkClient;
import io.tooi.tcp.gateway.utils.JsonUtil;
import io.tooi.tcp.gateway.utils.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tooi
 * @since 2021-02-14 15:01:54
 */
@Slf4j
public class WorkerRouter {

    private CuratorFramework client = null;

    // 注册路径
    private String pathRegistered = null;
    // 监听路径
    private static final String path = ServerConstants.MANAGE_PATH;
    // 节点容器
    private ConcurrentHashMap<Long, PeerSender> workerMap = new ConcurrentHashMap<>();
    private GateNode node = null;

    // 单例模式
    private static WorkerRouter singleInstance = null;

    public static WorkerRouter getInstance() {
        if (null == singleInstance) {
            singleInstance = new WorkerRouter();
            singleInstance.client = CuratorZkClient.instance.getClient();
        }
        return singleInstance;
    }

    private WorkerRouter() {

    }

    /**
     * 初始化节点管理
     */
    public void init() {
        try {
            // 订阅节点新增、删除事件
            PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
            PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    log.debug("收到监听事件...");
                    ChildData data = event.getData();
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            log.info("{} 节点上线，数据：{}", data.getPath(), data.getData());
                            processNodeAdded(data);
                            break;
                        case CHILD_REMOVED:
                            log.info("{} 节点下线，数据：{}", data.getPath(), data.getData());
                            processNodeRemoved(data);
                            break;
                        case CHILD_UPDATED:
                            log.info("{} 节点更新, 数据：{}", data.getPath(), data.getData());
                            break;
                        default:
                            log.warn("[PathChildrenCache]节点数据为空, path={}", data == null ? "null" : data.getPath());
                            break;
                    }
                }
            };

            childrenCache.getListenable().addListener(childrenCacheListener);
            log.info("注册集群监听器成功!");
            childrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);

        } catch (Exception e) {
            log.error("初始化节点路由管理异常", e);
        }
    }

    /**
     * 移除节点
     *
     * @param data 节点数据
     */
    private void processNodeRemoved(ChildData data) {

    }

    /**
     * 新增节点
     *
     * @param data 新节点数据
     */
    private void processNodeAdded(ChildData data) {
        byte[] payload = data.getData();
        GateNode gateNode = ObjectUtil.JsonBytes2Object(payload, GateNode.class);

        long id = NodeWorker.getInstance().getIdByPath(data.getPath());
        gateNode.setId(id);

        if (gateNode.equals(getLocalNode())) {
            log.info("本地节点，path={}，data={}",
                    data.getPath(), JsonUtil.pojoToJson(gateNode));
            return;
        }

        PeerSender relaySender = workerMap.get(gateNode.getId());
        if (null != relaySender && relaySender.getNode().equals(gateNode)) {
            log.debug("重复收到注册事件，path={}，data={}",
                    data.getPath(), JsonUtil.pojoToJson(gateNode));
            return;
        }

        if (null != relaySender) {
            log.info("{} 节点更新，关闭旧连接", data.getPath());
        }

        // 创建消息转发器
        relaySender = new PeerSender(gateNode);
        relaySender.doConnect();
        workerMap.put(gateNode.getId(), relaySender);
    }

    public GateNode getLocalNode() {
        return NodeWorker.getInstance().getLocalNodeInfo();
    }

}
