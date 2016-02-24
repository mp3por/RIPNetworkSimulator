/**
 * Created by velin.
 */
public class TraceRouteTableEvent extends ScheduledEvent {

    private final NetworkNode node;

    public TraceRouteTableEvent(NetworkNode node, Integer iterationStartIndex, Integer iterationEndIndex) {
        super(iterationStartIndex);
        this.node = node;
    }

    @Override
    public void executeEvent(Integer currentIteration) {
        System.out.println("TraceRouteEvent for node: " + node.getNodeId());
        node.printTable();
    }
}
