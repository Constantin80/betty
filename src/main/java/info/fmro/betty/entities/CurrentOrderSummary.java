package info.fmro.betty.entities;

import info.fmro.betty.enums.OrderStatus;
import info.fmro.betty.enums.OrderType;
import info.fmro.betty.enums.PersistenceType;
import info.fmro.betty.enums.Side;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrentOrderSummary
        implements Serializable, Comparable<CurrentOrderSummary> {

    private static final Logger logger = LoggerFactory.getLogger(CurrentOrderSummary.class);
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = -2498492858943069726L;
    private final String betId;
    private final String marketId;
    private final Long selectionId;
    private Double handicap;
    private PriceSize priceSize;
    private Double bspLiability;
    private Side side;
    private OrderStatus status;
    private PersistenceType persistenceType;
    private OrderType orderType;
    private Date placedDate;
    private Date matchedDate;
    private Double averagePriceMatched;
    private Double sizeMatched;
    private Double sizeRemaining;
    private Double sizeLapsed;
    private Double sizeCancelled;
    private Double sizeVoided;
    private String regulatorAuthCode;
    private String regulatorCode;
    private String eventId; // created using the marketId

    public CurrentOrderSummary(String betId, String marketId, Long selectionId) {
        this.betId = betId;
        this.marketId = marketId;
        this.selectionId = selectionId;
    }

    public synchronized double getPlacedAmount() { // returns total amount placed on this order, unmatched included
        final double amount;

        if (this.priceSize == null) {
            amount = 0d;
        } else {
            final Double sizeObject = this.priceSize.getSize();
            final double size = sizeObject == null ? 0d : sizeObject;
            final Double priceObject = this.priceSize.getPrice();
            final double price = priceObject == null ? 0d : priceObject;

            if (this.side == null) {
                logger.error("null side in CurrentOrderSummary placedAmount: {}", Generic.objectToString(this));
                amount = Math.max(size, size * (price - 1d)); // assume the worst
            } else {
                switch (this.side) {
                    case BACK:
                        amount = size;
                        break;
                    case LAY:
                        amount = size * (price - 1d);
                        break;
                    default:
                        logger.error("unknown side {} in CurrentOrderSummary placedAmount: {}", this.side, Generic.objectToString(this));
                        amount = Math.max(size, size * (price - 1d)); // assume the worst
                        break;
                } // end switch
            }
        }

        return amount;
    }

    public synchronized String getEventId() {
        if (this.eventId == null) {
            createEventId();
        }
        return this.eventId;
    }

    private synchronized void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public synchronized void createEventId() {
        this.setEventId(Formulas.getEventIdOfMarketId(this.marketId));
        if (this.eventId == null) {
            logger.error("null eventId after creation in CurrentOrderSummary: {}", Generic.objectToString(this));
        }
    }

    public synchronized String getBetId() {
        return betId;
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

    public synchronized void setHandicap(Double handicap) {
        this.handicap = handicap;
    }

    public synchronized PriceSize getPriceSize() {
        return priceSize;
    }

    public synchronized void setPriceSize(PriceSize priceSize) {
        this.priceSize = priceSize;
    }

    public synchronized Double getBspLiability() {
        return bspLiability;
    }

    public synchronized void setBspLiability(Double bspLiability) {
        this.bspLiability = bspLiability;
    }

    public synchronized Side getSide() {
        return side;
    }

    public synchronized void setSide(Side side) {
        this.side = side;
    }

    public synchronized OrderStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(OrderStatus status) {
        this.status = status;
    }

    public synchronized PersistenceType getPersistenceType() {
        return persistenceType;
    }

    public synchronized void setPersistenceType(PersistenceType persistenceType) {
        this.persistenceType = persistenceType;
    }

    public synchronized OrderType getOrderType() {
        return orderType;
    }

    public synchronized void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public synchronized Date getPlacedDate() {
        return placedDate == null ? null : (Date) placedDate.clone();
    }

    public synchronized void setPlacedDate(Date placedDate) {
        this.placedDate = placedDate == null ? null : (Date) placedDate.clone();
    }

    public synchronized Date getMatchedDate() {
        return matchedDate == null ? null : (Date) matchedDate.clone();
    }

    public synchronized void setMatchedDate(Date matchedDate) {
        this.matchedDate = matchedDate == null ? null : (Date) matchedDate.clone();
    }

    public synchronized Double getAveragePriceMatched() {
        return averagePriceMatched;
    }

    public synchronized void setAveragePriceMatched(Double averagePriceMatched) {
        this.averagePriceMatched = averagePriceMatched;
    }

    public synchronized Double getSizeMatched() {
        return sizeMatched;
    }

    public synchronized void setSizeMatched(Double sizeMatched) {
        this.sizeMatched = sizeMatched;
    }

    public synchronized Double getSizeRemaining() {
        return sizeRemaining;
    }

    public synchronized void setSizeRemaining(Double sizeRemaining) {
        this.sizeRemaining = sizeRemaining;
    }

    public synchronized Double getSizeLapsed() {
        return sizeLapsed;
    }

    public synchronized void setSizeLapsed(Double sizeLapsed) {
        this.sizeLapsed = sizeLapsed;
    }

    public synchronized Double getSizeCancelled() {
        return sizeCancelled;
    }

    public synchronized void setSizeCancelled(Double sizeCancelled) {
        this.sizeCancelled = sizeCancelled;
    }

    public synchronized Double getSizeVoided() {
        return sizeVoided;
    }

    public synchronized void setSizeVoided(Double sizeVoided) {
        this.sizeVoided = sizeVoided;
    }

    public synchronized String getRegulatorAuthCode() {
        return regulatorAuthCode;
    }

    public synchronized void setRegulatorAuthCode(String regulatorAuthCode) {
        this.regulatorAuthCode = regulatorAuthCode;
    }

    public synchronized String getRegulatorCode() {
        return regulatorCode;
    }

    public synchronized void setRegulatorCode(String regulatorCode) {
        this.regulatorCode = regulatorCode;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(CurrentOrderSummary other) {
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
    public int hashCode() {
        int hash = 3;
        hash = 73 * hash + Objects.hashCode(this.betId);
        hash = 73 * hash + Objects.hashCode(this.marketId);
        hash = 73 * hash + Objects.hashCode(this.selectionId);
        return hash;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CurrentOrderSummary other = (CurrentOrderSummary) obj;
        if (!Objects.equals(this.betId, other.betId)) {
            return false;
        }
        if (!Objects.equals(this.marketId, other.marketId)) {
            return false;
        }
        return Objects.equals(this.selectionId, other.selectionId);
    }
}
