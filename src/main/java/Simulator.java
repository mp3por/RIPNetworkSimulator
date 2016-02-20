import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by velin.
 */
public class Simulator implements RouteTable.NetworkNodeRouteTableListener {
    public static final Integer DEFAULT_NUM_OF_ITERATIONS = 100;

    private final NetworkLink[][] links;
    private final int numOfNodes;

    private final HashMap<Integer, NetworkNode> nodesMap;

    private final Integer numOfIterations;
    private final HashMap<Integer, HashSet<ScheduledNetworkEvent>> scheduledEvents;
    private final HashMap<Integer, ArrayList<Integer>> connectionsMap;
    private boolean isStable = false;
    private boolean untilStability;

    public Simulator(NetworkLink[][] links, HashMap<Integer, ArrayList<Integer>> connectionsMap, Integer numOfIterations, boolean untilStability, HashMap<Integer, HashSet<ScheduledNetworkEvent>> scheduledEvents) {
        this.links = links;
        this.connectionsMap = connectionsMap;
        this.scheduledEvents = scheduledEvents;
        this.numOfIterations = numOfIterations != null ? numOfIterations : DEFAULT_NUM_OF_ITERATIONS;
        this.numOfNodes = this.links.length;
        this.untilStability = untilStability;
        this.nodesMap = new HashMap<Integer, NetworkNode>(numOfNodes);

        // instantiate nodes
        for (int i = 0; i < numOfNodes; i++) {
            NetworkNode node = new NetworkNode(i, this);
            nodesMap.put(i, node);
        }

        // set neighbours
        for (Integer nodeId : connectionsMap.keySet()) {
            NetworkNode node = nodesMap.get(nodeId);
            ArrayList<Integer> nodeConnectedNodesIDes = connectionsMap.get(nodeId);
            HashMap<NetworkNode, NetworkLink> neighboursMap = new HashMap<NetworkNode, NetworkLink>();
            for (Integer connectedNodeId : nodeConnectedNodesIDes) {
                NetworkNode neighbourNode = nodesMap.get(connectedNodeId);
                NetworkLink networkLink = links[node.getNodeId()][neighbourNode.getNodeId()];
                neighboursMap.put(neighbourNode, networkLink);
            }
            node.setNeighbours(neighboursMap);
        }

        printNodes();

        startSimulation();

    }

    public void printNodes() {
        StringBuilder b = new StringBuilder("nodes:\n");

        for (NetworkNode networkNode : nodesMap.values()) {
            b.append(networkNode + "\n");
        }


        System.out.println( b.toString());
    }

    private void startSimulation() {
        System.out.println("Starting simulation");
        printCosts();
        printStateOfNodes();
        printScheduledEvents();

        for (int currIteration = 1; currIteration <= numOfIterations; currIteration++) {
            System.out.println("--------------- Round " + currIteration + " -----------------");

            simulateNetworkExchange();
            if (isStable) {
                if (untilStability) {
                    System.out.println("Stability reached after iteration: " + (currIteration - 1));
                    break;
                } else {
                    System.out.println();
                    System.out.println("Network stable for this round");
                    System.out.println();
//                    printStateOfNodes();
                }

            } else {
                printStateOfNodes();
            }
            simulateNetworkEvents(currIteration);

            System.out.println("--------------- End round " + currIteration + " -----------------------------------");
            System.out.println();
            System.out.println();
        }
    }

    private void simulateNetworkEvents(int currIteration) {
        HashSet<ScheduledNetworkEvent> scheduledNetworkEvents = scheduledEvents.get(currIteration);
        if (scheduledNetworkEvents != null) {
            System.out.println("ScheduledEvents after exchange " + currIteration);
            for (ScheduledNetworkEvent event : scheduledNetworkEvents) {
                event.executeEvent();
            }
            printCosts();
        }
    }

    private void simulateNetworkExchange() {
        isStable = true;
        for (NetworkNode node : nodesMap.values()) {
            if (node.isActive()) {
                node.sendCostsToNeighbours();
            }
        }
    }

    public void printCosts() {
        StringBuilder b = new StringBuilder("links:\n");
        for (int i = 0; i < numOfNodes; i++) {
            for (int y = 0; y < numOfNodes; y++) {
                b.append("\t" + links[i][y]);
            }
            b.append("\n");
        }
        System.out.println(b.toString());
    }

    public void printScheduledEvents() {
        StringBuilder b = new StringBuilder("Scheduled events:\n");
        ArrayList<Integer> sortedKeys = new ArrayList<Integer>(scheduledEvents.keySet());
        for (Integer key : sortedKeys) {
            HashSet<ScheduledNetworkEvent> scheduledNetworkEvents = scheduledEvents.get(key);
            if (scheduledNetworkEvents != null) {
                b.append("\t" + key + " : [");
                for (ScheduledNetworkEvent event : scheduledNetworkEvents) {
                    b.append(event.toString() + "; ");
                }
                b.append("]");
            }
            b.append("\n");
        }
        System.out.println(b.toString());
    }

    public void printStateOfNodes() {
        StringBuilder b = new StringBuilder("State of nodes:\n");
        for (NetworkNode node : nodesMap.values()) {
            b.append(node.toString());
        }
        System.out.println(b.toString());
    }


    public void onRouteTableUpdate(Integer nodeId) {
        isStable = false;
    }
}
