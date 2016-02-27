import java.util.*;

public class RouteTable {

    public interface NetworkNodeRouteTableListener {
        void onRouteTableUpdate(Integer nodeId);
    }

    public static final Integer INFINITY_COST_DEFAULT = 16;
    public static final Integer FORGET_AFTER_DEFAULT = 4;
    public static final Integer FAILED_LINK_COST = -1;


    private final NetworkNodeRouteTableListener listener;
    private final HashMap<Integer, RouteTableEntry> routeTable;
    private final int nodeId;
    private final int infinityCost;

    public RouteTable(int nodeId, NetworkNodeRouteTableListener listener, int infinityCost) {
        this.nodeId = nodeId;
        this.listener = listener;
        this.infinityCost = infinityCost;
        this.routeTable = new HashMap<Integer, RouteTableEntry>();

        RouteTableEntry routeToSelf = new RouteTableEntry(this.nodeId, 0, null);
        routeTable.put(nodeId, routeToSelf);
    }

    public Integer getCost(Integer nodeId) {
        RouteTableEntry routeTableEntry = routeTable.get(nodeId);
        if (routeTableEntry != null) {
            routeTableEntry.resetForgetCounter();
            return routeTableEntry.getCost();
        }
        return infinityCost;
    }

    public HashMap<Integer, RouteTableEntry> getCosts() {
        HashMap<Integer, RouteTableEntry> costs = new HashMap<Integer, RouteTableEntry>(routeTable.size());

        Iterator<Integer> routeIterator = routeTable.keySet().iterator();
        while (routeIterator.hasNext()) {
            RouteTableEntry routeTableEntryCopy = routeTable.get(routeIterator.next()).copy();
            costs.put(routeTableEntryCopy.getDest(), routeTableEntryCopy);
        }
        return costs;
    }

    public void removeNeighbour(Integer nodeId) {
        System.out.println("Node " + this.nodeId + " remove neighbour " + nodeId);
        removeEntryForDest(nodeId);
        HashSet<RouteTableEntry> toBeRemoved = new HashSet<RouteTableEntry>();
        for (RouteTableEntry entry : getEntriesWithoutSelf()) {
            if (entry.getNextHop().equals(nodeId)) {
                toBeRemoved.add(entry);
            }
        }
        removeEntries(toBeRemoved);
    }

    private HashSet<RouteTableEntry> getEntriesWithoutSelf() {
        HashSet<RouteTableEntry> entries = new HashSet<RouteTableEntry>();
        for (RouteTableEntry entry : routeTable.values()) {
            if (!entry.getDest().equals(nodeId)) {
                entries.add(entry);
            }
        }
        return entries;
    }

    private void removeEntries(HashSet<RouteTableEntry> toBeRemoved) {
        if (toBeRemoved.size() > 0) {
            System.out.println("Node " + nodeId + " remove entries");
            for (RouteTableEntry entry : toBeRemoved) {
                System.out.println("Node " + nodeId + " removing timeout entry: " + entry);
                routeTable.remove(entry.getDest());
            }
        }
    }

    private void removeEntryForDest(Integer nodeId) {
        RouteTableEntry removedEntry = routeTable.remove(nodeId);
        if (removedEntry != null) {
            listener.onRouteTableUpdate(nodeId);
        }
    }

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

    public void logDestCost(Integer destinationId, Integer newCost, NetworkNode sender) {
        RouteTableEntry entry = new RouteTableEntry(destinationId, newCost, sender.getNodeId());
        System.out.println("Node " + nodeId + " logging " + entry);
        routeTable.put(destinationId, entry);
        listener.onRouteTableUpdate(nodeId);
    }

    public Integer getRouteNextHopForDest(Integer destinationId) {
        RouteTableEntry entry = routeTable.get(destinationId);
        if (entry != null) {
            return entry.nextNodeId;
        }
        return null;
    }

    public Integer getNextHopTowardsDest(NetworkNode toNode) {
        RouteTableEntry entry = routeTable.get(toNode.getNodeId());
        if (entry != null) {
            return entry.getNextHop();
        }
        return null;
    }

    public int getInfinityCost() {
        return infinityCost;
    }

    public void dropRoute(Integer destinationId) {
        RouteTableEntry entry = routeTable.get(destinationId);
        if (entry != null) {
            System.out.println("Node " + nodeId + " dropping route for dest (" + destinationId + ") because infinity (" + infinityCost + ") was reached.");
            listener.onRouteTableUpdate(nodeId);
            routeTable.remove(destinationId);
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

    public class RouteTableEntry {

        private final Integer destNodeId;
        private Integer cost;
        private Integer nextNodeId;
        private Integer forgetCounter = FORGET_AFTER_DEFAULT;

        public RouteTableEntry(Integer destNodeId, Integer cost, Integer nextNodeId) {
            // TODO: check values and raise error
            this.destNodeId = destNodeId;
            this.cost = cost;
            this.nextNodeId = nextNodeId;
        }

        public void reduceForgetCounter() {
            if (!destNodeId.equals(nodeId))
                forgetCounter--;
        }

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

        public boolean comesFromHost(Integer senderId) {
            return nextNodeId.equals(senderId);
        }

        public boolean isWorseThan(RouteTableEntry entry, Integer linkCost) {
            return cost > entry.cost + linkCost;
        }

        public boolean isBetterThan(RouteTableEntry entry, Integer linkCost) {
            return cost < entry.cost + linkCost;
        }

        @Override
        public String toString() {
            return "RouteEntry{ dest: " + destNodeId + ", cost: " + cost + ", next:" + nextNodeId + "}";
        }

        public RouteTableEntry copy() {
            return new RouteTableEntry(this.destNodeId, this.cost, this.nextNodeId);
        }
    }

}
