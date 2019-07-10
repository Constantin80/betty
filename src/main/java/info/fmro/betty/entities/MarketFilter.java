package info.fmro.betty.entities;

import info.fmro.betty.enums.MarketBettingType;
import info.fmro.betty.enums.OrderStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("ClassWithTooManyFields")
public class MarketFilter {
    private String textQuery;
    @Nullable
    private Set<String> exchangeIds;
    @Nullable
    private Set<String> eventTypeIds;
    @Nullable
    private Set<String> eventIds;
    @Nullable
    private Set<String> competitionIds;
    @Nullable
    private Set<String> marketIds;
    @Nullable
    private Set<String> venues;
    private Boolean bspOnly;
    private Boolean turnInPlayEnabled;
    private Boolean inPlayOnly;
    @Nullable
    private Set<MarketBettingType> marketBettingTypes;
    @Nullable
    private Set<String> marketCountries;
    @Nullable
    private Set<String> marketTypeCodes;
    private TimeRange marketStartTime;
    @Nullable
    private Set<OrderStatus> withOrders;
    @SuppressWarnings("unused")
    private Set<String> raceTypes;

    public synchronized String getTextQuery() {
        return this.textQuery;
    }

    public synchronized void setTextQuery(final String textQuery) {
        this.textQuery = textQuery;
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

    public synchronized Boolean getBspOnly() {
        return this.bspOnly;
    }

    public synchronized void setBspOnly(final Boolean bspOnly) {
        this.bspOnly = bspOnly;
    }

    public synchronized Boolean getTurnInPlayEnabled() {
        return this.turnInPlayEnabled;
    }

    public synchronized void setTurnInPlayEnabled(final Boolean turnInPlayEnabled) {
        this.turnInPlayEnabled = turnInPlayEnabled;
    }

    @Nullable
    public synchronized Set<MarketBettingType> getMarketBettingTypes() {
        return this.marketBettingTypes == null ? null : EnumSet.copyOf(this.marketBettingTypes); // immutable
    }

    public synchronized void setMarketBettingTypes(final Collection<MarketBettingType> marketBettingTypes) {
        this.marketBettingTypes = marketBettingTypes == null ? null : EnumSet.copyOf(marketBettingTypes); // immutable
    }

    @Nullable
    public synchronized Set<String> getMarketCountries() {
        return this.marketCountries == null ? null : new HashSet<>(this.marketCountries);
    }

    public synchronized void setMarketCountries(final Set<String> marketCountries) {
        this.marketCountries = marketCountries == null ? null : new HashSet<>(marketCountries);
    }

    @Nullable
    public synchronized Set<String> getMarketTypeCodes() {
        return this.marketTypeCodes == null ? null : new HashSet<>(this.marketTypeCodes);
    }

    public synchronized void setMarketTypeCodes(final Set<String> marketTypeCodes) {
        this.marketTypeCodes = marketTypeCodes == null ? null : new HashSet<>(marketTypeCodes);
    }

    public synchronized TimeRange getMarketStartTime() {
        return this.marketStartTime;
    }

    public synchronized void setMarketStartTime(final TimeRange marketStartTime) {
        this.marketStartTime = marketStartTime;
    }

    @Nullable
    public synchronized Set<OrderStatus> getWithOrders() {
        return this.withOrders == null ? null : EnumSet.copyOf(this.withOrders); // immutable
    }

    public synchronized void setWithOrders(final Collection<OrderStatus> withOrders) {
        this.withOrders = withOrders == null ? null : EnumSet.copyOf(withOrders); // immutable
    }
}
