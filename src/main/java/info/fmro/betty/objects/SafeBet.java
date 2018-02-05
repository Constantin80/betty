package info.fmro.betty.objects;

import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.enums.MarketStatus;
import info.fmro.betty.enums.ParsedMarketType;
import info.fmro.betty.enums.ParsedRunnerType;
import info.fmro.betty.enums.RunnerStatus;
import info.fmro.betty.enums.Side;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.SynchronizedMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SafeBet
        implements Comparable<SafeBet> {

    private static final Logger logger = LoggerFactory.getLogger(SafeBet.class);
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private final String marketId;
    private final MarketStatus marketStatus;
    private final boolean inplay;
    private final int betDelay;
    private final long runnerId; // selectionId from SafeRunner
    private final RunnerStatus runnerStatus;
    private final double price;
    private final double size;
    private final Side side;

    public SafeBet(String marketId, MarketStatus marketStatus, boolean inplay, int betDelay, long runnerId, RunnerStatus runnerStatus, double price, double size, Side side) {
        this.marketId = marketId;
        this.marketStatus = marketStatus;
        this.inplay = inplay;
        this.betDelay = betDelay;
        this.runnerId = runnerId;
        this.runnerStatus = runnerStatus;
        this.price = price;
        this.size = size;
        this.side = side;
    }

    public synchronized String printStats() {
        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.get(marketId);
        final ParsedMarket parsedMarket;
        if (marketCatalogue != null) {
            parsedMarket = marketCatalogue.getParsedMarket();
//            event = marketCatalogue.getEventStump();
        } else {
            parsedMarket = null;
//            event = null;
        }
        final Event event = Formulas.getStoredEventOfMarketCatalogue(marketCatalogue);

        final ParsedMarketType parsedMarketType;
        ParsedRunnerType parsedRunnerType = null;
        if (parsedMarket != null) {
            parsedMarketType = parsedMarket.getParsedMarketType();
            HashSet<ParsedRunner> parsedRunnersSet = parsedMarket.getParsedRunnersSet();
            if (parsedRunnersSet != null && !parsedRunnersSet.isEmpty()) {
                for (ParsedRunner parsedRunner : parsedRunnersSet) {
                    if (parsedRunner != null && parsedRunner.getSelectionId() == this.runnerId) {
                        parsedRunnerType = parsedRunner.getParsedRunnerType();
                        break;
                    }
                }
            }
        } else {
            parsedMarketType = null;
        }

        StringBuilder stringBuilder = new StringBuilder(32);
        if (parsedMarketType != null) {
            stringBuilder.append(parsedMarketType.name()).append(" ");
        }
        if (parsedRunnerType != null) {
            stringBuilder.append(parsedRunnerType.name()).append(" ");
        }

        final HashMap<Class<? extends ScraperEvent>, Long> scraperEventsMap;
        if (event != null) {
            scraperEventsMap = event.getScraperEventIds();
//            if (scraperEventsMap.isEmpty()) {
//                String eventId = event.getId();
//                if (eventId != null) {
//                    event = Statics.eventsMap.get(eventId);
//                    if (event != null) {
//                        scraperEventsMap = event.getScraperEventIds();
//                    } else { // keep existing empty map, nothing to be done
//                    }
//                } else { // keep existing empty map, nothing to be done
//                }
//            }
        } else {
            scraperEventsMap = null;
        }
        if (scraperEventsMap != null && !scraperEventsMap.isEmpty()) {
            for (Entry<Class<? extends ScraperEvent>, Long> entry : scraperEventsMap.entrySet()) {
                final Class<? extends ScraperEvent> clazz = entry.getKey();
                final long scraperEventId = entry.getValue();
                final SynchronizedMap<Long, ? extends ScraperEvent> map = Formulas.getScraperEventsMap(clazz);
                ScraperEvent scraperEvent;
                if (scraperEventId >= 0) {
                    scraperEvent = map.get(scraperEventId);
                } else {
                    scraperEvent = null;
                }
                if (scraperEvent != null) {
                    stringBuilder.append(scraperEvent.getHomeTeam()).append("/").append(scraperEvent.getAwayTeam()).append(" ").append(scraperEvent.getMatchStatus()).append(" ");
                    int homeScore = scraperEvent.getHomeScore();
                    int awayScore = scraperEvent.getAwayScore();
                    if (homeScore >= 0 || awayScore >= 0) {
                        stringBuilder.append(homeScore).append("-").append(awayScore).append(" ");
                    }
                    int homeHtScore = scraperEvent.getHomeHtScore();
                    int awayHtScore = scraperEvent.getAwayHtScore();
                    if (homeHtScore >= 0 || awayHtScore >= 0) {
                        stringBuilder.append("halfTime:").append(homeHtScore).append("-").append(awayHtScore).append(" ");
                    }
                    int minutesPlayed = scraperEvent.getMinutesPlayed();
                    if (minutesPlayed >= 0) {
                        stringBuilder.append(minutesPlayed);
                        int stoppage = scraperEvent.getStoppageTime();
                        if (stoppage >= 0) {
                            stringBuilder.append("+").append(stoppage);
                        }
                        stringBuilder.append("' ");
                    }
                    int homeRedCards = scraperEvent.getHomeRedCards();
                    if (homeRedCards > 0) {
                        stringBuilder.append("homeRed:").append(homeRedCards).append(" ");
                    }
                    int awayRedCards = scraperEvent.getAwayRedCards();
                    if (awayRedCards > 0) {
                        stringBuilder.append("awayRed:").append(awayRedCards).append(" ");
                    }
                } // end if scraperEvent != null
            } // end for
        } // end if scraperEventsMap

        return stringBuilder.toString().trim();
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized MarketStatus getMarketStatus() {
        return marketStatus;
    }

    public synchronized boolean isInplay() {
        return inplay;
    }

    public synchronized int getBetDelay() {
        return betDelay;
    }

    public synchronized long getRunnerId() {
        return runnerId;
    }

    public synchronized RunnerStatus getRunnerStatus() {
        return runnerStatus;
    }

    public synchronized double getPrice() {
        return price;
    }

    public synchronized double getSize() {
        return size;
    }

    public synchronized Side getSide() {
        return side;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(SafeBet other) {
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
        if (!Objects.equals(this.marketStatus, other.marketStatus)) {
            if (this.marketStatus == null) {
                return BEFORE;
            }
            if (other.marketStatus == null) {
                return AFTER;
            }
            return this.marketStatus.compareTo(other.marketStatus);
        }
        if (this.inplay != other.inplay) {
            if (!this.inplay && other.inplay) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (this.betDelay != other.betDelay) {
            if (this.betDelay < other.betDelay) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (this.runnerId != other.runnerId) {
            if (this.runnerId < other.runnerId) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (!Objects.equals(this.runnerStatus, other.runnerStatus)) {
            if (this.runnerStatus == null) {
                return BEFORE;
            }
            if (other.runnerStatus == null) {
                return AFTER;
            }
            return this.runnerStatus.compareTo(other.runnerStatus);
        }
        if (Double.doubleToLongBits(this.price) != Double.doubleToLongBits(other.price)) {
            if (Double.doubleToLongBits(this.price) < Double.doubleToLongBits(other.price)) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (!Objects.equals(this.side, other.side)) {
            if (this.side == null) {
                return BEFORE;
            }
            if (other.side == null) {
                return AFTER;
            }
            return this.side.compareTo(other.side);
        }
        if (Math.round(this.size) != Math.round(other.size)) {
            if (this.size < other.size) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }

        return EQUAL;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.marketId);
        hash = 71 * hash + Objects.hashCode(this.marketStatus);
        hash = 71 * hash + (this.inplay ? 1 : 0);
        hash = 71 * hash + this.betDelay;
        hash = 71 * hash + (int) (this.runnerId ^ (this.runnerId >>> 32));
        hash = 71 * hash + Objects.hashCode(this.runnerStatus);
        hash = 71 * hash + (int) (Double.doubleToLongBits(this.price) ^ (Double.doubleToLongBits(this.price) >>> 32));
        // hash = 71 * hash + (int) (Double.doubleToLongBits(this.size) ^ (Double.doubleToLongBits(this.size) >>> 32));
        hash = 71 * hash + (int) Math.round(this.size); // this doesn't protect from cases where the value is around .5
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
        final SafeBet other = (SafeBet) obj;
        if (!Objects.equals(this.marketId, other.marketId)) {
            return false;
        }
        if (this.marketStatus != other.marketStatus) {
            return false;
        }
        if (this.inplay != other.inplay) {
            return false;
        }
        if (this.betDelay != other.betDelay) {
            return false;
        }
        if (this.runnerId != other.runnerId) {
            return false;
        }
        if (this.runnerStatus != other.runnerStatus) {
            return false;
        }
        if (Double.doubleToLongBits(this.price) != Double.doubleToLongBits(other.price)) {
            return false;
        }
        if (!Objects.equals(this.side, other.side)) {
            return false;
        }
        return Math.round(this.size) == Math.round(other.size);
    }
}
