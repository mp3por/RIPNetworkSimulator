import java.util.*;

/**
 * Class representing a routing table.
 */
public class RouteTable {

    /**
     * Interface for the routing table listeners.
     */
    public interface NetworkNodeRouteTableListener {
        void onRouteTableUpdate(Integer nodeId);
    }

    /**
     * Default infinity cost
     */
    public static final Integer INFINITY_COST_DEFAULT = 16;

    /**
     * Default number of exchanges that need to pass without any update for a node for the routing table to forget it.
     */
    public static final Integer FORGET_AFTER_DEFAULT = 4;

    /**
     * Failed link cost
     */
    public static final Integer FAILED_LINK_COST = -1;

    /**
     * Listener for route table changes
     */
    private final NetworkNodeRouteTableListener listener;

    /**
     * route table map. It is indexed by the destination's node ID and holds a {@link RouteTableEntry RouteTableEntry} object
     */
    private final HashMap<Integer, RouteTableEntry> routeTable;

    /**
     * The id of the node this routing table belongs to.
     */
    private final int nodeId;

    /**
     * Infinity cost
     */
    private final int infinityCost;

    /**
     * Constructor
     *
     * @param nodeId       {@link #nodeId}
     * @param listener     {@link #listener}
     * @param infinityCost {@link #infinityCost}
     */
    public RouteTable(int nodeId, NetworkNodeRouteTableListener listener, int infinityCost) {
        this.nodeId = nodeId;
        this.listener = listener;
        this.infinityCost = infinityCost;
        this.routeTable = new HashMap<Integer, RouteTableEntry>();

        // creates the default entryo to self
        RouteTableEntry routeToSelf = new RouteTableEntry(this.nodeId, 0, null);
        routeTable.put(nodeId, routeToSelf);
    }

    /**
     * Helper method to get currently logged cost for a destination node
     *
     * @param destinationId the id of the destination node
     * @return the currently logged cost for the route to the destination node or {@link #infinityCost} if it does not know
     */
    public Integer getCost(Integer destinationId) {
        RouteTableEntry routeTableEntry = routeTable.get(destinationId);
        if (routeTableEntry != null) {
            routeTableEntry.resetForgetCounter();
            return routeTableEntry.getCost();
        }
        return infinityCost;
    }

    /**
     * Helper method for getting the all costs to all destinations registered in the routing table
     *
     * @return map indexed by the destination node id holding route table entries
     */
    public HashMap<Integer, RouteTableEntry> getCosts() {
        HashMap<Integer, RouteTableEntry> costs = new HashMap<Integer, RouteTableEntry>(routeTable.size());

        Iterator<Integer> routeIterator = routeTable.keySet().iterator();
        while (routeIterator.hasNext()) {
            RouteTableEntry routeTableEntryCopy = routeTable.get(routeIterator.next()).copy();
            costs.put(routeTableEntryCopy.getDest(), routeTableEntryCopy);
        }
        return costs;
    }

    /**
     * Helper method for removing a neighbour.
     * It removes the neighbour from the routing table as well as any routes that have been learned from the neighbour
     *
     * @param neighbourId
     */
    public void removeNeighbour(Integer neighbourId) {
        // for clarity
        String msg = "Node " + this.nodeId + " remove neighbour " + neighbourId;

        // remove the neighbouring node
        removeEntryForDest(neighbourId, msg);

        // find and remove all entries learned from the neighbour
        HashSet<RouteTableEntry> toBeRemoved = new HashSet<RouteTableEntry>(); // holds all the entries which are to be removed
        for (RouteTableEntry entry : getEntriesWithoutSelf()) {
            if (entry.getNextHop().equals(neighbourId)) {
                // entry learned from the neighbour => remove entry
                toBeRemoved.add(entry);

                // for clarity
                System.out.println("Node " + this.nodeId + " will remove entry because it came from neightbor " + neighbourId + " : " + entry);
            }
        }
        removeEntries(toBeRemoved);
    }

    /**
     * Helper method for getting all entries without the default entry.
     *
     * @return set of all entries
     */
    private HashSet<RouteTableEntry> getEntriesWithoutSelf() {
        HashSet<RouteTableEntry> entries = new HashSet<RouteTableEntry>();
        for (RouteTableEntry entry : routeTable.values()) {
            if (!entry.getDest().equals(nodeId)) {
                entries.add(entry);
            }
        }
        return entries;
    }

    /**
     * Helper method for removing entries from the routing table
     *
     * @param toBeRemoved set of nodes to be removed
     */
    private void removeEntries(HashSet<RouteTableEntry> toBeRemoved) {
        if (toBeRemoved.size() > 0) {
            for (RouteTableEntry entry : toBeRemoved) {
                removeEntryForDest(entry.getDest(), null);
            }
        }
    }

    /**
     * Helper method for removing an entry for a particular destination
     *
     * @param nodeId the destination node id
     * @param reason the reason for the removal
     */
    private void removeEntryForDest(Integer nodeId, String reason) {
        RouteTableEntry removedEntry = routeTable.remove(nodeId);
        if (removedEntry != null) {
            // notify the listener if an entry was really removed
            listener.onRouteTableUpdate(this.nodeId);

            // print reason
            if (reason != null && !reason.equals("")) {
                System.out.println(reason);
            }
        }
    }

