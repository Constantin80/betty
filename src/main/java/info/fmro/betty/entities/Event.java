package info.fmro.betty.entities;

import info.fmro.betty.enums.CommandType;
import info.fmro.betty.main.LaunchCommandThread;
import info.fmro.betty.objects.BlackList;
import info.fmro.betty.objects.ScraperEvent;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.Ignorable;
import info.fmro.shared.utility.SynchronizedMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods", "OverlyComplexClass"})
public class Event
        extends Ignorable
        implements Serializable, Comparable<Event> {
    private static final Logger logger = LoggerFactory.getLogger(Event.class);
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = -6755870038911915452L;
    private LinkedHashMap<Class<? extends ScraperEvent>, Long> scraperEventIds; // <class, scraperId>; initialization doesn't work when using Gson
    //    private transient boolean scraperEventsCached;
//    private transient LinkedHashSet<ScraperEvent> scraperEvents; // initialization doesn't work when using Gson
    private final String id;
    private String name;
    private String countryCode;
    private String timezone;
    private String venue;
    @Nullable
    private Date openDate;
    private int marketCount; // taken from EventResult manually; initialization doesn't work when using Gson
    private String homeName;
    private String awayName;
    private long timeFirstSeen, timeStamp, matchedTimeStamp;

    public Event(final String id) {
        super();
        this.id = id;
        this.initializeCollections();
    }

    //    // needed for deserialization when you need to initialize transient fields
//    private void readObject(ObjectInputStream objectInputStream)
//            throws IOException, ClassNotFoundException {
//        objectInputStream.defaultReadObject();
//
//        this.scraperEvents = new LinkedHashSet<>(0);
//    }
    @SuppressWarnings({"FinalMethod", "UnusedReturnValue"})
    final synchronized int initializeCollections() {
        int modified = 0;

        if (this.scraperEventIds == null) {
            modified++;
            this.scraperEventIds = new LinkedHashMap<>(0);
        } else { // normal behaviour, collection is already initialized
//            logger.error("scraperEventIds already initialized in Event.initializeColelctions for: {}", Generic.objectToString(this));
        }
//        if (this.scraperEvents == null) {
//            modified++;
//            this.scraperEvents = new LinkedHashSet<>(0);
//        } else {
//            logger.error("scraperEvents already initialized in Event.initializeColelctions for: {}", Generic.objectToString(this));
//        }

        return modified;
    }

    public synchronized int ignoredScrapersCheck() {
        final int modified;
        final List<Long> ignoredExpirations = new ArrayList<>(Statics.MIN_MATCHED);

        if (this.scraperEventIds != null) {
            final Set<Entry<Class<? extends ScraperEvent>, Long>> entrySet = this.scraperEventIds.entrySet();
            final Iterator<Entry<Class<? extends ScraperEvent>, Long>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                final Entry<Class<? extends ScraperEvent>, Long> entry = iterator.next();
                final Class<? extends ScraperEvent> scraperClass = entry.getKey();
                final Long scraperId = entry.getValue();
                final SynchronizedMap<Long, ? extends ScraperEvent> scraperEventsMap = Formulas.getScraperEventsMap(scraperClass);
                final ScraperEvent scraperEvent = scraperEventsMap.get(scraperId);
                if (scraperEvent == null) {
                    final long timeSinceLastRemoved = BlackList.timeSinceRemovalFromMap(scraperClass);
                    if (timeSinceLastRemoved <= Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD) {
                        logger.info("notExist scraperEvent in getNValidScraperEventIds, timeSinceLastRemoved: {}ms for: {} {} {} {}", timeSinceLastRemoved, scraperClass, scraperId, this.id, this.name);
                    } else {
                        logger.error("notExist scraperEvent in getNValidScraperEventIds, timeSinceLastRemoved: {}ms for: {} {} {}", timeSinceLastRemoved, scraperClass, scraperId, Generic.objectToString(this));
                    }
                    iterator.remove();
                    this.matchedTimeStamp(false); // removal of existing matchedScraper
                } else {
                    ignoredExpirations.add(scraperEvent.getIgnoredExpiration());
                }
            } // end while

            final int size = ignoredExpirations.size();
            //noinspection ConstantConditions,ConditionCoveredByFurtherCondition
            if (size >= Statics.MIN_MATCHED && size > 0) {
                Collections.sort(ignoredExpirations);
                final long newIgnoredExpiration = ignoredExpirations.get(size - 1);
                modified = this.setIgnored(0L, newIgnoredExpiration);
            } else { // not enough matched scrapers, nothing to be done
                modified = 0;
            }
        } else {
            logger.error("null scraperEventIds during ignoredScrapersCheck for: {}", Generic.objectToString(this));
            modified = 0;
        }

        return modified;
    }

    @Override
    public synchronized int setIgnored(final long period) {
        final long currentTime = System.currentTimeMillis();
        return setIgnored(period, currentTime);
    }

    @SuppressWarnings("OverlyNestedMethod")
    @Override
    public synchronized int setIgnored(final long period, final long startTime) {
        final int modified = super.setIgnored(period, startTime);

        if (modified > 0) {
            final Collection<MarketCatalogue> marketCataloguesCopy = Statics.marketCataloguesMap.valuesCopy();
            for (final MarketCatalogue marketCatalogue : marketCataloguesCopy) {
                if (marketCatalogue != null) {
                    final Event event = marketCatalogue.getEventStump();
                    if (event != null) {
                        final String eventId = event.getId();
                        if (eventId != null) {
                            if (eventId.equals(this.id)) {
                                marketCatalogue.setIgnored(period, startTime);
                            } else { // nothing to be done
                            }
                        } else {
                            logger.error("STRANGE null eventId during Event.setIgnored for: {}", Generic.objectToString(marketCatalogue));
                            Statics.marketCataloguesMap.removeValueAll(marketCatalogue);
                        }
                    } else {
                        logger.error("STRANGE null event during Event.setIgnored for: {}", Generic.objectToString(marketCatalogue));
                        Statics.marketCataloguesMap.removeValueAll(marketCatalogue);
                    }
                } else {
                    logger.error("STRANGE null value in marketCataloguesMap during Event.setIgnored");
                    Statics.marketCataloguesMap.removeValueAll(marketCatalogue);
                }
            } // end for

            // delayed starting of threads might no longer be necessary
            final long realCurrentTime = System.currentTimeMillis();
            final long realPeriod = period + startTime - realCurrentTime + 500L; // 500ms added to account for clock errors
            final HashSet<Event> eventsSet = new HashSet<>(2);
            eventsSet.add(this);

            logger.info("ignoreEvent to check: {} delay: {} launch: findMarkets", this.id, realPeriod);
            Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, eventsSet, realPeriod));
