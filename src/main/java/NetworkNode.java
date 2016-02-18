import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by velin.
 */
public class NetworkNode {
    protected final Integer nodeId;
    protected final Integer[][] costs;
    private final RouteTable routeTable;
    private final DistanceVectorRoutingAlg routeAlg;

    public NetworkNode(int nodeId, Integer[][] costs, NetworkNodeRouteTableListener listener) {
        this.nodeId = nodeId;
        this.costs = costs;
        this.routeTable = new RouteTable(nodeId, listener);
        this.routeAlg = new DistanceVectorRoutingAlg();
    }

    public HashMap<Integer, Integer> getCostsMsg() {
        return routeTable.getCosts();
    }

    public void receiveCostsMsg(HashMap<Integer, Integer> receivedCosts, NetworkNode sender) {
        routeAlg.handleCostMsg(receivedCosts, sender);
    }

    public Integer getNodeId() {
        return nodeId;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("NetworkNode " + nodeId + ":\n");
        b.append(routeTable.toString());
        return b.toString();
    }

    public interface NetworkNodeRouteTableListener{
        void onRouteTableUpdate(Integer nodeId);
    }

    public class DistanceVectorRoutingAlg {

        public synchronized void handleCostMsg(HashMap<Integer, Integer> costMsg, NetworkNode sender) {
            Integer linkCost = NetworkNode.this.costs[nodeId][sender.getNodeId()];

            Iterator<Integer> nodesIterator = costMsg.keySet().iterator();
            while (nodesIterator.hasNext()) {
                // get node id
                Integer nodeId = nodesIterator.next();
                if (nodeId != NetworkNode.this.nodeId) {

                    // get cost advertised by the sender
                    Integer advertisedCostToNode = costMsg.get(nodeId);

                    // calculate actual cost
                    Integer actualCost = advertisedCostToNode + linkCost;

                    // get current cost to said node
                    Integer currCostToNode = routeTable.getCost(nodeId);

                    HashMap<RouteTable.FIELD, Integer> entryValues = new HashMap<RouteTable.FIELD, Integer>(RouteTable.NUM_OF_FIELDS);
                    entryValues.put(RouteTable.FIELD.DEST, nodeId);
                    entryValues.put(RouteTable.FIELD.NEXT_HOP, sender.getNodeId());
                    entryValues.put(RouteTable.FIELD.COST, actualCost);
                    RouteTable.RouteTableEntry routeTableEntry = routeTable.new RouteTableEntry(entryValues);

                    // currCostToNOde !=null && !(advertisedCostToNode > currCostToNode)
                    if (currCostToNode != null) {
                        // entry exists
                        if (actualCost < currCostToNode) {
                            // update entry to reflect new cost and next_hop
                            routeTable.addOrUpdateRouteTableEntry(routeTableEntry);
                            System.out.println("Update route table 1");
                        }
                    } else {
                        // must create new entry
                        routeTable.addOrUpdateRouteTableEntry(routeTableEntry);
                        System.out.println("Update route table ");
                    }
                }
            }
        }
    }
}
