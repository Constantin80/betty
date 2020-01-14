package info.fmro.betty.stream.cache.order;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.stream.objects.RunnerId;
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
    private final @NotNull Map<RunnerId, OrderMarketRunner> marketRunners = new ConcurrentHashMap<>(4); // only place where orderMarketRunners are stored
    private boolean isClosed;

    OrderMarket(final String marketId) {
        this.marketId = marketId;
        Statics.rulesManager.newOrderMarketCreated.set(true);
    }

    synchronized void onOrderMarketChange(@NotNull final OrderMarketChange orderMarketChange) {
        // update runners
        if (orderMarketChange.getOrc() != null) {
            for (final OrderRunnerChange orderRunnerChange : orderMarketChange.getOrc()) {
                onOrderRunnerChange(orderRunnerChange);
            }
        }

        this.isClosed = Boolean.TRUE.equals(orderMarketChange.getClosed());
    }

    private synchronized void onOrderRunnerChange(@NotNull final OrderRunnerChange orderRunnerChange) {
        final RunnerId runnerId = new RunnerId(orderRunnerChange.getId(), orderRunnerChange.getHc());
        final OrderMarketRunner orderMarketRunner = this.marketRunners.computeIfAbsent(runnerId, r -> new OrderMarketRunner(getMarketId(), r));

        // update the runner
        orderMarketRunner.onOrderRunnerChange(orderRunnerChange);
    }

    @SuppressWarnings("SuspiciousGetterSetter")
    public synchronized boolean isClosed() {
        return this.isClosed;
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    public synchronized HashSet<RunnerId> getRunnerIds() {
        return new HashSet<>(this.marketRunners.keySet());
    }

    public synchronized ArrayList<OrderMarketRunner> getOrderMarketRunners() {
        return new ArrayList<>(this.marketRunners.values());
    }

    public synchronized OrderMarketRunner getOrderMarketRunner(final RunnerId runnerId) {
        return this.marketRunners.get(runnerId);
    }
}
