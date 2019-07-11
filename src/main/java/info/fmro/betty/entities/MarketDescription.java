package info.fmro.betty.entities;

import info.fmro.betty.enums.MarketBettingType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @SuppressWarnings("unused")
    private Double eachWayDivisor; // The divisor is returned for the marketType EACH_WAY only
    private String clarifications;
    private MarketLineRangeInfo lineRangeInfo; // Line range info for line markets
    @SuppressWarnings("unused")
    private String raceType; // An external identifier of a race type
    @SuppressWarnings("unused")
    private PriceLadderDescription priceLadderDescription; // Details about the price ladder in use for this market.

    @Contract(pure = true)
    public MarketDescription() {
    }

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Contract(pure = true)
    public MarketDescription(final Boolean persistenceEnabled, final Boolean bspMarket, @NotNull final Date marketTime, @NotNull final Date suspendTime, @NotNull final Date settleTime, final MarketBettingType bettingType, final Boolean turnInPlayEnabled,
                             final String marketType, final String regulator, final Double marketBaseRate, final Boolean discountAllowed, final String wallet, final String rules, final Boolean rulesHasDate, final String clarifications,
                             final MarketLineRangeInfo lineRangeInfo) {
        this.persistenceEnabled = persistenceEnabled;
        this.bspMarket = bspMarket;
        this.marketTime = (Date) marketTime.clone();
        this.suspendTime = (Date) suspendTime.clone();
        this.settleTime = (Date) settleTime.clone();
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
        this.lineRangeInfo = lineRangeInfo;
    }

    public synchronized Boolean getPersistenceEnabled() {
        return this.persistenceEnabled;
    }

    public synchronized Boolean getBspMarket() {
        return this.bspMarket;
    }

    @Nullable
    public synchronized Date getMarketTime() {
        return this.marketTime == null ? null : (Date) this.marketTime.clone();
    }

    @Nullable
    public synchronized Date getSuspendTime() {
        return this.suspendTime == null ? null : (Date) this.suspendTime.clone();
    }

    @Nullable
    public synchronized Date getSettleTime() {
        return this.settleTime == null ? null : (Date) this.settleTime.clone();
    }

    public synchronized MarketBettingType getBettingType() {
        return this.bettingType;
    }

    public synchronized Boolean getTurnInPlayEnabled() {
        return this.turnInPlayEnabled;
    }

    public synchronized String getMarketType() {
        return this.marketType;
    }

    public synchronized String getRegulator() {
        return this.regulator;
    }

    public synchronized Double getMarketBaseRate() {
        return this.marketBaseRate;
    }

    public synchronized Boolean getDiscountAllowed() {
        return this.discountAllowed;
    }

    public synchronized String getWallet() {
        return this.wallet;
    }

    public synchronized String getRules() {
        return this.rules;
    }

    public synchronized Boolean getRulesHasDate() {
        return this.rulesHasDate;
    }

    public synchronized Double getEachWayDivisor() {
        return this.eachWayDivisor;
    }

    public synchronized String getClarifications() {
        return this.clarifications;
    }

    public synchronized MarketLineRangeInfo getLineRangeInfo() {
        return this.lineRangeInfo;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
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
        if (!Objects.equals(this.clarifications, other.clarifications)) {
            return false;
        }
        return Objects.equals(this.lineRangeInfo, other.lineRangeInfo);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
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
        hash = 29 * hash + Objects.hashCode(this.lineRangeInfo);
        return hash;
    }
}
