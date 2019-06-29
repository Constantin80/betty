package info.fmro.betty.entities;

import info.fmro.betty.enums.BetOutcome;
import info.fmro.betty.enums.OrderType;
import info.fmro.betty.enums.PersistenceType;
import info.fmro.betty.enums.Side;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class ClearedOrderSummary
        implements Serializable, Comparable<ClearedOrderSummary> {
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = -3436677384425242662L;
    private String eventTypeId; // EventTypeId alias String
    private String eventId; // EventId alias String
    private final String marketId; // MarketId alias String
    private final Long selectionId; // SelectionId alias Long
    private Double handicap; // handicap alias Double
    private final String betId; // BetId alias String
    private Date placedDate;
    private PersistenceType persistenceType;
    private OrderType orderType;
    private Side side;
    private ItemDescription itemDescription;
    private BetOutcome betOutcome;
    private Double priceRequested; // Price alias Double
    private Date settledDate;
    private Date lastMatchedDate;
    private Integer betCount;
    private Double commission; // Size alias Double
    private Double priceMatched; // Price alias Double
    private Boolean priceReduced;
    private Double sizeSettled; // Size alias Double
    private Double profit; // Size alias Double
    private Double sizeCancelled; // Size alias Double
    private String customerOrderRef; // The order reference defined by the customer for the bet order
    private String customerStrategyRef; // The strategy reference defined by the customer for the bet order

    public ClearedOrderSummary(final String betId, final String marketId, final Long selectionId) {
        this.betId = betId;
        this.marketId = marketId;
        this.selectionId = selectionId;
    }

    public synchronized String getEventTypeId() {
        return eventTypeId;
    }

    public synchronized void setEventTypeId(final String eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public synchronized String getEventId() {
        return eventId;
    }

    public synchronized void setEventId(final String eventId) {
        this.eventId = eventId;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized Long getSelectionId() {
        return selectionId;
    }

    public synchronized Double getHandicap() {
        return handicap;
    }

    public synchronized void setHandicap(final Double handicap) {
        this.handicap = handicap;
    }

    public synchronized String getBetId() {
        return betId;
    }

    public synchronized Date getPlacedDate() {
        return placedDate == null ? null : (Date) placedDate.clone();
    }

    public synchronized void setPlacedDate(final Date placedDate) {
        this.placedDate = placedDate == null ? null : (Date) placedDate.clone();
    }

    public synchronized PersistenceType getPersistenceType() {
        return persistenceType;
    }

    public synchronized void setPersistenceType(final PersistenceType persistenceType) {
        this.persistenceType = persistenceType;
    }

    public synchronized OrderType getOrderType() {
        return orderType;
    }

    public synchronized void setOrderType(final OrderType orderType) {
        this.orderType = orderType;
    }

    public synchronized Side getSide() {
        return side;
    }

    public synchronized void setSide(final Side side) {
        this.side = side;
    }

    public synchronized ItemDescription getItemDescription() {
        return itemDescription;
    }

    public synchronized void setItemDescription(final ItemDescription itemDescription) {
        this.itemDescription = itemDescription;
    }

    public synchronized BetOutcome getBetOutcome() {
        return betOutcome;
    }

    public synchronized void setBetOutcome(final BetOutcome betOutcome) {
        this.betOutcome = betOutcome;
    }

    public synchronized Double getPriceRequested() {
        return priceRequested;
    }

    public synchronized void setPriceRequested(final Double priceRequested) {
        this.priceRequested = priceRequested;
    }

    public synchronized Date getSettledDate() {
        return settledDate == null ? null : (Date) settledDate.clone();
    }

    public synchronized void setSettledDate(final Date settledDate) {
        this.settledDate = settledDate == null ? null : (Date) settledDate.clone();
    }

    public synchronized Date getLastMatchedDate() {
        return lastMatchedDate == null ? null : (Date) lastMatchedDate.clone();
    }

    public synchronized void setLastMatchedDate(final Date lastMatchedDate) {
        this.lastMatchedDate = lastMatchedDate == null ? null : (Date) lastMatchedDate.clone();
    }

    public synchronized Integer getBetCount() {
        return betCount;
    }

    public synchronized void setBetCount(final Integer betCount) {
        this.betCount = betCount;
    }

    public synchronized Double getCommission() {
        return commission;
    }

    public synchronized void setCommission(final Double commission) {
        this.commission = commission;
    }

    public synchronized Double getPriceMatched() {
        return priceMatched;
    }

    public synchronized void setPriceMatched(final Double priceMatched) {
        this.priceMatched = priceMatched;
    }

    public synchronized Boolean isPriceReduced() {
        return priceReduced;
    }

    public synchronized void setPriceReduced(final Boolean priceReduced) {
        this.priceReduced = priceReduced;
    }

    public synchronized Double getSizeSettled() {
        return sizeSettled;
    }

    public synchronized void setSizeSettled(final Double sizeSettled) {
        this.sizeSettled = sizeSettled;
    }

    public synchronized Double getProfit() {
        return profit;
    }

    public synchronized void setProfit(final Double profit) {
        this.profit = profit;
    }

    public synchronized Double getSizeCancelled() {
        return sizeCancelled;
    }

    public synchronized void setSizeCancelled(final Double sizeCancelled) {
        this.sizeCancelled = sizeCancelled;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(final ClearedOrderSummary other) {
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
        if (!Objects.equals(this.betId, other.betId)) {
            if (this.betId == null) {
                return BEFORE;
            }
            if (other.betId == null) {
                return AFTER;
            }
            return this.betId.compareTo(other.betId);
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
        if (!Objects.equals(this.selectionId, other.selectionId)) {
            if (this.selectionId == null) {
                return BEFORE;
            }
            if (other.selectionId == null) {
                return AFTER;
            }
            return this.selectionId.compareTo(other.selectionId);
        }

        return EQUAL;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.betId);
        hash = 73 * hash + Objects.hashCode(this.marketId);
        hash = 73 * hash + Objects.hashCode(this.selectionId);
        return hash;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
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
        final ClearedOrderSummary other = (ClearedOrderSummary) obj;
        if (!Objects.equals(this.betId, other.betId)) {
            return false;
        }
        if (!Objects.equals(this.marketId, other.marketId)) {
            return false;
        }
        return Objects.equals(this.selectionId, other.selectionId);
    }
}
