import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by velin.
 */
public class NetworkNode {

    enum STATUS {
        ACTIVE, FAILED
    }

    protected final Integer nodeId;
    protected final NetworkLink[][] links;
    private final RouteTable routeTable;
    protected STATUS status;

    public NetworkNode(int nodeId, NetworkLink[][] links, NetworkNodeRouteTableListener listener, STATUS currentStatus) {
        this.nodeId = nodeId;
        this.links = links;
        this.routeTable = new RouteTable(nodeId, listener);
        this.status = currentStatus;
    }

    public HashMap<Integer, Integer> getCostsMsg() {
        routeTable.reduceAllForgetCounters();
        return routeTable.getCosts();
    }

    public void receiveCostsMsg(HashMap<Integer, Integer> receivedCosts, NetworkNode sender) {
        if (status.equals(STATUS.ACTIVE)) {
            handleCostsMsg(receivedCosts, sender);
        }
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

    public boolean isActive() {
        return status.equals(STATUS.ACTIVE);
    }


    private synchronized void handleCostsMsg(HashMap<Integer, Integer> costMsg, NetworkNode sender) {

        // get link cost
        Integer linkCost = links[nodeId][sender.getNodeId()].cost;

        // notify route table that sender has contacted
        routeTable.nodeHasContacted(sender);

        // iterate over msg and check new routes
        Iterator<Integer> nodesIterator = costMsg.keySet().iterator();
        while (nodesIterator.hasNext()) {
            // get node id
            Integer destinationId = nodesIterator.next();
            if (!destinationId.equals(this.nodeId)) {

                // get cost advertised by the sender
                Integer advertisedCostToNode = costMsg.get(destinationId);

                // calculate actual cost
                Integer newCost = advertisedCostToNode + linkCost;

                // get current cost to said node
                Integer currCostToNode = routeTable.getCost(destinationId);

                if (newCost < currCostToNode) {
                    // log new cost and sender
                    routeTable.logDestCost(destinationId, newCost, sender);
                } else if (newCost > currCostToNode) {
                    // check for link cost change
                    Integer routeNextHopForDest = routeTable.getRouteNextHopForDest(destinationId);
                    if (routeNextHopForDest != null && routeNextHopForDest.equals(sender.getNodeId())) {
                        // save new cost even though it is bigger than the current cost because it was
                        // advertised by the same node which advertised the current cost meaning that
                        // the link cost has changed for the worse
                        routeTable.logDestCost(destinationId, newCost, sender);
                    }
                }
            }
        }
    }

}
