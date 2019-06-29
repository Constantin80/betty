package info.fmro.betty.objects;

import info.fmro.shared.utility.SafeObjectInterface;
import info.fmro.shared.utility.SynchronizedSafeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RulesManagerStringObject
        implements SafeObjectInterface, Serializable { // String object with SafeObjectInterface implemented
    private static final Logger logger = LoggerFactory.getLogger(RulesManagerStringObject.class);
    private static final long serialVersionUID = -1239574862363223204L;
    private static final SynchronizedSafeSet objectContainer = Statics.rulesManager.marketsToCheck;
    private static final AtomicBoolean atomicOnAddMarker = Statics.rulesManager.marketsToCheckExist;
    private static final AtomicLong atomicOnAddMarkerStamp = Statics.rulesManager.marketsToCheckStamp;
    private final String marketId;

    public RulesManagerStringObject(final String marketId) {
        this.marketId = marketId;
    }

    public synchronized String getMarketId() {
        return marketId;
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

    @Override
    public synchronized boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RulesManagerStringObject that = (RulesManagerStringObject) o;
        return Objects.equals(marketId, that.marketId);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(marketId);
    }
}
