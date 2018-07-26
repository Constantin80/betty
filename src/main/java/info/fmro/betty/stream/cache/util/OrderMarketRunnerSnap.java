package info.fmro.betty.stream.cache.util;

import info.fmro.betty.entities.PriceSize;
import info.fmro.betty.stream.definitions.Order;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderMarketRunnerSnap {
    private List<PriceSize> layMatches;
    private List<PriceSize> backMatches;
    private Map<String, Order> unmatchedOrders;

    public synchronized List<PriceSize> getLayMatches() {
        return layMatches == null ? null : new ArrayList<>(layMatches);
    }

    public synchronized void setLayMatches(List<PriceSize> layMatches) {
        this.layMatches = layMatches == null ? null : new ArrayList<>(layMatches);
    }

    public synchronized List<PriceSize> getBackMatches() {
        return backMatches == null ? null : new ArrayList<>(backMatches);
    }

    public synchronized void setBackMatches(List<PriceSize> backMatches) {
        this.backMatches = backMatches == null ? null : new ArrayList<>(backMatches);
    }

    public synchronized Map<String, Order> getUnmatchedOrders() {
        return unmatchedOrders == null ? null : new HashMap<>(unmatchedOrders);
    }

    public synchronized void setUnmatchedOrders(Map<String, Order> unmatchedOrders) {
        this.unmatchedOrders = unmatchedOrders == null ? null : new HashMap<>(unmatchedOrders);
    }

    @Override
    public synchronized String toString() {
        StringBuilder uoOrdersSb = new StringBuilder(" ");
        for (Order order : unmatchedOrders.values()) {
            uoOrdersSb.append(order).append(" ");
        }

        return "OrderMarketRunnerSnap{" +
               "layMatches=" + layMatches +
               ", backMatches=" + backMatches +
               ", unmatchedOrders=" + uoOrdersSb.toString() +
               '}';
    }
}
