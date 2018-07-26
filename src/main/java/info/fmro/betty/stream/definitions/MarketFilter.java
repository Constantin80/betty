package info.fmro.betty.stream.definitions;

import info.fmro.betty.entities.TimeRange;
import info.fmro.betty.enums.MarketBettingType;
import info.fmro.betty.enums.OrderStatus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MarketFilter {

    private Set<String> marketIds; // If no marketIds passed user will be subscribed to all markets
    private Boolean bspMarket; // Restrict to bsp markets only, if True or non-bsp markets if False. If not specified then returns both BSP and non-BSP markets
    private Set<MarketBettingType> bettingTypes; // Restrict to markets that match the betting type of the market (i.e. Odds, Asian Handicap Singles, or Asian Handicap Doubles)
    private Set<String> eventTypeIds; // Restrict markets by event type associated with the market. (i.e., "1" for Football, "7" for Horse Racing, etc)
    private Set<String> eventIds; // Restrict markets by the event id associated with the market.
    private Boolean turnInPlayEnabled; // Restrict to markets that will turn in play if True or will not turn in play if false. If not specified, returns both.
    private Set<String> marketTypes; // Restrict to markets that match the type of the market (i.e., MATCH_ODDS, HALF_TIME_SCORE). You should use this instead of relying on the market name as the market type codes are the same in all locales
    private Set<String> venues; // Restrict markets by the venue associated with the market. Currently only Horse Racing markets have venues.
    private Set<String> countryCodes; // Restrict to markets that are in the specified country or countries
    private Set<String> raceTypes; // Restrict to markets that are a specific race type e.g.Harness, Flat, Hurdle, Chase, Bumper. NH Flat. NO_VALUE

    // these are not present in the stream API specification, but the specification could be incomplete, and I don't think it harms leaving them
    private String textQuery;
    private Set<String> exchangeIds;
    private Set<String> competitionIds;
    private Boolean inPlayOnly;
    private TimeRange marketStartTime;
    private Set<OrderStatus> withOrders;

    public MarketFilter() {
    }

    public synchronized String getTextQuery() {
        return textQuery;
    }

    public synchronized void setTextQuery(String textQuery) {
        this.textQuery = textQuery;
    }

    public synchronized Set<String> getRaceTypes() {
        return raceTypes == null ? null : new HashSet<>(raceTypes);
    }

    public synchronized void setRaceTypes(Set<String> raceTypes) {
        this.raceTypes = raceTypes == null ? null : new HashSet<>(raceTypes);
    }

    public synchronized Set<String> getExchangeIds() {
        return exchangeIds == null ? null : new HashSet<>(exchangeIds);
    }

    public synchronized void setExchangeIds(Set<String> exchangeIds) {
        this.exchangeIds = exchangeIds == null ? null : new HashSet<>(exchangeIds);
    }

    public synchronized Set<String> getEventTypeIds() {
        return eventTypeIds == null ? null : new HashSet<>(eventTypeIds);
    }

    public synchronized void setEventTypeIds(Set<String> eventTypeIds) {
        this.eventTypeIds = eventTypeIds == null ? null : new HashSet<>(eventTypeIds);
    }

    public synchronized void setEventTypeIds(String... eventTypeIds) {
        setEventTypeIds(new HashSet<>(Arrays.asList(eventTypeIds)));
    }

    public synchronized Set<String> getMarketIds() {
        return marketIds == null ? null : new HashSet<>(marketIds);
    }

    public synchronized void setMarketIds(Set<String> marketIds) {
        this.marketIds = marketIds == null ? null : new HashSet<>(marketIds);
    }

    public synchronized Boolean getInPlayOnly() {
        return inPlayOnly;
    }

    public synchronized void setInPlayOnly(Boolean inPlayOnly) {
        this.inPlayOnly = inPlayOnly;
    }

    public synchronized Set<String> getEventIds() {
        return eventIds == null ? null : new HashSet<>(eventIds);
    }

    public synchronized void setEventIds(Set<String> eventIds) {
        this.eventIds = eventIds == null ? null : new HashSet<>(eventIds);
    }

    public synchronized Set<String> getCompetitionIds() {
        return competitionIds == null ? null : new HashSet<>(competitionIds);
    }

    public synchronized void setCompetitionIds(Set<String> competitionIds) {
        this.competitionIds = competitionIds == null ? null : new HashSet<>(competitionIds);
    }

    public synchronized Set<String> getVenues() {
        return venues == null ? null : new HashSet<>(venues);
    }

    public synchronized void setVenues(Set<String> venues) {
        this.venues = venues == null ? null : new HashSet<>(venues);
    }

    public synchronized Boolean getBspMarket() {
        return bspMarket;
    }

    public synchronized void setBspMarket(Boolean bspMarket) {
        this.bspMarket = bspMarket;
    }

    public synchronized Boolean getTurnInPlayEnabled() {
        return turnInPlayEnabled;
    }

    public synchronized void setTurnInPlayEnabled(Boolean turnInPlayEnabled) {
        this.turnInPlayEnabled = turnInPlayEnabled;
    }

    public synchronized Set<MarketBettingType> getBettingTypes() {
        return bettingTypes == null ? null : new HashSet<>(bettingTypes);
    }

    public synchronized void setBettingTypes(Set<MarketBettingType> bettingTypes) {
        this.bettingTypes = bettingTypes == null ? null : new HashSet<>(bettingTypes);
    }

    public synchronized Set<String> getCountryCodes() {
        return countryCodes == null ? null : new HashSet<>(countryCodes);
    }

    public synchronized void setCountryCodes(Set<String> countryCodes) {
        this.countryCodes = countryCodes == null ? null : new HashSet<>(countryCodes);
    }

    public synchronized Set<String> getMarketTypes() {
        return marketTypes == null ? null : new HashSet<>(marketTypes);
    }

    public synchronized void setMarketTypes(Set<String> marketTypes) {
        this.marketTypes = marketTypes == null ? null : new HashSet<>(marketTypes);
    }

    public synchronized void setMarketTypes(String... marketTypes) {
        setMarketTypes(new HashSet<>(Arrays.asList(marketTypes)));
    }

    public synchronized TimeRange getMarketStartTime() {
        return marketStartTime;
    }

    public synchronized void setMarketStartTime(TimeRange marketStartTime) {
        this.marketStartTime = marketStartTime;
    }

    public synchronized Set<OrderStatus> getWithOrders() {
        return withOrders == null ? null : new HashSet<>(withOrders);
    }

    public synchronized void setWithOrders(Set<OrderStatus> withOrders) {
        this.withOrders = withOrders == null ? null : new HashSet<>(withOrders);
    }
}
