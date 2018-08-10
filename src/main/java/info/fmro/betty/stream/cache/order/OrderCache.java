package info.fmro.betty.stream.cache.order;

import info.fmro.betty.stream.definitions.OrderMarketChange;
import info.fmro.betty.stream.protocol.ChangeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderCache {
    private static final Logger logger = LoggerFactory.getLogger(OrderCache.class);
    private final Map<String, OrderMarket> markets = new ConcurrentHashMap<>();

    /**
     * Wether order markets are automatically removed on close
     * (default is true)
     */
    private boolean isOrderMarketRemovedOnClose = true; // default
    private boolean traceOrders = false;

    public OrderCache() {
    }

    public synchronized void traceOrders() {
        traceOrders = true;
    }

    public synchronized void onOrderChange(ChangeMessage<OrderMarketChange> changeMessage) {
        if (changeMessage.isStartOfNewSubscription()) {
            markets.clear();
        }

        if (changeMessage.getItems() != null) {
            for (OrderMarketChange change : changeMessage.getItems()) {
                final OrderMarket orderMarket = onOrderMarketChange(change);

                if (isOrderMarketRemovedOnClose && orderMarket.isClosed()) {
                    // remove on close
                    markets.remove(orderMarket.getMarketId());
                }

                dispatchOrderMarketChanged(orderMarket, change);
            } // end for
        }
    }

    private synchronized void dispatchOrderMarketChanged(OrderMarket orderMarket, OrderMarketChange change) {
        final OrderMarketChangeEvent orderMarketChangeEvent = new OrderMarketChangeEvent(this);
        orderMarketChangeEvent.setChange(change);
        orderMarketChangeEvent.setOrderMarket(orderMarket);

        try {
            if (traceOrders) { // does nothing now, I'll either add something or remove it completely in the future
//                Utils.printOrderMarket(orderMarketChangeEvent.snap());
            }
        } catch (Exception ex) {
            logger.error("Exception from event listener", ex);
        }
    }

    private synchronized OrderMarket onOrderMarketChange(OrderMarketChange orderMarketChange) {
        final OrderMarket orderMarket = markets.computeIfAbsent(orderMarketChange.getId(), key -> new OrderMarket(orderMarketChange.getId()));

        orderMarket.onOrderMarketChange(orderMarketChange);
        return orderMarket;
    }

    public synchronized boolean isOrderMarketRemovedOnClose() {
        return isOrderMarketRemovedOnClose;
    }

    public synchronized void setOrderMarketRemovedOnClose(boolean orderMarketRemovedOnClose) {
        isOrderMarketRemovedOnClose = orderMarketRemovedOnClose;
    }

    public synchronized Iterable<OrderMarket> getOrderMarkets() {
        return markets == null ? null : new ArrayList<>(markets.values());
    }

    public synchronized int getNumberOfOrderMarkets() {
        return markets.size();
    }
}
