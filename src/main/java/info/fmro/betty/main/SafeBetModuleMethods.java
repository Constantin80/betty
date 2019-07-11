package info.fmro.betty.main;

import com.google.common.collect.Iterables;
import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.EventResult;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.entities.MarketDescription;
import info.fmro.betty.entities.MarketFilter;
import info.fmro.betty.entities.RunnerCatalog;
import info.fmro.betty.enums.CommandType;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
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
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings({"OverlyComplexClass", "UtilityClass", "unused"})
final class SafeBetModuleMethods {
    private static final Logger logger = LoggerFactory.getLogger(SafeBetModuleMethods.class);

    @Contract(pure = true)
    private SafeBetModuleMethods() {
    }

    private static List<EventResult> getLiveEventResultList(final String appKeyString) {
        final Set<String> eventTypeIdsSet = new HashSet<>(2, 0.75f);
        eventTypeIdsSet.add("1"); // soccer

        final MarketFilter marketFilter = new MarketFilter();
        marketFilter.setInPlayOnly(true);
        marketFilter.setTurnInPlayEnabled(true);
        marketFilter.setEventTypeIds(eventTypeIdsSet);

        final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        final List<EventResult> eventResultList = ApiNgRescriptOperations.listEvents(marketFilter, appKeyString, rescriptResponseHandler);

        if (Statics.debugLevel.check(3, 113)) {
            logger.info("eventResultList size: {}", eventResultList == null ? null : eventResultList.size());
        }

        return eventResultList;
    }

    @SuppressWarnings("OverlyNestedMethod")
    public static void parseEventResultList() {
        final long startTime = System.currentTimeMillis();
        final List<EventResult> eventResultList = getLiveEventResultList(Statics.appKey.get());
        if (eventResultList != null) {
            // Statics.timeStamps.lastParseEventResultListStamp(Generic.MINUTE_LENGTH_MILLISECONDS);
            final HashSet<Event> addedEvents = new HashSet<>(0);
            final Collection<Event> modifiedEvents = new HashSet<>(0);
            for (final EventResult eventResult : eventResultList) {
                final Event event = eventResult.getEvent();
                if (event.parseName() > 0) {
                    final String eventId = event.getId();
                    final Event existingEvent;
                    synchronized (Statics.eventsMap) {
                        if (Statics.eventsMap.containsKey(eventId)) {
                            existingEvent = Statics.eventsMap.get(eventId);
                        } else {
                            if (event.isIgnored(startTime)) { // these events don't already exist in map, so they can't be ignored already; this would be an error
                                logger.error("blackListed event added: {}", eventId);
                            } else { // probably nothing to do on this branch
                            }

                            existingEvent = Statics.eventsMap.putIfAbsent(eventId, event);
                            if (existingEvent == null) { // event was added, no previous event existed, double check to avoid racing issues
                                addedEvents.add(event);
                            } else {
                                logger.error("existingEvent found during put double check: {} {}", Generic.objectToString(existingEvent), Generic.objectToString(event));
                            }
                        }
                    } // end synchronized

                    if (existingEvent != null) {
                        final int update = existingEvent.update(event);
                        if (update > 0) {
                            modifiedEvents.add(existingEvent);
                        }
                    }
                } else {
                    Generic.alreadyPrintedMap.logOnce(logger, LogLevel.WARN, "parseEventResultList ignoring unparsed event name: {}", event.getName());
                }
            } // end for
            Statics.eventsMap.timeStamp();

            final int sizeModified = modifiedEvents.size();
            if (sizeModified > 0) { // check on whether the modified events are matched is done in findInterestingMarkets
                final HashSet<Event> notIgnoredModifiedEvents = new HashSet<>(Generic.getCollectionCapacity(modifiedEvents));
                for (final Event event : modifiedEvents) {
                    if (!event.isIgnored()) {
                        notIgnoredModifiedEvents.add(event);
                    }
                } // end for
                final int sizeNotIgnoredModified = notIgnoredModifiedEvents.size();
                if (sizeNotIgnoredModified > 0) {
                    logger.info("parseEventResultList modifiedEvents: {} launch: findMarkets", sizeNotIgnoredModified);
                    Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, notIgnoredModifiedEvents));
                }
            }

