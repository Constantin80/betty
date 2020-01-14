package info.fmro.betty.entities;

import info.fmro.shared.entities.PriceSize;
import info.fmro.shared.enums.OrderStatus;
import info.fmro.shared.enums.OrderType;
import info.fmro.shared.enums.PersistenceType;
import info.fmro.shared.enums.Side;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public final class CurrentOrderSummary
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
    @Nullable
    private Date placedDate;
    @Nullable
    private Date matchedDate;
    private Double averagePriceMatched;
    private Double sizeMatched;
    private Double sizeRemaining;
    private Double sizeLapsed;
    private Double sizeCancelled;
    private Double sizeVoided;
    private String regulatorAuthCode;
    private String regulatorCode;
    private String customerOrderRef; // The order reference defined by the customer for this bet
    private String customerStrategyRef; // The strategy reference defined by the customer for this bet
    private String eventId; // created using the marketId

    @Contract(pure = true)
    private CurrentOrderSummary(final String betId, final String marketId, final Long selectionId) {
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
                amount = Math.max(size, info.fmro.shared.utility.Formulas.layExposure(price, size)); // assume the worst
            } else {
                switch (this.side) {
                    case BACK:
                        amount = size;
                        break;
                    case LAY:
                        amount = info.fmro.shared.utility.Formulas.layExposure(price, size);
                        break;
                    default:
                        logger.error("unknown side {} in CurrentOrderSummary placedAmount: {}", this.side, Generic.objectToString(this));
                        amount = Math.max(size, info.fmro.shared.utility.Formulas.layExposure(price, size)); // assume the worst
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

    private synchronized void setEventId(final String eventId) {
        this.eventId = eventId;
    }

    private synchronized void createEventId() {
        this.setEventId(Formulas.getEventIdOfMarketId(this.marketId));
        if (this.eventId == null) {
            logger.info("null eventId after creation in CurrentOrderSummary: {}", Generic.objectToString(this));
        }
    }

    public synchronized String getBetId() {
        return this.betId;
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

    public synchronized Double getHandicap() {
        return this.handicap;
    }

    public synchronized void setHandicap(final Double handicap) {
        this.handicap = handicap;
    }

    public synchronized PriceSize getPriceSize() {
        return this.priceSize;
    }

    public synchronized void setPriceSize(final PriceSize priceSize) {
        this.priceSize = priceSize;
    }

    public synchronized Double getBspLiability() {
        return this.bspLiability;
    }

    public synchronized void setBspLiability(final Double bspLiability) {
        this.bspLiability = bspLiability;
    }

    public synchronized Side getSide() {
        return this.side;
    }

    public synchronized void setSide(final Side side) {
        this.side = side;
    }

    public synchronized OrderStatus getStatus() {
        return this.status;
    }

    public synchronized void setStatus(final OrderStatus status) {
        this.status = status;
    }

    public synchronized PersistenceType getPersistenceType() {
        return this.persistenceType;
    }

    public synchronized void setPersistenceType(final PersistenceType persistenceType) {
        this.persistenceType = persistenceType;
    }

    public synchronized OrderType getOrderType() {
        return this.orderType;
    }

    public synchronized void setOrderType(final OrderType orderType) {
        this.orderType = orderType;
    }

    @Nullable
    public synchronized Date getPlacedDate() {
        return this.placedDate == null ? null : (Date) this.placedDate.clone();
    }

    public synchronized void setPlacedDate(final Date placedDate) {
        this.placedDate = placedDate == null ? null : (Date) placedDate.clone();
    }

    @Nullable
    public synchronized Date getMatchedDate() {
        return this.matchedDate == null ? null : (Date) this.matchedDate.clone();
    }

    public synchronized void setMatchedDate(final Date matchedDate) {
        this.matchedDate = matchedDate == null ? null : (Date) matchedDate.clone();
    }

    public synchronized Double getAveragePriceMatched() {
        return this.averagePriceMatched;
    }

    public synchronized void setAveragePriceMatched(final Double averagePriceMatched) {
        this.averagePriceMatched = averagePriceMatched;
    }

    public synchronized Double getSizeMatched() {
        return this.sizeMatched;
    }

    public synchronized void setSizeMatched(final Double sizeMatched) {
        this.sizeMatched = sizeMatched;
    }

    public synchronized Double getSizeRemaining() {
        return this.sizeRemaining;
    }

    public synchronized void setSizeRemaining(final Double sizeRemaining) {
        this.sizeRemaining = sizeRemaining;
    }

    public synchronized Double getSizeLapsed() {
        return this.sizeLapsed;
    }

    public synchronized void setSizeLapsed(final Double sizeLapsed) {
        this.sizeLapsed = sizeLapsed;
    }

    public synchronized Double getSizeCancelled() {
        return this.sizeCancelled;
    }

    public synchronized void setSizeCancelled(final Double sizeCancelled) {
        this.sizeCancelled = sizeCancelled;
    }

    public synchronized Double getSizeVoided() {
        return this.sizeVoided;
    }

    public synchronized void setSizeVoided(final Double sizeVoided) {
        this.sizeVoided = sizeVoided;
    }

    public synchronized String getRegulatorAuthCode() {
        return this.regulatorAuthCode;
    }

    public synchronized void setRegulatorAuthCode(final String regulatorAuthCode) {
        this.regulatorAuthCode = regulatorAuthCode;
    }

    public synchronized String getRegulatorCode() {
        return this.regulatorCode;
    }

    public synchronized void setRegulatorCode(final String regulatorCode) {
        this.regulatorCode = regulatorCode;
    }

    public synchronized String getCustomerOrderRef() {
        return this.customerOrderRef;
    }

    public synchronized void setCustomerOrderRef(final String customerOrderRef) {
        this.customerOrderRef = customerOrderRef;
    }

    public synchronized String getCustomerStrategyRef() {
        return this.customerStrategyRef;
    }

    public synchronized void setCustomerStrategyRef(final String customerStrategyRef) {
        this.customerStrategyRef = customerStrategyRef;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public synchronized int compareTo(@NotNull final CurrentOrderSummary o) {
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
        if (!Objects.equals(this.betId, o.betId)) {
            if (this.betId == null) {
                return BEFORE;
            }
            if (o.betId == null) {
                return AFTER;
            }
            return this.betId.compareTo(o.betId);
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
        if (!Objects.equals(this.selectionId, o.selectionId)) {
            if (this.selectionId == null) {
                return BEFORE;
            }
            if (o.selectionId == null) {
                return AFTER;
            }
            return this.selectionId.compareTo(o.selectionId);
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
