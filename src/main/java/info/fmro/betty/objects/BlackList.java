package info.fmro.betty.objects;

import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.main.LaunchCommandThread;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.Ignorable;
import info.fmro.shared.utility.SynchronizedMap;
import info.fmro.shared.utility.SynchronizedSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

public class BlackList //        implements Serializable 
{

    private static final Logger logger = LoggerFactory.getLogger(BlackList.class);
//    public static final Event dummyEvent = new Event("dummy");
//    public static final MarketCatalogue dummyMarketCatalogue = new MarketCatalogue("dummy");
//    public static final MarketBook dummyMarketBook = new MarketBook("dummy");
//    public static final ScraperEvent dummyScraperEvent = new ScraperEvent("dummy", 12345678901L);
//    public static final Long dummyLong = -1234567890L;
//    public static final SynchronizedSet<SafeRunner> dummySafeRunnersSet = new SynchronizedSet<>(Arrays.asList(new SafeRunner(123456789012L, Side.LAY,
//            ImmutableMap.of(ScrapedField.MATCH_STATUS, new SynchronizedMap<>(ImmutableMap.of(BetradarEvent.class, 123456789012L, CoralEvent.class, 123456789012L))))));
//    private static final long serialVersionUID = -4021000494758105346L;

//    private final Map<Long, Long> betradarIds = new HashMap<>(0);
//    private final Map<Long, Long> coralIds = new HashMap<>(0);
//    private final Map<String, Long> eventIds = new HashMap<>(0);
//    private final Map<String, Long> marketIds = new HashMap<>(0);
//    private final Map<String, Long> eventIdsShort = new HashMap<>(0);
//    private final Map<String, Long> marketIdsShort = new HashMap<>(0);
//    private final long checkExpiredInterval = 2_000L, checkExpiredAllInterval = 60_000L, defaultSafetyPeriod = 10_000L, defaultShortSafetyPeriod = 10_000L;
    public static final long defaultSafetyPeriod = 10_000L;

    private BlackList() {
    }

//    private long lastCheckExpired, lastCheckExpiredAll;
//    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
//    public static void copyFrom(BlackList blackList) {
////        this.lastCheckExpired = blackList.lastCheckExpired;
////        this.lastCheckExpiredAll = blackList.lastCheckExpiredAll;
//
////        this.coralIds.clear();
////        this.coralIds.putAll(blackList.coralIds);
////        this.betradarIds.clear();
////        this.betradarIds.putAll(blackList.betradarIds);
////        this.eventIds.clear();
////        this.eventIds.putAll(blackList.eventIds);
////        this.marketIds.clear();
////        this.marketIds.putAll(blackList.marketIds);
////        this.eventIdsShort.clear();
////        this.eventIdsShort.putAll(blackList.eventIdsShort);
////        this.marketIdsShort.clear();
////        this.marketIdsShort.putAll(blackList.marketIdsShort);
//    }
//    @SuppressWarnings("unchecked")
//    public static <V> V checkedPutScraper(Long scraperId, V value) {
//        if (value instanceof BetradarEvent) {
//            if (this.betradarIds.containsKey(scraperId)) {
//                Formulas.logOnce(Statics.debugLevel.check(3, 173), logger, LogLevel.INFO, "betradarEvent {} rejected as blacklisted", scraperId);
//                return (V) dummyScraperEvent;
//            } else {
//                return (V) Statics.betradarEventsMap.put(scraperId, (BetradarEvent) value);
//            }
//        } else if (value instanceof CoralEvent) {
//            if (this.coralIds.containsKey(scraperId)) {
//                Formulas.logOnce(Statics.debugLevel.check(3, 173), logger, LogLevel.INFO, "coralEvent {} rejected as blacklisted", scraperId);
//                return (V) dummyScraperEvent;
//            } else {
//                return (V) Statics.coralEventsMap.put(scraperId, (CoralEvent) value);
//            }
//        } else {
//            logger.error("unknown checkedPutScraper value class: {} {} {}", value.getClass(), scraperId, Generic.objectToString(value));
//            return null;
//        }
//    }
//
//    // public static <V> V checkedPutEvent(Map<String, V> map, String eventId, V value) {
//    //     if (this.eventIds.containsKey(eventId)) {
//    //         return null;
//    //     } else {
//    //         return map.put(eventId, value);
//    //     }
//    // }
//    @SuppressWarnings("unchecked")
//    public static <V> V checkedPutEvent(SynchronizedMap<String, V> map, String eventId, V value, long checkTime) {
//        if (this.eventIdsShort.containsKey(eventId)) {
//            final long timeShortLeft = this.eventIdsShort.get(eventId) - checkTime;
//            if (timeShortLeft >= 0L) {
//                logger.info("event {} still blacklisted for {} ms", eventId, timeShortLeft);
//                if (map == Statics.eventsMap) {
//                    return (V) dummyEvent;
//                } else {
//                    logger.error("unknown checkedPutEvent map");
//                    return null;
//                }
//            } else {
//                return map.put(eventId, value);
//            }
//        } else {
//            return map.put(eventId, value);
//        }
//    }
//
//    // public static <V> V checkedPutMarket(Map<String, V> map, String marketId, V value) {
//    //     if (this.marketIds.containsKey(marketId)) {
//    //         return null;
//    //     } else {
//    //         return map.put(marketId, value);
//    //     }
//    // }
//    @SuppressWarnings("unchecked")
//    public static <V> V checkedPutMarket(SynchronizedMap<String, V> map, String marketId, V value, long checkTime) {
//        if (this.marketIdsShort.containsKey(marketId)) {
//            final long timeShortLeft = this.marketIdsShort.get(marketId) - checkTime;
//            if (timeShortLeft >= 0L) {
//                logger.info("market {} still blacklisted for {} ms", marketId, timeShortLeft);
//                if (map == Statics.marketCataloguesMap) {
//                    return (V) dummyMarketCatalogue;
//                } else if (map == Statics.safeMarketBooksMap) {
//                    return (V) dummyMarketBook;
//                } else if (map == Statics.safeMarketsImportantMap) {
//                    return (V) dummyLong;
//                } else if (map == Statics.safeMarketsMap) {
//                    return (V) dummySafeRunnersSet;
//                } else {
//                    logger.error("unknown checkedPutMarket map");
//                    return null;
//                }
//            } else {
//                return map.put(marketId, value);
//            }
//        } else {
//            return map.put(marketId, value);
//        }
//    }
    // public static boolean checkedAddMarket(Set<String> set, String marketId) {
    //     if (this.marketIds.containsKey(marketId)) {
    //         return false;
    //     } else {
    //         return set.add(marketId);
    //     }
    // }
    // public static boolean checkedAddMarket(SynchronizedSet<String> set, String marketId, long checkTime) {
    //     if (this.marketIdsShort.containsKey(marketId)) {
    //         final long timeShortLeft = this.marketIdsShort.get(marketId) - checkTime;
    //         if (timeShortLeft >= 0L) {
    //             logger.warn("market {} still blacklisted for {} ms", marketId, timeShortLeft);
    //             return false;
    //         } else {
    //             return set.add(marketId);
    //         }
    //     } else {
    //         return set.add(marketId);
    //     }
    // }
    private static <T> int setIgnored(Class<? extends Ignorable> clazz, T key) {
        return setIgnored(clazz, key, BlackList.defaultSafetyPeriod);
    }

    private static <T> int setIgnored(Class<? extends Ignorable> clazz, T key, long safetyPeriod) {
        final long currentTime = System.currentTimeMillis();
        return setIgnored(clazz, key, safetyPeriod, currentTime);
    }

    private static <T> int setIgnored(Class<? extends Ignorable> clazz, T key, long safetyPeriod, long currentTime) {
        @SuppressWarnings("unchecked")
        SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);

