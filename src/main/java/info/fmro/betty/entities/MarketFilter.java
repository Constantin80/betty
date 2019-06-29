package info.fmro.betty.entities;

import info.fmro.betty.enums.MarketBettingType;
import info.fmro.betty.enums.OrderStatus;

import java.util.HashSet;
import java.util.Set;

public class MarketFilter {

    private String textQuery;
    private Set<String> exchangeIds;
    private Set<String> eventTypeIds;
    private Set<String> eventIds;
    private Set<String> competitionIds;
    private Set<String> marketIds;
    private Set<String> venues;
    private Boolean bspOnly;
    private Boolean turnInPlayEnabled;
    private Boolean inPlayOnly;
    private Set<MarketBettingType> marketBettingTypes;
    private Set<String> marketCountries;
    private Set<String> marketTypeCodes;
    private TimeRange marketStartTime;
    private Set<OrderStatus> withOrders;
    private Set<String> raceTypes;

    public MarketFilter() {
    }

    public synchronized String getTextQuery() {
        return textQuery;
    }

    public synchronized void setTextQuery(final String textQuery) {
        this.textQuery = textQuery;
    }

    public synchronized Set<String> getExchangeIds() {
        return exchangeIds == null ? null : new HashSet<>(exchangeIds);
    }

    public synchronized void setExchangeIds(final Set<String> exchangeIds) {
        this.exchangeIds = exchangeIds == null ? null : new HashSet<>(exchangeIds);
    }

    public synchronized Set<String> getEventTypeIds() {
        return eventTypeIds == null ? null : new HashSet<>(eventTypeIds);
    }

    public synchronized void setEventTypeIds(final Set<String> eventTypeIds) {
        this.eventTypeIds = eventTypeIds == null ? null : new HashSet<>(eventTypeIds);
    }

    public synchronized Set<String> getMarketIds() {
        return marketIds == null ? null : new HashSet<>(marketIds);
    }

    public synchronized void setMarketIds(final Set<String> marketIds) {
        this.marketIds = marketIds == null ? null : new HashSet<>(marketIds);
    }

    public synchronized Boolean getInPlayOnly() {
        return inPlayOnly;
    }

    public synchronized void setInPlayOnly(final Boolean inPlayOnly) {
        this.inPlayOnly = inPlayOnly;
    }

    public synchronized Set<String> getEventIds() {
        return eventIds == null ? null : new HashSet<>(eventIds);
    }

    public synchronized void setEventIds(final Set<String> eventIds) {
        this.eventIds = eventIds == null ? null : new HashSet<>(eventIds);
    }

    public synchronized Set<String> getCompetitionIds() {
        return competitionIds == null ? null : new HashSet<>(competitionIds);
    }

    public synchronized void setCompetitionIds(final Set<String> competitionIds) {
        this.competitionIds = competitionIds == null ? null : new HashSet<>(competitionIds);
    }

    public synchronized Set<String> getVenues() {
        return venues == null ? null : new HashSet<>(venues);
    }

    public synchronized void setVenues(final Set<String> venues) {
        this.venues = venues == null ? null : new HashSet<>(venues);
    }

    public synchronized Boolean getBspOnly() {
        return bspOnly;
    }

    public synchronized void setBspOnly(final Boolean bspOnly) {
        this.bspOnly = bspOnly;
    }

    public synchronized Boolean getTurnInPlayEnabled() {
        return turnInPlayEnabled;
    }

    public synchronized void setTurnInPlayEnabled(final Boolean turnInPlayEnabled) {
        this.turnInPlayEnabled = turnInPlayEnabled;
    }

    public synchronized Set<MarketBettingType> getMarketBettingTypes() {
        return marketBettingTypes == null ? null : new HashSet<>(marketBettingTypes);
    }

    public synchronized void setMarketBettingTypes(final Set<MarketBettingType> marketBettingTypes) {
        this.marketBettingTypes = marketBettingTypes == null ? null : new HashSet<>(marketBettingTypes);
    }

    public synchronized Set<String> getMarketCountries() {
        return marketCountries == null ? null : new HashSet<>(marketCountries);
    }

    public synchronized void setMarketCountries(final Set<String> marketCountries) {
        this.marketCountries = marketCountries == null ? null : new HashSet<>(marketCountries);
    }

    public synchronized Set<String> getMarketTypeCodes() {
        return marketTypeCodes == null ? null : new HashSet<>(marketTypeCodes);
    }

    public synchronized void setMarketTypeCodes(final Set<String> marketTypeCodes) {
        this.marketTypeCodes = marketTypeCodes == null ? null : new HashSet<>(marketTypeCodes);
    }

    public synchronized TimeRange getMarketStartTime() {
        return marketStartTime;
    }

    public synchronized void setMarketStartTime(final TimeRange marketStartTime) {
        this.marketStartTime = marketStartTime;
    }

    public synchronized Set<OrderStatus> getWithOrders() {
        return withOrders == null ? null : new HashSet<>(withOrders);
    }

    public synchronized void setWithOrders(final Set<OrderStatus> withOrders) {
        this.withOrders = withOrders == null ? null : new HashSet<>(withOrders);
    }
}
