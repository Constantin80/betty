package info.fmro.betty.entities;

import info.fmro.betty.enums.CommandType;
import info.fmro.betty.main.LaunchCommandThread;
import info.fmro.betty.main.MaintenanceThread;
import info.fmro.betty.objects.ParsedMarket;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.Ignorable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

@SuppressWarnings("OverlyComplexClass")
public class MarketCatalogue
        extends Ignorable
        implements Serializable, Comparable<MarketCatalogue> {
    private static final Logger logger = LoggerFactory.getLogger(MarketCatalogue.class);
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = 1172556202262757207L;
    private final String marketId;
    private String marketName;
    @Nullable
    private Date marketStartTime;
    private MarketDescription description;
    private Double totalMatched;
    @Nullable
    private List<RunnerCatalog> runners;
    private EventType eventType;
    private Competition competition;
    private Event event;
    private ParsedMarket parsedMarket; // only place where stored
    private long timeStamp;

    //    public MarketCatalogue() {
//    }
    public MarketCatalogue(final String marketId) {
        super();
        this.marketId = marketId;
    }

    @Override
    public synchronized int setIgnored(final long period) {
        final long currentTime = System.currentTimeMillis();
        return setIgnored(period, currentTime);
    }

    @Override
    public synchronized int setIgnored(final long period, final long startTime) {
        final int modified = super.setIgnored(period, startTime);

        if (modified > 0 && this.isIgnored()) {
            MaintenanceThread.removeFromSecondaryMaps(this.marketId);

            // delayed starting of threads might no longer be necessary
            final long realCurrentTime = System.currentTimeMillis();
            final long realPeriod = period + startTime - realCurrentTime + 500L; // 500ms added to account for clock errors
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
        return this.marketId;
    }

    public synchronized int getNRunners() {
        return this.runners == null ? 0 : this.runners.size();
    }

    public synchronized String getMarketName() {
        return this.marketName;
    }

    private synchronized int setMarketName(final String newMarketName) {
        final int modified;
        if (this.marketName == null) {
            if (newMarketName == null) {
                modified = 0;
            } else {
                this.marketName = newMarketName;
                modified = 1;
            }
        } else if (this.marketName.equals(newMarketName)) {
            modified = 0;
        } else {
            this.marketName = newMarketName;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    @Nullable
    private synchronized Date getMarketStartTime() {
        return this.marketStartTime == null ? null : (Date) this.marketStartTime.clone();
    }

    private synchronized int setMarketStartTime(final Date newMarketStartTime) {
        final int modified;
        if (this.marketStartTime == null) {
            if (newMarketStartTime == null) {
                modified = 0;
            } else {
                this.marketStartTime = (Date) newMarketStartTime.clone();
                modified = 1;
            }
        } else if (this.marketStartTime.equals(newMarketStartTime)) {
            modified = 0;
        } else {
            this.marketStartTime = newMarketStartTime == null ? null : (Date) newMarketStartTime.clone();
            modified = 1;
        }
        return modified;
    }

    public synchronized MarketDescription getDescription() {
        return this.description;
    }

    private synchronized int setDescription(final MarketDescription newDescription) {
        final int modified;
        if (this.description == null) {
            if (newDescription == null) {
                modified = 0;
            } else {
                this.description = newDescription;
                modified = 1;
            }
        } else if (this.description.equals(newDescription)) {
            modified = 0;
        } else {
            this.description = newDescription;
            modified = 1;
        }
        return modified;
    }

    public synchronized Double getTotalMatched() {
        return this.totalMatched;
    }

    public synchronized int setTotalMatched(final Double newTotalMatched) {
        final int modified;
        if (this.totalMatched == null) {
            if (newTotalMatched == null) {
                modified = 0;
            } else {
                this.totalMatched = newTotalMatched;
                modified = 1;
            }
        } else if (this.totalMatched.equals(newTotalMatched)) {
            modified = 0;
        } else {
            this.totalMatched = newTotalMatched;
            modified = 1;
        }
        return modified;
    }

    @Nullable
    public synchronized List<RunnerCatalog> getRunners() {
        return this.runners == null ? null : new ArrayList<>(this.runners);
    }

    private synchronized int setRunners(final List<? extends RunnerCatalog> newRunners) {
        final int modified;
        if (this.runners == null) {
            if (newRunners == null) {
                modified = 0;
            } else {
                this.runners = new ArrayList<>(newRunners);
                modified = 1;
            }
        } else if (this.runners.equals(newRunners)) {
            modified = 0;
        } else {
            this.runners = newRunners == null ? null : new ArrayList<>(newRunners);
            modified = 1;
        }
        return modified;
    }

    public synchronized EventType getEventType() {
        return this.eventType;
    }

    private synchronized int setEventType(final EventType newEventType) {
        final int modified;
        if (this.eventType == null) {
            if (newEventType == null) {
                modified = 0;
            } else {
                this.eventType = newEventType;
                modified = 1;
            }
        } else if (this.eventType.equals(newEventType)) {
            modified = 0;
        } else {
            this.eventType = newEventType;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized Competition getCompetition() {
        return this.competition;
    }

    private synchronized int setCompetition(final Competition newCompetition) {
        final int modified;
        if (this.competition == null) {
            if (newCompetition == null) {
                modified = 0;
            } else {
                this.competition = newCompetition;
                modified = 1;
            }
        } else if (this.competition.equals(newCompetition)) {
            modified = 0;
        } else {
            this.competition = newCompetition;
            modified = 1;
        }
        return modified;
    }

    public synchronized Event getEventStump() {
        // even with stump, because it's used during update, I still need some initialization
        if (this.event != null) {
            if (this.timeStamp > 0L) {
                this.event.setTimeStamp(this.timeStamp);
            } else {
                this.event.timeStamp();
            }
            this.event.setMarketCountStump();
        } else { // event null, not much to be done
        }
        return this.event;
    }

    private synchronized Event getEvent() { // I will only keep stump Event in MarketCatalogue
        if (this.event != null) {
            if (this.timeStamp > 0L) {
                this.event.setTimeStamp(this.timeStamp);
            } else {
                this.event.timeStamp();
            }
            this.event.setMarketCountStump();
            this.event.initializeCollections();
        } else { // event null, not much to be done
        }
        return this.event;
    }

    private synchronized int setEvent(final Event newEvent) { // doesn't set equal Events and only does an equality check on id
        final int modified;
        if (this.event == null) {
            if (newEvent == null) {
                modified = 0;
            } else {
                this.event = newEvent;
                modified = 1;
            }
        } else if (this.event.equals(newEvent)) {
            modified = 0;
        } else {
            this.event = newEvent;
            modified = 1;
        }
        return modified;
    }

    public synchronized ParsedMarket getParsedMarket() {
        return this.parsedMarket;
    }

    public synchronized int setParsedMarket(final ParsedMarket newParsedMarket) {
        final int modified;
        if (newParsedMarket == null) {
            if (this.parsedMarket == null) {
                if (Formulas.isMarketType(this, Statics.supportedEventTypes)) { // happens often enough that it can clutter my logs, won't print
//                    Generic.alreadyPrintedMap.logOnce(Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.INFO, "trying to set null over null value for parsedMarket in MarketCatalogue: {}", this.marketId);
                } else { // normal that market is not parsed and setting null is attempted
                }
            } else {
                // won't allow null to be set
                // this.parsedMarket = parsedMarket;
                // modified = 1;
                logger.error("not allowed to set null value for parsedMarket in MarketCatalogue: {}", Generic.objectToString(this));
            }
            modified = 0;
        } else if (newParsedMarket.equals(this.parsedMarket)) {
            modified = 0;
        } else {
            this.parsedMarket = newParsedMarket;
            modified = 1;
        }

        return modified;
    }

    public synchronized long getTimeStamp() {
        return this.timeStamp;
    }

    public synchronized void setTimeStamp(final long timeStamp) {
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

    public synchronized int update(final MarketCatalogue marketCatalogue) {
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

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public synchronized int compareTo(@NotNull final MarketCatalogue o) {
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
        if (!Objects.equals(this.marketId, o.marketId)) {
            if (this.marketId == null) {
                return BEFORE;
            }
            if (o.marketId == null) {
                return AFTER;
            }
            return this.marketId.compareTo(o.marketId);
        }

        return EQUAL;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.marketId);
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
        final MarketCatalogue other = (MarketCatalogue) obj;
        return Objects.equals(this.marketId, other.marketId);
    }
}
