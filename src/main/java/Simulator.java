import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by velin.
 */
public class Simulator {
    // fields
    private final JSONObject config; // configuration (just in case)
    private final HashMap<Integer, NetworkNode> nodes; // list of nodes
    private final Network network; // network
    private int numOfNodes = 0;
    private Integer[][] linkCosts;
    private final int iterations;

    public Simulator(JSONObject config) {
        // instantiate fields
        this.config = config;




        this.numOfNodes = this.config.getInt("numOfNodes");
        this.iterations = this.config.getInt("iterations");
        this.network = new Network(this.numOfNodes);
        this.nodes = new HashMap<Integer, NetworkNode>();

        // TODO: validate input data

//        // fill the network with nodes
//        fillNetwork();
//
//        // start simulation
//        startSimulation();
    }

    private void startSimulation() {
        printState();


        System.out.println("Starting simulation");
        for (int currIteration = 0; currIteration < iterations; currIteration++) {
            System.out.println("--------------- Round " + currIteration + " -----------------");
//            for (NetworkNode node : nodes.values()) {
//                HashMap<Integer, Integer> costsMsg = node.getCostsMsg();
//                ArrayList<NetworkLink> linksForNode = network.getLinksForNode(node);
//                for (NetworkLink link : linksForNode) {
//                    link.toNode.receiveCostMsg(costsMsg, node.nodeId);
//                }
//
//                printState();
//            }
            System.out.println("--------------- End round -----------------------------------");
            System.out.println();
            System.out.println();
        }
    }

    private void printState() {
        System.out.println("Printing state of simulation");
        System.out.println("network:\n");
        System.out.println("\t" + network.toString());
        System.out.println("indvidual nodes:\n");
        ArrayList<Integer> nodesIds = new ArrayList<Integer>(nodes.keySet());
        for (Integer nodeId : nodesIds) {
            NetworkNode networkNode = nodes.get(nodeId);
            System.out.println(networkNode.toString());
        }
    }

    private void fillNetwork() {
        // get costs
        linkCosts = parseLinkCosts();

        // get graph description
        JSONObject graphJSON = config.getJSONObject("graph");

        // iterate over the nodes
        Iterator<String> nodesIterator = graphJSON.keys();
        while (nodesIterator.hasNext()) {
            // get node ID as string
            String nodeIdString = nodesIterator.next();

            // get node connection
            JSONArray nodeConnectionsJSONArray = graphJSON.getJSONArray(nodeIdString);

            // get or create node
            NetworkNode node = getOrRegisterNodeForSimulation(Integer.valueOf(nodeIdString));

            // iterate over connections and append to list of connections
            int numOfConnections = nodeConnectionsJSONArray.length();
            ArrayList<NetworkLink> nodeConnections = new ArrayList<NetworkLink>();
            if (numOfConnections > 0) {
                nodeConnections = new ArrayList<NetworkLink>(numOfConnections);
                for (int i = 0; i < numOfConnections; i++) {
                    NetworkNode connectionNode = getOrRegisterNodeForSimulation(nodeConnectionsJSONArray.getInt(i));
                    NetworkLink link = new NetworkLink(node, connectionNode, linkCosts[node.getNodeId()][connectionNode.getNodeId()]);
                    nodeConnections.add(link);
                }
            }

            // add node to network
            network.addNode(node, nodeConnections);
        }
    }

    private Integer[][] parseLinkCosts() {
        // extract JSON array representation
        JSONArray costsJSONArray = config.getJSONArray("costs_between_nodes");
        numOfNodes = costsJSONArray.length();

        // Instantiate the variable
        Integer[][] costs = new Integer[numOfNodes][numOfNodes];

        // fill in the costs
        for (int i = 0; i < numOfNodes; i++) {
            JSONArray innerCostsJSONArray = costsJSONArray.getJSONArray(i);
            for (int y = 0; y < numOfNodes; y++) {
                int cost = innerCostsJSONArray.getInt(y);
                costs[i][y] = cost;
            }
        }
        return costs;
    }


    private NetworkNode registerNodeInSimulation(int nodeId) {
        NetworkNode newNode = new NetworkNode(nodeId, network);
        nodes.put(nodeId, newNode);
        return newNode;
    }

    private NetworkNode getOrRegisterNodeForSimulation(int nodeId) {
        NetworkNode node = nodes.get(nodeId);
        if (node == null) {
            node = registerNodeInSimulation(nodeId);
        }
        return node;
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Simulator:\nnodes= [");
        Iterator<Integer> nodesIterator = nodes.keySet().iterator();
        while (nodesIterator.hasNext()) {
            Integer nodeId = nodesIterator.next();
            b.append(nodes.get(nodeId).toString() + ", ");
        }
        b.append("],\nnetwork = " + network.toString());
        b.append(",\nlink_costs:\n");

        for (int i = 0; i < numOfNodes; i++) {
            Integer[] innerLinkCost = linkCosts[i];
            for (int y = 0; y < numOfNodes; y++) {
                Integer cost = innerLinkCost[y];
                b.append(cost + " ");
            }
            b.append("\n");
        }

        return b.toString();
    }
}
