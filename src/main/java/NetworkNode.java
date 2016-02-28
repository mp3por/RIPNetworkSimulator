import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Class representing a network node.
 */
public class NetworkNode {

    /**
     * split horizon flag
     */
    private boolean splitHorizon;

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
     * Constructor
     *
     * @param nodeId       integer node id
     * @param listener     listener for routing table changes
     * @param splitHorizon boolean if split-horizon is engaged or not
     * @param infinityCost cost of infinity
     */
    public NetworkNode(int nodeId, RouteTable.NetworkNodeRouteTableListener listener, boolean splitHorizon, Integer infinityCost) {
        this.nodeId = nodeId;
        this.routeTable = new RouteTable(nodeId, listener, infinityCost);
        this.neighbours = new HashMap<NetworkNode, NetworkLink>();
        this.splitHorizon = splitHorizon;
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

    /**
     * Checks all the links this node is part of in order to detect link failures
     */
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

    /**
     * Helper method for removing nodes from neighbours list
     *
     * @param nodes the nodes to be removed
     */
    private void removeNodesFromNeighbours(HashSet<NetworkNode> nodes) {
        for (NetworkNode node : nodes) {
            removeNodeFromNeighbours(node);
        }
    }

    /**
     * Helper method for removing node from neighbour list
     *
     * @param node the node to be removed
     */
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
        // get link cost
        Integer linkCost = neighbours.get(sender).cost;

        if (linkCost.equals(RouteTable.FAILED_LINK_COST)) {// link is down
            // remove from neighbours
            removeNodeFromNeighbours(sender);
        } else {// link is up and running
            // get routes
            HashMap<Integer, RouteTable.RouteTableEntry> sendersRoutes = sender.getRoutesForAdvertising(this);

            // iterate over msg and check new routes
            for (Integer destinationId : sendersRoutes.keySet()) {
                // get node id
                if (!destinationId.equals(this.nodeId)) {
                    // extract route table entry from senders routes table for node with id = destinationId
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
                        Integer routeNextHopForDest = routeTable.getNextHopTowardsDest(destinationId);
                        if (routeNextHopForDest != null && routeNextHopForDest.equals(sender.getNodeId())) {
                            // save new cost even though it is bigger than the current cost because it was
                            // advertised by the same node which is advertising the current cost
                            routeTable.logDestCost(destinationId, newCost, sender);
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper method for adding nodes as neighbours to this node
     *
     * @param connectedNode the neighbour node
     * @param networkLink   the link they share
     */
    public void addNeighbour(NetworkNode connectedNode, NetworkLink networkLink) {
        neighbours.put(connectedNode, networkLink);
    }

    /**
     * Helper method for find out the next hop towards a destination
     *
     * @param toNode destination node
     * @return {@link RouteTable#getNextHopTowardsDest(NetworkNode)}
     */
    public Integer getNextHopToDest(NetworkNode toNode) {
        return routeTable.getNextHopTowardsDest(toNode);
    }

    /**
     * Split-horizon flag setter
     *
     * @param splitHorizon split-horizon value
     */
    public void setSplitHorizon(boolean splitHorizon) {
        this.splitHorizon = splitHorizon;
    }

    /**
     * Helper method for printing this node's routing table
     */
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
