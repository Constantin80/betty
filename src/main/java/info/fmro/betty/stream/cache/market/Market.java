package info.fmro.betty.stream.cache.market;

import info.fmro.betty.enums.MarketStatus;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.cache.util.Utils;
import info.fmro.betty.stream.definitions.MarketChange;
import info.fmro.betty.stream.definitions.MarketDefinition;
import info.fmro.betty.stream.definitions.RunnerChange;
import info.fmro.betty.stream.definitions.RunnerDefinition;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Thread safe, reference invariant reference to a market.
 * Repeatedly calling <see cref="Snap"/> will return atomic snapshots of the market.
 */
public class Market {
    private final String marketId;
    private Map<RunnerId, MarketRunner> marketRunners = new ConcurrentHashMap<>();
    private MarketDefinition marketDefinition;
    private double tv;
    //An atomic snapshot of the state of the market.
    private MarketSnap snap;

    public Market(String marketId) {
        this.marketId = marketId;
    }

    synchronized void onMarketChange(MarketChange marketChange) {
        //initial image means we need to wipe our data
        final boolean isImage = Boolean.TRUE.equals(marketChange.getImg());
        //market definition changed
        Optional.ofNullable(marketChange.getMarketDefinition()).ifPresent(this::onMarketDefinitionChange);
        //runners changed
        Optional.ofNullable(marketChange.getRc()).ifPresent(l -> l.forEach(p -> onPriceChange(isImage, p)));

        final MarketSnap newSnap = new MarketSnap();
        newSnap.setMarketId(marketId);
        newSnap.setMarketDefinition(marketDefinition);
        newSnap.setMarketRunners(marketRunners.entrySet().stream().map(l -> l.getValue().getSnap()).collect(Collectors.toList()));
        newSnap.setTradedVolume(tv = Utils.selectPrice(isImage, tv, marketChange.getTv()));
        snap = newSnap;
    }

    private synchronized void onPriceChange(boolean isImage, RunnerChange runnerChange) {
        final MarketRunner marketRunner = getOrAdd(new RunnerId(runnerChange.getId(), runnerChange.getHc()));
        //update runner
        marketRunner.onPriceChange(isImage, runnerChange);
    }

    private synchronized void onMarketDefinitionChange(MarketDefinition marketDefinition) {
        this.marketDefinition = marketDefinition;
        Optional.ofNullable(marketDefinition.getRunners()).ifPresent(rds -> rds.forEach(this::onRunnerDefinitionChange));
    }

    private synchronized void onRunnerDefinitionChange(RunnerDefinition runnerDefinition) {
        final MarketRunner marketRunner = getOrAdd(new RunnerId(runnerDefinition.getId(), runnerDefinition.getHc()));
        //update runner
        marketRunner.onRunnerDefinitionChange(runnerDefinition);
    }

    private synchronized MarketRunner getOrAdd(RunnerId runnerId) {
        final MarketRunner runner = marketRunners.computeIfAbsent(runnerId, k -> new MarketRunner(getMarketId(), k));
        return runner;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized boolean isClosed() {
        //whether the market is closed
        return (marketDefinition != null && marketDefinition.getStatus() == MarketStatus.CLOSED);
    }

    public synchronized MarketSnap getSnap() {
        return snap;
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
