package info.fmro.betty.entities;

import info.fmro.betty.enums.OrderStatus;
import info.fmro.betty.enums.OrderType;
import info.fmro.betty.enums.PersistenceType;
import info.fmro.betty.enums.Side;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@SuppressWarnings("ClassWithTooManyFields")
public class Order
        implements Serializable {
    private static final long serialVersionUID = 3021807768896649660L;
    private String betId;
    private OrderType orderType;
    private OrderStatus status;
    private PersistenceType persistenceType;
    private Side side;
    private Double price;
    private Double size;
    private Double bspLiability;
    private Date placedDate;
    private Double avgPriceMatched;
    private Double sizeMatched;
    private Double sizeRemaining;
    private Double sizeLapsed;
    private Double sizeCancelled;
    private Double sizeVoided;
    @SuppressWarnings("unused")
    private String customerOrderRef; // The customer order reference sent for this bet
    @SuppressWarnings("unused")
    private String customerStrategyRef; // The customer strategy reference sent for this bet

    @SuppressWarnings("unused")
    @Contract(pure = true)
    private Order() {
    }

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Contract(pure = true)
    public Order(final String betId, final OrderType orderType, final OrderStatus status, final PersistenceType persistenceType, final Side side, final Double price, final Double size, final Double bspLiability, @NotNull final Date placedDate,
                 final Double avgPriceMatched, final Double sizeMatched, final Double sizeRemaining, final Double sizeLapsed, final Double sizeCancelled, final Double sizeVoided) {
        this.betId = betId;
        this.orderType = orderType;
        this.status = status;
        this.persistenceType = persistenceType;
        this.side = side;
        this.price = price;
        this.size = size;
        this.bspLiability = bspLiability;
        this.placedDate = (Date) placedDate.clone();
        this.avgPriceMatched = avgPriceMatched;
        this.sizeMatched = sizeMatched;
        this.sizeRemaining = sizeRemaining;
        this.sizeLapsed = sizeLapsed;
        this.sizeCancelled = sizeCancelled;
        this.sizeVoided = sizeVoided;
    }

    public synchronized String getBetId() {
        return this.betId;
    }

    public synchronized OrderType getOrderType() {
        return this.orderType;
    }

    public synchronized OrderStatus getStatus() {
        return this.status;
    }

    public synchronized PersistenceType getPersistenceType() {
        return this.persistenceType;
    }

    public synchronized Side getSide() {
        return this.side;
    }

    public synchronized Double getPrice() {
        return this.price;
    }

    public synchronized Double getSize() {
        return this.size;
    }

    public synchronized Double getBspLiability() {
        return this.bspLiability;
    }

    @Nullable
    public synchronized Date getPlacedDate() {
        return this.placedDate == null ? null : (Date) this.placedDate.clone();
    }

    public synchronized Double getAvgPriceMatched() {
        return this.avgPriceMatched;
    }

    public synchronized Double getSizeMatched() {
        return this.sizeMatched;
    }

    public synchronized Double getSizeRemaining() {
        return this.sizeRemaining;
    }

    public synchronized Double getSizeLapsed() {
        return this.sizeLapsed;
    }

    public synchronized Double getSizeCancelled() {
        return this.sizeCancelled;
    }

    public synchronized Double getSizeVoided() {
        return this.sizeVoided;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.betId);
        hash = 73 * hash + Objects.hashCode(this.orderType);
        hash = 73 * hash + Objects.hashCode(this.status);
        hash = 73 * hash + Objects.hashCode(this.persistenceType);
        hash = 73 * hash + Objects.hashCode(this.side);
        hash = 73 * hash + Objects.hashCode(this.price);
        hash = 73 * hash + Objects.hashCode(this.size);
        hash = 73 * hash + Objects.hashCode(this.bspLiability);
        hash = 73 * hash + Objects.hashCode(this.placedDate);
        hash = 73 * hash + Objects.hashCode(this.avgPriceMatched);
        hash = 73 * hash + Objects.hashCode(this.sizeMatched);
        hash = 73 * hash + Objects.hashCode(this.sizeRemaining);
        hash = 73 * hash + Objects.hashCode(this.sizeLapsed);
        hash = 73 * hash + Objects.hashCode(this.sizeCancelled);
        hash = 73 * hash + Objects.hashCode(this.sizeVoided);
        return hash;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Order other = (Order) obj;
        if (!Objects.equals(this.betId, other.betId)) {
            return false;
        }
        if (this.orderType != other.orderType) {
            return false;
        }
        if (this.status != other.status) {
            return false;
        }
        if (this.persistenceType != other.persistenceType) {
            return false;
        }
        if (this.side != other.side) {
            return false;
        }
        if (!Objects.equals(this.price, other.price)) {
            return false;
        }
        if (!Objects.equals(this.size, other.size)) {
            return false;
        }
        if (!Objects.equals(this.bspLiability, other.bspLiability)) {
            return false;
        }
        if (!Objects.equals(this.placedDate, other.placedDate)) {
            return false;
        }
        if (!Objects.equals(this.avgPriceMatched, other.avgPriceMatched)) {
            return false;
        }
        if (!Objects.equals(this.sizeMatched, other.sizeMatched)) {
            return false;
        }
        if (!Objects.equals(this.sizeRemaining, other.sizeRemaining)) {
            return false;
        }
        if (!Objects.equals(this.sizeLapsed, other.sizeLapsed)) {
            return false;
        }
        if (!Objects.equals(this.sizeCancelled, other.sizeCancelled)) {
            return false;
        }
        return Objects.equals(this.sizeVoided, other.sizeVoided);
    }
}
