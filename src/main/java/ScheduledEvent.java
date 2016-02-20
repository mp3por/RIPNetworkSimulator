/**
 * Created by velin.
 */
public abstract class ScheduledEvent {
    protected final Integer afterExchange;

    public ScheduledEvent(Integer afterExchange) {
        this.afterExchange = afterExchange;
    }

    public abstract void executeEvent();
}
