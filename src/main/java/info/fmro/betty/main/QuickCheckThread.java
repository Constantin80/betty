package info.fmro.betty.main;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.ExBestOffersOverrides;
import info.fmro.betty.entities.MarketBook;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.entities.PriceProjection;
import info.fmro.betty.entities.Runner;
import info.fmro.betty.enums.MarketStatus;
import info.fmro.betty.enums.PriceData;
import info.fmro.betty.enums.RollupModel;
import info.fmro.betty.objects.AverageLogger;
import info.fmro.betty.objects.AverageLoggerInterface;
import info.fmro.betty.objects.BlackList;
import info.fmro.betty.objects.SafeRunner;
import info.fmro.betty.objects.ScraperEvent;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import info.fmro.shared.utility.SynchronizedMap;
import info.fmro.shared.utility.SynchronizedSet;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class QuickCheckThread
        extends Thread
        implements AverageLoggerInterface {
    private static final Logger logger = LoggerFactory.getLogger(QuickCheckThread.class);
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Map<Long, Integer> nThreadsMarketBook = Collections.synchronizedMap(new HashMap<>(16));
    public static final AtomicLong lastBooksFullRun = new AtomicLong();
    //    public static final AtomicInteger timedBooksCounter = new AtomicInteger();
    public static final EnumSet<PriceData> priceDataSetAll = EnumSet.of(PriceData.EX_ALL_OFFERS);
    public static final PriceProjection priceProjectionAll = new PriceProjection();
    public static final EnumSet<PriceData> priceDataSetBest = EnumSet.of(PriceData.EX_BEST_OFFERS);
    public static final PriceProjection priceProjectionBest = new PriceProjection();
    public static final ExBestOffersOverrides exBestOffersOverrides = new ExBestOffersOverrides();
    @SuppressWarnings({"PackageVisibleField", "ThisEscapedInObjectConstruction"})
    final AverageLogger averageLogger =
            new AverageLogger(this, "getMarketBooks ran {}({}) times average/max: listSize {}/{} took {}/{} ms of which {}/{} ms waiting for threads", "getMarketBooks ran {}({}) times", 3);

    static {
//        priceDataSetAll.add(PriceData.EX_ALL_OFFERS);
        priceProjectionAll.setPriceData(priceDataSetAll);

        exBestOffersOverrides.setBestPricesDepth(2);
        exBestOffersOverrides.setRollupModel(RollupModel.STAKE);
        exBestOffersOverrides.setRollupLimit(0);
//        priceDataSetBest.add(PriceData.EX_BEST_OFFERS);
        priceProjectionBest.setPriceData(priceDataSetBest);
        priceProjectionBest.setExBestOffersOverrides(exBestOffersOverrides);
    }

    @SuppressWarnings("UnusedReturnValue")
    private static Integer putNThreadsMarketBook(final int size, final int marketsPerOperation) {
        final long currentTime = System.currentTimeMillis();
        synchronized (nThreadsMarketBook) {
            //noinspection NumericCastThatLosesPrecision
            return nThreadsMarketBook.containsKey(currentTime) ? nThreadsMarketBook.put(currentTime, (int) Math.ceil((double) size / marketsPerOperation) + nThreadsMarketBook.get(currentTime)) :
                   nThreadsMarketBook.put(currentTime, (int) Math.ceil((double) size / marketsPerOperation));
        } // end synchronized
    }

    public static LinkedHashMap<Class<? extends ScraperEvent>, Long> getTimeScraperEventCheck(final String marketId) { // I decided to remove isIgnored support for this method
        final LinkedHashMap<Class<? extends ScraperEvent>, Long> timeScraperEventCheckMap = new LinkedHashMap<>(4);
        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.get(marketId);
        if (marketCatalogue != null) {
//            Event event = marketCatalogue.getEventStump();
            final Event event = Formulas.getStoredEventOfMarketCatalogue(marketCatalogue);
            if (event != null) {
                final LinkedHashMap<Class<? extends ScraperEvent>, Long> scraperEventIds = event.getScraperEventIds();
                if (scraperEventIds != null && !scraperEventIds.isEmpty()) {
                    for (final Entry<Class<? extends ScraperEvent>, Long> entry : scraperEventIds.entrySet()) {
                        final Class<? extends ScraperEvent> clazz = entry.getKey();
                        final long scraperEventId = entry.getValue();
                        final SynchronizedMap<Long, ? extends ScraperEvent> scraperEventsMap = Formulas.getScraperEventsMap(clazz);
                        @Nullable final ScraperEvent scraperEvent = scraperEventsMap != null ? scraperEventsMap.get(scraperEventId) : null;
                        if (scraperEvent != null) {
//                            if (!scraperEvent.isIgnored()) {
                            timeScraperEventCheckMap.put(clazz, scraperEvent.getTimeStamp());
//                            } else { // ignored, I guess nothing to be done
//                            }
                        } else {
                            logger.error("scraperEvent null while building safeBet for: {}", Generic.objectToString(scraperEventIds));
//                        timeScraperEventCheck = 0L;
                        }
                    } // end for
                } else { // error messages are printed on other elses
//                    timeScraperEventCheck = 0L;
                }
            } else {
                logger.error("event null while building safeBet for: {}", Generic.objectToString(marketCatalogue));
//                timeScraperEventCheck = 0L;
            }
        } else {
            logger.error("no marketCatalogue found for {} while building safeBet", marketId);
//            timeScraperEventCheck = 0L;
        }
        return timeScraperEventCheckMap;
    }

    public static int popNRunsMarketBook(final long currentTime) {
        final long cutoffTime = currentTime - Statics.N_MARKET_BOOK_THREADS_INTERVAL;
        int nRuns = 0;
        synchronized (nThreadsMarketBook) {
            final Iterator<Entry<Long, Integer>> iterator = nThreadsMarketBook.entrySet().iterator();
            while (iterator.hasNext()) {
                final Entry<Long, Integer> entry = iterator.next();
                final int nThreads = entry.getValue();
                final long runTime = entry.getKey();
                if (runTime < cutoffTime) {
                    iterator.remove();
                } else {
                    nRuns += nThreads;
                }
            } // end while
        } // end synchronized

        return nRuns;
    }

    @SuppressWarnings("NumericCastThatLosesPrecision")
    @Override
    public int getExpectedRuns() { // formula is approximation and doesn't work well when the limitation is reached
        final int expectedRuns;
        final double safeMarkets = Statics.safeMarketsMap.size();
        final int maxExpectedRuns = (int) (Math.ceil(safeMarkets / Statics.N_ALL) * Statics.DELAY_PRINT_AVERAGES / Statics.DELAY_GET_MARKET_BOOKS);
        final int maxLimitation = (int) (Statics.DELAY_PRINT_AVERAGES / 2_000L * Statics.N_MARKET_BOOK_THREADS_LIMIT);
        if (maxExpectedRuns <= maxLimitation) {
            expectedRuns = maxExpectedRuns;
        } else {
            final double safeMarketsImportant = Statics.safeMarketsImportantMap.size();

            final int nAllThreads = (int) Math.ceil(safeMarkets / Statics.N_ALL); // nThreads if N_ALL is used
            final int nBestThreads = (int) (Math.ceil((safeMarkets - safeMarketsImportant) / Statics.N_BEST) + Math.ceil(safeMarketsImportant / Statics.N_ALL)); // nThreads if N_BEST

            final int minExpectedRuns;
            // nAllThreads > nBestThreads
            minExpectedRuns = nAllThreads <= nBestThreads ? (int) (Math.ceil(safeMarkets / Statics.N_ALL) * Statics.DELAY_PRINT_AVERAGES / Statics.DELAY_GET_MARKET_BOOKS) :
                              (int) ((Math.ceil(safeMarketsImportant / Statics.N_ALL) + Math.ceil((safeMarkets - safeMarketsImportant) / Statics.N_BEST)) * Statics.DELAY_PRINT_AVERAGES / Statics.DELAY_GET_MARKET_BOOKS);

            expectedRuns = Math.max(maxLimitation, minExpectedRuns); // always at least minExpectedRuns
        }
        return expectedRuns;
    }

    @SuppressWarnings("SameParameterValue")
    private void getMarketBooks(@SuppressWarnings("TypeMayBeWeakened") final Set<String> safeMarketsSet, final long timeStamp, final int marketsPerOperation) {
        if (timeStamp > 0) {
            Statics.timeStamps.lastGetMarketBooksStamp(timeStamp);
        }

        final int size = safeMarketsSet.size();
        if (size > 0) {
            final Iterable<List<String>> marketIdsListSplits = Iterables.partition(safeMarketsSet, marketsPerOperation);
            putNThreadsMarketBook(size, marketsPerOperation);
            for (final List<String> marketIdsListSplit : marketIdsListSplits) {
                Statics.threadPoolExecutorMarketBooks.execute(new GetMarketBooksThread(this, marketIdsListSplit));
            }
        } else {
            if (Statics.debugLevel.check(2, 168)) {
                logger.info("no safeMarkets available in getMarketBooks set");
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    void getMarketBooks(final List<String> marketIdsList, final long timeStamp, final int marketsPerOperation) {
        if (timeStamp > 0) {
            Statics.timeStamps.lastGetMarketBooksStamp(timeStamp);
        }

        final int size = marketIdsList.size();
        if (size > 0) {
            final List<List<String>> marketIdsListSplits = Lists.partition(marketIdsList, marketsPerOperation);
            putNThreadsMarketBook(size, marketsPerOperation);
            for (final List<String> marketIdsListSplit : marketIdsListSplits) {
                Statics.threadPoolExecutorMarketBooks.execute(new GetMarketBooksThread(this, marketIdsListSplit));
            }
        } else {
            if (Statics.debugLevel.check(2, 156)) {
                logger.info("no safeMarkets available in getMarketBooks list");
            }
        }
    }

    @SuppressWarnings("OverlyNestedMethod")
    void singleGetMarketBooks(final List<String> marketIdsListSplit) {
        if (marketIdsListSplit != null) {
            final int listSize = marketIdsListSplit.size();
            if (listSize > 0) {
                // int mapCapacity = Generic.getCollectionCapacity(listSize);
                // Set<MarketBook> returnSet = Collections.synchronizedSet(new HashSet<MarketBook>(mapCapacity));
                final long startTime = System.currentTimeMillis();

                // RescriptOpThread<MarketBook> rescriptOpThreadMarketBook = new RescriptOpThread<>(returnSet, marketIdsListSplit, priceProjectionAll, null);
                // rescriptOpThreadMarketBook.run(); // thread runs here, not in parallel; this is intentional
                final PriceProjection localPriceProjection = listSize <= 11 ? priceProjectionAll : priceProjectionBest;
                final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
                final List<MarketBook> marketBooksList = ApiNgRescriptOperations.listMarketBook(marketIdsListSplit, localPriceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);
                final long endTime = System.currentTimeMillis();

                // logger.info("retrieved {} marketBooks from {} marketIds in {} ms", returnSet.size(), listSize, endTime - startTime);
                if (marketBooksList != null) {
//                    final HashSet<String> toRemoveSet = new HashSet<>(0);
                    @SuppressWarnings("unused") int safeMarketBooksMapModified = 0; // has no use now, but might in the future
                    final int returnListSize = marketBooksList.size();

                    if (returnListSize > 0) {
                        // AtomicDouble localUsedBalance = new AtomicDouble();
                        // Statics.safetyLimits.addLocalUsedBalance(localUsedBalance);
                        for (final MarketBook marketBook : marketBooksList) {
                            marketBook.setTimeStamp(endTime);
                            final String marketId = marketBook.getMarketId();
                            final Boolean inPlayBoolean = marketBook.getInplay();
                            final Integer betDelayInteger = marketBook.getBetDelay();

                            if (marketId != null && inPlayBoolean != null && betDelayInteger != null) {
                                final boolean inPlay = inPlayBoolean;
                                final int betDelay = betDelayInteger;
                                final MarketBook existingMarketBook;
//                                synchronized (Statics.safeMarketBooksMap) {

                                if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId, startTime)) {
                                    BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "marketBook, marketCatalogue map");

                                    MaintenanceThread.removeFromSecondaryMaps(marketId);
                                    //noinspection ContinueStatement
                                    continue; // next for element
                                } else {
                                    existingMarketBook = Statics.safeMarketBooksMap.putIfAbsent(marketId, marketBook);
                                    if (existingMarketBook == null) { // marketBook was added, no previous marketBook existed
                                        if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId, startTime)) {
                                            BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "marketBook, marketCatalogue map, after put");
                                            MaintenanceThread.removeFromSecondaryMaps(marketId);
                                            //noinspection ContinueStatement
                                            continue; // next for element
                                        } else {
//                                            existingMarketBook = null;
                                        }
                                    } else {
//                                        existingMarketBook = inMapMarketBook;
                                    }
                                }

                                final long timePreviousMarketBookCheck;
                                if (existingMarketBook != null) {
                                    timePreviousMarketBookCheck = existingMarketBook.getTimeStamp();
                                    if (existingMarketBook.update(marketBook) > 0) {
                                        safeMarketBooksMapModified++;
                                    }
                                } else {
                                    timePreviousMarketBookCheck = 0L;
                                    safeMarketBooksMapModified++;
                                }

                                // checking for safeRunners, on the returned MarketBook set, rather than on Statics.safeMarketBooksMap, for MarketBook freshness
                                final SynchronizedSet<SafeRunner> safeRunnersSet = Statics.safeMarketsMap.get(marketId);
                                if (safeRunnersSet != null && !safeRunnersSet.isEmpty()) {
                                    final MarketStatus marketStatus = marketBook.getStatus();
                                    final List<Runner> runnersList = marketBook.getRunners();
                                    if (marketStatus != null && runnersList != null) {
                                        Statics.safetyLimits.createPlaceInstructionList(runnersList, safeRunnersSet, marketBook, marketId, startTime, endTime, marketStatus, inPlay, betDelay, timePreviousMarketBookCheck);
                                    } else {
                                        logger.error("null marketStatus or runnersList in getMarketBooks for: {}", Generic.objectToString(marketBook));
                                    }
                                } else {
                                    final long timeSinceLastRemoved = startTime - Statics.safeMarketsMap.getTimeStampRemoved();
                                    final String printedString = MessageFormatter.arrayFormat("null/empty safeRunnersSet in getMarketBooks for marketId: {} timeSinceLastRemoved: {}ms", new Object[]{marketId, timeSinceLastRemoved}).getMessage();
                                    if (timeSinceLastRemoved < 1_000L) {
                                        logger.info(printedString);
                                    } else {
                                        logger.error(printedString);
                                    }
                                    // printing error message should be enough; remove support eliminated
//                                    toRemoveSet.add(marketId);
                                }
                            } else {
                                logger.error("null marketId etc in getMarketBooks for: {}", Generic.objectToString(marketBook));
                            }

                            if (marketId != null) {
                                Statics.safeBetsMap.parseNoLongerSeenSafeBets(marketId, localPriceProjection, endTime);
                            } else { // null marketId, error message printed in the main if, nothing to be done
                            }
                        } //  end for marketBook : marketBooksList
                    } else {
                        // warning is posted on returnListSize != listSize if branch, further on; this does happen when program is restarted and markets are very old
                        // logger.error("empty marketBooksList in singleGetMarketBooks: listSize: {} took: {}ms", listSize, endTime - startTime);
                    }

                    if (returnListSize != listSize) {
                        // this does happen with old, expired markedIds; it should go away with the next maintenance clean maps
                        Generic.alreadyPrintedMap.logOnce(Statics.debugLevel.check(2, 172), Generic.MINUTE_LENGTH_MILLISECONDS * 5L, logger, LogLevel.WARN,
                                                          "returnSetSize: {} different from listSize: {} in getMarketBooks", returnListSize, listSize);
                    }

                    final long currentTime = System.currentTimeMillis();
                    this.averageLogger.addRecords(returnListSize, currentTime - startTime, endTime - startTime);
                    if (Statics.debugLevel.check(3, 151)) {
                        logger.info("getMarketBooks: {} marketBooks in {} ms of which {} ms waiting for threads", returnListSize, currentTime - startTime, endTime - startTime);
                    }

                    Statics.safeMarketBooksMap.timeStamp();
                } else {
                    logger.error("null marketBooksList in singleGetMarketBooks: listSize={} runTime={}ms", listSize, endTime - startTime);
                }
            } else {
                logger.info("list empty in singleGetMarketBooks");
            }
        } else {
            logger.info("list null in singleGetMarketBooks");
        }
    }

    private long timedGetMarketBooks() {
        long timeForNext = Statics.timeStamps.getLastGetMarketBooks();
        final long currentTime = System.currentTimeMillis();
        long timeTillNext = timeForNext - currentTime;
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastGetMarketBooksStamp(Statics.DELAY_GET_MARKET_BOOKS);

            final int safeMarketsMapSize = Statics.safeMarketsMap.size();
            final int safeMarketsImportantMapSize = Statics.safeMarketsImportantMap.size();
            @SuppressWarnings("NumericCastThatLosesPrecision") final int nAllThreads = (int) Math.ceil((double) safeMarketsMapSize / Statics.N_ALL); // nThreads if N_ALL is used
            @SuppressWarnings("NumericCastThatLosesPrecision") final int nBestThreads =
                    (int) (Math.ceil((double) (safeMarketsMapSize - safeMarketsImportantMapSize) / Statics.N_BEST) + Math.ceil((double) safeMarketsImportantMapSize / Statics.N_ALL)); // nThreads if N_BEST is used

            if (nAllThreads <= nBestThreads || currentTime >= lastBooksFullRun.get() + 2_000L) {
                lastBooksFullRun.set(currentTime);
                this.getMarketBooks(Statics.safeMarketsMap.keySetCopy(), 0L, Statics.N_ALL); // delay moved after if (timeTillNext <= 0)
            } else { // nAllThreads > nBestThreads
                final int nRuns = popNRunsMarketBook(currentTime);
                if (nRuns + nAllThreads <= Statics.N_MARKET_BOOK_THREADS_LIMIT) { // limit threads / 2000ms
                    lastBooksFullRun.set(currentTime);
                    this.getMarketBooks(Statics.safeMarketsMap.keySetCopy(), 0L, Statics.N_ALL); // delay moved after if (timeTillNext <= 0)
                } else if (Statics.safeMarketsImportantMap.isEmpty()) {
                    this.getMarketBooks(Statics.safeMarketsMap.keySetCopy(), 0L, Statics.N_BEST); // delay moved after if (timeTillNext <= 0)
                } else {
                    final Set<String> importantBooksSet = Statics.safeMarketsImportantMap.keySetCopy();
                    final Set<String> nonImportantBooksSet = Statics.safeMarketsMap.keySetCopy();
                    nonImportantBooksSet.removeAll(importantBooksSet);

                    this.getMarketBooks(importantBooksSet, 0L, Statics.N_ALL); // delay set by the next line; 2 delays would be bad, as they add up; now delay moved
                    this.getMarketBooks(nonImportantBooksSet, 0L, Statics.N_BEST); // delay moved after if (timeTillNext <= 0)
                } // end else
//            } else {
//                logger.error("STRANGE nAllThreads < nBestThreads in timedGetMarketBooks: {} {}", nAllThreads, nBestThreads);
            } // end else

            //noinspection ReuseOfLocalVariable
            timeForNext = Statics.timeStamps.getLastGetMarketBooks();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "quickCheck thread");
                }
                GetLiveMarketsThread.waitForSessionToken("QuickCheckThread main");

                final long timeToSleep;

                timeToSleep = timedGetMarketBooks();

                Generic.threadSleepSegmented(timeToSleep, 100L, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside QuickCheck loop", throwable);
            }
        }

        logger.info("quickCheck thread ends");
    }
}
