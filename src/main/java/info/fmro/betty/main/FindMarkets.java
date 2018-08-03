package info.fmro.betty.main;

import com.google.common.collect.Iterables;
import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.entities.MarketDescription;
import info.fmro.betty.entities.RunnerCatalog;
import info.fmro.betty.enums.CommandType;
import info.fmro.betty.enums.MarketProjection;
import info.fmro.betty.enums.ParsedMarketType;
import info.fmro.betty.enums.ParsedRunnerType;
import info.fmro.betty.objects.ParsedMarket;
import info.fmro.betty.objects.ParsedRunner;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class FindMarkets {

    private static final Logger logger = LoggerFactory.getLogger(FindMarkets.class);
    public static final HashSet<MarketProjection> marketProjectionsSet = new HashSet<>(8, 0.75f);

    private FindMarkets() {
    }

    static {
        marketProjectionsSet.add(MarketProjection.COMPETITION);
        marketProjectionsSet.add(MarketProjection.EVENT);
        marketProjectionsSet.add(MarketProjection.EVENT_TYPE);
        marketProjectionsSet.add(MarketProjection.MARKET_DESCRIPTION);
        marketProjectionsSet.add(MarketProjection.RUNNER_DESCRIPTION);
        marketProjectionsSet.add(MarketProjection.MARKET_START_TIME);
    }

    public static boolean parsedRunnersSetSizeCheck(HashSet<ParsedRunner> parsedRunnersSet, int expectedSize, String marketId, ParsedMarketType parsedMarketType, MarketCatalogue marketCatalogue) {
        boolean interestingMarket;
        if (parsedRunnersSet == null || marketCatalogue == null) {
            logger.error("null parsedRunnersSet or marketCatalogue in parsedRunnersSetSizeCheck: {} {}", Generic.objectToString(parsedRunnersSet), Generic.objectToString(marketCatalogue));
            interestingMarket = false;
        } else if (marketCatalogue.isIgnored()) {
            interestingMarket = false;
        } else if (parsedRunnersSet.size() == expectedSize) {
            final ParsedMarket parsedMarket = new ParsedMarket(marketId, parsedMarketType, parsedRunnersSet);
            if (parsedMarket.checkSameTypeRunners()) {
                interestingMarket = marketCatalogue.setParsedMarket(parsedMarket) > 0; // only true if modified
            } else {
                interestingMarket = false;
            }
        } else {
            Generic.alreadyPrintedMap.logOnce(Statics.newMarketSynchronizedWriter, logger, LogLevel.ERROR, "incorrect parsedRunnersSet size {} in: {} {} for: {}", parsedRunnersSet.size(), parsedMarketType, marketId,
                                              Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
            interestingMarket = false;
        }
        return interestingMarket;
    }

    public static void addParsedRunner(HashSet<ParsedRunner> parsedRunnersSet, RunnerCatalog runnerCatalog, final ParsedRunnerType parsedRunnerType) {
        final Long selectionId = runnerCatalog.getSelectionId();
        if (parsedRunnerType != null) {
            final ParsedRunner parsedRunner = new ParsedRunner(selectionId, parsedRunnerType);
            parsedRunnersSet.add(parsedRunner);
        }
    }

    public static ParsedRunnerType unknownParsedRunnerTypeError(ParsedMarketType parsedMarketType, long sortPriority, String runnerName, String marketId, MarketCatalogue marketCatalogue) {
        Generic.alreadyPrintedMap.logOnce(logger, LogLevel.ERROR, "unknown parsedRunnerType for {} {} {} in: {} for: {}", parsedMarketType, sortPriority, runnerName, marketId,
                                          Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
        return null;
    }

    public static boolean unknownParsedMarketTypeError(String marketName, String eventHomeName, String eventAwayName, String marketId, MarketCatalogue marketCatalogue) {
        Generic.alreadyPrintedMap.logOnce(Statics.newMarketSynchronizedWriter, logger, LogLevel.ERROR, "parsedMarketType null for {} {}/{} in: {} for: {}", marketName, eventHomeName, eventAwayName, marketId,
                                          Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
        return false;
    }

    public static boolean unknownParsedMarketTypeError(String marketName, StringBuilder eventHomeName, StringBuilder eventAwayName, String marketId, MarketCatalogue marketCatalogue) {
        Generic.alreadyPrintedMap.logOnce(Statics.newMarketSynchronizedWriter, logger, LogLevel.ERROR, "parsedMarketType null for {} {}/{} in: {} for: {}", marketName, eventHomeName, eventAwayName, marketId,
                                          Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
        return false;
    }

    public static HashSet<ParsedRunner> createParsedRunnersSet(int nParsedRunners) {
        final int capacity = Generic.getCollectionCapacity(nParsedRunners);
        return new HashSet<>(capacity);
    }

    public static ParsedMarketType pickParsedMarketType(String endMarker, String marketName, StringBuilder eventHomeName, StringBuilder eventAwayName, String marketId, ParsedMarketType choiceA, ParsedMarketType choiceB) {
        ParsedMarketType parsedMarketType;
        if (StringUtils.endsWithIgnoreCase(marketName, endMarker)) {
            final String marketSubstring = marketName.substring(0, StringUtils.indexOfIgnoreCase(marketName, endMarker));
            final double homeMatch = Formulas.matchTeams(eventHomeName.toString(), marketSubstring);
            final double awayMatch = Formulas.matchTeams(eventAwayName.toString(), marketSubstring);
            if (homeMatch > Statics.threshold && awayMatch < homeMatch) {
                Generic.stringBuilderReplace(eventHomeName, marketSubstring);
            } else if (awayMatch > Statics.threshold && homeMatch < awayMatch) {
                Generic.stringBuilderReplace(eventAwayName, marketSubstring);
            } else {
                Generic.alreadyPrintedMap.logOnce(logger, LogLevel.ERROR, "STRANGE market team matching {} {} {} {} {} in: {}", homeMatch, awayMatch, eventHomeName, eventAwayName, marketName, marketId);
            }
            if (marketName.equalsIgnoreCase(eventHomeName + endMarker)) {
                parsedMarketType = choiceA;
            } else if (marketName.equalsIgnoreCase(eventAwayName + endMarker)) {
                parsedMarketType = choiceB;
            } else {
                parsedMarketType = null;
            }
        } else {
            parsedMarketType = null;
        }
        return parsedMarketType;
    }

    public static boolean getNamesFromRunnerNames(StringBuilder eventHomeName, StringBuilder eventAwayName, String marketId, String runnerName, String beginMarker, String endMarker, int sortPriority, int sortHome, int sortAway,
                                                  MarketCatalogue marketCatalogue) {
        boolean success = true;
        if (sortPriority == sortHome || sortPriority == sortAway) {
            String teamString;
            try {
                if (beginMarker == null && endMarker == null) {
                    teamString = runnerName;
                } else if (beginMarker != null && endMarker != null) {
                    teamString = runnerName.substring(StringUtils.indexOfIgnoreCase(runnerName, beginMarker) + beginMarker.length(), StringUtils.indexOfIgnoreCase(runnerName, endMarker));
                } else if (beginMarker == null && endMarker != null) {
                    teamString = runnerName.substring(0, StringUtils.indexOfIgnoreCase(runnerName, endMarker));
                } else if (beginMarker != null && endMarker == null) {
                    teamString = runnerName.substring(StringUtils.indexOfIgnoreCase(runnerName, beginMarker) + beginMarker.length());
                } else {
                    Generic.alreadyPrintedMap.logOnce(logger, LogLevel.ERROR, "STRANGE unknown if branch in runner team matching {} {} {} {} {} in: {} {}", runnerName, beginMarker, endMarker, eventHomeName, eventAwayName, marketId,
                                                      Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
                    teamString = null;
                }
            } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
                Generic.alreadyPrintedMap.logOnce(Statics.newMarketSynchronizedWriter, logger, LogLevel.ERROR, "STRANGE stringIndexOutOfBoundsException in runner team matching {} {} {} {} {} {} {} {} in: {} {}", runnerName, beginMarker, endMarker,
                                                  sortPriority, sortHome, sortAway, eventHomeName, eventAwayName, marketId, Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"), stringIndexOutOfBoundsException);
                teamString = null;
            }

            if (teamString != null) {
                if (sortPriority == sortHome) {
                    final double homeMatch = Formulas.matchTeams(eventHomeName.toString(), teamString);
                    final double awayMatch = Formulas.matchTeams(eventAwayName.toString(), teamString);
                    if (homeMatch > Statics.threshold && awayMatch < homeMatch) {
                        Generic.stringBuilderReplace(eventHomeName, teamString);
                    } else {
                        Generic.alreadyPrintedMap.logOnce(logger, LogLevel.ERROR, "STRANGE runner home team matching {} {} {} / {} - {} in: {} {}", homeMatch, awayMatch, eventHomeName, eventAwayName, teamString, marketId,
                                                          Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
                        success = false;
                    }
                }
                if (sortPriority == sortAway) {
                    final double homeMatch = Formulas.matchTeams(eventHomeName.toString(), teamString);
                    final double awayMatch = Formulas.matchTeams(eventAwayName.toString(), teamString);
                    if (awayMatch > Statics.threshold && homeMatch < awayMatch) {
                        Generic.stringBuilderReplace(eventAwayName, teamString);
                    } else {
                        Generic.alreadyPrintedMap.logOnce(logger, LogLevel.ERROR, "STRANGE runner away team matching {} {} {} / {} - {} in: {} {}", homeMatch, awayMatch, eventHomeName, eventAwayName, teamString, marketId,
                                                          Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
                        success = false;
                    }
                }
            } else { // error message is already printed when teamString becomes null
                success = false;
            }
        } else { // won't get any names now
        }

        return success;
    }

    public static void findMarkets() {
        findMarkets(null, false);
    }

    public static void findMarkets(boolean checkAll) {
        findMarkets(null, checkAll);
    }

    public static void findMarkets(HashSet<Event> eventsSet) {
        findMarkets(eventsSet, false);
    }

    public static void findMarkets(TreeSet<String> marketIdsSet) {
        if (marketIdsSet != null) {
            final long methodStartTime = System.currentTimeMillis();
            final Set<MarketCatalogue> returnSet = Collections.synchronizedSet(new HashSet<>(Generic.getCollectionCapacity(marketIdsSet.size())));
            final Iterable<List<String>> splitsIterable = Iterables.partition(marketIdsSet, 200);
            final List<Thread> threadList = new ArrayList<>(Iterables.size(splitsIterable));
            for (List<String> marketIdsList : splitsIterable) {
                final RescriptOpThread<MarketCatalogue> rescriptOpThread = new RescriptOpThread<>(returnSet, new TreeSet<>(marketIdsList), marketProjectionsSet);
                final Thread thread = new Thread(rescriptOpThread);
                thread.start();
                threadList.add(thread);
            } // end for

            final long startedWaitingTime = System.currentTimeMillis();
            for (final Thread thread : threadList) {
                if (thread.isAlive()) {
                    try {
                        thread.join();
                    } catch (InterruptedException interruptedException) {
                        logger.error("STRANGE interruptedException in findMarkets marketIds", interruptedException);
                    }
                } else { // thread already finished, nothing to be done
                }
            } // end for

            final HashSet<Event> modifiedEvents = new HashSet<>(0);
            final LinkedHashSet<Entry<String, MarketCatalogue>> toCheckMarkets = new LinkedHashSet<>(0);
            int nModifiedMarkets = 0, nAddedMarkets = 0;
            for (MarketCatalogue marketCatalogue : returnSet) {
                marketCatalogue.setTimeStamp(startedWaitingTime);
                final Event event = Formulas.getStoredEventOfMarketCatalogue(marketCatalogue);
                final String marketName = marketCatalogue.getMarketName();
                final String marketId = marketCatalogue.getMarketId();
                final MarketDescription marketDescription = marketCatalogue.getDescription();
                final List<RunnerCatalog> runnerCatalogsList = marketCatalogue.getRunners();
                final Event eventStump = marketCatalogue.getEventStump();

                if (event == null) {
                    logger.info("marketId {} without event in maps in findMarkets(marketIdsSet)", marketId);
                } else { // normal behaviour, nothing to do
                }
                if (eventStump != null && marketName != null && marketId != null && marketDescription != null && runnerCatalogsList != null) {
                    if (event != null && event.isIgnored(methodStartTime)) {
                        logger.warn("marketCatalogue is attached to blackListed event: {}", marketId);
                    }

                    if (event != null) {
                        final int update = event.update(eventStump);
                        if (update > 0) {
                            modifiedEvents.add(event);
                            logger.warn("event modified {} in findMarkets for: {} {} {}", update, marketId, Generic.objectToString(eventStump), Generic.objectToString(event)); // this might be normal behaviour
                        }
                    } else {
                        final String eventStumpId = eventStump.getId();
                        if (eventStumpId != null) {
                            final Event existingEvent = Statics.eventsMap.putIfAbsent(eventStumpId, eventStump);
                            if (existingEvent == null) { // event was added, no previous event existed, double check to avoid racing issues
                                modifiedEvents.add(eventStump);
                            } else { // this is the normal behaviour branch, event is supposed to exist
                            }
                            if (existingEvent != null && existingEvent.isIgnored(methodStartTime)) {
                                logger.warn("marketCatalogue attached to blackListed event: {}", marketId);
                            }
                        } else {
                            logger.error("null eventStumpId in findMarkets for: {}", Generic.objectToString(marketCatalogue));
                        }
                    }

                    final MarketCatalogue existingMarketCatalogue;

                    if (Statics.marketCataloguesMap.containsKey(marketId)) {
                        existingMarketCatalogue = Statics.marketCataloguesMap.get(marketId);
                    } else {
                        existingMarketCatalogue = null;
                    }

                    if (existingMarketCatalogue != null) {
                        existingMarketCatalogue.setTotalMatched(marketCatalogue.getTotalMatched()); // else market marked as updated almost every time
                        marketCatalogue.setParsedMarket(existingMarketCatalogue.getParsedMarket());
                        if (existingMarketCatalogue.update(marketCatalogue) > 0) {
                            if (!marketCatalogue.isIgnored()) {
                                toCheckMarkets.add(new SimpleEntry<>(marketId, marketCatalogue));
                            }
                            nModifiedMarkets++;
                        }
                        marketCatalogue = existingMarketCatalogue;
                    }

                    final MarketCatalogue inMapMarketCatalogue = Statics.marketCataloguesMap.putIfAbsent(marketId, marketCatalogue);
                    if (inMapMarketCatalogue == null) { // marketCatalogue was added, no previous marketCatalogue existed
                        if (marketCatalogue.isIgnored(methodStartTime)) {
                            logger.error("blackListed marketCatalogue has been added: {}", marketId);
                        } else {
                            toCheckMarkets.add(new SimpleEntry<>(marketId, marketCatalogue));
                            nAddedMarkets++;
                        }
                    }
                } else {
                    logger.error("null fields in findMarkets marketIds for: {}", Generic.objectToString(marketCatalogue));
                } // end else
            } // end for

            final int sizeModified = modifiedEvents.size();
            if (sizeModified > 0) {
                final String printedString = MessageFormatter.arrayFormat("findMarkets modifiedEvents: {} launch: findMarkets", new Object[]{sizeModified}).getMessage();
                logger.info(printedString);

                final HashSet<Event> notIgnoredModifiedEvents = new HashSet<>(Generic.getCollectionCapacity(modifiedEvents));
                for (Event event : modifiedEvents) {
                    if (!event.isIgnored()) {
                        notIgnoredModifiedEvents.add(event);
                    }
                } // end for
                final int sizeNotIgnoredModified = notIgnoredModifiedEvents.size();
                if (sizeNotIgnoredModified > 0) {
                    logger.info("findMarkets modifiedEvents: {} launch: findMarkets", sizeNotIgnoredModified);
                    Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, notIgnoredModifiedEvents));
                }
            }

            if (Statics.safeBetModuleActivated) {
                final int sizeEntries = toCheckMarkets.size();
                if (sizeEntries > 0) {
                    final String printedString = MessageFormatter.arrayFormat("findMarkets toCheckMarkets(modif/add): {}({}/{}) launch: findSafeRunners", new Object[]{sizeEntries, nModifiedMarkets, nAddedMarkets}).getMessage();
                    logger.info(printedString);

                    final LinkedHashSet<Entry<String, MarketCatalogue>> notIgnoredToCheckMarkets = new LinkedHashSet<>(Generic.getCollectionCapacity(toCheckMarkets));
                    for (Entry<String, MarketCatalogue> entry : toCheckMarkets) {
                        MarketCatalogue value = entry.getValue();
                        if (!value.isIgnored()) {
                            notIgnoredToCheckMarkets.add(entry);
                        }
                    } // end for
                    final int sizeNotIgnoredToCheckMarkets = notIgnoredToCheckMarkets.size();
                    if (sizeNotIgnoredToCheckMarkets > 0) {
                        Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findSafeRunners, notIgnoredToCheckMarkets));
                    }
                }
            }
        } else {
            logger.error("null marketIdsSet in findMarkets");
        }
    }

    public static void findMarkets(HashSet<Event> eventsSet, boolean checkAll) {
        if (!Statics.safeBetModuleActivated || System.currentTimeMillis() - Statics.scraperEventMaps.getNTimeStamp() <= Generic.HOUR_LENGTH_MILLISECONDS * 3L) {
            final long methodStartTime = System.currentTimeMillis();

            final Collection<Event> eventsCopy;
            if (eventsSet != null) {
                eventsCopy = eventsSet;
            } else {
                eventsCopy = Statics.eventsMap.valuesCopy();
            }

            final Iterator<Event> iterator = eventsCopy.iterator();
            while (iterator.hasNext()) {
                final Event event = iterator.next();
                if (event == null) {
                    logger.error("STRANGE null event in findMarkets initial removal");
                    iterator.remove();
                    Statics.eventsMap.removeValueAll(null);
                } else if (event.isIgnored()) {
                    iterator.remove();
                } else {
                    final int nMatched = event.getNValidScraperEventIds();
                    final String eventId = event.getId();
                    if (nMatched < Statics.MIN_MATCHED || eventId == null) {
                        iterator.remove();
                        if (eventId == null) {
                            logger.error("null eventId in findMarkets for: {}", Generic.objectToString(event));
                        }
                    } else { // it's matched, will be used in the method
                    }
                }
            } // end while

            if (!eventsCopy.isEmpty()) {
                final Set<MarketCatalogue> returnSet = Collections.synchronizedSet(new HashSet<>(128));
                final List<Thread> threadList = new ArrayList<>(1);
                HashSet<String> eventIds = new HashSet<>(8);
                int setMarketCount = 0;
                for (final Event event : eventsCopy) { // null and ignored verification have been done previously
                    final String eventId = event.getId();
                    final int marketCount = event.getMarketCount();
                    if (setMarketCount + marketCount <= 200) {
                        eventIds.add(eventId);
                        setMarketCount += marketCount;
                    } else {
                        final RescriptOpThread<MarketCatalogue> rescriptOpThread = new RescriptOpThread<>(returnSet, eventIds, marketProjectionsSet);
                        final Thread thread = new Thread(rescriptOpThread);
                        thread.start();
                        threadList.add(thread);
                        eventIds = new HashSet<>(8);
                        eventIds.add(eventId);
                        setMarketCount = marketCount;
                    }
                } // end for
                if (eventIds.size() > 0) {
                    final RescriptOpThread<MarketCatalogue> rescriptOpThread = new RescriptOpThread<>(returnSet, eventIds, marketProjectionsSet);
                    final Thread thread = new Thread(rescriptOpThread);
                    thread.start();
                    threadList.add(thread);
                }
                final long startedWaitingTime = System.currentTimeMillis();

                for (final Thread thread : threadList) {
                    if (thread.isAlive()) {
                        try {
                            thread.join();
                        } catch (InterruptedException interruptedException) {
                            logger.error("STRANGE interruptedException in findMarkets", interruptedException);
                        }
                    } else { // thread already finished, nothing to be done
                    }
                } // end for

                final long endTime = System.currentTimeMillis();
                final LinkedHashSet<Entry<String, MarketCatalogue>> toCheckMarkets = new LinkedHashSet<>(0);
                int nModifiedMarkets = 0, nAddedMarkets = 0;
                final HashSet<Event> modifiedEvents = new HashSet<>(0);
                for (MarketCatalogue marketCatalogue : returnSet) {
                    marketCatalogue.setTimeStamp(startedWaitingTime);
                    final Event event = Formulas.getStoredEventOfMarketCatalogue(marketCatalogue);
                    final String marketName = marketCatalogue.getMarketName();
                    final String marketId = marketCatalogue.getMarketId();
                    final MarketDescription marketDescription = marketCatalogue.getDescription();
                    final List<RunnerCatalog> runnerCatalogsList = marketCatalogue.getRunners();

                    if (event != null && marketName != null && marketId != null && marketDescription != null && runnerCatalogsList != null) {
                        final String eventId = event.getId();
                        final Event eventStump = marketCatalogue.getEventStump();
                        final int nMatched;
                        if (eventStump.parseName() <= 0) { // includes unparsed and errors
                            if (Formulas.isMarketType(marketCatalogue, Statics.supportedEventTypes)) {
//                                logger.info("findMarkets unparsed event name: {} for: {}", eventStump.getName(), marketId);
                                Generic.alreadyPrintedMap.logOnce(4L * Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.INFO, "findMarkets unparsed event name: {} for: {}", eventStump.getName(), marketId);
                            } else { // I won't clutter the logs, no print
//                                Generic.alreadyPrintedMap.logOnce(4L * Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.INFO, "findMarkets unparsed event name: {} for: {} {}", eventStump.getName(), marketId,
//                                                                  Generic.objectToString(marketCatalogue.getEventType()));
                            }

                            nMatched = 0; // so it just continues to next for element; actually without the safeBetsModule activated, it uses the element
                        } else {
                            nMatched = event.getNValidScraperEventIds();
                        }
                        if (event.isIgnored(methodStartTime)) {
                            logger.warn("marketCatalogue attached to blackListed event: {}", marketId);
                        }

                        final int update = event.update(eventStump);
                        if (update > 0) {
                            modifiedEvents.add(event);
                            logger.warn("event modified {} in findMarkets for: {} {} {}", update, marketId, Generic.objectToString(eventStump), Generic.objectToString(event)); // this might be normal behaviour
                        }

                        MarketCatalogue existingMarketCatalogue;
                        if (nMatched >= Statics.MIN_MATCHED) {
                            if (Statics.marketCataloguesMap.containsKey(marketId)) {
                                existingMarketCatalogue = Statics.marketCataloguesMap.get(marketId);
                            } else {
                                existingMarketCatalogue = null;
                            }
                            if (existingMarketCatalogue != null) {
                                existingMarketCatalogue.setTotalMatched(marketCatalogue.getTotalMatched()); // else market marked as updated almost every time
                                marketCatalogue.setParsedMarket(existingMarketCatalogue.getParsedMarket());
                                if (existingMarketCatalogue.update(marketCatalogue) > 0) {
                                    if (!marketCatalogue.isIgnored()) {
//                                        toCheckMarkets.add(new SimpleEntry<>(marketId, marketCatalogue));
                                        toCheckMarkets.add(new SimpleEntry<>(marketId, existingMarketCatalogue));
                                    }
                                    nModifiedMarkets++;
                                }
                                marketCatalogue = existingMarketCatalogue;
                            }
                        } else {
                            if (!Statics.safeBetModuleActivated) {
                                logger.error("this branch should not be entered when safeBet module disabled");
                            }

                            long timeSinceLastRemoved = methodStartTime - Statics.scraperEventMaps.getNTimeStampRemoved();
                            String printedString = MessageFormatter.arrayFormat("no scraperEvent found in findMarkets timeSinceLastRemoved: {}ms for eventId: {} marketId: {} marketCatalogue: {}",
                                                                                new Object[]{timeSinceLastRemoved, eventId, marketId, Generic.objectToString(marketCatalogue)}).getMessage();
                            if (timeSinceLastRemoved < 1_000L) {
                                logger.info(printedString);
                            } else {
                                logger.error(printedString);
                            }

                            existingMarketCatalogue = Statics.marketCataloguesMap.get(marketId);
                            if (existingMarketCatalogue != null) {
                                if (!existingMarketCatalogue.isIgnored()) {
                                    logger.error("STRANGE Statics.marketCataloguesMap in findMarkets contained unmatched marketId: {}", marketId);
                                }

                                existingMarketCatalogue = null;
                            }
                        } // end else
//                        logger.info("before if: {} {} {}", marketId, checkAll, returnSet.size());
                        if (nMatched >= Statics.MIN_MATCHED && (checkAll || existingMarketCatalogue == null)) {
                            final String eventHomeNameString = event.getHomeName();
                            final String eventAwayNameString = event.getAwayName();
//                            if (eventId != null && eventHomeNameString != null && eventAwayNameString != null) {
                            if (eventId != null) {
                                final boolean interestingMarket = parseSupportedMarket(eventHomeNameString, eventAwayNameString, marketCatalogue, marketId, marketName, marketDescription, runnerCatalogsList);
//                                logger.info("interesting or not: {} {} {}", marketId, interestingMarket, nMatched);
                                if (interestingMarket) {
                                    if (nMatched >= Statics.MIN_MATCHED) { // double check, just in case an error slips through
                                        final MarketCatalogue inMapMarketCatalogue = Statics.marketCataloguesMap.putIfAbsent(marketId, marketCatalogue);
                                        if (inMapMarketCatalogue == null) { // marketCatalogue was added, no previous marketCatalogue existed
//                                            logger.info("added: {} size: {}", marketId, Statics.marketCataloguesMap.size());
                                            if (marketCatalogue.isIgnored(methodStartTime)) {
                                                logger.error("blackListed marketCatalogue added: {}", marketId);
                                            } else {
                                                toCheckMarkets.add(new SimpleEntry<>(marketId, marketCatalogue));
                                                nAddedMarkets++;
                                            }
                                        } else {
//                                            logger.info("exists: {} size: {}", marketId, Statics.marketCataloguesMap.size());
                                            // nothing will be done, update is done at beginning
                                            final long existingTimeStamp = inMapMarketCatalogue.getTimeStamp();
                                            final long currentTimeStamp = marketCatalogue.getTimeStamp();
                                            final long timeDifference = currentTimeStamp - existingTimeStamp;
                                            if (timeDifference > 1_000L && startedWaitingTime - existingTimeStamp > 1_000L) {
                                                Generic.alreadyPrintedMap.logOnce(Statics.newMarketSynchronizedWriter, logger, LogLevel.ERROR, "probably modified {}({}) ms parsedMarket for: {} {}", timeDifference, startedWaitingTime - existingTimeStamp,
                                                                                  Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"), Generic.objectToString(inMapMarketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
                                            } else { // can happen due to concurrent threads, no need to print message
                                            }
                                        }
                                    } else {
                                        logger.error("nMatched {} found in findMarkets for eventId: {} marketId: {} marketCatalogue: {}", nMatched, eventId, marketId, Generic.objectToString(marketCatalogue));

                                        if (Statics.marketCataloguesMap.containsKey(marketId)) {
                                            // will not remove, having a bad error message is good enough
                                            logger.error("STRANGE Statics.marketCataloguesMap in findMarkets contained unmatched marketId: {}", marketId);
                                        }
                                    } // end else
                                } else { // market not interesting, nothing to be done
                                }
                            } else {
//                                if (eventId == null) {
                                logger.error("null eventId in findMarkets for: {}", Generic.objectToString(marketCatalogue));
//                                } else if (Formulas.isMarketType(marketCatalogue, Statics.supportedEventTypes)) {
//                                    logger.info("null parsed name fields in findMarkets for: {} {}", marketId, event.getName());
//                                } else {
////                                    Generic.alreadyPrintedMap.logOnce(logger, LogLevel.INFO, "null parsed name fields in findMarkets for: {} {} {}", marketId, Generic.objectToString(marketCatalogue.getEventType()), event.getName());
//                                    Generic.alreadyPrintedMap.logOnce(logger, LogLevel.INFO, "null parsed name fields in findMarkets for: {}", Generic.objectToString(marketCatalogue.getEventType()));
//                                }
                            }
                        } else { // no associated scraperEvent or market exists and not checkAll, won't be parsed
                        }
                    } else {
                        if (event == null) {
                            final long timeSinceLastRemoved = methodStartTime - Statics.eventsMap.getTimeStampRemoved();
                            if (timeSinceLastRemoved < 1_000L) {
                                logger.info("marketCatalogue {} attached to event not present in eventsMap", marketId);
                            } else {
                                logger.error("marketCatalogue attached to event not present in eventsMap: {}", Generic.objectToString(marketCatalogue));
                            }
                        } else {
                            logger.error("null fields in findMarkets for: {}", Generic.objectToString(marketCatalogue));
                        }
                    }
                } // end for marketCatalogue

                final boolean fullRun = eventsSet == null;
                final String fullRunString = fullRun ? " fullRun" : "";
                final String checkAllString = checkAll ? " checkAll" : "";
                logger.info("findMarkets{}{}: {} in map, {} returned from {} matched events, in {} ms, of which {} ms waiting for threads", fullRunString, checkAllString, Statics.marketCataloguesMap.size(), returnSet.size(), eventsCopy.size(),
                            System.currentTimeMillis() - methodStartTime, endTime - startedWaitingTime);

                Statics.marketCataloguesMap.timeStamp();

                final int sizeModified = modifiedEvents.size();
                if (sizeModified > 0) {
                    final String printedString = MessageFormatter.arrayFormat("findMarkets{}{} modifiedEvents: {} launch: findMarkets", new Object[]{fullRunString, checkAllString, sizeModified}).getMessage();
                    if (fullRun) {
                        logger.warn(printedString);
                    } else {
                        logger.info(printedString);
                    }

                    final HashSet<Event> notIgnoredModifiedEvents = new HashSet<>(Generic.getCollectionCapacity(modifiedEvents));
                    for (Event event : modifiedEvents) {
                        if (!event.isIgnored()) {
                            notIgnoredModifiedEvents.add(event);
                        }
                    } // end for
                    if (notIgnoredModifiedEvents.size() > 0) {
                        Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, notIgnoredModifiedEvents));
                    }
                }

                if (Statics.safeBetModuleActivated) {
                    final int sizeEntries = toCheckMarkets.size();
                    if (sizeEntries > 0) {
                        final String printedString =
                                MessageFormatter.arrayFormat("findMarkets{}{} toCheckMarkets(modif/add): {}({}/{}) launch: findSafeRunners", new Object[]{fullRunString, checkAllString, sizeEntries, nModifiedMarkets, nAddedMarkets}).getMessage();
                        if (fullRun) {
                            if (nModifiedMarkets > 0) {
                                logger.warn(printedString); // sometimes, rarely, does happen
                            } else {
                                logger.info(printedString); // sometimes markets are added, presumably replacing others, so that the event's total nMarkets remains the same
                            }
                        } else {
                            logger.info(printedString);
                        }

                        final LinkedHashSet<Entry<String, MarketCatalogue>> notIgnoredToCheckMarkets = new LinkedHashSet<>(Generic.getCollectionCapacity(toCheckMarkets));
                        for (Entry<String, MarketCatalogue> entry : toCheckMarkets) {
                            MarketCatalogue value = entry.getValue();
                            if (!value.isIgnored()) {
                                notIgnoredToCheckMarkets.add(entry);
                            }
                        } // end for
                        if (notIgnoredToCheckMarkets.size() > 0) {
                            Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findSafeRunners, notIgnoredToCheckMarkets));
                        }
                    }
                }
            } else {
                final boolean fullRun = eventsSet == null;
                final String fullRunString = fullRun ? " fullRun" : "";
                final String checkAllString = checkAll ? " checkAll" : "";
                final String printedString = MessageFormatter.arrayFormat("findMarkets{}{}: no matched events to work with", new Object[]{fullRunString, checkAllString}).getMessage();
                if (fullRun) {
                    logger.info(printedString);
                } else {
                    logger.info(printedString); // this is info as well, not error; the set can become empty after removal at beginning of method
                }
            } // end else
        } else {
            logger.info("scraperEventsMap too old in findMarkets");
        }
    }

    public static boolean parseSupportedMarket(String eventHomeNameString, String eventAwayNameString, MarketCatalogue marketCatalogue, String marketId, String marketName, MarketDescription marketDescription, List<RunnerCatalog> runnerCatalogsList) {
        final boolean interestingMarket;
        if (Formulas.isMarketType(marketCatalogue, Statics.supportedEventTypes) && eventHomeNameString != null && eventAwayNameString != null) {
            final StringBuilder eventHomeName = new StringBuilder(eventHomeNameString);
            final StringBuilder eventAwayName = new StringBuilder(eventAwayNameString);
            final String marketType = marketDescription.getMarketType();
            ParsedMarketType parsedMarketType;
            int nParsedRunners;
            HashSet<ParsedRunner> parsedRunnersSet;
            boolean errorAlreadyPrinted = false;
            if (marketType == null) {
                switch (marketName) {
                    case "Second Half Correct Score":
                        parsedMarketType = ParsedMarketType.SH_CORRECT_SCORE;
                        nParsedRunners = 10;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("0 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_0;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("1 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_1;
                            } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("2 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_2;
                            } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("1 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_0;
                            } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("2 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_0;
                            } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("2 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_1;
                            } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("0 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_1;
                            } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("0 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_2;
                            } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("1 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_2;
                            } else if (sortPriority == 10 && runnerName.trim().equalsIgnoreCase("Any Unquoted")) {
                                parsedRunnerType = ParsedRunnerType.ANY_UNQUOTED;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "Half Time Score 2":
                        if (runnerCatalogsList.size() == 11) {
                            RunnerCatalog runnerCatalog = runnerCatalogsList.get(0);
                            if (runnerCatalog != null) {
                                final String runnerName = runnerCatalog.getRunnerName();
                                if (runnerName != null) {
                                    switch (runnerName) {
                                        case "3 - 0":
                                            parsedMarketType = ParsedMarketType.HALF_TIME_SCORE2_A;
                                            break;
                                        case "0 - 3":
                                            parsedMarketType = ParsedMarketType.HALF_TIME_SCORE2_B;
                                            break;
                                        default:
                                            parsedMarketType = null;
                                            break;
                                    } // end switch
                                } else {
                                    parsedMarketType = null;
                                }
                            } else {
                                parsedMarketType = null;
                            }
                        } else {
                            parsedMarketType = null;
                        }
                        if (parsedMarketType == null) {
                            interestingMarket = unknownParsedMarketTypeError(marketName, eventHomeName, eventAwayName, marketId, marketCatalogue);
                        } else {
                            nParsedRunners = 11;
                            parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                            for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                final int sortPriority = runnerCatalog.getSortPriority();
                                final String runnerName = runnerCatalog.getRunnerName();
                                final ParsedRunnerType parsedRunnerType;
                                if (sortPriority == 11 && runnerName.trim().equalsIgnoreCase("Any unquoted")) {
                                    parsedRunnerType = ParsedRunnerType.ANY_UNQUOTED;
                                } else if (parsedMarketType == ParsedMarketType.HALF_TIME_SCORE2_A) {
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("3 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_3_0;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("3 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_3_1;
                                    } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("4 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_4_0;
                                    } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("4 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_4_1;
                                    } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("5 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_5_0;
                                    } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("5 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_5_1;
                                    } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("6 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_6_0;
                                    } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("6 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_6_1;
                                    } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("7 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_7_0;
                                    } else if (sortPriority == 10 && runnerName.trim().equalsIgnoreCase("7 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_7_1;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                } else if (parsedMarketType == ParsedMarketType.HALF_TIME_SCORE2_B) {
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("0 - 3")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_3;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("1 - 3")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_3;
                                    } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("0 - 4")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_4;
                                    } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("1 - 4")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_4;
                                    } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("0 - 5")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_5;
                                    } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("1 - 5")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_5;
                                    } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("0 - 6")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_6;
                                    } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("1 - 6")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_6;
                                    } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("0 - 7")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_7;
                                    } else if (sortPriority == 10 && runnerName.trim().equalsIgnoreCase("1 - 7")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_7;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                } else {
                                    parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                    errorAlreadyPrinted = true;
                                    break;
                                }
                                addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                            } // end for
                            if (errorAlreadyPrinted) {
                                interestingMarket = false;
                            } else {
                                interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                            }
                        } // end else
                        break;
                    default:
                        if (!Statics.marketNullTypes.contains(marketName)) {
                            if (StringUtils.endsWithIgnoreCase(marketName, " First Goalscorer")) { // known but not parsed
                                parsedMarketType = pickParsedMarketType(" First Goalscorer", marketName, eventHomeName, eventAwayName, marketId, ParsedMarketType.FIRST_GOAL_SCORER_A,
                                                                        ParsedMarketType.FIRST_GOAL_SCORER_B); // parsedMarketType value is not used as I'm not parsing this market
//                                interestingMarket = false; // known but not parsed
                                interestingMarket = true; // not supported markets, with no errors, pass all the time
                            } else {
                                Generic.alreadyPrintedMap.logOnce(Statics.newMarketSynchronizedWriter, logger, LogLevel.ERROR, "new marketName found: {}, for: {}", marketName,
                                                                  Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
                                interestingMarket = false;
                            }
                        } else { // known market that is not parsed
//                            interestingMarket = false;
                            interestingMarket = true; // not supported markets, with no errors, pass all the time
                        }
                        break;
                } // end switch
            } else {
                switch (marketType) {
                    case "MATCH_ODDS_AND_BTTS":
                        parsedMarketType = ParsedMarketType.MATCH_ODDS_AND_BTTS;
                        nParsedRunners = 6;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final boolean successInGettingName = getNamesFromRunnerNames(eventHomeName, eventAwayName, marketId, runnerName, null, "/", sortPriority, 1, 2, marketCatalogue);
                            if (!successInGettingName) {
                                errorAlreadyPrinted = true;
                                break;
                            }
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.equalsIgnoreCase(eventHomeName + "/Yes")) {
                                parsedRunnerType = ParsedRunnerType.HOME_AND_YES;
                            } else if (sortPriority == 2 && runnerName.equalsIgnoreCase(eventAwayName + "/Yes")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_AND_YES;
                            } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("Draw/Yes")) {
                                parsedRunnerType = ParsedRunnerType.DRAW_AND_YES;
                            } else if (sortPriority == 4 && runnerName.equalsIgnoreCase(eventHomeName + "/No")) {
                                parsedRunnerType = ParsedRunnerType.HOME_AND_NO;
                            } else if (sortPriority == 5 && runnerName.equalsIgnoreCase(eventAwayName + "/No")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_AND_NO;
                            } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("Draw/No")) {
                                parsedRunnerType = ParsedRunnerType.DRAW_AND_NO;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "LAST_TEAM_TO_SCORE":
                        parsedMarketType = ParsedMarketType.LAST_TEAM_TO_SCORE;
                        nParsedRunners = 3;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final boolean successInGettingName = getNamesFromRunnerNames(eventHomeName, eventAwayName, marketId, runnerName, null, null, sortPriority, 1, 2, marketCatalogue);
                            if (!successInGettingName) {
                                errorAlreadyPrinted = true;
                                break;
                            }
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.equalsIgnoreCase(eventHomeName.toString())) {
                                parsedRunnerType = ParsedRunnerType.HOME_SCORES;
                            } else if (sortPriority == 2 && runnerName.equalsIgnoreCase(eventAwayName.toString())) {
                                parsedRunnerType = ParsedRunnerType.AWAY_SCORES;
                            } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("No Goal")) {
                                parsedRunnerType = ParsedRunnerType.NO_GOAL;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "MATCH_ODDS_AND_OU_25":
                        parsedMarketType = ParsedMarketType.MATCH_ODDS_AND_OU_25;
                        nParsedRunners = 6;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final boolean successInGettingName = getNamesFromRunnerNames(eventHomeName, eventAwayName, marketId, runnerName, null, "/", sortPriority, 1, 3, marketCatalogue);
                            if (!successInGettingName) {
                                errorAlreadyPrinted = true;
                                break;
                            }
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.equalsIgnoreCase(eventHomeName + "/Under 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.HOME_AND_UNDER;
                            } else if (sortPriority == 2 && runnerName.equalsIgnoreCase(eventHomeName + "/Over 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.HOME_AND_OVER;
                            } else if (sortPriority == 3 && runnerName.equalsIgnoreCase(eventAwayName + "/Under 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_AND_UNDER;
                            } else if (sortPriority == 4 && runnerName.equalsIgnoreCase(eventAwayName + "/Over 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_AND_OVER;
                            } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("Draw/Under 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.DRAW_AND_UNDER;
                            } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("Draw/Over 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.DRAW_AND_OVER;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "MATCH_ODDS_AND_OU_35":
                        parsedMarketType = ParsedMarketType.MATCH_ODDS_AND_OU_35;
                        nParsedRunners = 6;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final boolean successInGettingName = getNamesFromRunnerNames(eventHomeName, eventAwayName, marketId, runnerName, null, "/", sortPriority, 1, 3, marketCatalogue);
                            if (!successInGettingName) {
                                errorAlreadyPrinted = true;
                                break;
                            }
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.equalsIgnoreCase(eventHomeName + "/Under 3.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.HOME_AND_UNDER;
                            } else if (sortPriority == 2 && runnerName.equalsIgnoreCase(eventHomeName + "/Over 3.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.HOME_AND_OVER;
                            } else if (sortPriority == 3 && runnerName.equalsIgnoreCase(eventAwayName + "/Under 3.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_AND_UNDER;
                            } else if (sortPriority == 4 && runnerName.equalsIgnoreCase(eventAwayName + "/Over 3.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_AND_OVER;
                            } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("Draw/Under 3.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.DRAW_AND_UNDER;
                            } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("Draw/Over 3.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.DRAW_AND_OVER;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "HALF_TIME_FULL_TIME":
                        parsedMarketType = ParsedMarketType.HALF_TIME_FULL_TIME;
                        nParsedRunners = 9;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final boolean successInGettingName = getNamesFromRunnerNames(eventHomeName, eventAwayName, marketId, runnerName, "/", null, sortPriority, 1, 3, marketCatalogue);
                            if (!successInGettingName) {
                                errorAlreadyPrinted = true;
                                break;
                            }
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.equalsIgnoreCase(eventHomeName + "/" + eventHomeName)) {
                                parsedRunnerType = ParsedRunnerType.HOME_AND_HOME;
                            } else if (sortPriority == 2 && runnerName.equalsIgnoreCase(eventHomeName + "/Draw")) {
                                parsedRunnerType = ParsedRunnerType.HOME_AND_DRAW;
                            } else if (sortPriority == 3 && runnerName.equalsIgnoreCase(eventHomeName + "/" + eventAwayName)) {
                                parsedRunnerType = ParsedRunnerType.HOME_AND_AWAY;
                            } else if (sortPriority == 4 && runnerName.equalsIgnoreCase("Draw/" + eventHomeName)) {
                                parsedRunnerType = ParsedRunnerType.DRAW_AND_HOME;
                            } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("Draw/Draw")) {
                                parsedRunnerType = ParsedRunnerType.DRAW_AND_DRAW;
                            } else if (sortPriority == 6 && runnerName.equalsIgnoreCase("Draw/" + eventAwayName)) {
                                parsedRunnerType = ParsedRunnerType.DRAW_AND_AWAY;
                            } else if (sortPriority == 7 && runnerName.equalsIgnoreCase(eventAwayName + "/" + eventHomeName)) {
                                parsedRunnerType = ParsedRunnerType.AWAY_AND_HOME;
                            } else if (sortPriority == 8 && runnerName.equalsIgnoreCase(eventAwayName + "/Draw")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_AND_DRAW;
                            } else if (sortPriority == 9 && runnerName.equalsIgnoreCase(eventAwayName + "/" + eventAwayName)) {
                                parsedRunnerType = ParsedRunnerType.AWAY_AND_AWAY;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "CORRECT_SCORE":
                        parsedMarketType = ParsedMarketType.CORRECT_SCORE;
//                                            nParsedRunners = 17;
                        nParsedRunners = 19;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final long selectionId = runnerCatalog.getSelectionId();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (selectionId == 1 && runnerName.trim().equalsIgnoreCase("0 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_0;
                            } else if (selectionId == 4 && runnerName.trim().equalsIgnoreCase("0 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_1;
                            } else if (selectionId == 9 && runnerName.trim().equalsIgnoreCase("0 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_2;
                            } else if (selectionId == 16 && runnerName.trim().equalsIgnoreCase("0 - 3")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_3;
                            } else if (selectionId == 2 && runnerName.trim().equalsIgnoreCase("1 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_0;
                            } else if (selectionId == 3 && runnerName.trim().equalsIgnoreCase("1 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_1;
                            } else if (selectionId == 8 && runnerName.trim().equalsIgnoreCase("1 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_2;
                            } else if (selectionId == 15 && runnerName.trim().equalsIgnoreCase("1 - 3")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_3;
                            } else if (selectionId == 5 && runnerName.trim().equalsIgnoreCase("2 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_0;
                            } else if (selectionId == 6 && runnerName.trim().equalsIgnoreCase("2 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_1;
                            } else if (selectionId == 7 && runnerName.trim().equalsIgnoreCase("2 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_2;
                            } else if (selectionId == 14 && runnerName.trim().equalsIgnoreCase("2 - 3")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_3;
                            } else if (selectionId == 10 && runnerName.trim().equalsIgnoreCase("3 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_3_0;
                            } else if (selectionId == 11 && runnerName.trim().equalsIgnoreCase("3 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_3_1;
                            } else if (selectionId == 12 && runnerName.trim().equalsIgnoreCase("3 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_3_2;
                            } else if (selectionId == 13 && runnerName.trim().equalsIgnoreCase("3 - 3")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_3_3;
                            } else if (selectionId == 9063254L && runnerName.trim().equalsIgnoreCase("Any Other Home Win")) {
                                parsedRunnerType = ParsedRunnerType.ANY_OTHER_HOME_WIN;
                            } else if (selectionId == 9063255L && runnerName.trim().equalsIgnoreCase("Any Other Away Win")) {
                                parsedRunnerType = ParsedRunnerType.ANY_OTHER_AWAY_WIN;
                            } else if (selectionId == 9063256L && runnerName.trim().equalsIgnoreCase("Any Other Draw")) {
                                parsedRunnerType = ParsedRunnerType.ANY_OTHER_DRAW;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, selectionId, runnerName, marketId, marketCatalogue);
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "ANYTIME_SCORE":
                        parsedMarketType = ParsedMarketType.ANYTIME_SCORE;
                        nParsedRunners = 15;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("0 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_1;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("0 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_2;
                            } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("0 - 3")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_3;
                            } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("1 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_0;
                            } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("1 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_1;
                            } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("1 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_2;
                            } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("1 - 3")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_3;
                            } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("2 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_0;
                            } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("2 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_1;
                            } else if (sortPriority == 10 && runnerName.trim().equalsIgnoreCase("2 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_2;
                            } else if (sortPriority == 11 && runnerName.trim().equalsIgnoreCase("2 - 3")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_3;
                            } else if (sortPriority == 12 && runnerName.trim().equalsIgnoreCase("3 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_3_0;
                            } else if (sortPriority == 13 && runnerName.trim().equalsIgnoreCase("3 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_3_1;
                            } else if (sortPriority == 14 && runnerName.trim().equalsIgnoreCase("3 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_3_2;
                            } else if (sortPriority == 15 && runnerName.trim().equalsIgnoreCase("3 - 3")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_3_3;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "HALF_TIME_SCORE":
                        parsedMarketType = ParsedMarketType.HALF_TIME_SCORE;
                        nParsedRunners = 10;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("0 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_0;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("1 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_1;
                            } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("2 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_2;
                            } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("1 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_0;
                            } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("2 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_0;
                            } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("2 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_1;
                            } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("0 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_1;
                            } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("0 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_2;
                            } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("1 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_2;
                            } else if (sortPriority == 10 && runnerName.trim().equalsIgnoreCase("Any Unquoted")) {
                                parsedRunnerType = ParsedRunnerType.ANY_UNQUOTED;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TOTAL_GOALS":
                        parsedMarketType = ParsedMarketType.TOTAL_GOALS;
                        nParsedRunners = 7;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("1 goals or more")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_1_OR_MORE;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("2 goals or more")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_2_OR_MORE;
                            } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("3 goals or more")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_3_OR_MORE;
                            } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("4 goals or more")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_4_OR_MORE;
                            } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("5 goals or more")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_5_OR_MORE;
                            } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("6 goals or more")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_6_OR_MORE;
                            } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("7 goals or more")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_7_OR_MORE;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "EXACT_GOALS":
                        parsedMarketType = ParsedMarketType.EXACT_GOALS;
                        nParsedRunners = 8;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("0 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_0;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("1 Goal")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_1;
                            } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("2 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_2;
                            } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("3 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_3;
                            } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("4 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_4;
                            } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_5;
                            } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("6 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_6;
                            } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("7+ Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_7_PLUS;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TEAM_TOTAL_GOALS":
                        parsedMarketType = pickParsedMarketType(" Total Goals", marketName, eventHomeName, eventAwayName, marketId, ParsedMarketType.TEAM_TOTAL_GOALS_A, ParsedMarketType.TEAM_TOTAL_GOALS_B);
                        if (parsedMarketType == null) {
                            interestingMarket = unknownParsedMarketTypeError(marketName, eventHomeName, eventAwayName, marketId, marketCatalogue);
                        } else {
                            nParsedRunners = 7;
                            parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                            for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                final int sortPriority = runnerCatalog.getSortPriority();
                                final String runnerName = runnerCatalog.getRunnerName();
                                final ParsedRunnerType parsedRunnerType;
                                if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("1 goals or more")) {
                                    parsedRunnerType = ParsedRunnerType.GOALS_1_OR_MORE;
                                } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("2 goals or more")) {
                                    parsedRunnerType = ParsedRunnerType.GOALS_2_OR_MORE;
                                } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("3 goals or more")) {
                                    parsedRunnerType = ParsedRunnerType.GOALS_3_OR_MORE;
                                } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("4 goals or more")) {
                                    parsedRunnerType = ParsedRunnerType.GOALS_4_OR_MORE;
                                } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("5 goals or more")) {
                                    parsedRunnerType = ParsedRunnerType.GOALS_5_OR_MORE;
                                } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("6 goals or more")) {
                                    parsedRunnerType = ParsedRunnerType.GOALS_6_OR_MORE;
                                } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("7 goals or more")) {
                                    parsedRunnerType = ParsedRunnerType.GOALS_7_OR_MORE;
                                } else {
                                    parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                    errorAlreadyPrinted = true;
                                    break;
                                }
                                addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                            } // end for
                            if (errorAlreadyPrinted) {
                                interestingMarket = false;
                            } else {
                                interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                            }
                        } // end else
                        break;
                    case "CORRECT_SCORE2":
                        switch (marketName) {
                            case "Correct Score 2 Home":
                                parsedMarketType = ParsedMarketType.CORRECT_SCORE2_A;
                                break;
                            case "Correct Score 2 Away":
                                parsedMarketType = ParsedMarketType.CORRECT_SCORE2_B;
                                break;
                            case "Correct Score 3 Home":
                                parsedMarketType = ParsedMarketType.CORRECT_SCORE3_A;
                                break;
                            case "Correct Score 3 Away":
                                parsedMarketType = ParsedMarketType.CORRECT_SCORE3_B;
                                break;
                            default:
                                parsedMarketType = null;
                                break;
                        } // end switch
                        if (parsedMarketType == null) {
                            interestingMarket = unknownParsedMarketTypeError(marketName, eventHomeName, eventAwayName, marketId, marketCatalogue);
                        } else {
                            nParsedRunners = 13;
                            parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                            for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                final int sortPriority = runnerCatalog.getSortPriority();
                                final String runnerName = runnerCatalog.getRunnerName();
                                final ParsedRunnerType parsedRunnerType;
                                if (sortPriority == 13 && runnerName.trim().equalsIgnoreCase("Any unquoted")) {
                                    parsedRunnerType = ParsedRunnerType.ANY_UNQUOTED;
                                } else if (parsedMarketType == ParsedMarketType.CORRECT_SCORE2_A) {
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("4 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_4_0;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("4 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_4_1;
                                    } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("4 - 2")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_4_2;
                                    } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("5 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_5_0;
                                    } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("5 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_5_1;
                                    } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("5 - 2")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_5_2;
                                    } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("6 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_6_0;
                                    } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("6 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_6_1;
                                    } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("6 - 2")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_6_2;
                                    } else if (sortPriority == 10 && runnerName.trim().equalsIgnoreCase("7 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_7_0;
                                    } else if (sortPriority == 11 && runnerName.trim().equalsIgnoreCase("7 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_7_1;
                                    } else if (sortPriority == 12 && runnerName.trim().equalsIgnoreCase("7 - 2")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_7_2;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                } else if (parsedMarketType == ParsedMarketType.CORRECT_SCORE2_B) {
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("0 - 4")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_4;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("1 - 4")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_4;
                                    } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("2 - 4")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_2_4;
                                    } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("0 - 5")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_5;
                                    } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("1 - 5")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_5;
                                    } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("2 - 5")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_2_5;
                                    } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("0 - 6")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_6;
                                    } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("1 - 6")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_6;
                                    } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("2 - 6")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_2_6;
                                    } else if (sortPriority == 10 && runnerName.trim().equalsIgnoreCase("0 - 7")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_7;
                                    } else if (sortPriority == 11 && runnerName.trim().equalsIgnoreCase("1 - 7")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_7;
                                    } else if (sortPriority == 12 && runnerName.trim().equalsIgnoreCase("2 - 7")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_2_7;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                } else if (parsedMarketType == ParsedMarketType.CORRECT_SCORE3_A) {
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("8 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_8_0;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("8 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_8_1;
                                    } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("8 - 2")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_8_2;
                                    } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("9 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_9_0;
                                    } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("9 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_9_1;
                                    } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("9 - 2")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_9_2;
                                    } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("10 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_10_0;
                                    } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("10 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_10_1;
                                    } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("10 - 2")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_10_2;
                                    } else if (sortPriority == 10 && runnerName.trim().equalsIgnoreCase("11 - 0")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_11_0;
                                    } else if (sortPriority == 11 && runnerName.trim().equalsIgnoreCase("11 - 1")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_11_1;
                                    } else if (sortPriority == 12 && runnerName.trim().equalsIgnoreCase("11 - 2")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_11_2;
                                    } else if (sortPriority == 13 && runnerName.trim().equalsIgnoreCase("Any unquoted")) {
                                        parsedRunnerType = ParsedRunnerType.ANY_UNQUOTED;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                } else if (parsedMarketType == ParsedMarketType.CORRECT_SCORE3_B) {
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("0 - 8")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_8;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("1 - 8")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_8;
                                    } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("2 - 8")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_2_8;
                                    } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("0 - 9")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_9;
                                    } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("1 - 9")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_9;
                                    } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("2 - 9")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_2_9;
                                    } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("0 - 10")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_10;
                                    } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("1 - 10")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_10;
                                    } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("2 - 10")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_2_10;
                                    } else if (sortPriority == 10 && runnerName.trim().equalsIgnoreCase("0 - 11")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_0_11;
                                    } else if (sortPriority == 11 && runnerName.trim().equalsIgnoreCase("1 - 11")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_1_11;
                                    } else if (sortPriority == 12 && runnerName.trim().equalsIgnoreCase("2 - 11")) {
                                        parsedRunnerType = ParsedRunnerType.SCORE_2_11;
                                    } else if (sortPriority == 13 && runnerName.trim().equalsIgnoreCase("Any unquoted")) {
                                        parsedRunnerType = ParsedRunnerType.ANY_UNQUOTED;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                } else {
                                    parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                    errorAlreadyPrinted = true;
                                    break;
                                }
                                addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                            } // end for
                            if (errorAlreadyPrinted) {
                                interestingMarket = false;
                            } else {
                                interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                            }
                        } // end else
                        break;
                    case "WINNING_MARGIN":
                        parsedMarketType = ParsedMarketType.WINNING_MARGIN;
                        nParsedRunners = 10;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final boolean successInGettingName = getNamesFromRunnerNames(eventHomeName, eventAwayName, marketId, runnerName, null, " by ", sortPriority, 1, 5, marketCatalogue);
                            if (!successInGettingName) {
                                errorAlreadyPrinted = true;
                                break;
                            }
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.equalsIgnoreCase(eventHomeName + " by 1")) {
                                parsedRunnerType = ParsedRunnerType.HOME_WIN_BY_1;
                            } else if (sortPriority == 2 && runnerName.equalsIgnoreCase(eventHomeName + " by 2")) {
                                parsedRunnerType = ParsedRunnerType.HOME_WIN_BY_2;
                            } else if (sortPriority == 3 && runnerName.equalsIgnoreCase(eventHomeName + " by 3")) {
                                parsedRunnerType = ParsedRunnerType.HOME_WIN_BY_3;
                            } else if (sortPriority == 4 && runnerName.equalsIgnoreCase(eventHomeName + " by 4+")) {
                                parsedRunnerType = ParsedRunnerType.HOME_WIN_BY_4_PLUS;
                            } else if (sortPriority == 5 && runnerName.equalsIgnoreCase(eventAwayName + " by 1")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_WIN_BY_1;
                            } else if (sortPriority == 6 && runnerName.equalsIgnoreCase(eventAwayName + " by 2")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_WIN_BY_2;
                            } else if (sortPriority == 7 && runnerName.equalsIgnoreCase(eventAwayName + " by 3")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_WIN_BY_3;
                            } else if (sortPriority == 8 && runnerName.equalsIgnoreCase(eventAwayName + " by 4+")) {
                                parsedRunnerType = ParsedRunnerType.AWAY_WIN_BY_4_PLUS;
                            } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("Score Draw")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_DRAW;
                            } else if (sortPriority == 10 && (runnerName.trim().equalsIgnoreCase("No Goals") || runnerName.trim().equalsIgnoreCase("No Goal"))) {
                                parsedRunnerType = ParsedRunnerType.NO_GOALS;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "HALF_WITH_MOST_GOALS":
                        parsedMarketType = ParsedMarketType.HALF_WITH_MOST_GOALS;
                        nParsedRunners = 3;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("1st Half")) {
                                parsedRunnerType = ParsedRunnerType.FIRST_HALF;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("2nd Half")) {
                                parsedRunnerType = ParsedRunnerType.SECOND_HALF;
                            } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("Tie")) {
                                parsedRunnerType = ParsedRunnerType.TIE;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "METHOD_OF_VICTORY":
                        parsedMarketType = ParsedMarketType.METHOD_OF_VICTORY;
                        final int nRunners = marketCatalogue.getNRunners();
                        final int sortAway;
                        if (nRunners == 6) {
                            nParsedRunners = 6;
                            sortAway = 4;
                        } else if (nRunners == 4) {
                            nParsedRunners = 4;
                            sortAway = 3;
                        } else {
                            logger.error("wrong nRunners {} in findMarkets for: {} {}", nRunners, parsedMarketType, Generic.objectToString(marketCatalogue));
                            nParsedRunners = 0;
                            sortAway = 0;
                        }
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final boolean successInGettingName = getNamesFromRunnerNames(eventHomeName, eventAwayName, marketId, runnerName, null, " 90 ", sortPriority, 1, sortAway, marketCatalogue);
                            if (!successInGettingName) {
                                errorAlreadyPrinted = true;
                                break;
                            }
                            final ParsedRunnerType parsedRunnerType;
                            if (nParsedRunners == 6) {
                                if (sortPriority == 1 && runnerName.equalsIgnoreCase(eventHomeName + " 90 Minutes")) {
                                    parsedRunnerType = ParsedRunnerType.HOME_90_MINUTES;
                                } else if (sortPriority == 2 && runnerName.equalsIgnoreCase(eventHomeName + " Extra Time")) {
                                    parsedRunnerType = ParsedRunnerType.HOME_ET;
                                } else if (sortPriority == 3 && runnerName.equalsIgnoreCase(eventHomeName + " Penalties")) {
                                    parsedRunnerType = ParsedRunnerType.HOME_PENALTIES;
                                } else if (sortPriority == 4 && runnerName.equalsIgnoreCase(eventAwayName + " 90 Minutes")) {
                                    parsedRunnerType = ParsedRunnerType.AWAY_90_MINUTES;
                                } else if (sortPriority == 5 && runnerName.equalsIgnoreCase(eventAwayName + " Extra Time")) {
                                    parsedRunnerType = ParsedRunnerType.AWAY_ET;
                                } else if (sortPriority == 6 && runnerName.equalsIgnoreCase(eventAwayName + " Penalties")) {
                                    parsedRunnerType = ParsedRunnerType.AWAY_PENALTIES;
                                } else {
                                    parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                    errorAlreadyPrinted = true;
                                    break;
                                }
                            } else if (nParsedRunners == 4) {
                                if (sortPriority == 1 && runnerName.equalsIgnoreCase(eventHomeName + " 90 Minutes")) {
                                    parsedRunnerType = ParsedRunnerType.HOME_90_MINUTES;
                                } else if (sortPriority == 2 && runnerName.equalsIgnoreCase(eventHomeName + " Penalties")) {
                                    parsedRunnerType = ParsedRunnerType.HOME_PENALTIES;
                                } else if (sortPriority == 3 && runnerName.equalsIgnoreCase(eventAwayName + " 90 Minutes")) {
                                    parsedRunnerType = ParsedRunnerType.AWAY_90_MINUTES;
                                } else if (sortPriority == 4 && runnerName.equalsIgnoreCase(eventAwayName + " Penalties")) {
                                    parsedRunnerType = ParsedRunnerType.AWAY_PENALTIES;
                                } else {
                                    parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                    errorAlreadyPrinted = true;
                                    break;
                                }
                            } else { // should never enter on this branch
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TO_SCORE_BOTH_HALVES":
                        parsedMarketType = pickParsedMarketType(" To Score in Both Halves", marketName, eventHomeName, eventAwayName, marketId, ParsedMarketType.TO_SCORE_BOTH_HALVES_A, ParsedMarketType.TO_SCORE_BOTH_HALVES_B);
                        if (parsedMarketType == null) {
                            interestingMarket = unknownParsedMarketTypeError(marketName, eventHomeName, eventAwayName, marketId, marketCatalogue);
                        } else {
                            nParsedRunners = 2;
                            parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                            for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                final int sortPriority = runnerCatalog.getSortPriority();
                                final String runnerName = runnerCatalog.getRunnerName();
                                final ParsedRunnerType parsedRunnerType;
                                if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Yes")) {
                                    parsedRunnerType = ParsedRunnerType.YES;
                                } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("No")) {
                                    parsedRunnerType = ParsedRunnerType.NO;
                                } else {
                                    parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                    errorAlreadyPrinted = true;
                                    break;
                                }
                                addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                            }
                            if (errorAlreadyPrinted) {
                                interestingMarket = false;
                            } else {
                                interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                            }
                        } // end for
                        break;
                    case "WIN_HALF":
                        parsedMarketType = pickParsedMarketType(" Win a Half?", marketName, eventHomeName, eventAwayName, marketId, ParsedMarketType.WIN_HALF_A, ParsedMarketType.WIN_HALF_B);
                        if (parsedMarketType == null) {
                            interestingMarket = unknownParsedMarketTypeError(marketName, eventHomeName, eventAwayName, marketId, marketCatalogue);
                        } else {
                            nParsedRunners = 2;
                            parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                            for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                final int sortPriority = runnerCatalog.getSortPriority();
                                final String runnerName = runnerCatalog.getRunnerName();
                                final ParsedRunnerType parsedRunnerType;
                                if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Yes")) {
                                    parsedRunnerType = ParsedRunnerType.YES;
                                } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("No")) {
                                    parsedRunnerType = ParsedRunnerType.NO;
                                } else {
                                    parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                    errorAlreadyPrinted = true;
                                    break;
                                }
                                addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                            }
                            if (errorAlreadyPrinted) {
                                interestingMarket = false;
                            } else {
                                interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                            }
                        } // end for
                        break;
                    case "GOAL_BOTH_HALVES":
                        parsedMarketType = ParsedMarketType.GOAL_BOTH_HALVES;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Yes")) {
                                parsedRunnerType = ParsedRunnerType.YES;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("No")) {
                                parsedRunnerType = ParsedRunnerType.NO;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TEAM_A_WIN_TO_NIL":
                        parsedMarketType = ParsedMarketType.TEAM_A_WIN_TO_NIL;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Yes")) {
                                parsedRunnerType = ParsedRunnerType.YES;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("No")) {
                                parsedRunnerType = ParsedRunnerType.NO;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TEAM_B_WIN_TO_NIL":
                        parsedMarketType = ParsedMarketType.TEAM_B_WIN_TO_NIL;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Yes")) {
                                parsedRunnerType = ParsedRunnerType.YES;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("No")) {
                                parsedRunnerType = ParsedRunnerType.NO;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "WIN_BOTH_HALVES":
                        parsedMarketType = pickParsedMarketType(" Win both Halves", marketName, eventHomeName, eventAwayName, marketId, ParsedMarketType.WIN_BOTH_HALVES_A, ParsedMarketType.WIN_BOTH_HALVES_B);
                        if (parsedMarketType == null) {
                            interestingMarket = unknownParsedMarketTypeError(marketName, eventHomeName, eventAwayName, marketId, marketCatalogue);
                        } else {
                            nParsedRunners = 2;
                            parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                            for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                final int sortPriority = runnerCatalog.getSortPriority();
                                final String runnerName = runnerCatalog.getRunnerName();
                                final ParsedRunnerType parsedRunnerType;
                                if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Yes")) {
                                    parsedRunnerType = ParsedRunnerType.YES;
                                } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("No")) {
                                    parsedRunnerType = ParsedRunnerType.NO;
                                } else {
                                    parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                    errorAlreadyPrinted = true;
                                    break;
                                }
                                addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                            } // end for
                            if (errorAlreadyPrinted) {
                                interestingMarket = false;
                            } else {
                                interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                            }
                        }
                        break;
                    case "CLEAN_SHEET":
                        String endMarker;
                        if (marketName.endsWith("?")) {
                            endMarker = " Clean Sheet?";
                        } else {
                            endMarker = " Clean Sheet";
                        }
                        parsedMarketType = pickParsedMarketType(endMarker, marketName, eventHomeName, eventAwayName, marketId, ParsedMarketType.CLEAN_SHEET_A, ParsedMarketType.CLEAN_SHEET_B);
                        if (parsedMarketType == null) {
                            interestingMarket = unknownParsedMarketTypeError(marketName, eventHomeName, eventAwayName, marketId, marketCatalogue);
                        } else {
                            nParsedRunners = 2;
                            parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                            for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                final int sortPriority = runnerCatalog.getSortPriority();
                                final String runnerName = runnerCatalog.getRunnerName();
                                final ParsedRunnerType parsedRunnerType;
                                if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Yes")) {
                                    parsedRunnerType = ParsedRunnerType.YES;
                                } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("No")) {
                                    parsedRunnerType = ParsedRunnerType.NO;
                                } else {
                                    parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                    errorAlreadyPrinted = true;
                                    break;
                                }
                                addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                            } // end for
                            if (errorAlreadyPrinted) {
                                interestingMarket = false;
                            } else {
                                interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                            }
                        }
                        break;
                    case "BOTH_TEAMS_TO_SCORE":
                        parsedMarketType = ParsedMarketType.BOTH_TEAMS_TO_SCORE;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Yes")) {
                                parsedRunnerType = ParsedRunnerType.YES;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("No")) {
                                parsedRunnerType = ParsedRunnerType.NO;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "FIRST_HALF_GOALS_05":
                        parsedMarketType = ParsedMarketType.FIRST_HALF_GOALS_05;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 0.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_05;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 0.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_05;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "FIRST_HALF_GOALS_15":
                        parsedMarketType = ParsedMarketType.FIRST_HALF_GOALS_15;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 1.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_15;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 1.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_15;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "FIRST_HALF_GOALS_25":
                        parsedMarketType = ParsedMarketType.FIRST_HALF_GOALS_25;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_25;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_25;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "SECOND_HALF_GOALS_05":
                        parsedMarketType = ParsedMarketType.SECOND_HALF_GOALS_05;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 0.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_05;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 0.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_05;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "SECOND_HALF_GOALS_15":
                        parsedMarketType = ParsedMarketType.SECOND_HALF_GOALS_15;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 1.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_15;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 1.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_15;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "OVER_UNDER_05":
                        parsedMarketType = ParsedMarketType.OVER_UNDER_05;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 0.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_05;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 0.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_05;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "OVER_UNDER_15":
                        parsedMarketType = ParsedMarketType.OVER_UNDER_15;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 1.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_15;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 1.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_15;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "OVER_UNDER_25":
                        parsedMarketType = ParsedMarketType.OVER_UNDER_25;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_25;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_25;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "OVER_UNDER_35":
                        parsedMarketType = ParsedMarketType.OVER_UNDER_35;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 3.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_35;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 3.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_35;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "OVER_UNDER_45":
                        parsedMarketType = ParsedMarketType.OVER_UNDER_45;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 4.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_45;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 4.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_45;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "OVER_UNDER_55":
                        parsedMarketType = ParsedMarketType.OVER_UNDER_55;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 5.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_55;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 5.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_55;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "OVER_UNDER_65":
                        parsedMarketType = ParsedMarketType.OVER_UNDER_65;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 6.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_65;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 6.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_65;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "OVER_UNDER_75":
                        parsedMarketType = ParsedMarketType.OVER_UNDER_75;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 7.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_75;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 7.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_75;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "OVER_UNDER_85":
                        parsedMarketType = ParsedMarketType.OVER_UNDER_85;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 8.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_85;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 8.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_85;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TEAM_A_OVER_UNDER_05":
                        parsedMarketType = ParsedMarketType.TEAM_A_OVER_UNDER_05;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 0.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_05;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 0.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_05;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TEAM_A_OVER_UNDER_15":
                        parsedMarketType = ParsedMarketType.TEAM_A_OVER_UNDER_15;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 1.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_15;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 1.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_15;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TEAM_A_OVER_UNDER_25":
                        parsedMarketType = ParsedMarketType.TEAM_A_OVER_UNDER_25;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_25;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_25;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TEAM_B_OVER_UNDER_05":
                        parsedMarketType = ParsedMarketType.TEAM_B_OVER_UNDER_05;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 0.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_05;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 0.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_05;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TEAM_B_OVER_UNDER_15":
                        parsedMarketType = ParsedMarketType.TEAM_B_OVER_UNDER_15;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 1.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_15;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 1.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_15;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "TEAM_B_OVER_UNDER_25":
                        parsedMarketType = ParsedMarketType.TEAM_B_OVER_UNDER_25;
                        nParsedRunners = 2;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_UNDER_25;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 2.5 Goals")) {
                                parsedRunnerType = ParsedRunnerType.GOALS_OVER_25;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    case "OVER_UNDER":
                        switch (marketName) {
                            case "Over/Under 9.5 Goals":
                                parsedMarketType = ParsedMarketType.OVER_UNDER_95;
                                nParsedRunners = 2;
                                parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                                for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                    final int sortPriority = runnerCatalog.getSortPriority();
                                    final String runnerName = runnerCatalog.getRunnerName();
                                    final ParsedRunnerType parsedRunnerType;
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 9.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_UNDER_95;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 9.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_OVER_95;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                    addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                                } // end for
                                if (errorAlreadyPrinted) {
                                    interestingMarket = false;
                                } else {
                                    interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                                }
                                break;
                            case "Over/Under 10.5 Goals":
                                parsedMarketType = ParsedMarketType.OVER_UNDER_105;
                                nParsedRunners = 2;
                                parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                                for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                    final int sortPriority = runnerCatalog.getSortPriority();
                                    final String runnerName = runnerCatalog.getRunnerName();
                                    final ParsedRunnerType parsedRunnerType;
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 10.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_UNDER_105;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 10.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_OVER_105;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                    addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                                } // end for
                                if (errorAlreadyPrinted) {
                                    interestingMarket = false;
                                } else {
                                    interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                                }
                                break;
                            case "Over/Under 11.5 Goals":
                                parsedMarketType = ParsedMarketType.OVER_UNDER_115;
                                nParsedRunners = 2;
                                parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                                for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                    final int sortPriority = runnerCatalog.getSortPriority();
                                    final String runnerName = runnerCatalog.getRunnerName();
                                    final ParsedRunnerType parsedRunnerType;
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 11.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_UNDER_115;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 11.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_OVER_115;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                    addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                                } // end for
                                if (errorAlreadyPrinted) {
                                    interestingMarket = false;
                                } else {
                                    interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                                }
                                break;
                            case "Over/Under 12.5 Goals":
                                parsedMarketType = ParsedMarketType.OVER_UNDER_125;
                                nParsedRunners = 2;
                                parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                                for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                    final int sortPriority = runnerCatalog.getSortPriority();
                                    final String runnerName = runnerCatalog.getRunnerName();
                                    final ParsedRunnerType parsedRunnerType;
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 12.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_UNDER_125;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 12.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_OVER_125;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                    addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                                } // end for
                                if (errorAlreadyPrinted) {
                                    interestingMarket = false;
                                } else {
                                    interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                                }
                                break;
                            case "Over/Under 13.5 Goals":
                                parsedMarketType = ParsedMarketType.OVER_UNDER_135;
                                nParsedRunners = 2;
                                parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                                for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                    final int sortPriority = runnerCatalog.getSortPriority();
                                    final String runnerName = runnerCatalog.getRunnerName();
                                    final ParsedRunnerType parsedRunnerType;
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 13.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_UNDER_135;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 13.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_OVER_135;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                    addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                                } // end for
                                if (errorAlreadyPrinted) {
                                    interestingMarket = false;
                                } else {
                                    interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                                }
                                break;
                            case "Over/Under 14.5 Goals":
                                parsedMarketType = ParsedMarketType.OVER_UNDER_145;
                                nParsedRunners = 2;
                                parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                                for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                                    final int sortPriority = runnerCatalog.getSortPriority();
                                    final String runnerName = runnerCatalog.getRunnerName();
                                    final ParsedRunnerType parsedRunnerType;
                                    if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("Under 14.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_UNDER_145;
                                    } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("Over 14.5 Goals")) {
                                        parsedRunnerType = ParsedRunnerType.GOALS_OVER_145;
                                    } else {
                                        parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                        errorAlreadyPrinted = true;
                                        break;
                                    }
                                    addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                                } // end for
                                if (errorAlreadyPrinted) {
                                    interestingMarket = false;
                                } else {
                                    interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                                }
                                break;
                            default:
                                Generic.alreadyPrintedMap.logOnce(Statics.newMarketSynchronizedWriter, logger, LogLevel.ERROR, "new OVER_UNDER marketName found: {}, for: {}", marketName,
                                                                  Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
                                interestingMarket = false;
                                break;
                        } // end switch
                        break;
                    case "ET_CORRECT_SCORE":
                        parsedMarketType = ParsedMarketType.ET_CORRECT_SCORE;
                        nParsedRunners = 10;
                        parsedRunnersSet = createParsedRunnersSet(nParsedRunners);
                        for (final RunnerCatalog runnerCatalog : runnerCatalogsList) {
                            final int sortPriority = runnerCatalog.getSortPriority();
                            final String runnerName = runnerCatalog.getRunnerName();
                            final ParsedRunnerType parsedRunnerType;
                            if (sortPriority == 1 && runnerName.trim().equalsIgnoreCase("0 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_0;
                            } else if (sortPriority == 2 && runnerName.trim().equalsIgnoreCase("1 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_1;
                            } else if (sortPriority == 3 && runnerName.trim().equalsIgnoreCase("2 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_2;
                            } else if (sortPriority == 4 && runnerName.trim().equalsIgnoreCase("1 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_0;
                            } else if (sortPriority == 5 && runnerName.trim().equalsIgnoreCase("2 - 0")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_0;
                            } else if (sortPriority == 6 && runnerName.trim().equalsIgnoreCase("2 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_2_1;
                            } else if (sortPriority == 7 && runnerName.trim().equalsIgnoreCase("0 - 1")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_1;
                            } else if (sortPriority == 8 && runnerName.trim().equalsIgnoreCase("0 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_0_2;
                            } else if (sortPriority == 9 && runnerName.trim().equalsIgnoreCase("1 - 2")) {
                                parsedRunnerType = ParsedRunnerType.SCORE_1_2;
                            } else if (sortPriority == 10 && runnerName.trim().equalsIgnoreCase("Any Unquoted")) {
                                parsedRunnerType = ParsedRunnerType.ANY_UNQUOTED;
                            } else {
                                parsedRunnerType = unknownParsedRunnerTypeError(parsedMarketType, sortPriority, runnerName, marketId, marketCatalogue);
                                errorAlreadyPrinted = true;
                                break;
                            }
                            addParsedRunner(parsedRunnersSet, runnerCatalog, parsedRunnerType);
                        } // end for
                        if (errorAlreadyPrinted) {
                            interestingMarket = false;
                        } else {
                            interestingMarket = parsedRunnersSetSizeCheck(parsedRunnersSet, nParsedRunners, marketId, parsedMarketType, marketCatalogue);
                        }
                        break;
                    default:
                        if (!Statics.marketTypes.contains(marketType)) {
                            Generic.alreadyPrintedMap.logOnce(Statics.newMarketSynchronizedWriter, logger, LogLevel.ERROR, "new marketType found: {}, for: {}", marketType, Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
                            interestingMarket = false;
                        } else { // known market that is not parsed
//                            interestingMarket = false;
                            interestingMarket = true; // not supported markets, with no errors, pass all the time
                        }
                        break;
                } // end switch
            }
        } else {
            interestingMarket = true; // not supported markets, with no errors, pass all the time
        }

        return interestingMarket;
    }
}
