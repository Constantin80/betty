package info.fmro.betty.logic;

import info.fmro.shared.enums.RulesManagerModificationCommand;
import info.fmro.betty.objects.ManagedMarketsMap;
import info.fmro.shared.stream.objects.RulesManagerModification;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.cache.util.Utils;
import info.fmro.shared.utility.SynchronizedSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class ManagedEvent
        implements Serializable {
    private static final long serialVersionUID = 9206333179442623395L;
    private final String id;
    private double amountLimit = -1d;
    public final SynchronizedSet<String> marketIds = new SynchronizedSet<>(); // managedMarket ids associated with this event
    @SuppressWarnings("PackageVisibleField")
    transient ManagedMarketsMap marketsMap; // managedMarkets associated with this event

    public ManagedEvent(final String id) {
        this.id = id;
        this.marketsMap = new ManagedMarketsMap(this.id);
    }

    private void readObject(@NotNull final java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.marketsMap = new ManagedMarketsMap(this.id);
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
        return this.amountLimit;
    }

    @SuppressWarnings("UnusedReturnValue")
    synchronized boolean setAmountLimit(final double newAmountLimit) {
        final boolean modified;
        if (Double.isNaN(newAmountLimit)) {
            modified = false;
        } else  //noinspection FloatingPointEquality
            if (this.amountLimit == newAmountLimit) {
                modified = false;
            } else {
                this.amountLimit = newAmountLimit;
                modified = true;
            }

        if (modified) {
            Statics.rulesManager.listOfQueues.send(new RulesManagerModification(RulesManagerModificationCommand.setEventAmountLimit, this.amountLimit));
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
        final double safetyLimit = Statics.safetyLimits.getDefaultEventLimit(this.id);
        result = this.amountLimit >= 0 ? Math.min(this.amountLimit, safetyLimit) : safetyLimit;
        return result;
    }

    synchronized void calculateMarketLimits() {
        final double maxEventLimit = getMaxEventLimit();
        //noinspection NonPrivateFieldAccessedInSynchronizedContext
        Utils.calculateMarketLimits(maxEventLimit, this.marketsMap.valuesCopy(), false, false);
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ManagedEvent that = (ManagedEvent) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.id);
    }
}
