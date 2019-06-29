package info.fmro.betty.stream.cache.order;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.definitions.OrderMarketChange;
import info.fmro.betty.stream.definitions.OrderRunnerChange;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OrderMarket
        implements Serializable {
    private static final long serialVersionUID = 6849187708144779801L;
    private final String marketId;
    private final @NotNull Map<RunnerId, OrderMarketRunner> marketRunners = new ConcurrentHashMap<>(); // only place where orderMarketRunners are stored
    private boolean isClosed;

    public OrderMarket(final String marketId) {
        this.marketId = marketId;
        Statics.rulesManager.newOrderMarketCreated.set(true);
    }

    public synchronized void onOrderMarketChange(final OrderMarketChange orderMarketChange) {
        // update runners
        if (orderMarketChange.getOrc() != null) {
            for (OrderRunnerChange orderRunnerChange : orderMarketChange.getOrc()) {
                onOrderRunnerChange(orderRunnerChange);
            }
        }

        isClosed = Boolean.TRUE.equals(orderMarketChange.getClosed());
    }

    private synchronized void onOrderRunnerChange(final OrderRunnerChange orderRunnerChange) {
        final RunnerId runnerId = new RunnerId(orderRunnerChange.getId(), orderRunnerChange.getHc());
        final OrderMarketRunner orderMarketRunner = marketRunners.computeIfAbsent(runnerId, r -> new OrderMarketRunner(getMarketId(), r));

        // update the runner
        orderMarketRunner.onOrderRunnerChange(orderRunnerChange);
    }

    public synchronized boolean isClosed() {
        return isClosed;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized HashSet<RunnerId> getRunnerIds() {
        return new HashSet<>(marketRunners.keySet());
    }

    public synchronized ArrayList<OrderMarketRunner> getOrderMarketRunners() {
        return new ArrayList<>(marketRunners.values());
    }

    public synchronized OrderMarketRunner getOrderMarketRunner(final RunnerId runnerId) {
        return marketRunners.get(runnerId);
    }

    @Override
    public synchronized String toString() {
        final StringBuilder runnersSb = new StringBuilder(" ");
        for (OrderMarketRunner runner : marketRunners.values()) {
            runnersSb.append(runner).append(" ");
        }

        return "OrderMarket{" + "marketRunners=" + runnersSb.toString() + ", marketId='" + marketId + '\'' + '}';
    }
}
