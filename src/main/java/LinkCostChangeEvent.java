/**
 * Created by velin.
 */
public class LinkCostChangeEvent extends ScheduledEvent {
    protected final Integer newCost;
    protected final NetworkLink link;
    protected final Integer nodeId1;
    protected final Integer nodeId2;

    public LinkCostChangeEvent(Integer afterExchange, NetworkLink networkLink, Integer newCost, Integer node1Id, Integer node2Id) {
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
    public void executeEvent() {
        System.out.println("\tLinkCostChangeEvent");
        link.cost = newCost;
    }
}