            final int sizeAdded = addedEvents.size();
            if (sizeAdded > 0) { // only added; modified will be checked during fullRun
                if (Statics.safeBetModuleActivated) {
                    logger.info("parseEventResultList addedEvents: {} launch: mapEventsToScraperEvents delayed", sizeAdded);
                    Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.mapEventsToScraperEvents, addedEvents, Generic.MINUTE_LENGTH_MILLISECONDS));
                }
            }
        } else {
            logger.error("eventResultList null in parseEventResultList");

            if (!GetLiveMarketsThread.waitForSessionToken("parseEventList")) {
                logger.error("sessionToken seems to not have been needed in parseEventResultList");
                Generic.threadSleep(1000L); // avoid throttle, sessionToken might not have been needed
            }
        }
        logger.info("parseEventResultList finished in {} ms eventResultList size: {}", System.currentTimeMillis() - startTime, eventResultList == null ? null : eventResultList.size());
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod"})
    public static void findInterestingMarkets(final Collection<String> marketIdsSet) {
        if (marketIdsSet != null) {
            final long methodStartTime = System.currentTimeMillis();
            final Set<MarketCatalogue> returnSet = Collections.synchronizedSet(new HashSet<>(Generic.getCollectionCapacity(marketIdsSet.size())));
            final Iterable<List<String>> splitsIterable = Iterables.partition(marketIdsSet, 200);
            final Collection<Thread> threadList = new ArrayList<>(Iterables.size(splitsIterable));
            for (final List<String> marketIdsList : splitsIterable) {
                final RescriptOpThread<MarketCatalogue> rescriptOpThread = new RescriptOpThread<>(returnSet, new TreeSet<>(marketIdsList), FindMarkets.marketProjectionsSet);
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
                        logger.error("STRANGE interruptedException in findInterestingMarkets marketIds", interruptedException);
                    }
                } else { // thread already finished, nothing to be done
                }
            } // end for

            final Collection<Event> modifiedEvents = new HashSet<>(0);
            final Collection<Map.Entry<String, MarketCatalogue>> toCheckMarkets = new LinkedHashSet<>(0);
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
                    logger.info("marketId {} without event in maps in findInterestingMarkets(marketIdsSet)", marketId);
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
                            logger.warn("event modified {} in findInterestingMarkets for: {} {} {}", update, marketId, Generic.objectToString(eventStump), Generic.objectToString(event)); // this might be normal behaviour
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
                            logger.error("null eventStumpId in findInterestingMarkets for: {}", Generic.objectToString(marketCatalogue));
                        }
                    }

                    @Nullable final MarketCatalogue existingMarketCatalogue = Statics.marketCataloguesMap.containsKey(marketId) ? Statics.marketCataloguesMap.get(marketId) : null;
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
                    logger.error("null fields in findInterestingMarkets marketIds for: {}", Generic.objectToString(marketCatalogue));
                } // end else
            } // end for

            final int sizeModified = modifiedEvents.size();
            if (sizeModified > 0) {
                final String printedString = MessageFormatter.arrayFormat("findInterestingMarkets modifiedEvents: {} launch: findMarkets", new Object[]{sizeModified}).getMessage();
                logger.info(printedString);

                final HashSet<Event> notIgnoredModifiedEvents = new HashSet<>(Generic.getCollectionCapacity(modifiedEvents));
                for (final Event event : modifiedEvents) {
                    if (!event.isIgnored()) {
                        notIgnoredModifiedEvents.add(event);
                    }
                } // end for
                if (!notIgnoredModifiedEvents.isEmpty()) {
                    Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, notIgnoredModifiedEvents));
                }
            }
            final int sizeEntries = toCheckMarkets.size();
            if (sizeEntries > 0) {
                final String printedString = MessageFormatter.arrayFormat("findInterestingMarkets toCheckMarkets(modify/add): {}({}/{}) launch: findSafeRunners", new Object[]{sizeEntries, nModifiedMarkets, nAddedMarkets}).getMessage();
                logger.info(printedString);

                final LinkedHashSet<Map.Entry<String, MarketCatalogue>> notIgnoredToCheckMarkets = new LinkedHashSet<>(Generic.getCollectionCapacity(toCheckMarkets));
                for (final Map.Entry<String, MarketCatalogue> entry : toCheckMarkets) {
                    final MarketCatalogue value = entry.getValue();
                    if (!value.isIgnored()) {
                        notIgnoredToCheckMarkets.add(entry);
                    }
                } // end for
                if (!notIgnoredToCheckMarkets.isEmpty()) {
                    if (Statics.safeBetModuleActivated) {
                        Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findSafeRunners, notIgnoredToCheckMarkets));
                    }
                }
            }
        } else {
            logger.error("null marketIdsSet in findInterestingMarkets");
        }
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod"})
    public static void findInterestingMarkets(final Collection<Event> eventsSet, final boolean checkAll) {
        if (!Statics.safeBetModuleActivated || System.currentTimeMillis() - Statics.scraperEventMaps.getNTimeStamp() <= Generic.HOUR_LENGTH_MILLISECONDS * 3L) {
            final long methodStartTime = System.currentTimeMillis();
            final Collection<Event> eventsCopy = eventsSet != null ? eventsSet : Statics.eventsMap.valuesCopy();
            final Iterator<Event> iterator = eventsCopy.iterator();
            while (iterator.hasNext()) {
                final Event event = iterator.next();
                if (event == null) {
                    logger.error("STRANGE null event in findInterestingMarkets initial removal");
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
                            logger.error("null eventId in findInterestingMarkets for: {}", Generic.objectToString(event));
                        }
                    } else { // it's matched, will be used in the method
                    }
                }
            } // end while

            if (eventsCopy.isEmpty()) {
                final boolean fullRun = eventsSet == null;
                final String fullRunString = fullRun ? " fullRun" : "";
                final String checkAllString = checkAll ? " checkAll" : "";
                final String printedString = MessageFormatter.arrayFormat("findInterestingMarkets{}{}: no matched events to work with", new Object[]{fullRunString, checkAllString}).getMessage();
                logger.info(printedString); // this is info, not error; the set can become empty after removal at beginning of method
            } // end else
            else {
                final Set<MarketCatalogue> returnSet = Collections.synchronizedSet(new HashSet<>(128));
                final Collection<Thread> threadList = new ArrayList<>(1);
                HashSet<String> eventIds = new HashSet<>(8);
                int setMarketCount = 0;
                for (final Event event : eventsCopy) { // null and ignored verification have been done previously
                    final String eventId = event.getId();
                    final int marketCount = event.getMarketCount();
                    if (setMarketCount + marketCount <= 200) {
                        eventIds.add(eventId);
                        setMarketCount += marketCount;
                    } else {
                        final RescriptOpThread<MarketCatalogue> rescriptOpThread = new RescriptOpThread<>(returnSet, eventIds, FindMarkets.marketProjectionsSet);
                        final Thread thread = new Thread(rescriptOpThread);
                        thread.start();
                        threadList.add(thread);
                        eventIds = new HashSet<>(8);
                        eventIds.add(eventId);
                        setMarketCount = marketCount;
                    }
                } // end for
                if (!eventIds.isEmpty()) {
                    final RescriptOpThread<MarketCatalogue> rescriptOpThread = new RescriptOpThread<>(returnSet, eventIds, FindMarkets.marketProjectionsSet);
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
                            logger.error("STRANGE interruptedException in findInterestingMarkets", interruptedException);
                        }
                    } else { // thread already finished, nothing to be done
                    }
                } // end for

                final long endTime = System.currentTimeMillis();
                final Collection<Map.Entry<String, MarketCatalogue>> toCheckMarkets = new LinkedHashSet<>(0);
                int nModifiedMarkets = 0, nAddedMarkets = 0;
                final Collection<Event> modifiedEvents = new HashSet<>(0);

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
                                logger.warn("findInterestingMarkets unparsed event name: {} for: {}", eventStump.getName(), marketId);
                            } else {
                                Generic.alreadyPrintedMap.logOnce(4L * Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.INFO, "findInterestingMarkets unparsed event name: {} for: {} {}", eventStump.getName(), marketId,
                                                                  Generic.objectToString(marketCatalogue.getEventType()));
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
                            logger.warn("event modified {} in findInterestingMarkets for: {} {} {}", update, marketId, Generic.objectToString(eventStump), Generic.objectToString(event)); // this might be normal behaviour
                        }

                        @Nullable MarketCatalogue existingMarketCatalogue;
                        if (nMatched >= Statics.MIN_MATCHED) {
                            existingMarketCatalogue = Statics.marketCataloguesMap.containsKey(marketId) ? Statics.marketCataloguesMap.get(marketId) : null;
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

                            final long timeSinceLastRemoved = methodStartTime - Statics.scraperEventMaps.getNTimeStampRemoved();
                            final String printedString = MessageFormatter.arrayFormat("no scraperEvent found in findInterestingMarkets timeSinceLastRemoved: {}ms for eventId: {} marketId: {} marketCatalogue: {}",
                                                                                      new Object[]{timeSinceLastRemoved, eventId, marketId, Generic.objectToString(marketCatalogue)}).getMessage();
                            if (timeSinceLastRemoved < 1_000L) {
                                logger.info(printedString);
                            } else {
                                logger.error(printedString);
                            }

                            existingMarketCatalogue = Statics.marketCataloguesMap.get(marketId);
                            if (existingMarketCatalogue != null) {
                                if (!existingMarketCatalogue.isIgnored()) {
                                    logger.error("STRANGE Statics.marketCataloguesMap in findInterestingMarkets contained unmatched marketId: {}", marketId);
                                }

                                existingMarketCatalogue = null;
                            }
                        } // end else
