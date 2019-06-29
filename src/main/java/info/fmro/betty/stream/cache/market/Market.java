package info.fmro.betty.stream.cache.market;

import info.fmro.betty.enums.MarketStatus;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.cache.util.Utils;
import info.fmro.betty.stream.definitions.MarketChange;
import info.fmro.betty.stream.definitions.MarketDefinition;
import info.fmro.betty.stream.definitions.RunnerChange;
import info.fmro.betty.stream.definitions.RunnerDefinition;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class Market
        implements Serializable {
    private static final long serialVersionUID = -288902433432290477L;
    private final String marketId;
    private Map<RunnerId, MarketRunner> marketRunners = new ConcurrentHashMap<>(); // the only place where MarketRunners are permanently stored
    private MarketDefinition marketDefinition;
    private double tv; //total value traded

    public Market(final String marketId) {
        this.marketId = marketId;
    }

    synchronized void onMarketChange(final MarketChange marketChange) {
        //initial image means we need to wipe our data
        final boolean isImage = Boolean.TRUE.equals(marketChange.getImg());
        //market definition changed
        Optional.ofNullable(marketChange.getMarketDefinition()).ifPresent(this::onMarketDefinitionChange);
        //runners changed
        Optional.ofNullable(marketChange.getRc()).ifPresent(l -> l.forEach(p -> onPriceChange(isImage, p)));

        tv = Utils.selectPrice(isImage, tv, marketChange.getTv());
    }

    private synchronized void onPriceChange(final boolean isImage, final RunnerChange runnerChange) {
        final MarketRunner marketRunner = getOrAdd(new RunnerId(runnerChange.getId(), runnerChange.getHc()));
        //update runner
        marketRunner.onPriceChange(isImage, runnerChange);
    }

    private synchronized void onMarketDefinitionChange(final MarketDefinition marketDefinition) {
        this.marketDefinition = marketDefinition;
        Optional.ofNullable(marketDefinition.getRunners()).ifPresent(rds -> rds.forEach(this::onRunnerDefinitionChange));
    }

    private synchronized void onRunnerDefinitionChange(final RunnerDefinition runnerDefinition) {
        final MarketRunner marketRunner = getOrAdd(new RunnerId(runnerDefinition.getId(), runnerDefinition.getHc()));
        //update runner
        marketRunner.onRunnerDefinitionChange(runnerDefinition);
    }

    private synchronized MarketRunner getOrAdd(final RunnerId runnerId) {
        final MarketRunner runner = marketRunners.computeIfAbsent(runnerId, k -> new MarketRunner(getMarketId(), k));
        return runner;
    }

    public synchronized MarketRunner getMarketRunner(final RunnerId runnerId) {
        return this.marketRunners.get(runnerId);
    }

    public synchronized HashSet<RunnerId> getRunnerIds() {
        return new HashSet<>(marketRunners.keySet());
    }

    private synchronized double getTv() {
        return tv;
    }

    public synchronized double getTvEUR() {
        return getTv() * Statics.safetyLimits.currencyRate.get();
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized boolean isClosed() {
        //whether the market is closed
        return (marketDefinition != null && marketDefinition.getStatus() == MarketStatus.CLOSED);
    }

    public synchronized MarketDefinition getMarketDefinition() {
        return marketDefinition;
    }

    @Override
    public synchronized String toString() {
        return "Market{" +
               "marketId='" + marketId + '\'' +
               ", marketRunners=" + marketRunners +
               ", marketDefinition=" + marketDefinition +
               '}';
    }
}
