package info.fmro.betty.main;

import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.MarketBook;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.enums.ScrapedField;
import info.fmro.betty.objects.BlackList;
import info.fmro.betty.objects.SafeBet;
import info.fmro.betty.objects.SafeBetStats;
import info.fmro.betty.objects.SafeRunner;
import info.fmro.betty.objects.ScraperEvent;
import info.fmro.betty.objects.StampedDouble;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.objects.TwoOrderedStrings;
import info.fmro.betty.utility.DebuggingMethods;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import info.fmro.shared.utility.SynchronizedMap;
import info.fmro.shared.utility.SynchronizedSafeSet;
import info.fmro.shared.utility.SynchronizedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

public class MaintenanceThread
        extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(MaintenanceThread.class);

    // public static long timedCheckDiskSpace() {
    //     long timeForNext = Statics.debugger.getTimeLastCheckDiskSpace() + Generic.MINUTE_LENGTH_MILLISECONDS;
    //     long timeTillNext = timeForNext - System.currentTimeMillis();
    //     if (timeTillNext <= 0) {
    //         Statics.debugger.checkDiskSpace();
    //         timeForNext = Statics.debugger.getTimeLastCheckDiskSpace() + Generic.MINUTE_LENGTH_MILLISECONDS;
    //         timeTillNext = timeForNext - System.currentTimeMillis();
    //     } else { // nothing to be done
    //     }
    //     return timeTillNext;
    // }

    public static long timedPrintAverages() {
        long timeForNext = Statics.timeStamps.getLastPrintAverages();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastPrintAveragesStamp(Statics.DELAY_PRINTAVERAGES);
            if (Statics.safeBetModuleActivated) {
                Statics.betradarScraperThread.averageLogger.printRecords();
                Statics.betradarScraperThread.averageLoggerFull.printRecords();
                Statics.coralScraperThread.averageLogger.printRecords();
                // Statics.coralScraperThread.averageLoggerFull.printRecords(); // this will have a check on Statics.coralScraperThread.singleLogger
            }
            Betty.quickCheckThread.averageLogger.printRecords();

            int size = Statics.linkedBlockingQueue.size();
            if (size >= 10) {
                logger.error("elements in linkedBlockingQueue: {}", size);
            } else if (size > 0) {
                logger.warn("elements in linkedBlockingQueue: {}", size);
            }
            size = Statics.linkedBlockingQueueMarketBooks.size();
            if (size >= 10) {
                logger.error("elements in linkedBlockingQueueMarketBooks: {}", size);
            } else if (size > 0) {
                logger.warn("elements in linkedBlockingQueueMarketBooks: {}", size);
            }
            size = Statics.linkedBlockingQueueImportant.size();
            if (size >= 10) {
                logger.error("elements in linkedBlockingQueueImportant: {}", size);
            } else if (size > 0) {
                logger.warn("elements in linkedBlockingQueueImportant: {}", size);
            }

            timeForNext = Statics.timeStamps.getLastPrintAverages();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedReadAliases() {
        long timeForNext = Statics.timeStamps.getLastCheckAliases();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastCheckAliasesStamp(Generic.MINUTE_LENGTH_MILLISECONDS);

            VarsIO.checkAliasesFile(Statics.ALIASES_FILE_NAME, Statics.aliasesTimeStamp, Formulas.aliasesMap);
            VarsIO.checkAliasesFile(Statics.FULL_ALIASES_FILE_NAME, Statics.fullAliasesTimeStamp, Formulas.fullAliasesMap);

            timeForNext = Statics.timeStamps.getLastCheckAliases();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedPrintDebug() {
        long timeForNext = Statics.timeStamps.getLastPrintDebug();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastPrintDebugStamp(Generic.MINUTE_LENGTH_MILLISECONDS * 2L);
            logger.info("maxMemory: {} totalMemory: {} freeMemory: {}", Generic.addCommas(Runtime.getRuntime().maxMemory()), Generic.addCommas(Runtime.getRuntime().totalMemory()), Generic.addCommas(Runtime.getRuntime().freeMemory()));
            logger.info("threadPools active/mostEver marketBooks: {}/{} important: {}/{} general: {}/{}", Statics.threadPoolExecutorMarketBooks.getActiveCount(), Statics.threadPoolExecutorMarketBooks.getLargestPoolSize(),
                        Statics.threadPoolExecutorImportant.getActiveCount(), Statics.threadPoolExecutorImportant.getLargestPoolSize(), Statics.threadPoolExecutor.getActiveCount(), Statics.threadPoolExecutor.getLargestPoolSize());
            if (Statics.safeBetModuleActivated) {
                logger.info("scraperEventsMap: b:{} c:{} eventsMap: {} marketCataloguesMap: {} safeMarketsMap: {} safeMarketBooksMap: {} safeMarketsImportantMap: {}", Statics.betradarEventsMap.size(), Statics.coralEventsMap.size(),
                            Statics.eventsMap.size(), Statics.marketCataloguesMap.size(), Statics.safeMarketsMap.size(), Statics.safeMarketBooksMap.size(), Statics.safeMarketsImportantMap.size());
            } else {
                logger.info("eventsMap: {} marketCataloguesMap: {} safeMarketsMap: {} safeMarketBooksMap: {} safeMarketsImportantMap: {}", Statics.eventsMap.size(), Statics.marketCataloguesMap.size(), Statics.safeMarketsMap.size(),
                            Statics.safeMarketBooksMap.size(), Statics.safeMarketsImportantMap.size());
            }
            logger.info("connManager stats: {}", Statics.connManager.getTotalStats().toString());

            timeForNext = Statics.timeStamps.getLastPrintDebug();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedSaveObjects() {
        long timeForNext = Statics.timeStamps.getLastObjectsSave();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            VarsIO.writeObjectsToFiles();
            timeForNext = Statics.timeStamps.getLastObjectsSave();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedCleanScraperEventsMap() {
        long timeForNext = Statics.timeStamps.getLastCleanScraperEventsMap();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastCleanScraperEventsMapStamp(Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
            for (Class<? extends ScraperEvent> classFromSet : Statics.scraperEventSubclassesSet) {
                cleanScraperEventsMap(classFromSet);
            }
            timeForNext = Statics.timeStamps.getLastCleanScraperEventsMap();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedCleanEventsMap() {
        long timeForNext = Statics.eventsMap.getTimeClean();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            cleanEventsMap();
            timeForNext = Statics.eventsMap.getTimeClean();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedCleanMarketCataloguesMap() {
        long timeForNext = Statics.marketCataloguesMap.getTimeClean();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            cleanMarketCataloguesMap();
            timeForNext = Statics.marketCataloguesMap.getTimeClean();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedCleanSecondaryMaps() {
        long timeForNext = Statics.timeStamps.getLastCleanSecondaryMaps();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            cleanSecondaryMaps();
            timeForNext = Statics.timeStamps.getLastCleanSecondaryMaps();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedCleanSafeMarketsImportantMap() {
        long timeForNext = Statics.safeMarketsImportantMap.getTimeClean();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            cleanSafeMarketsImportantMap();
            timeForNext = Statics.safeMarketsImportantMap.getTimeClean();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedCleanTimedMaps() {
        long timeForNext = Statics.timeStamps.getLastCleanTimedMaps();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            cleanTimedMaps();
            timeForNext = Statics.timeStamps.getLastCleanTimedMaps();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedCleanSafeBetsMap() {
        long timeForNext = Statics.safeBetsMap.getTimeClean();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            cleanSafeBetsMap();
            timeForNext = Statics.safeBetsMap.getTimeClean();

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static void cleanSafeBetsMap() {
        Statics.safeBetsMap.timeCleanStamp(20_000L);

        final long startTime = System.currentTimeMillis();

        final int initialSize = Statics.safeBetsMap.size();
        if (initialSize > 0) {
            final Set<Entry<String, SynchronizedMap<SafeBet, SafeBetStats>>> entrySetCopy = Statics.safeBetsMap.entrySetCopy();
            for (Entry<String, SynchronizedMap<SafeBet, SafeBetStats>> entry : entrySetCopy) {
                final SynchronizedMap<SafeBet, SafeBetStats> value = entry.getValue();
                final String marketId = entry.getKey();
                if (value == null) {
                    logger.error("null value during cleanSafeBetsMap for: {}", marketId);
                    Statics.safeBetsMap.removeValueAll(null);
                } else {
                    if (BlackList.notExistOrIgnored(MarketCatalogue.class, marketId)) {
                        BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "marketCatalogue, during cleanSafeBetsMap");
                        removeFromSecondaryMaps(marketId);
                    } else {
                        final Set<Entry<SafeBet, SafeBetStats>> entrySetInnerCopy = value.entrySetCopy();
                        for (Entry<SafeBet, SafeBetStats> entryInner : entrySetInnerCopy) {
                            final SafeBetStats safeBetStats = entryInner.getValue();

                            final long timeLastAppear = safeBetStats.getTimeLastAppear();
                            if (startTime - timeLastAppear > Generic.MINUTE_LENGTH_MILLISECONDS) {
                                final SafeBet safeBet = entryInner.getKey();

                                final String printedString =
                                        MessageFormatter.arrayFormat("noLongerAppearsFor {} ms: {} {} {}",
                                                                     new Object[]{startTime - timeLastAppear, safeBetStats.printStats(), Generic.objectToString(safeBet), Generic.objectToString(safeBetStats)}).getMessage();
                                logger.warn(printedString);
                                Statics.safebetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");

//                            iteratorInner.remove();
                                Statics.safeBetsMap.removeSafeBet(marketId, safeBet);
                            } else { // nothing to be done, entry is not yet obsolete
                            }
                        } // end while
                        if (value.isEmpty()) {
                            Statics.safeBetsMap.remove(marketId, value);
                        }
                    }
                } // end else
            } // end for

            final int newSize = Statics.safeBetsMap.size();
            if (newSize != initialSize) {
                logger.info("cleaned safeBetsMap, initialSize: {} newSize: {} in {} ms", initialSize, newSize, System.currentTimeMillis() - startTime);
            }
        } else { // map empty, nothing to be done
        }
    }

    public static void finalCleanSafeBetsMap() {
        try {
            Statics.safeBetsMap.timeCleanStamp(20_000L);

            final int initialSize = Statics.safeBetsMap.size();
            if (initialSize > 0) {
                final Set<Entry<String, SynchronizedMap<SafeBet, SafeBetStats>>> entrySetCopy = Statics.safeBetsMap.entrySetCopy();
                for (Entry<String, SynchronizedMap<SafeBet, SafeBetStats>> entry : entrySetCopy) {
                    final SynchronizedMap<SafeBet, SafeBetStats> value = entry.getValue();
                    final String key = entry.getKey();
                    if (value == null) {
                        logger.error("null value during cleanSafeBetsMap for: {}", key);
                        Statics.safeBetsMap.removeValueAll(null);
                    } else {
                        final Set<Entry<SafeBet, SafeBetStats>> entrySetInnerCopy = value.entrySetCopy();
                        for (Entry<SafeBet, SafeBetStats> entryInner : entrySetInnerCopy) {
                            final SafeBetStats valueInner = entryInner.getValue();

                            // long timeLastAppear = valueInner.getTimeLastAppear();
                            // if (currentTime - timeLastAppear > Generic.MINUTE_LENGTH_MILLISECONDS) {
                            final SafeBet keyInner = entryInner.getKey();

                            final String printedString = MessageFormatter.arrayFormat("final {} {} {} {}", new Object[]{valueInner.printStats(), keyInner.printStats(),
                                                                                                                        Generic.objectToString(keyInner), Generic.objectToString(valueInner)}).getMessage();
                            logger.info(printedString);
                            Statics.safebetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");

//                            iteratorInner.remove();
                            Statics.safeBetsMap.removeSafeBet(key, keyInner);
                            // } else { // nothing to be done, entry is not yet obsolete
                            // }
                        } // end while
                        if (value.isEmpty()) {
                            Statics.safeBetsMap.remove(key, value);
                        }
                    } // end else
                } // end for

                final int newSize = Statics.safeBetsMap.size();
                if (newSize != initialSize) {
                    logger.info("final cleaned safeBetsMap, initialSize: {} newSize: {}", initialSize, newSize);
                }
            } else { // map empty, nothing to be done
            }
        } catch (Throwable throwable) {
            logger.error("throwable in finalCleanSafeBetsMap", throwable);
        }
    }

    public static void cleanTimedMaps() {
        Statics.timeStamps.lastCleanTimedMapsStamp(Generic.MINUTE_LENGTH_MILLISECONDS * 30L);

        final long startTimeTimedWarnings = System.currentTimeMillis();
//        synchronized (Statics.timedWarningsMap) {
        int initialSize = Statics.timedWarningsMap.size();
        Collection<Long> valuesCopy = Statics.timedWarningsMap.valuesCopy();
        for (Long value : valuesCopy) {
            if (value == null) {
                logger.error("null value during clean timedWarningsMap");
                Statics.timedWarningsMap.removeValueAll(null); // remove null
            } else {
                final long primitive = value;
                if (startTimeTimedWarnings - primitive > Generic.HOUR_LENGTH_MILLISECONDS * 2L) {
                    Statics.timedWarningsMap.removeValue(value);
                } else { // nothing to be done, entry is not yet obsolete
                }
            }
        } // end while

        int newSize = Statics.timedWarningsMap.size();
        if (newSize != initialSize) {
            logger.info("cleaned timedWarningsMap, initialSize: {} newSize: {} in {} ms", initialSize, newSize, System.currentTimeMillis() - startTimeTimedWarnings);
        }
//        } // end synchronized

        Generic.alreadyPrintedMap.clean();

//        final long startTimeAlreadyPrinted = System.currentTimeMillis();
////        synchronized (Statics.alreadyPrintedMap) {
//        initialSize = Generic.alreadyPrintedMap.size();
//        valuesCopy = Generic.alreadyPrintedMap.valuesCopy();
//        for (Long value : valuesCopy) {
//            if (value == null) {
//                logger.error("null value during clean alreadyPrintedMap");
//                Generic.alreadyPrintedMap.removeValueAll(null); // remove null
//            } else {
//                final long primitive = value;
//                if (startTimeAlreadyPrinted >= primitive) {
//                    Generic.alreadyPrintedMap.removeValue(value);
//                } else { // nothing to be done, entry is not yet obsolete
//                }
//            }
//        } // end for
//
//        newSize = Generic.alreadyPrintedMap.size();
//        if (newSize != initialSize) {
//            logger.info("cleaned alreadyPrintedMap, initialSize: {} newSize: {} in {} ms", initialSize, newSize, System.currentTimeMillis() - startTimeAlreadyPrinted);
//        }
        //      } // end synchronized

        final long startTimeMatchesCache = System.currentTimeMillis();
        initialSize = Formulas.matchesCache.size();
        final Set<Entry<TwoOrderedStrings, StampedDouble>> entriesCopy = Formulas.matchesCache.entrySetCopy();
        for (Entry<TwoOrderedStrings, StampedDouble> entry : entriesCopy) {
            final StampedDouble stampedDouble = entry.getValue();
            if (stampedDouble == null) {
                logger.error("null value during clean matchesCache");
                Formulas.matchesCache.removeValueAll(null); // remove null
            } else {
                final long lastUse = stampedDouble.getStamp();
                if (startTimeMatchesCache - lastUse > Generic.HOUR_LENGTH_MILLISECONDS) {
                    final TwoOrderedStrings key = entry.getKey();
                    Formulas.matchesCache.remove(key); // I can't remove the value, as there are many similar ones
                } else { // nothing to be done, entry is not yet obsolete
                }
            }
        } // end for

        newSize = Formulas.matchesCache.size();
        if (newSize != initialSize) {
            logger.info("cleaned matchesCache, initialSize: {} newSize: {} in {} ms", initialSize, newSize, System.currentTimeMillis() - startTimeMatchesCache);
        }
    }

    public static void cleanSafeMarketsImportantMap() {
        Statics.safeMarketsImportantMap.timeCleanStamp(Generic.MINUTE_LENGTH_MILLISECONDS);

        final long startTime = System.currentTimeMillis();

        final int initialSize = Statics.safeMarketsImportantMap.size();
        final Collection<Long> valuesCopy = Statics.safeMarketsImportantMap.valuesCopy();
        for (Long value : valuesCopy) {
            if (value == null) {
                logger.error("null value during cleanSafeMarketsImportantMap");
                Statics.safeMarketsImportantMap.removeValueAll(null);
            } else {
                final long primitive = value;
                if (startTime - primitive > Generic.MINUTE_LENGTH_MILLISECONDS) {
                    Statics.safeMarketsImportantMap.removeValue(value);
                } else { // nothing to be done, this market is still important
                }
            }
        } // end for

        final Set<String> marketIds = Statics.safeMarketsImportantMap.keySetCopy();
        for (String marketId : marketIds) {
            if (BlackList.notExistOrIgnored(MarketCatalogue.class, marketId)) {
                BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "marketCatalogue, during cleanSafeMarketsImportantMap");
                removeFromSecondaryMaps(marketId);
            }
        } // end for

        final int newSize = Statics.safeMarketsImportantMap.size();
        if (newSize != initialSize) {
            logger.info("cleaned safeMarketsImportantMap, initialSize: {} newSize: {} in {} ms", initialSize, newSize, System.currentTimeMillis() - startTime);
        }
    }

    public static void cleanSecondaryMaps() {
        Statics.timeStamps.lastCleanSecondaryMapsStamp(Generic.MINUTE_LENGTH_MILLISECONDS * 10L);

        final long startTime = System.currentTimeMillis();
        final int initialSizeSafeMarketsMap = Statics.safeMarketsMap.size();
        final int initialSizeSafeMarketBooksMap = Statics.safeMarketBooksMap.size();
        synchronized (Statics.marketCataloguesMap) {
            final Set<String> marketIdsSetCopy = Statics.marketCataloguesMap.keySetCopy();
            Statics.safeMarketsMap.retainAllKeys(marketIdsSetCopy);
            Statics.safeMarketBooksMap.retainAllKeys(marketIdsSetCopy);
        } // end synchronized

        if (Statics.safeMarketsMap.containsKey(null)) {
            final SynchronizedSafeSet<SafeRunner> removedSet = Statics.safeMarketsMap.remove(null);
            logger.error("null key found in safeMarketsMap for: {}", Generic.objectToString(removedSet));
        }
        final Set<Entry<String, SynchronizedSafeSet<SafeRunner>>> entrySetCopy = Statics.safeMarketsMap.entrySetCopy();
        for (Entry<String, SynchronizedSafeSet<SafeRunner>> entry : entrySetCopy) {
            final String marketId = entry.getKey();
            final SynchronizedSafeSet<SafeRunner> runnersSet = entry.getValue();
            if (runnersSet == null) {
                logger.error("null runnersSet found in safeMarketsMap during cleaning for: {}", marketId);
//                Statics.safeMarketsMap.removeValueAll(null);
//                Statics.safeMarketsMap.remove(marketId);
                removeSafeMarket(marketId);
                Statics.safeMarketBooksMap.remove(marketId);
            } else {
                final HashSet<SafeRunner> setCopy = runnersSet.copy();
                for (SafeRunner safeRunner : setCopy) {
                    if (safeRunner == null) {
                        logger.error("null safeRunner in usedScrapersMap during cleanup: {}", marketId);
                        runnersSet.remove(null);
                    } else {
                        final HashMap<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedScrapers = safeRunner.getUsedScrapers();
                        if (usedScrapers == null) {
                            logger.error("null usedScrapers in usedScrapersMap during cleanup: {} {}", marketId);
                            runnersSet.remove(safeRunner);
                        } else {
                            for (Entry<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedEntry : usedScrapers.entrySet()) {
                                final ScrapedField scrapedField = usedEntry.getKey();
                                final SynchronizedMap<Class<? extends ScraperEvent>, Long> map = usedEntry.getValue();
                                if (scrapedField == null || map == null) {
                                    logger.error("null scrapedField or map in usedScrapersMap during cleanup: {} {}", marketId, Generic.objectToString(usedScrapers));
                                    runnersSet.remove(safeRunner);
                                } else {
                                    final Set<Entry<Class<? extends ScraperEvent>, Long>> entrySet = map.entrySetCopy();
                                    for (Entry<Class<? extends ScraperEvent>, Long> innerEntry : entrySet) {
                                        final Class<? extends ScraperEvent> clazz = innerEntry.getKey();
                                        final Long scraperId = innerEntry.getValue();
                                        if (clazz == null || scraperId == null) {
                                            logger.error("null clazz or scraperId in usedScrapersMap during cleanup: {} {}", marketId, Generic.objectToString(map));
                                            map.remove(clazz);
                                        } else {
                                            final SynchronizedMap<Long, ? extends ScraperEvent> staticsMap = Formulas.getScraperEventsMap(clazz);
                                            if (!staticsMap.containsKey(scraperId)) {
                                                map.remove(clazz);
                                            }
                                        }
                                    } // end for
                                    if (map.size() < Statics.MIN_MATCHED) {
                                        runnersSet.remove(safeRunner);
                                    }
                                } // end else
                            } // end for
                        } // end else
                    } // end else
                } // end for
                if (runnersSet.isEmpty()) {
//                    Statics.safeMarketsMap.remove(marketId);
                    removeSafeMarket(marketId);
                    Statics.safeMarketBooksMap.remove(marketId);
                }
            } // end else
        } // end for

        final Set<String> marketIds = Statics.safeMarketsMap.keySetCopy();
        marketIds.addAll(Statics.safeMarketBooksMap.keySetCopy());
        for (String marketId : marketIds) {
            if (BlackList.notExistOrIgnored(MarketCatalogue.class, marketId)) {
                BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "marketCatalogue, during cleanSecondaryMaps");
                removeFromSecondaryMaps(marketId);
            }
        } // end for

        int newSize = Statics.safeMarketsMap.size();
        if (newSize != initialSizeSafeMarketsMap) {
            logger.info("secondary cleaned safeMarketsMap, initialSize: {} newSize: {} in {} ms", initialSizeSafeMarketsMap, newSize, System.currentTimeMillis() - startTime);
        }

        if (Statics.safeMarketBooksMap.containsKey(null)) {
            final MarketBook removedMarketBook = Statics.safeMarketBooksMap.remove(null);
            logger.error("null key found in safeMarketBooksMap for: {}", Generic.objectToString(removedMarketBook));
        }
        newSize = Statics.safeMarketBooksMap.size();
        if (newSize != initialSizeSafeMarketBooksMap) {
            logger.info("secondary cleaned safeMarketBooksMap, initialSize: {} newSize: {} in {} ms", initialSizeSafeMarketBooksMap, newSize,
                        System.currentTimeMillis() - startTime);
        }
    }

    public static void checkScraperEvents(Event event) {
        if (event != null) {
            final LinkedHashMap<Class<? extends ScraperEvent>, Long> scraperEventIds = event.getScraperEventIds();
            if (scraperEventIds != null && !scraperEventIds.isEmpty()) {
                final Iterator<Entry<Class<? extends ScraperEvent>, Long>> iterator = scraperEventIds.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Entry<Class<? extends ScraperEvent>, Long> entry = iterator.next();
                    final Class<? extends ScraperEvent> clazz = entry.getKey();
                    final long scraperId = entry.getValue();
                    final SynchronizedMap<Long, ? extends ScraperEvent> scraperMap = Formulas.getScraperEventsMap(clazz);
                    if (!scraperMap.containsKey(scraperId)) {
                        logger.error("scraperId {} not found in {} scraperMap in checkScraperEvents for: {}", scraperId, clazz.getSimpleName(), Generic.objectToString(event));
                        // won't remove, only print error message; removal might cause problems due to racing condition
//                    iterator.remove();
//                    event.removeScraperEventId(clazz);
                    }
                } // end while        
            } else { // usually normal behaviour
            }
        } else { // else does nothing, null event is supported
        }
    }

    public static void cleanMarketCataloguesMap() {
        Statics.marketCataloguesMap.timeCleanStamp(Generic.MINUTE_LENGTH_MILLISECONDS * 15L);
        final long startTime = System.currentTimeMillis();

        final long marketCataloguesMapLastUpdate = Statics.marketCataloguesMap.getTimeStamp();
        final long eventsMapLastUpdate = Statics.eventsMap.getTimeStamp();
//        final HashSet<String> removedMarketIds = new HashSet<>(0);
        int nRemoved = 0;

        final int initialSize = Statics.marketCataloguesMap.size();

        if (Statics.marketCataloguesMap.containsKey(null)) {
            final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.remove(null);
            nRemoved++;
            logger.error("null key found in marketCataloguesMap for: {}", Generic.objectToString(marketCatalogue));
        }
        if (Statics.marketCataloguesMap.containsValue(null)) {
            logger.error("null value found in marketCataloguesMap");
            final boolean modified = Statics.marketCataloguesMap.removeValueAll(null);
            if (modified) {
                nRemoved++;
            } else {
                logger.error("null value not removed from marketCataloguesMap in cleanMarketCataloguesMap");
            }
        }

        final Set<Entry<String, MarketCatalogue>> entrySetCopy = Statics.marketCataloguesMap.entrySetCopy();
        for (Entry<String, MarketCatalogue> entry : entrySetCopy) {
            final String marketId = entry.getKey();
            final MarketCatalogue marketCatalogue = entry.getValue();

            if (marketCatalogue != null) {
                final long timeStamp = marketCatalogue.getTimeStamp();
                if (Math.min(eventsMapLastUpdate, marketCataloguesMapLastUpdate) - timeStamp > Generic.MINUTE_LENGTH_MILLISECONDS * 5L) {
                    final MarketCatalogue removedMarketCatalogue = removeMarket(marketId);
//                    Statics.marketCataloguesMap.remove(marketId);
//                    removedMarketIds.add(marketId);
                    if (removedMarketCatalogue != null) {
                        nRemoved++;
                    } else {
                        logger.error("marketCatalogue not removed from marketCataloguesMap in cleanMarketCataloguesMap for: {} {}", marketId, Generic.objectToString(marketCatalogue));
                    }
                } else {
                    final Event eventStump = marketCatalogue.getEventStump();
                    if (eventStump != null) {
                        final Event event = Formulas.getStoredEventOfMarketCatalogue(marketCatalogue);
                        if (event != null) {
                            if (Statics.safeBetModuleActivated) {
                                checkScraperEvents(event);
                                final int nScraperEventIds = event.getNValidScraperEventIds();
                                if (nScraperEventIds < Statics.MIN_MATCHED) {
                                    if (marketCatalogue.isIgnored()) { // normal behavior
                                    } else if (marketCatalogue.isResetIgnoredRecent()) {
                                        logger.warn("nScraperEventIds {} too small in cleanMarketCataloguesMap for: {} {}", nScraperEventIds, Generic.objectToString(event), Generic.objectToString(marketCatalogue));
                                    } else {
                                        logger.error("nScraperEventIds {} too small in cleanMarketCataloguesMap for: {} {}", nScraperEventIds, Generic.objectToString(event), Generic.objectToString(marketCatalogue));
                                    }
                                    // won't remove, only print error message
//                            Statics.marketCataloguesMap.remove(marketId);
//                            removedMarketIds.add(marketId);
                                } else { // nothing wrong, won't remove
                                }
                            }

                            if (marketCatalogue.isIgnored() || event.isIgnored()) { // check that marketIgnore is at least as long as eventIgnore
                                final long marketIgnoredExpiration = marketCatalogue.getIgnoredExpiration();
                                final long eventIgnoredExpiration = event.getIgnoredExpiration();

                                if (marketIgnoredExpiration < eventIgnoredExpiration) {
                                    logger.error("marketIgnoredExpiry {} smaller than eventIgnoredExpiry {} by {} in cleanMarketCataloguesMap for: {} {}",
                                                 marketIgnoredExpiration, eventIgnoredExpiration, eventIgnoredExpiration - marketIgnoredExpiration, Generic.objectToString(
                                                    marketCatalogue));
//                                marketCatalogue.setIgnoredExpiration(eventIgnoredExpiration);
                                    marketCatalogue.setIgnored(0L, eventIgnoredExpiration);
                                }
                            }
                        } else {
                            logger.error("no event found in map during cleanMarketCataloguesMap for: {}", Generic.objectToString(marketCatalogue));
                            final MarketCatalogue removedMarketCatalogue = removeMarket(marketId);
//                        Statics.marketCataloguesMap.remove(marketId);
//                        removedMarketIds.add(marketId); 
                            if (removedMarketCatalogue != null) {
                                nRemoved++;
                            } else {
                                logger.error("marketCatalogue without event in map not removed from marketCataloguesMap in cleanMarketCataloguesMap for: {}",
                                             Generic.objectToString(marketCatalogue));
                            }
                        }
                    } else {
                        logger.error("event null found in marketCatalogue during cleanup for: {}", Generic.objectToString(marketCatalogue));
                        final MarketCatalogue removedMarketCatalogue = removeMarket(marketId);
//                        Statics.marketCataloguesMap.remove(marketId);
//                        removedMarketIds.add(marketId); 
                        if (removedMarketCatalogue != null) {
                            nRemoved++;
                        } else {
                            logger.error("marketCatalogue with null event not removed from marketCataloguesMap in cleanMarketCataloguesMap for: {}",
                                         Generic.objectToString(marketCatalogue));
                        }
                    }
                } // end else
            } else { // marketCatalogue == null
                logger.error("marketCatalogue null found in marketCataloguesMap during cleanup for: {}", marketId);
//                final MarketCatalogue removedMarketCatalogue = BlackList.removeMarket(marketId);
//                Statics.marketCataloguesMap.remove(marketId);
//                removedMarketIds.add(marketId);
                final boolean modified = Statics.marketCataloguesMap.removeValueAll(null);
                if (modified) {
                    nRemoved++;
                } else {
                    logger.error("marketCatalogue null value not removed from marketCataloguesMap in cleanMarketCataloguesMap");
                }
            }
        } // end for
        if (nRemoved > 0) {
            final int newSize = Statics.marketCataloguesMap.size();
            logger.info("cleaned marketCataloguesMap, nRemoved: {} initialSize: {} newSize: {} in {} ms", nRemoved, initialSize, newSize, System.currentTimeMillis() - startTime);
        } else { // nothing removed, I guess nothing needs to be printed
        }
    }

    //    public static void removeScraperEvents(LinkedHashMap<Class<? extends ScraperEvent>, Long> map) {
//        if (map != null && !map.isEmpty()) {
//            for (Entry<Class<? extends ScraperEvent>, Long> entry : map.entrySet()) {
//                final Class<? extends ScraperEvent> clazz = entry.getKey();
//                final long scraperId = entry.getValue();
//                final SynchronizedMap<Long, ? extends ScraperEvent> scraperMap = Formulas.getScraperEventsMap(clazz);
//                scraperMap.remove(scraperId);
//            } // end for
//        } // end if
//    }safeRunners.isEmpty
    public static void cleanEventsMap() {
        Statics.eventsMap.timeCleanStamp(Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
        final long startTime = System.currentTimeMillis();

        final long eventsMapLastUpdate = Statics.eventsMap.getTimeStamp();
//        final HashSet<String> removedEventIds = new HashSet<>(0);
        int nEventsRemoved = 0;
        int nScraperEventsRemoved = 0;
        final int initialBetradarSize = Statics.betradarEventsMap.size();
        final int initialCoralSize = Statics.coralEventsMap.size();

//        synchronized (Statics.eventsMap) {
        final int initialSize = Statics.eventsMap.size();

        if (Statics.eventsMap.containsKey(null)) {
            final Event event = Statics.eventsMap.remove(null);
            nEventsRemoved++;
            logger.error("null key found in eventsMap for: {}", Generic.objectToString(event));
        }
        if (Statics.eventsMap.containsValue(null)) {
            logger.error("null value found in eventsMap");
            final boolean modified = Statics.eventsMap.removeValueAll(null);
            if (modified) {
                nEventsRemoved++;
            } else {
                logger.error("null value not removed from eventsMap in cleanEventsMap");
            }
        }

        final Set<Entry<String, Event>> entrySetCopy = Statics.eventsMap.entrySetCopy();
        for (Entry<String, Event> entry : entrySetCopy) {
            final Event event = entry.getValue();
            final long timeStamp = event.getTimeStamp();
            // Date openDate = value.getOpenDate();
            // long openTime = openDate.getTime();
            final String eventId = entry.getKey();

            if (eventsMapLastUpdate - timeStamp > Generic.MINUTE_LENGTH_MILLISECONDS * 5L // && eventsMapLastUpdate - openTime > Generic.HOUR_LENGTH_MILLISECONDS * 12L
                    ) {
                final Event removedEvent = removeEvent(eventId);
//                Statics.eventsMap.remove(key);
//                removedEventIds.add(key);
                if (removedEvent != null) {
                    nEventsRemoved++;
                } else {
                    logger.error("event not removed from eventsMap in cleanEventsMap for: {} {}", eventId, Generic.objectToString(event));
                }

                final LinkedHashMap<Class<? extends ScraperEvent>, Long> scraperEventIds = event.getScraperEventIds();
                nScraperEventsRemoved += removeScrapers(scraperEventIds);
//                removeScraperEvents(scraperEventIds);
            } else if (Statics.safeBetModuleActivated) { // not expired yet, will check to see if attached scraperEvents are still in their map
                final LinkedHashMap<Class<? extends ScraperEvent>, Long> scraperEventIds = event.getScraperEventIds();
                if (scraperEventIds != null) {
                    if (!scraperEventIds.isEmpty()) {
                        final Set<Entry<Class<? extends ScraperEvent>, Long>> scraperEventIdsEntries = scraperEventIds.entrySet();
                        for (Entry<Class<? extends ScraperEvent>, Long> scraperEntry : scraperEventIdsEntries) {
                            final Class<? extends ScraperEvent> clazz = scraperEntry.getKey();
                            final Long scraperId = scraperEntry.getValue();
                            final SynchronizedMap<Long, ? extends ScraperEvent> scraperMap = Formulas.getScraperEventsMap(clazz);
                            final ScraperEvent scraperEvent = scraperMap.get(scraperId);

                            if (scraperEvent == null) {
                                logger.error("non existent matchedScraper {} {} found in cleanEventsMap for: {} {}", clazz.getSimpleName(), scraperId, eventId, Generic.objectToString(event));
                                event.setIgnored(Generic.DAY_LENGTH_MILLISECONDS); // error is no joke
//                                event.removeScraperEventId(clazz);
                            } else { // everything is good, nothing to be done, ignored scrapers are OK
                            }
                        }
                    } else { // no attached scraperEventsIds, nothing to be done                        
                    }
                } else {
                    logger.error("null scraperEventIds in cleanEventsMap for: {} {}", eventId, Generic.objectToString(event));
                }
            } else { // safeBetModule not activated and event not expired, nothing to be done
            }
        } // end for
        //        } // end synchronized block

        if (nEventsRemoved > 0) {
            final int newSize = Statics.eventsMap.size();
            logger.info("cleaned eventsMap, nEventsRemoved: {} initialSize: {} newSize: {} in {} ms", nEventsRemoved, initialSize, newSize, System.currentTimeMillis() - startTime);
        } else { // nothing removed, I guess nothing needs to be printed
        }
        if (nScraperEventsRemoved > 0) {
            final int newCoralSize = Statics.coralEventsMap.size();
            final int newBetradarSize = Statics.betradarEventsMap.size();
            logger.info("cleaned eventsMap, nScraperEventsRemoved: {} initialCoralSize: {} newCoralSize: {} initialBetradarSize: {} newBetradarSize: {} in {} ms",
                        nScraperEventsRemoved, initialCoralSize, newCoralSize, initialBetradarSize, newBetradarSize, System.currentTimeMillis() - startTime);
        } else { // nothing removed, I guess nothing needs to be printed
        }
    }

    public static void cleanScraperEventsMap(Class<? extends ScraperEvent> clazz) {
        final SynchronizedMap<Long, ? extends ScraperEvent> map = Formulas.getScraperEventsMap(clazz);
        final long startTime = System.currentTimeMillis();

        final long scraperEventsMapLastUpdate = map.getTimeStamp();
        // HashSet<Long> removedScraperEventIds = new HashSet<>(0);
//        final HashSet<String> removedEventIds = new HashSet<>(0);
        final int initialEventSize = Statics.eventsMap.size();
        int nRemovedScraperEvents = 0;

//        synchronized (map) {
        final int initialSize = map.size();

        if (map.containsKey(null)) {
            final ScraperEvent removedScraperEvent = map.remove(null);
            logger.error("null key found in scraperEventsMap for: {} {}", clazz.getSimpleName(), Generic.objectToString(removedScraperEvent));
            nRemovedScraperEvents++;
//            map.remove(null);
        }
        if (map.containsValue(null)) {
            logger.error("null value found in scraperEventsMap {}", clazz.getSimpleName());
            final boolean modified = map.removeValueAll(null);
            if (modified) {
                nRemovedScraperEvents++;
            } else {
                logger.error("null value not removed from eventsMap in cleanScraperEventsMap {}", clazz.getSimpleName());
            }
        }

        final Set<? extends Entry<Long, ? extends ScraperEvent>> entrySetCopy = map.entrySetCopy();
        for (Entry<Long, ? extends ScraperEvent> entry : entrySetCopy) {
            final ScraperEvent value = entry.getValue();
            final long timeStamp = value.getTimeStamp();
            final Long key = entry.getKey();
            final String matchedEventId = value.getMatchedEventId();

            if ((matchedEventId != null && scraperEventsMapLastUpdate - timeStamp > Generic.HOUR_LENGTH_MILLISECONDS) ||
                (matchedEventId == null && scraperEventsMapLastUpdate - timeStamp > Generic.MINUTE_LENGTH_MILLISECONDS * 5L)) {
                final ScraperEvent removedScraperEvent = removeScraper(clazz, key);
                if (removedScraperEvent != null) {
                    nRemovedScraperEvents++;
                } else {
                    logger.error("scraperEvent not removed from {} scraperEventsMap in cleanScraperEventsMap for: {} {}", clazz.getSimpleName(), key, Generic.objectToString(value));
                }

                if (matchedEventId != null) {
                    logger.error("matched scraperEvent getting removed during maintenance: {} {} {} {}", matchedEventId, clazz, key, Generic.objectToString(removedScraperEvent));
                } else { // normal, non matched scraper removed
                }

//                map.remove(key);
//                // removedScraperEventIds.add(key);
//                if (matchedEventId != null && Statics.eventsMap.containsKey(matchedEventId)) {
//                    Statics.eventsMap.remove(matchedEventId);
//                    removedEventIds.add(matchedEventId);
//                }
            } else { // not expired yet, nothing to be done
            }
        } // end for

        if (nRemovedScraperEvents > 0) {
            final int newSize = map.size();
            logger.info("cleaned {} scraperEventsMap, nRemovedScraperEvents: {} initialSize: {} newSize: {} in {} ms", clazz.getSimpleName(), nRemovedScraperEvents, initialSize,
                        newSize, System.currentTimeMillis() - startTime);

            if (Statics.safeBetModuleActivated) {
                Statics.timeStamps.setLastMapEventsToScraperEvents(System.currentTimeMillis() + Generic.MINUTE_LENGTH_MILLISECONDS);
                Statics.threadPoolExecutor.execute(new LaunchCommandThread("mapEventsToScraperEvents"));
            }
        } else { // nothing removed, I guess nothing needs to be printed
        }
    }

    private static int removeScrapers(LinkedHashMap<Class<? extends ScraperEvent>, Long> map) {
        int nRemoved = 0;
        if (map == null) {
            logger.warn("null map in removeScrapers");
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

    private static <T extends ScraperEvent> T removeScraper(Class<T> clazz, Long scraperEventId) {
        @SuppressWarnings("unchecked") final SynchronizedMap<Long, T> scraperEventsMap = (SynchronizedMap<Long, T>) Formulas.getScraperEventsMap(clazz);
        final T existingScraperEvent;
        if (scraperEventId != null && scraperEventId >= 0) {
            existingScraperEvent = scraperEventsMap.remove(scraperEventId);
        } else {
            logger.error("null or negative scraperEventId {} in removeScraper for: {}", scraperEventId, clazz.getSimpleName());
            existingScraperEvent = null;
        }

        if (existingScraperEvent != null) {
            final String matchedEventId = existingScraperEvent.getMatchedEventId();
            if (matchedEventId != null) {
//            ignoreEvent(matchedEventId, safetyPeriod, shortSafetyPeriod);
                if (BlackList.notExist(Event.class, matchedEventId)) { // matched event was removed already, normal behavior
                } else {
                    logger.error("scraper getting removed and matched event still present in maps: {} {} {} {}", scraperEventId, clazz.getSimpleName(), matchedEventId, Generic.objectToString(existingScraperEvent));
                }
//                BlackList.checkEventNMatched(matchedEventId, clazz);
            } else { // no event attached, nothing to be done
            }
        } else {
            logger.error("no existingScraperEvent found in removeScraper for: {} {}", clazz.getSimpleName(), scraperEventId);
        }

        return existingScraperEvent;
    }

    private static <T extends ScraperEvent> T removeScraper(T scraperEvent) {
        final T existingScraperEvent;
        if (scraperEvent == null) {
            logger.error("null scraperEvent in removeScraper");
            existingScraperEvent = null;
        } else {
            final long scraperEventId = scraperEvent.getEventId();
//        scraperEvent.setIgnored(safetyPeriod);

            @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>) scraperEvent.getClass();
            existingScraperEvent = removeScraper(clazz, scraperEventId);

//            @SuppressWarnings("unchecked") final SynchronizedMap<Long, T> scraperEventsMap = (SynchronizedMap<Long, T>) Formulas.getScraperEventsMap(clazz);
//            if (scraperEventId >= 0) {
//                existingScraperEvent = scraperEventsMap.get(scraperEventId);
//            } else {
//                logger.error("negative scraperEventId {} in removeScraper for: {}", scraperEventId, scraperEvent);
//                existingScraperEvent = null;
//            }
//            if (existingScraperEvent != null) {
//                scraperEventsMap.remove(scraperEventId);
//            }

            // this portion is passed in the invoked overloaded removeScraper method
//            String matchedEventId = null;
//            if (existingScraperEvent != null) {
//                matchedEventId = existingScraperEvent.getMatchedEventId();
//            } else { // no ScraperEvent present in the map, nothing to be done here
//            }
//            if (matchedEventId == null) {
//                matchedEventId = scraperEvent.getMatchedEventId();
//            } else { // using the matchedEventId I have
//            }
//
//            if (matchedEventId != null) {
////            ignoreEvent(matchedEventId, safetyPeriod, shortSafetyPeriod);
//                if (BlackList.notExist(Event.class, matchedEventId)) { // matched event was removed already, normal behavior
//                } else {
//                    logger.error("scraper getting removed and matched event still present in maps: {} {} {}", scraperEventId, matchedEventId, Generic.objectToString(scraperEvent));
//                }
////                BlackList.checkEventNMatched(matchedEventId, clazz);
//            } else { // no event attached, nothing to be done
//            }
        }

        return existingScraperEvent;
    }

    private static Event removeEvent(Event event) {
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
            } else { // already removed, nothing to be done
            }
        }

        return existingEvent;
    }

    private static Event removeEvent(String eventId) {
        final Event matchedEvent = Statics.eventsMap.remove(eventId);

        if (matchedEvent != null) {
            int nRemovedMarkets = removeMarkets(matchedEvent);
//            final Set<Entry<String, MarketCatalogue>> entrySetCopy = Statics.marketCataloguesMap.entrySetCopy();
//            for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
//                final MarketCatalogue marketCatalogue = entry.getValue();
//                if (marketCatalogue != null) {
//                    final Event eventStump = marketCatalogue.getEventStump();
//                    if (matchedEvent.equals(eventStump)) { // it doesn't matter if it's null
//                        final String marketId = entry.getKey();
//                        removeMarket(marketId);
//                        nRemovedMarkets++;
//                    } else { // not interesting marketCatalogue, nothing to be done
//                    }
//                } else {
//                    logger.error("null marketCatalogue value found in marketCataloguesMap during removeEvent for: {}", entry.getKey());
//                    Statics.marketCataloguesMap.removeValueAll(null);
//                }
//            } // end for

            final int nScraperEventIds = matchedEvent.getNTotalScraperEventIds();
            if (nScraperEventIds > 0) {
                final LinkedHashMap<Class<? extends ScraperEvent>, Long> scraperEventIds = matchedEvent.getScraperEventIds();
                final Set<Entry<Class<? extends ScraperEvent>, Long>> entrySet = scraperEventIds.entrySet();
                for (final Entry<Class<? extends ScraperEvent>, Long> entry : entrySet) {
                    final Class<? extends ScraperEvent> clazz = entry.getKey();
                    final Long scraperId = entry.getValue();
//                    final SynchronizedMap<Long, ? extends ScraperEvent> scraperEventMap = Formulas.getScraperEventsMap(clazz);
                    final ScraperEvent scraperEvent = removeScraper(clazz, scraperId);
                    if (scraperEvent != null) {
                        final String matchedEventId = scraperEvent.getMatchedEventId();
                        if (matchedEventId.equals(eventId)) { // it gets removed, nothing else needs to be done
//                            scraperEvent.resetMatchedEventId();
                        } else {
                            logger.error("matchedEventId {} not equals eventId {} in removeEvent for: {} {}", matchedEventId, eventId, Generic.objectToString(matchedEvent), Generic.objectToString(scraperEvent));
                        }
                    } else { // scraperEvent not found in map; might be acceptable, will reevaluate after adding ignore list
                    }
                } // end for
            } else { // no matched scrapers, nothing to be done about it
            }

            if (nRemovedMarkets == 0 && nScraperEventIds >= Statics.MIN_MATCHED) {
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

    private static int removeMarkets(Event event) {
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

    private static MarketCatalogue removeMarket(String marketId) {
        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.remove(marketId);
        removeFromSecondaryMaps(marketId);

        return marketCatalogue;
    }

    public static void removeFromSecondaryMaps(String marketId) {
        final SynchronizedMap<SafeBet, SafeBetStats> safeBetsStatsMap = Statics.safeBetsMap.remove(marketId);
        if (safeBetsStatsMap != null && !safeBetsStatsMap.isEmpty()) {
            final Set<Entry<SafeBet, SafeBetStats>> entrySetCopy = safeBetsStatsMap.entrySetCopy();
            for (final Entry<SafeBet, SafeBetStats> entry : entrySetCopy) {
                final SafeBet key = entry.getKey();
                final SafeBetStats value = entry.getValue();
                if (value != null) {
                    String printedString = MessageFormatter.arrayFormat("blackRList {} {} {} {}", new Object[]{value.printStats(), key.printStats(), Generic.objectToString(key), Generic.objectToString(value)}).getMessage();
                    logger.warn(printedString);
                    Statics.safebetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");
                } else {
                    logger.error("null SafeBetStats in safeBetsStatsMap for: {}", Generic.objectToString(key));
                }
            } // end for
        } else { // no safeBetsStatsMap exists, nothing to be done
        }

        // Statics.interestingMarketsSet.removeAll(toRemoveSet);
        removeSafeMarket(marketId);
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

    public static SynchronizedSafeSet<SafeRunner> removeSafeMarket(String marketId) {
        final SynchronizedSafeSet<SafeRunner> safeRunners = Statics.safeMarketsMap.remove(marketId);
        if (safeRunners != null) {
            safeRunners.clear();
        } else { // with safeBetModule disabled, there are no safeRunners anymore
            if (Statics.safeBetModuleActivated) {
                logger.warn("null safeRunners in removeSafeMarket for: {}", marketId);
            } else {
//                Generic.alreadyPrintedMap.logOnce(Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.INFO, "null safeRunners in removeSafeMarket for: {}", marketId);
                Generic.alreadyPrintedMap.logOnce(Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.INFO, "null safeRunners in removeSafeMarket");
            }
        }
        return safeRunners;
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "maintenance thread");
                }
                long timeToSleep;

                if (Statics.needSessionToken.get()) {
                    Betty.authenticate(Statics.AUTH_URL, Statics.bu, Statics.bp, Statics.sessionTokenObject, Statics.KEY_STORE_FILE_NAME, Statics.KEY_STORE_PASSWORD, Statics.KEY_STORE_TYPE, Statics.appKey);
                }
                if (Statics.mustWriteObjects.get()) {
                    VarsIO.writeObjectsToFiles();
                    Statics.mustWriteObjects.set(false);
                }
                if (Statics.orderToPrint.get() != null) {
                    String orders = Statics.orderToPrint.getAndSet(null);
                    orders = orders.trim();
                    final String[] ordersArray = orders.split(" ");

                    for (String singleOrder : ordersArray) {
                        if (singleOrder.equals("findmarkettypes")) {
                            DebuggingMethods.findNewMarketTypes();
                        } else if (singleOrder.equals("cancelorders")) {
                            CancelOrdersThread.addOrder(0L);
                        } else if (singleOrder.equals("weighttest")) {
                            DebuggingMethods.weightTest();
                        } else if (singleOrder.startsWith("market:")) {
                            String marketId = singleOrder.substring("market:".length());
                            DebuggingMethods.printMarket(marketId);
                        } else if (singleOrder.startsWith("event:")) {
                            String eventId = singleOrder.substring("event:".length());
                            DebuggingMethods.printEvent(eventId);
                        } else if (singleOrder.startsWith("markettype:")) {
                            String marketType = singleOrder.substring("markettype:".length());
                            DebuggingMethods.printMarketType(marketType);
                        } else {
                            logger.error("unknown order in maintenance thread: {}", singleOrder);
                        }
                    }
                }

                timeToSleep = timedSaveObjects();
                timeToSleep = Math.min(timeToSleep, timedCleanEventsMap());
                timeToSleep = Math.min(timeToSleep, timedCleanScraperEventsMap());
                timeToSleep = Math.min(timeToSleep, timedCleanMarketCataloguesMap());
                timeToSleep = Math.min(timeToSleep, timedCleanSecondaryMaps());
                timeToSleep = Math.min(timeToSleep, timedCleanSafeMarketsImportantMap());
                timeToSleep = Math.min(timeToSleep, timedCleanTimedMaps());
                timeToSleep = Math.min(timeToSleep, timedCleanSafeBetsMap());
                timeToSleep = Math.min(timeToSleep, timedPrintDebug());
                timeToSleep = Math.min(timeToSleep, timedPrintAverages());
                // timeToSleep = Math.min(timeToSleep, BlackList.timedCheckExpired());
                timeToSleep = Math.min(timeToSleep, timedReadAliases());

                Generic.threadSleepSegmented(timeToSleep, 100L, Statics.mustStop, Statics.mustWriteObjects, Statics.needSessionToken, Statics.orderToPrint);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside Maintenance loop", throwable);
            }
        } // end while

        finalCleanSafeBetsMap();

        logger.info("maintenance thread ends");
        if (Statics.orderToPrint.get() != null) {
            logger.info("order not executed due to program stop: {}", Statics.orderToPrint.get());
        }
    }
}
