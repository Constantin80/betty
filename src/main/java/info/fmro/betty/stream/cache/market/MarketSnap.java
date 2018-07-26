package info.fmro.betty.stream.cache.market;

import info.fmro.betty.stream.definitions.MarketDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Thread safe atomic snapshot of a market.
 * Reference only changes if the snapshot changes: i.e. if snap1 == snap2 then they are the same (same is true for sub-objects)
 */
public class MarketSnap {
    private String marketId;
    private MarketDefinition marketDefinition;
    private List<MarketRunnerSnap> marketRunners;
    private double tradedVolume;

    public synchronized String getMarketId() {
        return marketId;
    }

    synchronized void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public synchronized MarketDefinition getMarketDefinition() {
        return marketDefinition;
    }

    synchronized void setMarketDefinition(MarketDefinition marketDefinition) {
        this.marketDefinition = marketDefinition;
    }

    public synchronized List<MarketRunnerSnap> getMarketRunners() {
        return marketRunners == null ? null : new ArrayList<>(marketRunners);
    }

    synchronized void setMarketRunners(List<MarketRunnerSnap> marketRunners) {
        this.marketRunners = marketRunners == null ? null : new ArrayList<>(marketRunners);
    }

    public synchronized double getTradedVolume() {
        return tradedVolume;
    }

    synchronized void setTradedVolume(double tradedVolume) {
        this.tradedVolume = tradedVolume;
    }

    @Override
    public synchronized String toString() {
        return "MarketSnap{" +
               "marketId='" + marketId + '\'' +
               ", marketDefinition=" + marketDefinition +
               ", marketRunners=" + marketRunners +
               ", tradedVolume=" + tradedVolume +
               '}';
    }
}
