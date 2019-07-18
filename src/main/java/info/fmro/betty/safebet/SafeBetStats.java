package info.fmro.betty.safebet;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class SafeBetStats {
    private static final Logger logger = LoggerFactory.getLogger(SafeBetStats.class);
    private final long timeFirstAppear;
    private final long timeMarketBookCheckBeforeAppear;
    private final LinkedHashMap<Class<? extends ScraperEvent>, Long> timeScraperEventCheckBeforeAppearMap;
    private long timeLastAppear;
    private long timeFirstNotAppeared;
    private int nAppeared;

    @Contract(pure = true)
    SafeBetStats(final long timeFirstAppear, final long timeMarketBookCheckBeforeAppear, @NotNull final LinkedHashMap<Class<? extends ScraperEvent>, Long> timeScraperEventCheckBeforeAppear) {
        this.timeFirstAppear = timeFirstAppear;
        this.timeMarketBookCheckBeforeAppear = timeMarketBookCheckBeforeAppear;
        this.timeScraperEventCheckBeforeAppearMap = new LinkedHashMap<>(timeScraperEventCheckBeforeAppear);
        this.timeLastAppear = timeFirstAppear;
        this.nAppeared = 1;
    }

    public synchronized String printStats() {
        final StringBuilder stringBuilder = new StringBuilder("safeBet seenFor ");
        final long seenFor = this.timeLastAppear - this.timeFirstAppear;
        stringBuilder.append(seenFor).append("ms beforeChecks MarketBook:");
        if (this.timeMarketBookCheckBeforeAppear == 0L) {
            stringBuilder.append("- ");
        } else {
            stringBuilder.append(this.timeFirstAppear - this.timeMarketBookCheckBeforeAppear).append("ms ");
        }
        if (!this.timeScraperEventCheckBeforeAppearMap.isEmpty()) {
            for (final Entry<Class<? extends ScraperEvent>, Long> entry : this.timeScraperEventCheckBeforeAppearMap.entrySet()) {
                final Class<? extends ScraperEvent> clazz = entry.getKey();
                final long timeScraperEventCheckBeforeAppear = entry.getValue();
                stringBuilder.append(clazz.getSimpleName()).append(":");
                if (timeScraperEventCheckBeforeAppear == 0L) {
                    stringBuilder.append("- ");
                } else {
                    stringBuilder.append(this.timeFirstAppear - timeScraperEventCheckBeforeAppear).append("ms ");
                }
            } // end for
        } // end if

        stringBuilder.append("timeAfterLastAppear ");
        if (this.timeFirstNotAppeared > 0) {
            stringBuilder.append(this.timeFirstNotAppeared - this.timeLastAppear);
        } else {
            stringBuilder.append(System.currentTimeMillis() - this.timeLastAppear);
        }

        stringBuilder.append("ms seenEvery ").append(seenFor / this.nAppeared).append("ms");

        return stringBuilder.toString();
    }

    public synchronized long getTimeFirstAppear() {
        return this.timeFirstAppear;
    }

    public synchronized long getTimeMarketBookCheckBeforeAppear() {
        return this.timeMarketBookCheckBeforeAppear;
    }

    public synchronized LinkedHashMap<Class<? extends ScraperEvent>, Long> getTimeScraperEventCheckBeforeAppear() {
        return new LinkedHashMap<>(this.timeScraperEventCheckBeforeAppearMap);
    }

    public synchronized long getTimeLastAppear() {
        return this.timeLastAppear;
    }

    synchronized void setTimeLastAppear(final long timeLastAppear) {
        if (this.timeLastAppear == timeLastAppear) { // this does sometimes happen, due to server sending reply at almost the same time, and time granularity being bad
            logger.info("attempt to set the same timeLastAppear for: {}", Generic.objectToString(this));
        } else {
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
        }
        this.nAppeared++; // increased anyway, even for same or older value
    }

    public synchronized long getTimeFirstNotAppeared() {
        return this.timeFirstNotAppeared;
    }

    synchronized void setTimeFirstNotAppeared(final long timeFirstNotAppeared) {
        this.timeFirstNotAppeared = timeFirstNotAppeared;
    }

    public synchronized int getNAppeared() {
        return this.nAppeared;
    }

    public synchronized void setNAppeared(final int nAppeared) {
        this.nAppeared = nAppeared;
    }
}
