package info.fmro.betty.main;

import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.EventResult;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GetLiveMarketsThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(GetLiveMarketsThread.class);
    public static final AtomicInteger timedMapEventsCounter = new AtomicInteger(), timedFindInterestingMarketsCounter = new AtomicInteger();

    public static void parseEventResultList() {
        final long startTime = System.currentTimeMillis();
        final List<EventResult> eventResultList = SafeBetModuleMethods.getLiveEventResultList(Statics.appKey.get());
        if (eventResultList != null) {
            // Statics.timeStamps.lastParseEventResultListStamp(Generic.MINUTE_LENGTH_MILLISECONDS);
            final HashSet<Event> addedEvents = new HashSet<>(0);
            final HashSet<Event> modifiedEvents = new HashSet<>(0);
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
                            } else {
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
                        int update = existingEvent.update(event);
                        if (update > 0) {
                            modifiedEvents.add(existingEvent);
                        }
                    }
                } else {
                    Generic.alreadyPrintedMap.logOnce(logger, LogLevel.WARN, "parseEventResultList ignoring unparsed event name: {}", event.getName());
                }
            } // end for
            Statics.eventsMap.timeStamp();

            int sizeModified = modifiedEvents.size();
            if (sizeModified > 0) { // check on whether the modified events are matched is done in findInterestingMarkets
                final HashSet<Event> notIgnoredModifiedEvents = new HashSet<>(Generic.getCollectionCapacity(modifiedEvents));
                for (Event event : modifiedEvents) {
                    if (!event.isIgnored()) {
                        notIgnoredModifiedEvents.add(event);
                    }
                } // end for
                if (notIgnoredModifiedEvents.size() > 0) {
                    logger.info("parseEventResultList modifiedEvents: {} launch: findInterestingMarkets", sizeModified);
                    Statics.threadPoolExecutor.execute(new LaunchCommandThread("findInterestingMarkets", modifiedEvents));
                }
            }

            int sizeAdded = addedEvents.size();
            if (sizeAdded > 0) { // only added; modified will be checked during fullRun
                if (Statics.safeBetModuleActivated) {
                    logger.info("parseEventResultList addedEvents: {} launch: mapEventsToScraperEvents delayed", sizeAdded);
                    Statics.threadPoolExecutor.execute(new LaunchCommandThread("mapEventsToScraperEvents", addedEvents, Generic.MINUTE_LENGTH_MILLISECONDS));
                }
            }
        } else {
            logger.error("eventResultList null in parseEventResultList");

            if (!waitForSessionToken("parseEventResultList")) {
                logger.error("sessionToken seems to not have been needed in parseEventResultList");
                Generic.threadSleep(1000L); // avoid throttle, sessionToken might not have been needed
            }
        }
        logger.info("parseEventResultList finished in {} ms eventResultList size: {}", System.currentTimeMillis() - startTime,
                    eventResultList == null ? null : eventResultList.size());
    }

    public static long timedParseEventResultList() {
        long timeForNext = Statics.timeStamps.getLastParseEventResultList();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastParseEventResultListStamp(Generic.MINUTE_LENGTH_MILLISECONDS);
            Statics.threadPoolExecutor.execute(new LaunchCommandThread("parseEventResultList"));

            timeForNext = Statics.timeStamps.getLastParseEventResultList();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedMapEventsToScraperEvents() {
        long timeForNext = Statics.timeStamps.getLastMapEventsToScraperEvents();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastMapEventsToScraperEventsStamp(Generic.MINUTE_LENGTH_MILLISECONDS);
            if (timedMapEventsCounter.getAndIncrement() % 10 == 0) {
                Statics.threadPoolExecutor.execute(new LaunchCommandThread("mapEventsToScraperEvents", true));
            } else {
                Statics.threadPoolExecutor.execute(new LaunchCommandThread("mapEventsToScraperEvents"));
            }

            timeForNext = Statics.timeStamps.getLastMapEventsToScraperEvents();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedFindInterestingMarkets() {
        long timeForNext = Statics.timeStamps.getLastFindInterestingMarkets();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastFindInterestingMarketsStamp(Generic.MINUTE_LENGTH_MILLISECONDS);
            if (timedFindInterestingMarketsCounter.getAndIncrement() % 10 == 0) {
                Statics.threadPoolExecutor.execute(new LaunchCommandThread("findInterestingMarkets", true));
            } else {
                Statics.threadPoolExecutor.execute(new LaunchCommandThread("findInterestingMarkets"));
            }

            timeForNext = Statics.timeStamps.getLastFindInterestingMarkets();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static long timedFindSafeRunners() {
        long timeForNext = Statics.timeStamps.getLastFindSafeRunners();
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            Statics.timeStamps.lastFindSafeRunnersStamp(Generic.MINUTE_LENGTH_MILLISECONDS);
            Statics.threadPoolExecutor.execute(new LaunchCommandThread("findSafeRunners"));

            timeForNext = Statics.timeStamps.getLastFindSafeRunners();
            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public static boolean waitForSessionToken(String id) {
        boolean neededToken = false;
        int whileCounter = 0;
        while (Statics.needSessionToken.get() && !Statics.mustStop.get()) {
            if (whileCounter == 0) {
                logger.info("{} waiting for sessionToken...", id);
                neededToken = true;
            } else if (whileCounter >= 1000 && whileCounter % 100 == 0) {
                logger.error("{} still waiting for sessionToken {}...", id, whileCounter);
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

                timeToSleep = timedParseEventResultList();
                if (Statics.safeBetModuleActivated) {
                    timeToSleep = Math.min(timeToSleep, timedMapEventsToScraperEvents());
                }
                timeToSleep = Math.min(timeToSleep, timedFindInterestingMarkets());
                if (Statics.safeBetModuleActivated) {
                    timeToSleep = Math.min(timeToSleep, timedFindSafeRunners());
                }

                Generic.threadSleepSegmented(timeToSleep, 100L, Statics.mustStop);
            } catch (Throwable throwable) { // safety net
                logger.error("STRANGE ERROR inside GetLiveMarketsThread loop", throwable);
            }
        }

        logger.info("getLiveMarkets thread ends");
    }
}
