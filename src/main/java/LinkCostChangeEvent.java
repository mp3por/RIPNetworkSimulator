/**
 * Created by velin.
 */
public class LinkCostChangeEvent extends ScheduledEvent {
    protected final Integer newCost;
    protected final NetworkLink link;
    protected final NetworkNode nodeId1;
    protected final NetworkNode nodeId2;

    public LinkCostChangeEvent(Integer afterExchange, NetworkLink networkLink, Integer newCost, NetworkNode node1Id, NetworkNode node2Id) {
        super(afterExchange);
        this.newCost = newCost;
        this.link = networkLink;
        this.nodeId1 = node1Id;
        this.nodeId2 = node2Id;
    }

    @Override
    public String toString() {
        return "LinkCostChangeEvent: " + link + ", " + newCost;
    }

    @Override
    public void executeEvent(Integer currentIteration) {
        System.out.println("\tLinkCostChangeEvent, link between " + nodeId1.getNodeId() + " and " + nodeId2.getNodeId() + " from " + link.cost + " to " + newCost);
        link.cost = newCost;
    }
}
