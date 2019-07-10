package info.fmro.betty.objects;

import info.fmro.shared.utility.SafeObjectInterface;
import info.fmro.shared.utility.SynchronizedSafeSet;
import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RulesManagerStringObject
        implements SafeObjectInterface, Serializable { // String object with SafeObjectInterface implemented
    private static final long serialVersionUID = -1239574862363223204L;
    private static final SynchronizedSafeSet<RulesManagerStringObject> objectContainer = Statics.rulesManager.marketsToCheck;
    private static final AtomicBoolean atomicOnAddMarker = Statics.rulesManager.marketsToCheckExist;
    private static final AtomicLong atomicOnAddMarkerStamp = Statics.rulesManager.marketsToCheckStamp;
    private final String marketId;

    @Contract(pure = true)
    public RulesManagerStringObject(final String marketId) {
        this.marketId = marketId;
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    @Override
    public synchronized int runAfterRemoval() {
        final int modified;
        final boolean containerIsEmpty = objectContainer.isEmpty();
        if (containerIsEmpty) {
            final boolean oldValue = atomicOnAddMarker.getAndSet(false);
            modified = oldValue ? 1 : 0;
        } else {
            modified = 0; // objectContainer not empty yet, not doing atomicMarker reset unless empty
        }

        if (modified > 0) {
            atomicOnAddMarkerStamp.set(System.currentTimeMillis());
        }
        return modified;
    }

    @Override
    public synchronized int runAfterAdd() {
        final int modified;
        final boolean oldValue = atomicOnAddMarker.getAndSet(true);
        modified = oldValue ? 0 : 1;

        if (modified > 0) {
            atomicOnAddMarkerStamp.set(System.currentTimeMillis());
        }
        return modified;
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
        final RulesManagerStringObject that = (RulesManagerStringObject) obj;
        return Objects.equals(this.marketId, that.marketId);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.marketId);
    }
}
