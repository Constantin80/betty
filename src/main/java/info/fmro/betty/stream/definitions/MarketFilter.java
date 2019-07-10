package info.fmro.betty.stream.definitions;

import info.fmro.betty.entities.TimeRange;
import info.fmro.betty.enums.MarketBettingType;
import info.fmro.betty.enums.OrderStatus;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MarketFilter
        implements Serializable {
    private static final long serialVersionUID = 6993130569488828844L;
    @Nullable
    private Set<String> marketIds; // If no marketIds passed user will be subscribed to all markets
    private Boolean bspMarket; // Restrict to bsp markets only, if True or non-bsp markets if False. If not specified then returns both BSP and non-BSP markets
    @Nullable
    private Set<MarketBettingType> bettingTypes; // Restrict to markets that match the betting type of the market (i.e. Odds, Asian Handicap Singles, or Asian Handicap Doubles)
    @Nullable
    private Set<String> eventTypeIds; // Restrict markets by event type associated with the market. (i.e., "1" for Football, "7" for Horse Racing, etc)
    @Nullable
    private Set<String> eventIds; // Restrict markets by the event id associated with the market.
    private Boolean turnInPlayEnabled; // Restrict to markets that will turn in play if True or will not turn in play if false. If not specified, returns both.
    @Nullable
    private Set<String> marketTypes; // Restrict to markets that match the type of the market (i.e., MATCH_ODDS, HALF_TIME_SCORE). You should use this instead of relying on the market name as the market type codes are the same in all locales
    @Nullable
    private Set<String> venues; // Restrict markets by the venue associated with the market. Currently only Horse Racing markets have venues.
    @Nullable
    private Set<String> countryCodes; // Restrict to markets that are in the specified country or countries
    @Nullable
    private Set<String> raceTypes; // Restrict to markets that are a specific race type e.g.Harness, Flat, Hurdle, Chase, Bumper. NH Flat. NO_VALUE

    // these are not present in the stream API specification, but the specification could be incomplete, and I don't think it harms leaving them
    private String textQuery;
    @Nullable
    private Set<String> exchangeIds;
    @Nullable
    private Set<String> competitionIds;
    private Boolean inPlayOnly;
    private TimeRange marketStartTime;
    @Nullable
    private Set<OrderStatus> withOrders;

    public synchronized String getTextQuery() {
        return this.textQuery;
    }

    public synchronized void setTextQuery(final String textQuery) {
        this.textQuery = textQuery;
    }

    @Nullable
    public synchronized Set<String> getRaceTypes() {
        return this.raceTypes == null ? null : new HashSet<>(this.raceTypes);
    }

    public synchronized void setRaceTypes(final Set<String> raceTypes) {
        this.raceTypes = raceTypes == null ? null : new HashSet<>(raceTypes);
    }

    @Nullable
    public synchronized Set<String> getExchangeIds() {
        return this.exchangeIds == null ? null : new HashSet<>(this.exchangeIds);
    }

    public synchronized void setExchangeIds(final Set<String> exchangeIds) {
        this.exchangeIds = exchangeIds == null ? null : new HashSet<>(exchangeIds);
    }

    @Nullable
    public synchronized Set<String> getEventTypeIds() {
        return this.eventTypeIds == null ? null : new HashSet<>(this.eventTypeIds);
    }

    public synchronized void setEventTypeIds(final Set<String> eventTypeIds) {
        this.eventTypeIds = eventTypeIds == null ? null : new HashSet<>(eventTypeIds);
    }

    public synchronized void setEventTypeIds(final String... eventTypeIds) {
        setEventTypeIds(new HashSet<>(Arrays.asList(eventTypeIds)));
    }

    @Nullable
    public synchronized Set<String> getMarketIds() {
        return this.marketIds == null ? null : new HashSet<>(this.marketIds);
    }

    public synchronized void setMarketIds(final Set<String> marketIds) {
        this.marketIds = marketIds == null ? null : new HashSet<>(marketIds);
    }

    public synchronized Boolean getInPlayOnly() {
        return this.inPlayOnly;
    }

    public synchronized void setInPlayOnly(final Boolean inPlayOnly) {
        this.inPlayOnly = inPlayOnly;
    }

    @Nullable
    public synchronized Set<String> getEventIds() {
        return this.eventIds == null ? null : new HashSet<>(this.eventIds);
    }

    public synchronized void setEventIds(final Set<String> eventIds) {
        this.eventIds = eventIds == null ? null : new HashSet<>(eventIds);
    }

    @Nullable
    public synchronized Set<String> getCompetitionIds() {
        return this.competitionIds == null ? null : new HashSet<>(this.competitionIds);
    }

    public synchronized void setCompetitionIds(final Set<String> competitionIds) {
        this.competitionIds = competitionIds == null ? null : new HashSet<>(competitionIds);
    }

    @Nullable
    public synchronized Set<String> getVenues() {
        return this.venues == null ? null : new HashSet<>(this.venues);
    }

    public synchronized void setVenues(final Set<String> venues) {
        this.venues = venues == null ? null : new HashSet<>(venues);
    }

    public synchronized Boolean getBspMarket() {
        return this.bspMarket;
    }

    public synchronized void setBspMarket(final Boolean bspMarket) {
        this.bspMarket = bspMarket;
    }

    public synchronized Boolean getTurnInPlayEnabled() {
        return this.turnInPlayEnabled;
    }

    public synchronized void setTurnInPlayEnabled(final Boolean turnInPlayEnabled) {
        this.turnInPlayEnabled = turnInPlayEnabled;
    }

    @Nullable
    public synchronized Set<MarketBettingType> getBettingTypes() {
        return this.bettingTypes == null ? null : new HashSet<>(this.bettingTypes);
    }

    public synchronized void setBettingTypes(final Set<MarketBettingType> bettingTypes) {
        this.bettingTypes = bettingTypes == null ? null : new HashSet<>(bettingTypes);
    }

    @Nullable
    public synchronized Set<String> getCountryCodes() {
        return this.countryCodes == null ? null : new HashSet<>(this.countryCodes);
    }

    public synchronized void setCountryCodes(final Set<String> countryCodes) {
        this.countryCodes = countryCodes == null ? null : new HashSet<>(countryCodes);
    }

    @Nullable
    public synchronized Set<String> getMarketTypes() {
        return this.marketTypes == null ? null : new HashSet<>(this.marketTypes);
    }

    public synchronized void setMarketTypes(final Set<String> marketTypes) {
        this.marketTypes = marketTypes == null ? null : new HashSet<>(marketTypes);
    }

    public synchronized void setMarketTypes(final String... marketTypes) {
        setMarketTypes(new HashSet<>(Arrays.asList(marketTypes)));
    }

    public synchronized TimeRange getMarketStartTime() {
        return this.marketStartTime;
    }

    public synchronized void setMarketStartTime(final TimeRange marketStartTime) {
        this.marketStartTime = marketStartTime;
    }

    @Nullable
    public synchronized Set<OrderStatus> getWithOrders() {
        return this.withOrders == null ? null : new HashSet<>(this.withOrders);
    }

    public synchronized void setWithOrders(final Set<OrderStatus> withOrders) {
        this.withOrders = withOrders == null ? null : new HashSet<>(withOrders);
    }
}