    /**
     * Helper method for reducing the forget counters for the route table entries
     */
    public void reduceAllForgetCounters() {
        HashSet<RouteTableEntry> toBeRemoved = new HashSet<RouteTableEntry>();
        for (RouteTableEntry entry : routeTable.values()) {
            entry.reduceForgetCounter();
            if (entry.shouldForget()) {
                System.out.println("Node " + nodeId + " removing timeout entry: " + entry);
                toBeRemoved.add(entry);
            }
        }
        removeEntries(toBeRemoved);
    }

    /**
     * Helper method for logging route towards a particular destination
     *
     * @param destinationId the id of the route destination
     * @param newCost       the cost of the route
     * @param sender        the node which advertised the route
     */
    public void logDestCost(Integer destinationId, Integer newCost, NetworkNode sender) {
        // create route table entry
        RouteTableEntry entry = new RouteTableEntry(destinationId, newCost, sender.getNodeId());

        // record the new entry ( override old value if exists )
        routeTable.put(destinationId, entry);

        // notify listener for change in routing table
        listener.onRouteTableUpdate(nodeId);

        // for clarity
        System.out.println("Node " + nodeId + " logging " + entry);
    }

    /**
     * Helper method for getting the next node on a route for a destination
     *
     * @param destinationId the destination node id
     * @return the id of the next-on-the-route node or null if it does not know
     */
    public Integer getNextHopTowardsDest(Integer destinationId) {
        RouteTableEntry entry = routeTable.get(destinationId);
        if (entry != null) {
            return entry.nextNodeId;
        }
        return null;
    }

    /**
     * Helper method for getting the next node on a route for a destination
     *
     * @param toNode destination node
     * @return the id of the next-on-the-route node or null if it does not know
     */
    public Integer getNextHopTowardsDest(NetworkNode toNode) {
        return getNextHopTowardsDest(toNode.getNodeId());
    }

    /**
     * Getter for infinity cost
     *
     * @return the infinity cost of the routing table
     */
    public int getInfinityCost() {
        return infinityCost;
    }

    /**
     * Helper method for dropping a route
     *
     * @param destinationId destination node id
     */
    public void dropRoute(Integer destinationId) {
        RouteTableEntry entry = routeTable.get(destinationId);
        if (entry != null) {
            // for clarity
            String msg = "Node " + nodeId + " dropping route for dest (" + destinationId + ") because infinity (" + infinityCost + ") was reached.";

            // remove
            removeEntryForDest(entry.getDest(), msg);
        }
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("RouteTable for node : " + nodeId + "\n");
        b.append("\tdest cost next\n");
        Object[] routesDest = routeTable.keySet().toArray();
        Arrays.sort(routesDest);
        for (int i = 0; i < routesDest.length; i++) {
            Integer routesDestNodeId = (Integer) routesDest[i];
            RouteTableEntry routeTableEntry = routeTable.get(routesDestNodeId);
            String format = String.format("\t%4d%5d%5d\n", routeTableEntry.getDest(), routeTableEntry.getCost(), routeTableEntry.getNextHop());
            b.append(format);
        }

        return b.toString();
    }

    /**
     * Class which represents a route table entry
     */
    public class RouteTableEntry {

        /**
         * The destination of the entry.
         */
        private final Integer destNodeId;

        /**
         * The current cost of the route for the destination.
         */
        private Integer cost;

        /**
         * The next hop on the route for the destination.
         */
        private Integer nextNodeId;

        /**
         * Forget counter
         */
        private Integer forgetCounter = FORGET_AFTER_DEFAULT;

        /**
         * Constructor
         *
         * @param destNodeId the destination id {@link #destNodeId}
         * @param cost       the cost of the route {@link #cost}
         * @param nextNodeId the next hop node id {@link #nextNodeId}
         */
        public RouteTableEntry(Integer destNodeId, Integer cost, Integer nextNodeId) {
            this.destNodeId = destNodeId;
            this.cost = cost;
            this.nextNodeId = nextNodeId;
        }

        /**
         * Helper method for reducing the forget counter for the entry.
         */
        public void reduceForgetCounter() {
            if (!destNodeId.equals(nodeId))
                forgetCounter--;
        }

        /**
         * Helper method for resetting the forget counter.
         */
        public void resetForgetCounter() {
            forgetCounter = FORGET_AFTER_DEFAULT;
        }

        public Integer getDest() {
            return destNodeId;
        }

        public Integer getCost() {
            return cost;
        }

        public Integer getNextHop() {
            return nextNodeId;
        }

        public boolean shouldForget() {
            return forgetCounter <= 0;
        }

        @Override
        public String toString() {
            return "RouteEntry{ dest: " + destNodeId + ", cost: " + cost + ", next:" + nextNodeId + "}";
        }

        /**
         * Helper method for copying the entry
         *
         * @return a copy of the entry
         */
        public RouteTableEntry copy() {
            return new RouteTableEntry(this.destNodeId, this.cost, this.nextNodeId);
        }
    }

}
