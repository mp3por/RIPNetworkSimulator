import java.util.ArrayList;

/**
 * Created by velin.
 */
public class ShowBestRouteEvent extends ScheduledEvent {
    public interface ShowBestRouteCapable {
        ArrayList<NetworkNode> findBestRoute(NetworkNode fromNode, NetworkNode toNode, ArrayList<NetworkNode> currPath);
    }

    private final NetworkNode fromNode;
    private final NetworkNode toNode;
    private final ShowBestRouteCapable simulator;

    public ShowBestRouteEvent(Integer afterExchange, NetworkNode fromNode, NetworkNode toNode, ShowBestRouteCapable simulator) {
        super(afterExchange);
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.simulator = simulator;
    }

    @Override
    public void executeEvent(Integer currentExchange) {
        ArrayList<NetworkNode> currPath = new ArrayList<NetworkNode>();
        System.out.println(this.toString());
        currPath = simulator.findBestRoute(fromNode, toNode, currPath);
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
//        b.append(toNode.getNodeId());
        System.out.println(b.toString());
    }

    @Override
    public String toString() {
        return "ShowBestRouteEvent from " + fromNode.getNodeId() + " to " + toNode.getNodeId();
    }
}
