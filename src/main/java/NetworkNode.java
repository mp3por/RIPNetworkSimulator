import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by velin.
 */
public class NetworkNode {
    protected final Integer nodeId;
    protected final NetworkLink[][] links;
    private final RouteTable routeTable;
    private final DistanceVectorRoutingAlg routeAlg;

    public NetworkNode(int nodeId, NetworkLink[][] links, NetworkNodeRouteTableListener listener) {
        this.nodeId = nodeId;
        this.links = links;
        this.routeTable = new RouteTable(nodeId, listener);
        this.routeAlg = new DistanceVectorRoutingAlg();
    }

    public HashMap<Integer, Integer> getCostsMsg() {
        routeTable.reduceAllForgetCounters();
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

    public interface NetworkNodeRouteTableListener {
        void onRouteTableUpdate(Integer nodeId);
    }

    public class DistanceVectorRoutingAlg {

        public synchronized void handleCostMsg(HashMap<Integer, Integer> costMsg, NetworkNode sender) {
            Integer linkCost = NetworkNode.this.links[nodeId][sender.getNodeId()].cost;

            if (linkCost > 0) { // link exists
                // notify route table that sender has contacted
                routeTable.nodeHasContacted(sender);

                // iterate over msg and check new routes
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

                        RouteTable.RouteTableEntry routeTableEntry = routeTable.new RouteTableEntry(nodeId, actualCost, sender.getNodeId());

                        // currCostToNOde !=null && !(advertisedCostToNode > currCostToNode)
                        if (currCostToNode != null) {
                            // entry exists
                            if (actualCost < currCostToNode) {
                                // update entry to reflect new cost and next_hop
                                routeTable.addOrUpdateRouteTableEntry(routeTableEntry);
                            }
                        } else {
                            // must create new entry
                            routeTable.addOrUpdateRouteTableEntry(routeTableEntry);
                        }
                    }
                }
            }
        }
    }
}
