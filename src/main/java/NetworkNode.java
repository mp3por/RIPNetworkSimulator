//import java.util.HashMap;
//import java.util.Iterator;
//
///**
// * Created by velin.
// */
//public class NetworkNode {
//
//    protected final Integer nodeId;
//    private final RouteTable routeTable;
//    private final DistanceVectorRoutingAlg routingAlg;
//    private final Network network;
//
//    public NetworkNode(Integer nodeId, Network network) {
//        this.nodeId = nodeId;
//        this.network = network;
//        this.routeTable = new RouteTable(this.nodeId);
//        this.routingAlg = new DistanceVectorRoutingAlg();
//    }
//
//    public synchronized HashMap<Integer, Integer> getCostsMsg() {
//        return routeTable.getCosts();
//    }
//
//    public synchronized void receiveCostMsg(HashMap<Integer, Integer> costMsg, Integer senderId) {
//        routingAlg.handleCostMsg(costMsg, senderId);
//    }
//
//    public Integer getNodeId() {
//        return nodeId;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder b = new StringBuilder("NetworkNode " + nodeId + "\n");
//        b.append("\t" + routeTable.toString());
//        return b.toString();
//    }
//
//
//    public class DistanceVectorRoutingAlg {
//
//        public synchronized void handleCostMsg(HashMap<Integer, Integer> costMsg, NetworkNode sender) {
//            System.out.println(nodeId + " handleCostMsg from " + sender.getNodeId());
//            Integer linkCost = network.getLinkCost(NetworkNode.this, sender);
//
//            Iterator<Integer> nodesIterator = costMsg.keySet().iterator();
//            while (nodesIterator.hasNext()) {
//                // get node id
//                Integer nodeId = nodesIterator.next();
//
//                // get cost advertised by the sender
//                Integer advertisedCostToNode = costMsg.get(nodeId);
//
//                // calculate actual cost
//                System.out.println("advertisedCostToNode: " + advertisedCostToNode + ", linkCost: " + linkCost);
//                Integer actualCost = advertisedCostToNode + linkCost;
//
//                // get current cost to said node
//                Integer currCostToNode = routeTable.getCost(nodeId);
//
//                // check
//                if (advertisedCostToNode < currCostToNode) {
//                    // update entry to reflect new cost and next_hop
//                    HashMap<RouteTable.FIELD, Integer> entryValues = new HashMap<RouteTable.FIELD, Integer>(RouteTable.NUM_OF_FIELDS);
//                    entryValues.put(RouteTable.FIELD.DEST, nodeId);
//                    entryValues.put(RouteTable.FIELD.NEXT_HOP, sender.getNodeId());
//                    entryValues.put(RouteTable.FIELD.COST, actualCost);
//                    routeTable.updateRoute(entryValues);
//                }
//            }
//        }
//    }
//}
