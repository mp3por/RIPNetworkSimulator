import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class RouteTable {
    public static final Integer INFINITY_COST = 16;
    public static final Integer FORGET_AFTER_DEFAULT = 16;
    private final NetworkNode.NetworkNodeRouteTableListener listener;


    private final HashMap<Integer, RouteTableEntry> routeTable;
    private final int nodeId;

    public RouteTable(int nodeId, NetworkNode.NetworkNodeRouteTableListener listener) {
        this.nodeId = nodeId;
        this.listener = listener;
        this.routeTable = new HashMap<Integer, RouteTableEntry>();

        RouteTableEntry routeToSelf = new RouteTableEntry(this.nodeId, 0, null);
        routeTable.put(nodeId, routeToSelf);
    }

    public Integer getCost(Integer nodeId) {
        RouteTableEntry routeTableEntry = routeTable.get(nodeId);
        if (routeTableEntry != null) {
            return routeTableEntry.getCost();
        }
        return null;
    }

    public void addOrUpdateRouteTableEntry(RouteTableEntry entry) {
        // TODO: handle same key error
        Integer dest = entry.getDest();
        routeTable.put(dest, entry);
        listener.onRouteTableUpdate(nodeId);
    }

    public HashMap<Integer, Integer> getCosts() {
        HashMap<Integer, Integer> costs = new HashMap<Integer, Integer>(routeTable.size());

        Iterator<Integer> routeIterator = routeTable.keySet().iterator();
        while (routeIterator.hasNext()) {
            RouteTableEntry routeTableEntry = routeTable.get(routeIterator.next());
            Integer cost = routeTableEntry.getCost();
            Integer dest = routeTableEntry.getDest();
            costs.put(dest, cost);
        }
        return costs;
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

    public void reduceAllForgetCounters() {
        HashSet<RouteTableEntry> toBeRemoved = new HashSet<RouteTableEntry>();
        for (RouteTableEntry entry : routeTable.values()) {
            entry.reduceForgetCounter();
            if (entry.shouldForget()) {
                toBeRemoved.add(entry);
            }
        }
        for (RouteTableEntry entry : toBeRemoved) {
            routeTable.remove(entry);
        }
    }

    public void nodeHasContacted(NetworkNode sender) {
        RouteTableEntry routeTableEntry = routeTable.get(sender.getNodeId());
        if (routeTableEntry != null) {
            routeTableEntry.resetForgetCounter();
        }
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
    }

}
