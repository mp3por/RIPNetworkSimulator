///**
// * Created by velin.
// * Tuk 6te staa cqlata rabota sas simuiraneto na dobavqne/mahane na linkove,
// * kakto i vseki NetworkNode 6te ima dostap do network i 6te polu4ava/priema suob6teniq,
// * nodovete 6te si imat taimer s koito da pra6tat subo6teniq,
// */
//public class Network {
//    protected final Integer[][] network; // -1 => no link, 0..INF link cost
//    protected final Integer numOfNodes;
//
//    public Network(Integer numOfNodes) {
//        this.numOfNodes = numOfNodes;
//
//        // instantiate empty network
//        network = new Integer[this.numOfNodes][this.numOfNodes];
//        for (int i = 0; i < this.numOfNodes; i++) {
//            for (int y = 0; y < numOfNodes; y++) {
//                network[i][y] = -1;
//            }
//        }
//    }
//
////    public void addNode(NetworkNode node, ) {
////        // TODO:handle collisions, should probably have a big input check in the beginning and make no checks later on
////        graph.put(node, connections);
////    }
//
//    public void addLink(NetworkNode fromNode, NetworkNode toNode, Integer cost) {
//        network[fromNode.getNodeId()][toNode.getNodeId()] = network[toNode.getNodeId()][fromNode.getNodeId()] = cost;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder b = new StringBuilder("Network:\n");
//        for (int i = 0; i < numOfNodes; i++) {
//            for (int y = 0; y < numOfNodes; y++) {
//                b.append("\t" + network[i][y] + " ");
//            }
//            b.append("\n");
//        }
//        return b.toString();
//    }
//
////    public Integer getLinkCost(NetworkNode fromNode, NetworkNode toNode) {
////
////    }
//
////    public ArrayList<NetworkLink> getLinksForNode(NetworkNode node) {
////        return graph.get(node);
////    }
//}
