/**
 * Abstract class which represents a scheduled event
 */
public abstract class ScheduledEvent {

    /**
     * Indicator after which exchange should the event happen
     */
    protected final Integer afterExchange;

    /**
     * Constructor
     *
     * @param afterExchange after which exchange should the event happen
     */
    public ScheduledEvent(Integer afterExchange) {
        this.afterExchange = afterExchange;
    }

    /**
     * Abstract method which is to be executed at the scheduled time
     *
     * @param currentExchange the current exchange index
     */
    public abstract void executeEvent(Integer currentExchange);
}