//            Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findSafeRunners, eventsSet, realPeriod));
        } else { // ignored was not modified, likely nothing to be done
        }

        return modified;
    }

    public synchronized String getId() {
        return this.id;
    }

    public synchronized String getName() {
        return this.name;
    }

    private synchronized int setName(final String newName) {
        final int modified;
        if (this.name == null) {
            if (newName == null) {
                modified = 0;
            } else {
                this.name = newName;
                modified = 1;
            }
        } else if (this.name.equals(newName)) {
            modified = 0;
        } else {
            this.name = newName;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized String getCountryCode() {
        return this.countryCode;
    }

    private synchronized int setCountryCode(final String newCountryCode) {
        final int modified;
        if (this.countryCode == null) {
            if (newCountryCode == null) {
                modified = 0;
            } else {
                this.countryCode = newCountryCode;
                modified = 1;
            }
        } else if (this.countryCode.equals(newCountryCode)) {
            modified = 0;
        } else {
            this.countryCode = newCountryCode;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized String getTimezone() {
        return this.timezone;
    }

    private synchronized int setTimezone(final String newTimezone) {
        final int modified;
        if (this.timezone == null) {
            if (newTimezone == null) {
                modified = 0;
            } else {
                this.timezone = newTimezone;
                modified = 1;
            }
        } else if (this.timezone.equals(newTimezone)) {
            modified = 0;
        } else {
            this.timezone = newTimezone;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized String getVenue() {
        return this.venue;
    }

    private synchronized int setVenue(final String newVenue) {
        final int modified;
        if (this.venue == null) {
            if (newVenue == null) {
                modified = 0;
            } else {
                this.venue = newVenue;
                modified = 1;
            }
        } else if (this.venue.equals(newVenue)) {
            modified = 0;
        } else {
            logger.warn("changing venue from {} to {} in Event.setVenue for: {}", this.venue, newVenue, Generic.objectToString(this));
            this.venue = newVenue;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    @Nullable
    private synchronized Date getOpenDate() {
        return this.openDate == null ? null : (Date) this.openDate.clone();
    }

    private synchronized int setOpenDate(final Date newOpenDate) {
        final int modified;
        if (this.openDate == null) {
            if (newOpenDate == null) {
                modified = 0;
            } else {
                this.openDate = (Date) newOpenDate.clone();
                modified = 1;
            }
        } else if (this.openDate.equals(newOpenDate)) {
            modified = 0;
        } else {
            this.openDate = newOpenDate == null ? null : (Date) newOpenDate.clone();
            modified = 1;
        }
        return modified;
    }

    public synchronized int getMarketCount() {
        return this.marketCount;
    }

    public synchronized int setMarketCount(final Integer newMarketCount) {
        final int modified;

        if (newMarketCount == null) {
            modified = 0;
            logger.error("null marketCount in Event.setMarketCount for: {}", Generic.objectToString(this));
        } else if (newMarketCount == this.marketCount) {
            modified = 0;
        } else if (newMarketCount < 0) {
            if (newMarketCount == -1) { // attempt to set -1 is made when events are updated from eventStumps, normal behaviour
            } else {
                logger.error("not allowed to set negative value {} for marketCount in Event: {}", newMarketCount, Generic.objectToString(this));
            }
            modified = 0;

            // if (this.marketCount < 0) {
            //     modified = 0;
            // } else {
            // won't allow negative to be set
            // this.marketCount = marketCount;
            // modified = 1;
            //     modified = 0;
            // }
        } else {
            this.marketCount = newMarketCount;
            modified = 1;
        }

        return modified;
    }

    @SuppressWarnings("UnusedReturnValue")
    synchronized int setMarketCountStump() { // indicate the fact marketCount is not used, for eventStumps
        final int modified;

        if (this.marketCount == 0) {
            this.marketCount = -1;
            modified = 1;
        } else if (this.marketCount == -1) { // normal behaviour, marketCountStump already initialized
            modified = 0;
        } else {
            logger.error("trying to setMarketCountStump on existing marketCount {} for: {}", this.marketCount, Generic.objectToString(this));
            modified = 0;
        }

        return modified;
    }

    public synchronized String getHomeName() {
        return this.homeName;
    }

    private synchronized int setHomeName(final String newHomeName) {
        final int modified;
        if (newHomeName == null) {
            modified = 0;
        } else if (newHomeName.equals(this.homeName)) {
            modified = 0;
        } else {
            this.homeName = newHomeName;
            modified = 1;
        }

        return modified;
    }

    public synchronized String getAwayName() {
        return this.awayName;
    }

    private synchronized int setAwayName(final String newAwayName) {
        final int modified;
        if (newAwayName == null) {
            modified = 0;
        } else if (newAwayName.equals(this.awayName)) {
            modified = 0;
        } else {
            this.awayName = newAwayName;
            modified = 1;
        }

        return modified;
    }

    public synchronized int parseName() {
        final int modified;
        if (this.name.contains(" v ")) {
            final int homeModified = this.setHomeName(this.name.substring(0, this.name.indexOf(" v ")));
            final int awayModified = this.setAwayName(this.name.substring(this.name.indexOf(" v ") + " v ".length()));
            modified = homeModified + awayModified;
        } else if (this.name.contains(" @ ")) {
            final int homeModified = this.setHomeName(this.name.substring(0, this.name.indexOf(" @ ")));
            final int awayModified = this.setAwayName(this.name.substring(this.name.indexOf(" @ ") + " @ ".length()));
            modified = homeModified + awayModified;
        } else { // creates clutter in the logs even if logging once
            //            Generic.alreadyPrintedMap.logOnce(logger, LogLevel.INFO, "unknown event name home/away separator for: {}", name);

            // won't allow null to be set
            // homeName = null;
            // awayName = null;
            modified = 0;
        }

        return modified;
    }

    public synchronized long getTimeFirstSeen() {
        return this.timeFirstSeen;
    }

    private synchronized int setTimeFirstSeen(final long newTimeFirstSeen) {
        final int modified;
        if (this.timeFirstSeen > 0) {
            if (this.timeFirstSeen > newTimeFirstSeen) {
                logger.error("changing timeFirstSeen event difference {} from {} to {} for: {}", this.timeFirstSeen - newTimeFirstSeen, this.timeFirstSeen, newTimeFirstSeen, Generic.objectToString(this));
                this.timeFirstSeen = newTimeFirstSeen;
                modified = 1;
            } else {
                modified = 0; // values are equal or new value is more recent
            }
        } else if (newTimeFirstSeen > 0) {
            this.timeFirstSeen = newTimeFirstSeen;
            modified = 1;
        } else {
            modified = 0; // values are both <= 0
        }
        return modified;
    }

    public synchronized long getTimeStamp() {
        return this.timeStamp;
    }

    public synchronized int setTimeStamp(final long newTimeStamp) {
        this.timeStamp = newTimeStamp;
        return setTimeFirstSeen(this.timeStamp);
    }

    public synchronized int timeStamp() {
        this.timeStamp = System.currentTimeMillis();
        return setTimeFirstSeen(this.timeStamp);
    }

    public synchronized long getMatchedTimeStamp() {
        return this.matchedTimeStamp;
    }

    public synchronized int setMatchedTimeStamp(final long newTimeStamp) {
        final int modified;
        if (this.matchedTimeStamp > 0) {
            if (this.matchedTimeStamp < newTimeStamp) {
//                logger.error("changing matchedTimeStamp event difference {} from {} to {} for: {}", timeStamp-this.matchedTimeStamp, this.matchedTimeStamp, timeStamp, Generic.objectToString(this));
                this.matchedTimeStamp = newTimeStamp;
                modified = 1;
            } else {
                modified = 0; // values are equal or new value is older
            }
        } else if (newTimeStamp > 0) {
            this.matchedTimeStamp = newTimeStamp;
            modified = 1;
        } else {
            modified = 0; // values are both <= 0
        }
        return modified;
    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized int matchedTimeStamp() {
        return matchedTimeStamp(true); // default behavior is true
    }

    private synchronized int matchedTimeStamp(final boolean runIgnoredScrapersCheck) {
        if (runIgnoredScrapersCheck) {
            this.ignoredScrapersCheck(); // check at every modification
        } else { // not running ignoredScrapersCheck, that's likely because this method was invoked from within ignoredScrapersCheck
        }

        final int modified;
        final long currentTime = System.currentTimeMillis();
        if (currentTime > this.matchedTimeStamp) {
            this.matchedTimeStamp = currentTime;
            modified = 1;
        } else {
            modified = 0;
        }
        return modified;
    }

    public synchronized int getNTotalScraperEventIds() {
        return this.scraperEventIds == null ? 0 : this.scraperEventIds.size();
    }

    public synchronized int getNValidScraperEventIds() {
        int nScraperEventIds;
//        if (!this.isScraperEventsCached()) {
//            this.cacheScraperEvents();
//        }

//        final Iterator<ScraperEvent> iterator = this.scraperEvents.iterator();
        if (this.scraperEventIds != null) {
            nScraperEventIds = 0;
            final Set<Entry<Class<? extends ScraperEvent>, Long>> entrySet = this.scraperEventIds.entrySet();
            final Iterator<Entry<Class<? extends ScraperEvent>, Long>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                final Entry<Class<? extends ScraperEvent>, Long> entry = iterator.next();
                final Class<? extends ScraperEvent> scraperClazz = entry.getKey();
                final Long scraperId = entry.getValue();
                final boolean notExistOrIgnored = BlackList.notExistOrIgnored(scraperClazz, scraperId);
                if (notExistOrIgnored) {
                    final boolean notExist = BlackList.notExist(scraperClazz, scraperId);
                    if (notExist) {
                        final long timeSinceLastRemoved = BlackList.timeSinceRemovalFromMap(scraperClazz);
//                    if (timeSinceLastRemoved <= Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD) {
//                        logger.info("notExist scraperEvent in getNValidScraperEventIds, timeSinceLastRemoved: {}ms for: {} {} {} {}", timeSinceLastRemoved, scraperClazz, scraperId,
//                                    this.id, this.name);
//                    } else {
                        logger.error("notExist scraperEvent in getNValidScraperEventIds, timeSinceLastRemoved: {}ms for: {} {} {}", timeSinceLastRemoved, scraperClazz, scraperId, Generic.objectToString(this));
//                    }
                        iterator.remove();
                        this.matchedTimeStamp(); // removal of existing matchedScraper
                    } else { // exists but ignored, normal and nothing to be done
                    }
                } else {
                    nScraperEventIds++;
                }

//            if (scraperEvent == null) {
//                iterator.remove();
//                logger.error("null scraperEvent in getNValidScraperEventIds for: {}", Generic.objectToString(this));
//            } else {
//                if (!scraperEvent.isIgnored()) {
//                    nScraperEventIds++;
//                }
//            }
            } // end while
        } else {
            nScraperEventIds = 0;
        }

        return nScraperEventIds;
    }

    public synchronized Long getScraperEventId(final Class<? extends ScraperEvent> clazz) {
//        Long returnValue;
//        if (this.scraperEventIds.containsKey(clazz)) {
//            returnValue = this.scraperEventIds.get(clazz);
//        } else {
//            returnValue = -1;
//        }

//            returnValue = 
        return this.scraperEventIds.get(clazz);
//        return returnValue;
    }

    // I don't think removeScraper is necessary; I can ignore the scraper; for maintenance at the end, when event is obsolete, I should remove the Event and all it's scrapers from the maps
//    public synchronized Long removeScraperEventId(Class<? extends ScraperEvent> clazz) {
//        Long returnValue;
//        if (this.scraperEventIds.containsKey(clazz)) {
////            this.removeScraperEvent(clazz);
//            returnValue = this.scraperEventIds.remove(clazz);
//            this.matchedTimeStamp(); // removal of existing matchedScraper
//            BlackList.checkEventNMatched(this.id);
//        } else {
//            returnValue = null;
//        }
//
//        return returnValue;
//    }

    public synchronized int setScraperEventId(final Class<? extends ScraperEvent> clazz, final long scraperEventId) {
        final int modified;
        final long existingScraperEventId = this.scraperEventIds.containsKey(clazz) ? this.scraperEventIds.get(clazz) : -1;

        if (existingScraperEventId >= 0) {
            if (existingScraperEventId == scraperEventId) {
                modified = 0; // values are equal
            } else {
                logger.error("changing matched scraper event from {} to {} for: {}", existingScraperEventId, scraperEventId, Generic.objectToString(this));

//                this.removeScraperEvent(clazz);
//                this.addScraperEvent(clazz, scraperEventId);
                this.scraperEventIds.put(clazz, scraperEventId);
                modified = 1;
            }
        } else if (scraperEventId >= 0) {
//            this.addScraperEvent(clazz, scraperEventId);
            this.scraperEventIds.put(clazz, scraperEventId);
            modified = 1;
        } else {
            modified = 0; // values are both negative
        }

        if (modified > 0) {
            this.matchedTimeStamp();
        }

        return modified;
    }

    @Nullable
    public synchronized LinkedHashMap<Class<? extends ScraperEvent>, Long> getScraperEventIds() {
        return this.scraperEventIds == null ? null : new LinkedHashMap<>(this.scraperEventIds);
    }

//    public synchronized int setScraperEventIds(LinkedHashMap<Class<? extends ScraperEvent>, Long> map) {
//        int modified = 0;
//
//        if (map == null) {
//            logger.error("trying to set null map in Event.setScraperEventIds for: {}", Generic.objectToString(this));
//        } else {
//            if (this.scraperEventIds.keySet().retainAll(map.keySet())) {
//                modified++;
//            }
//            for (Class<? extends ScraperEvent> clazz : map.keySet()) {
//                modified += setScraperEventId(clazz, map.get(clazz));
//            }
//        }
//
//        if (modified > 0) {
//            this.matchedTimeStamp();
//            BlackList.checkEventNMatched(this.id);
//        }
//
//        return modified;
//    }

    public synchronized int update(final Event event) {
        int modified;
        if (this == event) {
            logger.error("update from same object in Event.update: {}", Generic.objectToString(this));
            modified = 0;
        } else if (this.id == null ? event.getId() != null : !this.id.equals(event.getId())) {
            logger.error("mismatch eventId in Event.update: {} {}", Generic.objectToString(this), Generic.objectToString(event));
            modified = 0;
        } else {
            final long thatTimeStamp = event.getTimeStamp();
            if (this.timeStamp > thatTimeStamp) {
                final long currentTime = System.currentTimeMillis();
                if (this.timeStamp > currentTime) { // clock jump
                    logger.error("clock jump in the past of at least {} ms detected", this.timeStamp - currentTime);
                    modified = this.setTimeStamp(currentTime); // won't update the object further, as I have no guarantees on the time ordering
                } else {
                    final long timeDifference = this.timeStamp - thatTimeStamp;
                    Statics.loggerThread.addLogEntry("attempt to update Event from older by {}ms object", timeDifference);

//                    if (timeDifference < 200L) { // it happens often
////                        logger.info("attempt to update from older by {}ms object Event.update: {} {}", timeDifference, getId(), getName());
//                    } else if (timeDifference < 1_000L) {
//                        logger.warn("attempt to update from older by {}ms object Event.update: {} {}", timeDifference, getId(), getName());
//                    } else {
//                        logger.error("attempt to update from older by {}ms object Event.update: {} {}", timeDifference, Generic.objectToString(this), Generic.objectToString(event));
//                    }
                    modified = 0;
                }
            } else {
                modified = 0; // initialized
                modified += this.setTimeStamp(thatTimeStamp); // count timeFirstSeen modification
                modified += this.setTimeFirstSeen(event.getTimeFirstSeen()); // it's not always updated on the previous line, as it only updates to smaller stamps
                modified += this.setName(event.getName());
                modified += this.setCountryCode(event.getCountryCode());
                modified += this.setTimezone(event.getTimezone());
                modified += this.setVenue(event.getVenue());
                modified += this.setOpenDate(event.getOpenDate());
                modified += this.setMarketCount(event.getMarketCount());
                modified += this.setHomeName(event.getHomeName());
                modified += this.setAwayName(event.getAwayName());

                // updating the scraperEventIds won't be done at all, as this can cause issues due to racing condition
//                final LinkedHashMap<Class<? extends ScraperEvent>, Long> newScraperEventIds = event.getScraperEventIds();
//                if (newScraperEventIds != null && !newScraperEventIds.isEmpty()) {
//                    modified += this.setScraperEventIds(newScraperEventIds);
//                } else {
//                    // not allowing setting null or empty scraperEventIds map in update
//                }
                modified += this.updateIgnorable(event);
                //scraperEventsCached & scraperEvents don't need to be updated
            }
        }
        return modified;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public synchronized int compareTo(@NotNull final Event o) {
        //noinspection ConstantConditions
        if (o == null) {
            return AFTER;
        }
        if (this == o) {
            return EQUAL;
        }

        if (this.getClass() != o.getClass()) {
            return this.getClass().hashCode() < o.getClass().hashCode() ? BEFORE : AFTER;
        }
        if (!Objects.equals(this.id, o.id)) {
            if (this.id == null) {
                return BEFORE;
            }
            if (o.id == null) {
                return AFTER;
            }
            return this.id.compareTo(o.id);
        }

        return EQUAL;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Event other = (Event) obj;
        return Objects.equals(this.id, other.id);
    }
}
