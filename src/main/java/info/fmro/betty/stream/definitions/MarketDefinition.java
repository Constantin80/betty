package info.fmro.betty.stream.definitions;

import info.fmro.betty.enums.MarketBettingType;
import info.fmro.betty.enums.MarketStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketDefinition {
    private Long betDelay;
    private MarketBettingType bettingType; // betting type of the market (i.e. Odds, Asian Handicap Singles, or Asian Handicap Doubles)
    private Boolean bspMarket; // bsp market only, if True or non-bsp market if False.
    private Boolean bspReconciled;
    private Boolean complete;
    private String countryCode;
    private Boolean crossMatching;
    private Boolean discountAllowed;
    private Double eachWayDivisor; // The divisor for the marketType EACH_WAY only
    private String eventId;
    private String eventTypeId; // event type associated with the market. (i.e., "1" for Football, "7" for Horse Racing, etc)
    private Boolean inPlay;
    private KeyLineDefinition keyLineDefinition;
    private Double lineInterval; // For Handicap and Line markets, the lines available on this market will be between the range of lineMinUnit and lineMaxUnit, in increments of the lineInterval value.
    //                              e.g. If unit is runs, lineMinUnit=10, lineMaxUnit=20 and lineInterval=0.5, then valid lines include 10, 10.5, 11, 11.5 up to 20 runs.
    private Double lineMaxUnit; // For Handicap and Line markets, the maximum value for the outcome, in market units for this market (eg 100 runs).
    private Double lineMinUnit; // For Handicap and Line markets, the minimum value for the outcome, in market units for this market (eg 0 runs).
    private Double marketBaseRate;
    private Date marketTime;
    private String marketType;
    private Integer numberOfActiveRunners;
    private Integer numberOfWinners;
    private Date openDate;
    private Boolean persistenceEnabled;
    private PriceLadderDefinition priceLadderDefinition;
    private String raceType; // market that is a specific race type e.g.Harness, Flat, Hurdle, Chase, Bumper. NH Flat. NO_VALUE
    private List<String> regulators; // The market regulators.
    private List<RunnerDefinition> runners;
    private Boolean runnersVoidable;
    private Date settledTime;
    private MarketStatus status;
    private Date suspendTime;
    private String timezone;
    private Boolean turnInPlayEnabled; // market that will turn in play if True or will not turn in play if false.
    private String venue; // venue associated with the market. Currently only Horse Racing markets have venues.
    private Long version;

    public MarketDefinition() {
    }

    public synchronized Long getBetDelay() {
        return betDelay;
    }

    public synchronized void setBetDelay(Long betDelay) {
        this.betDelay = betDelay;
    }

    public synchronized MarketBettingType getBettingType() {
        return bettingType;
    }

    public synchronized void setBettingType(MarketBettingType bettingType) {
        this.bettingType = bettingType;
    }

    public synchronized Boolean getBspMarket() {
        return bspMarket;
    }

    public synchronized void setBspMarket(Boolean bspMarket) {
        this.bspMarket = bspMarket;
    }

    public synchronized Boolean getBspReconciled() {
        return bspReconciled;
    }

    public synchronized void setBspReconciled(Boolean bspReconciled) {
        this.bspReconciled = bspReconciled;
    }

    public synchronized Boolean getComplete() {
        return complete;
    }

    public synchronized void setComplete(Boolean complete) {
        this.complete = complete;
    }

    public synchronized String getCountryCode() {
        return countryCode;
    }

    public synchronized void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public synchronized Boolean getCrossMatching() {
        return crossMatching;
    }

    public synchronized void setCrossMatching(Boolean crossMatching) {
        this.crossMatching = crossMatching;
    }

    public synchronized Boolean getDiscountAllowed() {
        return discountAllowed;
    }

    public synchronized void setDiscountAllowed(Boolean discountAllowed) {
        this.discountAllowed = discountAllowed;
    }

    public synchronized Double getEachWayDivisor() {
        return eachWayDivisor;
    }

    public synchronized void setEachWayDivisor(Double eachWayDivisor) {
        this.eachWayDivisor = eachWayDivisor;
    }

    public synchronized String getEventId() {
        return eventId;
    }

    public synchronized void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public synchronized String getEventTypeId() {
        return eventTypeId;
    }

    public synchronized void setEventTypeId(String eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public synchronized Boolean getInPlay() {
        return inPlay;
    }

    public synchronized void setInPlay(Boolean inPlay) {
        this.inPlay = inPlay;
    }

    public synchronized KeyLineDefinition getKeyLineDefinition() {
        return keyLineDefinition;
    }

    public synchronized void setKeyLineDefinition(KeyLineDefinition keyLineDefinition) {
        this.keyLineDefinition = keyLineDefinition;
    }

    public synchronized Double getLineInterval() {
        return lineInterval;
    }

    public synchronized void setLineInterval(Double lineInterval) {
        this.lineInterval = lineInterval;
    }

    public synchronized Double getLineMaxUnit() {
        return lineMaxUnit;
    }

    public synchronized void setLineMaxUnit(Double lineMaxUnit) {
        this.lineMaxUnit = lineMaxUnit;
    }

    public synchronized Double getLineMinUnit() {
        return lineMinUnit;
    }

    public synchronized void setLineMinUnit(Double lineMinUnit) {
        this.lineMinUnit = lineMinUnit;
    }

    public synchronized Double getMarketBaseRate() {
        return marketBaseRate;
    }

    public synchronized void setMarketBaseRate(Double marketBaseRate) {
        this.marketBaseRate = marketBaseRate;
    }

    public synchronized Date getMarketTime() {
        return marketTime == null ? null : (Date) marketTime.clone();
    }

    public synchronized void setMarketTime(Date marketTime) {
        this.marketTime = marketTime == null ? null : (Date) marketTime.clone();
    }

    public synchronized String getMarketType() {
        return marketType;
    }

    public synchronized void setMarketType(String marketType) {
        this.marketType = marketType;
    }

    public synchronized Integer getNumberOfActiveRunners() {
        return numberOfActiveRunners;
    }

    public synchronized void setNumberOfActiveRunners(Integer numberOfActiveRunners) {
        this.numberOfActiveRunners = numberOfActiveRunners;
    }

    public synchronized Integer getNumberOfWinners() {
        return numberOfWinners;
    }

    public synchronized void setNumberOfWinners(Integer numberOfWinners) {
        this.numberOfWinners = numberOfWinners;
    }

    public synchronized Date getOpenDate() {
        return openDate == null ? null : (Date) openDate.clone();
    }

    public synchronized void setOpenDate(Date openDate) {
        this.openDate = openDate == null ? null : (Date) openDate.clone();
    }

    public synchronized Boolean getPersistenceEnabled() {
        return persistenceEnabled;
    }

    public synchronized void setPersistenceEnabled(Boolean persistenceEnabled) {
        this.persistenceEnabled = persistenceEnabled;
    }

    public synchronized PriceLadderDefinition getPriceLadderDefinition() {
        return priceLadderDefinition;
    }

    public synchronized void setPriceLadderDefinition(PriceLadderDefinition priceLadderDefinition) {
        this.priceLadderDefinition = priceLadderDefinition;
    }

    public synchronized String getRaceType() {
        return raceType;
    }

    public synchronized void setRaceType(String raceType) {
        this.raceType = raceType;
    }

    public synchronized List<String> getRegulators() {
        return regulators == null ? null : new ArrayList<>(regulators);
    }

    public synchronized void setRegulators(List<String> regulators) {
        this.regulators = regulators == null ? null : new ArrayList<>(regulators);
    }

    public synchronized List<RunnerDefinition> getRunners() {
        return runners == null ? null : new ArrayList<>(runners);
    }

    public synchronized void setRunners(List<RunnerDefinition> runners) {
        this.runners = runners == null ? null : new ArrayList<>(runners);
    }

    public synchronized Boolean getRunnersVoidable() {
        return runnersVoidable;
    }

    public synchronized void setRunnersVoidable(Boolean runnersVoidable) {
        this.runnersVoidable = runnersVoidable;
    }

    public synchronized Date getSettledTime() {
        return settledTime == null ? null : (Date) settledTime.clone();
    }

    public synchronized void setSettledTime(Date settledTime) {
        this.settledTime = settledTime == null ? null : (Date) settledTime.clone();
    }

    public synchronized MarketStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(MarketStatus status) {
        this.status = status;
    }

    public synchronized Date getSuspendTime() {
        return suspendTime == null ? null : (Date) suspendTime.clone();
    }

    public synchronized void setSuspendTime(Date suspendTime) {
        this.suspendTime = suspendTime == null ? null : (Date) suspendTime.clone();
    }

    public synchronized String getTimezone() {
        return timezone;
    }

    public synchronized void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public synchronized Boolean getTurnInPlayEnabled() {
        return turnInPlayEnabled;
    }

    public synchronized void setTurnInPlayEnabled(Boolean turnInPlayEnabled) {
        this.turnInPlayEnabled = turnInPlayEnabled;
    }

    public synchronized String getVenue() {
        return venue;
    }

    public synchronized void setVenue(String venue) {
        this.venue = venue;
    }

    public synchronized Long getVersion() {
        return version;
    }

    public synchronized void setVersion(Long version) {
        this.version = version;
    }
}
