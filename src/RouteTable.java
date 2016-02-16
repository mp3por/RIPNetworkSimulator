import java.util.HashMap;

public class RouteTable {
    public static final Integer INFINITY_COST = 16;

    enum FIELD {
        DEST(0, "Destination"), COST(1, "Cost"), NEXT_HOP(2, "Next hop");
        private int index;
        private String verboseName;

        FIELD(int index, String verboseName) {
            this.index = index;
            this.verboseName = verboseName;
        }
    }

    private final HashMap<Integer, RouteTableEntry> routeTable;
    private final int nodeId;

    public RouteTable(int nodeId) {
        this.nodeId = nodeId;
        this.routeTable = new HashMap<Integer, RouteTableEntry>();

        HashMap<FIELD, Integer> entryValues = new HashMap<FIELD, Integer>();
        entryValues.put(FIELD.DEST, nodeId);
        entryValues.put(FIELD.COST, 0);
        entryValues.put(FIELD.NEXT_HOP, null);

        RouteTableEntry routeToSelf = new RouteTableEntry(entryValues);
        routeTable.put(nodeId, routeToSelf);
    }

    public void addRouteTableEntry (RouteTableEntry entry){
        // TODO: handle same key error
        Integer dest = entry.getDest();
        routeTable.put(dest, entry);
    }


    public class RouteTableEntry {

        private HashMap<FIELD, Integer> fieldValues;

        public RouteTableEntry(HashMap<FIELD, Integer> entryValues) {
            // TODO: check values and raise error
            this.fieldValues = new HashMap<FIELD, Integer>(FIELD.values().length);
            for (FIELD field : FIELD.values()) {
                Integer fieldValue = entryValues.get(field);
                this.fieldValues.put(field, fieldValue);
            }
        }

        public Integer getDest(){
            return fieldValues.get(FIELD.DEST);
        }

    }


}