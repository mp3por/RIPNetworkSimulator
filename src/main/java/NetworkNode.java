import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by velin.
 */
public class NetworkNode {

    /**
     * split horizon flag
     */
    private boolean splitHorizon;

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
     * @param nodeId       node id
     * @param listener     listener for routing table changes
     * @param infinityCost
     */
    public NetworkNode(int nodeId, RouteTable.NetworkNodeRouteTableListener listener, boolean splitHorizon, Integer infinityCost) {
        this.nodeId = nodeId;
        this.routeTable = new RouteTable(nodeId, listener, infinityCost);
        this.status = STATUS.ACTIVE;
        this.neighbours = new HashMap<NetworkNode, NetworkLink>();
        this.splitHorizon = splitHorizon;
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
     * @param requester the requester of the routes
     * @return tuples (destId, routeTableEntry)
     */
    public HashMap<Integer, RouteTable.RouteTableEntry> getRoutesForAdvertising(NetworkNode requester) {
        HashMap<Integer, RouteTable.RouteTableEntry> costs = routeTable.getCosts();
        if (splitHorizon) {
            // remove entries learned from the requester
            Iterator<Integer> routesIterator = costs.keySet().iterator();
            while (routesIterator.hasNext()) {
                Integer destId = routesIterator.next();
                RouteTable.RouteTableEntry entry = costs.get(destId);
                Integer nextHop = entry.getNextHop();
                if (nextHop != null && nextHop.equals(requester.getNodeId())) {
                    routesIterator.remove();
                }
            }
            return costs;
        } else {
            return costs;
        }

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
        // check for broken links and update table/neighbours
        checkLinksAndRemoveDisconnectedNeighbours();

        // remove old entries from table
        routeTable.reduceAllForgetCounters();

        // "call-out" to all neighbours
        for (NetworkNode neighbourNode : neighbours.keySet()) {
            neighbourNode.handleCostsMsg(this);
        }
    }

    private void checkLinksAndRemoveDisconnectedNeighbours() {
        HashSet<NetworkNode> noLongerNeighbours = new HashSet<NetworkNode>();
        for (NetworkNode neighbourNode : neighbours.keySet()) {
            NetworkLink networkLink = neighbours.get(neighbourNode);
            if (networkLink == null || networkLink.cost < 0) {
                noLongerNeighbours.add(neighbourNode);
            }
        }
        removeNodesFromNeighbours(noLongerNeighbours);
    }

    private void removeNodesFromNeighbours(HashSet<NetworkNode> nodes) {
        for (NetworkNode node : nodes) {
            removeNodeFromNeighbours(node);
        }
    }

    private void removeNodeFromNeighbours(NetworkNode node) {
        // remove from neighbours
        neighbours.remove(node);

        // remove route table entry for the destination since you now do not know how to get to there
        routeTable.removeNeighbour(node.getNodeId());
    }

    /**
     * Method to be called by a neighbour node which is sending you it's routing table
     *
     * @param sender the node that initiated the
     */
    private synchronized void handleCostsMsg(NetworkNode sender) {
        // notify route table that sender has contacted
//        routeTable.nodeHasContacted(sender);
//        System.out.println(sender.nodeId + " sent routes to " + nodeId);
//        printNeighbours();

        // get link cost

        Integer linkCost = neighbours.get(sender).cost;
//        System.out.println("\tget linkCost between " + nodeId + " and " + sender.getNodeId() + " = " + linkCost);

        if (linkCost.equals(RouteTable.FAILED_LINK_COST)) {// link is down
            // remove from neighbours
            removeNodeFromNeighbours(sender);
        } else {// link is up and running
            // get routes
            HashMap<Integer, RouteTable.RouteTableEntry> sendersRoutes = sender.getRoutesForAdvertising(this);

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

                    if (newCost >= routeTable.getInfinityCost()) {
                        // infinity reached => drop route
                        routeTable.dropRoute(destinationId);
                    } else if (newCost < currCostToNode) {
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

    private void printNeighbours() {
        StringBuilder b = new StringBuilder("Neighbours for node (" + nodeId + "): [");
        for (NetworkNode networkNode : neighbours.keySet()) {
            b.append(networkNode.getNodeId() + " - " + neighbours.get(networkNode) + ", ");
        }
        b.append("]");
        System.out.println("neighbours for node " + nodeId + " : " + b.toString());
    }

    public void addNeighbour(NetworkNode connectedNode, NetworkLink networkLink) {
        neighbours.put(connectedNode, networkLink);
    }

    public boolean isNodeNeighbour(NetworkNode toNode) {
        return neighbours.keySet().contains(toNode);
    }

    public Integer getNextHopToDest(NetworkNode toNode) {
        return routeTable.getNextHopTowardsDest(toNode);
    }

    public void setSplitHorizon(boolean splitHorizon) {
        this.splitHorizon = splitHorizon;
    }

    public void printTable() {
        System.out.print(routeTable.toString());
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("NetworkNode " + nodeId + ":\n");
        b.append("neighbours: [");
        for (NetworkNode networkNode : neighbours.keySet()) {
            b.append("node" + networkNode.getNodeId() + " : cost " + neighbours.get(networkNode) + ";");
        }
        b.append("]\n");
        b.append(routeTable.toString());
        return b.toString();
    }
}
