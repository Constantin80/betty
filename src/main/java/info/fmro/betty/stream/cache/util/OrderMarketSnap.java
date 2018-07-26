package info.fmro.betty.stream.cache.util;

import com.google.common.collect.Lists;

public class OrderMarketSnap {
    private String marketId;
    private boolean isClosed;
    private Iterable<OrderMarketRunnerSnap> orderMarketRunners;

    public synchronized boolean isClosed() {
        return isClosed;
    }

    public synchronized void setClosed(boolean closed) {
        isClosed = closed;
    }

    public synchronized Iterable<OrderMarketRunnerSnap> getOrderMarketRunners() {
        return orderMarketRunners == null ? null : Lists.newArrayList(orderMarketRunners);
    }

    public synchronized void setOrderMarketRunners(Iterable<OrderMarketRunnerSnap> orderMarketRunners) {
        this.orderMarketRunners = orderMarketRunners == null ? null : Lists.newArrayList(orderMarketRunners);
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    @Override
    public synchronized String toString() {
        return "OrderMarketSnap{" +
               "marketId='" + marketId + '\'' +
               ", isClosed=" + isClosed +
               ", orderMarketRunners=" + orderMarketRunners +
               '}';
    }
}