        return setIgnored(synchronizedMap, key, safetyPeriod, currentTime);
    }

    private static <T> int setIgnored(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key) {
        return setIgnored(synchronizedMap, key, BlackList.defaultSafetyPeriod);
    }

    private static <T> int setIgnored(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key, long safetyPeriod) {
        final long currentTime = System.currentTimeMillis();
        return setIgnored(synchronizedMap, key, safetyPeriod, currentTime);
    }

    private static <T> int setIgnored(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key, long safetyPeriod, long currentTime) {
        int modified;

        if (synchronizedMap == null) {
            logger.error("null synchronizedMap in setIgnored for {} {} {}", key, safetyPeriod, currentTime);
            modified = -10000;
        } else if (synchronizedMap.containsKey(key)) {
            Ignorable value = synchronizedMap.get(key);
            if (value == null) {
                logger.error("null value in setIgnored for key: {} period: {}", key, safetyPeriod);
                modified = -10000; // there's an error
            } else {
                modified = value.setIgnored(safetyPeriod, currentTime);
            }
        } else {
            logger.error("attempted to setIgnored on non existing key: {} period: {}", key, safetyPeriod);
            modified = -10000; // not found
        }

        return modified;
    }

    public static <T> void printNotExistOrBannedErrorMessages(Class<? extends Ignorable> clazz, T key, String format) {
        @SuppressWarnings("unchecked")
        SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        final long currentTime = System.currentTimeMillis();

        printNotExistOrBannedErrorMessages(synchronizedMap, key, currentTime, Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, format);
    }

    public static <T> void printNotExistOrBannedErrorMessages(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key, String format) {
        final long currentTime = System.currentTimeMillis();
        printNotExistOrBannedErrorMessages(synchronizedMap, key, currentTime, Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, format);
    }

    public static <T> void printNotExistOrBannedErrorMessages(Class<? extends Ignorable> clazz, T key, long currentTime, String format) {
        @SuppressWarnings("unchecked")
        SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        printNotExistOrBannedErrorMessages(synchronizedMap, key, currentTime, Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, format);
    }

    public static <T> void printNotExistOrBannedErrorMessages(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key, long currentTime, String format) {
        printNotExistOrBannedErrorMessages(synchronizedMap, key, currentTime, Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, format);
    }

    public static <T> void printNotExistOrBannedErrorMessages(Class<? extends Ignorable> clazz, T key, long currentTime, long safetyPeriod, String format) {
        @SuppressWarnings("unchecked")
        SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        printNotExistOrBannedErrorMessages(synchronizedMap, key, currentTime, safetyPeriod, format);
    }

    public static <T> void printNotExistOrBannedErrorMessages(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key, long currentTime, long safetyPeriod, String format) {
        if (BlackList.notExist(synchronizedMap, key)) {
            final long timeSinceLastRemoved = BlackList.timeSinceRemovalFromMap(synchronizedMap, currentTime);
            final String printedString =
                    MessageFormatter.arrayFormat("{} no value in map, timeSinceLastRemoved: {} for key: {}",
                            new Object[]{format, timeSinceLastRemoved, key}).getMessage();
            if (timeSinceLastRemoved <= safetyPeriod) {
                logger.info(printedString);
            } else {
                logger.error(printedString);
            }
        } else {
            final long timeSinceBan = BlackList.timeSinceBan(synchronizedMap, currentTime);
            final String printedString = MessageFormatter.arrayFormat("{} ignored for key: {} {}",
                    new Object[]{format, timeSinceBan, key}).getMessage();
            if (timeSinceBan <= safetyPeriod) {
                logger.info(printedString);
            } else {
                logger.error(printedString);
            }
        }
    }

    public static <T> long timeSinceBan(Ignorable ignorable, T key) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceBan(ignorable, key, currentTime);
    }

    public static <T> long timeSinceBan(Ignorable ignorable, T key, long currentTime) {
        final long result;
        if (ignorable == null) {
            logger.error("null ignorable in timeSinceBan for {} {}", key, currentTime);
            result = Long.MAX_VALUE;
        } else {
            final Class<? extends Ignorable> clazz = ignorable.getClass();
            result = timeSinceBan(clazz, key, currentTime);
        }

        return result;
    }

    public static <T> long timeSinceBan(Class<? extends Ignorable> clazz, T key) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceBan(clazz, key, currentTime);
    }

    public static <T> long timeSinceBan(Class<? extends Ignorable> clazz, T key, long currentTime) {
        @SuppressWarnings("unchecked")
        SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        return timeSinceBan(synchronizedMap, key, currentTime);
    }

    public static <T> long timeSinceBan(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceBan(synchronizedMap, key, currentTime);
    }

    public static <T> long timeSinceBan(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key, long currentTime) {
        final long result;

        if (synchronizedMap == null) {
            logger.error("null synchronizedMap in timeSinceBan for {} {}", key, currentTime);
            result = Long.MAX_VALUE;
        } else if (synchronizedMap.containsKey(key)) {
            Ignorable value = synchronizedMap.get(key);
            if (value == null) {
                logger.error("null value in timeSinceBan for key {} {}", key, currentTime);
                result = Long.MAX_VALUE;
            } else {
                result = value.timeSinceBan(currentTime); // it does exist
            }
        } else {
            result = Long.MAX_VALUE;
        }

        return result;
    }

    public static <T> long timeSinceRemovalFromMap(Ignorable ignorable) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceRemovalFromMap(ignorable, currentTime);
    }

    public static <T> long timeSinceRemovalFromMap(Ignorable ignorable, long currentTime) {
        final long result;
        if (ignorable == null) {
            logger.error("null ignorable in timeSinceRemovalFromMap for {}");
            result = Long.MAX_VALUE;
        } else {
            final Class<? extends Ignorable> clazz = ignorable.getClass();
            result = timeSinceRemovalFromMap(clazz, currentTime);
        }

        return result;
    }

    public static <T> long timeSinceRemovalFromMap(Class<? extends Ignorable> clazz) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceRemovalFromMap(clazz, currentTime);
    }

    public static <T> long timeSinceRemovalFromMap(Class<? extends Ignorable> clazz, long currentTime) {
        @SuppressWarnings("unchecked")
        SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        return timeSinceRemovalFromMap(synchronizedMap, currentTime);
    }

    public static <T> long timeSinceRemovalFromMap(SynchronizedMap<T, ? extends Ignorable> synchronizedMap) {
        final long currentTime = System.currentTimeMillis();
        return timeSinceRemovalFromMap(synchronizedMap, currentTime);
    }

    public static <T> long timeSinceRemovalFromMap(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, long currentTime) {
        final long result;

        if (synchronizedMap == null) {
            logger.error("null synchronizedMap in timeSinceRemovalFromMap for {}");
            result = Long.MAX_VALUE;
        } else {
            final long timeStampRemoved = synchronizedMap.getTimeStampRemoved();
//            final long currentTime = System.currentTimeMillis();
            result = currentTime - timeStampRemoved;
        }

        return result;
    }

    public static <T> boolean notExist(Ignorable ignorable, T key) {
        final boolean result;
        if (ignorable == null) {
            logger.error("null ignorable in notExist for {}", key);
            result = true; // it's true that there's an error
        } else {
            final Class<? extends Ignorable> clazz = ignorable.getClass();
            result = notExist(clazz, key);
        }

        return result;
    }

    public static <T> boolean notExist(Class<? extends Ignorable> clazz, T key) {
        @SuppressWarnings("unchecked")
        SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        return notExist(synchronizedMap, key);
    }

    public static <T> boolean notExist(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key) {
        final boolean result;

        if (synchronizedMap == null) {
            logger.error("null synchronizedMap in notExist for {}", key);
            result = true; // it's true that it doesn't exist
        } else if (synchronizedMap.containsKey(key)) {
            Ignorable value = synchronizedMap.get(key);
            if (value == null) {
                logger.error("null value in notExist for key {}", key);
                result = true; // it's true that there's an error
            } else {
//                result = value.isIgnored(currentTime);
                result = false; // it does exist
            }
        } else {
            result = true; // it's true that it doesn't exist
        }

        return result;
    }

    public static <T> boolean notExistOrIgnored(Ignorable ignorable, T key) {
        final long currentTime = System.currentTimeMillis();
        return notExistOrIgnored(ignorable, key, currentTime);
    }

    public static <T> boolean notExistOrIgnored(Ignorable ignorable, T key, long currentTime) {
        final boolean result;
        if (ignorable == null) {
            logger.error("null ignorable in notExistOrIgnored for {} {}", key, currentTime);
            result = true; // it's true that there's an error
        } else {
            final Class<? extends Ignorable> clazz = ignorable.getClass();
            result = notExistOrIgnored(clazz, key, currentTime);
        }

        return result;
    }

    public static <T> boolean notExistOrIgnored(Class<? extends Ignorable> clazz, T key) {
        final long currentTime = System.currentTimeMillis();
        return notExistOrIgnored(clazz, key, currentTime);
    }

    public static <T> boolean notExistOrIgnored(Class<? extends Ignorable> clazz, T key, long currentTime) {
        @SuppressWarnings("unchecked")
        SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
        return notExistOrIgnored(synchronizedMap, key, currentTime);
    }

    public static <T> boolean notExistOrIgnored(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key) {
        final long currentTime = System.currentTimeMillis();
        return notExistOrIgnored(synchronizedMap, key, currentTime);
    }

    public static <T> boolean notExistOrIgnored(SynchronizedMap<T, ? extends Ignorable> synchronizedMap, T key, long currentTime) {
        final boolean result;

        if (synchronizedMap == null) {
            logger.error("null synchronizedMap in notExistOrIgnored for {} {}", key, currentTime);
            result = true; // it's true that it doesn't exist
        } else if (synchronizedMap.containsKey(key)) {
            Ignorable value = synchronizedMap.get(key);
            if (value == null) {
                logger.error("null value in notExistOrIgnored for key {}", key);
                result = true; // it's true that there's an error
            } else {
                result = value.isIgnored(currentTime);
            }
        } else {
            result = true; // it's true that it doesn't exist
        }

        return result;
    }

    public static int removeScrapers(LinkedHashMap<Class<? extends ScraperEvent>, Long> map) {
        int nRemoved = 0;
        if (map == null) {
            logger.error("null map in removeScrapers");
        } else {
            final Set<Entry<Class<? extends ScraperEvent>, Long>> entrySet = map.entrySet();
            for (Entry<Class<? extends ScraperEvent>, Long> entry : entrySet) {
                final Class<? extends ScraperEvent> clazz = entry.getKey();
                final Long id = entry.getValue();
                final ScraperEvent returnValue = removeScraper(clazz, id);
                if (returnValue == null) {
                    logger.error("scraperEvent not removed in removeScrapers for: {} {}", clazz.getSimpleName(), id);
                } else {
                    nRemoved++;
                }
            } // end for
        }

        return nRemoved;
    }

    public static <T extends ScraperEvent> T removeScraper(Class<T> clazz, Long scraperEventId) {
        @SuppressWarnings("unchecked")
        final SynchronizedMap<Long, T> scraperEventsMap = (SynchronizedMap<Long, T>) Formulas.getScraperEventsMap(clazz);
        final T existingScraperEvent;
        if (scraperEventId != null && scraperEventId >= 0) {
            existingScraperEvent = scraperEventsMap.get(scraperEventId);
        } else {
            logger.error("null or negative scraperEventId {} in removeScraper for: {}", scraperEventId, clazz.getSimpleName());
            existingScraperEvent = null;
        }

        if (existingScraperEvent != null) {
            scraperEventsMap.remove(scraperEventId);

            final String matchedEventId = existingScraperEvent.getMatchedEventId();
            if (matchedEventId != null) {
                BlackList.checkEventNMatched(matchedEventId, clazz);
            } else { // no event attached, nothing to be done
            }
        } else {
            logger.error("no existingScraperEvent found in removeScraper for: {} {}", clazz.getSimpleName(), scraperEventId);
        }

        return existingScraperEvent;
    }

    public static <T extends ScraperEvent> T removeScraper(T scraperEvent) {
        final T existingScraperEvent;
        if (scraperEvent == null) {
            logger.error("null scraperEvent in removeScraper");
            existingScraperEvent = null;
        } else {
            final long scraperEventId = scraperEvent.getEventId();
//        scraperEvent.setIgnored(safetyPeriod);

            @SuppressWarnings("unchecked")
            final Class<T> clazz = (Class<T>) scraperEvent.getClass();
            @SuppressWarnings("unchecked")
            final SynchronizedMap<Long, T> scraperEventsMap = (SynchronizedMap<Long, T>) Formulas.getScraperEventsMap(clazz);
            if (scraperEventId >= 0) {
                existingScraperEvent = scraperEventsMap.get(scraperEventId);

//            if (scraperEvent instanceof BetradarEvent) {
////                this.putScraperId("betradar", this.betradarIds, scraperEventId, blackListPeriod);
////                existingScraperEvent = Statics.betradarEventsMap.remove(scraperEventId);
//                existingScraperEvent = Statics.betradarEventsMap.get(scraperEventId);
//            } else if (scraperEvent instanceof CoralEvent) {
////                this.putScraperId("coral", this.coralIds, scraperEventId, blackListPeriod);
////                existingScraperEvent = Statics.coralEventsMap.remove(scraperEventId);
//                existingScraperEvent = Statics.coralEventsMap.get(scraperEventId);
//            } else {
//                logger.error("unknown removeScraper scraperEvent class: {} {} {}", scraperEvent.getClass(), scraperEventId, Generic.objectToString(scraperEvent));
//                existingScraperEvent = null;
//            }
            } else {
                logger.error("negative scraperEventId {} in removeScraper for: {}", scraperEventId, scraperEvent);
                existingScraperEvent = null;
            }
//        if (existingScraperEvent != null && existingScraperEvent != scraperEvent) {
//            existingScraperEvent.setIgnored(safetyPeriod);
//        }
            if (existingScraperEvent != null) {
                scraperEventsMap.remove(scraperEventId);
            }

            String matchedEventId = null;
            if (existingScraperEvent != null) {
                matchedEventId = existingScraperEvent.getMatchedEventId();
            } else { // no ScraperEvent present in the map, nothing to be done here
            }
            if (matchedEventId == null) {
                matchedEventId = scraperEvent.getMatchedEventId();
            } else { // using the matchedEventId I have
            }

            if (matchedEventId != null) {
//            ignoreEvent(matchedEventId, safetyPeriod, shortSafetyPeriod);
                BlackList.checkEventNMatched(matchedEventId, clazz);
            } else { // no event attached, nothing to be done
            }
        }

        return existingScraperEvent;
    }

