package info.fmro.betty.objects;

import info.fmro.betty.stream.cache.util.Utils;
import info.fmro.shared.utility.SynchronizedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class ManagedEvent
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ManagedEvent.class);
    private static final long serialVersionUID = 9206333179442623395L;
    private final String id;
    private double amountLimit = -1d;
    public final SynchronizedSet<String> marketIds = new SynchronizedSet<>(); // managedMarket ids associated with this event
    public transient ManagedMarketsMap marketsMap; // managedMarkets associated with this event

    public ManagedEvent(final String id) {
        this.id = id;
        this.marketsMap = new ManagedMarketsMap(this.id);
    }

    private void readObject(final java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        marketsMap = new ManagedMarketsMap(this.id);
    }

//    private synchronized HashMap<String, ManagedMarket> getMarketsMap() {
//        if (!isMarketsMapInitialized()) {
//            initializeMarketsMap();
//        } else { // already initialized, nothing to do
//        }
//
//        return new HashMap<>(marketsMap);
//    }

    public synchronized double getAmountLimit() {
        return amountLimit;
    }

    public synchronized boolean setAmountLimit(final double amountLimit) {
        final boolean modified;
        if (this.amountLimit == amountLimit) {
            modified = false;
        } else {
            this.amountLimit = amountLimit;
            modified = true;
        }

        if (modified) {
            Statics.rulesManager.rulesHaveChanged.set(true);
        }
        return modified;
    }

//    public synchronized boolean addManagedMarket(ManagedMarket managedMarket) {
//        if (marketIds.add(managedMarket.getId())) {
//            Statics.rulesManager.rulesHaveChanged.set(true);
//        }
//
//        return markets.add(managedMarket);
//    }

//    public synchronized boolean containsManagedMarket(ManagedMarket managedMarket) {
//        return markets.contains(managedMarket);
//    }

//    public synchronized boolean containsMarketId(String marketId) {
//        return marketIds.contains(marketId);
//    }

    private synchronized double getMaxEventLimit() {
        final double result;
        final double safetyLimit = Statics.safetyLimits.getDefaultEventLimit(id);
        if (this.amountLimit >= 0) {
            result = Math.min(this.amountLimit, safetyLimit);
        } else {
            result = safetyLimit;
        }
        return result;
    }

    public synchronized void calculateMarketLimits() {
        final double maxEventLimit = getMaxEventLimit();
        Utils.calculateMarketLimits(maxEventLimit, this.marketsMap.valuesCopy(), false, false);
    }

    @Override
    public synchronized boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ManagedEvent that = (ManagedEvent) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(id);
    }
}
