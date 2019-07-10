package info.fmro.betty.objects;

import info.fmro.betty.entities.PriceProjection;
import info.fmro.betty.main.QuickCheckThread;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.SynchronizedMap;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class SafeBetsMap<K extends SafeBet>
        extends SynchronizedMap<String, SynchronizedMap<K, SafeBetStats>>
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(SafeBetsMap.class);
    private static final long serialVersionUID = 6744057008225898485L;

    SafeBetsMap() {
        super();
    }

    public SafeBetsMap(final int initialSize) {
        super(initialSize);
    }

    public SafeBetsMap(final int initialSize, final float loadFactor) {
        super(initialSize, loadFactor);
    }
//
//    @Override
//    public synchronized SynchronizedMap<K, SafeBetStats> get(String key) {
//        logger.error("should use get instead of get in SafeBetsMap");
//        return get(key);
//    }
//
//    public synchronized SynchronizedMap<K, SafeBetStats> get(String key) {
//        final SynchronizedMap<K, SafeBetStats> map = super.get(key); // real map value
////        return map == null ? null : new LinkedHashMap<>(map);
//        return map;
//    }

    @SuppressWarnings("OverlyNestedMethod")
    public synchronized void parseNoLongerSeenSafeBets(final String marketId, final PriceProjection localPriceProjection, final long endTime) {
        final SynchronizedMap<K, SafeBetStats> safeBetsStatsMap = this.get(marketId); // will be null if no such key exists

        if (safeBetsStatsMap != null && !safeBetsStatsMap.isEmpty()) {
            final Set<Entry<K, SafeBetStats>> entrySetCopy = safeBetsStatsMap.entrySetCopy();
            for (final Entry<K, SafeBetStats> entry : entrySetCopy) {
                final SafeBetStats value = entry.getValue();
                final K key = entry.getKey();
                if (value != null) {
                    if (value.getTimeLastAppear() < endTime) {
                        if (localPriceProjection == QuickCheckThread.priceProjectionAll || key.getSize() >= 2L) {
                            value.setTimeFirstNotAppeared(endTime);

                            final String printedString = MessageFormatter.arrayFormat("{} {} {} {}", new Object[]{value.printStats(), key.printStats(),
                                                                                                                  Generic.objectToString(key), Generic.objectToString(value)}).getMessage();
                            logger.info(printedString);
                            Statics.safeBetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");

//                                                    iterator.remove();
                            final SafeBetStats existingValue = this.removeSafeBet(marketId, key);
                            if (existingValue == null) { // sometimes this happens if two threads are removing the safeBet at almost the same time
                                logger.error("safeBet already removed in SafeBetsMap.parseNoLongerSeenSafeBets for {} {}", marketId, Generic.objectToString(key));
                            }
                        } else { // size < 2 and not getting those
                        }
                    } else { // this entry has just been updated, nothing to be done
                    }
                } else {
                    logger.error("null SafeBetStats in safeBetsStatsMap for: {} {}", marketId, Generic.objectToString(key));
//                                            iterator.remove();
                    this.removeSafeBet(marketId, key);
                }
            } // end for
            // I remove empty safeBetsStatsMaps in maintenance, as removing them here might create concurrency problems
        } else { // no safeBetsStatsMap exists, nothing to be done
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    synchronized SafeBetStats addAndGetSafeBetStats(final String marketId, final K safeBet, final long endTime, final long timePreviousMarketBookCheck) {
        final SynchronizedMap<K, SafeBetStats> safeBetsStatsMap;
        if (this.containsKey(marketId)) {
            safeBetsStatsMap = get(marketId); // real map value
        } else {
            safeBetsStatsMap = new SynchronizedMap<>(2);
            this.put(marketId, safeBetsStatsMap, true); // synchronized method, no need for putIfAbsent
        }

        final SafeBetStats safeBetStats;
        if (safeBetsStatsMap.containsKey(safeBet)) {
            safeBetStats = safeBetsStatsMap.get(safeBet);
            safeBetStats.setTimeLastAppear(endTime); // increases nAppeared too
        } else {
            final LinkedHashMap<Class<? extends ScraperEvent>, Long> timeScraperEventCheck = QuickCheckThread.getTimeScraperEventCheck(marketId);
            final SafeBetStats newSafeBetStats = new SafeBetStats(endTime, timePreviousMarketBookCheck, timeScraperEventCheck);
            final SafeBetStats existingValue = safeBetsStatsMap.putIfAbsent(safeBet, newSafeBetStats);
            if (existingValue != null) {
                logger.error("existing safeBetStats for: {} {} {}", marketId, Generic.objectToString(safeBet), Generic.objectToString(existingValue));
                existingValue.setTimeLastAppear(endTime); // increases nAppeared too
                safeBetStats = existingValue;
            } else { // normal behaviour
                safeBetStats = newSafeBetStats;
            }
        }
        return safeBetStats;
    }

    @Nullable
    public synchronized SafeBetStats removeSafeBet(final String key, final K safeBet) {
        final SynchronizedMap<K, SafeBetStats> map = get(key); // real map value
        return map == null ? null : map.remove(safeBet);
    }
}
