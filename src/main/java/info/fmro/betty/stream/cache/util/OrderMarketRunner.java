package info.fmro.betty.stream.cache.util;

import info.fmro.betty.stream.definitions.Order;
import info.fmro.betty.stream.definitions.OrderRunnerChange;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderMarketRunner {
    //    private final OrderMarket orderMarket;
    private final String marketId;
    private final RunnerId runnerId;
    private OrderMarketRunnerSnap orderMarketRunnerSnap;
    private PriceSizeLadder layMatches = PriceSizeLadder.newLay();
    private PriceSizeLadder backMatches = PriceSizeLadder.newBack();
    private Map<String, Order> unmatchedOrders = new ConcurrentHashMap<>();

    public OrderMarketRunner(String marketId, RunnerId runnerId) {
        this.marketId = marketId;
        this.runnerId = runnerId;
    }

    public synchronized void onOrderRunnerChange(OrderRunnerChange orderRunnerChange) {
        boolean isImage = Boolean.TRUE.equals(orderRunnerChange.getFullImage());

        if (isImage) {
            unmatchedOrders.clear();
        }

        if (orderRunnerChange.getUo() != null) {
            for (Order order : orderRunnerChange.getUo()) {
                unmatchedOrders.put(order.getId(), order);
            }
        }

        OrderMarketRunnerSnap newSnap = new OrderMarketRunnerSnap();
        newSnap.setUnmatchedOrders(new HashMap<>(unmatchedOrders));

        newSnap.setLayMatches(layMatches.onPriceChange(isImage, orderRunnerChange.getMl()));
        newSnap.setBackMatches(backMatches.onPriceChange(isImage, orderRunnerChange.getMb()));

        orderMarketRunnerSnap = newSnap;
    }

    public synchronized OrderMarketRunnerSnap getOrderMarketRunnerSnap() {
        return orderMarketRunnerSnap;
    }

    public synchronized RunnerId getRunnerId() {
        return runnerId;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    @Override
    public synchronized String toString() {
        return orderMarketRunnerSnap == null ? "null" : orderMarketRunnerSnap.toString();
    }
}
