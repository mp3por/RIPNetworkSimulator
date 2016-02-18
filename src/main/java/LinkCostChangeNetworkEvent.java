/**
 * Created by velin.
 */
public class LinkCostChangeNetworkEvent extends ScheduledNetworkEvent {
    protected final Integer newCost;
    protected final NetworkLink link;

    public LinkCostChangeNetworkEvent(Integer afterExchange, NetworkLink networkLink, Integer newCost) {
        super(afterExchange);
        this.newCost = newCost;
        this.link = networkLink;
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
