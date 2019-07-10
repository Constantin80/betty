package info.fmro.betty.objects;

import java.util.Iterator;
import java.util.TreeMap;

public class RecordedMaxValue {
    private final TreeMap<Integer, Long> map = new TreeMap<>();
    private final long expiryTime; // checked for only when new values are added, unless checked manually
    private final int maxMapSize;
    private long lastCheckMap;

    public RecordedMaxValue(final long expiryTime) {
        this.expiryTime = expiryTime;
        this.maxMapSize = 4;
    }

    public RecordedMaxValue(final long expiryTime, final int maxMapSize) {
        this.expiryTime = expiryTime;
        // minimum value, else my algorithm won't work
        this.maxMapSize = maxMapSize >= 2 ? maxMapSize : 2;
    }

    public synchronized int getValue() {
        return this.map.isEmpty() ? 0 : this.map.lastKey();
    }

    public synchronized void setValue(final int value) {
        setValue(value, System.currentTimeMillis());
    }

    public synchronized void setValue(final int key, final long timeStamp) {
        if (this.map.size() >= this.maxMapSize && !this.map.containsKey(key) && !checkMap(timeStamp)) { // replace smallest key, even with a new lower key
            this.map.pollFirstEntry();
        }
        this.map.put(key, timeStamp);

        if (timeStamp - this.lastCheckMap > this.expiryTime) {
            checkMap(timeStamp); // check it once in a while, else very old values will get stuck there; added at end for the rare case where it is checked earlier in the method
        }
    }

    public synchronized boolean checkMap() {
        return checkMap(System.currentTimeMillis());
    }

    private synchronized boolean checkMap(final long currentTime) {
        this.lastCheckMap = currentTime;
        boolean modified = false;
        final Iterator<Long> iterator = this.map.values().iterator();
        while (iterator.hasNext()) {
            final long value = iterator.next();
            if (currentTime - value > this.expiryTime) {
                iterator.remove();
                modified = true;
            }
        } // end while
        return modified;
    }
}
