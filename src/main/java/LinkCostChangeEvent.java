/**
 * Class which will change the cost of a link
 */
public class LinkCostChangeEvent extends ScheduledEvent {

    /**
     * the new cost of the link
     */
    protected final Integer newCost;

    /**
     * the link itself
     */
    protected final NetworkLink link;

    /**
     * Constructor
     * @param afterExchange specifies after which exchange should this event fire
     * @param networkLink specifies which network link should get its cost changed
     * @param newCost specifies the new cost of the network link
     */
    public LinkCostChangeEvent(Integer afterExchange, NetworkLink networkLink, Integer newCost) {
        super(afterExchange);
        this.newCost = newCost;
        this.link = networkLink;
    }

    @Override
    public void executeEvent(Integer currentExchange) {
        // print for clarity
        System.out.println("LinkCostChangeEvent\n\tlink between " + link.getNode1().getNodeId() + " and " + link.getNode2().getNodeId() + " from " + link.cost + " to " + newCost);

        // change cost
        link.cost = newCost;
    }

    @Override
    public String toString() {
        return "LinkCostChangeEvent: " + link + ", newCost: " + newCost;
    }
}
