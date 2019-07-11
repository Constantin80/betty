package info.fmro.betty.main;

import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.enums.MatchStatus;
import info.fmro.betty.enums.ParsedMarketType;
import info.fmro.betty.enums.ParsedRunnerType;
import info.fmro.betty.enums.ScrapedField;
import info.fmro.betty.enums.Side;
import info.fmro.betty.objects.BlackList;
import info.fmro.betty.objects.ParsedMarket;
import info.fmro.betty.objects.ParsedRunner;
import info.fmro.betty.objects.SafeRunner;
import info.fmro.betty.objects.ScraperEvent;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import info.fmro.shared.utility.SynchronizedMap;
import info.fmro.shared.utility.SynchronizedSafeSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"OverlyComplexClass", "UtilityClass"})
final class FindSafeRunners {
    private static final Logger logger = LoggerFactory.getLogger(FindSafeRunners.class);
    public static final Side LAY = Side.LAY, BACK = Side.BACK;
    private static final ScrapedField[] statusScoresHts = {ScrapedField.MATCH_STATUS, ScrapedField.HOME_SCORE, ScrapedField.AWAY_SCORE, ScrapedField.HOME_HT_SCORE, ScrapedField.AWAY_HT_SCORE},
            statusScores = {ScrapedField.MATCH_STATUS, ScrapedField.HOME_SCORE, ScrapedField.AWAY_SCORE}, statusHts = {ScrapedField.MATCH_STATUS, ScrapedField.HOME_HT_SCORE, ScrapedField.AWAY_HT_SCORE},
            statusHomeScore = {ScrapedField.MATCH_STATUS, ScrapedField.HOME_SCORE}, statusAwayScore = {ScrapedField.MATCH_STATUS, ScrapedField.AWAY_SCORE};
    public static final AtomicLong lastRegularFindSafeRunnersRunStamp = new AtomicLong();

    @Contract(pure = true)
    private FindSafeRunners() {
    }

