package info.fmro.betty.objects;

import java.util.Iterator;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecordedMaxValue {

    private static final Logger logger = LoggerFactory.getLogger(RecordedMaxValue.class);
    private final TreeMap<Integer, Long> map = new TreeMap<>();
    private final long expiryTime; // checked for only when new values are added, unless checked manually
    private final int maxMapSize;
    private long lastCheckMap;

    public RecordedMaxValue(long expiryTime) {
        this.expiryTime = expiryTime;
        this.maxMapSize = 4;
    }

    public RecordedMaxValue(long expiryTime, int maxMapSize) {
        this.expiryTime = expiryTime;
        if (maxMapSize >= 2) {
            this.maxMapSize = maxMapSize;
        } else {
            this.maxMapSize = 2; // minimum value, else my algorithm won't work
        }
    }

    public synchronized int getValue() {
        if (map.isEmpty()) {
            return 0;
        } else {
            return map.lastKey();
        }
    }

    public synchronized void setValue(int value) {
        setValue(value, System.currentTimeMillis());
    }

    public synchronized void setValue(int key, long timeStamp) {
        if (map.size() < maxMapSize || map.containsKey(key) || checkMap(timeStamp)) {
            map.put(key, timeStamp);
        } else { // replace smallest key, even with a new lower key
            map.pollFirstEntry();
            map.put(key, timeStamp);
        }

        if (timeStamp - this.lastCheckMap > this.expiryTime) {
            checkMap(timeStamp); // check it once in a while, else very old values will get stuck there; added at end for the rare case where it is checked earlier in the method
        }
    }

    public synchronized boolean checkMap() {
        return checkMap(System.currentTimeMillis());
    }

    public synchronized boolean checkMap(long currentTime) {
        this.lastCheckMap = currentTime;
        boolean modified = false;
        Iterator<Long> iterator = map.values().iterator();
        while (iterator.hasNext()) {
            long value = iterator.next();
            if (currentTime - value > expiryTime) {
                iterator.remove();
                modified = true;
            }
        } // end while
        return modified;
    }
}
