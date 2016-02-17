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

    public Simulator(JSONObject config) {
        // hashmap for the network
        HashMap<NetworkNode, ArrayList<NetworkNode>> networkGraph = new HashMap<NetworkNode, ArrayList<NetworkNode>>();

        // instantiate fields
        this.config = config;
        this.nodes = new HashMap<Integer, NetworkNode>();
        this.network = new Network(networkGraph);

        // fill the network with nodes
        fillNetwork();
    }

    private void fillNetwork() {
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
            NetworkNode node = getOrCreateNode(Integer.valueOf(nodeIdString));

            // iterate over connections and append to list of connections
            int numOfConnections = nodeConnectionsJSONArray.length();
            ArrayList<NetworkNode> nodeConnections = null;
            if (numOfConnections > 0) {
                nodeConnections = new ArrayList<NetworkNode>(numOfConnections);
                for (int i = 0; i < numOfConnections; i++) {
                    nodeConnections.add(getOrCreateNode(nodeConnectionsJSONArray.getInt(i)));
                }
            }

            // add node to network
            network.addNode(node, nodeConnections);
        }
    }


    private NetworkNode registerNode(int nodeId) {
        NetworkNode newNode = new NetworkNode(nodeId);
        nodes.put(nodeId, newNode);
        return newNode;
    }

    private NetworkNode getOrCreateNode(int nodeId) {
        NetworkNode node = nodes.get(nodeId);
        if (node == null) {
            node = registerNode(nodeId);
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
        return b.toString();
    }
}
