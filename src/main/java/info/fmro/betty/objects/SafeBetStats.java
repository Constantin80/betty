package info.fmro.betty.objects;

import info.fmro.shared.utility.Generic;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeBetStats {

    private static final Logger logger = LoggerFactory.getLogger(SafeBetStats.class);
    private final long timeFirstAppear;
    private final long timeMarketBookCheckBeforeAppear;
    private final LinkedHashMap<Class<? extends ScraperEvent>, Long> timeScraperEventCheckBeforeAppearMap;
    private long timeLastAppear;
    private long timeFirstNotAppeared;
    private int nAppeared;

    public SafeBetStats(final long timeFirstAppear, final long timeMarketBookCheckBeforeAppear, final LinkedHashMap<Class<? extends ScraperEvent>, Long> timeScraperEventCheckBeforeAppear) {
        this.timeFirstAppear = timeFirstAppear;
        this.timeMarketBookCheckBeforeAppear = timeMarketBookCheckBeforeAppear;
        this.timeScraperEventCheckBeforeAppearMap = timeScraperEventCheckBeforeAppear;
        this.timeLastAppear = timeFirstAppear;
        this.nAppeared = 1;
    }

    public synchronized String printStats() {
        StringBuilder stringBuilder = new StringBuilder("safeBet seenFor ");
        long seenFor = timeLastAppear - timeFirstAppear;
        stringBuilder.append(seenFor).append("ms beforeChecks MarketBook:");
        if (timeMarketBookCheckBeforeAppear == 0L) {
            stringBuilder.append("- ");
        } else {
            stringBuilder.append(timeFirstAppear - timeMarketBookCheckBeforeAppear).append("ms ");
        }
        if (timeScraperEventCheckBeforeAppearMap != null && !timeScraperEventCheckBeforeAppearMap.isEmpty()) {
            for (Entry<Class<? extends ScraperEvent>, Long> entry : timeScraperEventCheckBeforeAppearMap.entrySet()) {
                Class<? extends ScraperEvent> clazz = entry.getKey();
                long timeScraperEventCheckBeforeAppear = entry.getValue();
                stringBuilder.append(clazz.getSimpleName()).append(":");
                if (timeScraperEventCheckBeforeAppear == 0L) {
                    stringBuilder.append("- ");
                } else {
                    stringBuilder.append(timeFirstAppear - timeScraperEventCheckBeforeAppear).append("ms ");
                }
            } // end for
        } // end if

        stringBuilder.append("timeAfterLastAppear ");
        if (timeFirstNotAppeared > 0) {
            stringBuilder.append(timeFirstNotAppeared - timeLastAppear);
        } else {
            stringBuilder.append(System.currentTimeMillis() - timeLastAppear);
        }

        stringBuilder.append("ms seenEvery ").append(seenFor / nAppeared).append("ms");

        return stringBuilder.toString();
    }

    public synchronized long getTimeFirstAppear() {
        return timeFirstAppear;
    }

    public synchronized long getTimeMarketBookCheckBeforeAppear() {
        return timeMarketBookCheckBeforeAppear;
    }

    public synchronized LinkedHashMap<Class<? extends ScraperEvent>, Long> getTimeScraperEventCheckBeforeAppear() {
        return this.timeScraperEventCheckBeforeAppearMap == null ? null : new LinkedHashMap<>(this.timeScraperEventCheckBeforeAppearMap);
    }

    public synchronized long getTimeLastAppear() {
        return timeLastAppear;
    }

    public synchronized void setTimeLastAppear(final long timeLastAppear) {
        if (this.timeLastAppear != timeLastAppear) {
            if (this.timeLastAppear < timeLastAppear) {
                this.timeLastAppear = timeLastAppear;
            } else {
                final long timeSinceLastDiskSave = this.timeLastAppear - Statics.timeLastSaveToDisk.get();
                if (timeSinceLastDiskSave > 5_000L) {
                    final long timeDifference = this.timeLastAppear - timeLastAppear;
                    if (timeDifference > 500L) {
                        logger.error("attempt to set older by {}ms timeLastAppear (timeSinceLastDiskSave: {}ms) for: {}", timeDifference, timeSinceLastDiskSave,
                                Generic.objectToString(this));
                    } else if (timeDifference > 200L) {
                        logger.warn("attempt to set older by {} ms timeLastAppear for: {}", timeDifference, Generic.objectToString(this));
                    } else { // happens due to concurrent threads and high processor load; no need to print error message
                    }
                } else { // objects were written to disk recently, resulting in lag; no error message will be printed
                }
            }
        } else { // this does sometimes happen, due to server sending reply at almost the same time, and time granularity being bad
            logger.info("attempt to set the same timeLastAppear for: {}", Generic.objectToString(this));
        }
        nAppeared++; // increased anyway, even for same or older value
    }

    public synchronized long getTimeFirstNotAppeared() {
        return timeFirstNotAppeared;
    }

    public synchronized void setTimeFirstNotAppeared(final long timeFirstNotAppeared) {
        this.timeFirstNotAppeared = timeFirstNotAppeared;
    }

    public synchronized int getnAppeared() {
        return nAppeared;
    }

    public synchronized void setnAppeared(final int nAppeared) {
        this.nAppeared = nAppeared;
    }
}
