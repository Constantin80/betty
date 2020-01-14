package info.fmro.betty.threads.permanent;

import info.fmro.betty.betapi.ApiNGJRescriptDemo;
import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.EventResult;
import info.fmro.shared.enums.CommandType;
import info.fmro.betty.main.Betty;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.LaunchCommandThread;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ReuseOfLocalVariable")
public class GetLiveMarketsThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(GetLiveMarketsThread.class);
    public static final AtomicInteger timedMapEventsCounter = new AtomicInteger(), timedFindInterestingMarketsCounter = new AtomicInteger();

    public static void parseEventList() {
        final long startTime = System.currentTimeMillis();
        final List<EventResult> eventResultList = ApiNGJRescriptDemo.getEventList(Statics.appKey.get());
        final long receivedListFromServerTime = System.currentTimeMillis();
        if (eventResultList != null) {
            final Collection<Event> addedEvents = new HashSet<>(0);
            final Collection<Event> modifiedEvents = new HashSet<>(0);
            for (final EventResult eventResult : eventResultList) {
                final Event event = eventResult.getEvent();
                final String eventId = event.getId();
                final Event existingEvent;

                if (Statics.eventsMap.containsKey(eventId)) {
                    existingEvent = Statics.eventsMap.get(eventId);
                } else {
                    if (event.isIgnored(startTime)) { // these events don't already exist in map, so they can't be ignored already; this would be an error
                        logger.error("blackListed event added: {}", eventId);
                    } else { // normal case, nothing to be done
                    }

                    existingEvent = Statics.eventsMap.putIfAbsent(eventId, event);
                    if (existingEvent == null) { // event was added, no previous event existed, double check to avoid racing issues
                        addedEvents.add(event);
                    } else {
                        final long eventStamp = event.getTimeStamp();
                        final long existingEventStamp = existingEvent.getTimeStamp();
                        final long timeDifference = eventStamp - existingEventStamp;
                        if (timeDifference < 1_000L) {
                            logger.info("existingEvent found {} ms during put double check: {} {}", timeDifference, Generic.objectToString(existingEvent), Generic.objectToString(event));
                        } else {
                            logger.error("existingEvent found {} ms during put double check: {} {}", timeDifference, Generic.objectToString(existingEvent), Generic.objectToString(event));
                        }
                    }
                }

                if (existingEvent != null) {
                    final int update = existingEvent.update(event);
                    if (update > 0) {
                        modifiedEvents.add(existingEvent);
                    }
                }
            } // end for
            Statics.eventsMap.timeStamp();

            final Collection<Event> notIgnoredModifiedEvents = new HashSet<>(Generic.getCollectionCapacity(modifiedEvents));
            final int sizeModified = modifiedEvents.size();
            if (sizeModified > 0) {
                for (final Event event : modifiedEvents) {
                    if (!event.isIgnored()) {
                        notIgnoredModifiedEvents.add(event);
                    }
                } // end for
            }

            final int sizeNotIgnoredModified = notIgnoredModifiedEvents.size();
            final int sizeAdded = addedEvents.size();
            final int sizeToCheck = sizeNotIgnoredModified + sizeAdded;
            if (sizeToCheck > 0) {
                final HashSet<Event> toCheckEvents = new HashSet<>(Generic.getCollectionCapacity(sizeToCheck));
                toCheckEvents.addAll(notIgnoredModifiedEvents);
                toCheckEvents.addAll(addedEvents);
                logger.info("parseEventResultList toCheckEvents: {}({}+{}) launch: findMarkets", sizeToCheck, sizeAdded, sizeNotIgnoredModified);
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, toCheckEvents));
            }
        } else {
            if (Statics.mustStop.get() && Statics.needSessionToken.get()) { // normal to happen during program stop, if not logged in
            } else {
                logger.error("eventResultList null in parseEventList");

                if (!waitForSessionToken("parseEventList")) {
                    logger.error("sessionToken seems to not have been needed in parseEventList");
                    Generic.threadSleep(1_000L); // avoid throttle, sessionToken might not have been needed
                }
            }
        }
        final long endTime = System.currentTimeMillis();
        logger.info("parseEventList eventResultList size: {} finished in {} ms of which {} ms spent parsing", eventResultList == null ? null : eventResultList.size(), endTime - startTime, endTime - receivedListFromServerTime);
    }

    private static long timedCheckEventResultList() {
        long timeForNext = Statics.timeStamps.getLastParseEventResultList();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastParseEventResultListStamp(5L * Generic.MINUTE_LENGTH_MILLISECONDS);
            Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.checkEventResultList));

            timeForNext = Statics.timeStamps.getLastParseEventResultList();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedFindMarkets() {
        long timeForNext = Statics.timeStamps.getLastFindInterestingMarkets();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastFindInterestingMarketsStamp(2L * Generic.MINUTE_LENGTH_MILLISECONDS);
            if (timedFindInterestingMarketsCounter.getAndIncrement() % 10 == 0) {
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, true));
            } else {
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets));
            }

            timeForNext = Statics.timeStamps.getLastFindInterestingMarkets();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedMapEventsToScraperEvents() {
        long timeForNext = Statics.timeStamps.getLastMapEventsToScraperEvents();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastMapEventsToScraperEventsStamp(Generic.MINUTE_LENGTH_MILLISECONDS);
            if (timedMapEventsCounter.getAndIncrement() % 10 == 0) {
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.mapEventsToScraperEvents, true));
            } else {
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.mapEventsToScraperEvents));
            }

            timeForNext = Statics.timeStamps.getLastMapEventsToScraperEvents();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedFindSafeRunners() {
        long timeForNext = Statics.timeStamps.getLastFindSafeRunners();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastFindSafeRunnersStamp(Generic.MINUTE_LENGTH_MILLISECONDS);
            Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findSafeRunners));

            timeForNext = Statics.timeStamps.getLastFindSafeRunners();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedStreamMarkets() {
        long timeForNext = Statics.timeStamps.getLastStreamMarkets();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastStreamMarketsStamp(Generic.MINUTE_LENGTH_MILLISECONDS << 1);
            Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.streamMarkets));

            timeForNext = Statics.timeStamps.getLastStreamMarkets();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static boolean waitForSessionToken(final String id) {
        boolean neededToken = false;
        int whileCounter = 0;
        while (Statics.needSessionToken.get() && !Statics.mustStop.get()) {
            if (whileCounter == 0) {
                Generic.alreadyPrintedMap.logOnce(20_000L, logger, LogLevel.INFO, "{} waiting for sessionToken...", id);
                neededToken = true;
            } else if (whileCounter >= 2000 && whileCounter % 500 == 0) {
                Generic.alreadyPrintedMap.logOnce(20_000L, logger, LogLevel.ERROR, "{} still waiting for sessionToken {}...", id, whileCounter);
//                logger.error("{} still waiting for sessionToken {}...", id, whileCounter);
            }
            whileCounter++;
            Generic.threadSleep(10L);
        }
        return neededToken;
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "getLiveMarkets thread");
                }
                waitForSessionToken("GetLiveMarketsThread main");

                long timeToSleep;

                timeToSleep = timedCheckEventResultList();
                if (Statics.safeBetModuleActivated) {
                    timeToSleep = Math.min(timeToSleep, timedMapEventsToScraperEvents());
                }
                timeToSleep = Math.min(timeToSleep, timedFindMarkets());
                if (Statics.safeBetModuleActivated) {
                    timeToSleep = Math.min(timeToSleep, timedFindSafeRunners());
                }
                timeToSleep = Math.min(timeToSleep, timedStreamMarkets());

                Generic.threadSleepSegmented(timeToSleep, 100L, Statics.mustStop);
            } catch (Throwable throwable) { // safety net
                logger.error("STRANGE ERROR inside GetLiveMarketsThread loop", throwable);
            }
        }

        logger.info("getLiveMarkets thread ends");
    }
}
