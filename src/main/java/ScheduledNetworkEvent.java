/**
 * Created by velin.
 */
public abstract class ScheduledNetworkEvent {
    protected final Integer afterExchange;

    public ScheduledNetworkEvent(Integer afterExchange) {
        this.afterExchange = afterExchange;
    }

    public abstract void executeEvent();
}
