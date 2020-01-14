package info.fmro.betty.stream.cache.market;

import info.fmro.shared.enums.MarketStatus;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.stream.objects.RunnerId;
import info.fmro.shared.stream.definitions.MarketChange;
import info.fmro.shared.stream.definitions.MarketDefinition;
import info.fmro.shared.stream.definitions.RunnerChange;
import info.fmro.shared.stream.definitions.RunnerDefinition;
import info.fmro.shared.utility.Formulas;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Market
        implements Serializable {
    private static final long serialVersionUID = -288902433432290477L;
    private final String marketId;
    private final Map<RunnerId, MarketRunner> marketRunners = new ConcurrentHashMap<>(4); // the only place where MarketRunners are permanently stored
    private MarketDefinition marketDefinition;
    private double tv; //total value traded

    public Market(final String marketId) {
        this.marketId = marketId;
    }

    synchronized void onMarketChange(@NotNull final MarketChange marketChange) {
        //initial image means we need to wipe our data
        final boolean isImage = Boolean.TRUE.equals(marketChange.getImg());
        //market definition changed
        Optional.ofNullable(marketChange.getMarketDefinition()).ifPresent(this::onMarketDefinitionChange);
        //runners changed
        Optional.ofNullable(marketChange.getRc()).ifPresent(l -> l.forEach(p -> onPriceChange(isImage, p)));

        this.tv = Formulas.selectPrice(isImage, this.tv, marketChange.getTv());
    }

    private synchronized void onPriceChange(final boolean isImage, @NotNull final RunnerChange runnerChange) {
        final MarketRunner marketRunner = getOrAdd(new RunnerId(runnerChange.getId(), runnerChange.getHc()));
        //update runner
        marketRunner.onPriceChange(isImage, runnerChange);
    }

    private synchronized void onMarketDefinitionChange(@NotNull final MarketDefinition newMarketDefinition) {
        this.marketDefinition = newMarketDefinition;
        Optional.ofNullable(newMarketDefinition.getRunners()).ifPresent(rds -> rds.forEach(this::onRunnerDefinitionChange));
    }

    private synchronized void onRunnerDefinitionChange(@NotNull final RunnerDefinition runnerDefinition) {
        final MarketRunner marketRunner = getOrAdd(new RunnerId(runnerDefinition.getId(), runnerDefinition.getHc()));
        //update runner
        marketRunner.onRunnerDefinitionChange(runnerDefinition);
    }

    private synchronized MarketRunner getOrAdd(final RunnerId runnerId) {
        return this.marketRunners.computeIfAbsent(runnerId, k -> new MarketRunner(getMarketId(), k));
    }

    public synchronized MarketRunner getMarketRunner(final RunnerId runnerId) {
        return this.marketRunners.get(runnerId);
    }

    public synchronized HashSet<RunnerId> getRunnerIds() {
        return new HashSet<>(this.marketRunners.keySet());
    }

    @Contract(pure = true)
    private synchronized double getTv() {
        return this.tv;
    }

    public synchronized double getTvEUR() {
        return getTv() * Statics.safetyLimits.currencyRate.get();
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    public synchronized boolean isClosed() {
        //whether the market is closed
        return (this.marketDefinition != null && this.marketDefinition.getStatus() == MarketStatus.CLOSED);
    }

    public synchronized MarketDefinition getMarketDefinition() {
        return this.marketDefinition;
    }
}
