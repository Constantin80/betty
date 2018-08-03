package info.fmro.betty.entities;

public class EventResult {

    private Event event;
    private Integer marketCount;

    public EventResult() {
    }

    public synchronized Event getEvent() {
        event.timeStamp();
        event.setMarketCount(marketCount);
        event.initializeCollections();
        event.parseName();
        return event;
    }

    public synchronized void setEvent(Event event) {
        this.event = event;
    }

    public synchronized Integer getMarketCount() {
        return marketCount;
    }

    public synchronized void setMarketCount(Integer marketCount) {
        this.marketCount = marketCount;
    }
}
