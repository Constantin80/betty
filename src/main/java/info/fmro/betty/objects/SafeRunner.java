package info.fmro.betty.objects;

import info.fmro.betty.enums.ScrapedField;
import info.fmro.betty.enums.Side;
import info.fmro.betty.main.MaintenanceThread;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.SafeObjectInterface;
import info.fmro.shared.utility.SynchronizedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
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
    private final HashMap<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedScrapers = new HashMap<>(4);
    private boolean hasBeenRemoved; // flag placed as I remove the object from set, so I don't use the object anymore
//    private double placedAmount; // only used for the specialLimitPeriod

    @SuppressWarnings("LeakingThisInConstructor")
    public SafeRunner(String marketId, Long selectionId, Side side, long timeStamp, Map<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedScrapers,
                      ScrapedField... scrapedFields) {
        this.marketId = marketId;
        this.selectionId = selectionId;
        this.side = side;
        if (scrapedFields != null) {
            final int length = scrapedFields.length;
            for (int i = 0; i < length; i++) {
                SynchronizedMap<Class<? extends ScraperEvent>, Long> existingValue = usedScrapers.get(scrapedFields[i]);
                if (existingValue != null && existingValue.size() >= Statics.MIN_MATCHED) {
                    this.usedScrapers.put(scrapedFields[i], existingValue);
                } else {
                    logger.error("null or too low size map during SafeRunner creation for: {} {} {} {}", Generic.objectToString(scrapedFields[i]),
                                 Generic.objectToString(existingValue), Generic.objectToString(scrapedFields), Generic.objectToString(this));
                }
            } // end for
        } else {
            this.usedScrapers.putAll(usedScrapers); // allows using the entire map
            if (!sufficientScrapers()) {
                logger.error("insufficient scrapers during SafeRunner creation for: {}", Generic.objectToString(this));
            }
        } // end else
        addedStamp = timeStamp;
    }

    public SafeRunner(String marketId, Long selectionId, Side side, Map<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedScrapers,
                      ScrapedField... scrapedFields) {
        this(marketId, selectionId, side, System.currentTimeMillis(), usedScrapers, scrapedFields);
    }

    public SafeRunner(String marketId, Long selectionId, Side side) {
        // create stump for SafetyLimits use
        this.marketId = marketId;
        this.selectionId = selectionId;
        this.side = side;
        this.addedStamp = System.currentTimeMillis();
    }

    public boolean hasBeenRemoved() {
        return this.hasBeenRemoved;
    }

    public void setHasBeenRemoved(boolean hasBeenRemoved) {
        this.hasBeenRemoved = hasBeenRemoved;
    }

    @Override
    public int runOnRemoval() {
        final int modified;
        if (!this.hasBeenRemoved()) {
            modified = 1;
        } else {
            modified = 0;
        }
        this.setHasBeenRemoved(true);

        return modified;
    }

    @Override
    public int runOnAdd() {
        final int modified;
        modified = 0; // method not used so far

        return modified;
    }

    public String getMarketId() {
        return marketId;
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
    public synchronized int getMatchedScrapers(ScrapedField scrapedField) {
        SynchronizedMap<Class<? extends ScraperEvent>, Long> map = usedScrapers.get(scrapedField);
        int result;
        if (map == null) {
            result = 0;
        } else {
            result = map.size();
        }
        return result;
    }

    public synchronized int getMinScoreScrapers() {
        return Math.min(this.getMatchedScrapers(ScrapedField.HOME_SCORE), this.getMatchedScrapers(ScrapedField.AWAY_SCORE));

    }

    public synchronized long getAddedStamp() {
        return addedStamp;
    }

    public synchronized Long getSelectionId() {
        return selectionId;
    }

    //    public synchronized void setSelectionId(long selectionId) {
//        this.selectionId = selectionId;
//    }
    public synchronized Side getSide() {
        return side;
    }

    //    public synchronized void setSide(Side side) {
//        this.side = side;
//    }
    public synchronized HashMap<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> getUsedScrapers() {
        return usedScrapers == null ? null : new HashMap<>(usedScrapers);
    }

    //    public synchronized SynchronizedMap<Class<? extends ScraperEvent>, Long> removeUsedField(ScrapedField scrapedField) {
//        return usedScrapers.remove(scrapedField);
//    }
//    public synchronized boolean removeUsedScraper(ScrapedField scrapedField, Class<? extends ScraperEvent> clazz, Long scraperId) {
//        boolean returnValue;
//        SynchronizedMap<Class<? extends ScraperEvent>, Long> map = usedScrapers.get(scrapedField);
//        if (map == null) {
//            returnValue = false;
//        } else {
//            returnValue = map.remove(clazz, scraperId);
//        }
//        return returnValue;
//    }
//
//    public synchronized Long removeUsedScraper(ScrapedField scrapedField, Class<? extends ScraperEvent> clazz) {
//        Long returnValue;
//        SynchronizedMap<Class<? extends ScraperEvent>, Long> map = usedScrapers.get(scrapedField);
//        if (map == null) {
//            returnValue = null;
//        } else {
//            returnValue = map.remove(clazz);
//        }
//        return returnValue;
//    }
//
//    public synchronized boolean removeUsedScraper(Class<? extends ScraperEvent> clazz, Long scraperId) {
//        boolean returnValue = false;
//        for (SynchronizedMap<Class<? extends ScraperEvent>, Long> map : usedScrapers.values()) {
//            if (map != null) {
//                if (map.remove(clazz, scraperId)) {
//                    returnValue = true;
//                }
//            } else {
//                logger.error("null map found in removeUsedScraper: {} {} {} {}", returnValue, clazz.getSimpleName(), scraperId, selectionId);
//            }
//        } // end for
//
//        return returnValue;
//    }
//
//    public synchronized Long removeUsedScraper(Class<? extends ScraperEvent> clazz) {
//        Long returnValue = null;
//        for (SynchronizedMap<Class<? extends ScraperEvent>, Long> map : usedScrapers.values()) {
//            if (map != null) {
//                Long removeResult = map.remove(clazz);
//                if (removeResult != null) {
//                    if (returnValue == null) {
//                        returnValue = removeResult;
//                    } else if (Objects.equals(returnValue, removeResult)) { // nothing to be done
//                    } else {
//                        logger.error("different scraperIds found in removeUsedScraper: {} {} {} {}", returnValue, removeResult, clazz.getSimpleName(), selectionId);
//                    }
//                } else { // nothing removed, nothing to be done
//                }
//            } else {
//                logger.error("null map found in removeUsedScraper: {} {} {}", returnValue, clazz.getSimpleName(), selectionId);
//            }
//        } // end for
//
//        return returnValue;
//    }
    public synchronized int leastUsedScrapers() {
        int minimum = Integer.MAX_VALUE;
        for (SynchronizedMap<Class<? extends ScraperEvent>, Long> map : usedScrapers.values()) {
            minimum = Math.min(minimum, map.size());
        }
        return minimum;
    }

    @SuppressWarnings("FinalMethod")
    public final synchronized boolean sufficientScrapers() {
        return leastUsedScrapers() >= Statics.MIN_MATCHED;
    }

    public synchronized int checkScrapers() {
        // checks the associated scrapers
        // self-removes from map if not enough associated scrapers left
        // sets lenghty ignore on self if safeRunner is young and some associated scraper is ignored, although enough associated scrapers left; safety feature
        int modified = 0;

        for (SynchronizedMap<Class<? extends ScraperEvent>, Long> map : usedScrapers.values()) {
            final Set<Entry<Class<? extends ScraperEvent>, Long>> entrySetCopy = map.entrySetCopy();
            for (Entry<Class<? extends ScraperEvent>, Long> entry : entrySetCopy) {
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
            if (!sufficientScrapers()) {
                MaintenanceThread.removeSafeRunner(marketId, this);
            } else {
                // this default ignore was mostly moved to ScraperEvent.setIgnore and affects the whole Event
//                final long currentTime = System.currentTimeMillis();
//                final long safeRunnerAge = currentTime - this.addedStamp;
//
//                if (safeRunnerAge < 30_000L) { // sufficient scrapers left, but 1 was ignored and safeRunner is young; this results in a long ignore for the safeRunner
//                    logger.error("ignoring young {}ms safeRunner due to scraperEvent ignore: {}", safeRunnerAge, Generic.objectToString(this));
//                    this.setIgnored(300_000L, currentTime); // 5 minutes ignore
//                }
            }
        }

        return modified;
    }

    public synchronized int updateUsedScrapers(SafeRunner safeRunner) {
        int modified;
        if (this == safeRunner) {
            logger.error("update from same object in SafeRunner.update: {}", Generic.objectToString(this));
            modified = 0;
        } else if (!Objects.equals(this.selectionId, safeRunner.getSelectionId())) {
            logger.error("mismatch selectionId in SafeRunner.update: {} {}", Generic.objectToString(this), Generic.objectToString(safeRunner));
            modified = 0;
        } else if (!Objects.equals(this.side, safeRunner.getSide())) {
            logger.error("mismatch side in SafeRunner.update: {} {}", Generic.objectToString(this), Generic.objectToString(safeRunner));
            modified = 0;
        } else {
            final HashMap<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> otherUsedScrapers = safeRunner.getUsedScrapers();
            if (this.usedScrapers == null || otherUsedScrapers == null) {
                logger.error("usedScrapers or otherUsedScrapers null in SafeRunner.update: {} {}", Generic.objectToString(this), Generic.objectToString(safeRunner));
                modified = 0;
            } else {
                final int usedScrapersSize = this.usedScrapers.size();
                final int otherUsedScrapersSize = otherUsedScrapers.size();
                if (usedScrapersSize != otherUsedScrapersSize) {
                    logger.error("usedScrapersSize {} != otherUsedScrapersSize {} in SafeRunner.update: {} {}", usedScrapersSize, otherUsedScrapersSize,
                                 Generic.objectToString(this), Generic.objectToString(safeRunner));
                    modified = 0;
                } else {
                    final Set<ScrapedField> usedScrapersKeys = this.usedScrapers.keySet();
                    final Set<ScrapedField> otherUsedScrapersKeys = otherUsedScrapers.keySet();
                    if (!Objects.equals(usedScrapersKeys, otherUsedScrapersKeys)) {
                        logger.error("usedScrapersKeys {} != otherUsedScrapersKeys {} in SafeRunner.update: {} {}", Generic.objectToString(usedScrapersKeys),
                                     Generic.objectToString(otherUsedScrapersKeys), Generic.objectToString(this), Generic.objectToString(safeRunner));
                        modified = 0;
                    } else {
                        modified = 0; // initialized
                        for (ScrapedField scrapedField : usedScrapersKeys) {
                            final SynchronizedMap<Class<? extends ScraperEvent>, Long> map = this.usedScrapers.get(scrapedField);
                            final SynchronizedMap<Class<? extends ScraperEvent>, Long> otherMap = otherUsedScrapers.get(scrapedField);
                            if (Objects.equals(map, otherMap)) { // equal, nothing to be done
                            } else {
                                modified++;
                                this.usedScrapers.put(scrapedField, otherMap);
                            }
                        } // end for
                    }
                }
            }
        }
        if (modified > 0) {
            checkScrapers();
        }

        return modified;
    }

    @Override
    @SuppressWarnings(value = "AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(SafeRunner other) {
        if (other == null) {
            return AFTER;
        }
        if (this == other) {
            return EQUAL;
        }

        if (this.getClass() != other.getClass()) {
            if (this.getClass().hashCode() < other.getClass().hashCode()) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (!Objects.equals(this.marketId, other.marketId)) {
            if (this.marketId == null) {
                return BEFORE;
            }
            if (other.marketId == null) {
                return AFTER;
            }
            return this.marketId.compareTo(other.marketId);
        }
        if (!Objects.equals(this.selectionId, other.selectionId)) {
            if (this.selectionId == null) {
                return BEFORE;
            }
            if (other.selectionId == null) {
                return AFTER;
            }
            return this.selectionId.compareTo(other.selectionId);
        }
        if (!Objects.equals(this.side, other.side)) {
            if (this.side == null) {
                return BEFORE;
            }
            if (other.side == null) {
                return AFTER;
            }
            return this.side.compareTo(other.side);
        }

        return EQUAL;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.marketId);
        hash = 53 * hash + Objects.hashCode(this.selectionId);
        hash = 53 * hash + Objects.hashCode(this.side);
        return hash;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(Object obj) {
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
        return Objects.equals(this.marketId, other.marketId) && Objects.equals(this.selectionId, other.selectionId) && Objects.equals(this.side, other.side);
    }
}
