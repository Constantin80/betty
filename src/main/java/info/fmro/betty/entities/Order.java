package info.fmro.betty.entities;

import info.fmro.betty.enums.OrderStatus;
import info.fmro.betty.enums.OrderType;
import info.fmro.betty.enums.PersistenceType;
import info.fmro.betty.enums.Side;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

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
    private String customerOrderRef; // The customer order reference sent for this bet
    private String customerStrategyRef; // The customer strategy reference sent for this bet

    public Order() {
    }

    public Order(final String betId, final OrderType orderType, final OrderStatus status, final PersistenceType persistenceType, final Side side, final Double price, final Double size, final Double bspLiability, final Date placedDate, final Double avgPriceMatched, final Double sizeMatched, final Double sizeRemaining,
                 final Double sizeLapsed, final Double sizeCancelled, final Double sizeVoided) {
        this.betId = betId;
        this.orderType = orderType;
        this.status = status;
        this.persistenceType = persistenceType;
        this.side = side;
        this.price = price;
        this.size = size;
        this.bspLiability = bspLiability;
        this.placedDate = placedDate;
        this.avgPriceMatched = avgPriceMatched;
        this.sizeMatched = sizeMatched;
        this.sizeRemaining = sizeRemaining;
        this.sizeLapsed = sizeLapsed;
        this.sizeCancelled = sizeCancelled;
        this.sizeVoided = sizeVoided;
    }

    public synchronized String getBetId() {
        return betId;
    }

    //    public synchronized void setBetId(String betId) {
//        this.betId = betId;
//    }
    public synchronized OrderType getOrderType() {
        return orderType;
    }

    //    public synchronized void setOrderType(OrderType orderType) {
//        this.orderType = orderType;
//    }
    public synchronized OrderStatus getStatus() {
        return status;
    }

    //    public synchronized void setStatus(OrderStatus status) {
//        this.status = status;
//    }
    public synchronized PersistenceType getPersistenceType() {
        return persistenceType;
    }

    //    public synchronized void setPersistenceType(PersistenceType persistenceType) {
//        this.persistenceType = persistenceType;
//    }
    public synchronized Side getSide() {
        return side;
    }

    //    public synchronized void setSide(Side side) {
//        this.side = side;
//    }
    public synchronized Double getPrice() {
        return price;
    }

    //    public synchronized void setPrice(Double price) {
//        this.price = price;
//    }
    public synchronized Double getSize() {
        return size;
    }

    //    public synchronized void setSize(Double size) {
//        this.size = size;
//    }
    public synchronized Double getBspLiability() {
        return bspLiability;
    }

    //    public synchronized void setBspLiability(Double bspLiability) {
//        this.bspLiability = bspLiability;
//    }
    public synchronized Date getPlacedDate() {
        return placedDate == null ? null : (Date) placedDate.clone();
    }

    //    public synchronized void setPlacedDate(Date placedDate) {
//        this.placedDate = placedDate == null ? null : (Date) placedDate.clone();
//    }
    public synchronized Double getAvgPriceMatched() {
        return avgPriceMatched;
    }

    //    public synchronized void setAvgPriceMatched(Double avgPriceMatched) {
//        this.avgPriceMatched = avgPriceMatched;
//    }
    public synchronized Double getSizeMatched() {
        return sizeMatched;
    }

    //    public synchronized void setSizeMatched(Double sizeMatched) {
//        this.sizeMatched = sizeMatched;
//    }
    public synchronized Double getSizeRemaining() {
        return sizeRemaining;
    }

    //    public synchronized void setSizeRemaining(Double sizeRemaining) {
//        this.sizeRemaining = sizeRemaining;
//    }
    public synchronized Double getSizeLapsed() {
        return sizeLapsed;
    }

    //    public synchronized void setSizeLapsed(Double sizeLapsed) {
//        this.sizeLapsed = sizeLapsed;
//    }
    public synchronized Double getSizeCancelled() {
        return sizeCancelled;
    }

    //    public synchronized void setSizeCancelled(Double sizeCancelled) {
//        this.sizeCancelled = sizeCancelled;
//    }
    public synchronized Double getSizeVoided() {
        return sizeVoided;
    }

    //    public synchronized void setSizeVoided(Double sizeVoided) {
//        this.sizeVoided = sizeVoided;
//    }
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

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
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
