/**
 * Event which will print the route table of a node.
 */
public class TraceRouteTableEvent extends ScheduledEvent {

    private final NetworkNode node;

    public TraceRouteTableEvent(NetworkNode node, Integer exchangeStartIndex, Integer exchangeEndIndex) {
        super(exchangeStartIndex);
        this.node = node;
    }

    @Override
    public void executeEvent(Integer currentExchange) {
        // for clarity
        System.out.println(this.toString());

        // print table
        node.printTable();
    }

    @Override
    public String toString() {
        return "TraceRouteTableEvent for node " + node.getNodeId();
    }
}
