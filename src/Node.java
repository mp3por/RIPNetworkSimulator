/**
 * Created by velin.
 */
public class Node {
    private final Integer nodeId;
    private final RouteTable routeTable;

    public Node(Integer nodeId) {
        this.nodeId = nodeId;
        this.routeTable = new RouteTable(this.nodeId);
    }
}
