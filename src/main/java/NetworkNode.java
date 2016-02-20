import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by velin.
 */
public class NetworkNode {

    /**
     * Node status
     */
    enum STATUS {
        ACTIVE, FAILED
    }

    /**
     * Node Id
     */
    protected final Integer nodeId;

    /**
     * Node routing table
     */
    private final RouteTable routeTable;

    /**
     * Node neighbours
     */
    private HashMap<NetworkNode, NetworkLink> neighbours;

    /**
     * Node current status
     */
    private STATUS status;

    /**
     * Constructor
     *
     * @param nodeId   node id
     * @param listener listener for routing table changes
     */
    public NetworkNode(int nodeId, RouteTable.NetworkNodeRouteTableListener listener) {
        this.nodeId = nodeId;
        this.routeTable = new RouteTable(nodeId, listener);
        this.status = STATUS.ACTIVE;
        this.neighbours = new HashMap<NetworkNode, NetworkLink>();
    }

    /**
     * Setter for neighbours
     *
     * @param neighbours the neighbours
     */
    public void setNeighbours(HashMap<NetworkNode, NetworkLink> neighbours) {
        this.neighbours = neighbours;
    }

    /**
     * Method for extracting routes from the routing table
     *
     * @return tuples (destId, routeTableEntry)
     */
    public HashMap<Integer, RouteTable.RouteTableEntry> getRoutesForAdvertising() {
        routeTable.reduceAllForgetCounters();
        return routeTable.getCosts();
    }

    /**
     * Getter for node Id
     *
     * @return this node id
     */
    public Integer getNodeId() {
        return nodeId;
    }

    /**
     * Is node active check
     *
     * @return true if node is active, false otherwise
     */
    public boolean isActive() {
        return status.equals(STATUS.ACTIVE);
    }

    /**
     * Method to be called by the simulator to tell the node to send routing table to neighbours
     */
    public void sendCostsToNeighbours() {
        for (NetworkNode neighbourNode : neighbours.keySet()) {
            neighbourNode.handleCostsMsg(this);
        }
    }

    /**
     * Method to be called by a neighbour node which is sending you it's routing table
     *
     * @param sender the node that initiated the
     */
    private synchronized void handleCostsMsg(NetworkNode sender) {

        // get senders routes
        HashMap<Integer, RouteTable.RouteTableEntry> sendersRoutes = sender.getRoutesForAdvertising();

        // notify route table that sender has contacted
        routeTable.nodeHasContacted(sender);

        // get link cost
//        Integer linkCost = links[nodeId][sender.getNodeId()].cost;
        Integer linkCost = neighbours.get(sender).cost;

        if (linkCost.equals(-1)) {// link is down
            // remove from neighbours as per assignment
            neighbours.remove(sender);
        } else {// link is up and running
            // iterate over msg and check new routes
            Iterator<Integer> nodesIterator = sendersRoutes.keySet().iterator();
            while (nodesIterator.hasNext()) {
                // get node id
                Integer destinationId = nodesIterator.next();
                if (!destinationId.equals(this.nodeId)) {
                    RouteTable.RouteTableEntry sendersRouteForDest = sendersRoutes.get(destinationId);

                    // get cost advertised by the sender
                    Integer advertisedCostToNode = sendersRouteForDest.getCost();

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

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("NetworkNode " + nodeId + ":\n");
        b.append("neighbours: [");
        for (NetworkNode networkNode : neighbours.keySet()) {
            b.append(networkNode.getNodeId() + " : " + neighbours.get(networkNode) + ";");
        }
        b.append("]\n");
        b.append(routeTable.toString());
        return b.toString();
    }
}
