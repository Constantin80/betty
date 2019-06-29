package info.fmro.betty.objects;

import info.fmro.shared.utility.SynchronizedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class ManagedEventsMap
        extends SynchronizedMap<String, ManagedEvent>
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ManagedEventsMap.class);
    private static final long serialVersionUID = 3165547218025993674L;

    public ManagedEventsMap() {
        super();
    }

    public ManagedEventsMap(final int initialSize) {
        super(initialSize);
    }

    public ManagedEventsMap(final int initialSize, final float loadFactor) {
        super(initialSize, loadFactor);
    }

    @Override
    public synchronized ManagedEvent get(final String key) {
        ManagedEvent managedEvent = super.get(key);
        if (managedEvent == null) {
            managedEvent = new ManagedEvent(key);
            super.put(key, managedEvent);
            Statics.rulesManager.rulesHaveChanged.set(true);
        } else { // I got the event and I'll return it, nothing else to be done
        }

        return managedEvent;
    }
}
