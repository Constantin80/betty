package info.fmro.betty.stream.cache.order;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.definitions.OrderMarketChange;
import info.fmro.betty.stream.protocol.ChangeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderCache
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(OrderCache.class);
    private static final long serialVersionUID = -6023803756520072425L;
    private final Map<String, OrderMarket> markets = new ConcurrentHashMap<>(); // only place where orderMarkets are permanently stored
    private boolean isOrderMarketRemovedOnClose = true; // default

    public OrderCache() {
    }

//    public synchronized void copyFrom(OrderCache orderCache) {
//        if (!this.markets.isEmpty()) {
//            logger.error("not empty map in OrderCache copyFrom: {}", Generic.objectToString(this));
//        }
//
//        if (orderCache == null) {
//            logger.error("null orderCache in copyFrom for: {}", Generic.objectToString(this));
//        } else {
//            this.markets.clear();
//            if (orderCache.markets != null) {
//                this.markets.putAll(orderCache.markets);
//            } else {
//                logger.error("null markets in OrderCache copyFrom: {}", Generic.objectToString(orderCache));
//            }
//
//            this.isOrderMarketRemovedOnClose = orderCache.isOrderMarketRemovedOnClose;
//        }
//        copyFromStamp();
//    }

    public synchronized void onOrderChange(final ChangeMessage<OrderMarketChange> changeMessage) {
        if (changeMessage.isStartOfNewSubscription()) {
            markets.clear();
            Statics.rulesManager.orderCacheHasReset.set(true);
        }

        if (changeMessage.getItems() != null) {
            for (OrderMarketChange change : changeMessage.getItems()) {
                final OrderMarket orderMarket = onOrderMarketChange(change);

                if (isOrderMarketRemovedOnClose && orderMarket.isClosed()) {
                    // remove on close
                    markets.remove(orderMarket.getMarketId());
                }
//                dispatchOrderMarketChanged(orderMarket, change);
            } // end for
        }
    }

//    private synchronized void dispatchOrderMarketChanged(OrderMarket orderMarket, OrderMarketChange change) {
//        final OrderMarketChangeEvent orderMarketChangeEvent = new OrderMarketChangeEvent(this);
//        orderMarketChangeEvent.setChange(change);
//        orderMarketChangeEvent.setOrderMarket(orderMarket);
//    }

    private synchronized OrderMarket onOrderMarketChange(final OrderMarketChange orderMarketChange) {
        final OrderMarket orderMarket = markets.computeIfAbsent(orderMarketChange.getId(), OrderMarket::new);

        orderMarket.onOrderMarketChange(orderMarketChange);
        return orderMarket;
    }

    public synchronized boolean isOrderMarketRemovedOnClose() {
        return isOrderMarketRemovedOnClose;
    }

    public synchronized void setOrderMarketRemovedOnClose(final boolean orderMarketRemovedOnClose) {
        isOrderMarketRemovedOnClose = orderMarketRemovedOnClose;
    }

    public synchronized Iterable<OrderMarket> getOrderMarkets() {
        return markets == null ? null : new ArrayList<>(markets.values());
    }

    public synchronized HashSet<String> getOrderMarketKeys() {
        return markets == null ? null : new HashSet<>(markets.keySet());
    }

    public synchronized int getNOrderMarkets() {
        return markets.size();
    }

    public synchronized OrderMarket getOrderMarket(final String marketId) {
        return markets == null ? null : markets.get(marketId);
    }

    public synchronized OrderMarketRunner getOrderMarketRunner(final String marketId, final RunnerId runnerId) {
        final OrderMarket orderMarket = this.getOrderMarket(marketId);
        return orderMarket == null ? null : orderMarket.getOrderMarketRunner(runnerId);
    }
}