//    public static void tempDeleteScraper(ScraperEvent scraperEvent) {
//        ignoreScraper(scraperEvent, Ignorable.TEMP_REMOVED);
//    }
    public static void ignoreScraper(ScraperEvent scraperEvent) {
        ignoreScraper(scraperEvent, BlackList.defaultSafetyPeriod);
    }

//    public static void ignoreScraper(ScraperEvent scraperEvent, long blackListPeriod) {
//        if (blackListPeriod > 0L) {
//            ignoreScraper(scraperEvent, blackListPeriod, this.defaultSafetyPeriod, this.defaultShortSafetyPeriod);
//        } else {
//            ignoreScraper(scraperEvent, blackListPeriod, blackListPeriod, blackListPeriod);
//        }
//    }
    public static void ignoreScraper(ScraperEvent scraperEvent, long safetyPeriod) {
        if (scraperEvent == null) {
            logger.error("null scraperEvent in ignoreScraper");
        } else {
            final long scraperEventId = scraperEvent.getEventId();
            final long currentTime = System.currentTimeMillis();
            scraperEvent.setIgnored(safetyPeriod, currentTime);

            final Class<? extends ScraperEvent> clazz = scraperEvent.getClass();
            final SynchronizedMap<Long, ? extends ScraperEvent> scraperEventsMap = Formulas.getScraperEventsMap(clazz);
            ScraperEvent existingScraperEvent;
            if (scraperEventId >= 0) {
                existingScraperEvent = scraperEventsMap.get(scraperEventId);

//            if (scraperEvent instanceof BetradarEvent) {
////                this.putScraperId("betradar", this.betradarIds, scraperEventId, blackListPeriod);
////                existingScraperEvent = Statics.betradarEventsMap.remove(scraperEventId);
//                existingScraperEvent = Statics.betradarEventsMap.get(scraperEventId);
//            } else if (scraperEvent instanceof CoralEvent) {
////                this.putScraperId("coral", this.coralIds, scraperEventId, blackListPeriod);
////                existingScraperEvent = Statics.coralEventsMap.remove(scraperEventId);
//                existingScraperEvent = Statics.coralEventsMap.get(scraperEventId);
//            } else {
//                logger.error("unknown ignoreScraper scraperEvent class: {} {} {}", scraperEvent.getClass(), scraperEventId, Generic.objectToString(scraperEvent));
//                existingScraperEvent = null;
//            }
            } else {
                logger.error("negative scraperEventId {} in ignoreScraper for: {}", scraperEventId, scraperEvent);
                existingScraperEvent = null;
            }
            if (existingScraperEvent != null && existingScraperEvent != scraperEvent) {
                existingScraperEvent.setIgnored(safetyPeriod, currentTime);
            }

            String matchedEventId = null;
            if (existingScraperEvent != null) {
                matchedEventId = existingScraperEvent.getMatchedEventId();
            } else { // no ScraperEvent present in the map, nothing to be done here
            }
            if (matchedEventId == null) {
                matchedEventId = scraperEvent.getMatchedEventId();
            } else { // using the matchedEventId I have
            }

//            String matchedEventId = scraperEvent.getMatchedEventId();
//            if (matchedEventId == null) {
//                if (existingScraperEvent != null) {
//                    matchedEventId = existingScraperEvent.getMatchedEventId();
//                } else { // no ScraperEvent present in the map, nothing to be done here
//                }
//            } else { // using the matchedEventId I have
//            }
            if (matchedEventId != null) {
//            ignoreEvent(matchedEventId, safetyPeriod, shortSafetyPeriod);
                BlackList.checkEventNMatched(matchedEventId);

                // moved to ScraperEvent.setIgnored
//                final Event event = Statics.eventsMap.get(matchedEventId);
//                if (event != null) {
//                    final HashSet<Event> attachedEvents = new HashSet<>(2);
//                    attachedEvents.add(event);
//                    logger.info("ignoreScraper {} toCheckEvent: {} delay: {} launch: findSafeRunners", scraperEventId, matchedEventId, safetyPeriod);
//                    Statics.threadPoolExecutor.execute(new LaunchCommandThread("findSafeRunners", attachedEvents, safetyPeriod));
//                } else { // error message is printed in checkEventNMatched, nothing to be done
//                }
            } else { // no event attached, nothing to be done
            }
        }
    }

    public static int checkEventNMatched(String eventId) {
        final Class<? extends ScraperEvent> clazzToRemove = null; // by default no remove
        return checkEventNMatched(eventId, clazzToRemove);
    }

    public static int checkEventNMatched(String eventId, Class<? extends ScraperEvent> clazzToRemove) {
        int nMatched;
        final Event event = Statics.eventsMap.get(eventId);
        if (event != null) {
            if (clazzToRemove != null) {
                event.removeScraperEventId(clazzToRemove);
            } else {
                // not removing anything
            }
            nMatched = event.getNScraperEventIds();

            if (nMatched < Statics.MIN_MATCHED) {
                tempRemoveMarkets(event, nMatched);
            } else {
                checkSafeRunners(event);
            }
        } else {
            logger.error("null event value found in eventsMap during checkEventNMatched for: {}", eventId);
            Statics.eventsMap.removeValueAll(null);
            nMatched = -100;
        }
        return nMatched;
    }

    public static Event removeEvent(Event event) {
        Event existingEvent;
        if (event == null) {
            logger.error("null event in removeEvent");
            existingEvent = null;
        } else {
            final String eventId = event.getId();
            existingEvent = removeEvent(eventId);
            if (event != existingEvent) {
                logger.error("removeEvent on an event different than the one existing in the map; this is probably not intended behaviour and is not supported: {} {} {}", eventId,
                        Generic.objectToString(event), Generic.objectToString(existingEvent));
                existingEvent = null; // not supported
                // not supported, and probably not needed; should be invoked on existingEvent or eventId
            } else {
                // already removed, nothing to be done
            }
        }

        return existingEvent;
    }

    public static Event removeEvent(String eventId) {
        final Event matchedEvent = Statics.eventsMap.remove(eventId);

        if (matchedEvent != null) {
            final int nScraperEventIds = matchedEvent.getNScraperEventIds();
            final Set<Entry<String, MarketCatalogue>> entrySetCopy = Statics.marketCataloguesMap.entrySetCopy();
            final AtomicInteger nRemovedMarkets = new AtomicInteger();
            for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
                final MarketCatalogue marketCatalogue = entry.getValue();
                if (marketCatalogue != null) {
                    final Event eventStump = marketCatalogue.getEventStump();
                    if (matchedEvent.equals(eventStump)) { // it doesn't matter if it's null
                        final String marketId = entry.getKey();
                        removeMarket(marketId);
                        nRemovedMarkets.incrementAndGet();
                    } else { // not interesting marketCatalogue, nothing to be done
                    }
                } else {
                    logger.error("null marketCatalogue value found in marketCataloguesMap during removeEvent for: {}", entry.getKey());
                    Statics.marketCataloguesMap.removeValueAll(null);
                }
            } // end for
            if (nScraperEventIds > 0) {
                final LinkedHashMap<Class<? extends ScraperEvent>, Long> scraperEventIds = matchedEvent.getScraperEventIds();
                final Set<Entry<Class<? extends ScraperEvent>, Long>> entrySet = scraperEventIds.entrySet();
                for (final Entry<Class<? extends ScraperEvent>, Long> entry : entrySet) {
                    final Class<? extends ScraperEvent> clazz = entry.getKey();
                    final Long scraperId = entry.getValue();
                    final SynchronizedMap<Long, ? extends ScraperEvent> scraperEventMap = Formulas.getScraperEventsMap(clazz);
                    final ScraperEvent scraperEvent = scraperEventMap.get(scraperId);
                    if (scraperEvent != null) {
                        final String matchedEventId = scraperEvent.getMatchedEventId();
                        if (matchedEventId.equals(eventId)) {
                            scraperEvent.resetMatchedEventId();
                        } else {
                            logger.error("matchedEventId {} not equals eventId {} in removeEvent for: {} {}", matchedEventId, eventId,
                                    Generic.objectToString(matchedEvent), Generic.objectToString(scraperEvent));
                        }
                    } else { // scraperEvent not found in map; might be acceptable, will reevaluate after adding ignore list
                    }
                } // end for
            } else { // not matched scrapers, nothing to be done about it
            }

            if (nRemovedMarkets.get() == 0 && nScraperEventIds >= Statics.MIN_MATCHED) {
                final long currentTime = System.currentTimeMillis();
                final long timeSinceEventsMapRemoval = currentTime - Statics.eventsMap.getTimeStampRemoved();
                if (timeSinceEventsMapRemoval < 200L) {
                    logger.info("no marketIds found while purging matched event: {}", eventId);
                } else {
                    logger.error("no marketIds found while purging matched event: {} {}", eventId, Generic.objectToString(matchedEvent));
                }

            } else { // not enough scrapers matched to have associated marketIds
            }
        } else {
            logger.error("null event value found in eventsMap during removeEvent for: {}", eventId);
            Statics.eventsMap.removeValueAll(null);
        }

        return matchedEvent;
    }

