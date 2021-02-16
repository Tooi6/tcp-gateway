package io.tooi.tcp.gateway.distributed;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author Tooi
 * @since 2021-02-14 13:13:11
 */
@Data
public class GateNode implements Comparable<GateNode>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * zk生成的ID
     */
    private long id;

    /**
     * 负载均衡参数（连接数）
     */
    private Integer balance = 0;

    /**
     * 服务IP
     */
    private String host;

    /**
     * 服务端口
     */
    private Integer port;

    public void incrementBalance() {
        balance++;
    }

    public void decrementBalance() {
        balance--;
    }

    @Override
    public int compareTo(GateNode o) {
        if (this.balance > o.balance) {
            return 1;
        } else if (this.balance < o.balance) {
            return -1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GateNode node = (GateNode) o;
        return Objects.equals(host, node.host) &&
                Objects.equals(port, node.port);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, host, port);
    }

    @Override
    public String toString() {
        return "GateNode{" +
                "id=" + id +
                ", balance=" + balance +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
