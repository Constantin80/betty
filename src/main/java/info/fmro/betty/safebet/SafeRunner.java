package info.fmro.betty.safebet;

import info.fmro.shared.enums.ScrapedField;
import info.fmro.shared.enums.Side;
import info.fmro.betty.objects.BlackList;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.permanent.MaintenanceThread;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.objects.SafeObjectInterface;
import info.fmro.shared.utility.SynchronizedMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

// replace the container of SafeRunner from SynchronizedSet to Safe variant, to have on add and on remove methods; on remove I'll add a removed boolean
public class SafeRunner
        implements SafeObjectInterface, Serializable, Comparable<SafeRunner> {
    private static final Logger logger = LoggerFactory.getLogger(SafeRunner.class);
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = -7260869968913768056L;
    private final long addedStamp;
    private final String marketId;
    private final Long selectionId; // The unique id of the runner (unique for that particular market)
    private final Side side; // only one side can ever be safe
    private final EnumMap<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedScrapers = new EnumMap<>(ScrapedField.class);
    @SuppressWarnings("FieldHasSetterButNoGetter")
    private boolean hasBeenRemoved; // flag placed as I remove the object from set, so I don't use the object anymore
//    private double placedAmount; // only used for the specialLimitPeriod

    @SuppressWarnings("ConstructorWithTooManyParameters")
    private SafeRunner(final String marketId, final Long selectionId, final Side side, final long timeStamp, final Map<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedScrapers, final ScrapedField... scrapedFields) {
        this.marketId = marketId;
        this.selectionId = selectionId;
        this.side = side;
        if (scrapedFields != null) {
            for (final ScrapedField scrapedField : scrapedFields) {
                final SynchronizedMap<Class<? extends ScraperEvent>, Long> existingValue = usedScrapers.get(scrapedField);
                if (existingValue != null && existingValue.size() >= Statics.MIN_MATCHED) {
                    this.usedScrapers.put(scrapedField, existingValue);
                } else {
                    //noinspection ThisEscapedInObjectConstruction
                    logger.error("null or too low size map during SafeRunner creation for: {} {} {} {}", Generic.objectToString(scrapedField), Generic.objectToString(existingValue), Generic.objectToString(scrapedFields), Generic.objectToString(this));
                }
            } // end for
        } else {
            this.usedScrapers.putAll(usedScrapers); // allows using the entire map
            if (!sufficientScrapers()) {
                //noinspection ThisEscapedInObjectConstruction
                logger.error("insufficient scrapers during SafeRunner creation for: {}", Generic.objectToString(this));
            }
        } // end else
        this.addedStamp = timeStamp;
    }

    public SafeRunner(final String marketId, final Long selectionId, final Side side, final Map<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedScrapers, final ScrapedField... scrapedFields) {
        this(marketId, selectionId, side, System.currentTimeMillis(), usedScrapers, scrapedFields);
    }

    public SafeRunner(final String marketId, final Long selectionId, final Side side) {
        // create stump for SafetyLimits use
        this.marketId = marketId;
        this.selectionId = selectionId;
        this.side = side;
        this.addedStamp = System.currentTimeMillis();
    }

    public synchronized boolean hasBeenRemoved() {
        return this.hasBeenRemoved;
    }

    private synchronized void setHasBeenRemoved(final boolean hasBeenRemoved) {
        this.hasBeenRemoved = hasBeenRemoved;
    }

    @Override
    public synchronized int runAfterRemoval() {
        final int modified = this.hasBeenRemoved() ? 0 : 1;
        this.setHasBeenRemoved(true);

        return modified;
    }

    @Override
    public synchronized int runAfterAdd() {
        final int modified;
        modified = 0; // method not used so far

        return modified;
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    //    public double getPlacedAmount() {
//        return placedAmount;
//    }
//
//    public void setPlacedAmount(double placedAmount) {
//        this.placedAmount = placedAmount;
//    }
//
//    public double addPlacedAmount(double placedAmount) {
//        this.placedAmount += placedAmount;
//        return placedAmount;
//    }
    private synchronized int getMatchedScrapers(final ScrapedField scrapedField) {
        final SynchronizedMap<Class<? extends ScraperEvent>, Long> map = this.usedScrapers.get(scrapedField);
        return map == null ? 0 : map.size();
    }

    public synchronized int getMinScoreScrapers() {
        return Math.min(this.getMatchedScrapers(ScrapedField.HOME_SCORE), this.getMatchedScrapers(ScrapedField.AWAY_SCORE));
    }

    public synchronized long getAddedStamp() {
        return this.addedStamp;
    }

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

    //    public synchronized void setSelectionId(long selectionId) {
//        this.selectionId = selectionId;
//    }
    public synchronized Side getSide() {
        return this.side;
    }

    //    public synchronized void setSide(Side side) {
//        this.side = side;
//    }
    public synchronized EnumMap<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> getUsedScrapers() {
        return new EnumMap<>(this.usedScrapers);
    }

    private synchronized int leastUsedScrapers() {
        int minimum = Integer.MAX_VALUE;
        for (final SynchronizedMap<Class<? extends ScraperEvent>, Long> map : this.usedScrapers.values()) {
            minimum = Math.min(minimum, map.size());
        }
        return minimum;
    }

    public final synchronized boolean sufficientScrapers() {
        return leastUsedScrapers() >= Statics.MIN_MATCHED;
    }

    public synchronized int checkScrapers() {
        // checks the associated scrapers
        // self-removes from map if not enough associated scrapers left
        // sets lenghty ignore on self if safeRunner is young and some associated scraper is ignored, although enough associated scrapers left; safety feature
        int modified = 0;

        for (final SynchronizedMap<Class<? extends ScraperEvent>, Long> map : this.usedScrapers.values()) {
            final Set<Entry<Class<? extends ScraperEvent>, Long>> entrySetCopy = map.entrySetCopy();
            for (final Entry<Class<? extends ScraperEvent>, Long> entry : entrySetCopy) {
                final Class<? extends ScraperEvent> key = entry.getKey();
                final Long value = entry.getValue();
                if (BlackList.notExistOrIgnored(key, value)) {
                    if (map.remove(key) != null) {
                        modified++;
                    } else {
                        logger.error("null during map remove for: {} {} {}", key, value, Generic.objectToString(this));
                    }
                }
//                final SynchronizedMap<Long, ? extends ScraperEvent> scraperEventsMap = Formulas.getScraperEventsMap(key);
//                final ScraperEvent scraperEvent = scraperEventsMap.get(value);
//                if (scraperEvent == null || scraperEvent.isIgnored()) {
//                    map.remove(key);
//                }
            } // end for
        } // end for
        if (modified > 0) {
            if (sufficientScrapers()) {
                // this default ignore was mostly moved to ScraperEvent.setIgnore and affects the whole Event
//                final long currentTime = System.currentTimeMillis();
//                final long safeRunnerAge = currentTime - this.addedStamp;
//
//                if (safeRunnerAge < 30_000L) { // sufficient scrapers left, but 1 was ignored and safeRunner is young; this results in a long ignore for the safeRunner
//                    logger.error("ignoring young {}ms safeRunner due to scraperEvent ignore: {}", safeRunnerAge, Generic.objectToString(this));
//                    this.setIgnored(300_000L, currentTime); // 5 minutes ignore
//                }
            } else {
                MaintenanceThread.removeSafeRunner(this.marketId, this);
            }
        }

        return modified;
    }

    @SuppressWarnings("OverlyNestedMethod")
    public synchronized int updateUsedScrapers(final SafeRunner safeRunner) {
        int modified;
        if (this == safeRunner) {
            logger.error("update from same object in SafeRunner.update: {}", Generic.objectToString(this));
            modified = 0;
        } else if (!Objects.equals(this.selectionId, safeRunner.getSelectionId())) {
            logger.error("mismatch selectionId in SafeRunner.update: {} {}", Generic.objectToString(this), Generic.objectToString(safeRunner));
            modified = 0;
        } else if (this.side != safeRunner.getSide()) {
            logger.error("mismatch side in SafeRunner.update: {} {}", Generic.objectToString(this), Generic.objectToString(safeRunner));
            modified = 0;
        } else {
            final EnumMap<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> otherUsedScrapers = safeRunner.getUsedScrapers();
            if (otherUsedScrapers == null) {
                logger.error("otherUsedScrapers null in SafeRunner.update: {} {}", Generic.objectToString(this), Generic.objectToString(safeRunner));
                modified = 0;
            } else {
                final int usedScrapersSize = this.usedScrapers.size();
                final int otherUsedScrapersSize = otherUsedScrapers.size();
                if (usedScrapersSize == otherUsedScrapersSize) {
                    final Set<ScrapedField> usedScrapersKeys = this.usedScrapers.keySet();
                    final Set<ScrapedField> otherUsedScrapersKeys = otherUsedScrapers.keySet();
                    if (Objects.equals(usedScrapersKeys, otherUsedScrapersKeys)) {
                        modified = 0; // initialized
                        for (final Entry<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> entry : this.usedScrapers.entrySet()) {
                            final ScrapedField scrapedField = entry.getKey();
                            final SynchronizedMap<Class<? extends ScraperEvent>, Long> map = entry.getValue();
                            final SynchronizedMap<Class<? extends ScraperEvent>, Long> otherMap = otherUsedScrapers.get(scrapedField);
                            if (Objects.equals(map, otherMap)) { // equal, nothing to be done
                            } else {
                                modified++;
                                this.usedScrapers.put(scrapedField, otherMap);
                            }
                        } // end for
                    } else {
                        logger.error("usedScrapersKeys {} != otherUsedScrapersKeys {} in SafeRunner.update: {} {}", Generic.objectToString(usedScrapersKeys), Generic.objectToString(otherUsedScrapersKeys), Generic.objectToString(this),
                                     Generic.objectToString(safeRunner));
                        modified = 0;
                    }
                } else {
                    logger.error("usedScrapersSize {} != otherUsedScrapersSize {} in SafeRunner.update: {} {}", usedScrapersSize, otherUsedScrapersSize, Generic.objectToString(this), Generic.objectToString(safeRunner));
                    modified = 0;
                }
            }
        }
        if (modified > 0) {
            checkScrapers();
        }

        return modified;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public synchronized int compareTo(@NotNull final SafeRunner o) {
        //noinspection ConstantConditions
        if (o == null) {
            return AFTER;
        }
        if (this == o) {
            return EQUAL;
        }

        if (this.getClass() != o.getClass()) {
            return this.getClass().hashCode() < o.getClass().hashCode() ? BEFORE : AFTER;
        }
        if (!Objects.equals(this.marketId, o.marketId)) {
            if (this.marketId == null) {
                return BEFORE;
            }
            if (o.marketId == null) {
                return AFTER;
            }
            return this.marketId.compareTo(o.marketId);
        }
        if (!Objects.equals(this.selectionId, o.selectionId)) {
            if (this.selectionId == null) {
                return BEFORE;
            }
            if (o.selectionId == null) {
                return AFTER;
            }
            return this.selectionId.compareTo(o.selectionId);
        }
        if (this.side != o.side) {
            if (this.side == null) {
                return BEFORE;
            }
            if (o.side == null) {
                return AFTER;
            }
            return this.side.compareTo(o.side);
        }

        return EQUAL;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.marketId);
        hash = 53 * hash + Objects.hashCode(this.selectionId);
        hash = 53 * hash + Objects.hashCode(this.side);
        return hash;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SafeRunner other = (SafeRunner) obj;
        return Objects.equals(this.marketId, other.marketId) && Objects.equals(this.selectionId, other.selectionId) && this.side == other.side;
    }
}