//    public static void tempDeleteEvent(String eventId) {
//        ignoreEvent(eventId, Ignorable.TEMP_REMOVED);
//    }
    public static void ignoreEvent(String eventId) {
        ignoreEvent(eventId, BlackList.defaultSafetyPeriod);
    }

    public static void ignoreEvent(String eventId, long safetyPeriod) {
//        BlackList.setIgnored(Statics.eventsMap, eventId, safetyPeriod);
//        final Event matchedEvent = Statics.eventsMap.remove(eventId);
        final Event matchedEvent = Statics.eventsMap.get(eventId);

        if (matchedEvent != null) {
            final long currentTime = System.currentTimeMillis();
            matchedEvent.setIgnored(safetyPeriod, currentTime);

            // moved to Event.setIgnored
//            final HashSet<Event> attachedEvents = new HashSet<>(2);
//            attachedEvents.add(matchedEvent);
//            logger.info("ignoreEvent toCheckEvent: {} delay: {} launch: findSafeRunners", eventId, safetyPeriod);
//            Statics.threadPoolExecutor.execute(new LaunchCommandThread("findSafeRunners", attachedEvents, safetyPeriod));
//            final HashSet<String> toRemoveSet = new HashSet<>(8);
            final int nScraperEventIds = matchedEvent.getNScraperEventIds();
            final Set<Entry<String, MarketCatalogue>> entrySetCopy = Statics.marketCataloguesMap.entrySetCopy();
            final AtomicInteger nIgnoredMarkets = new AtomicInteger();
            for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
                final MarketCatalogue marketCatalogue = entry.getValue();
                if (marketCatalogue != null) {
                    final Event eventStump = marketCatalogue.getEventStump();
                    if (matchedEvent.equals(eventStump)) { // it doesn't matter if it's null
                        final String marketId = entry.getKey();
                        ignoreMarket(marketId, safetyPeriod, currentTime);
                        nIgnoredMarkets.incrementAndGet();
//                                this.putMarketId(marketId, safetyPeriod, shortSafetyPeriod);
//                                Statics.marketCataloguesMap.remove(marketId);
//                                toRemoveSet.add(marketId);
                    } else { // not interesting marketCatalogue, nothing to be done
                    }
                } else {
                    logger.error("null marketCatalogue value found in marketCataloguesMap during ignoreEvent for: {}", entry.getKey());
                    Statics.marketCataloguesMap.removeValueAll(null);
                }
            } // end for
            if (nScraperEventIds > 0) {
                final LinkedHashMap<Class<? extends ScraperEvent>, Long> scraperEventIds = matchedEvent.getScraperEventIds();
                final Set<Entry<Class<? extends ScraperEvent>, Long>> entrySet = scraperEventIds.entrySet();
                for (final Entry<Class<? extends ScraperEvent>, Long> entry : entrySet) {
                    final Class<? extends ScraperEvent> clazz = entry.getKey();
                    final Long scraperId = entry.getValue();
                    final SynchronizedMap<Long, ? extends ScraperEvent> scraperEventMap = Formulas.getScraperEventsMap(clazz);
                    final ScraperEvent scraperEvent = scraperEventMap.get(scraperId);
                    if (scraperEvent != null) {
                        final String matchedEventId = scraperEvent.getMatchedEventId();
                        if (matchedEventId == null || !matchedEventId.equals(eventId)) {
                            logger.error("matchedEventId {} not equals eventId {} in ignoreEvent for: {} {}", matchedEventId, eventId, Generic.objectToString(matchedEvent),
                                    safetyPeriod);
                        } else {
//                            scraperEvent.resetMatchedEventId();
                            // will not reset matchedEvent; nothing to be done; this whole block might not be necessary
                        }
                    } else { // scraperEvent not found in map; might be acceptable, will reevaluate after adding ignore list
                    }
                } // end for
            } else { // not matched scrapers, nothing to be done about it
            }
            if (nIgnoredMarkets.get() == 0 && nScraperEventIds >= Statics.MIN_MATCHED) {
                logger.error("no marketIds found while purging matched event: {} {}", eventId, Generic.objectToString(matchedEvent));
            } else { // not enough scrapers matched to have associated marketIds
            }
//            if (!toRemoveSet.isEmpty()) {
//                toRemoveSet.stream().
//                        map((marketId) -> Statics.safeBetsMap.remove(marketId)).
//                        forEach((safeBetsStatsMap) -> {
//                            // will be null if no such key exists
//
//                            if (safeBetsStatsMap != null && !safeBetsStatsMap.isEmpty()) {
//                                safeBetsStatsMap.entrySet().stream().
//                                forEach((entry) -> {
//                                    final SafeBet key = entry.getKey();
//                                    final SafeBetStats value = entry.getValue();
//                                    if (value != null) {
//                                        String printedString = MessageFormatter.arrayFormat("blackList {} {} {} {}", new Object[]{value.printStats(), key.printStats(),
//                                            Generic.objectToString(key), Generic.objectToString(value)}).getMessage();
//                                        logger.warn(printedString);
//                                        Statics.safebetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");
//                                    } else {
//                                        logger.error("null SafeBetStats in safeBetsStatsMap for: {}", Generic.objectToString(key));
//                                    }
//                                }); // end for
//                            } else { // no safeBetsStatsMap exists, nothing to be done
//                            }
//                        }); // end for
//
//                // Statics.interestingMarketsSet.removeAll(toRemoveSet);
//                Statics.safeMarketsMap.removeAllKeys(toRemoveSet);
//                Statics.safeMarketBooksMap.removeAllKeys(toRemoveSet);
//                Statics.safeMarketsImportantMap.removeAllKeys(toRemoveSet);
//            } else {
//                if (matchedEvent.getNScraperEventIds() >= Statics.MIN_MATCHED) {
//                    logger.error("no marketIds found for: {} {}", eventId, Generic.objectToString(matchedEvent));
//                } else { // not enough scrapers matched to have associated marketIds
//                }
//            }
        } else {
            logger.error("null event value found in eventsMap during ignoreEvent for: {}", eventId);
            Statics.eventsMap.removeValueAll(null);
        }
    }

    public static int checkSafeRunners(Event event) {
        // checks safeRunners of an Event, by running safeRunner.checkScrapers on them, which can remove or ignore the safeRunners if needed
        int removedRunnerMatchedScrapers = 0;
        if (event == null) {
            logger.error("null event in checkSafeRunners");
        } else {
            final Set<Entry<String, MarketCatalogue>> entrySetCopy = Statics.marketCataloguesMap.entrySetCopy();
            for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
                final MarketCatalogue marketCatalogue = entry.getValue();
                if (marketCatalogue != null) {
                    final Event eventStump = marketCatalogue.getEventStump();
                    if (event.equals(eventStump)) { // it doesn't matter if marketCatalogueEvent is null
                        final String marketId = entry.getKey();
                        final SynchronizedSet<SafeRunner> safeRunnersSet = Statics.safeMarketsMap.get(marketId);
                        synchronized (safeRunnersSet) {
                            final HashSet<SafeRunner> safeRunnersSetCopy = safeRunnersSet.copy();
                            for (SafeRunner safeRunner : safeRunnersSetCopy) {
                                removedRunnerMatchedScrapers += safeRunner.checkScrapers();
                            }
                        } // end synchronized
//                        if (removeMarket(marketId) != null) {
//                            nPurgedMarkets++;
//                        } else {
//                            logger.error("couldn't removeMarket in removeMarkets: {} {} {}", marketId, Generic.objectToString(marketCatalogue), Generic.objectToString(event));
//                        }
                    } else { // not interesting marketCatalogue, nothing to be done
                    }
                } else {
                    logger.error("null marketCatalogue value found in marketCataloguesMap during removeMarkets for: {}", entry.getKey());
                    Statics.marketCataloguesMap.removeValueAll(null);
                }
            } // end for
        }

        return removedRunnerMatchedScrapers;
    }

    public static int removeMarkets(Event event) {
        int nPurgedMarkets = 0;
        if (event == null) {
            logger.error("null event in removeMarkets");
        } else {
            final Set<Entry<String, MarketCatalogue>> entrySetCopy = Statics.marketCataloguesMap.entrySetCopy();
            for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
                final MarketCatalogue marketCatalogue = entry.getValue();
                if (marketCatalogue != null) {
                    final Event eventStump = marketCatalogue.getEventStump();
                    if (event.equals(eventStump)) { // it doesn't matter if marketCatalogueEvent is null
                        final String marketId = entry.getKey();
                        if (removeMarket(marketId) != null) {
                            nPurgedMarkets++;
                        } else {
                            logger.error("couldn't removeMarket in removeMarkets: {} {} {}", marketId, Generic.objectToString(marketCatalogue), Generic.objectToString(event));
                        }
                    } else { // not interesting marketCatalogue, nothing to be done
                    }
                } else {
                    logger.error("null marketCatalogue value found in marketCataloguesMap during removeMarkets for: {}", entry.getKey());
                    Statics.marketCataloguesMap.removeValueAll(null);
                }
            } // end for
        }

        return nPurgedMarkets;
    }

    public static MarketCatalogue removeMarket(String marketId) {
        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.remove(marketId);

        removeFromSecondaryMaps(marketId);

        return marketCatalogue;
    }

    public static boolean isTempRemovedMarket(String marketId) {
        boolean isTempRemoved;
        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.get(marketId);
        if (marketCatalogue != null) {
            isTempRemoved = marketCatalogue.isTempRemoved();
        } else {
            logger.error("attempted isTempRemovedMarket on non existing marketId: {}", marketId);
            isTempRemoved = false;
        }

        return isTempRemoved;
    }

    public static int resetTempRemovedMarkets(Event event) {
        int nResetTempRemovedMarkets = 0;
        if (event == null) {
            logger.error("null event in resetTempRemovedMarkets");
        } else {
            final Set<Entry<String, MarketCatalogue>> entrySetCopy = Statics.marketCataloguesMap.entrySetCopy();
            for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
                final MarketCatalogue marketCatalogue = entry.getValue();
                if (marketCatalogue != null) {
                    final Event eventStump = marketCatalogue.getEventStump();
                    if (event.equals(eventStump)) { // no need for eventStump null check
                        final int marketWasModifiedResettingTempRemove = marketCatalogue.resetTempRemoved(event);
                        if (marketWasModifiedResettingTempRemove > 0) {
                            nResetTempRemovedMarkets++;
                        } else {
                            // this might be normal behaviour in case of some markets
                            // like the markets added to the event after the tempRemoved being set and added to the marketCatalogues map before this method being invoked
                        }
                    } else { // not interesting marketCatalogue, nothing to be done
                    }
                } else {
                    logger.error("null marketCatalogue value found in marketCataloguesMap during resetTempRemovedMarkets for: {}", entry.getKey());
                    Statics.marketCataloguesMap.removeValueAll(null);
                }
            } // end for
        }

        return nResetTempRemovedMarkets;
    }

    public static int tempRemoveMarkets(Event event, int nMatched) {
        int nTempRemovedMarkets = 0;
        if (event == null) {
            logger.error("null event in tempRemoveMarkets");
        } else {
            final Set<Entry<String, MarketCatalogue>> entrySetCopy = Statics.marketCataloguesMap.entrySetCopy();
            for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
                final MarketCatalogue marketCatalogue = entry.getValue();
                if (marketCatalogue != null) {
                    final Event eventStump = marketCatalogue.getEventStump();
                    if (event.equals(eventStump)) { // no need for eventStump null check
                        final String marketId = entry.getKey();

                        // normal behavior is:
                        // marketWasModifiedSettingTempRemove > 0 if nMatched == Statics.MIN_MATCHED - 1 (market had enough scrapers before and now it doesn't)
                        // marketWasModifiedSettingTempRemove == 0 if nMatched < Statics.MIN_MATCHED - 1 (market had insufficient scrapers before and now has even less)
                        final int marketWasModifiedSettingTempRemove = tempRemoveMarket(marketId);
                        if (marketWasModifiedSettingTempRemove > 0) {
                            nTempRemovedMarkets++;
                            if (nMatched < Statics.MIN_MATCHED - 1) {
                                logger.error("removeMarket in tempRemoveMarkets: {} {} {} {}", marketId, marketCatalogue.isTempRemoved(),
                                        Generic.objectToString(marketCatalogue), Generic.objectToString(event));
                            } else { // normal behaviour
                            }
                        } else {
                            // it means it was already tempRemoved, but this actually can happen if the event had insufficient scrapers already (1 scraper before, and now has 0)
                            // might be normal behavior anyway, for example if ScraperEvent was first ignored and then removed, and tempRemove is invoked both times
                            // there might be other cases when this could be normal behavior either way
//                            if (nMatched == Statics.MIN_MATCHED - 1) {
//                                logger.error("couldn't removeMarket in tempRemoveMarkets: {} {} {} {}", marketId, marketCatalogue.isTempRemoved(),
//                                        Generic.objectToString(marketCatalogue), Generic.objectToString(event));
//                            } else { // normal behaviour
//                            }
                        }
                    } else { // not interesting marketCatalogue, nothing to be done
                    }
                } else {
                    logger.error("null marketCatalogue value found in marketCataloguesMap during tempRemoveMarkets for: {}", entry.getKey());
                    Statics.marketCataloguesMap.removeValueAll(null);
                }
            } // end for
        }

        return nTempRemovedMarkets;
    }

    public static int ignoreMarkets(Set<String> marketIds, long safetyPeriod) {
        int nModified = 0;
        if (marketIds != null) {
            if (!marketIds.isEmpty()) {
                for (final String marketId : marketIds) {
                    if (ignoreMarket(marketId, safetyPeriod) > 0) {
                        nModified++;
                    }
                } // end for
            } else { // nothing to be done, no error either
            }
        } else {
            logger.error("null marketIds set in ignoreMarkets for period: {}", safetyPeriod);
        }

        return nModified;
    }

    public static int tempRemoveMarket(String marketId) {
//        return ignoreMarket(marketId, Ignorable.TEMP_REMOVED);
        int modified;
        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.get(marketId);
        if (marketCatalogue != null) {
            modified = marketCatalogue.setTempRemoved();
        } else {
            logger.error("attempted to tempRemoveMarket on non existing marketId: {}", marketId);
            modified = -10000; // not found
        }

        removeFromSecondaryMaps(marketId);

        return modified;
    }

    public static int ignoreMarket(String marketId) {
        return ignoreMarket(marketId, BlackList.defaultSafetyPeriod);
    }

    public static int ignoreMarket(String marketId, long safetyPeriod) {
        final long currentTime = System.currentTimeMillis();
        return ignoreMarket(marketId, safetyPeriod, currentTime);
    }

    public static int ignoreMarket(String marketId, long safetyPeriod, long currentTime) {
        int modified = BlackList.setIgnored(Statics.marketCataloguesMap, marketId, safetyPeriod, currentTime);
//        Statics.marketCataloguesMap.remove(marketId);

        removeFromSecondaryMaps(marketId);

        return modified;
    }

    public static void removeFromSecondaryMaps(String marketId) {
        final SynchronizedMap<SafeBet, SafeBetStats> safeBetsStatsMap = Statics.safeBetsMap.remove(marketId);
        if (safeBetsStatsMap != null && !safeBetsStatsMap.isEmpty()) {
            final Set<Entry<SafeBet, SafeBetStats>> entrySetCopy = safeBetsStatsMap.entrySetCopy();
            for (final Entry<SafeBet, SafeBetStats> entry : entrySetCopy) {
                final SafeBet key = entry.getKey();
                final SafeBetStats value = entry.getValue();
                if (value != null) {
                    String printedString = MessageFormatter.arrayFormat("blackRList {} {} {} {}", new Object[]{value.printStats(), key.printStats(),
                        Generic.objectToString(key), Generic.objectToString(value)}).getMessage();
                    logger.warn(printedString);
                    Statics.safebetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");
                } else {
                    logger.error("null SafeBetStats in safeBetsStatsMap for: {}", Generic.objectToString(key));
                }
            } // end for
        } else { // no safeBetsStatsMap exists, nothing to be done
        }

        // Statics.interestingMarketsSet.removeAll(toRemoveSet);
        Statics.safeMarketsMap.remove(marketId);
        Statics.safeMarketBooksMap.remove(marketId);
        Statics.safeMarketsImportantMap.remove(marketId);
    }

    public static boolean removeSafeRunner(String marketId, SafeRunner safeRunner) {
        boolean modified;
        final SynchronizedSet<SafeRunner> safeRunnersSet = Statics.safeMarketsMap.get(marketId);
        if (safeRunnersSet == null) {
            logger.error("null safeRunnersSet in removeSafeRunner for: {}", marketId);
            modified = false;
        } else {
            modified = safeRunnersSet.remove(safeRunner);
        }

        final SynchronizedMap<SafeBet, SafeBetStats> safeBetsStatsMap = Statics.safeBetsMap.get(marketId);
        if (safeRunner != null && safeBetsStatsMap != null && !safeBetsStatsMap.isEmpty()) {
            final Long safeRunnerId = safeRunner.getSelectionId();
            final Set<Entry<SafeBet, SafeBetStats>> entrySetCopy = safeBetsStatsMap.entrySetCopy();
            for (final Entry<SafeBet, SafeBetStats> entry : entrySetCopy) {
                final SafeBet key = entry.getKey();
                final SafeBetStats value = entry.getValue();
                if (key != null) {
                    final Long safeBetId = key.getRunnerId();
                    if (Objects.equals(safeRunnerId, safeBetId)) {
                        safeBetsStatsMap.remove(key);

                        if (value != null) {
                            String printedString = MessageFormatter.arrayFormat("blackRList {} {} {} {}", new Object[]{value.printStats(), key.printStats(),
                                Generic.objectToString(key), Generic.objectToString(value)}).getMessage();
                            logger.warn(printedString);
                            Statics.safebetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");
                        } else {
                            logger.error("null SafeBetStats in safeBetsStatsMap for: {} {}", marketId, Generic.objectToString(key));
                        }
                    }
                } else {
                    logger.error("null SafeBet in safeBetsStatsMap for: {} {}", marketId, Generic.objectToString(value));
                }
            } // end for
        } else { // no safeBetsStatsMap exists, nothing to be done
            if (safeRunner == null) {
                logger.error("null safeRunner in removeSafeRunner for: {}", marketId);
            }
        }

        return modified;
    }

    // not necessary; notExistOrIgnored includes this case
