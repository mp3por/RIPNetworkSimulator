import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by velin.
 */
public class Simulator implements NetworkNode.NetworkNodeRouteTableListener {
    public static final Integer DEFAULT_NUM_OF_ITERATIONS = 100;

    private final Integer[][] costs;
    private final int numOfNodes;
    private final ArrayList<NetworkNode> nodes;
    private final Integer numOfIterations;
    private final HashMap<NetworkNode, HashSet<NetworkNode>> nodeConnectionsMap;
    private boolean isStable;
    private boolean untilStability;

    public Simulator(Integer[][] costs, Integer numOfIterations, boolean untilStability) {
        this.costs = costs;
        this.numOfIterations = numOfIterations != null ? numOfIterations : DEFAULT_NUM_OF_ITERATIONS;
        this.numOfNodes = this.costs.length;
        this.nodes = new ArrayList<NetworkNode>(numOfNodes);
        this.nodeConnectionsMap = new HashMap<NetworkNode, HashSet<NetworkNode>>();
        this.isStable = false;
        this.untilStability = untilStability;

        for (int i = 0; i < numOfNodes; i++) {
            nodes.add(new NetworkNode(i, costs, this));
        }

        for (int i = 0; i < numOfNodes; i++) {
            NetworkNode networkNode = nodes.get(i);
            HashSet<NetworkNode> connections = new HashSet<NetworkNode>();
            for (int y = 0; y < numOfNodes; y++) {
                if (costs[i][y] > 0) {
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

        for (int currIteration = 0; currIteration < numOfIterations; currIteration++) {

            if (untilStability && isStable) {
                System.out.println("Stability reached after iteration: " + currIteration);
                break;
            }
            System.out.println("--------------- Round " + currIteration + " -----------------");

            isStable = true;
            for (NetworkNode node : nodes) {
                HashMap<Integer, Integer> nodeCostsMsg = node.getCostsMsg();
                HashSet<NetworkNode> nodeConnections = nodeConnectionsMap.get(node);
                for (NetworkNode connectedNode : nodeConnections) {
                    connectedNode.receiveCostsMsg(nodeCostsMsg, node);
                }
            }
            printStateOfNodes();

            System.out.println("--------------- End round -----------------------------------");
            System.out.println();
            System.out.println();
        }


    }

    public void printCosts() {
        StringBuilder b = new StringBuilder("costs:\n");
        for (int i = 0; i < numOfNodes; i++) {
            for (int y = 0; y < numOfNodes; y++) {
                b.append("\t" + costs[i][y]);
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
