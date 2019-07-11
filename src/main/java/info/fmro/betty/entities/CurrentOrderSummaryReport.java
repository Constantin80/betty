package info.fmro.betty.entities;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CurrentOrderSummaryReport {
    @Nullable
    private List<CurrentOrderSummary> currentOrders;
    private Boolean moreAvailable;

    public synchronized int getNCurrentOrders() {
        return this.currentOrders == null ? -1 : this.currentOrders.size();
    }

    @Nullable
    public synchronized List<CurrentOrderSummary> getCurrentOrders() {
        return this.currentOrders == null ? null : new ArrayList<>(this.currentOrders);
    }

    public synchronized void setCurrentOrders(final List<CurrentOrderSummary> currentOrders) {
        this.currentOrders = currentOrders == null ? null : new ArrayList<>(currentOrders);
    }

    public synchronized Boolean getMoreAvailable() {
        return this.moreAvailable;
    }

    public synchronized void setMoreAvailable(final Boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }
}
