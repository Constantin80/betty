package info.fmro.betty.entities;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClearedOrderSummaryReport {
    @Nullable
    private List<ClearedOrderSummary> clearedOrders;
    private Boolean moreAvailable;

    public synchronized int getNClearedOrders() {
        return this.clearedOrders == null ? -1 : this.clearedOrders.size();
    }

    @Nullable
    public synchronized List<ClearedOrderSummary> getClearedOrders() {
        return this.clearedOrders == null ? null : new ArrayList<>(this.clearedOrders);
    }

    public synchronized void setClearedOrders(final List<? extends ClearedOrderSummary> clearedOrders) {
        this.clearedOrders = clearedOrders == null ? null : new ArrayList<>(clearedOrders);
    }

    public synchronized Boolean getMoreAvailable() {
        return this.moreAvailable;
    }

    public synchronized void setMoreAvailable(final Boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }
}
