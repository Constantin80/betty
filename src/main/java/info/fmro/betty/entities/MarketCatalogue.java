package info.fmro.betty.entities;

import info.fmro.betty.enums.CommandType;
import info.fmro.betty.main.LaunchCommandThread;
import info.fmro.betty.main.MaintenanceThread;
import info.fmro.betty.objects.ParsedMarket;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.Ignorable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

public class MarketCatalogue
        extends Ignorable
        implements Serializable, Comparable<MarketCatalogue> {

    private static final Logger logger = LoggerFactory.getLogger(MarketCatalogue.class);
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = 1172556202262757207L;
    private final String marketId;
    private String marketName;
    private Date marketStartTime;
    private MarketDescription description;
    private Double totalMatched;
    private List<RunnerCatalog> runners;
    private EventType eventType;
    private Competition competition;
    private Event event;
    private ParsedMarket parsedMarket;
    private long timeStamp;

    //    public MarketCatalogue() {
//    }
    public MarketCatalogue(String marketId) {
        this.marketId = marketId;
    }

    @Override
    public synchronized int setIgnored(long period) {
        final long currentTime = System.currentTimeMillis();
        return setIgnored(period, currentTime);
    }

    @Override
    public synchronized int setIgnored(long period, long currentTime) {
        final int modified = super.setIgnored(period, currentTime);

        if (modified > 0 && this.isIgnored()) {
            MaintenanceThread.removeFromSecondaryMaps(marketId);

            // delayed starting of threads might no longer be necessary
            final long realCurrentTime = System.currentTimeMillis();
            final long realPeriod = period + currentTime - realCurrentTime + 500L; // 500ms added to account for clock errors
            final LinkedHashSet<Entry<String, MarketCatalogue>> marketCatalogueEntriesSet = new LinkedHashSet<>(2);
            marketCatalogueEntriesSet.add(new SimpleEntry<>(this.marketId, this));

            if (Statics.safeBetModuleActivated) {
                logger.info("ignoreMarketCatalogue to check: {} delay: {} launch: findSafeRunners", this.marketId, realPeriod);
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findSafeRunners, marketCatalogueEntriesSet, realPeriod));
            }
        } else { // ignored was not modified or market is not ignored, likely nothing to be done
        }

        return modified;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized int getNRunners() {
        return this.runners == null ? 0 : runners.size();
    }

    public synchronized String getMarketName() {
        return marketName;
    }

    public synchronized int setMarketName(String marketName) {
        final int modified;
        if (this.marketName == null) {
            if (marketName == null) {
                modified = 0;
            } else {
                this.marketName = marketName;
                modified = 1;
            }
        } else if (this.marketName.equals(marketName)) {
            modified = 0;
        } else {
            this.marketName = marketName;
            modified = 1;
        }
        return modified;
    }

    public synchronized Date getMarketStartTime() {
        return marketStartTime == null ? null : (Date) marketStartTime.clone();
    }

    public synchronized int setMarketStartTime(Date marketStartTime) {
        final int modified;
        if (this.marketStartTime == null) {
            if (marketStartTime == null) {
                modified = 0;
            } else {
                this.marketStartTime = (Date) marketStartTime.clone();
                modified = 1;
            }
        } else if (this.marketStartTime.equals(marketStartTime)) {
            modified = 0;
        } else {
            this.marketStartTime = marketStartTime == null ? null : (Date) marketStartTime.clone();
            modified = 1;
        }
        return modified;
    }

    public synchronized MarketDescription getDescription() {
        return description;
    }

    public synchronized int setDescription(MarketDescription description) {
        final int modified;
        if (this.description == null) {
            if (description == null) {
                modified = 0;
            } else {
                this.description = description;
                modified = 1;
            }
        } else if (this.description.equals(description)) {
            modified = 0;
        } else {
            this.description = description;
            modified = 1;
        }
        return modified;
    }

    public synchronized Double getTotalMatched() {
        return totalMatched;
    }

    public synchronized int setTotalMatched(Double totalMatched) {
        final int modified;
        if (this.totalMatched == null) {
            if (totalMatched == null) {
                modified = 0;
            } else {
                this.totalMatched = totalMatched;
                modified = 1;
            }
        } else if (this.totalMatched.equals(totalMatched)) {
            modified = 0;
        } else {
            this.totalMatched = totalMatched;
            modified = 1;
        }
        return modified;
    }

    public synchronized List<RunnerCatalog> getRunners() {
        return runners == null ? null : new ArrayList<>(runners);
    }

    public synchronized int setRunners(List<RunnerCatalog> runners) {
        final int modified;
        if (this.runners == null) {
            if (runners == null) {
                modified = 0;
            } else {
                this.runners = new ArrayList<>(runners);
                modified = 1;
            }
        } else if (this.runners.equals(runners)) {
            modified = 0;
        } else {
            this.runners = runners == null ? null : new ArrayList<>(runners);
            modified = 1;
        }
        return modified;
    }

    public synchronized EventType getEventType() {
        return eventType;
    }

    public synchronized int setEventType(EventType eventType) {
        final int modified;
        if (this.eventType == null) {
            if (eventType == null) {
                modified = 0;
            } else {
                this.eventType = eventType;
                modified = 1;
            }
        } else if (this.eventType.equals(eventType)) {
            modified = 0;
        } else {
            this.eventType = eventType;
            modified = 1;
        }
        return modified;
    }

    public synchronized Competition getCompetition() {
        return competition;
    }

    public synchronized int setCompetition(Competition competition) {
        final int modified;
        if (this.competition == null) {
            if (competition == null) {
                modified = 0;
            } else {
                this.competition = competition;
                modified = 1;
            }
        } else if (this.competition.equals(competition)) {
            modified = 0;
        } else {
            this.competition = competition;
            modified = 1;
        }
        return modified;
    }

    public synchronized Event getEventStump() {
        // even with stump, because it's used during update, I still need some initialization
        if (event != null) {
            if (this.timeStamp > 0L) {
                event.setTimeStamp(this.timeStamp);
            } else {
                event.timeStamp();
            }
            event.setMarketCountStump();
        } else { // event null, not much to be done
        }
        return event;
    }

    private synchronized Event getEvent() { // I will only keep stump Event in MarketCatalogue
        if (event != null) {
            if (this.timeStamp > 0L) {
                event.setTimeStamp(this.timeStamp);
            } else {
                event.timeStamp();
            }
            event.setMarketCountStump();
            event.initializeCollections();
        } else { // event null, not much to be done
        }
        return event;
    }

    public synchronized int setEvent(Event event) { // doesn't set equal Events and only does an equality check on id
        final int modified;
        if (this.event == null) {
            if (event == null) {
                modified = 0;
            } else {
                this.event = event;
                modified = 1;
            }
        } else if (this.event.equals(event)) {
            modified = 0;
        } else {
            this.event = event;
            modified = 1;
        }
        return modified;
    }

    public synchronized ParsedMarket getParsedMarket() {
        return parsedMarket;
    }

    public synchronized int setParsedMarket(ParsedMarket parsedMarket) {
        final int modified;
        if (parsedMarket == null) {
            if (this.parsedMarket == null) {
                if (Formulas.isMarketType(this, Statics.supportedEventTypes)) { // happens often enough that it can clutter my logs, won't print
//                    Generic.alreadyPrintedMap.logOnce(Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.INFO, "trying to set null over null value for parsedMarket in MarketCatalogue: {}", this.marketId);
                } else { // normal that market is not parsed and setting null is attempted
                }
                modified = 0;
            } else {
                // won't allow null to be set
                // this.parsedMarket = parsedMarket;
                // modified = 1;

                logger.error("not allowed to set null value for parsedMarket in MarketCatalogue: {}", Generic.objectToString(this));
                modified = 0;
            }
        } else if (parsedMarket.equals(this.parsedMarket)) {
            modified = 0;
        } else {
            this.parsedMarket = parsedMarket;
            modified = 1;
        }

        return modified;
    }

    public synchronized long getTimeStamp() {
        return timeStamp;
    }

    public synchronized void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public synchronized void timeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

