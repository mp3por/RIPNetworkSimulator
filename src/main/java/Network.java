import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by velin.
 * Tuk 6te staa cqlata rabota sas simuiraneto na dobavqne/mahane na linkove,
 * kakto i vseki NetworkNode 6te ima dostap do network i 6te polu4ava/priema suob6teniq,
 * nodovete 6te si imat taimer s koito da pra6tat subo6teniq,
 */
public class Network {
    private final HashMap<NetworkNode, ArrayList<NetworkNode>> graph;

    public Network(HashMap<NetworkNode, ArrayList<NetworkNode>> graph) {
        this.graph = graph;
    }

    public void addNode(NetworkNode node, ArrayList<NetworkNode> connections) {
        // TODO:handle collisions, should probably have a big input check in the beginning and make no checks later on
        graph.put(node, connections);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Network:\n");
        for (NetworkNode node : graph.keySet()) {
            b.append("\t" + node + " : ");
            ArrayList<NetworkNode> connections = graph.get(node);
            for (NetworkNode connection : connections) {
                b.append(connection + ", ");
            }
            b.append("\n");
        }
        return b.toString();
    }
}
