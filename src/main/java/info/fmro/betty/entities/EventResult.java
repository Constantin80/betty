package info.fmro.betty.entities;

public class EventResult {
    private Event event;
    private Integer marketCount;

    public synchronized Event getEvent() {
        this.event.timeStamp();
        this.event.setMarketCount(this.marketCount);
        this.event.initializeCollections();
        this.event.parseName();
        return this.event;
    }

    public synchronized void setEvent(final Event event) {
        this.event = event;
    }

    public synchronized Integer getMarketCount() {
        return this.marketCount;
    }

    public synchronized void setMarketCount(final Integer marketCount) {
        this.marketCount = marketCount;
    }
}
