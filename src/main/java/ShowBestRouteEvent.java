/**
 * Created by velin.
 */
public class ShowBestRouteEvent extends ScheduledEvent {

    private final NetworkNode fromNode;
    private final NetworkNode toNode;

    public ShowBestRouteEvent(Integer afterExchange, NetworkNode fromNode, NetworkNode toNode) {
        super(afterExchange);
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    @Override
    public void executeEvent() {

    }
}
