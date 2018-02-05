package info.fmro.betty.entities;

import info.fmro.betty.enums.MarketBettingType;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class MarketDescription
        implements Serializable {

    private static final long serialVersionUID = -2877536783333417777L;
    private Boolean persistenceEnabled;
    private Boolean bspMarket;
    private Date marketTime;
    private Date suspendTime;
    private Date settleTime;
    private MarketBettingType bettingType;
    private Boolean turnInPlayEnabled;
    private String marketType;
    private String regulator;
    private Double marketBaseRate;
    private Boolean discountAllowed;
    private String wallet;
    private String rules;
    private Boolean rulesHasDate;
    private Double eachWayDivisor; // The divisor is returned for the marketType EACH_WAY only
    private String clarifications;

    public MarketDescription() {
    }

    public MarketDescription(Boolean persistenceEnabled, Boolean bspMarket, Date marketTime, Date suspendTime, Date settleTime, MarketBettingType bettingType,
            Boolean turnInPlayEnabled, String marketType, String regulator, Double marketBaseRate, Boolean discountAllowed, String wallet, String rules, Boolean rulesHasDate,
            String clarifications) {
        this.persistenceEnabled = persistenceEnabled;
        this.bspMarket = bspMarket;
        this.marketTime = marketTime;
        this.suspendTime = suspendTime;
        this.settleTime = settleTime;
        this.bettingType = bettingType;
        this.turnInPlayEnabled = turnInPlayEnabled;
        this.marketType = marketType;
        this.regulator = regulator;
        this.marketBaseRate = marketBaseRate;
        this.discountAllowed = discountAllowed;
        this.wallet = wallet;
        this.rules = rules;
        this.rulesHasDate = rulesHasDate;
        this.clarifications = clarifications;
    }

    public synchronized Boolean getPersistenceEnabled() {
        return persistenceEnabled;
    }

//    public synchronized void setPersistenceEnabled(Boolean persistenceEnabled) {
//        this.persistenceEnabled = persistenceEnabled;
//    }
    public synchronized Boolean getBspMarket() {
        return bspMarket;
    }

//    public synchronized void setBspMarket(Boolean bspMarket) {
//        this.bspMarket = bspMarket;
//    }
    public synchronized Date getMarketTime() {
        return marketTime == null ? null : (Date) marketTime.clone();
    }

//    public synchronized void setMarketTime(Date marketTime) {
//        this.marketTime = marketTime == null ? null : (Date) marketTime.clone();
//    }
    public synchronized Date getSuspendTime() {
        return suspendTime == null ? null : (Date) suspendTime.clone();
    }

//    public synchronized void setSuspendTime(Date suspendTime) {
//        this.suspendTime = suspendTime == null ? null : (Date) suspendTime.clone();
//    }
    public synchronized Date getSettleTime() {
        return settleTime == null ? null : (Date) settleTime.clone();
    }

//    public synchronized void setSettleTime(Date settleTime) {
//        this.settleTime = settleTime == null ? null : (Date) settleTime.clone();
//    }
    public synchronized MarketBettingType getBettingType() {
        return bettingType;
    }

//    public synchronized void setBettingType(MarketBettingType bettingType) {
//        this.bettingType = bettingType;
//    }
    public synchronized Boolean getTurnInPlayEnabled() {
        return turnInPlayEnabled;
    }

//    public synchronized void setTurnInPlayEnabled(Boolean turnInPlayEnabled) {
//        this.turnInPlayEnabled = turnInPlayEnabled;
//    }
    public synchronized String getMarketType() {
        return marketType;
    }

//    public synchronized void setMarketType(String marketType) {
//        this.marketType = marketType;
//    }
    public synchronized String getRegulator() {
        return regulator;
    }

//    public synchronized void setRegulator(String regulator) {
//        this.regulator = regulator;
//    }
    public synchronized Double getMarketBaseRate() {
        return marketBaseRate;
    }

//    public synchronized void setMarketBaseRate(Double marketBaseRate) {
//        this.marketBaseRate = marketBaseRate;
//    }
    public synchronized Boolean getDiscountAllowed() {
        return discountAllowed;
    }

//    public synchronized void setDiscountAllowed(Boolean discountAllowed) {
//        this.discountAllowed = discountAllowed;
//    }
    public synchronized String getWallet() {
        return wallet;
    }

//    public synchronized void setWallet(String wallet) {
//        this.wallet = wallet;
//    }
    public synchronized String getRules() {
        return rules;
    }

//    public synchronized void setRules(String rules) {
//        this.rules = rules;
//    }
    public synchronized Boolean getRulesHasDate() {
        return rulesHasDate;
    }

//    public synchronized void setRulesHasDate(Boolean rulesHasDate) {
//        this.rulesHasDate = rulesHasDate;
//    }
    public synchronized Double getEachWayDivisor() {
        return eachWayDivisor;
    }

//    public synchronized void setEachWayDivisor(Double eachWayDivisor) {
//        this.eachWayDivisor = eachWayDivisor;
//    }
    public synchronized String getClarifications() {
        return clarifications;
    }

//    public synchronized void setClarifications(String clarifications) {
//        this.clarifications = clarifications;
//    }
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
        final MarketDescription other = (MarketDescription) obj;
        if (!Objects.equals(this.persistenceEnabled, other.persistenceEnabled)) {
            return false;
        }
        if (!Objects.equals(this.bspMarket, other.bspMarket)) {
            return false;
        }
        if (!Objects.equals(this.marketTime, other.marketTime)) {
            return false;
        }
        if (!Objects.equals(this.suspendTime, other.suspendTime)) {
            return false;
        }
        if (!Objects.equals(this.settleTime, other.settleTime)) {
            return false;
        }
        if (this.bettingType != other.bettingType) {
            return false;
        }
        if (!Objects.equals(this.turnInPlayEnabled, other.turnInPlayEnabled)) {
            return false;
        }
        if (!Objects.equals(this.marketType, other.marketType)) {
            return false;
        }
        if (!Objects.equals(this.regulator, other.regulator)) {
            return false;
        }
        if (!Objects.equals(this.marketBaseRate, other.marketBaseRate)) {
            return false;
        }
        if (!Objects.equals(this.discountAllowed, other.discountAllowed)) {
            return false;
        }
        if (!Objects.equals(this.wallet, other.wallet)) {
            return false;
        }
        if (!Objects.equals(this.rules, other.rules)) {
            return false;
        }
        if (!Objects.equals(this.rulesHasDate, other.rulesHasDate)) {
            return false;
        }
        if (!Objects.equals(this.eachWayDivisor, other.eachWayDivisor)) {
            return false;
        }
        return Objects.equals(this.clarifications, other.clarifications);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.persistenceEnabled);
        hash = 29 * hash + Objects.hashCode(this.bspMarket);
        hash = 29 * hash + Objects.hashCode(this.marketTime);
        hash = 29 * hash + Objects.hashCode(this.suspendTime);
        hash = 29 * hash + Objects.hashCode(this.settleTime);
        hash = 29 * hash + Objects.hashCode(this.bettingType);
        hash = 29 * hash + Objects.hashCode(this.turnInPlayEnabled);
        hash = 29 * hash + Objects.hashCode(this.marketType);
        hash = 29 * hash + Objects.hashCode(this.regulator);
        hash = 29 * hash + Objects.hashCode(this.marketBaseRate);
        hash = 29 * hash + Objects.hashCode(this.discountAllowed);
        hash = 29 * hash + Objects.hashCode(this.wallet);
        hash = 29 * hash + Objects.hashCode(this.rules);
        hash = 29 * hash + Objects.hashCode(this.rulesHasDate);
        hash = 29 * hash + Objects.hashCode(this.eachWayDivisor);
        hash = 29 * hash + Objects.hashCode(this.clarifications);
        return hash;
    }
}
