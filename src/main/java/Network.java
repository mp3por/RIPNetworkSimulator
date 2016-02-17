import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by velin.
 * Tuk 6te staa cqlata rabota sas simuiraneto na dobavqne/mahane na linkove,
 * kakto i vseki Node 6te ima dostap do network i 6te polu4ava/priema suob6teniq,
 * nodovete 6te si imat taimer s koito da pra6tat subo6teniq,
 */
public class Network {
    private final HashMap<Node, ArrayList<Node>> graph;

    public Network(HashMap<Node, ArrayList<Node>> graph) {
        this.graph = graph;
    }

    public void addNode(Node node, ArrayList<Node> connections) {
        // TODO:handle collisions, should probably have a big input check in the beginning and make no checks later on
        graph.put(node, connections);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder("Network:\n");
        for (Node node : graph.keySet()) {
            b.append("\t" + node + " : ");
            ArrayList<Node> connections = graph.get(node);
            for (Node connection : connections) {
                b.append(connection + ", ");
            }
            b.append("\n");
        }
        return b.toString();
    }
}
