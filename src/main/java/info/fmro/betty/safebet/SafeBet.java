package info.fmro.betty.safebet;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.entities.Event;
import info.fmro.shared.entities.MarketCatalogue;
import info.fmro.shared.enums.MarketStatus;
import info.fmro.shared.enums.ParsedMarketType;
import info.fmro.shared.enums.ParsedRunnerType;
import info.fmro.shared.enums.RunnerStatus;
import info.fmro.shared.enums.Side;
import info.fmro.shared.objects.ParsedMarket;
import info.fmro.shared.objects.ParsedRunner;
import info.fmro.shared.stream.objects.ScraperEventInterface;
import info.fmro.shared.utility.SynchronizedMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Objects;

public class SafeBet
        implements Comparable<SafeBet> {
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private final String marketId;
    private final MarketStatus marketStatus;
    private final boolean inPlay;
    private final int betDelay;
    private final long runnerId; // selectionId from SafeRunner
    private final RunnerStatus runnerStatus;
    private final double price;
    private final double size;
    private final Side side;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Contract(pure = true)
    SafeBet(final String marketId, final MarketStatus marketStatus, final boolean inPlay, final int betDelay, final long runnerId, final RunnerStatus runnerStatus, final double price, final double size, final Side side) {
        this.marketId = marketId;
        this.marketStatus = marketStatus;
        this.inPlay = inPlay;
        this.betDelay = betDelay;
        this.runnerId = runnerId;
        this.runnerStatus = runnerStatus;
        this.price = price;
        this.size = size;
        this.side = side;
    }

    public synchronized String printStats() {
        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.get(this.marketId);
        @Nullable final ParsedMarket parsedMarket = marketCatalogue != null ? marketCatalogue.getParsedMarket() : null;
        final Event event = Formulas.getStoredEventOfMarketCatalogue(marketCatalogue);

        @Nullable final ParsedMarketType parsedMarketType;
        ParsedRunnerType parsedRunnerType = null;
        if (parsedMarket != null) {
            parsedMarketType = parsedMarket.getParsedMarketType();
            final HashSet<ParsedRunner> parsedRunnersSet = parsedMarket.getParsedRunnersSet();
            if (parsedRunnersSet != null && !parsedRunnersSet.isEmpty()) {
                for (final ParsedRunner parsedRunner : parsedRunnersSet) {
                    if (parsedRunner != null && parsedRunner.getSelectionId() == this.runnerId) {
                        parsedRunnerType = parsedRunner.getParsedRunnerType();
                        break;
                    }
                }
            }
        } else {
            parsedMarketType = null;
        }

        final StringBuilder stringBuilder = new StringBuilder(32);
        if (parsedMarketType != null) {
            stringBuilder.append(parsedMarketType.name()).append(" ");
        }
        if (parsedRunnerType != null) {
            stringBuilder.append(parsedRunnerType.name()).append(" ");
        }

        @Nullable final HashMap<Class<? extends ScraperEventInterface>, Long> scraperEventsMap = event != null ? event.getScraperEventIds() : null;
        if (scraperEventsMap != null && !scraperEventsMap.isEmpty()) {
            for (final Entry<Class<? extends ScraperEventInterface>, Long> entry : scraperEventsMap.entrySet()) {
                final Class<? extends ScraperEventInterface> clazz = entry.getKey();
                final long scraperEventId = entry.getValue();
                final SynchronizedMap<Long, ? extends ScraperEventInterface> map = Formulas.getScraperEventsMap(clazz);
                @Nullable final ScraperEventInterface scraperEvent = scraperEventId >= 0 ? map.get(scraperEventId) : null;
                if (scraperEvent != null) {
                    stringBuilder.append(scraperEvent.getHomeTeam()).append("/").append(scraperEvent.getAwayTeam()).append(" ").append(scraperEvent.getMatchStatus()).append(" ");
                    final int homeScore = scraperEvent.getHomeScore();
                    final int awayScore = scraperEvent.getAwayScore();
                    if (homeScore >= 0 || awayScore >= 0) {
                        stringBuilder.append(homeScore).append("-").append(awayScore).append(" ");
                    }
                    final int homeHtScore = scraperEvent.getHomeHtScore();
                    final int awayHtScore = scraperEvent.getAwayHtScore();
                    if (homeHtScore >= 0 || awayHtScore >= 0) {
                        stringBuilder.append("halfTime:").append(homeHtScore).append("-").append(awayHtScore).append(" ");
                    }
                    final int minutesPlayed = scraperEvent.getMinutesPlayed();
                    if (minutesPlayed >= 0) {
                        stringBuilder.append(minutesPlayed);
                        final int stoppage = scraperEvent.getStoppageTime();
                        if (stoppage >= 0) {
                            stringBuilder.append("+").append(stoppage);
                        }
                        stringBuilder.append("' ");
                    }
                    final int homeRedCards = scraperEvent.getHomeRedCards();
                    if (homeRedCards > 0) {
                        stringBuilder.append("homeRed:").append(homeRedCards).append(" ");
                    }
                    final int awayRedCards = scraperEvent.getAwayRedCards();
                    if (awayRedCards > 0) {
                        stringBuilder.append("awayRed:").append(awayRedCards).append(" ");
                    }
                } // end if scraperEvent != null
            } // end for
        } // end if scraperEventsMap

        return stringBuilder.toString().trim();
    }

    public String getMarketId() {
        return this.marketId;
    }

    public MarketStatus getMarketStatus() {
        return this.marketStatus;
    }

    public boolean isInPlay() {
        return this.inPlay;
    }

    public int getBetDelay() {
        return this.betDelay;
    }

    public long getRunnerId() {
        return this.runnerId;
    }

    public RunnerStatus getRunnerStatus() {
        return this.runnerStatus;
    }

    public double getPrice() {
        return this.price;
    }

    public double getSize() {
        return this.size;
    }

    public Side getSide() {
        return this.side;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public int compareTo(@NotNull final SafeBet o) {
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
        if (this.marketStatus != o.marketStatus) {
            if (this.marketStatus == null) {
                return BEFORE;
            }
            if (o.marketStatus == null) {
                return AFTER;
            }
            return this.marketStatus.compareTo(o.marketStatus);
        }
        if (this.inPlay != o.inPlay) {
            //noinspection ConstantConditions
            return !this.inPlay && o.inPlay ? BEFORE : AFTER;
        }
        if (this.betDelay != o.betDelay) {
            return this.betDelay < o.betDelay ? BEFORE : AFTER;
        }
        if (this.runnerId != o.runnerId) {
            return this.runnerId < o.runnerId ? BEFORE : AFTER;
        }
        if (this.runnerStatus != o.runnerStatus) {
            if (this.runnerStatus == null) {
                return BEFORE;
            }
            if (o.runnerStatus == null) {
                return AFTER;
            }
            return this.runnerStatus.compareTo(o.runnerStatus);
        }
        if (Double.doubleToLongBits(this.price) != Double.doubleToLongBits(o.price)) {
            return Double.doubleToLongBits(this.price) < Double.doubleToLongBits(o.price) ? BEFORE : AFTER;
        }
        if (this.side != o.side) {
            if (this.side == null) {
                return BEFORE;
            }
            if (o.side == null) {
                return AFTER;
            }
            return this.side.compareTo(o.side);
        }
        if (Math.round(this.size) != Math.round(o.size)) {
            return this.size < o.size ? BEFORE : AFTER;
        }

        return EQUAL;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final SafeBet safeBet = (SafeBet) obj;
        return this.inPlay == safeBet.inPlay &&
               this.betDelay == safeBet.betDelay &&
               this.runnerId == safeBet.runnerId &&
               Double.compare(safeBet.price, this.price) == 0 &&
               Double.compare(safeBet.size, this.size) == 0 &&
               Objects.equals(this.marketId, safeBet.marketId) &&
               this.marketStatus == safeBet.marketStatus &&
               this.runnerStatus == safeBet.runnerStatus &&
               this.side == safeBet.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.marketId, this.marketStatus, this.inPlay, this.betDelay, this.runnerId, this.runnerStatus, this.price, this.size, this.side);
    }
}
