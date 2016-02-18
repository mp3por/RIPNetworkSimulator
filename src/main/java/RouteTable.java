//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.Iterator;
//
//public class RouteTable {
//    public static final Integer INFINITY_COST = 16;
//    public static final int NUM_OF_FIELDS = FIELD.values().length;
//
//    enum FIELD {
//        DEST(0, "Destination"), COST(1, "Cost"), NEXT_HOP(2, "Next hop");
//        private int index;
//        private String verboseName;
//
//        FIELD(int index, String verboseName) {
//            this.index = index;
//            this.verboseName = verboseName;
//        }
//    }
//
//    private final HashMap<Integer, RouteTableEntry> routeTable;
//    private final int nodeId;
//
//    public RouteTable(int nodeId) {
//        this.nodeId = nodeId;
//        this.routeTable = new HashMap<Integer, RouteTableEntry>();
//
//        HashMap<FIELD, Integer> entryValues = new HashMap<FIELD, Integer>();
//        entryValues.put(FIELD.DEST, nodeId);
//        entryValues.put(FIELD.COST, 0);
//        entryValues.put(FIELD.NEXT_HOP, null);
//
//        RouteTableEntry routeToSelf = new RouteTableEntry(entryValues);
//        routeTable.put(nodeId, routeToSelf);
//    }
//
//    public Integer getCost(Integer nodeId) {
//        RouteTableEntry routeTableEntry = routeTable.get(nodeId);
//        if (routeTableEntry != null) {
//            return routeTableEntry.getCost();
//        }
//        return null;
//    }
//
//    public void addRouteTableEntry(RouteTableEntry entry) {
//        // TODO: handle same key error
//        Integer dest = entry.getDest();
//        routeTable.put(dest, entry);
//    }
//
//    public void updateRoute(HashMap<FIELD, Integer> entryValues) {
//
//    }
//
//    public HashMap<Integer, Integer> getCosts() {
//        HashMap<Integer, Integer> costs = new HashMap<Integer, Integer>(routeTable.size());
//
//        Iterator<Integer> routeIterator = routeTable.keySet().iterator();
//        while (routeIterator.hasNext()) {
//            RouteTableEntry routeTableEntry = routeTable.get(routeIterator.next());
//            Integer cost = routeTableEntry.getCost();
//            Integer dest = routeTableEntry.getDest();
//            costs.put(dest, cost);
//        }
//        return costs;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder b = new StringBuilder("RouteTable for node : " + nodeId + "\n");
//        Object[] routesDest =  routeTable.keySet().toArray();
//        Arrays.sort(routesDest);
//        for (int i = 0; i < routesDest.length; i++) {
//            Integer routesDestNodeId = (Integer) routesDest[i];
//            RouteTableEntry routeTableEntry = routeTable.get(routesDestNodeId);
//            b.append("\t" + routeTableEntry.getDest() + " " + routeTableEntry.getCost() + " " + routeTableEntry.getNextHop() + "\n");
//        }
//
//        return b.toString();
//    }
//
//    public class RouteTableEntry {
//
//        private HashMap<FIELD, Integer> fieldValues;
//
//        public RouteTableEntry(HashMap<FIELD, Integer> entryValues) {
//            // TODO: check values and raise error
//            this.fieldValues = new HashMap<FIELD, Integer>(FIELD.values().length);
//            for (FIELD field : FIELD.values()) {
//                Integer fieldValue = entryValues.get(field);
//                this.fieldValues.put(field, fieldValue);
//            }
//        }
//
//        public Integer getDest() {
//            return fieldValues.get(FIELD.DEST);
//        }
//
//        public Integer getCost() {
//            return fieldValues.get(FIELD.COST);
//        }
//
//        public Integer getNextHop() {
//            return fieldValues.get(FIELD.NEXT_HOP);
//        }
//    }
//
//}
