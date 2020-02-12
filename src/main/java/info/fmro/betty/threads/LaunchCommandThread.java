package info.fmro.betty.threads;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.safebet.FindMarkets;
import info.fmro.betty.safebet.FindSafeRunners;
import info.fmro.betty.safebet.ScraperEvent;
import info.fmro.betty.safebet.ScraperPermanentThread;
import info.fmro.betty.stream.client.ClientHandler;
import info.fmro.betty.threads.permanent.GetLiveMarketsThread;
import info.fmro.betty.threads.permanent.MaintenanceThread;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.entities.Event;
import info.fmro.shared.entities.MarketCatalogue;
import info.fmro.shared.enums.CommandType;
import info.fmro.shared.enums.MatchStatus;
import info.fmro.shared.stream.objects.ScraperEventInterface;
import info.fmro.shared.utility.BlackList;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.Ignorable;
import info.fmro.shared.utility.LogLevel;
import info.fmro.shared.utility.SynchronizedMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"ClassWithTooManyConstructors", "OverlyComplexClass"})
public class LaunchCommandThread
        implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(LaunchCommandThread.class);
    @NotNull
    public static final Constructor<LaunchCommandThread> constructorLinkedHashSetLongMarket = Objects.requireNonNull(Generic.getConstructor(LaunchCommandThread.class, CommandType.class, LinkedHashSet.class, long.class)); // constructorMarket
    @NotNull
    public static final Constructor<LaunchCommandThread> constructorHashSetLongEvent = Objects.requireNonNull(Generic.getConstructor(LaunchCommandThread.class, CommandType.class, HashSet.class, long.class)); // constructorEvent
    private final CommandType command;
    private boolean checkAll;
    private long delay;
    public static final long RECENT_PERIOD = 1_000L;
    public static final SynchronizedMap<Event, Long> recentlyUsedEvents = new SynchronizedMap<>();
    public static final SynchronizedMap<String, Long> recentlyUsedMarketIds = new SynchronizedMap<>();
    public static final AtomicLong lastRecentlyUsedCleanStamp = new AtomicLong();
    private HashSet<Event> eventsSet;
    private TreeSet<String> marketIdsSet;
    private Set<?> scraperEventsSet;
    private LinkedHashSet<Entry<String, MarketCatalogue>> entrySet;
    private Class<? extends ScraperEvent> clazz;
//    private InterfaceConnectionThread interfaceConnectionThread;
//    private StreamObjectInterface streamObjectInterface;

    @Contract(pure = true)
    public LaunchCommandThread(final CommandType command) {
        this.command = command;
    }