//                        logger.info("before if: {} {} {}", marketId, checkAll, returnSet.size());
                        if (nMatched >= Statics.MIN_MATCHED && (checkAll || existingMarketCatalogue == null)) {
                            final String eventHomeNameString = event.getHomeName();
                            final String eventAwayNameString = event.getAwayName();
                            if (eventId != null && eventHomeNameString != null && eventAwayNameString != null) {
                                final boolean interestingMarket = FindMarkets.parseSupportedMarket(eventHomeNameString, eventAwayNameString, marketCatalogue, marketId, marketName, marketDescription, runnerCatalogsList);
//                                logger.info("interesting or not: {} {} {}", marketId, interestingMarket, nMatched);
                                if (interestingMarket) {
                                    //noinspection ConstantConditions
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
                                                                                  Generic.objectToString(marketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"),
                                                                                  Generic.objectToString(inMapMarketCatalogue, "Stamp", "timeFirstSeen", "totalMatched"));
                                            } else { // can happen due to concurrent threads, no need to print message
                                            }
                                        }
                                    } else {
                                        logger.error("nMatched {} found in findInterestingMarkets for eventId: {} marketId: {} marketCatalogue: {}", nMatched, eventId, marketId, Generic.objectToString(marketCatalogue));

                                        if (Statics.marketCataloguesMap.containsKey(marketId)) {
                                            // will not remove, having a bad error message is good enough
                                            logger.error("STRANGE Statics.marketCataloguesMap in findInterestingMarkets contained unmatched marketId: {}", marketId);
                                        }
                                    } // end else
                                } else { // market not interesting, nothing to be done
                                }
                            } else {
                                if (Formulas.isMarketType(marketCatalogue, Statics.supportedEventTypes)) {
                                    logger.error("null fields in findInterestingMarkets inner for: {}", Generic.objectToString(marketCatalogue));
                                } else {
                                    Generic.alreadyPrintedMap.logOnce(logger, LogLevel.INFO, "null fields in findInterestingMarkets inner for: {} {}", marketId, Generic.objectToString(marketCatalogue.getEventType()));
                                }
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
                            logger.error("null fields in findInterestingMarkets for: {}", Generic.objectToString(marketCatalogue));
                        }
                    }
                } // end for marketCatalogue

                final boolean fullRun = eventsSet == null;
                final String fullRunString = fullRun ? " fullRun" : "";
                final String checkAllString = checkAll ? " checkAll" : "";
                logger.info("findInterestingMarkets{}{}: {} in map, {} returned from {} matched events, in {} ms, of which {} ms waiting for threads", fullRunString, checkAllString, Statics.marketCataloguesMap.size(), returnSet.size(), eventsCopy.size(),
                            System.currentTimeMillis() - methodStartTime, endTime - startedWaitingTime);

                Statics.marketCataloguesMap.timeStamp();

                final int sizeModified = modifiedEvents.size();
                if (sizeModified > 0) {
                    final String printedString = MessageFormatter.arrayFormat("findInterestingMarkets{}{} modifiedEvents: {} launch: findMarkets", new Object[]{fullRunString, checkAllString, sizeModified}).getMessage();
                    if (fullRun) {
                        logger.error(printedString);
                    } else {
                        logger.info(printedString);
                    }

                    final HashSet<Event> notIgnoredModifiedEvents = new HashSet<>(Generic.getCollectionCapacity(modifiedEvents));
                    for (final Event event : modifiedEvents) {
                        if (!event.isIgnored()) {
                            notIgnoredModifiedEvents.add(event);
                        }
                    } // end for
                    if (!notIgnoredModifiedEvents.isEmpty()) {
                        Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, notIgnoredModifiedEvents));
                    }
                }
                final int sizeEntries = toCheckMarkets.size();
                if (sizeEntries > 0) {
                    final String printedString =
                            MessageFormatter.arrayFormat("findInterestingMarkets{}{} toCheckMarkets(modify/add): {}({}/{}) launch: findSafeRunners", new Object[]{fullRunString, checkAllString, sizeEntries, nModifiedMarkets, nAddedMarkets}).getMessage();
                    if (fullRun) {
                        if (nModifiedMarkets > 0) {
                            logger.warn(printedString); // sometimes, rarely, does happen
                        } else {
                            logger.info(printedString); // sometimes markets are added, presumably replacing others, so that the event's total nMarkets remains the same
                        }
                    } else {
                        logger.info(printedString);
                    }

                    final LinkedHashSet<Map.Entry<String, MarketCatalogue>> notIgnoredToCheckMarkets = new LinkedHashSet<>(Generic.getCollectionCapacity(toCheckMarkets));
                    for (final Map.Entry<String, MarketCatalogue> entry : toCheckMarkets) {
                        final MarketCatalogue value = entry.getValue();
                        if (!value.isIgnored()) {
                            notIgnoredToCheckMarkets.add(entry);
                        }
                    } // end for
                    if (!notIgnoredToCheckMarkets.isEmpty()) {
                        if (Statics.safeBetModuleActivated) {
                            Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findSafeRunners, notIgnoredToCheckMarkets));
                        }
                    }
                }
            }
        } else {
            logger.info("scraperEventsMap too old in findInterestingMarkets");
        }
    }
}
