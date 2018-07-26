package info.fmro.betty.stream.cache.order;

import info.fmro.betty.stream.cache.util.OrderMarketSnap;
import info.fmro.betty.stream.definitions.OrderMarketChange;

import java.util.EventObject;

public class OrderMarketChangeEvent
        extends EventObject {
    private OrderMarketChange orderMarketChange;
    private OrderMarket orderMarket;

    /**
     * Constructs a prototypical Event.
     *
     * @param source The object on which the Event initially occurred.
     * @throws IllegalArgumentException if source is null.
     */
    public OrderMarketChangeEvent(Object source) {
        super(source);
    }

    public synchronized OrderMarketSnap snap() {
        return orderMarket.getOrderMarketSnap();
    }

    public synchronized OrderMarketChange getChange() {
        return orderMarketChange;
    }

    public synchronized void setChange(OrderMarketChange change) {
        this.orderMarketChange = change;
    }

    public synchronized void setOrderMarket(OrderMarket orderMarket) {
        this.orderMarket = orderMarket;
    }
}
