/**
 * Created by velin.
 */
public class NetworkNode {

    private final Integer nodeId;
    private final RouteTable routeTable;

    public NetworkNode(Integer nodeId) {
        this.nodeId = nodeId;
        this.routeTable = new RouteTable(this.nodeId);
    }


    @Override
    public String toString() {
        return "NetworkNode{" +
                "nodeId=" + nodeId +
                '}';
    }
}
