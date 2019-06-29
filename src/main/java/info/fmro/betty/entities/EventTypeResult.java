package info.fmro.betty.entities;

public class EventTypeResult {
    private EventType eventType;
    private Integer marketCount;

    public EventTypeResult() {
    }

    public synchronized EventType getEventType() {
        return eventType;
    }

    public synchronized void setEventType(final EventType eventType) {
        this.eventType = eventType;
    }

    public synchronized Integer getMarketCount() {
        return marketCount;
    }

    public synchronized void setMarketCount(final Integer marketCount) {
        this.marketCount = marketCount;
    }
}
