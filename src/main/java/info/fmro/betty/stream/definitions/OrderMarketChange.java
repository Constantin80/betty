package info.fmro.betty.stream.definitions;

import java.util.ArrayList;
import java.util.List;

public class OrderMarketChange {
    private Long accountId;
    private Boolean closed;
    private String id; // Market Id - the id of the market the order is on
    private List<OrderRunnerChange> orc; // Order Changes - a list of changes to orders on a selection

    public OrderMarketChange() {
    }

    public synchronized Long getAccountId() {
        return accountId;
    }

    public synchronized void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public synchronized Boolean getClosed() {
        return closed;
    }

    public synchronized void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(String id) {
        this.id = id;
    }

    public synchronized List<OrderRunnerChange> getOrc() {
        return orc == null ? null : new ArrayList<>(orc);
    }

    public synchronized void setOrc(List<OrderRunnerChange> orc) {
        this.orc = orc == null ? null : new ArrayList<>(orc);
    }
}
