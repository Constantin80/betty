package info.fmro.betty.entities;

import java.util.ArrayList;
import java.util.List;

public class ClearedOrderSummaryReport {

    private List<ClearedOrderSummary> clearedOrders;
    private Boolean moreAvailable;

    public ClearedOrderSummaryReport() {
    }

    public synchronized int getNClearedOrders() {
        return clearedOrders == null ? -1 : clearedOrders.size();
    }

    public synchronized List<ClearedOrderSummary> getClearedOrders() {
        return clearedOrders == null ? null : new ArrayList<>(clearedOrders);
    }

    public synchronized void setClearedOrders(List<ClearedOrderSummary> clearedOrders) {
        this.clearedOrders = clearedOrders == null ? null : new ArrayList<>(clearedOrders);
    }

    public synchronized Boolean isMoreAvailable() {
        return moreAvailable;
    }

    public synchronized void setMoreAvailable(Boolean moreAvailable) {
        this.moreAvailable = moreAvailable;
    }
}
