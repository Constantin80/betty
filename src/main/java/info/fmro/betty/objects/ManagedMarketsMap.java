package info.fmro.betty.objects;

import info.fmro.shared.utility.SynchronizedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ManagedMarketsMap
        extends SynchronizedMap<String, ManagedMarket>
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ManagedMarketsMap.class);
    private static final long serialVersionUID = -1774486587659030612L;
    private final String eventId;
    private transient boolean isInitialized;

    public ManagedMarketsMap(final String eventId) {
        super();
        this.eventId = eventId; // the exact object reference
    }

    public ManagedMarketsMap(final int initialSize, final String eventId) {
        super(initialSize);
        this.eventId = eventId; // the exact object reference
    }

    public ManagedMarketsMap(final int initialSize, final float loadFactor, final String eventId) {
        super(initialSize, loadFactor);
        this.eventId = eventId; // the exact object reference
    }

    public synchronized void initializeMap() {
        if (!this.isInitialized) {
            final ManagedEvent managedEvent = Statics.rulesManager.events.get(eventId);

            for (String marketId : managedEvent.marketIds.copy()) {
                final ManagedMarket market = Statics.rulesManager.markets.get(marketId);
                if (market == null) { // I'll print error message, but I'll still add the null value to the returnMap
                    logger.error("null managedMarket found during initializeMap in rulesManager markets map for: {}", marketId);
                } else { // normal case, nothing to be done on branch
                }
                this.put(marketId, market);
            }

            this.isInitialized = true;
        } else { // already initialized, won't initialize again
        }
    }

    @Override
    public synchronized HashMap<String, ManagedMarket> copy() {
        initializeMap();
        return super.copy();
    }

    @Override
    public synchronized Set<Map.Entry<String, ManagedMarket>> entrySetCopy() {
        initializeMap();
        return super.entrySetCopy();
    }

    @Override
    public synchronized Set<String> keySetCopy() {
        initializeMap();
        return super.keySetCopy();
    }

    @Override
    public synchronized Collection<ManagedMarket> valuesCopy() {
        initializeMap();
        return super.valuesCopy();
    }

    @Override
    public synchronized void clear() {
        initializeMap();
        super.clear();
    }

    @Override
    public synchronized boolean containsKey(final String key) {
        initializeMap();
        return super.containsKey(key);
    }

    @Override
    public synchronized boolean containsValue(final ManagedMarket value) {
        initializeMap();
        return super.containsValue(value);
    }

    @Override
    public synchronized ManagedMarket get(final String key) {
        initializeMap();
        return super.get(key);
    }

    @Override
    public synchronized boolean isEmpty() {
        initializeMap();
        return super.isEmpty();
    }

    @Override
    public synchronized ManagedMarket put(final String key, final ManagedMarket value, final boolean intentionalPutInsteadOfPutIfAbsent) {
        initializeMap();
        return super.put(key, value, intentionalPutInsteadOfPutIfAbsent);
    }

    @Override
    public synchronized ManagedMarket put(final String key, final ManagedMarket value) {
        initializeMap();
        return super.put(key, value);
    }

    @Override
    public synchronized ManagedMarket putIfAbsent(final String key, final ManagedMarket value) {
        initializeMap();
        return super.putIfAbsent(key, value);
    }

    @Override
    public synchronized void putAll(final Map<? extends String, ? extends ManagedMarket> m) {
        initializeMap();
        super.putAll(m);
    }

    @Override
    public synchronized ManagedMarket remove(final String key) {
        initializeMap();
        return super.remove(key);
    }

    @Override
    public synchronized boolean remove(final String key, final ManagedMarket value) {
        initializeMap();
        return super.remove(key, value);
    }

    @Override
    public synchronized int size() {
        initializeMap();
        return super.size();
    }

    @Override
    public synchronized boolean containsEntry(final Map.Entry<String, ManagedMarket> entry) {
        initializeMap();
        return super.containsEntry(entry);
    }

    @Override
    public synchronized boolean containsAllEntries(final Collection<?> c) {
        initializeMap();
        return super.containsAllEntries(c);
    }

    @Override
    public synchronized boolean removeEntry(final Map.Entry<String, ManagedMarket> entry) {
        initializeMap();
        return super.removeEntry(entry);
    }

    @Override
    public synchronized boolean removeAllEntries(final Collection<?> c) {
        initializeMap();
        return super.removeAllEntries(c);
    }

    @Override
    public synchronized boolean retainAllEntries(final Collection<?> c) {
        initializeMap();
        return super.retainAllEntries(c);
    }

    @Override
    public synchronized boolean containsAllKeys(final Collection<?> c) {
        initializeMap();
        return super.containsAllKeys(c);
    }

    @Override
    public synchronized boolean removeAllKeys(final Collection<?> c) {
        initializeMap();
        return super.removeAllKeys(c);
    }

    @Override
    public synchronized boolean retainAllKeys(final Collection<?> c) {
        initializeMap();
        return super.retainAllKeys(c);
    }

    @Override
    public synchronized boolean containsAllValues(final Collection<?> c) {
        initializeMap();
        return super.containsAllValues(c);
    }

    @Override
    public synchronized boolean removeValue(final ManagedMarket value) {
        initializeMap();
        return super.removeValue(value);
    }

    @Override
    public synchronized boolean removeValueAll(final ManagedMarket value) {
        initializeMap();
        return super.removeValueAll(value);
    }

    @Override
    public synchronized boolean removeAllValues(final Collection<?> c) {
        initializeMap();
        return super.removeAllValues(c);
    }

    @Override
    public synchronized boolean retainAllValues(final Collection<?> c) {
        initializeMap();
        return super.retainAllValues(c);
    }

    @Override
    public synchronized int hashCode() {
        initializeMap();
        return super.hashCode();
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(final Object obj) {
        initializeMap();
        return super.equals(obj);
    }
}