//    public synchronized int resetTempRemoved(Event event) {
//        final int modified;
//        if (this.isTempRemoved()) {
//            final int nMatched;
//            if (event != null) {
//                nMatched = event.getNValidScraperEventIds();
//            } else {
//                nMatched = 0;
//            }
//
//            if (nMatched >= Statics.MIN_MATCHED) {
//                modified = super.resetTempRemoved();
//            } else {
//                logger.error("attempted marketCatalogue.resetTempRemoved on for attached event with insufficient {} scrapers for: {} {}", nMatched, Generic.objectToString(this),
//                             Generic.objectToString(event));
//                modified = 0;
//            }
//        } else {
//            modified = 0;
//        }
//
//        return modified;
//    }

    public synchronized int update(MarketCatalogue marketCatalogue) {
        int modified;
        if (this == marketCatalogue) {
            logger.error("update from same object in MarketCatalogue.update: {}", Generic.objectToString(this));
            modified = 0;
        } else if (this.marketId == null ? marketCatalogue.getMarketId() != null : !this.marketId.equals(marketCatalogue.getMarketId())) {
            logger.error("mismatch marketId in MarketCatalogue.update: {} {}", Generic.objectToString(this), Generic.objectToString(marketCatalogue));
            modified = 0;
        } else {
            final long thatTimeStamp = marketCatalogue.getTimeStamp();

            if (this.timeStamp > thatTimeStamp) {
                final long currentTime = System.currentTimeMillis();
                if (this.timeStamp > currentTime) { // clock jump
                    logger.error("clock jump in the past of at least {} ms detected", this.timeStamp - currentTime);
                    this.timeStamp = currentTime; // won't update the object further, as I have no guarantees on the time ordering
                } else {
                    final long timeDifference = this.timeStamp - thatTimeStamp;
                    Statics.loggerThread.addLogEntry("attempt to update MarketCatalogue from older by {}ms object", timeDifference);

//                    if (timeDifference > 1_000L) {
//                        logger.error("attempt to update MarketCatalogue from older by {} ms object: {} {}", timeDifference, Generic.objectToString(this),
//                                Generic.objectToString(marketCatalogue));
//                    } else { // happens due to concurrent threads and high processor load; no need to print error message
//                    }
                }
                modified = 0;
            } else {
                modified = 0; // initialized
                this.timeStamp = thatTimeStamp; // this doesn't count as modification

                modified += this.setMarketName(marketCatalogue.getMarketName());
                modified += this.setMarketStartTime(marketCatalogue.getMarketStartTime());
                modified += this.setDescription(marketCatalogue.getDescription());
                modified += this.setTotalMatched(marketCatalogue.getTotalMatched());
                modified += this.setRunners(marketCatalogue.getRunners());
                modified += this.setEventType(marketCatalogue.getEventType());
                modified += this.setCompetition(marketCatalogue.getCompetition());
                modified += this.setEvent(marketCatalogue.getEvent());
                modified += this.setParsedMarket(marketCatalogue.getParsedMarket());
                modified += this.updateIgnorable(marketCatalogue);
            }
        }
        return modified;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(MarketCatalogue other) {
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
        if (!Objects.equals(this.marketId, other.marketId)) {
            if (this.marketId == null) {
                return BEFORE;
            }
            if (other.marketId == null) {
                return AFTER;
            }
            return this.marketId.compareTo(other.marketId);
        }

        return EQUAL;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.marketId);
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
        final MarketCatalogue other = (MarketCatalogue) obj;
        return Objects.equals(this.marketId, other.marketId);
    }
}