//    public static boolean marketIsIgnored(String marketId) {
//        boolean isIgnored;
//        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.get(marketId);
//        if (marketCatalogue == null) {
//            isIgnored = true;
//        } else {
//            isIgnored = marketCatalogue.isIgnored();
//        }
//
//        return isIgnored;
//    }
//    public static long timedCheckExpired() {
//        long timeTillNext;
//        final long currentTime = System.currentTimeMillis();
//
//        long timeTillCheckExpiredAll = (lastCheckExpiredAll + checkExpiredAllInterval) - currentTime;
//        if (timeTillCheckExpiredAll <= 0L) {
//            timeTillNext = checkExpiredInterval;
//            checkExpired(true);
//        } else {
//            long timeTillCheckExpired = (lastCheckExpired + checkExpiredInterval) - currentTime;
//            if (timeTillCheckExpired <= 0L) {
//                timeTillNext = Math.min(checkExpiredInterval, timeTillCheckExpiredAll);
//                checkExpired(false);
//            } else { // not the time to run yet
//                timeTillNext = Math.min(timeTillCheckExpired, timeTillCheckExpiredAll);
//            }
//        } // end else
//
//        return timeTillNext;
//    }
//
//    public static void checkExpired() {
//        checkExpired(false);
//    }
//
//    public static void checkExpired(boolean checkAll) {
//        final long currentTime = System.currentTimeMillis();
//        this.lastCheckExpired = currentTime;
//
//        int removedCounterCoral = 0, removedCounterBetradar = 0, removedCounterEvent = 0, removedCounterMarket = 0;
//        boolean betradarNotEmpty;
//        if (!this.betradarIds.isEmpty()) {
//            betradarNotEmpty = true;
//            final Iterator<Entry<Long, Long>> iterator = this.betradarIds.entrySet().iterator();
//            while (iterator.hasNext()) {
//                final Entry<Long, Long> entry = iterator.next();
//                final long value = entry.getValue();
//                if (value <= currentTime) {
//                    iterator.remove();
//                    logger.info("scraper {} removed from blackList", entry.getKey());
//                    removedCounterBetradar++;
//                }
//            } // end while
//        } else { // empty, nothing to be done
//            betradarNotEmpty = false;
//        }
//        boolean coralNotEmpty;
//        if (!this.coralIds.isEmpty()) {
//            coralNotEmpty = true;
//            final Iterator<Entry<Long, Long>> iterator = this.coralIds.entrySet().iterator();
//            while (iterator.hasNext()) {
//                final Entry<Long, Long> entry = iterator.next();
//                final long value = entry.getValue();
//                if (value <= currentTime) {
//                    iterator.remove();
//                    logger.info("scraper {} removed from blackList", entry.getKey());
//                    removedCounterCoral++;
//                }
//            } // end while
//        } else { // empty, nothing to be done
//            coralNotEmpty = false;
//        }
//
//        if (coralNotEmpty || betradarNotEmpty || checkAll) {
//            this.lastCheckExpiredAll = currentTime;
//
//            if (!this.eventIds.isEmpty()) {
//                final Iterator<Entry<String, Long>> iterator = this.eventIds.entrySet().iterator();
//                while (iterator.hasNext()) {
//                    final Entry<String, Long> entry = iterator.next();
//                    final long value = entry.getValue();
//                    if (value <= currentTime) {
//                        iterator.remove();
//                        final String eventId = entry.getKey();
//                        this.eventIdsShort.remove(eventId);
//                        logger.info("event {} removed from blackList", eventId);
//                        removedCounterEvent++;
//                    }
//                } // end while
//            } else { // empty, nothing to be done
//            }
//
//            if (!this.marketIds.isEmpty()) {
//                final Iterator<Entry<String, Long>> iterator = this.marketIds.entrySet().iterator();
//                while (iterator.hasNext()) {
//                    final Entry<String, Long> entry = iterator.next();
//                    final long value = entry.getValue();
//                    if (value <= currentTime) {
//                        iterator.remove();
//                        final String marketId = entry.getKey();
//                        this.marketIdsShort.remove(marketId);
//                        logger.info("market {} removed from blackList", marketId);
//                        removedCounterMarket++;
//                    }
//                } // end while
//            } else { // empty, nothing to be done
//            }
//        } else { // no need to check the other maps
//        }
//
//        if (removedCounterCoral > 0 || removedCounterBetradar > 0 || removedCounterEvent > 0 || removedCounterMarket > 0) {
//            if (removedCounterBetradar > 0) {
//                Statics.betradarScraperThread.timedScraperCounter.set(
//                        Statics.betradarScraperThread.timedScraperCounter.get() - Statics.betradarScraperThread.timedScraperCounter.get() % 10); // will run full next
//            }
//            if (removedCounterCoral > 0) {
//                Statics.coralScraperThread.timedScraperCounter.set(
//                        Statics.coralScraperThread.timedScraperCounter.get() - Statics.coralScraperThread.timedScraperCounter.get() % 10); // will run full next
//            }
//            if (removedCounterEvent > 0) {
//                Statics.timeStamps.setLastParseEventResultList(currentTime + Generic.MINUTE_LENGTH_MILLISECONDS);
//                Statics.threadPoolExecutor.execute(new LaunchCommandThread("parseEventResultList"));
//            }
//            if (removedCounterMarket > 0) {
//                Statics.timeStamps.setLastFindInterestingMarkets(currentTime + Generic.MINUTE_LENGTH_MILLISECONDS);
//                Statics.threadPoolExecutor.execute(new LaunchCommandThread("findInterestingMarkets"));
//            }
//
//            // if (removedCounterScraper == 0) {
//            //     logger.error("REVIEW removed {} {} {} entries from blackList", removedCounterScraper, removedCounterEvent, removedCounterMarket);
//            // } else {
//            logger.info("blackList removed: b{} c{} {} {} remain: b{} c{} {}/{} {}/{}", removedCounterBetradar, removedCounterCoral, removedCounterEvent, removedCounterMarket,
//                    this.betradarIds.size(), this.coralIds.size(), this.eventIds.size(), this.eventIdsShort.size(), this.marketIds.size(), this.marketIdsShort.size());
//            // }
//        }
//    }
//    public static boolean containsBetradarId(Long scraperId) {
//        return this.betradarIds.containsKey(scraperId);
//    }
//
//    public static boolean containsCoralId(Long scraperId) {
//        return this.coralIds.containsKey(scraperId);
//    }
//
//    public static boolean containsEventId(String eventId) {
//        return this.eventIds.containsKey(eventId);
//    }
//
//    public static boolean containsMarketId(String marketId) {
//        return this.marketIds.containsKey(marketId);
//    }
//
//    public static void putScraperId(String classId, Map<Long, Long> scraperIds, Long scraperId, long blackListPeriod) {
//        if (blackListPeriod > 0L) {
//            final long currentTime = System.currentTimeMillis();
//            final long timeBlackListExpires = currentTime + blackListPeriod;
//            if (scraperIds.containsKey(scraperId)) {
//                final long previousValue = scraperIds.get(scraperId);
//
//                logger.warn("{} {} already blacklisted: {} {} {}", classId, scraperId, blackListPeriod, timeBlackListExpires, previousValue);
//                if (previousValue < timeBlackListExpires) {
//                    scraperIds.put(scraperId, timeBlackListExpires);
//                    logger.warn("{} {} blacklisted again for {} ms", classId, scraperId, blackListPeriod);
//                } else { // value won't be changed
//                }
//            } else {
//                scraperIds.put(scraperId, timeBlackListExpires);
//                logger.warn("{} {} blacklisted for {} ms", classId, scraperId, blackListPeriod);
//            }
//        } // end if
//    }
//    public static void putBetradarId(Long scraperId, long blackListPeriod) {
//        final long currentTime = System.currentTimeMillis();
//        final long timeBlackListExpires = currentTime + blackListPeriod;
//        if (this.betradarIds.containsKey(scraperId)) {
//            final long previousValue = this.betradarIds.get(scraperId);
//
//            logger.warn("betradarId already blacklisted: {} {} {} {}", scraperId, blackListPeriod, timeBlackListExpires, previousValue);
//            if (previousValue < timeBlackListExpires) {
//                this.betradarIds.put(scraperId, timeBlackListExpires);
//                logger.warn("betradar {} blacklisted again for {} ms", scraperId, blackListPeriod);
//            } else { // value won't be changed
//            }
//        } else {
//            this.betradarIds.put(scraperId, timeBlackListExpires);
//            logger.warn("betradar {} blacklisted for {} ms", scraperId, blackListPeriod);
//        }
//    }
//
//    public static void putCoralId(Long scraperId, long blackListPeriod) {
//        final long currentTime = System.currentTimeMillis();
//        final long timeBlackListExpires = currentTime + blackListPeriod;
//        if (this.coralIds.containsKey(scraperId)) {
//            final long previousValue = this.coralIds.get(scraperId);
//
//            logger.warn("coralId already blacklisted: {} {} {} {}", scraperId, blackListPeriod, timeBlackListExpires, previousValue);
//            if (previousValue < timeBlackListExpires) {
//                this.coralIds.put(scraperId, timeBlackListExpires);
//                logger.warn("coral {} blacklisted again for {} ms", scraperId, blackListPeriod);
//            } else { // value won't be changed
//            }
//        } else {
//            this.coralIds.put(scraperId, timeBlackListExpires);
//            logger.warn("coral {} blacklisted for {} ms", scraperId, blackListPeriod);
//        }
//    }
//    public static void putEventId(String eventId, long safetyPeriod, long shortSafetyPeriod) {
//        if (safetyPeriod > 0L || shortSafetyPeriod > 0L) {
//            final long currentTime = System.currentTimeMillis();
//            final long timeSafetyPeriodExpires = currentTime + safetyPeriod;
//            final long timeShortSafetyPeriodExpires = currentTime + shortSafetyPeriod;
//            if (this.eventIds.containsKey(eventId)) {
//                final long previousValue = this.eventIds.get(eventId);
//                logger.error("eventId already blacklisted: {} {} {} {}", eventId, safetyPeriod, timeSafetyPeriodExpires, previousValue);
//
//                if (safetyPeriod > 0L) {
//                    if (previousValue < timeSafetyPeriodExpires) {
//                        this.eventIds.put(eventId, timeSafetyPeriodExpires);
//                        logger.error("event {} blacklisted again for {} ms", eventId, safetyPeriod);
//                    } else { // value won't be changed
//                    }
//                }
//
//                if (shortSafetyPeriod > 0L) {
//                    final long previousShortValue = this.eventIdsShort.get(eventId);
//                    if (previousShortValue < timeShortSafetyPeriodExpires) {
//                        this.eventIdsShort.put(eventId, timeShortSafetyPeriodExpires);
//                        logger.error("event {} blacklisted again shortSafety {} ms", eventId, shortSafetyPeriod);
//                    } else { // value won't be changed
//                    }
//                }
//            } else {
//                if (safetyPeriod > 0L) {
//                    this.eventIds.put(eventId, timeSafetyPeriodExpires);
//                }
//                if (shortSafetyPeriod > 0L) {
//                    this.eventIdsShort.put(eventId, timeShortSafetyPeriodExpires);
//                }
////            if (safetyPeriod > 0L || shortSafetyPeriod > 0L) {
//                logger.warn("event {} blacklisted for {} ms shortSafety {} ms", eventId, safetyPeriod, shortSafetyPeriod);
////            }
//            } // end else
//        } // end if
//    }
//
//    public static void putMarketId(String marketId, long safetyPeriod, long shortSafetyPeriod) {
//        if (safetyPeriod > 0L || shortSafetyPeriod > 0L) {
//            final long currentTime = System.currentTimeMillis();
//            final long timeSafetyPeriodExpires = currentTime + safetyPeriod;
//            final long timeShortSafetyPeriodExpires = currentTime + shortSafetyPeriod;
//            putMarketId(marketId, timeSafetyPeriodExpires, timeShortSafetyPeriodExpires, safetyPeriod, shortSafetyPeriod);
//        } // end if
//    }
//
//    public static void putMarketId(String marketId, long timeSafetyPeriodExpires, long timeShortSafetyPeriodExpires, long safetyPeriod, long shortSafetyPeriod) {
//        if (this.marketIds.containsKey(marketId)) {
//            final long previousValue = this.marketIds.get(marketId);
//            logger.error("marketId already blacklisted: {} {} {} {}", marketId, safetyPeriod, timeSafetyPeriodExpires, previousValue);
//
//            if (safetyPeriod > 0L) {
//                if (previousValue < timeSafetyPeriodExpires) {
//                    this.marketIds.put(marketId, timeSafetyPeriodExpires);
//                    logger.error("market {} blacklisted again for {} ms", marketId, safetyPeriod);
//                } else { // value won't be changed
//                }
//            }
//
//            if (shortSafetyPeriod > 0L) {
//                final long previousShortValue = this.marketIdsShort.get(marketId);
//                if (previousShortValue < timeShortSafetyPeriodExpires) {
//                    this.marketIdsShort.put(marketId, timeShortSafetyPeriodExpires);
//                    logger.error("market {} blacklisted again shortSafety {} ms", marketId, shortSafetyPeriod);
//                } else { // value won't be changed
//                }
//            }
//        } else {
//            if (safetyPeriod > 0L) {
//                this.marketIds.put(marketId, timeSafetyPeriodExpires);
//            }
//            if (shortSafetyPeriod > 0L) {
//                this.marketIdsShort.put(marketId, timeShortSafetyPeriodExpires);
//            }
////            if (safetyPeriod > 0L || shortSafetyPeriod > 0L) {
//            logger.warn("market {} blacklisted for {} ms shortSafety {} ms", marketId, safetyPeriod, shortSafetyPeriod);
////            }
//        } // end else
//    }
//
//    public static void putMarketIds(Set<String> marketIds, long safetyPeriod, long shortSafetyPeriod) {
//        if (safetyPeriod > 0L || shortSafetyPeriod > 0L) {
//            final long currentTime = System.currentTimeMillis();
//            final long timeSafetyPeriodExpires = currentTime + safetyPeriod;
//            final long timeShortSafetyPeriodExpires = currentTime + shortSafetyPeriod;
//            marketIds.stream().
//                    forEach((marketId) -> {
//                        putMarketId(marketId, timeSafetyPeriodExpires, timeShortSafetyPeriodExpires, safetyPeriod, shortSafetyPeriod);
////                    if (this.marketIds.containsKey(marketId)) {
////                        final long previousValue = this.marketIds.get(marketId);
////                        final long previousShortValue = this.marketIdsShort.get(marketId);
////
////                        logger.error("marketId already blacklisted: {} {} {} {}", marketId, safetyPeriod, timeSafetyPeriodExpires, previousValue);
////                        if (previousValue < timeSafetyPeriodExpires) {
////                            this.marketIds.put(marketId, timeSafetyPeriodExpires);
////                            logger.error("market {} blacklisted again for {} ms", marketId, safetyPeriod);
////                        } else { // value won't be changed
////                        }
////                        if (previousShortValue < timeShortSafetyPeriodExpires) {
////                            this.marketIdsShort.put(marketId, timeShortSafetyPeriodExpires);
////                            logger.error("market {} blacklisted again shortSafety {} ms", marketId, shortSafetyPeriod);
////                        } else { // value won't be changed
////                        }
////                    } else {
////                        this.marketIds.put(marketId, timeSafetyPeriodExpires);
////                        this.marketIdsShort.put(marketId, timeShortSafetyPeriodExpires);
////                        logger.warn("market {} blacklisted for {} ms shortSafety {} ms", marketId, safetyPeriod, shortSafetyPeriod);
////                    }
//                    }); // end for
//        } // end if
//    }
}
