import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by velin.
 */
public class Simulator implements NetworkNode.NetworkNodeRouteTableListener {
    public static final Integer DEFAULT_NUM_OF_ITERATIONS = 100;

    private final NetworkLink[][] links;
    private final int numOfNodes;
    private final ArrayList<NetworkNode> nodes;
    private final Integer numOfIterations;
    private final HashMap<NetworkNode, HashSet<NetworkNode>> nodeConnectionsMap;
    private final HashMap<Integer, HashSet<ScheduledNetworkEvent>> scheduledEvents;
    private boolean isStable;
    private boolean untilStability;

    public Simulator(NetworkLink[][] links, Integer numOfIterations, boolean untilStability, HashMap<Integer, HashSet<ScheduledNetworkEvent>> scheduledEvents) {
        this.links = links;
        this.scheduledEvents = scheduledEvents;
        this.numOfIterations = numOfIterations != null ? numOfIterations : DEFAULT_NUM_OF_ITERATIONS;
        this.numOfNodes = this.links.length;
        this.nodes = new ArrayList<NetworkNode>(numOfNodes);
        this.nodeConnectionsMap = new HashMap<NetworkNode, HashSet<NetworkNode>>();
        this.isStable = false;
        this.untilStability = untilStability;

        for (int i = 0; i < numOfNodes; i++) {
            nodes.add(new NetworkNode(i, links, this, NetworkNode.STATUS.ACTIVE));
        }

        for (int i = 0; i < numOfNodes; i++) {
            NetworkNode networkNode = nodes.get(i);
            HashSet<NetworkNode> connections = new HashSet<NetworkNode>();
            for (int y = 0; y < numOfNodes; y++) {
                if (links[i][y].cost > 0) {
                    NetworkNode connectedNode = nodes.get(y);
                    connections.add(connectedNode);
                }
            }
            nodeConnectionsMap.put(networkNode, connections);
        }

        startSimulation();

    }

    public String printNodes() {
        StringBuilder b = new StringBuilder("nodes:");

        for (int i = 0; i < numOfNodes; i++) {
            b.append(nodes.get(i).nodeId + ", ");
        }
        return b.toString();
    }

    private void startSimulation() {
        System.out.println("Starting simulation");
        printCosts();
        printStateOfNodes();
        printScheduledEvents();

        for (int currIteration = 0; currIteration < numOfIterations; currIteration++) {

            if (untilStability && isStable) {
                System.out.println("Stability reached after iteration: " + currIteration);
                break;
            }
            System.out.println("--------------- Round " + currIteration + " -----------------");

            simulateNetworkExchange();
            if (isStable) {
                System.out.println();
                System.out.println("Network stable for this round");
                System.out.println();
//                printStateOfNodes();
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
        for (NetworkNode node : nodes) {
            if (node.isActive()) {
                HashMap<Integer, Integer> nodeCostsMsg = node.getCostsMsg();
                HashSet<NetworkNode> nodeConnections = nodeConnectionsMap.get(node);
                for (NetworkNode connectedNode : nodeConnections) {
                    if (links[node.getNodeId()][connectedNode.getNodeId()].cost >= 0) {
                        connectedNode.receiveCostsMsg(nodeCostsMsg, node);
                    }
                }
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
        for (NetworkNode node : nodes) {
            b.append(node.toString());
        }
        System.out.println(b.toString());
    }


    public void onRouteTableUpdate(Integer nodeId) {
        isStable = false;
    }
}
