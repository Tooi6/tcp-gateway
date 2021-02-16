package io.tooi.tcp.gateway.distributed;

import io.tooi.tcp.gateway.constans.ServerConstants;
import io.tooi.tcp.gateway.distributed.zk.CuratorZkClient;
import io.tooi.tcp.gateway.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * 节点协调客户端
 *
 * @author Tooi
 * @since 2021-02-14 13:11:45
 */
@Slf4j
public class NodeWorker {

    /**
     * zk 客户端
     */
    private CuratorFramework client = null;

    /**
     * 当前节点路径
     */
    private String pathRegistered = null;

    /**
     * 节点信息
     */
    private GateNode localNode = null;

    // 单例模式
    private static NodeWorker singleInstance = null;

    private NodeWorker() {
    }

    public static NodeWorker getInstance() {
        if (null == singleInstance) {

            singleInstance = new NodeWorker();
            singleInstance.client =
                    CuratorZkClient.instance.getClient();
            singleInstance.localNode = new GateNode();
        }
        return singleInstance;
    }

    /**
     * 设置 Node
     *
     * @param host IP地址
     * @param port 端口
     */
    public void setLocalNode(String host, int port) {
        localNode.setHost(host);
        localNode.setPort(port);
    }

    /**
     * 初始化节点（在ZK中创建一个临时节点）
     */
    public void init() {
        createParentIfNeeded(ServerConstants.MANAGE_PATH);
        try {
            byte[] payload = JsonUtil.object2JsonBytes(localNode);

            pathRegistered = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(ServerConstants.PATH_PREFIX, payload);

            //为node 设置id
            localNode.setId(getId());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 取得当前节点编号
     *
     * @return 编号
     */
    public long getId() {
        return getIdByPath(pathRegistered);
    }

    /**
     * 取得 节点编号
     *
     * @param path 路径
     * @return 编号
     */
    public long getIdByPath(String path) {
        String sid = null;
        if (null == path) {
            throw new RuntimeException("节点路径有误");
        }
        int index = path.lastIndexOf(ServerConstants.PATH_PREFIX);
        if (index >= 0) {
            index += ServerConstants.PATH_PREFIX.length();
            sid = index <= path.length() ? path.substring(index) : null;
        }
        if (null == sid) {
            throw new RuntimeException("节点ID获取失败");
        }
        return Long.parseLong(sid);
    }

    /**
     * 创建父节点
     *
     * @param managePath 父节点路径
     */
    private void createParentIfNeeded(String managePath) {
        try {
            Stat stat = client.checkExists().forPath(managePath);
            if (null == stat) {
                client.create()
                        .creatingParentsIfNeeded()
                        .withProtection()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(managePath);
            }
        } catch (Exception e) {
            log.error("创建发生节点异常", e);
        }
    }

    /**
     * 返回本地的节点信息
     *
     * @return 本地的节点信息
     */
    public GateNode getLocalNodeInfo() {
        return localNode;
    }


}
