package info.fmro.betty.main;

import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.shared.utility.LogLevel;
import info.fmro.betty.enums.MatchStatus;
import info.fmro.betty.objects.BlackList;
import info.fmro.betty.objects.ScraperEvent;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.AlreadyPrintedMap;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.SynchronizedMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

public class LaunchCommandThread
        implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(LaunchCommandThread.class);
    private final String command;
    private boolean checkAll;
    private long delay;
    private HashSet<Event> eventsSet;
    private Set<?> scraperEventsSet;
    private LinkedHashSet<Entry<String, MarketCatalogue>> entrySet;
    private Class<? extends ScraperEvent> clazz;

    public LaunchCommandThread(String command) {
        this.command = command;
    }

    public LaunchCommandThread(String command, boolean checkAll) {
        this.command = command;
        this.checkAll = checkAll;
    }

    public LaunchCommandThread(String command, HashSet<Event> eventsSet) {
        this.command = command;
        this.eventsSet = eventsSet;
    }

    public LaunchCommandThread(String command, HashSet<Event> eventsSet, long delay) {
        this.command = command;
        this.eventsSet = eventsSet;
        this.delay = delay;
    }

    public LaunchCommandThread(String command, Set<?> scraperEventsSet, Class<? extends ScraperEvent> clazz) {
        this.command = command;
        this.scraperEventsSet = scraperEventsSet;
        this.clazz = clazz;
    }

    public LaunchCommandThread(String command, LinkedHashSet<Entry<String, MarketCatalogue>> entrySet) {
        this.command = command;
        this.entrySet = entrySet;
    }

    public LaunchCommandThread(String command, LinkedHashSet<Entry<String, MarketCatalogue>> entrySet, long delay) {
        this.command = command;
        this.entrySet = entrySet;
        this.delay = delay;
    }

    public static void mapEventsToScraperEvents(Class<? extends ScraperEvent> clazz) {
        LaunchCommandThread.mapEventsToScraperEvents(clazz, false, null, null);
    }

    public static void mapEventsToScraperEvents(Class<? extends ScraperEvent> clazz, boolean checkAll) {
        LaunchCommandThread.mapEventsToScraperEvents(clazz, checkAll, null, null);
    }

    public static void mapEventsToScraperEvents(Class<? extends ScraperEvent> clazz, Set<ScraperEvent> scraperEventsSet) {
        LaunchCommandThread.mapEventsToScraperEvents(clazz, false, null, scraperEventsSet);
    }

    public static void mapEventsToScraperEvents(Class<? extends ScraperEvent> clazz, HashSet<Event> eventsSet) {
        LaunchCommandThread.mapEventsToScraperEvents(clazz, false, eventsSet, null);
    }

    public static void mapEventsToScraperEvents(Class<? extends ScraperEvent> clazz, boolean checkAll, Set<Event> eventsSet, Set<ScraperEvent> scraperEventsSet) {
        // Statics.timeStamps.lastMapEventsToScraperEventsStamp(); // stamp added in the timed method now, and is only used for that method when running with checkAll
        if (clazz == null) {
            if (scraperEventsSet != null) { // error printed, but method will continue
                logger.error("STRANGE scraperEventsSet exists and clazz null in mapEventsToScraperEvents");
            }
            for (final Class<? extends ScraperEvent> classFromSet : Statics.scraperEventSubclassesSet) {
                mapEventsToScraperEvents(classFromSet, checkAll, eventsSet, scraperEventsSet);
            }
        } else {
            final long startTime = System.currentTimeMillis();
            final SynchronizedMap<Long, ? extends ScraperEvent> synchronizedScraperEventsMap = Formulas.getScraperEventsMap(clazz);
            final ScraperThread scraperThread = Formulas.getScraperThread(clazz);

            if (synchronizedScraperEventsMap == null || scraperThread == null) { // nothing to be done, error message printed previously
            } else if (startTime - synchronizedScraperEventsMap.getTimeStamp() > Generic.HOUR_LENGTH_MILLISECONDS * 3L) {
                logger.info("scraperEventsMap too old in mapEventsToScraperEvents {}", clazz.getSimpleName());
            } else if (synchronizedScraperEventsMap.isEmpty()) {
                logger.info("scraperEventsMap empty in mapEventsToScraperEvents {}", clazz.getSimpleName());
            } else {
                final Collection<? extends ScraperEvent> scraperEventsCopy;
                if (scraperEventsSet != null) {
                    scraperEventsCopy = scraperEventsSet;
                } else {
                    scraperEventsCopy = synchronizedScraperEventsMap.valuesCopy();
                }

//                if (!checkAll) {
                final Iterator<? extends ScraperEvent> iteratorScraperEventsCopy = scraperEventsCopy.iterator();
                while (iteratorScraperEventsCopy.hasNext()) {
                    ScraperEvent scraperEvent = iteratorScraperEventsCopy.next();
                    if (scraperEvent == null) {
                        logger.error("STRANGE null {} scraperEvent in mapEventsToScraperEvents initial removal", clazz.getSimpleName());
                        iteratorScraperEventsCopy.remove();
                        synchronizedScraperEventsMap.removeValueAll(null);
                    } else if (scraperEvent.isTooOldForMatching(startTime)) {
                        logger.info("not using tooOld scraper id {} for matching", scraperEvent.getEventId());
                        iteratorScraperEventsCopy.remove();
                    } else {
                        if (!checkAll) {
                            String matchedEventId = scraperEvent.getMatchedEventId();
                            if (matchedEventId != null) {
                                iteratorScraperEventsCopy.remove();
                            } else { // not yet matched, will be checked in the method
                            }
                        }
                    }// end else
                } // end while
//                } // end if checkAll

                if (Statics.debugLevel.check(2, 103)) { // obsolete, working for the full or checkAll runs
                    logger.info("{} scraperEventsMap size: {} unmatched: {}", clazz.getSimpleName(), synchronizedScraperEventsMap.size(), scraperEventsCopy.size());
                }

                final Collection<Event> eventsCopy;
                if (eventsSet != null) {
                    eventsCopy = eventsSet;
                } else {
                    eventsCopy = Statics.eventsMap.valuesCopy();
                }

//                if (!checkAll) {
                final Iterator<Event> iteratorEventsCopy = eventsCopy.iterator();
                while (iteratorEventsCopy.hasNext()) {
                    final Event event = iteratorEventsCopy.next();
                    if (event == null) {
                        logger.error("STRANGE null event in {} mapEventsToScraperEvents initial removal", clazz.getSimpleName());
                        iteratorEventsCopy.remove();
                        Statics.eventsMap.removeValueAll(null);
                    } else {
                        if (!checkAll) {
                            final Long scraperEventId = event.getScraperEventId(clazz);
                            if (scraperEventId != null) {
                                iteratorEventsCopy.remove();
                            } else { // not yet matched, will be checked in the method
                            }
                        }
                    } // end else
                } // end while
//                } // end if checkAll

                if (Statics.debugLevel.check(2, 174)) { // obsolete, working for the full or checkAll runs
                    logger.info("{} eventsMap size: {} unmatched: {}", clazz.getSimpleName(), Statics.eventsMap.size(), eventsCopy.size());
                }

                final HashSet<Event> toCheckEvents = new HashSet<>(0);
                int counterNotMatched = 0;
                final HashSet<Event> tooRecentMatchedEvents = new HashSet<>(0);
                long minTimeDifference = Statics.INITIAL_EVENT_SAFETY_PERIOD;

                if (!eventsCopy.isEmpty() && !scraperEventsCopy.isEmpty()) {
                    final ArrayList<Event> twoPotentialMatchesEvents = new ArrayList<>(0);
                    final ArrayList<ScraperEvent> twoPotentialMatchesScraperEvents = new ArrayList<>(0);
                    for (final Event event : eventsCopy) {
                        if (event != null) {
                            counterNotMatched++;
                            if (Statics.debugLevel.check(3, 104)) {
                                logger.info("{} found unmatched event: {}", clazz.getSimpleName(), Generic.objectToString(event));
                            }
                            final String eventName = event.getName();
                            final String eventHomeTeam = event.getHomeName();
                            final String eventAwayTeam = event.getAwayName();
                            if (eventHomeTeam != null && eventAwayTeam != null) {
                                ScraperEvent matchedScraperEvent = null;
                                double maxTotalMatch = 0, recordedHomeMatch = 0, recordedAwayMatch = 0;
                                boolean twoPotentialMatches = false;
                                final ArrayList<Double> errTotalMatchList = new ArrayList<>(0);
                                final ArrayList<ScraperEvent> errScraperEventList = new ArrayList<>(0);
                                final HashSet<String> oneTeamMatchedPrintStrings = new HashSet<>(0);

                                for (ScraperEvent scraperEvent : scraperEventsCopy) {
                                    final String homeTeam = scraperEvent.getHomeTeam();
                                    final String awayTeam = scraperEvent.getAwayTeam();
                                    final double homeMatch = Formulas.matchTeams(eventHomeTeam, homeTeam);
                                    final double awayMatch = Formulas.matchTeams(eventAwayTeam, awayTeam);
                                    final double totalMatch = homeMatch * awayMatch;

                                    if (totalMatch < Statics.threshold && (homeMatch >= Statics.threshold || awayMatch >= Statics.threshold)) {
                                        // some extra spaces intentionally left in the output, due to removeStrangeChars; those spaces tell me where a char was replaced
                                        String printedString = Generic.alreadyPrintedMap.logOnce(3 * Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.INFO,
                                                "{} one team matched t:{} h:{} a:{} event: {} scraper: {} / {}", clazz.getSimpleName(), totalMatch, homeMatch, awayMatch,
                                                Formulas.removeStrangeChars(eventName), Formulas.removeStrangeChars(scraperEvent.getHomeTeam()),
                                                Formulas.removeStrangeChars(scraperEvent.getAwayTeam()));
                                        oneTeamMatchedPrintStrings.add(printedString);
                                    }

                                    if (totalMatch >= Math.min(maxTotalMatch, Statics.threshold)) {
                                        if (maxTotalMatch < Statics.threshold) {
                                            maxTotalMatch = totalMatch;
                                            recordedHomeMatch = homeMatch;
                                            recordedAwayMatch = awayMatch;
                                            matchedScraperEvent = scraperEvent;
                                        } else {
                                            // two potential matches support disabled; it can be added later as a separate method, but unlikely
//                                        @SuppressWarnings(value = "null")
//                                        final long scraperEventTime = scraperEvent.getStartTime().getTime();
//                                        @SuppressWarnings(value = "null")
//                                        final long matchedScraperEventTime = matchedScraperEvent.getStartTime().getTime();
//                                        final boolean scraperEventHasStarted = scraperEvent.hasStarted();
//                                        final boolean matchedScraperEventHasStarted = matchedScraperEvent.hasStarted();
//                                        final String eventId = event.getId();
//                                        final String scraperMatchedEventId = scraperEvent.getMatchedEventId();
//                                        final String matchedScraperMatchedEventId = matchedScraperEvent.getMatchedEventId();
//
//                                        Formulas.logOnce(Statics.matcherSynchronizedWriter, logger, LogLevel.WARN, "two potential matches: {} {} {} {}/{}/{} {}/{}/{}",
//                                                maxTotalMatch, totalMatch, eventName, matchedScraperEvent.getHomeTeam(), matchedScraperEvent.getAwayTeam(),
//                                                matchedScraperEvent.getMatchStatus(), scraperEvent.getHomeTeam(), scraperEvent.getAwayTeam(), scraperEvent.getMatchStatus());
//                                        if (scraperMatchedEventId != null && !scraperMatchedEventId.equals(eventId)) {
//                                            // scraperEvent already matched to another event, nothing will be done
//                                            if (Statics.debugLevel.check(3, 160)) {
//                                                logger.info("two potential matches: second scraperEvent already matched to another event, the other scraperEvent will be used");
//                                            }
//                                        } else if (matchedScraperMatchedEventId != null && !matchedScraperMatchedEventId.equals(eventId)) {
//                                            // matchedScraperEvent already matched to another event, I'll change it
//                                            if (Statics.debugLevel.check(3, 161)) {
//                                                logger.warn("two potential matches: first scraperEvent already matched to another event, the other scraperEvent will be used");
//                                            }
//                                            maxTotalMatch = totalMatch;
//                                            matchedScraperEvent = scraperEvent;
//                                        } else {
//                                            if (matchedScraperEventHasStarted && !scraperEventHasStarted) {
//                                                // existing scraperEvent has started, nothing will be done
//                                                if (Statics.debugLevel.check(3, 162)) {
//                                                    logger.warn("two potential matches: first scraperEvent has started and will be used");
//                                                }
//                                            } else if (!matchedScraperEventHasStarted && scraperEventHasStarted) {
//                                                if (Statics.debugLevel.check(3, 163)) {
//                                                    logger.warn("two potential matches: second scraperEvent has started and will be used");
//                                                }
//                                                maxTotalMatch = totalMatch;
//                                                matchedScraperEvent = scraperEvent;
//                                            } else if (matchedScraperEventHasStarted && scraperEventHasStarted) {
//                                                if (Statics.debugLevel.check(3, 164)) {
//                                                    logger.warn("two potential matches: both scraperEvents have started and last to start be used");
//                                                }
//                                                if (scraperEventTime > matchedScraperEventTime) {
//                                                    maxTotalMatch = totalMatch;
//                                                    matchedScraperEvent = scraperEvent;
//                                                } else if (scraperEventTime < matchedScraperEventTime) {
//                                                    // existing scraperEvent is chosen, nothing will be done
//                                                } else {
//                                                    if (Statics.debugLevel.check(3, 165)) {
//                                                        logger.error("!!!two potential matches: both start at the same time");
//                                                    }
//                                                    twoPotentialMatches = true;
//                                                }
//                                            } else if (!matchedScraperEventHasStarted && !scraperEventHasStarted) {
//                                                if (Statics.debugLevel.check(3, 166)) {
//                                                    logger.warn("two potential matches: none of the scraperEvents has started and first to start and not postponed will be used");
//                                                }
//                                                if (scraperEventTime < matchedScraperEventTime) {
//                                                    final MatchStatus olderMatchStatus = scraperEvent.getMatchStatus();
//                                                    if (olderMatchStatus == MatchStatus.POSTPONED) {
//                                                        // postponed, won't be used, nothing to be done
//                                                    } else {
//                                                        maxTotalMatch = totalMatch;
//                                                        matchedScraperEvent = scraperEvent;
//                                                    }
//                                                } else if (scraperEventTime > matchedScraperEventTime) {
//                                                    final MatchStatus olderMatchStatus = matchedScraperEvent.getMatchStatus();
//                                                    if (olderMatchStatus == MatchStatus.POSTPONED) {
//                                                        // postponed, the other one will be used
//                                                        maxTotalMatch = totalMatch;
//                                                        matchedScraperEvent = scraperEvent;
//                                                    } else {
//                                                        // existing scraperEvent is chosen, nothing will be done
//                                                    }
//                                                } else {
//                                                    final MatchStatus matchStatus = scraperEvent.getMatchStatus();
//                                                    final MatchStatus matchedMatchStatus = matchedScraperEvent.getMatchStatus();
//                                                    if (matchedMatchStatus == MatchStatus.POSTPONED && matchStatus != MatchStatus.POSTPONED) {
//                                                        maxTotalMatch = totalMatch;
//                                                        matchedScraperEvent = scraperEvent;
//                                                    } else if (matchStatus == MatchStatus.POSTPONED && matchedMatchStatus != MatchStatus.POSTPONED) {
//                                                        // nothing will be done
//                                                    } else if (matchStatus == MatchStatus.POSTPONED && matchedMatchStatus == MatchStatus.POSTPONED) {
//                                                        // both postponed, nothing will be done, unimportant situation
//                                                    } else if (matchStatus != MatchStatus.POSTPONED && matchedMatchStatus != MatchStatus.POSTPONED) {
//                                                        if (Statics.debugLevel.check(3, 167)) {
//                                                            logger.error("!!!two potential matches: both start at the same time");
//                                                        }
//                                                        twoPotentialMatches = true;
//                                                    } else {
//                                                        logger.error("VERY STRANGE two potential matches error, unknown postponed if branch");
//                                                        twoPotentialMatches = true;
//                                                    }
//                                                }
//                                            } else {
//                                                logger.error("VERY STRANGE two potential matches error, unknown if branch");
//                                                twoPotentialMatches = true;
//                                            }
//                                        }

                                            errTotalMatchList.add(totalMatch);
                                            errScraperEventList.add(scraperEvent);
                                            if (!twoPotentialMatches) {
                                                twoPotentialMatches = true; // algorithm is not safe and maybe can't be made safe; but worse, this can affect multiple scraped sites
                                            }
                                        } // end else
                                    } // end if
                                } // end for scraperEvent

                                if (maxTotalMatch >= Statics.threshold) {
                                    if (matchedScraperEvent != null) {
                                        if (!twoPotentialMatches) {
                                            final MatchStatus matchStatus = matchedScraperEvent.getMatchStatus();
                                            if (matchStatus != MatchStatus.NOT_STARTED && matchStatus != MatchStatus.POSTPONED) {
                                                final long timeFirstSeen = event.getTimeFirstSeen();
                                                final long lastGetScraperEvents = scraperThread.lastGetScraperEvents.get();
                                                final long scraperStamp = matchedScraperEvent.getTimeStamp();
                                                final long timeDifference = Math.max(lastGetScraperEvents, scraperStamp) - timeFirstSeen;

                                                if (timeDifference >= Statics.INITIAL_EVENT_SAFETY_PERIOD) {
                                                    final Long matchedScraperEventId = matchedScraperEvent.getEventId();
                                                    final Long existingMatchedScraperEventId = event.getScraperEventId(clazz);
                                                    final String eventId = event.getId();
                                                    final String existingMatchedEventId = matchedScraperEvent.getMatchedEventId();

                                                    if (Objects.equals(matchedScraperEventId, existingMatchedScraperEventId) && Objects.equals(eventId, existingMatchedEventId)) {
                                                        // akready matched, nothing further to be done; this might be normal in checkAll and maybe other situations
                                                    } else if (existingMatchedScraperEventId == null && existingMatchedEventId == null) {
                                                        final int eventModified = event.setScraperEventId(clazz, matchedScraperEventId);
                                                        final int scraperEventModified = matchedScraperEvent.setMatchedEventId(eventId);

                                                        if (eventModified > 0 && scraperEventModified > 0) {
                                                            toCheckEvents.add(event);
                                                            counterNotMatched--;
                                                            // some extra spaces intentionally left in the output, due to removeStrangeChars
                                                            // those spaces tell me where a char was replaced
                                                            String printedString = MessageFormatter.arrayFormat("{} matched t:{} h:{} a:{} event: {} scraper: {} / {}",
                                                                    new Object[]{clazz.getSimpleName(), maxTotalMatch, recordedHomeMatch, recordedAwayMatch,
                                                                        Formulas.removeStrangeChars(eventName), Formulas.removeStrangeChars(matchedScraperEvent.getHomeTeam()),
                                                                        Formulas.removeStrangeChars(matchedScraperEvent.getAwayTeam())}).getMessage();

                                                            logger.info(printedString);
                                                            if (maxTotalMatch < Statics.highThreshold &&
                                                                    (recordedHomeMatch < Statics.highThreshold || recordedAwayMatch < Statics.highThreshold)) {
                                                                Statics.matcherSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");
                                                            }
                                                        } else { // there was error changing matchedEventId
                                                            final long timeSinceEventMatched = startTime - event.getMatchedTimeStamp();
                                                            final long timeSinceScraperEventMatched = startTime - matchedScraperEvent.getMatchedTimeStamp();
                                                            final String printedString =
                                                                    MessageFormatter.arrayFormat("{} {} ({} {})ms while setting matched partners {}: {} {} {} {}",
                                                                            new Object[]{eventModified, scraperEventModified, timeSinceEventMatched, timeSinceScraperEventMatched,
                                                                                clazz.getSimpleName(), eventId, matchedScraperEventId, Generic.objectToString(event),
                                                                                Generic.objectToString(matchedScraperEvent)}).getMessage();
                                                            if ((eventModified <= 0 && timeSinceEventMatched < 1_000L) ||
                                                                    (scraperEventModified <= 0 && timeSinceScraperEventMatched < 1_000L)) {
                                                                logger.warn("warning {}", printedString);
                                                            } else {
                                                                logger.error("error {}", printedString);
                                                                event.removeScraperEventId(clazz);
                                                                matchedScraperEvent.resetMatchedEventId();
                                                            }
                                                        }
                                                    } else if (existingMatchedScraperEventId == null && BlackList.notExist(synchronizedScraperEventsMap, matchedScraperEventId)) {
                                                        // normal behaviour, matchedScraperEventId doesn't exist in map, so it was just removed
                                                        final long currentTime = System.currentTimeMillis();
                                                        final long timeSinceLastRemoved = currentTime - synchronizedScraperEventsMap.getTimeStampRemoved();
                                                        if (timeSinceLastRemoved < 200L) {
                                                            logger.info("scraperEvent was probably removed just before matching {} {}ms {} {} {} {}", clazz.getSimpleName(),
                                                                    timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId, existingMatchedEventId, eventId);
                                                        } else if (timeSinceLastRemoved < 1_000L) {
                                                            logger.warn("scraperEvent was probably removed just before matching {} {}ms {} {} {} {}", clazz.getSimpleName(),
                                                                    timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId, existingMatchedEventId, eventId);
                                                        } else {
                                                            logger.error("scraperEvent was probably removed just before matching {} {}ms {} {} {} {}: {} {}", clazz.getSimpleName(),
                                                                    timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId, existingMatchedEventId, eventId,
                                                                    Generic.objectToString(event), Generic.objectToString(matchedScraperEvent));
                                                        }
                                                    } else if (existingMatchedEventId == null && BlackList.notExist(Statics.eventsMap, eventId)) {
                                                        // probably normal behaviour, eventId doesn't exist in map, so it was just removed
                                                        final long currentTime = System.currentTimeMillis();
                                                        final long timeSinceLastRemoved = currentTime - Statics.eventsMap.getTimeStampRemoved();
                                                        if (timeSinceLastRemoved < 200L) {
                                                            logger.info("event was probably removed just before matching {} {}ms {} {} {} {}", clazz.getSimpleName(),
                                                                    timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId, existingMatchedEventId, eventId);
                                                        } else if (timeSinceLastRemoved < 1_000L) {
                                                            logger.warn("event was probably removed just before matching {} {}ms {} {} {} {}", clazz.getSimpleName(),
                                                                    timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId, existingMatchedEventId, eventId);
                                                        } else {
                                                            logger.error("event was probably removed just before matching {} {}ms {} {} {} {}: {} {}", clazz.getSimpleName(),
                                                                    timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId, existingMatchedEventId, eventId,
                                                                    Generic.objectToString(event), Generic.objectToString(matchedScraperEvent));
                                                        }
                                                    } else { // at least one matched and not matched to same partner
                                                        logger.error("error at least one matched and not matched to same partner {} {} {} {} {}: {} {}", clazz.getSimpleName(),
                                                                existingMatchedScraperEventId, matchedScraperEventId, existingMatchedEventId, eventId, Generic.objectToString(event),
                                                                Generic.objectToString(matchedScraperEvent));
                                                        event.removeScraperEventId(clazz);
                                                        matchedScraperEvent.resetMatchedEventId();
                                                        if (existingMatchedScraperEventId != null && !Objects.equals(matchedScraperEventId, existingMatchedScraperEventId)) {
                                                            ScraperEvent existingScraperEvent = synchronizedScraperEventsMap.get(existingMatchedScraperEventId);
                                                            existingScraperEvent.resetMatchedEventId();
                                                        }
                                                        if (existingMatchedEventId != null && !Objects.equals(eventId, existingMatchedEventId)) {
                                                            Event existingEvent = Statics.eventsMap.get(existingMatchedEventId);
                                                            existingEvent.removeScraperEventId(clazz);
                                                        }
                                                    }
                                                } else {
                                                    logger.info("{} event too recent to be matched: {}({}) ms event: {} scraper: {}/{}", clazz.getSimpleName(), timeDifference,
                                                            Statics.INITIAL_EVENT_SAFETY_PERIOD, eventName, matchedScraperEvent.getHomeTeam(), matchedScraperEvent.getAwayTeam());
                                                    tooRecentMatchedEvents.add(event);
                                                    minTimeDifference = Math.min(minTimeDifference, Statics.INITIAL_EVENT_SAFETY_PERIOD - timeDifference);
                                                }
                                            } else {
//                                                if (clazz.equals(CoralEvent.class) && matchStatus == null) { // normal, nothing will be done
//                                                } else {
                                                logger.error("STRANGE {} matchStatus {} on matched event: {} scraper: {}/{}", clazz.getSimpleName(), matchStatus, eventName,
                                                        matchedScraperEvent.getHomeTeam(), matchedScraperEvent.getAwayTeam());
                                                BlackList.ignoreScraper(matchedScraperEvent, Generic.MINUTE_LENGTH_MILLISECONDS * 2L);
//                                                }
                                            }
                                        } else {
                                            final int listSize = errTotalMatchList.size();
                                            // some extra spaces intentionally left in the output, due to removeStrangeChars; those spaces tell me where a char was replaced
                                            if (Generic.alreadyPrintedMap.logOnce(3 * Generic.HOUR_LENGTH_MILLISECONDS, Statics.matcherSynchronizedWriter, logger, LogLevel.WARN,
                                                    "{} {} twoPotentialMatches not matched {} event: {} to {} / {}", clazz.getSimpleName(), listSize + 1, maxTotalMatch,
                                                    Formulas.removeStrangeChars(eventName), Formulas.removeStrangeChars(matchedScraperEvent.getHomeTeam()),
                                                    Formulas.removeStrangeChars(matchedScraperEvent.getAwayTeam())) != null) {
                                                for (int i = 0; i < listSize; i++) {
                                                    final ScraperEvent localScraperEvent = errScraperEventList.get(i);
                                                    logger.warn("{} twoPotentialMatches other match {} for {} / {}", clazz.getSimpleName(), errTotalMatchList.get(i),
                                                            localScraperEvent.getHomeTeam(), localScraperEvent.getAwayTeam());
                                                } // end for
                                            } else { // printed already
                                            }

                                            twoPotentialMatchesEvents.add(event);
                                            twoPotentialMatchesScraperEvents.add(matchedScraperEvent);
                                            twoPotentialMatchesScraperEvents.addAll(errScraperEventList);
                                        } // end else
                                    } else {
                                        logger.error("STRANGE {} matchedScraperEvent null in mapEventsToScraperEvents matched: {}", clazz.getSimpleName(), scraperEventsCopy.size());
                                    }
                                } else {
                                    if (matchedScraperEvent != null) {
                                        if (Statics.debugLevel.check(2, 149)) { // only print in this case
                                            Generic.alreadyPrintedMap.logOnce(logger, LogLevel.INFO, "{} not matched, closest was {} event: {} scraper: {} / {}", clazz.
                                                    getSimpleName(),
                                                    maxTotalMatch, eventName, matchedScraperEvent.getHomeTeam(), matchedScraperEvent.getAwayTeam());
                                        }

                                        if (!oneTeamMatchedPrintStrings.isEmpty()) {
                                            for (final String printedString : oneTeamMatchedPrintStrings) {
                                                if (printedString != null) {
                                                    Statics.matcherSynchronizedWriter.
                                                            writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");
                                                } else { // likely already printed, nothing to be done
                                                }
                                            } // end for
                                        } else { // no oneTeamMatches, nothing to be done
                                        }
                                    } else {
                                        logger.error("{} matchedScraperEvent null in mapEventsToScraperEvents not matched: {}", clazz.getSimpleName(), scraperEventsCopy.size());
                                    }
                                } // end else
                            } else {
                                // eventHomeTeam or eventAwayTeam are null, which means unknown separator, nothing to be done
                            }
                        } else {
                            logger.error("STRANGE event null in Statics.eventsMap {} in mapEventsToScraperEvents", clazz.getSimpleName());
                            Statics.eventsMap.removeValueAll(null);
                            // toRemoveEntrySet.add(entry);
                        }
                    } // end for event

                    final int twoPotentialMatchesEventsSize = twoPotentialMatchesEvents.size();
                    if (twoPotentialMatchesEventsSize > 0) {
                        logger.info("twoPotentialMatches: removing matched {} scrapers from {} events", clazz.getSimpleName(), twoPotentialMatchesEventsSize);
                        for (int i = 0; i < twoPotentialMatchesEventsSize; i++) {
                            final Event event = twoPotentialMatchesEvents.get(i);
                            final Long matchedScraperId = event.getScraperEventId(clazz);
                            event.removeScraperEventId(clazz);
                            if (matchedScraperId != null) {
                                logger.error("{} matchedScraperId {} found while removing twoPotentialMatches for event: {}", clazz.getSimpleName(), matchedScraperId,
                                        Generic.objectToString(event));
                                final ScraperEvent matchedScraperEvent = synchronizedScraperEventsMap.get(matchedScraperId);
                                matchedScraperEvent.resetMatchedEventId();
                            }
                        } // end for
                    }

                    final int twoPotentialMatchesScraperEventsSize = twoPotentialMatchesScraperEvents.size();
                    if (twoPotentialMatchesScraperEventsSize > 0) {
                        logger.info("twoPotentialMatches: removing matched events from {} {} scrapers", twoPotentialMatchesScraperEventsSize, clazz.getSimpleName());
                        for (int i = 0; i < twoPotentialMatchesScraperEventsSize; i++) {
                            final ScraperEvent scraperEvent = twoPotentialMatchesScraperEvents.get(i);
                            final String matchedEventId = scraperEvent.getMatchedEventId();
                            scraperEvent.resetMatchedEventId();
                            if (matchedEventId != null) {
                                logger.error("matchedEventId {} found while removing twoPotentialMatches for {} scraperEvent: {}", matchedEventId, clazz.getSimpleName(),
                                        Generic.objectToString(scraperEvent));
                                final Event matchedEvent = Statics.eventsMap.get(matchedEventId);
                                matchedEvent.removeScraperEventId(clazz);
                            }
                        } // end for
                    }
                } else {
                    logger.info("{} eventsCopy or scraperEventsCopy empty in mapEventsToScraperEvents: {} {}", clazz.getSimpleName(), eventsCopy.size(), scraperEventsCopy.size());
                }

                // Statics.eventsMap.timeStamp(); // no need to stamp here, as the events are simply matched
                final boolean fullRun = !checkAll && eventsSet == null && scraperEventsSet == null;
                final String checkAllString = checkAll ? "checkAll " : "";
                final String fullRunString = fullRun ? "fullRun " : "";

                final int toCheckSize = toCheckEvents.size();
                if (toCheckSize > 0) {
                    final HashSet<Event> notIgnoredToCheckEvents = new HashSet<>(Generic.getCollectionCapacity(toCheckEvents));
                    for (Event event : toCheckEvents) {
                        if (!event.isIgnored()) {
                            notIgnoredToCheckEvents.add(event);
                        }
                    } // end for
                    final int notIgnoredSize = notIgnoredToCheckEvents.size();
                    if (notIgnoredSize > 0) {
                        final String printedString = MessageFormatter.arrayFormat("{} mapEventsToScraperEvents {}{}toCheckEvents: {} launch: findInterestingMarkets",
                                new Object[]{clazz.getSimpleName(), checkAllString, fullRunString, notIgnoredSize}).getMessage();
                        if (checkAll || fullRun) {
                            logger.warn(printedString); // it sometimes happens if a regular run is scheduled very close (sometimes a delayed run)
                        } else {
                            logger.info(printedString);
                        }
                        Statics.threadPoolExecutor.execute(new LaunchCommandThread("findInterestingMarkets", notIgnoredToCheckEvents));
                    }
                }

                final int sizeRecent = tooRecentMatchedEvents.size();
                if (sizeRecent > 0) {
                    // minimal delay
                    minTimeDifference = Math.max(minTimeDifference, scraperThread.DELAY_GETSCRAPEREVENTS);
                    logger.info("{} mapEventsToScraperEvents {}{}tooRecentMatchedEvents: {} launch: mapEventsToScraperEvents smallDelay {}ms", clazz.getSimpleName(),
                            checkAllString, fullRunString, sizeRecent, minTimeDifference);
                    Statics.threadPoolExecutor.execute(new LaunchCommandThread("mapEventsToScraperEvents", tooRecentMatchedEvents, minTimeDifference));
                }

                logger.info("{} mapEventsToScraperEvents {}{}size: {} not matched: {} finished in {} ms", clazz.getSimpleName(), checkAllString, fullRunString,
                        Statics.eventsMap.size(), counterNotMatched, System.currentTimeMillis() - startTime);
            } // end else
        } // end else clazz == null
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        // Statics.launchCommandThreadsSet.add(this);
        switch (command) {
            case "mapEventsToScraperEvents":
                if (Statics.debugLevel.check(2, 152)) {
                    logger.info("Running mapEventsToScraperEvents");
                }
                if (delay > 0L) {
                    Generic.threadSleepSegmented(delay, 100L, Statics.mustStop);
                }
                if (!Statics.mustStop.get()) {
                    if (checkAll) {
                        LaunchCommandThread.mapEventsToScraperEvents(this.clazz, true);
                    } else if (this.eventsSet != null) {
                        LaunchCommandThread.mapEventsToScraperEvents(this.clazz, this.eventsSet);
                    } else if (this.scraperEventsSet != null) {
                        LaunchCommandThread.mapEventsToScraperEvents(this.clazz, (Set<ScraperEvent>) this.scraperEventsSet);
                    } else {
                        mapEventsToScraperEvents(this.clazz); // full run
                    }
                }
                break;
            case "findInterestingMarkets":
                if (Statics.debugLevel.check(2, 153)) {
                    logger.info("Running findInterestingMarkets");
                }
                if (delay > 0L) {
                    Generic.threadSleepSegmented(delay, 100L, Statics.mustStop);
                }
                if (!Statics.mustStop.get()) {
                    if (checkAll) {
                        FindInterestingMarkets.findInterestingMarkets(true);
                    } else if (this.eventsSet != null) {
                        FindInterestingMarkets.findInterestingMarkets(this.eventsSet);
                    } else {
                        FindInterestingMarkets.findInterestingMarkets(); // full run
                    }
                }
                break;
            case "findSafeRunners":
                if (Statics.debugLevel.check(2, 154)) {
                    logger.info("Running findSafeRunners");
                }
                if (delay > 0L) {
                    Generic.threadSleepSegmented(delay, 100L, Statics.mustStop);
                }
                if (!Statics.mustStop.get()) {
                    if (this.eventsSet != null) {
                        FindSafeRunners.findSafeRunners(this.eventsSet);
                    } else if (this.entrySet != null) {
                        FindSafeRunners.findSafeRunners(this.entrySet);
                    } else {
                        FindSafeRunners.findSafeRunners(); // full run
                    }
                }
                break;
            case "parseEventResultList":
                if (Statics.debugLevel.check(2, 174)) {
                    logger.info("Running parseEventResultList");
                }
                GetLiveMarketsThread.parseEventResultList(); // this only has full run
                break;
            default:
                logger.error("unknown operation in LaunchCommandThread: {}", command);
                break;
        } // end switch
    }
}