//    @Contract(pure = true)
//    public LaunchCommandThread(final CommandType command, @NotNull final InterfaceConnectionThread interfaceConnectionThread, final StreamObjectInterface streamObjectInterface) {
//        this.command = command;
//        this.interfaceConnectionThread = interfaceConnectionThread;
//        this.streamObjectInterface = streamObjectInterface;
//    }

    @Contract(pure = true)
    public LaunchCommandThread(final CommandType command, final boolean checkAll) {
        this.command = command;
        this.checkAll = checkAll;
    }

    @Contract(pure = true)
    public LaunchCommandThread(final CommandType command, @NotNull final TreeSet<String> marketIdsSet) {
        this.command = command;
        this.marketIdsSet = new TreeSet<>(marketIdsSet);
    }

    @Contract(pure = true)
    public LaunchCommandThread(final CommandType command, @NotNull final HashSet<Event> eventsSet) {
        this.command = command;
        this.eventsSet = new HashSet<>(eventsSet);
    }

    @Contract(pure = true)
    public LaunchCommandThread(final CommandType command, @NotNull final HashSet<Event> eventsSet, final long delay) {
        this.command = command;
        this.eventsSet = new HashSet<>(eventsSet);
        this.delay = delay;
    }

    @Contract(pure = true)
    public LaunchCommandThread(final CommandType command, @NotNull final Set<?> scraperEventsSet, final Class<? extends ScraperEvent> clazz) {
        this.command = command;
        this.scraperEventsSet = new HashSet<>(scraperEventsSet);
        this.clazz = clazz;
    }

    @Contract(pure = true)
    public LaunchCommandThread(final CommandType command, @NotNull final LinkedHashSet<Entry<String, MarketCatalogue>> entrySet) {
        this.command = command;
        this.entrySet = new LinkedHashSet<>(entrySet);
    }

    @Contract(pure = true)
    public LaunchCommandThread(final CommandType command, @NotNull final LinkedHashSet<Entry<String, MarketCatalogue>> entrySet, final long delay) {
        this.command = command;
        this.entrySet = new LinkedHashSet<>(entrySet);
        this.delay = delay;
    }

    private static void mapEventsToScraperEvents(final Class<? extends ScraperEvent> clazz) {
        mapEventsToScraperEvents(clazz, false, null, null);
    }

    private static void mapEventsToScraperEvents(final Class<? extends ScraperEvent> clazz, final boolean checkAll) {
        mapEventsToScraperEvents(clazz, checkAll, null, null);
    }

    private static void mapEventsToScraperEvents(final Class<? extends ScraperEvent> clazz, final Set<? extends ScraperEvent> scraperEventsSet) {
        mapEventsToScraperEvents(clazz, false, null, scraperEventsSet);
    }

    private static void mapEventsToScraperEvents(final Class<? extends ScraperEvent> clazz, @SuppressWarnings("TypeMayBeWeakened") final HashSet<Event> eventsSet) {
        mapEventsToScraperEvents(clazz, false, eventsSet, null);
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod", "unchecked"})
    private static void mapEventsToScraperEvents(final Class<? extends ScraperEvent> clazz, final boolean checkAll, final Set<Event> eventsSet, final Set<? extends ScraperEvent> scraperEventsSet) {
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
            final SynchronizedMap<Long, ? extends ScraperEventInterface> synchronizedScraperEventsMap = Formulas.getScraperEventsMap(clazz);
            final ScraperPermanentThread scraperThread = Formulas.getScraperThread(clazz);

            if (synchronizedScraperEventsMap == null || scraperThread == null) { // nothing to be done, error message printed previously
            } else if (startTime - synchronizedScraperEventsMap.getTimeStamp() > Generic.HOUR_LENGTH_MILLISECONDS * 3L) {
                logger.info("scraperEventsMap too old in mapEventsToScraperEvents {}", clazz.getSimpleName());
            } else if (synchronizedScraperEventsMap.isEmpty()) {
                logger.info("scraperEventsMap empty in mapEventsToScraperEvents {}", clazz.getSimpleName());
            } else {
                final Collection<? extends ScraperEventInterface> scraperEventsCopy = scraperEventsSet != null ? scraperEventsSet : synchronizedScraperEventsMap.valuesCopy();
//                if (!checkAll) {
                final Iterator<? extends ScraperEventInterface> iteratorScraperEventsCopy = scraperEventsCopy.iterator();
                while (iteratorScraperEventsCopy.hasNext()) {
                    final ScraperEventInterface scraperEvent = iteratorScraperEventsCopy.next();
                    if (scraperEvent == null) {
                        logger.error("STRANGE null {} scraperEvent in mapEventsToScraperEvents initial removal", clazz.getSimpleName());
                        iteratorScraperEventsCopy.remove();
                        synchronizedScraperEventsMap.removeValueAll(null);
                    } else if (scraperEvent.isTooOldForMatching(startTime)) {
                        logger.info("not using tooOld scraper id {} for matching", scraperEvent.getEventId());
                        iteratorScraperEventsCopy.remove();
                    } else {
                        if (!checkAll) {
                            final String matchedEventId = scraperEvent.getMatchedEventId();
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

                final Collection<Event> eventsCopy = eventsSet != null ? eventsSet : Statics.eventsMap.valuesCopy();
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

                final Collection<Event> toCheckEvents = new HashSet<>(0);
                int counterNotMatched = 0;
                final HashSet<Event> tooRecentMatchedEvents = new HashSet<>(0);
                long minTimeDifference = Statics.INITIAL_EVENT_SAFETY_PERIOD;

                if (!eventsCopy.isEmpty() && !scraperEventsCopy.isEmpty()) {
                    final Collection<Event> twoPotentialMatchesEvents = new ArrayList<>(0);
                    final Collection<ScraperEventInterface> twoPotentialMatchesScraperEvents = new ArrayList<>(0);
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
                                ScraperEventInterface matchedScraperEvent = null;
                                double maxTotalMatch = 0, recordedHomeMatch = 0, recordedAwayMatch = 0;
                                boolean twoPotentialMatches = false;
                                final List<Double> errTotalMatchList = new ArrayList<>(0);
                                final ArrayList<ScraperEventInterface> errScraperEventList = new ArrayList<>(0);
                                final Collection<String> oneTeamMatchedPrintStrings = new HashSet<>(0);

                                for (final ScraperEventInterface scraperEvent : scraperEventsCopy) {
                                    final String homeTeam = scraperEvent.getHomeTeam();
                                    final String awayTeam = scraperEvent.getAwayTeam();
                                    final double homeMatch = Formulas.matchTeams(eventHomeTeam, homeTeam);
                                    final double awayMatch = Formulas.matchTeams(eventAwayTeam, awayTeam);
                                    final double totalMatch = homeMatch * awayMatch;

                                    if (totalMatch < Statics.threshold && (homeMatch >= Statics.threshold || awayMatch >= Statics.threshold)) {
                                        // some extra spaces intentionally left in the output, due to removeStrangeChars; those spaces tell me where a char was replaced
                                        final String printedString = Generic.alreadyPrintedMap
                                                .logOnce(3 * Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.INFO, "{} one team matched t:{} h:{} a:{} event: {} scraper: {} / {}", clazz.getSimpleName(), totalMatch, homeMatch, awayMatch,
                                                         Formulas.removeStrangeChars(eventName), Formulas.removeStrangeChars(scraperEvent.getHomeTeam()), Formulas.removeStrangeChars(scraperEvent.getAwayTeam()));
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
                                        if (twoPotentialMatches) {
                                            final int listSize = errTotalMatchList.size();
                                            // some extra spaces intentionally left in the output, due to removeStrangeChars; those spaces tell me where a char was replaced
                                            if (Generic.alreadyPrintedMap
                                                        .logOnce(3 * Generic.HOUR_LENGTH_MILLISECONDS, Statics.matcherSynchronizedWriter, logger, LogLevel.WARN, "{} {} twoPotentialMatches not matched {} event: {} to {} / {}", clazz.getSimpleName(),
                                                                 listSize + 1, maxTotalMatch, Formulas.removeStrangeChars(eventName), Formulas.removeStrangeChars(matchedScraperEvent.getHomeTeam()),
                                                                 Formulas.removeStrangeChars(matchedScraperEvent.getAwayTeam())) != null) {
                                                for (int i = 0; i < listSize; i++) {
                                                    final ScraperEventInterface localScraperEvent = errScraperEventList.get(i);
                                                    logger.warn("{} twoPotentialMatches other match {} for {} / {}", clazz.getSimpleName(), errTotalMatchList.get(i), localScraperEvent.getHomeTeam(), localScraperEvent.getAwayTeam());
                                                } // end for
                                            } else { // printed already
                                            }
                                            twoPotentialMatchesEvents.add(event);
                                            twoPotentialMatchesScraperEvents.add(matchedScraperEvent);
                                            twoPotentialMatchesScraperEvents.addAll(errScraperEventList);
                                        } // end else
                                        else {
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
                                                        final int eventModified =
                                                                event.setScraperEventId(clazz, matchedScraperEventId, MaintenanceThread.removeFromSecondaryMapsMethod, Statics.threadPoolExecutor, constructorLinkedHashSetLongMarket,
                                                                                        Statics.safeBetModuleActivated,
                                                                                        Statics.MIN_MATCHED, Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, Statics.marketCataloguesMap, Formulas.getScraperEventsMapMethod, Formulas.getIgnorableMapMethod,
                                                                                        constructorHashSetLongEvent);
                                                        final int scraperEventModified = matchedScraperEvent.setMatchedEventId(eventId);
                                                        if (eventModified > 0 && scraperEventModified > 0) {
                                                            toCheckEvents.add(event);
                                                            counterNotMatched--;
                                                            // some extra spaces intentionally left in the output, due to removeStrangeChars
                                                            // those spaces tell me where a char was replaced
                                                            final String printedString = MessageFormatter.arrayFormat("{} matched t:{} h:{} a:{} event: {} scraper: {} / {}",
                                                                                                                      new Object[]{clazz.getSimpleName(), maxTotalMatch, recordedHomeMatch, recordedAwayMatch, Formulas.removeStrangeChars(eventName),
                                                                                                                                   Formulas.removeStrangeChars(matchedScraperEvent.getHomeTeam()),
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
                                                                                                 new Object[]{eventModified, scraperEventModified, timeSinceEventMatched, timeSinceScraperEventMatched, clazz.getSimpleName(), eventId, matchedScraperEventId,
                                                                                                              Generic.objectToString(event), Generic.objectToString(matchedScraperEvent)}).getMessage();
                                                            if ((eventModified <= 0 && timeSinceEventMatched < 1_000L) ||
                                                                (scraperEventModified <= 0 && timeSinceScraperEventMatched < 1_000L)) {
                                                                logger.warn("warning {}", printedString);
                                                            } else {
                                                                logger.error("error {}", printedString);
                                                                event.setIgnored(Generic.DAY_LENGTH_MILLISECONDS, MaintenanceThread.removeFromSecondaryMapsMethod, Statics.threadPoolExecutor, constructorLinkedHashSetLongMarket,
                                                                                 Statics.safeBetModuleActivated, Statics.marketCataloguesMap, constructorHashSetLongEvent); // having an error is no joke
                                                                matchedScraperEvent.setIgnored(Generic.DAY_LENGTH_MILLISECONDS); // having an error is no joke
//                                                                event.removeScraperEventId(clazz);
//                                                                matchedScraperEvent.resetMatchedEventId();
                                                            }
                                                        }
                                                    } else if (existingMatchedScraperEventId == null && BlackList.notExist((SynchronizedMap<Long, ? extends Ignorable>) synchronizedScraperEventsMap, matchedScraperEventId)) {
                                                        // normal behaviour, matchedScraperEventId doesn't exist in map, so it was just removed
                                                        final long currentTime = System.currentTimeMillis();
                                                        final long timeSinceLastRemoved = currentTime - synchronizedScraperEventsMap.getTimeStampRemoved();
                                                        if (timeSinceLastRemoved < 200L) {
                                                            logger.info("scraperEvent was probably removed just before matching {} {}ms {} {} {} {}", clazz.getSimpleName(), timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId,
                                                                        existingMatchedEventId, eventId);
                                                        } else if (timeSinceLastRemoved < 1_000L) {
                                                            logger.warn("scraperEvent was probably removed just before matching {} {}ms {} {} {} {}", clazz.getSimpleName(), timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId,
                                                                        existingMatchedEventId, eventId);
                                                        } else {
                                                            logger.error("scraperEvent was probably removed just before matching {} {}ms {} {} {} {}: {} {}", clazz.getSimpleName(), timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId,
                                                                         existingMatchedEventId, eventId, Generic.objectToString(event), Generic.objectToString(matchedScraperEvent));
                                                        }
                                                    } else if (existingMatchedEventId == null && BlackList.notExist(Statics.eventsMap, eventId)) {
                                                        // probably normal behaviour, eventId doesn't exist in map, so it was just removed
                                                        final long currentTime = System.currentTimeMillis();
                                                        final long timeSinceLastRemoved = currentTime - Statics.eventsMap.getTimeStampRemoved();
                                                        if (timeSinceLastRemoved < 200L) {
                                                            logger.info("event was probably removed just before matching {} {}ms {} {} {} {}", clazz.getSimpleName(), timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId,
                                                                        existingMatchedEventId, eventId);
                                                        } else if (timeSinceLastRemoved < 1_000L) {
                                                            logger.warn("event was probably removed just before matching {} {}ms {} {} {} {}", clazz.getSimpleName(), timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId,
                                                                        existingMatchedEventId, eventId);
                                                        } else {
                                                            logger.error("event was probably removed just before matching {} {}ms {} {} {} {}: {} {}", clazz.getSimpleName(), timeSinceLastRemoved, existingMatchedScraperEventId, matchedScraperEventId,
                                                                         existingMatchedEventId, eventId, Generic.objectToString(event), Generic.objectToString(matchedScraperEvent));
                                                        }
                                                    } else { // at least one matched and not matched to same partner
                                                        logger.error("error at least one matched and not matched to same partner {} {} {} {} {}: {} {}", clazz.getSimpleName(), existingMatchedScraperEventId, matchedScraperEventId, existingMatchedEventId,
                                                                     eventId, Generic.objectToString(event), Generic.objectToString(matchedScraperEvent));
                                                        event.setIgnored(Generic.DAY_LENGTH_MILLISECONDS, MaintenanceThread.removeFromSecondaryMapsMethod, Statics.threadPoolExecutor, constructorLinkedHashSetLongMarket, Statics.safeBetModuleActivated,
                                                                         Statics.marketCataloguesMap, constructorHashSetLongEvent); // having an error is no joke
                                                        matchedScraperEvent.setIgnored(Generic.DAY_LENGTH_MILLISECONDS); // having an error is no joke
//                                                        event.removeScraperEventId(clazz);
//                                                        matchedScraperEvent.resetMatchedEventId();

                                                        if (existingMatchedScraperEventId != null && !Objects.equals(matchedScraperEventId, existingMatchedScraperEventId)) {
                                                            final ScraperEventInterface existingScraperEvent = synchronizedScraperEventsMap.get(existingMatchedScraperEventId);
                                                            existingScraperEvent.setIgnored(Generic.DAY_LENGTH_MILLISECONDS); // having an error is no joke
//                                                            existingScraperEvent.resetMatchedEventId();
                                                        }
                                                        if (existingMatchedEventId != null && !Objects.equals(eventId, existingMatchedEventId)) {
                                                            final Event existingEvent = Statics.eventsMap.get(existingMatchedEventId);
                                                            existingEvent.setIgnored(Generic.DAY_LENGTH_MILLISECONDS, MaintenanceThread.removeFromSecondaryMapsMethod, Statics.threadPoolExecutor, constructorLinkedHashSetLongMarket,
                                                                                     Statics.safeBetModuleActivated, Statics.marketCataloguesMap, constructorHashSetLongEvent); // having an error is no joke
//                                                            existingEvent.removeScraperEventId(clazz);
                                                        }
                                                    }
                                                } else {
                                                    logger.info("{} event too recent to be matched: {}({}) ms event: {} scraper: {}/{}", clazz.getSimpleName(), timeDifference, Statics.INITIAL_EVENT_SAFETY_PERIOD, eventName,
                                                                matchedScraperEvent.getHomeTeam(), matchedScraperEvent.getAwayTeam());
                                                    tooRecentMatchedEvents.add(event);
                                                    minTimeDifference = Math.min(minTimeDifference, Statics.INITIAL_EVENT_SAFETY_PERIOD - timeDifference);
                                                }
                                            } else {
//                                                if (clazz.equals(CoralEvent.class) && matchStatus == null) { // normal, nothing will be done
//                                                } else {
                                                logger.error("STRANGE {} matchStatus {} on matched event: {} scraper: {}/{}", clazz.getSimpleName(), matchStatus, eventName, matchedScraperEvent.getHomeTeam(), matchedScraperEvent.getAwayTeam());
                                                matchedScraperEvent.setIgnored(Generic.MINUTE_LENGTH_MILLISECONDS << 1);
//                                                }
                                            }
                                        }
                                    } else {
                                        logger.error("STRANGE {} matchedScraperEvent null in mapEventsToScraperEvents matched: {}", clazz.getSimpleName(), scraperEventsCopy.size());
                                    }
                                } else {
                                    if (matchedScraperEvent != null) {
                                        if (Statics.debugLevel.check(2, 149)) { // only print in this case
                                            Generic.alreadyPrintedMap.logOnce(logger, LogLevel.INFO, "{} not matched, closest was {} event: {} scraper: {} / {}", clazz.getSimpleName(), maxTotalMatch, eventName, matchedScraperEvent.getHomeTeam(),
                                                                              matchedScraperEvent.getAwayTeam());
                                        }

                                        if (oneTeamMatchedPrintStrings.isEmpty()) { // no oneTeamMatches, nothing to be done
                                        } else {
                                            for (final String printedString : oneTeamMatchedPrintStrings) {
                                                if (printedString != null) {
                                                    Statics.matcherSynchronizedWriter.writeAndFlush(Generic.properTimeStamp() + " " + printedString + "\r\n");
                                                } else { // likely already printed, nothing to be done
                                                }
                                            } // end for
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
                        for (final Event event : twoPotentialMatchesEvents) {
                            final Long matchedScraperId = event.getScraperEventId(clazz);
                            event.setIgnored(Generic.DAY_LENGTH_MILLISECONDS, MaintenanceThread.removeFromSecondaryMapsMethod, Statics.threadPoolExecutor, constructorLinkedHashSetLongMarket, Statics.safeBetModuleActivated, Statics.marketCataloguesMap,
                                             constructorHashSetLongEvent); // having an error is no joke
//                            event.removeScraperEventId(clazz);
                            if (matchedScraperId != null) {
                                logger.error("{} matchedScraperId {} found while removing twoPotentialMatches for event: {}", clazz.getSimpleName(), matchedScraperId, Generic.objectToString(event));
                                final ScraperEventInterface matchedScraperEvent = synchronizedScraperEventsMap.get(matchedScraperId);

                                matchedScraperEvent.setIgnored(Generic.DAY_LENGTH_MILLISECONDS); // having an error is no joke
//                                matchedScraperEvent.resetMatchedEventId();
                            }
                        } // end for
                    }

                    final int twoPotentialMatchesScraperEventsSize = twoPotentialMatchesScraperEvents.size();
                    if (twoPotentialMatchesScraperEventsSize > 0) {
                        logger.info("twoPotentialMatches: removing matched events from {} {} scrapers", twoPotentialMatchesScraperEventsSize, clazz.getSimpleName());
                        for (final ScraperEventInterface scraperEvent : twoPotentialMatchesScraperEvents) {
                            final String matchedEventId = scraperEvent.getMatchedEventId();
                            scraperEvent.setIgnored(Generic.DAY_LENGTH_MILLISECONDS); // having an error is no joke
//                            scraperEvent.resetMatchedEventId();
                            if (matchedEventId != null) {
                                logger.error("matchedEventId {} found while removing twoPotentialMatches for {} scraperEvent: {}", matchedEventId, clazz.getSimpleName(),
                                             Generic.objectToString(scraperEvent));
                                final Event matchedEvent = Statics.eventsMap.get(matchedEventId);
                                matchedEvent.setIgnored(Generic.DAY_LENGTH_MILLISECONDS, MaintenanceThread.removeFromSecondaryMapsMethod, Statics.threadPoolExecutor, constructorLinkedHashSetLongMarket, Statics.safeBetModuleActivated,
                                                        Statics.marketCataloguesMap, constructorHashSetLongEvent); // having an error is no joke
//                                matchedEvent.removeScraperEventId(clazz);
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
                    for (final Event event : toCheckEvents) {
                        if (!event.isIgnored()) {
                            notIgnoredToCheckEvents.add(event);
                        }
                    } // end for
                    final int notIgnoredSize = notIgnoredToCheckEvents.size();
                    if (notIgnoredSize > 0) {
                        final String printedString = MessageFormatter.arrayFormat("{} mapEventsToScraperEvents {}{}toCheckEvents: {} launch: findMarkets", new Object[]{clazz.getSimpleName(), checkAllString, fullRunString, notIgnoredSize}).getMessage();
                        if (checkAll || fullRun) {
                            logger.warn(printedString); // it sometimes happens if a regular run is scheduled very close (sometimes a delayed run)
                        } else {
                            logger.info(printedString);
                        }
                        Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, notIgnoredToCheckEvents));
                    }
                }

                final int sizeRecent = tooRecentMatchedEvents.size();
                if (sizeRecent > 0) {
                    // minimal delay
                    minTimeDifference = Math.max(minTimeDifference, scraperThread.delayGetScraperEvents);
                    logger.info("{} mapEventsToScraperEvents {}{}tooRecentMatchedEvents: {} launch: mapEventsToScraperEvents smallDelay {}ms", clazz.getSimpleName(), checkAllString, fullRunString, sizeRecent, minTimeDifference);
                    Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.mapEventsToScraperEvents, tooRecentMatchedEvents, minTimeDifference));
                }

                logger.info("{} mapEventsToScraperEvents {}{}size: {} not matched: {} finished in {} ms", clazz.getSimpleName(), checkAllString, fullRunString, Statics.eventsMap.size(), counterNotMatched, System.currentTimeMillis() - startTime);
            } // end else
        } // end else clazz == null
    }

    private static void checkRecentlyUsed(@NotNull @SuppressWarnings("TypeMayBeWeakened") final HashSet<Event> eventsSet) {
        final long currentTime = System.currentTimeMillis();
        cleanRecentlyUsed(currentTime);

        final Iterator<Event> iterator = eventsSet.iterator();
        while (iterator.hasNext()) {
            final Event event = iterator.next();
            final Long lastUsedTime = recentlyUsedEvents.get(event);
            final long lastUsedTimePrimitive = lastUsedTime == null ? 0 : lastUsedTime;
            if (currentTime - lastUsedTimePrimitive <= RECENT_PERIOD) {
                iterator.remove();
            } else {
                recentlyUsedEvents.put(event, currentTime, true);
            }
        } // end while
    }

    private static void checkRecentlyUsed(@NotNull @SuppressWarnings("TypeMayBeWeakened") final TreeSet<String> marketIdsSet) {
        final long currentTime = System.currentTimeMillis();
        cleanRecentlyUsed(currentTime);

        final Iterator<String> iterator = marketIdsSet.iterator();
        while (iterator.hasNext()) {
            final String marketId = iterator.next();
            final Long lastUsedTime = recentlyUsedMarketIds.get(marketId);
            final long lastUsedTimePrimitive = lastUsedTime == null ? 0 : lastUsedTime;
            if (currentTime - lastUsedTimePrimitive <= RECENT_PERIOD) {
                iterator.remove();
            } else {
                recentlyUsedMarketIds.put(marketId, currentTime, true);
            }
        } // end while
    }

    private static void cleanRecentlyUsedMap(@NotNull final SynchronizedMap<?, Long> synchronizedMap) {
        final Collection<Long> valuesCopy = synchronizedMap.valuesCopy();
        final long currentTime = System.currentTimeMillis();
        for (final Long recentTimeStamp : valuesCopy) {
            if (recentTimeStamp == null || currentTime - recentTimeStamp > RECENT_PERIOD) {
                synchronizedMap.removeValueAll(recentTimeStamp);
            }
        }
    }

    public static void cleanRecentlyUsed() {
        cleanRecentlyUsed(System.currentTimeMillis());
    }

    private static void cleanRecentlyUsed(final long currentTime) {
        if (currentTime - lastRecentlyUsedCleanStamp.get() > Generic.MINUTE_LENGTH_MILLISECONDS * 10L) {
            lastRecentlyUsedCleanStamp.set(currentTime);
            cleanRecentlyUsedMap(recentlyUsedEvents);
            cleanRecentlyUsedMap(recentlyUsedMarketIds);
        } else { // no yet the time for cleaning, nothing to be done
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        // Statics.launchCommandThreadsSet.add(this);
        if (this.delay > 0L) {
            if (!Statics.safeBetModuleActivated) {
                logger.error("delay present and probably not necessary without safeBets");
            }
            Generic.threadSleepSegmented(this.delay, 100L, Statics.mustStop);
        }
        //noinspection SwitchStatementDensity
        switch (this.command) {
            case mapEventsToScraperEvents:
                if (!Statics.safeBetModuleActivated) {
                    logger.error("mapEventsToScraperEvents without safeBetModuleActivated: {} {} {} {}", this.delay, this.checkAll, Generic.objectToString(this.eventsSet), Generic.objectToString(this.scraperEventsSet));
                }
                if (Statics.debugLevel.check(2, 152)) {
                    logger.info("Running mapEventsToScraperEvents");
                }
                if (!Statics.mustStop.get()) {
                    if (this.checkAll) {
                        mapEventsToScraperEvents(this.clazz, true);
                    } else if (this.eventsSet != null) {
                        mapEventsToScraperEvents(this.clazz, this.eventsSet);
                    } else if (this.scraperEventsSet != null) {
                        mapEventsToScraperEvents(this.clazz, (Set<ScraperEvent>) this.scraperEventsSet);
                    } else {
                        mapEventsToScraperEvents(this.clazz); // full run
                    }
                }
                break;
            case findSafeRunners:
                if (!Statics.safeBetModuleActivated) {
                    logger.error("findSafeRunners without safeBetModuleActivated: {} {} {}", this.delay, Generic.objectToString(this.eventsSet), Generic.objectToString(this.entrySet));
                }
                if (Statics.debugLevel.check(2, 154)) {
                    logger.info("Running findSafeRunners");
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
            case findMarkets:
                if (Statics.debugLevel.check(2, 153)) {
                    logger.info("Running findMarkets");
                }
                if (!Statics.mustStop.get()) {
                    if (this.checkAll) {
                        FindMarkets.findMarkets(true);
                    } else if (this.eventsSet != null) {
                        checkRecentlyUsed(this.eventsSet);
                        FindMarkets.findMarkets(this.eventsSet);
                    } else if (this.marketIdsSet != null) {
                        checkRecentlyUsed(this.marketIdsSet);
                        FindMarkets.findMarkets(this.marketIdsSet);
                    } else {
                        FindMarkets.findMarkets(); // full run
                    }
                }
                break;
            case checkEventResultList:
                if (Statics.debugLevel.check(2, 174)) {
                    logger.info("Running parseEventResultList");
                }
                GetLiveMarketsThread.parseEventList(); // this only has full run
                break;
            case streamMarkets:
                ClientHandler.streamAllMarkets();
                break;
//            case sendObject:
//                this.interfaceConnectionThread.sendObject(this.streamObjectInterface);
//                break;
            default:
                logger.error("unknown operation in LaunchCommandThread: {}", this.command);
                break;
        } // end switch
    }
}
