package info.fmro.betty.entities;

import java.util.ArrayList;
import java.util.List;

public class CurrentOrderSummaryReport {
    private List<CurrentOrderSummary> currentOrders;
    private Boolean moreAvailable;

    public CurrentOrderSummaryReport() {
    }

    public synchronized int getNCurrentOrders() {
        return currentOrders == null ? -1 : currentOrders.size();
    }

    public synchronized List<CurrentOrderSummary> getCurrentOrders() {
        return currentOrders == null ? null : new ArrayList<>(currentOrders);
    }

    public synchronized void setCurrentOrders(final List<CurrentOrderSummary> currentOrders) {
        this.currentOrders = currentOrders == null ? null : new ArrayList<>(currentOrders);
    }

    public synchronized Boolean isMoreAvailable() {
        return moreAvailable;
    }

    public synchronized void setMoreAvailable(final Boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }
}
