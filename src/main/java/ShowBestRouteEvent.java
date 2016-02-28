import java.util.ArrayList;

/**
 * Event which finds and prints the current best route from source to destination node.
 */
public class ShowBestRouteEvent extends ScheduledEvent {

    /**
     * Interface
     */
    public interface ShowBestRouteCapable {
        ArrayList<NetworkNode> findBestRoute(NetworkNode fromNode, NetworkNode toNode, ArrayList<NetworkNode> currPath);
    }

    private final NetworkNode fromNode;
    private final NetworkNode toNode;
    private final ShowBestRouteCapable simulator;

    public ShowBestRouteEvent(Integer afterExchange, NetworkNode fromNode, NetworkNode toNode, ShowBestRouteCapable showBestRouteFinder) {
        super(afterExchange);
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.simulator = showBestRouteFinder;
    }

    @Override
    public void executeEvent(Integer currentExchange) {
        // create empty path
        ArrayList<NetworkNode> currPath = new ArrayList<NetworkNode>();

        // for clarity
        System.out.println(this.toString());

        // find the best route
        currPath = simulator.findBestRoute(fromNode, toNode, currPath);

        // print the best route
        StringBuilder b = new StringBuilder("\t" + fromNode.getNodeId() + " - > ");
        for (int i = 0; i < currPath.size(); i++) {
            NetworkNode networkNode = currPath.get(i);
            if (networkNode != null) {
                b.append(networkNode.getNodeId());
                if (i != currPath.size() - 1) {
                    b.append(" -> ");
                }

            } else {
                b.append(" next hop unknown yet ");
                break;
            }
        }
        System.out.println(b.toString());
    }

    @Override
    public String toString() {
        return "ShowBestRouteEvent from " + fromNode.getNodeId() + " to " + toNode.getNodeId();
    }
}
