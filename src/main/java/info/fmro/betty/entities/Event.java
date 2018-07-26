package info.fmro.betty.entities;

import info.fmro.betty.main.LaunchCommandThread;
import info.fmro.betty.objects.BlackList;
import info.fmro.betty.objects.ScraperEvent;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.Ignorable;
import info.fmro.shared.utility.LogLevel;
import info.fmro.shared.utility.SynchronizedMap;
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
    private Date openDate;
    private int marketCount; // taken from EventResult manually; initialization doesn't work when using Gson
    private String homeName;
    private String awayName;
    private long timeFirstSeen, timeStamp, matchedTimeStamp;

    //    public Event() {
//    }
    public Event(String id) {
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
    public synchronized int initializeCollections() {
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
            if (size >= Statics.MIN_MATCHED) {
                Collections.sort(ignoredExpirations);
                final long newIgnoredExpiration = ignoredExpirations.get(Statics.MIN_MATCHED - 1);
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
    public synchronized int setIgnored(long period) {
        final long currentTime = System.currentTimeMillis();
        return setIgnored(period, currentTime);
    }

    @Override
    public synchronized int setIgnored(long period, long currentTime) {
        final int modified = super.setIgnored(period, currentTime);

        if (modified > 0) {
            final Collection<MarketCatalogue> marketCataloguesCopy = Statics.marketCataloguesMap.valuesCopy();
            for (MarketCatalogue marketCatalogue : marketCataloguesCopy) {
                if (marketCatalogue != null) {
                    final Event event = marketCatalogue.getEventStump();
                    if (event != null) {
                        final String eventId = event.getId();
                        if (eventId != null) {
                            if (eventId.equals(this.id)) {
                                marketCatalogue.setIgnored(period, currentTime);
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
            final long realPeriod = period + currentTime - realCurrentTime + 500L; // 500ms added to account for clock errors
            final HashSet<Event> eventsSet = new HashSet<>(2);
            eventsSet.add(this);

            logger.info("ignoreEvent to check: {} delay: {} launch: findInterestingMarkets", this.id, realPeriod);
            Statics.threadPoolExecutor.execute(new LaunchCommandThread("findInterestingMarkets", eventsSet, realPeriod));
//            Statics.threadPoolExecutor.execute(new LaunchCommandThread("findSafeRunners", eventsSet, realPeriod));
        } else { // ignored was not modified, likely nothing to be done
        }

        return modified;
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized String getName() {
        return name;
    }

    public synchronized int setName(String name) {
        final int modified;
        if (this.name == null) {
            if (name == null) {
                modified = 0;
            } else {
                this.name = name;
                modified = 1;
            }
        } else if (this.name.equals(name)) {
            modified = 0;
        } else {
            this.name = name;
            modified = 1;
        }
        return modified;
    }

    public synchronized String getCountryCode() {
        return countryCode;
    }

    public synchronized int setCountryCode(String countryCode) {
        final int modified;
        if (this.countryCode == null) {
            if (countryCode == null) {
                modified = 0;
            } else {
                this.countryCode = countryCode;
                modified = 1;
            }
        } else if (this.countryCode.equals(countryCode)) {
            modified = 0;
        } else {
            this.countryCode = countryCode;
            modified = 1;
        }
        return modified;
    }

    public synchronized String getTimezone() {
        return timezone;
    }

    public synchronized int setTimezone(String timezone) {
        final int modified;
        if (this.timezone == null) {
            if (timezone == null) {
                modified = 0;
            } else {
                this.timezone = timezone;
                modified = 1;
            }
        } else if (this.timezone.equals(timezone)) {
            modified = 0;
        } else {
            this.timezone = timezone;
            modified = 1;
        }
        return modified;
    }

    public synchronized String getVenue() {
        return venue;
    }

    public synchronized int setVenue(String venue) {
        final int modified;
        if (this.venue == null) {
            if (venue == null) {
                modified = 0;
            } else {
                this.venue = venue;
                modified = 1;
            }
        } else if (this.venue.equals(venue)) {
            modified = 0;
        } else {
            logger.warn("changing venue from {} to {} in Event.setVenue for: {}", this.venue, venue, Generic.objectToString(this));
            this.venue = venue;
            modified = 1;
        }
        return modified;
    }

    public synchronized Date getOpenDate() {
        return openDate == null ? null : (Date) openDate.clone();
    }

    public synchronized int setOpenDate(Date openDate) {
        final int modified;
        if (this.openDate == null) {
            if (openDate == null) {
                modified = 0;
            } else {
                this.openDate = (Date) openDate.clone();
                modified = 1;
            }
        } else if (this.openDate.equals(openDate)) {
            modified = 0;
        } else {
            this.openDate = openDate == null ? null : (Date) openDate.clone();
            modified = 1;
        }
        return modified;
    }

    public synchronized int getMarketCount() {
        return marketCount;
    }

    public synchronized int setMarketCount(Integer marketCount) {
        final int modified;

        if (marketCount == null) {
            modified = 0;
            logger.error("null marketCount in Event.setMarketCount for: {}", Generic.objectToString(this));
        } else if (marketCount == this.marketCount) {
            modified = 0;
        } else if (marketCount < 0) {
            if (marketCount != -1) {
                logger.error("not allowed to set negative value {} for marketCount in Event: {}", marketCount, Generic.objectToString(this));
            } else { // attempt to set -1 is made when events are updated from eventStumps, normal behaviour
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
            this.marketCount = marketCount;
            modified = 1;
        }

        return modified;
    }

    public synchronized int setMarketCountStump() { // indicate the fact marketCount is not used, for eventStumps
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
        return homeName;
    }

    public synchronized int setHomeName(String homeName) {
        final int modified;
        if (homeName == null) {
            if (this.homeName == null) {
                modified = 0;
            } else { // happens with update from stump
//                logger.error("not allowed to set null value for homeName in Event: {}" + Generic.objectToString(this));
                // won't allow null to be set
                // this.homeName = homeName;
                // modified = 1;

                modified = 0;
            }
        } else if (homeName.equals(this.homeName)) {
            modified = 0;
        } else {
            this.homeName = homeName;
            modified = 1;
        }

        return modified;
    }

    public synchronized String getAwayName() {
        return awayName;
    }

    public synchronized int setAwayName(String awayName) {
        final int modified;
        if (awayName == null) {
            if (this.awayName == null) {
                modified = 0;
            } else { // happens with update from stump
//                logger.error("not allowed to set null value for awayName in Event: {}" + Generic.objectToString(this));
                // won't allow null to be set
                // this.awayName = awayName;
                // modified = 1;

                modified = 0;
            }
        } else if (awayName.equals(this.awayName)) {
            modified = 0;
        } else {
            this.awayName = awayName;
            modified = 1;
        }

        return modified;
    }

    public synchronized int parseName() {
        final int modified;
        if (name.contains(" v ")) {
            int homeModified = this.setHomeName(name.substring(0, name.indexOf(" v ")));
            int awayModified = this.setAwayName(name.substring(name.indexOf(" v ") + " v ".length()));
            modified = homeModified + awayModified;
        } else if (name.contains(" @ ")) {
            int homeModified = this.setHomeName(name.substring(0, name.indexOf(" @ ")));
            int awayModified = this.setAwayName(name.substring(name.indexOf(" @ ") + " @ ".length()));
            modified = homeModified + awayModified;
        } else {
            Generic.alreadyPrintedMap.logOnce(logger, LogLevel.WARN, "unknown event name home/away separator for: {}", name);

            // won't allow null to be set
            // homeName = null;
            // awayName = null;
            modified = 0;
        }

        return modified;
    }

    public synchronized long getTimeFirstSeen() {
        return timeFirstSeen;
    }

    public synchronized int setTimeFirstSeen(long timeFirstSeen) {
        final int modified;
        if (this.timeFirstSeen > 0) {
            if (this.timeFirstSeen > timeFirstSeen) {
                logger.error("changing timeFirstSeen event difference {} from {} to {} for: {}", this.timeFirstSeen - timeFirstSeen, this.timeFirstSeen, timeFirstSeen, Generic.objectToString(this));
                this.timeFirstSeen = timeFirstSeen;
                modified = 1;
            } else {
                modified = 0; // values are equal or new value is more recent
            }
        } else if (timeFirstSeen > 0) {
            this.timeFirstSeen = timeFirstSeen;
            modified = 1;
        } else {
            modified = 0; // values are both <= 0
        }
        return modified;
    }

    public synchronized long getTimeStamp() {
        return timeStamp;
    }

    public synchronized int setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
        return setTimeFirstSeen(this.timeStamp);
    }

    public synchronized int timeStamp() {
        this.timeStamp = System.currentTimeMillis();
        return setTimeFirstSeen(this.timeStamp);
    }

    public synchronized long getMatchedTimeStamp() {
        return matchedTimeStamp;
    }

    public synchronized int setMatchedTimeStamp(long timeStamp) {
        final int modified;
        if (this.matchedTimeStamp > 0) {
            if (this.matchedTimeStamp < timeStamp) {
//                logger.error("changing matchedTimeStamp event difference {} from {} to {} for: {}", timeStamp-this.matchedTimeStamp, this.matchedTimeStamp, timeStamp, Generic.objectToString(this));
                this.matchedTimeStamp = timeStamp;
                modified = 1;
            } else {
                modified = 0; // values are equal or new value is older
            }
        } else if (timeStamp > 0) {
            this.matchedTimeStamp = timeStamp;
            modified = 1;
        } else {
            modified = 0; // values are both <= 0
        }
        return modified;
    }

    public synchronized int matchedTimeStamp() {
        return matchedTimeStamp(true); // default behavior is true
    }

    public synchronized int matchedTimeStamp(boolean runIgnoredScrapersCheck) {
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
                        logger.error("notExist scraperEvent in getNValidScraperEventIds, timeSinceLastRemoved: {}ms for: {} {} {}", timeSinceLastRemoved, scraperClazz, scraperId,
                                     Generic.objectToString(this));
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

    public synchronized Long getScraperEventId(Class<? extends ScraperEvent> clazz) {
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

    public synchronized int setScraperEventId(Class<? extends ScraperEvent> clazz, long scraperEventId) {
        final int modified;
        long existingScraperEventId;
        if (this.scraperEventIds.containsKey(clazz)) {
            existingScraperEventId = this.scraperEventIds.get(clazz);
        } else {
            existingScraperEventId = -1;
        }

        if (existingScraperEventId >= 0) {
            if (existingScraperEventId != scraperEventId) {
                logger.error("changing matched scraper event from {} to {} for: {}", existingScraperEventId, scraperEventId, Generic.objectToString(this));

//                this.removeScraperEvent(clazz);
//                this.addScraperEvent(clazz, scraperEventId);
                this.scraperEventIds.put(clazz, scraperEventId);
                modified = 1;
            } else {
                modified = 0; // values are equal
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

    public synchronized int update(Event event) {
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

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(Event other) {
        if (other == null) {
            return AFTER;
        }
        if (this == other) {
            return EQUAL;
        }

        if (this.getClass() != other.getClass()) {
            if (this.getClass().hashCode() < other.getClass().hashCode()) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (!Objects.equals(this.id, other.id)) {
            if (this.id == null) {
                return BEFORE;
            }
            if (other.id == null) {
                return AFTER;
            }
            return this.id.compareTo(other.id);
        }

        return EQUAL;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(Object obj) {
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
