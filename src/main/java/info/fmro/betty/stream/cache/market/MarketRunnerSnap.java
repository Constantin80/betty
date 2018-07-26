package info.fmro.betty.stream.cache.market;

import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.definitions.RunnerDefinition;

/**
 * Thread safe atomic snapshot of a market runner.
 * Reference only changes if the snapshot changes: i.e. if snap1 == snap2 then they are the same (same is true for sub-objects)
 */
public class MarketRunnerSnap {
    private RunnerId runnerId;
    private RunnerDefinition definition;
    private MarketRunnerPrices prices;

    public MarketRunnerSnap(RunnerId runnerId, RunnerDefinition definition, MarketRunnerPrices prices) {
        this.runnerId = runnerId;
        this.definition = definition;
        this.prices = prices;
    }

    public RunnerId getRunnerId() {
        return runnerId;
    }

    synchronized void setRunnerId(RunnerId runnerId) {
        this.runnerId = runnerId;
    }

    public synchronized RunnerDefinition getDefinition() {
        return definition;
    }

    synchronized void setDefinition(RunnerDefinition definition) {
        this.definition = definition;
    }

    public synchronized MarketRunnerPrices getPrices() {
        return prices;
    }

    synchronized void setPrices(MarketRunnerPrices prices) {
        this.prices = prices;
    }

    @Override
    public synchronized String toString() {
        return "MarketRunnerSnap{" +
               "runnerId=" + runnerId +
               ", definition=" + definition +
               ", prices=" + prices +
               '}';
    }
}
