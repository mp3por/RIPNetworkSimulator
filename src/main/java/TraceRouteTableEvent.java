/**
 * Created by velin.
 */
public class TraceRouteTableEvent extends ScheduledEvent {

    private final NetworkNode node;

    public TraceRouteTableEvent(NetworkNode node, Integer exchangeStartIndex, Integer exchangeEndIndex) {
        super(exchangeStartIndex);
        this.node = node;
    }

    @Override
    public void executeEvent(Integer currentExchange) {
        System.out.println("TraceRouteEvent for node: " + node.getNodeId());
        node.printTable();
    }
}
