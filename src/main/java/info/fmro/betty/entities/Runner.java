package info.fmro.betty.entities;

import info.fmro.betty.enums.RunnerStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("ClassWithTooManyFields")
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
    private List<? extends Order> orders;
    private List<? extends Match> matches;
    @SuppressWarnings("unused")
    private Map<String, List<Match>> matchesByStrategy;

    @Contract(pure = true)
    public Runner() {
    }

    @SuppressWarnings("ConstructorWithTooManyParameters")
    @Contract(pure = true)
    public Runner(final Long selectionId, final Double handicap, final RunnerStatus status, final Double adjustmentFactor, final Double lastPriceTraded, final Double totalMatched, @NotNull final Date removalDate, final StartingPrices sp,
                  final ExchangePrices ex, @NotNull final List<? extends Order> orders, @NotNull final List<? extends Match> matches) {
        this.selectionId = selectionId;
        this.handicap = handicap;
        this.status = status;
        this.adjustmentFactor = adjustmentFactor;
        this.lastPriceTraded = lastPriceTraded;
        this.totalMatched = totalMatched;
        this.removalDate = (Date) removalDate.clone();
        this.sp = sp;
        this.ex = ex;
        this.orders = new ArrayList<>(orders);
        this.matches = new ArrayList<>(matches);
    }

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

    public synchronized Double getHandicap() {
        return this.handicap;
    }

    public synchronized RunnerStatus getStatus() {
        return this.status;
    }

    public synchronized Double getAdjustmentFactor() {
        return this.adjustmentFactor;
    }

    public synchronized Double getLastPriceTraded() {
        return this.lastPriceTraded;
    }

    public synchronized Double getTotalMatched() {
        return this.totalMatched;
    }

    @Nullable
    public synchronized Date getRemovalDate() {
        return this.removalDate == null ? null : (Date) this.removalDate.clone();
    }

    public synchronized StartingPrices getSp() {
        return this.sp;
    }

    public synchronized ExchangePrices getEx() {
        return this.ex;
    }

    @Nullable
    public synchronized List<Order> getOrders() {
        return this.orders == null ? null : new ArrayList<>(this.orders);
    }

    @Nullable
    public synchronized List<Match> getMatches() {
        return this.matches == null ? null : new ArrayList<>(this.matches);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.selectionId);
        hash = 73 * hash + Objects.hashCode(this.handicap);
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
        final Runner other = (Runner) obj;
        if (!Objects.equals(this.selectionId, other.selectionId)) {
            return false;
        }
        return Objects.equals(this.handicap, other.handicap);
    }
}
