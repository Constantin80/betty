package info.fmro.betty.threads.permanent;

import info.fmro.betty.main.Betty;
import info.fmro.betty.main.VarsIO;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.safebet.SafeBet;
import info.fmro.betty.safebet.SafeBetStats;
import info.fmro.betty.safebet.SafeRunner;
import info.fmro.betty.safebet.ScraperEvent;
import info.fmro.betty.threads.LaunchCommandThread;
import info.fmro.betty.utility.DebuggingMethods;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.entities.Event;
import info.fmro.shared.entities.MarketBook;
import info.fmro.shared.entities.MarketCatalogue;
import info.fmro.shared.enums.CommandType;
import info.fmro.shared.enums.ScrapedField;
import info.fmro.shared.objects.StampedDouble;
import info.fmro.shared.objects.TwoOrderedStrings;
import info.fmro.shared.stream.objects.ScraperEventInterface;
import info.fmro.shared.utility.BlackList;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.SynchronizedMap;
import info.fmro.shared.utility.SynchronizedSafeSet;
import info.fmro.shared.utility.SynchronizedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.lang.management.ThreadInfo;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass", "ReuseOfLocalVariable"})
public class MaintenanceThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceThread.class);
    @NotNull
    public static final Method removeFromSecondaryMapsMethod = Objects.requireNonNull(Generic.getMethod(MaintenanceThread.class, "removeFromSecondaryMaps", String.class));

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
    private static long timedPrintAverages() {
        long timeForNext = Statics.timeStamps.getLastPrintAverages();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            Statics.timeStamps.lastPrintAveragesStamp(Statics.DELAY_PRINT_AVERAGES);
            if (Statics.safeBetModuleActivated) {
                if (Statics.betradarScraperThread.averageLogger != null) {
                    Statics.betradarScraperThread.averageLogger.printRecords();
                }
                if (Statics.betradarScraperThread.averageLoggerFull != null) {
                    Statics.betradarScraperThread.averageLoggerFull.printRecords();
                }
                if (Statics.coralScraperThread.averageLogger != null) {
                    Statics.coralScraperThread.averageLogger.printRecords();
                }
                // Statics.coralScraperThread.averageLoggerFull.printRecords(); // this will have a check on Statics.coralScraperThread.singleLogger
                Statics.quickCheckThread.averageLogger.printRecords();
            }

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

    private static long timedReadAliases() {
        long timeForNext = Statics.timeStamps.getLastCheckAliases();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            Statics.timeStamps.lastCheckAliasesStamp(Generic.MINUTE_LENGTH_MILLISECONDS);

            VarsIO.checkAliasesFile(Statics.ALIASES_FILE_NAME, Statics.aliasesTimeStamp, Formulas.aliasesMap);
            VarsIO.checkAliasesFile(Statics.FULL_ALIASES_FILE_NAME, Statics.fullAliasesTimeStamp, Formulas.fullAliasesMap);

            timeForNext = Statics.timeStamps.getLastCheckAliases();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    @SuppressWarnings("OverlyNestedMethod")
    private static long timedCheckDeadlock() {
        long timeForNext = Statics.timeStamps.getLastCheckDeadlock();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            Statics.timeStamps.lastCheckDeadlockStamp(Generic.HOUR_LENGTH_MILLISECONDS);

            final long[] deadlockedThreadIds = Statics.threadMXBean.findDeadlockedThreads();
            if (deadlockedThreadIds != null) {
                logger.error("Deadlock detected!");
                final ThreadInfo[] deadlockedThreadsInfo = Statics.threadMXBean.getThreadInfo(deadlockedThreadIds);
                final Map<Thread, StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
                for (final ThreadInfo threadInfo : deadlockedThreadsInfo) {
                    if (threadInfo != null) {
                        for (final Thread thread : stackTraceMap.keySet()) {
                            if (thread == null) {
                                logger.error("null thread in timedCheckDeadlock in: {}", Generic.objectToString(stackTraceMap.keySet()));
                            } else if (thread.getId() == threadInfo.getThreadId()) {
                                logger.error(threadInfo.toString().trim());
                                for (final StackTraceElement stackTraceElement : thread.getStackTrace()) {
                                    logger.error("\t{}", stackTraceElement.toString().trim());
                                }
                                break;
                            } else { // not the element I'm searching, loop will continue
                            }
                        }
                    } else {
                        logger.error("null threadInfo in timedCheckDeadlock in: {}", Generic.objectToString(deadlockedThreadsInfo));
                    }
                }
            } else { // no deadlocked threads, nothing to be done
            }

            timeForNext = Statics.timeStamps.getLastCheckDeadlock();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedPrintDebug() {
        long timeForNext = Statics.timeStamps.getLastPrintDebug();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            Statics.timeStamps.lastPrintDebugStamp(Generic.MINUTE_LENGTH_MILLISECONDS << 1);
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
            logger.info("connManager stats: {} writeSettingsCounter: {} BetFrequencyLimit.nOrdersSinceReset: {}", Statics.connManager.getTotalStats(), Generic.addCommas(VarsIO.writeSettingsCounter.get()),
                        Generic.addCommas(Statics.safetyLimits.speedLimit.getNOrdersSinceReset()));

            timeForNext = Statics.timeStamps.getLastPrintDebug();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedSaveObjects() {
        long timeForNext = Statics.timeStamps.getLastObjectsSave();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            VarsIO.writeObjectsToFiles();

            timeForNext = Statics.timeStamps.getLastObjectsSave();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedSaveSettings() {
        long timeForNext = Statics.timeStamps.getLastSettingsSave();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            VarsIO.writeSettings();

            timeForNext = Statics.timeStamps.getLastSettingsSave();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedCleanScraperEventsMap() {
        long timeForNext = Statics.timeStamps.getLastCleanScraperEventsMap();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            Statics.timeStamps.lastCleanScraperEventsMapStamp(Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
            for (final Class<? extends ScraperEvent> classFromSet : Statics.scraperEventSubclassesSet) {
                cleanScraperEventsMap(classFromSet);
            }

            timeForNext = Statics.timeStamps.getLastCleanScraperEventsMap();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

//    public static long timedCleanMarketCache() {
//        long timeForNext = Statics.marketCache.getTimeClean();
//        long timeTillNext = timeForNext - System.currentTimeMillis();
//        if (timeTillNext <= 0) {
//            cleanMarketCache();
//            timeForNext = Statics.marketCache.getTimeClean();
//
//            timeTillNext = timeForNext - System.currentTimeMillis();
//        } else { // nothing to be done
//        }
//        return timeTillNext;
//    }

    private static long timedCleanEventsMap() {
        long timeForNext = Statics.eventsMap.getTimeClean();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            cleanEventsMap();

            timeForNext = Statics.eventsMap.getTimeClean();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedCleanMarketCataloguesMap() {
        long timeForNext = Statics.marketCataloguesMap.getTimeClean();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            cleanMarketCataloguesMap();

            timeForNext = Statics.marketCataloguesMap.getTimeClean();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedCleanSecondaryMaps() {
        long timeForNext = Statics.timeStamps.getLastCleanSecondaryMaps();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            cleanSecondaryMaps();

            timeForNext = Statics.timeStamps.getLastCleanSecondaryMaps();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedCleanSafeMarketsImportantMap() {
        long timeForNext = Statics.safeMarketsImportantMap.getTimeClean();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            cleanSafeMarketsImportantMap();

            timeForNext = Statics.safeMarketsImportantMap.getTimeClean();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedCleanTimedMaps() {
        long timeForNext = Statics.timeStamps.getLastCleanTimedMaps();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            cleanTimedMaps();

            timeForNext = Statics.timeStamps.getLastCleanTimedMaps();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedCleanSafeBetsMap() {
        long timeForNext = Statics.safeBetsMap.getTimeClean();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0L) {
            cleanSafeBetsMap();

            timeForNext = Statics.safeBetsMap.getTimeClean();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    @SuppressWarnings("OverlyNestedMethod")
    private static void cleanSafeBetsMap() {
        Statics.safeBetsMap.timeCleanStamp(20_000L);
        final long startTime = System.currentTimeMillis();
        final int initialSize = Statics.safeBetsMap.size();
        if (initialSize > 0) {
            final Set<Entry<String, SynchronizedMap<SafeBet, SafeBetStats>>> entrySetCopy = Statics.safeBetsMap.entrySetCopy();
            for (final Entry<String, SynchronizedMap<SafeBet, SafeBetStats>> entry : entrySetCopy) {
                final SynchronizedMap<SafeBet, SafeBetStats> value = entry.getValue();
                final String marketId = entry.getKey();
                if (value == null) {
                    logger.error("null value during cleanSafeBetsMap for: {}", marketId);
                    Statics.safeBetsMap.removeValueAll(null);
                } else {
                    if (BlackList.notExistOrIgnored(MarketCatalogue.class, marketId, Formulas.getIgnorableMapMethod)) {
                        BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "marketCatalogue, during cleanSafeBetsMap", Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD);
                        removeFromSecondaryMaps(marketId);
                    } else {
                        final Set<Entry<SafeBet, SafeBetStats>> entrySetInnerCopy = value.entrySetCopy();
                        for (final Entry<SafeBet, SafeBetStats> entryInner : entrySetInnerCopy) {
                            final SafeBetStats safeBetStats = entryInner.getValue();
                            final long timeLastAppear = safeBetStats.getTimeLastAppear();
                            if (startTime - timeLastAppear > Generic.MINUTE_LENGTH_MILLISECONDS) {
                                final SafeBet safeBet = entryInner.getKey();

                                final String printedString =
                                        MessageFormatter.arrayFormat("noLongerAppearsFor {} ms: {} {} {}",
                                                                     new Object[]{startTime - timeLastAppear, safeBetStats.printStats(), Generic.objectToString(safeBet), Generic.objectToString(safeBetStats)}).getMessage();
                                logger.warn(printedString);
                                Statics.safeBetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");

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

    private static void finalCleanSafeBetsMap() {
        try {
            Statics.safeBetsMap.timeCleanStamp(20_000L);
            final int initialSize = Statics.safeBetsMap.size();
            if (initialSize > 0) {
                final Set<Entry<String, SynchronizedMap<SafeBet, SafeBetStats>>> entrySetCopy = Statics.safeBetsMap.entrySetCopy();
                for (final Entry<String, SynchronizedMap<SafeBet, SafeBetStats>> entry : entrySetCopy) {
                    final SynchronizedMap<SafeBet, SafeBetStats> value = entry.getValue();
                    final String key = entry.getKey();
                    if (value == null) {
                        logger.error("null value during cleanSafeBetsMap for: {}", key);
                        Statics.safeBetsMap.removeValueAll(null);
                    } else {
                        final Set<Entry<SafeBet, SafeBetStats>> entrySetInnerCopy = value.entrySetCopy();
                        for (final Entry<SafeBet, SafeBetStats> entryInner : entrySetInnerCopy) {
                            final SafeBetStats valueInner = entryInner.getValue();
                            // long timeLastAppear = valueInner.getTimeLastAppear();
                            // if (currentTime - timeLastAppear > Generic.MINUTE_LENGTH_MILLISECONDS) {
                            final SafeBet keyInner = entryInner.getKey();

                            final String printedString = MessageFormatter.arrayFormat("final {} {} {} {}", new Object[]{valueInner.printStats(), keyInner.printStats(), Generic.objectToString(keyInner), Generic.objectToString(valueInner)}).getMessage();
                            logger.info(printedString);
                            Statics.safeBetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");

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

    private static void cleanTimedMaps() {
        Statics.timeStamps.lastCleanTimedMapsStamp(Generic.MINUTE_LENGTH_MILLISECONDS * 30L);
        final long startTimeTimedWarnings = System.currentTimeMillis();
//        synchronized (Statics.timedWarningsMap) {
        int initialSize = Statics.timedWarningsMap.size();
        final Collection<Long> valuesCopy = Statics.timedWarningsMap.valuesCopy();
        for (final Long value : valuesCopy) {
            if (value == null) {
                logger.error("null value during clean timedWarningsMap");
                Statics.timedWarningsMap.removeValueAll(null); // remove null
            } else {
                final long primitive = value;
                if (startTimeTimedWarnings - primitive > Generic.HOUR_LENGTH_MILLISECONDS << 1) {
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
        for (final Entry<TwoOrderedStrings, StampedDouble> entry : entriesCopy) {
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

    private static void cleanSafeMarketsImportantMap() {
        Statics.safeMarketsImportantMap.timeCleanStamp(Generic.MINUTE_LENGTH_MILLISECONDS);
        final long startTime = System.currentTimeMillis();

        final int initialSize = Statics.safeMarketsImportantMap.size();
        final Collection<Long> valuesCopy = Statics.safeMarketsImportantMap.valuesCopy();
        for (final Long value : valuesCopy) {
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
        for (final String marketId : marketIds) {
            if (BlackList.notExistOrIgnored(MarketCatalogue.class, marketId, Formulas.getIgnorableMapMethod)) {
                BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "marketCatalogue, during cleanSafeMarketsImportantMap", Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD);
                removeFromSecondaryMaps(marketId);
            }
        } // end for

        final int newSize = Statics.safeMarketsImportantMap.size();
        if (newSize != initialSize) {
            logger.info("cleaned safeMarketsImportantMap, initialSize: {} newSize: {} in {} ms", initialSize, newSize, System.currentTimeMillis() - startTime);
        }
    }

    @SuppressWarnings("OverlyNestedMethod")
    private static void cleanSecondaryMaps() {
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
        for (final Entry<String, SynchronizedSafeSet<SafeRunner>> entry : entrySetCopy) {
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
                for (final SafeRunner safeRunner : setCopy) {
                    if (safeRunner == null) {
                        logger.error("null safeRunner in usedScrapersMap during cleanup: {}", marketId);
                        runnersSet.remove(null);
                    } else {
                        final EnumMap<ScrapedField, SynchronizedMap<Class<? extends ScraperEventInterface>, Long>> usedScrapers = safeRunner.getUsedScrapers();
                        if (usedScrapers == null) {
                            logger.error("null usedScrapers in usedScrapersMap during cleanup: {} {}", marketId, Generic.objectToString(safeRunner));
                            runnersSet.remove(safeRunner);
                        } else {
                            for (final Entry<ScrapedField, SynchronizedMap<Class<? extends ScraperEventInterface>, Long>> usedEntry : usedScrapers.entrySet()) {
                                final ScrapedField scrapedField = usedEntry.getKey();
                                final SynchronizedMap<Class<? extends ScraperEventInterface>, Long> map = usedEntry.getValue();
                                if (scrapedField == null || map == null) {
                                    logger.error("null scrapedField or map in usedScrapersMap during cleanup: {} {}", marketId, Generic.objectToString(usedScrapers));
                                    runnersSet.remove(safeRunner);
                                } else {
                                    final Set<Entry<Class<? extends ScraperEventInterface>, Long>> entrySet = map.entrySetCopy();
                                    for (final Entry<Class<? extends ScraperEventInterface>, Long> innerEntry : entrySet) {
                                        final Class<? extends ScraperEventInterface> clazz = innerEntry.getKey();
                                        final Long scraperId = innerEntry.getValue();
                                        if (clazz == null || scraperId == null) {
                                            logger.error("null clazz or scraperId in usedScrapersMap during cleanup: {} {}", marketId, Generic.objectToString(map));
                                            map.remove(clazz);
                                        } else {
                                            final SynchronizedMap<Long, ? extends ScraperEventInterface> staticsMap = Formulas.getScraperEventsMap(clazz);
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
        for (final String marketId : marketIds) {
            if (BlackList.notExistOrIgnored(MarketCatalogue.class, marketId, Formulas.getIgnorableMapMethod)) {
                BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "marketCatalogue, during cleanSecondaryMaps", Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD);
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
            logger.info("secondary cleaned safeMarketBooksMap, initialSize: {} newSize: {} in {} ms", initialSizeSafeMarketBooksMap, newSize, System.currentTimeMillis() - startTime);
        }
    }

    private static void checkScraperEvents(final Event event) {
        if (event != null) {
            final LinkedHashMap<Class<? extends ScraperEventInterface>, Long> scraperEventIds = event.getScraperEventIds();
            if (scraperEventIds != null && !scraperEventIds.isEmpty()) {
                for (final Entry<Class<? extends ScraperEventInterface>, Long> entry : scraperEventIds.entrySet()) {
                    final Class<? extends ScraperEventInterface> clazz = entry.getKey();
                    final long scraperId = entry.getValue();
                    final SynchronizedMap<Long, ? extends ScraperEventInterface> scraperMap = Formulas.getScraperEventsMap(clazz);
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

    @SuppressWarnings("OverlyNestedMethod")
    private static void cleanMarketCataloguesMap() {
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
        for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
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
                                final int nScraperEventIds =
                                        event.getNValidScraperEventIds(removeFromSecondaryMapsMethod, Statics.threadPoolExecutor, LaunchCommandThread.constructorLinkedHashSetLongMarket, Statics.safeBetModuleActivated, Statics.MIN_MATCHED,
                                                                       Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, Statics.marketCataloguesMap, Formulas.getScraperEventsMapMethod, Formulas.getIgnorableMapMethod,
                                                                       LaunchCommandThread.constructorHashSetLongEvent);
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
                                    logger.error("marketIgnoredExpiry {} smaller than eventIgnoredExpiry {} by {} in cleanMarketCataloguesMap for: {} {}", marketIgnoredExpiration, eventIgnoredExpiration, eventIgnoredExpiration - marketIgnoredExpiration,
                                                 Generic.objectToString(marketCatalogue), Generic.objectToString(event));
//                                marketCatalogue.setIgnoredExpiration(eventIgnoredExpiration);
                                    marketCatalogue.setIgnored(0L, eventIgnoredExpiration, removeFromSecondaryMapsMethod, Statics.threadPoolExecutor, LaunchCommandThread.constructorLinkedHashSetLongMarket, Statics.safeBetModuleActivated);
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
                                logger.error("marketCatalogue without event in map not removed from marketCataloguesMap in cleanMarketCataloguesMap for: {}", Generic.objectToString(marketCatalogue));
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
                            logger.error("marketCatalogue with null event not removed from marketCataloguesMap in cleanMarketCataloguesMap for: {}", Generic.objectToString(marketCatalogue));
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

    @SuppressWarnings("OverlyNestedMethod")
    private static void cleanEventsMap() {
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

        final Collection<Event> removedEvents = new HashSet<>(2);
        final Set<Entry<String, Event>> entrySetCopy = Statics.eventsMap.entrySetCopy();
        for (final Entry<String, Event> entry : entrySetCopy) {
            final Event event = entry.getValue();
            final long timeStamp = event.getTimeStamp();
//            final Date openDate = event.getOpenDate();
//            final long openTime = openDate == null ? -1 : openDate.getTime();
            final String eventId = entry.getKey();
            if (eventsMapLastUpdate - timeStamp > Generic.MINUTE_LENGTH_MILLISECONDS * 5L || startTime - eventsMapLastUpdate > Generic.DAY_LENGTH_MILLISECONDS << 1) {
                final Event removedEvent = removeEvent(eventId, false);
//                Statics.eventsMap.remove(key);
//                removedEventIds.add(key);
                if (removedEvent != null) {
                    removedEvents.add(removedEvent);
                    nEventsRemoved++;
                } else {
                    logger.error("event not removed from eventsMap in cleanEventsMap for: {} {}", eventId, Generic.objectToString(event));
                }

                final LinkedHashMap<Class<? extends ScraperEventInterface>, Long> scraperEventIds = event.getScraperEventIds();
                nScraperEventsRemoved += removeScrapers(scraperEventIds);
//                removeScraperEvents(scraperEventIds);
            } else if (Statics.safeBetModuleActivated) { // not expired yet, will check to see if attached scraperEvents are still in their map
                final LinkedHashMap<Class<? extends ScraperEventInterface>, Long> scraperEventIds = event.getScraperEventIds();
                if (scraperEventIds != null) {
                    if (scraperEventIds.isEmpty()) { // no attached scraperEventsIds, nothing to be done
                    } else {
                        final Set<Entry<Class<? extends ScraperEventInterface>, Long>> scraperEventIdsEntries = scraperEventIds.entrySet();
                        for (final Entry<Class<? extends ScraperEventInterface>, Long> scraperEntry : scraperEventIdsEntries) {
                            final Class<? extends ScraperEventInterface> clazz = scraperEntry.getKey();
                            final Long scraperId = scraperEntry.getValue();
                            final SynchronizedMap<Long, ? extends ScraperEventInterface> scraperMap = Formulas.getScraperEventsMap(clazz);
                            final ScraperEventInterface scraperEvent = scraperMap.get(scraperId);

                            if (scraperEvent == null) {
                                logger.error("non existent matchedScraper {} {} found in cleanEventsMap for: {} {}", clazz.getSimpleName(), scraperId, eventId, Generic.objectToString(event));
                                event.setIgnored(Generic.DAY_LENGTH_MILLISECONDS, removeFromSecondaryMapsMethod, Statics.threadPoolExecutor, LaunchCommandThread.constructorLinkedHashSetLongMarket, Statics.safeBetModuleActivated,
                                                 Statics.marketCataloguesMap, LaunchCommandThread.constructorHashSetLongEvent); // error is no joke
//                                event.removeScraperEventId(clazz);
                            } else { // everything is good, nothing to be done, ignored scrapers are OK
                            }
                        }
                    }
                } else {
                    logger.error("null scraperEventIds in cleanEventsMap for: {} {}", eventId, Generic.objectToString(event));
                }
            } else { // safeBetModule not activated and event not expired, nothing to be done
//                final long currentTime = System.currentTimeMillis();
//                logger.info("event not removed: {} {} {} {} {} {}", eventsMapLastUpdate, timeStamp, eventsMapLastUpdate - timeStamp, currentTime, currentTime - eventsMapLastUpdate, currentTime - timeStamp);
            }
        } // end for
        //        } // end synchronized block

        int removedMarkets = 0;
        if (!removedEvents.isEmpty()) {
            removedMarkets = removeMarkets(removedEvents);
        }
        if (nEventsRemoved > 0) {
            final int newSize = Statics.eventsMap.size();
            logger.info("cleaned eventsMap, nEventsRemoved: {} initialSize: {} newSize: {}, markets removed: {} left: {}, in {} ms", nEventsRemoved, initialSize, newSize, removedMarkets, Statics.marketCataloguesMap.size(),
                        System.currentTimeMillis() - startTime);
        } else { // nothing removed, I guess nothing needs to be printed
        }

        if (nScraperEventsRemoved > 0) {
            final int newCoralSize = Statics.coralEventsMap.size();
            final int newBetradarSize = Statics.betradarEventsMap.size();
            logger.info("cleaned eventsMap, nScraperEventsRemoved: {} initialCoralSize: {} newCoralSize: {} initialBetradarSize: {} newBetradarSize: {} in {} ms", nScraperEventsRemoved, initialCoralSize, newCoralSize, initialBetradarSize, newBetradarSize,
                        System.currentTimeMillis() - startTime);
        } else { // nothing removed, I guess nothing needs to be printed
        }
    }

    private static void cleanScraperEventsMap(final Class<? extends ScraperEventInterface> clazz) {
        final SynchronizedMap<Long, ? extends ScraperEventInterface> map = Formulas.getScraperEventsMap(clazz);
        final long startTime = System.currentTimeMillis();

        final long scraperEventsMapLastUpdate = map.getTimeStamp();
        // HashSet<Long> removedScraperEventIds = new HashSet<>(0);
//        final HashSet<String> removedEventIds = new HashSet<>(0);
//        final int initialEventSize = Statics.eventsMap.size();
        int nRemovedScraperEvents = 0;

//        synchronized (map) {
        final int initialSize = map.size();

        if (map.containsKey(null)) {
            final ScraperEventInterface removedScraperEvent = map.remove(null);
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

        final Set<? extends Entry<Long, ? extends ScraperEventInterface>> entrySetCopy = map.entrySetCopy();
        for (final Entry<Long, ? extends ScraperEventInterface> entry : entrySetCopy) {
            final ScraperEventInterface value = entry.getValue();
            final long timeStamp = value.getTimeStamp();
            final Long key = entry.getKey();
            final String matchedEventId = value.getMatchedEventId();

            if (matchedEventId != null ? scraperEventsMapLastUpdate - timeStamp > Generic.HOUR_LENGTH_MILLISECONDS : scraperEventsMapLastUpdate - timeStamp > Generic.MINUTE_LENGTH_MILLISECONDS * 5L) {
                final ScraperEventInterface removedScraperEvent = removeScraper(clazz, key);
                if (removedScraperEvent != null) {
                    nRemovedScraperEvents++;
                } else {
                    logger.error("scraperEvent not removed from {} scraperEventsMap in cleanScraperEventsMap for: {} {}", clazz.getSimpleName(), key, Generic.objectToString(value));
                }

                if (matchedEventId != null) {
                    logger.error("matched scraperEvent getting removed during maintenance: {} {} {} {}", matchedEventId, clazz, key, Generic.objectToString(removedScraperEvent));
                } else { // normal, non matched scraper removed
                }
            } else { // not expired yet, nothing to be done
            }
        } // end for

        if (nRemovedScraperEvents > 0) {
            final int newSize = map.size();
            logger.info("cleaned {} scraperEventsMap, nRemovedScraperEvents: {} initialSize: {} newSize: {} in {} ms", clazz.getSimpleName(), nRemovedScraperEvents, initialSize, newSize, System.currentTimeMillis() - startTime);

            if (Statics.safeBetModuleActivated) {
                Statics.timeStamps.setLastMapEventsToScraperEvents(System.currentTimeMillis() + Generic.MINUTE_LENGTH_MILLISECONDS);
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.mapEventsToScraperEvents));
            }
        } else { // nothing removed, I guess nothing needs to be printed
        }
    }

    private static int removeScrapers(final Map<Class<? extends ScraperEventInterface>, Long> map) {
        int nRemoved = 0;
        if (map == null) {
            logger.warn("null map in removeScrapers");
        } else {
            final Set<Entry<Class<? extends ScraperEventInterface>, Long>> entrySet = map.entrySet();
            for (final Entry<Class<? extends ScraperEventInterface>, Long> entry : entrySet) {
                final Class<? extends ScraperEventInterface> clazz = entry.getKey();
                final Long id = entry.getValue();
                final ScraperEventInterface returnValue = removeScraper(clazz, id);
                if (returnValue == null) {
                    logger.error("scraperEvent not removed in removeScrapers for: {} {}", clazz.getSimpleName(), id);
                } else {
                    nRemoved++;
                }
            } // end for
        }

        return nRemoved;
    }

    private static <T extends ScraperEventInterface> T removeScraper(final Class<T> clazz, final Long scraperEventId) {
        @SuppressWarnings("unchecked") final SynchronizedMap<Long, T> scraperEventsMap = (SynchronizedMap<Long, T>) Formulas.getScraperEventsMap(clazz);
        @Nullable final T existingScraperEvent;
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
                if (BlackList.notExist(Event.class, matchedEventId, Formulas.getIgnorableMapMethod)) { // matched event was removed already, normal behavior
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

    @SuppressWarnings("unused")
    private static <T extends ScraperEvent> T removeScraper(final T scraperEvent) {
        @Nullable final T existingScraperEvent;
        if (scraperEvent == null) {
            logger.error("null scraperEvent in removeScraper");
            existingScraperEvent = null;
        } else {
            final long scraperEventId = scraperEvent.getEventId();
//        scraperEvent.setIgnored(safetyPeriod);

            @SuppressWarnings("unchecked") final Class<T> clazz = (Class<T>) scraperEvent.getClass();
            existingScraperEvent = removeScraper(clazz, scraperEventId);
        }

        return existingScraperEvent;
    }

    @SuppressWarnings("unused")
    private static Event removeEvent(final Event event) {
        @Nullable Event existingEvent;
        if (event == null) {
            logger.error("null event in removeEvent");
            existingEvent = null;
        } else {
            final String eventId = event.getId();
            existingEvent = removeEvent(eventId);
            if (event == existingEvent) { // already removed, nothing to be done
            } else {
                logger.error("removeEvent on an event different than the one existing in the map; this is probably not intended behaviour and is not supported: {} {} {}", eventId, Generic.objectToString(event), Generic.objectToString(existingEvent));
                existingEvent = null; // not supported
                // not supported, and probably not needed; should be invoked on existingEvent or eventId
            }
        }

        return existingEvent;
    }

    private static Event removeEvent(final String eventId) {
        return removeEvent(eventId, true); // by default, removeMarkets; keep in mind the performance hit is huge, markets should be removed for batches of events
    }

    @SuppressWarnings("OverlyNestedMethod")
    private static Event removeEvent(final String eventId, final boolean removeMarkets) {
        final Event matchedEvent = Statics.eventsMap.remove(eventId);

        if (matchedEvent != null) {
            final int nRemovedMarkets = removeMarkets ? removeMarkets(matchedEvent) : 0;
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
                final LinkedHashMap<Class<? extends ScraperEventInterface>, Long> scraperEventIds = matchedEvent.getScraperEventIds();
                final Set<Entry<Class<? extends ScraperEventInterface>, Long>> entrySet = scraperEventIds != null ? scraperEventIds.entrySet() : null;
                if (entrySet != null) {
                    for (final Entry<Class<? extends ScraperEventInterface>, Long> entry : entrySet) {
                        final Class<? extends ScraperEventInterface> clazz = entry.getKey();
                        final Long scraperId = entry.getValue();
                        //                    final SynchronizedMap<Long, ? extends ScraperEvent> scraperEventMap = Formulas.getScraperEventsMap(clazz);
                        final ScraperEventInterface scraperEvent = removeScraper(clazz, scraperId);
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
                } else {
                    logger.error("null scraperEventIds in removeEvent for: {} {} {}", nScraperEventIds, eventId, Generic.objectToString(matchedEvent));
                }
            } else { // no matched scrapers, nothing to be done about it
            }

            if (removeMarkets && nRemovedMarkets == 0 && nScraperEventIds >= Statics.MIN_MATCHED) {
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

    private static int removeMarkets(final Collection<Event> events) {
        int nPurgedMarkets = 0;
        if (events == null) {
            logger.error("null events set in removeMarkets");
        } else {
            final Set<Entry<String, MarketCatalogue>> entrySetCopy = Statics.marketCataloguesMap.entrySetCopy();
            for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
                final MarketCatalogue marketCatalogue = entry.getValue();
                if (marketCatalogue != null) {
                    final Event eventStump = marketCatalogue.getEventStump();
                    if (events.contains(eventStump)) { // it doesn't matter if marketCatalogueEvent is null
                        final String marketId = entry.getKey();
                        if (removeMarket(marketId) != null) {
                            nPurgedMarkets++;
                        } else {
                            logger.error("couldn't removeMarket in removeMarkets: {} {}", marketId, Generic.objectToString(marketCatalogue));
                        }
                    } else { // not interesting marketCatalogue, nothing to be done
                    }
                } else {
                    logger.error("null marketCatalogue value found in marketCataloguesMap during removeMarkets(set) for: {}", entry.getKey());
                    Statics.marketCataloguesMap.removeValueAll(null);
                }
            } // end for
        }

        return nPurgedMarkets;
    }

    private static int removeMarkets(final Event event) {
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

    private static MarketCatalogue removeMarket(final String marketId) {
        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.remove(marketId);
        removeFromSecondaryMaps(marketId);

        return marketCatalogue;
    }

    public static void removeFromSecondaryMaps(final String marketId) {
        final SynchronizedMap<SafeBet, SafeBetStats> safeBetsStatsMap = Statics.safeBetsMap.remove(marketId);
        if (safeBetsStatsMap != null && !safeBetsStatsMap.isEmpty()) {
            final Set<Entry<SafeBet, SafeBetStats>> entrySetCopy = safeBetsStatsMap.entrySetCopy();
            for (final Entry<SafeBet, SafeBetStats> entry : entrySetCopy) {
                final SafeBet key = entry.getKey();
                final SafeBetStats value = entry.getValue();
                if (value != null) {
                    final String printedString = MessageFormatter.arrayFormat("blackRList {} {} {} {}", new Object[]{value.printStats(), key.printStats(), Generic.objectToString(key), Generic.objectToString(value)}).getMessage();
                    logger.warn(printedString);
                    Statics.safeBetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");
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

    public static boolean removeSafeRunner(final String marketId, final SafeRunner safeRunner) {
        final boolean modified;
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
                            final String printedString = MessageFormatter.arrayFormat("blackRList {} {} {} {}", new Object[]{value.printStats(), key.printStats(), Generic.objectToString(key), Generic.objectToString(value)}).getMessage();
                            logger.warn(printedString);
                            Statics.safeBetsSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");
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

    @SuppressWarnings("UnusedReturnValue")
    private static SynchronizedSafeSet<SafeRunner> removeSafeMarket(final String marketId) {
        final SynchronizedSafeSet<SafeRunner> safeRunners = Statics.safeMarketsMap.remove(marketId);
        if (safeRunners != null) {
            safeRunners.clear();
        } else { // with safeBetModule disabled, there are no safeRunners anymore
            if (Statics.safeBetModuleActivated) {
                logger.warn("null safeRunners in removeSafeMarket for: {}", marketId);
            } else {
//                Generic.alreadyPrintedMap.logOnce(Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.INFO, "null safeRunners in removeSafeMarket for: {}", marketId);
//                Generic.alreadyPrintedMap.logOnce(logger, LogLevel.INFO, "null safeRunners in removeSafeMarket"); // I don't think I event need to print this, as it is normal behavior
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
                if (Statics.needSessionToken.get()) {
                    Betty.authenticate(Statics.AUTH_URL, Statics.bu, Statics.bp, Statics.sessionTokenObject, Statics.KEY_STORE_FILE_NAME, Statics.KEY_STORE_PASSWORD, Statics.KEY_STORE_TYPE, Statics.appKey);
                }
                long timeToSleep = Statics.needSessionToken.get() ? 10_000L : 5L * Generic.MINUTE_LENGTH_MILLISECONDS; // initialized with maximum sleep time

                if (Statics.mustWriteObjects.get()) {
                    VarsIO.writeObjectsToFiles();
                    VarsIO.writeSettings();
                    Statics.mustWriteObjects.set(false);
                }
                if (Statics.rulesManagerThread.rulesManager.rulesHaveChanged.get()) {
                    Generic.threadSleepSegmented(500L, 50L, Statics.mustStop); // small sleep against multiple quick modifications that lead to writes
                    VarsIO.writeSettings(); // this method sets the AtomicBoolean to false
                }
                if (Statics.orderToPrint.get() != null) {
                    final String orders = Statics.orderToPrint.getAndSet(null).trim();
                    final String[] ordersArray = orders.split(" ");

                    for (final String singleOrder : ordersArray) {
                        if ("findMarketTypes".equals(singleOrder)) {
                            DebuggingMethods.findNewMarketTypes();
                        } else if ("cancelOrders".equals(singleOrder)) {
                            logger.error("cancelOrders command being executed; this is supported, but discouraged; this will cause error messages");
                            Statics.pendingOrdersThread.addCancelAllOrder(0L);
                        } else if ("weightTest".equals(singleOrder)) {
                            DebuggingMethods.weightTest();
                        } else if (singleOrder.startsWith("market:")) {
                            final String marketId = singleOrder.substring("market:".length());
                            DebuggingMethods.printMarket(marketId);
                        } else if (singleOrder.startsWith("event:")) {
                            final String eventId = singleOrder.substring("event:".length());
                            DebuggingMethods.printEvent(eventId);
                        } else if (singleOrder.startsWith("marketType:")) {
                            final String marketType = singleOrder.substring("marketType:".length());
                            DebuggingMethods.printMarketType(marketType);
                        } else {
                            logger.error("unknown order in maintenance thread: {}", singleOrder);
                        }
                    }
                }

                timeToSleep = Math.min(timeToSleep, timedSaveObjects());
                timeToSleep = Math.min(timeToSleep, timedSaveSettings());
//                timeToSleep = Math.min(timeToSleep, timedCleanMarketCache());
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
                timeToSleep = Math.min(timeToSleep, timedCheckDeadlock());

                Generic.threadSleepSegmented(timeToSleep, 100L, Statics.mustStop, Statics.mustWriteObjects, Statics.needSessionToken, Statics.orderToPrint, Statics.rulesManagerThread.rulesManager.rulesHaveChanged);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside Maintenance loop", throwable);
            }
        } // end while

        finalCleanSafeBetsMap();

        logger.info("maintenance thread ends");
        if (Statics.orderToPrint.get() != null) {
            logger.info("order not executed due to program stop: {}", Statics.orderToPrint.get());
        } else { // no error, nothing to be done, thread finishes cleanly
        }
    }
}
