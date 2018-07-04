package info.fmro.betty.objects;

import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Ignorable;
import info.fmro.shared.utility.SynchronizedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

public class BlackList //        implements Serializable 
{

    private static final Logger logger = LoggerFactory.getLogger(BlackList.class);
    public static final long defaultSafetyPeriod = 10_000L;

    private BlackList() {
    }

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
        final int modified;

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
                    MessageFormatter.arrayFormat("{} no value in map, timeSinceLastRemoved: {} for key: {}", new Object[]{format, timeSinceLastRemoved, key}).getMessage();
            if (timeSinceLastRemoved <= safetyPeriod) {
                logger.info(printedString);
            } else {
                logger.error(printedString);
            }
        } else {
            final long timeSinceBan = BlackList.timeSinceBan(synchronizedMap, key, currentTime);
            final String printedString = MessageFormatter.arrayFormat("{} ignored for key: {} {}", new Object[]{format, timeSinceBan, key}).getMessage();
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
                result = value.timeSinceSetIgnored(currentTime); // it does exist
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
        @SuppressWarnings("unchecked") final SynchronizedMap<T, ? extends Ignorable> synchronizedMap = (SynchronizedMap<T, ? extends Ignorable>) Formulas.getIgnorableMap(clazz);
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

//    public static int checkSafeRunners(Event event) {
//        // checks safeRunners of an Event, by running safeRunner.checkScrapers on them, which can remove or ignore the safeRunners if needed
//        int removedRunnerMatchedScrapers = 0;
//        if (event == null) {
//            logger.error("null event in checkSafeRunners");
//        } else {
//            final Set<Entry<String, MarketCatalogue>> entrySetCopy = Statics.marketCataloguesMap.entrySetCopy();
//            for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
//                final MarketCatalogue marketCatalogue = entry.getValue();
//                if (marketCatalogue != null) {
//                    final Event eventStump = marketCatalogue.getEventStump();
//                    if (event.equals(eventStump)) { // it doesn't matter if marketCatalogueEvent is null
//                        final String marketId = entry.getKey();
//                        final SynchronizedSet<SafeRunner> safeRunnersSet = Statics.safeMarketsMap.get(marketId);
//                        synchronized (safeRunnersSet) {
//                            final HashSet<SafeRunner> safeRunnersSetCopy = safeRunnersSet.copy();
//                            for (SafeRunner safeRunner : safeRunnersSetCopy) {
//                                removedRunnerMatchedScrapers += safeRunner.checkScrapers();
//                            }
//                        } // end synchronized
////                        if (removeMarket(marketId) != null) {
////                            nPurgedMarkets++;
////                        } else {
////                            logger.error("couldn't removeMarket in removeMarkets: {} {} {}", marketId, Generic.objectToString(marketCatalogue), Generic.objectToString(event));
////                        }
//                    } else { // not interesting marketCatalogue, nothing to be done
//                    }
//                } else {
//                    logger.error("null marketCatalogue value found in marketCataloguesMap during removeMarkets for: {}", entry.getKey());
//                    Statics.marketCataloguesMap.removeValueAll(null);
//                }
//            } // end for
//        }
//
//        return removedRunnerMatchedScrapers;
//    }

//    public static int ignoreMarkets(Set<String> marketIds, long safetyPeriod) {
//        int nModified = 0;
//        if (marketIds != null) {
//            if (!marketIds.isEmpty()) {
//                for (final String marketId : marketIds) {
//                    if (ignoreMarket(marketId, safetyPeriod) > 0) {
//                        nModified++;
//                    }
//                } // end for
//            } else { // nothing to be done, no error either
//            }
//        } else {
//            logger.error("null marketIds set in ignoreMarkets for period: {}", safetyPeriod);
//        }
//
//        return nModified;
//    }
//
//    public static int ignoreMarket(String marketId) {
//        return ignoreMarket(marketId, BlackList.defaultSafetyPeriod);
//    }
//
//    public static int ignoreMarket(String marketId, long safetyPeriod) {
//        final long currentTime = System.currentTimeMillis();
//        return ignoreMarket(marketId, safetyPeriod, currentTime);
//    }
//
//    public static int ignoreMarket(String marketId, long safetyPeriod, long currentTime) {
//        int modified = BlackList.setIgnored(Statics.marketCataloguesMap, marketId, safetyPeriod, currentTime);
////        Statics.marketCataloguesMap.remove(marketId);
//
//        MaintenanceThread.removeFromSecondaryMaps(marketId);
//
//        return modified;
//    }
}
