package info.fmro.betty.stream.cache.order;

import info.fmro.betty.stream.cache.util.OrderMarketRunner;
import info.fmro.betty.stream.cache.util.OrderMarketRunnerSnap;
import info.fmro.betty.stream.cache.util.OrderMarketSnap;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.definitions.OrderMarketChange;
import info.fmro.betty.stream.definitions.OrderRunnerChange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderMarket {
    private final String marketId;
    private final Map<RunnerId, OrderMarketRunner> marketRunners = new ConcurrentHashMap<>();
    private OrderMarketSnap orderMarketSnap;
    private boolean isClosed;

    public OrderMarket(String marketId) {
        this.marketId = marketId;
    }

    public synchronized void onOrderMarketChange(OrderMarketChange orderMarketChange) {
        final OrderMarketSnap newSnap = new OrderMarketSnap();
        newSnap.setMarketId(this.marketId);

        // update runners
        if (orderMarketChange.getOrc() != null) {
            for (OrderRunnerChange orderRunnerChange : orderMarketChange.getOrc()) {
                onOrderRunnerChange(orderRunnerChange);
            }
        }

        final List<OrderMarketRunnerSnap> snaps = new ArrayList<>(marketRunners.size());
        for (OrderMarketRunner orderMarketRunner : marketRunners.values()) {
            snaps.add(orderMarketRunner.getOrderMarketRunnerSnap());
        }

        newSnap.setOrderMarketRunners(snaps);

        isClosed = Boolean.TRUE.equals(orderMarketChange.getClosed());
        newSnap.setClosed(isClosed);

        orderMarketSnap = newSnap;
    }

    private synchronized void onOrderRunnerChange(OrderRunnerChange orderRunnerChange) {
        final RunnerId runnerId = new RunnerId(orderRunnerChange.getId(), orderRunnerChange.getHc());

        final OrderMarketRunner orderMarketRunner = marketRunners.computeIfAbsent(runnerId, r -> new OrderMarketRunner(getMarketId(), r));

        // update the runner
        orderMarketRunner.onOrderRunnerChange(orderRunnerChange);
    }

    public synchronized boolean isClosed() {
        return isClosed;
    }

    public synchronized OrderMarketSnap getOrderMarketSnap() {
        return orderMarketSnap;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    @Override
    public synchronized String toString() {
        final StringBuilder runnersSb = new StringBuilder(" ");
        for (OrderMarketRunner runner : marketRunners.values()) {
            runnersSb.append(runner).append(" ");
        }

        return "OrderMarket{" +
               "marketRunners=" + runnersSb.toString() +
               ", marketId='" + marketId + '\'' +
               '}';
    }
}
