package info.fmro.betty.stream.cache.market;

import info.fmro.betty.stream.definitions.MarketChange;

import java.util.EventObject;

// Listeners
public class MarketChangeEvent
        extends EventObject {
    private static final long serialVersionUID = 2828124631512388452L;
    
    //the raw change message that was just applied
    private MarketChange change;
    //the market changed - this is reference invariant
    private Market market;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public MarketChangeEvent(Object source) {
        super(source);
    }

    public synchronized MarketChange getChange() {
        return change;
    }

    synchronized void setChange(MarketChange change) {
        this.change = change;
    }

    public synchronized Market getMarket() {
        return market;
    }

    synchronized void setMarket(Market market) {
        this.market = market;
    }

    public synchronized MarketSnap getSnap() {
        return market.getSnap();
    }
}
