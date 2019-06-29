package info.fmro.betty.entities;

import info.fmro.betty.enums.RunnerStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Runner
        implements Serializable {
    private static final long serialVersionUID = -8538359656490442656L;
    private Long selectionId;
    private Double handicap;
    private RunnerStatus status;
    private Double adjustmentFactor;
    private Double lastPriceTraded;
    private Double totalMatched;
    private Date removalDate;
    private StartingPrices sp;
    private ExchangePrices ex;
    private List<Order> orders;
    private List<Match> matches;
    private Map<String, List<Match>> matchesByStrategy;

    public Runner() {
    }

    public Runner(final Long selectionId, final Double handicap, final RunnerStatus status, final Double adjustmentFactor, final Double lastPriceTraded, final Double totalMatched, final Date removalDate, final StartingPrices sp, final ExchangePrices ex, final List<Order> orders, final List<Match> matches) {
        this.selectionId = selectionId;
        this.handicap = handicap;
        this.status = status;
        this.adjustmentFactor = adjustmentFactor;
        this.lastPriceTraded = lastPriceTraded;
        this.totalMatched = totalMatched;
        this.removalDate = removalDate;
        this.sp = sp;
        this.ex = ex;
        this.orders = orders;
        this.matches = matches;
    }

    public synchronized Long getSelectionId() {
        return selectionId;
    }

    //    public synchronized void setSelectionId(Long selectionId) {
//        this.selectionId = selectionId;
//    }
    public synchronized Double getHandicap() {
        return handicap;
    }

    //    public synchronized void setHandicap(Double handicap) {
//        this.handicap = handicap;
//    }
    public synchronized RunnerStatus getStatus() {
        return status;
    }

    //    public synchronized void setStatus(RunnerStatus status) {
//        this.status = status;
//    }
    public synchronized Double getAdjustmentFactor() {
        return adjustmentFactor;
    }

    //    public synchronized void setAdjustmentFactor(Double adjustmentFactor) {
//        this.adjustmentFactor = adjustmentFactor;
//    }
    public synchronized Double getLastPriceTraded() {
        return lastPriceTraded;
    }

    //    public synchronized void setLastPriceTraded(Double lastPriceTraded) {
//        this.lastPriceTraded = lastPriceTraded;
//    }
    public synchronized Double getTotalMatched() {
        return totalMatched;
    }

    //    public synchronized void setTotalMatched(Double totalMatched) {
//        this.totalMatched = totalMatched;
//    }
    public synchronized Date getRemovalDate() {
        return removalDate == null ? null : (Date) removalDate.clone();
    }

    //    public synchronized void setRemovalDate(Date removalDate) {
//        this.removalDate = removalDate == null ? null : (Date) removalDate.clone();
//    }
    public synchronized StartingPrices getSp() {
        return sp;
    }

    //    public synchronized void setSp(StartingPrices sp) {
//        this.sp = sp;
//    }
    public synchronized ExchangePrices getEx() {
        return ex;
    }

    //    public synchronized void setEx(ExchangePrices ex) {
//        this.ex = ex;
//    }
    public synchronized List<Order> getOrders() {
        return orders == null ? null : new ArrayList<>(orders);
    }

    //    public synchronized void setOrders(List<Order> orders) {
//        this.orders = orders == null ? null : new ArrayList<>(orders);
//    }
    public synchronized List<Match> getMatches() {
        return matches == null ? null : new ArrayList<>(matches);
    }

    //    public synchronized void setMatches(List<Match> matches) {
//        this.matches = matches == null ? null : new ArrayList<>(matches);
//    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.selectionId);
        hash = 73 * hash + Objects.hashCode(this.handicap);
//        hash = 73 * hash + Objects.hashCode(this.status);
//        hash = 73 * hash + Objects.hashCode(this.adjustmentFactor);
//        hash = 73 * hash + Objects.hashCode(this.lastPriceTraded);
//        hash = 73 * hash + Objects.hashCode(this.totalMatched);
//        hash = 73 * hash + Objects.hashCode(this.removalDate);
//        hash = 73 * hash + Objects.hashCode(this.sp);
//        hash = 73 * hash + Objects.hashCode(this.ex);
//        hash = 73 * hash + Objects.hashCode(this.orders);
//        hash = 73 * hash + Objects.hashCode(this.matches);

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
        final Runner other = (Runner) obj;
        if (!Objects.equals(this.selectionId, other.selectionId)) {
            return false;
        }
        if (!Objects.equals(this.handicap, other.handicap)) {
            return false;
        }
//        if (this.status != other.status) {
//            return false;
//        }
//        if (!Objects.equals(this.adjustmentFactor, other.adjustmentFactor)) {
//            return false;
//        }
//        if (!Objects.equals(this.lastPriceTraded, other.lastPriceTraded)) {
//            return false;
//        }
//        if (!Objects.equals(this.totalMatched, other.totalMatched)) {
//            return false;
//        }
//        if (!Objects.equals(this.removalDate, other.removalDate)) {
//            return false;
//        }
//        if (!Objects.equals(this.sp, other.sp)) {
//            return false;
//        }
//        if (!Objects.equals(this.ex, other.ex)) {
//            return false;
//        }
//        if (!Objects.equals(this.orders, other.orders)) {
//            return false;
//        }
//        if (!Objects.equals(this.matches, other.matches)) {
//            return false;
//        }

        return true;
    }
}
