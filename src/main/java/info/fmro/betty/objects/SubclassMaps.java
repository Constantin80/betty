package info.fmro.betty.objects;

import info.fmro.betty.safebet.BetradarEvent;
import info.fmro.betty.safebet.CoralEvent;
import info.fmro.shared.utility.SynchronizedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class SubclassMaps<T>
//        implements Serializable
{
    //    private static final long serialVersionUID = 3963261546843195228L;
    private static final Logger logger = LoggerFactory.getLogger(SubclassMaps.class);
    private final Map<Class<? extends T>, SynchronizedMap<Long, ? extends T>> mapsList = new LinkedHashMap<>(Statics.scraperEventSubclassesSet.size());

    @SuppressWarnings("unchecked")
    public SubclassMaps(@SuppressWarnings("unused") final Set<Class<? extends T>> set) {
        // this will be used in the final object
//        for (Class<? extends T> clazz: set){
//            mapsList.put(clazz, );
//        }

        // temporary
        this.mapsList.put((Class<? extends T>) BetradarEvent.class, (SynchronizedMap<Long, ? extends T>) Statics.betradarEventsMap);
        this.mapsList.put((Class<? extends T>) CoralEvent.class, (SynchronizedMap<Long, ? extends T>) Statics.coralEventsMap);
    }

    public synchronized int size() {
        return this.mapsList.size();
    }

    public synchronized long getTimeStamp(final Class<? extends T> clazz) {
        return this.mapsList.get(clazz).getTimeStamp();
    }

    public synchronized long getTimeStampRemoved(final Class<? extends T> clazz) {
        return this.mapsList.get(clazz).getTimeStampRemoved();
    }

    public synchronized long getNTimeStampRemoved() {
        return getNTimeStampRemoved(Statics.MIN_MATCHED);
    }

    public synchronized long getNTimeStampRemoved(final int minNMultiple) {
        final long returnValue;
        if (minNMultiple < 1) {
            logger.error("minNMultiple {} too small in getNTimeStampRemoved", minNMultiple);
            returnValue = 0L;
        } else {
            final ArrayList<Long> list = new ArrayList<>(size());
            for (final SynchronizedMap<Long, ? extends T> map : this.mapsList.values()) {
                list.add(map.getTimeStampRemoved());
            }

            Collections.sort(list);
            final int listSize = list.size();
            if (minNMultiple <= listSize) {
                returnValue = list.get(listSize - minNMultiple);
            } else {
                logger.error("minNMultiple {} > listSize {} in getNTimeStampRemoved", minNMultiple, listSize);
                returnValue = 0L;
            }
        }
        return returnValue;
    }

    public synchronized long getNTimeStamp() {
        return getNTimeStamp(Statics.MIN_MATCHED);
    }

    public synchronized long getNTimeStamp(final int minNMultiple) {
        final long returnValue;
        if (minNMultiple < 1) {
            logger.error("minNMultiple {} too small in getNTimeStamp", minNMultiple);
            returnValue = 0L;
        } else {
            final ArrayList<Long> list = new ArrayList<>(size());
            for (final SynchronizedMap<Long, ? extends T> map : this.mapsList.values()) {
                list.add(map.getTimeStamp());
            }

            Collections.sort(list);
            final int listSize = list.size();
            if (minNMultiple <= listSize) {
                returnValue = list.get(listSize - minNMultiple);
            } else {
                logger.error("minNMultiple {} > listSize {} in getNTimeStamp", minNMultiple, listSize);
                returnValue = 0L;
            }
        }
        return returnValue;
    }
}