    private static void safeRunnersSetSizeCheck(@NotNull final SynchronizedSafeSet<SafeRunner> safeRunnersSet, final int nSafeRunners, final MarketCatalogue marketCatalogue, final String marketId, final ParsedMarketType parsedMarketType,
                                                final String eventName, final Collection<? super String> modifiedMarketsList, final long startTime, final HashSet<ParsedRunner> usedParsedRunnersSet, final String scraperData) {
        if (safeRunnersSet.size() == nSafeRunners) {
            if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId, startTime)) {
                BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "marketCatalogue in safeRunnersSetSizeCheck");
                MaintenanceThread.removeFromSecondaryMaps(marketId);
            } else if (Statics.marketsUnderTesting.contains(parsedMarketType)) {
                Generic.alreadyPrintedMap.logOnce(logger, LogLevel.ERROR, "marketsUnderTesting safeRunnersSet not added, size: {} in findSafeRunners for: {} {} {} {}", safeRunnersSet.size(), scraperData, Generic.objectToString(usedParsedRunnersSet),
                                                  Generic.objectToString(safeRunnersSet), Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
            } else {
                final SynchronizedSafeSet<SafeRunner> inMapSafeRunnersSet = Statics.safeMarketsMap.putIfAbsent(marketId, safeRunnersSet);
                if (inMapSafeRunnersSet == null) { // safeRunnersSet was added, no previous safeRunnersSet existed
                    if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId, startTime)) {
                        BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "marketCatalogue in safeRunnersSetSizeCheck after add");
                        MaintenanceThread.removeFromSecondaryMaps(marketId);
                    } else {
                        if (!modifiedMarketsList.contains(marketId)) {
                            modifiedMarketsList.add(marketId);
                        }
                        Generic.alreadyPrintedMap
                                .logOnce(logger, LogLevel.INFO, "Safe market {} {} of event ({}) added with {} runners: {} {}", marketId, parsedMarketType.name(), eventName, nSafeRunners, scraperData, Generic.objectToString(usedParsedRunnersSet));
//                                Generic.objectToString(safeRunnersSet));
                    }
                } else {
                    if (updateSafeRunnersSet(inMapSafeRunnersSet, safeRunnersSet) > 0) {
//                        Statics.safeMarketsMap.put(marketId, safeRunnersSet);
                        if (!modifiedMarketsList.contains(marketId)) {
                            modifiedMarketsList.add(marketId);
                        }
                    } else { // no modification made, nothing to be done
                    }
                }
            }
        } else {
            Generic.alreadyPrintedMap.logOnce(Statics.newMarketSynchronizedWriter, logger, LogLevel.ERROR, "STRANGE safeRunnersSet size: {}({}) in findSafeRunners for: {} {} {} {}", safeRunnersSet.size(), nSafeRunners, scraperData,
                                              Generic.objectToString(usedParsedRunnersSet), Generic.objectToString(safeRunnersSet, "Stamp"), Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
        }
    }

    private static int updateSafeRunnersSet(final SynchronizedSafeSet<SafeRunner> existingSafeRunnersSet, final SynchronizedSafeSet<SafeRunner> newSafeRunnersSet) {
        int modified;
        if (existingSafeRunnersSet == null || newSafeRunnersSet == null) {
            logger.error("existingSafeRunnersSet or newSafeRunnersSet null in updateSafeRunnersSet: {} {}", Generic.objectToString(existingSafeRunnersSet), Generic.objectToString(newSafeRunnersSet));
            modified = 0;
        } else {
            modified = 0; // initialized
            final HashSet<SafeRunner> newSafeRunnersSetCopy = newSafeRunnersSet.copy();
            for (final SafeRunner newSafeRunner : newSafeRunnersSetCopy) {
                if (existingSafeRunnersSet.add(newSafeRunner)) {
                    newSafeRunner.checkScrapers();
                    modified++;
                } else {
                    final SafeRunner existingSafeRunner = existingSafeRunnersSet.getEqualElement(newSafeRunner);
                    if (existingSafeRunner == null) {
                        logger.error("null existingSafeRunner in updateSafeRunnersSet: {} {} {}", Generic.objectToString(newSafeRunner), Generic.objectToString(existingSafeRunnersSet), Generic.objectToString(newSafeRunnersSet));
                    } else {
                        modified += existingSafeRunner.updateUsedScrapers(newSafeRunner);
                    }
                }
//                if (existingSafeRunnersSet.contains(newSafeRunner)) {
//                    final SafeRunner existingSafeRunner = existingSafeRunnersSet.getEqualElement(newSafeRunner);
//                    if (existingSafeRunner == null) {
//                        logger.error("null existingSafeRunner in updateSafeRunnersSet: {} {} {}", Generic.objectToString(newSafeRunner),
//                                Generic.objectToString(existingSafeRunnersSet), Generic.objectToString(newSafeRunnersSet));
//                    } else {
//                        modified += existingSafeRunner.updateUsedScrapers(newSafeRunner);
//                    }
//                } else {
//                    existingSafeRunnersSet.add(newSafeRunner);
//                    modified++;
//                }
            } // end for
        }

        return modified;
    }

    @NotNull
    private static SynchronizedSafeSet<SafeRunner> createSafeRunnersSet(final int nSafeRunners) {
        final int capacity = Generic.getCollectionCapacity(nSafeRunners);
        return new SynchronizedSafeSet<>(capacity);
    }

    private static void addSafeRunner(final SynchronizedSafeSet<? super SafeRunner> safeRunnersSet, final SafeRunner safeRunner, final Collection<? super ParsedRunner> usedParsedRunnersSet, final ParsedRunner parsedRunner) {
        if (safeRunner != null) {
            safeRunnersSet.add(safeRunner);
            usedParsedRunnersSet.add(parsedRunner);
        } else { // runner not safe
        }
    }

    private static void defaultParsedRunnerTypeError(final ParsedRunnerType parsedRunnerType, final MarketCatalogue marketCatalogue) {
        Generic.alreadyPrintedMap.logOnce(Statics.newMarketSynchronizedWriter, logger, LogLevel.ERROR, "STRANGE unknown parsedRunnerType: {} in findSafeRunners for: {}",
                                          parsedRunnerType, Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
    }

    @Contract(pure = true)
    private static boolean during90Minutes(final MatchStatus matchStatus) {
        return matchStatus == MatchStatus.FIRST_HALF || matchStatus == MatchStatus.HALF_TIME || matchStatus == MatchStatus.SECOND_HALF || matchStatus == MatchStatus.AWAITING_ET;
    }

    @Contract(pure = true)
    private static boolean during90MinutesWithoutFirstHalf(final MatchStatus matchStatus) {
        return matchStatus == MatchStatus.HALF_TIME || matchStatus == MatchStatus.SECOND_HALF || matchStatus == MatchStatus.AWAITING_ET;
    }

    @Contract(pure = true)
    private static boolean duringFirstHalf(final MatchStatus matchStatus) {
        return matchStatus == MatchStatus.FIRST_HALF || matchStatus == MatchStatus.HALF_TIME;
    }

    @Contract(pure = true)
    private static boolean duringSecondHalf(final MatchStatus matchStatus) {
        return matchStatus == MatchStatus.SECOND_HALF || matchStatus == MatchStatus.AWAITING_ET;
    }

    private static SynchronizedMap<Class<? extends ScraperEvent>, Long> putUsedScrapersSet(final Map<? super ScrapedField, ? super SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedScrapersMap, final ScrapedField scrapedField) {
        return putUsedScrapersMap(usedScrapersMap, scrapedField, 4);
    }

    private static SynchronizedMap<Class<? extends ScraperEvent>, Long> putUsedScrapersMap(final @NotNull Map<? super ScrapedField, ? super SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedScrapersMap, final ScrapedField scrapedField,
                                                                                           final int initialSetSize) {
        final SynchronizedMap<Class<? extends ScraperEvent>, Long> map = new SynchronizedMap<>(initialSetSize);
        usedScrapersMap.put(scrapedField, map);
        return map;
    }

    public static void findSafeRunners() {
        findSafeRunners(null, null);
    }

    public static void findSafeRunners(final Collection<Event> eventsSet) {
        findSafeRunners(eventsSet, null);
    }

    public static void findSafeRunners(final Set<Entry<String, MarketCatalogue>> entrySet) {
        findSafeRunners(null, entrySet);
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod", "NestedSwitchStatement", "SwitchStatementDensity", "ConstantConditions"})
    private static void findSafeRunners(final Collection<Event> eventsSet, final Set<Entry<String, MarketCatalogue>> entrySet) {
        if (System.currentTimeMillis() - Statics.scraperEventMaps.getNTimeStamp() <= Generic.HOUR_LENGTH_MILLISECONDS * 3L) {
            final long startTime = System.currentTimeMillis();
            final boolean fullRun = eventsSet == null && entrySet == null;
            if (fullRun) { // fullRun, not updating the stamp
            } else {
                lastRegularFindSafeRunnersRunStamp.getAndSet(startTime);
            }

            final Set<Entry<String, MarketCatalogue>> entrySetCopy = entrySet == null ? Statics.marketCataloguesMap.entrySetCopy() : entrySet;
            final List<String> modifiedMarketsList = new ArrayList<>(0);
            for (final Entry<String, MarketCatalogue> entry : entrySetCopy) {
                final String marketId = entry.getKey();
                final MarketCatalogue marketCatalogue = entry.getValue();
                if (marketId != null && marketCatalogue != null && !marketCatalogue.isIgnored()) {
                    final ParsedMarket parsedMarket = marketCatalogue.getParsedMarket();
                    final Event event = Formulas.getStoredEventOfMarketCatalogue(marketCatalogue);
                    if (parsedMarket != null && event != null && !event.isIgnored(startTime)) {
                        if (eventsSet == null || eventsSet.contains(event)) {
                            final LinkedHashMap<Class<? extends ScraperEvent>, Long> scraperEventIds = event.getScraperEventIds();
                            final HashSet<ParsedRunner> parsedRunnersSet = parsedMarket.getParsedRunnersSet();
                            final HashSet<ParsedRunner> usedParsedRunnersSet = new HashSet<>(0);
                            final ParsedMarketType parsedMarketType = parsedMarket.getParsedMarketType();
                            final String eventName = event.getName();
                            if (parsedRunnersSet != null && parsedMarketType != null && eventName != null && scraperEventIds != null) {
                                final int nMatches = event.getNValidScraperEventIds();
                                final List<Integer> homeScores = new ArrayList<>(nMatches), awayScores = new ArrayList<>(nMatches), homeHtScores = new ArrayList<>(nMatches), awayHtScores = new ArrayList<>(nMatches);
                                final List<MatchStatus> matchStatuses = new ArrayList<>(nMatches);
                                final Set<Entry<Class<? extends ScraperEvent>, Long>> scraperEventIdsEntrySet = scraperEventIds.entrySet();
                                for (final Entry<Class<? extends ScraperEvent>, Long> entryMatched : scraperEventIdsEntrySet) {
                                    final Class<? extends ScraperEvent> clazz = entryMatched.getKey();
                                    final long scraperEventId = entryMatched.getValue();
                                    final SynchronizedMap<Long, ? extends ScraperEvent> map = Formulas.getScraperEventsMap(clazz);
                                    final ScraperEvent scraperEvent = map.get(scraperEventId);
                                    if (scraperEvent != null) {
                                        if (scraperEvent.isIgnored()) { // ignored, I guess nothing to be done
                                        } else {
                                            final long scraperErrors = scraperEvent.errors();
                                            final boolean startedScraperEvent = scraperEvent.hasStarted();
                                            if (scraperErrors <= 0L && startedScraperEvent) {
                                                final MatchStatus matchStatus = scraperEvent.getMatchStatus();
                                                matchStatuses.add(matchStatus);
                                                final int homeScore = scraperEvent.getHomeScore();
                                                homeScores.add(homeScore);
                                                final int awayScore = scraperEvent.getAwayScore();
                                                awayScores.add(awayScore);
                                                final int homeHtScore = scraperEvent.getHomeHtScore();
                                                homeHtScores.add(homeHtScore);
                                                final int awayHtScore = scraperEvent.getAwayHtScore();
                                                awayHtScores.add(awayHtScore);
                                            } else {
                                                logger.error("errors or not started for: {} {} {}", clazz.getSimpleName(), scraperEventId, Generic.objectToString(scraperEvent));
                                            }
                                        }
                                    } else {
                                        final long timeSinceLastRemoved = startTime - map.getTimeStampRemoved();
                                        final String printedString =
                                                MessageFormatter.arrayFormat("null {} in map in findSafeRunners timeSinceLastRemoved: {} for scraperEvent: {}", new Object[]{clazz.getName(), timeSinceLastRemoved, scraperEventId}).getMessage();
                                        if (timeSinceLastRemoved < 1_000L) {
                                            logger.info("null {} in map in findSafeRunners timeSinceLastRemoved {} for scraperEventId {}", clazz.getSimpleName(), timeSinceLastRemoved, scraperEventId);
                                        } else {
                                            logger.error(printedString);
                                        }
                                    } // end else
                                } // end for
                                final int homeScore = Formulas.getMaxMultiple(homeScores, -1);
                                final int awayScore = Formulas.getMaxMultiple(awayScores, -1);
                                final int homeHtScore = Formulas.getMaxMultiple(homeHtScores, -1);
                                final int awayHtScore = Formulas.getMaxMultiple(awayHtScores, -1);
                                final MatchStatus matchStatus = Formulas.getMaxMultiple(matchStatuses, null, Statics.nullComparator);
                                final String scraperData = matchStatus + " " + homeScore + "-" + awayScore + "(" + homeHtScore + "-" + awayHtScore + ")";

                                if (homeScore >= 0 && awayScore >= 0 && (homeHtScore >= 0) == (awayHtScore >= 0)) {
                                    final EnumMap<ScrapedField, SynchronizedMap<Class<? extends ScraperEvent>, Long>> usedScrapersMap = new EnumMap<>(ScrapedField.class);
                                    final SynchronizedMap<Class<? extends ScraperEvent>, Long> homeScoreScrapers = putUsedScrapersSet(usedScrapersMap, ScrapedField.HOME_SCORE),
                                            awayScoreScrapers = putUsedScrapersSet(usedScrapersMap, ScrapedField.AWAY_SCORE), homeHtScoreScrapers = putUsedScrapersSet(usedScrapersMap, ScrapedField.HOME_HT_SCORE),
                                            awayHtScoreScrapers = putUsedScrapersSet(usedScrapersMap, ScrapedField.AWAY_HT_SCORE), matchStatusScrapers = putUsedScrapersSet(usedScrapersMap, ScrapedField.MATCH_STATUS);
                                    for (final Entry<Class<? extends ScraperEvent>, Long> entryMatched : scraperEventIdsEntrySet) {
                                        final Class<? extends ScraperEvent> clazz = entryMatched.getKey();
                                        final long scraperEventId = entryMatched.getValue();
                                        final SynchronizedMap<Long, ? extends ScraperEvent> map = Formulas.getScraperEventsMap(clazz);
                                        final ScraperEvent scraperEvent = map.get(scraperEventId);
                                        if (scraperEvent != null) {
                                            if (scraperEvent.isIgnored()) { // ignored, I guess nothing to be done
                                            } else {
                                                final long scraperErrors = scraperEvent.errors();
                                                final boolean startedScraperEvent = scraperEvent.hasStarted();
                                                if (scraperErrors <= 0L && startedScraperEvent) {
                                                    final MatchStatus scraperMatchStatus = scraperEvent.getMatchStatus();
                                                    if (matchStatus == scraperMatchStatus) {
                                                        matchStatusScrapers.put(clazz, scraperEventId);
                                                    }
                                                    final int scraperHomeScore = scraperEvent.getHomeScore();
                                                    if (homeScore == scraperHomeScore) {
                                                        homeScoreScrapers.put(clazz, scraperEventId);
                                                    }
                                                    final int scraperAwayScore = scraperEvent.getAwayScore();
                                                    if (awayScore == scraperAwayScore) {
                                                        awayScoreScrapers.put(clazz, scraperEventId);
                                                    }
                                                    final int scraperHomeHtScore = scraperEvent.getHomeHtScore();
                                                    if (homeHtScore == scraperHomeHtScore) {
                                                        homeHtScoreScrapers.put(clazz, scraperEventId);
                                                    }
                                                    final int scraperAwayHtScore = scraperEvent.getAwayHtScore();
                                                    if (awayHtScore == scraperAwayHtScore) {
                                                        awayHtScoreScrapers.put(clazz, scraperEventId);
                                                    }
                                                } else {
                                                    logger.error("b errors or not started for: {} {} {}", clazz.getSimpleName(), scraperEventId, Generic.objectToString(scraperEvent));
                                                }
                                            }
                                        } else {
                                            final long timeSinceLastRemoved = startTime - map.getTimeStampRemoved();
                                            final String printedString =
                                                    MessageFormatter.arrayFormat("b null {} in map in findSafeRunners timeSinceLastRemoved: {} for scraperEvent: {}", new Object[]{clazz.getName(), timeSinceLastRemoved, scraperEventId}).getMessage();
                                            if (timeSinceLastRemoved < 1_000L) {
                                                logger.info("b null {} in map in findSafeRunners timeSinceLastRemoved {} for scraperEventId {}", clazz.getSimpleName(), timeSinceLastRemoved, scraperEventId);
                                            } else {
                                                logger.error(printedString);
                                            }
                                        } // end else
                                    } // end for
                                    //noinspection SwitchStatementDensity
                                    switch (parsedMarketType) {
                                        case SH_CORRECT_SCORE:
                                            if (duringSecondHalf(matchStatus) && homeHtScore >= 0 && awayHtScore >= 0 && (homeScore - homeHtScore > 0 || awayScore - awayHtScore > 0)) {
                                                @SuppressWarnings("OverlyComplexArithmeticExpression") final int nSafeRunners =
                                                        homeScore - homeHtScore > 2 || awayScore - awayHtScore > 2 ? 10 : 3 * (homeScore - homeHtScore + awayScore - awayHtScore) - (homeScore - homeHtScore) * (awayScore - awayHtScore);
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case SCORE_0_0:
                                                            if (homeScore - homeHtScore > 0 || awayScore - awayHtScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case SCORE_0_1:
                                                            if (homeScore - homeHtScore > 0 || awayScore - awayHtScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case SCORE_0_2:
                                                            if (homeScore - homeHtScore > 0 || awayScore - awayHtScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case SCORE_1_0:
                                                            if (homeScore - homeHtScore > 1 || awayScore - awayHtScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case SCORE_1_1:
                                                            if (homeScore - homeHtScore > 1 || awayScore - awayHtScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case SCORE_1_2:
                                                            if (homeScore - homeHtScore > 1 || awayScore - awayHtScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case SCORE_2_0:
                                                            if (homeScore - homeHtScore > 2 || awayScore - awayHtScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case SCORE_2_1:
                                                            if (homeScore - homeHtScore > 2 || awayScore - awayHtScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case SCORE_2_2:
                                                            if (homeScore - homeHtScore > 2 || awayScore - awayHtScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case ANY_UNQUOTED:
                                                            if (homeScore - homeHtScore > 2 || awayScore - awayHtScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case LAST_TEAM_TO_SCORE:
                                            if (during90Minutes(matchStatus) && (homeScore > 0 || awayScore > 0)) {
                                                final int nSafeRunners = 1;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case HOME_SCORES:
                                                        case AWAY_SCORES:
                                                            // never safe
                                                            break;
                                                        case NO_GOAL:
                                                            if (homeScore > 0 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case MATCH_ODDS_AND_BTTS:
                                            if (during90Minutes(matchStatus) && homeScore > 0 && awayScore > 0) {
                                                final int nSafeRunners = 3;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case HOME_AND_YES:
                                                        case AWAY_AND_YES:
                                                        case DRAW_AND_YES:
                                                            // never safe
                                                            break;
                                                        case HOME_AND_NO:
                                                        case AWAY_AND_NO:
                                                        case DRAW_AND_NO:
                                                            if (homeScore > 0 && awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case MATCH_ODDS_AND_OU_25:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore >= 3) {
                                                final int nSafeRunners = 3;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case HOME_AND_OVER:
                                                        case AWAY_AND_OVER:
                                                        case DRAW_AND_OVER:
                                                            // never safe
                                                            break;
                                                        case HOME_AND_UNDER:
                                                        case AWAY_AND_UNDER:
                                                        case DRAW_AND_UNDER:
                                                            if (homeScore + awayScore >= 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case MATCH_ODDS_AND_OU_35:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore >= 4) {
                                                final int nSafeRunners = 3;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case HOME_AND_OVER:
                                                        case AWAY_AND_OVER:
                                                        case DRAW_AND_OVER:
                                                            // never safe
                                                            break;
                                                        case HOME_AND_UNDER:
                                                        case AWAY_AND_UNDER:
                                                        case DRAW_AND_UNDER:
                                                            if (homeScore + awayScore >= 4) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else { // market not safe
                                            }
                                            break;
                                        case HALF_TIME_FULL_TIME:
                                            if (during90MinutesWithoutFirstHalf(matchStatus) && homeHtScore >= 0 && awayHtScore >= 0) {
                                                final int nSafeRunners = 6;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case HOME_AND_HOME:
                                                        case HOME_AND_AWAY:
                                                        case HOME_AND_DRAW:
                                                            if (homeHtScore <= awayHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        case DRAW_AND_HOME:
                                                        case DRAW_AND_AWAY:
                                                        case DRAW_AND_DRAW:
                                                            if (homeHtScore != awayHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        case AWAY_AND_HOME:
                                                        case AWAY_AND_DRAW:
                                                        case AWAY_AND_AWAY:
                                                            if (homeHtScore >= awayHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case CORRECT_SCORE:
                                            if (during90Minutes(matchStatus) && (homeScore > 0 || awayScore > 0)) {
//                                            final int nSafeRunners = homeScore > 3 || awayScore > 3 ? 17 : 4 * (homeScore + awayScore) - homeScore * awayScore;
                                                final int nSafeRunners = homeScore > 3 || awayScore > 3 ? 16 : 4 * (homeScore + awayScore) - homeScore * awayScore;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case SCORE_0_0:
                                                            if (homeScore > 0 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_1:
                                                            if (homeScore > 0 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_2:
                                                            if (homeScore > 0 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_3:
                                                            if (homeScore > 0 || awayScore > 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_0:
                                                            if (homeScore > 1 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_1:
                                                            if (homeScore > 1 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_2:
                                                            if (homeScore > 1 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_3:
                                                            if (homeScore > 1 || awayScore > 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_0:
                                                            if (homeScore > 2 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_1:
                                                            if (homeScore > 2 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_2:
                                                            if (homeScore > 2 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_3:
                                                            if (homeScore > 2 || awayScore > 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_3_0:
                                                            if (homeScore > 3 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_3_1:
                                                            if (homeScore > 3 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_3_2:
                                                            if (homeScore > 3 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_3_3:
                                                            if (homeScore > 3 || awayScore > 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
//                                                    case ANY_UNQUOTED:
//                                                        if (homeScore > 3 || awayScore > 3) {
//                                                            safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
//                                                        }
//                                                        break;
                                                        case ANY_OTHER_HOME_WIN:
                                                            // never safe
                                                            break;
                                                        case ANY_OTHER_AWAY_WIN:
                                                        case ANY_OTHER_DRAW:
                                                            // never safe
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case ANYTIME_SCORE:
                                            if (during90Minutes(matchStatus) && (homeScore > 0 || awayScore > 0)) {
                                                final int additionalBack = homeScore == 0 || awayScore == 0 ? Math.abs(Math.min(homeScore, 3) - Math.min(awayScore, 3)) - 1 : 0;
                                                final int additionalLay;
                                                if (additionalBack != 0) {
                                                    additionalLay = 0;
                                                } else if (homeScore >= 4 || awayScore >= 4) {
                                                    additionalLay = -1;
                                                } else {
                                                    additionalLay = 0;
                                                }
                                                final int nSafeRunners = 1 + additionalBack + additionalLay + Math.min(homeScore, 4) * (3 - Math.min(awayScore, 3)) + Math.min(awayScore, 4) * (3 - Math.min(homeScore, 3));
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case SCORE_0_1:
                                                            if (homeScore == 0 && awayScore >= 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if (homeScore > 0 && awayScore < 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_2:
                                                            if (homeScore == 0 && awayScore >= 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if (homeScore > 0 && awayScore < 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_3:
                                                            if (homeScore == 0 && awayScore >= 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if (homeScore > 0 && awayScore < 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_0:
                                                            if (homeScore >= 1 && awayScore == 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if (homeScore < 1 && awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_1:
                                                            if (homeScore == 1 && awayScore == 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if ((homeScore < 1 && awayScore > 1) || (homeScore > 1 && awayScore < 1)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_2:
                                                            if (homeScore == 1 && awayScore == 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if ((homeScore < 1 && awayScore > 2) || (homeScore > 1 && awayScore < 2)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_3:
                                                            if (homeScore == 1 && awayScore == 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if ((homeScore < 1 && awayScore > 3) || (homeScore > 1 && awayScore < 3)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_0:
                                                            if (homeScore >= 2 && awayScore == 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if (homeScore < 2 && awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_1:
                                                            if (homeScore == 2 && awayScore == 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if ((homeScore < 2 && awayScore > 1) || (homeScore > 2 && awayScore < 1)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_2:
                                                            if (homeScore == 2 && awayScore == 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if ((homeScore < 2 && awayScore > 2) || (homeScore > 2 && awayScore < 2)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_3:
                                                            if (homeScore == 2 && awayScore == 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if ((homeScore < 2 && awayScore > 3) || (homeScore > 2 && awayScore < 3)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_3_0:
                                                            if (homeScore >= 3 && awayScore == 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if (homeScore < 3 && awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_3_1:
                                                            if (homeScore == 3 && awayScore == 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if ((homeScore < 3 && awayScore > 1) || (homeScore > 3 && awayScore < 1)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_3_2:
                                                            if (homeScore == 3 && awayScore == 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if ((homeScore < 3 && awayScore > 2) || (homeScore > 3 && awayScore < 2)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_3_3:
                                                            if (homeScore == 3 && awayScore == 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            if ((homeScore < 3 && awayScore > 3) || (homeScore > 3 && awayScore < 3)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case HALF_TIME_SCORE:
                                            if (duringFirstHalf(matchStatus) && (homeScore > 0 || awayScore > 0)) {
                                                final int nSafeRunners = homeScore > 2 || awayScore > 2 ? 10 : 3 * (homeScore + awayScore) - homeScore * awayScore;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case SCORE_0_0:
                                                            if (homeScore > 0 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_1:
                                                            if (homeScore > 0 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_2:
                                                            if (homeScore > 0 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_0:
                                                            if (homeScore > 1 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_1:
                                                            if (homeScore > 1 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_2:
                                                            if (homeScore > 1 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_0:
                                                            if (homeScore > 2 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_1:
                                                            if (homeScore > 2 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_2:
                                                            if (homeScore > 2 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case ANY_UNQUOTED:
                                                            if (homeScore > 2 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case TOTAL_GOALS:
                                            if (during90Minutes(matchStatus) && (homeScore > 0 || awayScore > 0)) {
                                                final int nSafeRunners = Math.min(homeScore + awayScore, 7);
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_1_OR_MORE:
                                                            if (homeScore + awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_2_OR_MORE:
                                                            if (homeScore + awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_3_OR_MORE:
                                                            if (homeScore + awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_4_OR_MORE:
                                                            if (homeScore + awayScore > 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_5_OR_MORE:
                                                            if (homeScore + awayScore > 4) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_6_OR_MORE:
                                                            if (homeScore + awayScore > 5) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_7_OR_MORE:
                                                            if (homeScore + awayScore > 6) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case EXACT_GOALS:
                                            if (during90Minutes(matchStatus) && (homeScore > 0 || awayScore > 0)) {
                                                final int nSafeRunners = homeScore + awayScore >= 7 ? 8 : homeScore + awayScore;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_0:
                                                            if (homeScore + awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_1:
                                                            if (homeScore + awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_2:
                                                            if (homeScore + awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_3:
                                                            if (homeScore + awayScore > 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_4:
                                                            if (homeScore + awayScore > 4) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_5:
                                                            if (homeScore + awayScore > 5) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_6:
                                                            if (homeScore + awayScore > 6) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_7_PLUS:
                                                            if (homeScore + awayScore > 6) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case TEAM_TOTAL_GOALS_A:
                                            if (during90Minutes(matchStatus) && homeScore > 0) {
                                                final int nSafeRunners = Math.min(homeScore, 7);
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_1_OR_MORE:
                                                            if (homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHomeScore);
                                                            }
                                                            break;
                                                        case GOALS_2_OR_MORE:
                                                            if (homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHomeScore);
                                                            }
                                                            break;
                                                        case GOALS_3_OR_MORE:
                                                            if (homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHomeScore);
                                                            }
                                                            break;
                                                        case GOALS_4_OR_MORE:
                                                            if (homeScore > 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHomeScore);
                                                            }
                                                            break;
                                                        case GOALS_5_OR_MORE:
                                                            if (homeScore > 4) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHomeScore);
                                                            }
                                                            break;
                                                        case GOALS_6_OR_MORE:
                                                            if (homeScore > 5) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHomeScore);
                                                            }
                                                            break;
                                                        case GOALS_7_OR_MORE:
                                                            if (homeScore > 6) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHomeScore);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case TEAM_TOTAL_GOALS_B:
                                            if (during90Minutes(matchStatus) && awayScore > 0) {
                                                final int nSafeRunners = Math.min(awayScore, 7);
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_1_OR_MORE:
                                                            if (awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusAwayScore);
                                                            }
                                                            break;
                                                        case GOALS_2_OR_MORE:
                                                            if (awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusAwayScore);
                                                            }
                                                            break;
                                                        case GOALS_3_OR_MORE:
                                                            if (awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusAwayScore);
                                                            }
                                                            break;
                                                        case GOALS_4_OR_MORE:
                                                            if (awayScore > 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusAwayScore);
                                                            }
                                                            break;
                                                        case GOALS_5_OR_MORE:
                                                            if (awayScore > 4) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusAwayScore);
                                                            }
                                                            break;
                                                        case GOALS_6_OR_MORE:
                                                            if (awayScore > 5) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusAwayScore);
                                                            }
                                                            break;
                                                        case GOALS_7_OR_MORE:
                                                            if (awayScore > 6) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusAwayScore);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case CORRECT_SCORE2_A:
                                            if (during90Minutes(matchStatus) && (homeScore > 4 || awayScore > 0)) {
                                                final int nSafeRunners = homeScore > 7 || awayScore > 2 ? 13 : 3 * Math.max(homeScore - 4, 0) + 4 * awayScore - Math.max(homeScore - 4, 0) * awayScore;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case SCORE_4_0:
                                                            if (homeScore > 4 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_4_1:
                                                            if (homeScore > 4 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_4_2:
                                                            if (homeScore > 4 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_5_0:
                                                            if (homeScore > 5 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_5_1:
                                                            if (homeScore > 5 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_5_2:
                                                            if (homeScore > 5 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_6_0:
                                                            if (homeScore > 6 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_6_1:
                                                            if (homeScore > 6 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_6_2:
                                                            if (homeScore > 6 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_7_0:
                                                            if (homeScore > 7 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_7_1:
                                                            if (homeScore > 7 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_7_2:
                                                            if (homeScore > 7 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case ANY_UNQUOTED:
                                                            if (homeScore > 7 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case CORRECT_SCORE2_B:
                                            if (during90Minutes(matchStatus) && (awayScore > 4 || homeScore > 0)) {
                                                final int nSafeRunners = awayScore > 7 || homeScore > 2 ? 13 : 3 * Math.max(awayScore - 4, 0) + 4 * homeScore - Math.max(awayScore - 4, 0) * homeScore;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case SCORE_0_4:
                                                            if (awayScore > 4 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_4:
                                                            if (awayScore > 4 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_4:
                                                            if (awayScore > 4 || homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_5:
                                                            if (awayScore > 5 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_5:
                                                            if (awayScore > 5 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_5:
                                                            if (awayScore > 5 || homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_6:
                                                            if (awayScore > 6 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_6:
                                                            if (awayScore > 6 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_6:
                                                            if (awayScore > 6 || homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_7:
                                                            if (awayScore > 7 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_7:
                                                            if (awayScore > 7 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_7:
                                                            if (awayScore > 7 || homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case ANY_UNQUOTED:
                                                            if (awayScore > 7 || homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case WINNING_MARGIN:
                                            if (during90Minutes(matchStatus) && (homeScore > 0 || awayScore > 0)) {
                                                final int nSafeRunners = 1;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case HOME_WIN_BY_1:
                                                        case SCORE_DRAW:
                                                        case AWAY_WIN_BY_4_PLUS:
                                                        case AWAY_WIN_BY_3:
                                                        case AWAY_WIN_BY_2:
                                                        case AWAY_WIN_BY_1:
                                                        case HOME_WIN_BY_4_PLUS:
                                                        case HOME_WIN_BY_3:
                                                        case HOME_WIN_BY_2:
                                                            // never safe
                                                            break;
                                                        case NO_GOALS:
                                                            if (homeScore > 0 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case HALF_WITH_MOST_GOALS:
                                            if (during90MinutesWithoutFirstHalf(matchStatus) && homeHtScore >= 0 && awayHtScore >= 0 && homeScore + awayScore >= 2 * (homeHtScore + awayHtScore)) {
                                                final int nSafeRunners = homeScore + awayScore > 2 * (homeHtScore + awayHtScore) ? 3 : 1;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case FIRST_HALF:
                                                            if (homeScore + awayScore >= 2 * (homeHtScore + awayHtScore)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case SECOND_HALF:
                                                            if (homeScore + awayScore > 2 * (homeHtScore + awayHtScore)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case TIE:
                                                            if (homeScore + awayScore > 2 * (homeHtScore + awayHtScore)) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case METHOD_OF_VICTORY:
                                            if (matchStatus == MatchStatus.AWAITING_ET || matchStatus == MatchStatus.OVERTIME || matchStatus == MatchStatus.FIRST_ET || matchStatus == MatchStatus.ET_HALF_TIME || matchStatus == MatchStatus.SECOND_ET ||
                                                matchStatus == MatchStatus.AWAITING_PEN || matchStatus == MatchStatus.PENALTIES) {
                                                final int nRunners = marketCatalogue.getNRunners();
                                                final int nSafeRunners;
                                                if (nRunners == 6) {
                                                    nSafeRunners = matchStatus == MatchStatus.AWAITING_PEN || matchStatus == MatchStatus.PENALTIES ? 4 : 2;
                                                } else if (nRunners == 4) {
                                                    nSafeRunners = 2;
                                                } else {
                                                    logger.error("wrong nRunners {} for: {} {} {}", nRunners, parsedMarketType, matchStatus, Generic.objectToString(marketCatalogue));
                                                    nSafeRunners = 0;
                                                }
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case HOME_90_MINUTES:
                                                        case AWAY_90_MINUTES:
                                                            if (matchStatus == MatchStatus.AWAITING_ET || matchStatus == MatchStatus.OVERTIME || matchStatus == MatchStatus.FIRST_ET || matchStatus == MatchStatus.ET_HALF_TIME ||
                                                                matchStatus == MatchStatus.SECOND_ET || matchStatus == MatchStatus.AWAITING_PEN || matchStatus == MatchStatus.PENALTIES) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, ScrapedField.MATCH_STATUS);
                                                            }
                                                            break;
                                                        case HOME_ET:
                                                        case AWAY_ET:
                                                            if (matchStatus == MatchStatus.AWAITING_PEN || matchStatus == MatchStatus.PENALTIES) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, ScrapedField.MATCH_STATUS);
                                                            }
                                                            break;
                                                        case HOME_PENALTIES:
                                                        case AWAY_PENALTIES:
                                                            // never safe
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case HALF_TIME_SCORE2_A:
                                            if (duringFirstHalf(matchStatus) && (homeScore > 3 || awayScore > 0)) {
                                                final int nSafeRunners = homeScore > 7 || awayScore > 1 ? 11 :
                                                                         2 * Math.max(homeScore - 3, 0) + 5 * awayScore - Math.max(homeScore - 3, 0) * awayScore;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case SCORE_3_0:
                                                            if (homeScore > 3 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_3_1:
                                                            if (homeScore > 3 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_4_0:
                                                            if (homeScore > 4 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_4_1:
                                                            if (homeScore > 4 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_5_0:
                                                            if (homeScore > 5 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_5_1:
                                                            if (homeScore > 5 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_6_0:
                                                            if (homeScore > 6 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_6_1:
                                                            if (homeScore > 6 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_7_0:
                                                            if (homeScore > 7 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_7_1:
                                                            if (homeScore > 7 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case ANY_UNQUOTED:
                                                            if (homeScore > 7 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case HALF_TIME_SCORE2_B:
                                            if (duringFirstHalf(matchStatus) && (awayScore > 3 || homeScore > 0)) {
                                                final int nSafeRunners = awayScore > 7 || homeScore > 1 ? 11 :
                                                                         2 * Math.max(awayScore - 3, 0) + 5 * homeScore - Math.max(awayScore - 3, 0) * homeScore;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case SCORE_0_3:
                                                            if (awayScore > 3 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_3:
                                                            if (awayScore > 3 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_4:
                                                            if (awayScore > 4 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_4:
                                                            if (awayScore > 4 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_5:
                                                            if (awayScore > 5 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_5:
                                                            if (awayScore > 5 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_6:
                                                            if (awayScore > 6 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_6:
                                                            if (awayScore > 6 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_7:
                                                            if (awayScore > 7 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_7:
                                                            if (awayScore > 7 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case ANY_UNQUOTED:
                                                            if (awayScore > 7 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case CORRECT_SCORE3_A:
                                            if (during90Minutes(matchStatus) && (homeScore > 8 || awayScore > 0)) {
                                                final int nSafeRunners = homeScore > 11 || awayScore > 2 ?
                                                                         13 : 3 * Math.max(homeScore - 8, 0) + 4 * awayScore - Math.max(homeScore - 8, 0) * awayScore;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case SCORE_8_0:
                                                            if (homeScore > 8 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_8_1:
                                                            if (homeScore > 8 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_8_2:
                                                            if (homeScore > 8 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_9_0:
                                                            if (homeScore > 9 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_9_1:
                                                            if (homeScore > 9 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_9_2:
                                                            if (homeScore > 9 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_10_0:
                                                            if (homeScore > 10 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_10_1:
                                                            if (homeScore > 10 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_10_2:
                                                            if (homeScore > 10 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_11_0:
                                                            if (homeScore > 11 || awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_11_1:
                                                            if (homeScore > 11 || awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_11_2:
                                                            if (homeScore > 11 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case ANY_UNQUOTED:
                                                            if (homeScore > 11 || awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case CORRECT_SCORE3_B:
                                            if (during90Minutes(matchStatus) && (awayScore > 8 || homeScore > 0)) {
                                                final int nSafeRunners = awayScore > 11 || homeScore > 2 ?
                                                                         13 : 3 * Math.max(awayScore - 8, 0) + 4 * homeScore - Math.max(awayScore - 8, 0) * homeScore;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case SCORE_0_8:
                                                            if (awayScore > 8 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_8:
                                                            if (awayScore > 8 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_8:
                                                            if (awayScore > 8 || homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_9:
                                                            if (awayScore > 9 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_9:
                                                            if (awayScore > 9 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_9:
                                                            if (awayScore > 9 || homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_10:
                                                            if (awayScore > 10 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_10:
                                                            if (awayScore > 10 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_10:
                                                            if (awayScore > 10 || homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_0_11:
                                                            if (awayScore > 11 || homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_1_11:
                                                            if (awayScore > 11 || homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case SCORE_2_11:
                                                            if (awayScore > 11 || homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case ANY_UNQUOTED:
                                                            if (awayScore > 11 || homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case TO_SCORE_BOTH_HALVES_A:
                                            if (during90MinutesWithoutFirstHalf(matchStatus) && (homeHtScore == 0 || (homeHtScore > 0 && homeScore > homeHtScore))) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case YES:
                                                            if (homeHtScore > 0 && homeScore > homeHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScoresHts);
                                                            }
                                                            if (homeHtScore == 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case NO:
                                                            if (homeHtScore > 0 && homeScore > homeHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            if (homeHtScore == 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case TO_SCORE_BOTH_HALVES_B:
                                            if (during90MinutesWithoutFirstHalf(matchStatus) && (awayHtScore == 0 || (awayHtScore > 0 && awayScore > awayHtScore))) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case YES:
                                                            if (awayHtScore > 0 && awayScore > awayHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScoresHts);
                                                            }
                                                            if (awayHtScore == 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case NO:
                                                            if (awayHtScore > 0 && awayScore > awayHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            if (awayHtScore == 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case WIN_HALF_A:
                                            if (during90MinutesWithoutFirstHalf(matchStatus) && awayHtScore >= 0 && homeHtScore > awayHtScore) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case YES:
                                                            if (homeHtScore > awayHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        case NO:
                                                            if (homeHtScore > awayHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case WIN_HALF_B:
                                            if (during90MinutesWithoutFirstHalf(matchStatus) && homeHtScore >= 0 && awayHtScore > homeHtScore) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case YES:
                                                            if (awayHtScore > homeHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        case NO:
                                                            if (awayHtScore > homeHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case GOAL_BOTH_HALVES:
                                            final boolean goalScoredInBothHalves = awayHtScore + homeHtScore > 0 && awayScore + homeScore > awayHtScore + homeHtScore;
                                            if (during90MinutesWithoutFirstHalf(matchStatus) && homeHtScore >= 0 && awayHtScore >= 0 && (awayHtScore + homeHtScore == 0 || goalScoredInBothHalves)) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case YES:
                                                            if (awayHtScore + homeHtScore == 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusHts);
                                                            }
                                                            if (goalScoredInBothHalves) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case NO:
                                                            if (awayHtScore + homeHtScore == 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHts);
                                                            }
                                                            if (goalScoredInBothHalves) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case TEAM_A_WIN_TO_NIL:
                                        case CLEAN_SHEET_A:
                                            if (during90Minutes(matchStatus) && awayScore > 0) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case YES:
                                                            if (awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusAwayScore);
                                                            }
                                                            break;
                                                        case NO:
                                                            if (awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusAwayScore);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case TEAM_B_WIN_TO_NIL:
                                        case CLEAN_SHEET_B:
                                            if (during90Minutes(matchStatus) && homeScore > 0) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case YES:
                                                            if (homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusHomeScore);
                                                            }
                                                            break;
                                                        case NO:
                                                            if (homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHomeScore);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case WIN_BOTH_HALVES_A:
                                            if (during90MinutesWithoutFirstHalf(matchStatus) && homeHtScore >= 0 && awayHtScore >= 0 && homeHtScore <= awayHtScore) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case YES:
                                                            if (homeHtScore <= awayHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        case NO:
                                                            if (homeHtScore <= awayHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case WIN_BOTH_HALVES_B:
                                            if (during90MinutesWithoutFirstHalf(matchStatus) && awayHtScore >= 0 && awayHtScore <= homeHtScore) { // includes homeHtScore >= 0
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case YES:
                                                            if (awayHtScore <= homeHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        case NO:
                                                            if (awayHtScore <= homeHtScore) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case BOTH_TEAMS_TO_SCORE:
                                            if (during90Minutes(matchStatus) && homeScore > 0 && awayScore > 0) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case YES:
                                                            if (homeScore > 0 && awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case NO:
                                                            if (homeScore > 0 && awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case FIRST_HALF_GOALS_05:
                                            if (duringFirstHalf(matchStatus) && homeScore + awayScore > 0 &&
                                                ((homeHtScore < 0 && awayHtScore < 0) || (homeHtScore + awayHtScore > 0))) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_05:
                                                            if (homeScore + awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_05:
                                                            if (homeScore + awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case FIRST_HALF_GOALS_15:
                                            if (duringFirstHalf(matchStatus) && homeScore + awayScore > 1 &&
                                                ((homeHtScore < 0 && awayHtScore < 0) || (homeHtScore + awayHtScore > 1))) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_15:
                                                            if (homeScore + awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_15:
                                                            if (homeScore + awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case FIRST_HALF_GOALS_25:
                                            if (duringFirstHalf(matchStatus) && homeScore + awayScore > 2 &&
                                                ((homeHtScore < 0 && awayHtScore < 0) || (homeHtScore + awayHtScore > 2))) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_25:
                                                            if (homeScore + awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_25:
                                                            if (homeScore + awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case SECOND_HALF_GOALS_05:
                                            final boolean scoredInSecondHalf = homeScore + awayScore - (homeHtScore + awayHtScore) > 0;
                                            if (duringSecondHalf(matchStatus) && homeHtScore >= 0 && awayHtScore >= 0 && scoredInSecondHalf) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_05:
                                                            if (scoredInSecondHalf) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case GOALS_OVER_05:
                                                            if (scoredInSecondHalf) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case SECOND_HALF_GOALS_15:
                                            final boolean scoredMoreThanOneGoalInSecondHalf = homeScore + awayScore - (homeHtScore + awayHtScore) > 1;
                                            if (duringSecondHalf(matchStatus) && homeHtScore >= 0 && awayHtScore >= 0 && scoredMoreThanOneGoalInSecondHalf) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_15:
                                                            if (scoredMoreThanOneGoalInSecondHalf) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        case GOALS_OVER_15:
                                                            if (scoredMoreThanOneGoalInSecondHalf) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScoresHts);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_05:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 0) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_05:
                                                            if (homeScore + awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_05:
                                                            if (homeScore + awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_15:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 1) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_15:
                                                            if (homeScore + awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_15:
                                                            if (homeScore + awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_25:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 2) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_25:
                                                            if (homeScore + awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_25:
                                                            if (homeScore + awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_35:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 3) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_35:
                                                            if (homeScore + awayScore > 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_35:
                                                            if (homeScore + awayScore > 3) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_45:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 4) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_45:
                                                            if (homeScore + awayScore > 4) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_45:
                                                            if (homeScore + awayScore > 4) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_55:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 5) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_55:
                                                            if (homeScore + awayScore > 5) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_55:
                                                            if (homeScore + awayScore > 5) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_65:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 6) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_65:
                                                            if (homeScore + awayScore > 6) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_65:
                                                            if (homeScore + awayScore > 6) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_75:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 7) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_75:
                                                            if (homeScore + awayScore > 7) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_75:
                                                            if (homeScore + awayScore > 7) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_85:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 8) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_85:
                                                            if (homeScore + awayScore > 8) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_85:
                                                            if (homeScore + awayScore > 8) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_95:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 9) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_95:
                                                            if (homeScore + awayScore > 9) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_95:
                                                            if (homeScore + awayScore > 9) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_105:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 10) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_105:
                                                            if (homeScore + awayScore > 10) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_105:
                                                            if (homeScore + awayScore > 10) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_115:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 11) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_115:
                                                            if (homeScore + awayScore > 11) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_115:
                                                            if (homeScore + awayScore > 11) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_125:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 12) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_125:
                                                            if (homeScore + awayScore > 12) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_125:
                                                            if (homeScore + awayScore > 12) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_135:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 13) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_135:
                                                            if (homeScore + awayScore > 13) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_135:
                                                            if (homeScore + awayScore > 13) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case OVER_UNDER_145:
                                            if (during90Minutes(matchStatus) && homeScore + awayScore > 14) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_145:
                                                            if (homeScore + awayScore > 14) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_145:
                                                            if (homeScore + awayScore > 14) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else {
                                                // market not safe
                                            }
                                            break;
                                        case TEAM_A_OVER_UNDER_05:
                                            if (during90Minutes(matchStatus) && homeScore > 0) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_05:
                                                            if (homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_05:
                                                            if (homeScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else { // market not safe
                                            }
                                            break;
                                        case TEAM_A_OVER_UNDER_15:
                                            if (during90Minutes(matchStatus) && homeScore > 1) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_15:
                                                            if (homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_15:
                                                            if (homeScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else { // market not safe
                                            }
                                            break;
                                        case TEAM_A_OVER_UNDER_25:
                                            if (during90Minutes(matchStatus) && homeScore > 2) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_25:
                                                            if (homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_25:
                                                            if (homeScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else { // market not safe
                                            }
                                            break;
                                        case TEAM_B_OVER_UNDER_05:
                                            if (during90Minutes(matchStatus) && awayScore > 0) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_05:
                                                            if (awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_05:
                                                            if (awayScore > 0) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else { // market not safe
                                            }
                                            break;
                                        case TEAM_B_OVER_UNDER_15:
                                            if (during90Minutes(matchStatus) && awayScore > 1) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_15:
                                                            if (awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_15:
                                                            if (awayScore > 1) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else { // market not safe
                                            }
                                            break;
                                        case TEAM_B_OVER_UNDER_25:
                                            if (during90Minutes(matchStatus) && awayScore > 2) {
                                                final int nSafeRunners = 2;
                                                final SynchronizedSafeSet<SafeRunner> safeRunnersSet = createSafeRunnersSet(nSafeRunners);
                                                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                                                    final ParsedRunnerType parsedRunnerType = parsedRunner.getParsedRunnerType();
                                                    final Long selectionId = parsedRunner.getSelectionId();
                                                    SafeRunner safeRunner = null;
                                                    switch (parsedRunnerType) {
                                                        case GOALS_UNDER_25:
                                                            if (awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, LAY, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        case GOALS_OVER_25:
                                                            if (awayScore > 2) {
                                                                safeRunner = new SafeRunner(marketId, selectionId, BACK, usedScrapersMap, statusScores);
                                                            }
                                                            break;
                                                        default:
                                                            defaultParsedRunnerTypeError(parsedRunnerType, marketCatalogue);
                                                            break;
                                                    }
                                                    addSafeRunner(safeRunnersSet, safeRunner, usedParsedRunnersSet, parsedRunner);
                                                }
                                                safeRunnersSetSizeCheck(safeRunnersSet, nSafeRunners, marketCatalogue, marketId, parsedMarketType, eventName, modifiedMarketsList, startTime, usedParsedRunnersSet, scraperData);
                                            } else { // market not safe
                                            }
                                            break;
                                        case ET_CORRECT_SCORE:
                                            // harder to code; postponed
                                            break;
                                        default:
                                            logger.error("STRANGE unsupported parsedMarketType {} in findSafeRunners: {}", parsedMarketType, Generic.objectToString(marketCatalogue));
                                            break;
                                    } // end switch
//                                } else { // strange error, but error message is printed when the error is first found
//                                }
                                } else {
                                    Generic.alreadyPrintedMap.logOnce(logger, LogLevel.WARN, "no proper scores for eventName:{} marketId:{} score:{}/{} ht:{}/{} status:{}", eventName, marketId, homeScore, awayScore, homeHtScore, awayHtScore,
                                                                      matchStatus == null ? null : matchStatus.name());
                                }
//                                    } else {
//                                        String scraperString = Generic.objectToString(scraperEvent);
//                                        long currentTime = System.currentTimeMillis();
//                                        if (Statics.timedWarningsMap.containsKey(scraperString)) {
//                                            long existingTime = Statics.timedWarningsMap.get(scraperString);
//                                            long lapsedTime = currentTime - existingTime;
//                                            if (lapsedTime > Generic.MINUTE_LENGTH_MILLISECONDS) {
//                                                logger.warn("scraperEvent not in play lasting {} ms in findSafeRunners for: {}", lapsedTime, scraperString);
//                                            } else {
//                                                // might be a normal temporary error; not enough time has lapsed
//                                            }
//                                        } else {
//                                            Statics.timedWarningsMap.put(scraperString, currentTime);
//                                        }
//                                    }
//                                } else {
//                                    logger.error("STRANGE matchStatus null or scraperErrors {} in findSafeRunners for marketCatalogue: {} scraperEvent: {}", scraperErrors,
//                                            Generic.objectToString(marketCatalogue), Generic.objectToString(scraperEvent));
//                                }
//                            } else {
//                                logger.error("STRANGE scraperEvent null in findSafeRunners for marketCatalogue: {}", Generic.objectToString(marketCatalogue));
//                                if (scraperEvent == null) {
//                                    Statics.betradarEventsMap.remove(scraperEventId);
//                                    Statics.marketCataloguesMap.remove(marketId);
//                                    Statics.eventsMap.removeValueAll(event);
//                                }
//                            }
                            } else {
                                logger.error("STRANGE not initialized value in findSafeRunners inner for marketCatalogue: {}", Generic.objectToString(marketCatalogue));
                            }
                        } else { // this else branch is normal
//                            logger.error("event null or ignored in findSafeRunners middleInner for: {} {}", Generic.objectToString(event),
//                                    Generic.objectToString(marketCatalogue));
                        }
                    } else {
//                        if (parsedMarket == null || event == null || event.isIgnored()) {
                        logger.error("STRANGE not initialized value in findSafeRunners for marketCatalogue: {}", Generic.objectToString(marketCatalogue));
//                        }
                    }
                } else {
//                    logger.error("null marketId or marketCatalogue in findSafeRunners: {} {}", marketId, Generic.objectToString(marketCatalogue));
                    if (marketCatalogue == null) {
                        logger.error("null marketCatalogue in findSafeRunners: {}", marketId);
                        Statics.marketCataloguesMap.removeValueAll(null);
                    } else if (marketId == null) {
                        logger.error("STRANGE ERROR null marketId in findSafeRunners: {}", Generic.objectToString(marketCatalogue));
                        // will not remove; the extra strong error message is good enough
//                        Statics.marketCataloguesMap.remove(null);
                    } else if (marketCatalogue.isIgnored()) { // ignored, probably nothing to be done
//                    } else if (marketCatalogue.isTempRemoved()) { // tempRemoved, probably nothing to be done
                    } else { // branch for both marketCatalogue & marketId non null; right now this shouldn't be possible
                        logger.error("this else branch shouldn't be possible in findSafeRunners: {} {}", marketId, Generic.objectToString(marketCatalogue));
                    }
                }
            } // end for

            // if (!toRemoveEntrySet.isEmpty()) {
            //     Statics.marketCataloguesMap.removeAllKeys(toRemoveEntrySet);
            // }
            final String fullRunString = fullRun ? "fullRun " : "";
            logger.info("findSafeRunners {}took {} ms safeMarketsMap: {}", fullRunString, System.currentTimeMillis() - startTime, Statics.safeMarketsMap.size());
            Statics.safeMarketsMap.timeStamp();

            final int sizeMarkets = modifiedMarketsList.size();
            if (sizeMarkets > 0) {
                // modified markets are supposed to be found during regular runs, not fullRun; when a modification happens, this should result in an immediate regular run
                final String printedString = MessageFormatter.arrayFormat("findSafeRunners {}modifiedMarketsList: {} launch: getMarketBooks", new Object[]{fullRunString, sizeMarkets}).getMessage();
                if (fullRun) {
                    if (startTime - Statics.PROGRAM_START_TIME <= Generic.MINUTE_LENGTH_MILLISECONDS) {
                        logger.info("beginning of the program {}", printedString); // this sometimes happens at the beginning of program run
                    } else {
                        final long currentTime = System.currentTimeMillis();
                        final long timeSinceLastRegularRun = currentTime - lastRegularFindSafeRunnersRunStamp.get();
                        final String stampedPrintedString = MessageFormatter.arrayFormat("{} timeSinceLastRegularRun: {}ms", new Object[]{printedString, timeSinceLastRegularRun}).getMessage();
                        if (timeSinceLastRegularRun < 100L) {
                            logger.warn(stampedPrintedString);
                        } else {
                            logger.error(stampedPrintedString);
                        }
                    }
                } else {
                    logger.info(printedString);
                }
                Statics.quickCheckThread.getMarketBooks(modifiedMarketsList, 0L, Statics.N_ALL); // no delay, as that would affect timed runs
            }
        } else {
            logger.info("scraperEventsMap too old in findSafeRunners");
        }
    }
}
