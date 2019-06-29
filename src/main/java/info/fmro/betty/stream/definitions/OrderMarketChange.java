package info.fmro.betty.stream.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// objects of this class are read from the stream
public class OrderMarketChange
        implements Serializable {
    private static final long serialVersionUID = -4363278428265553269L;
    private Long accountId;
    private Boolean closed;
    private String id; // Market Id - the id of the market the order is on
    private List<OrderRunnerChange> orc; // Order Changes - a list of changes to orders on a selection

    public OrderMarketChange() {
    }

    public synchronized Long getAccountId() {
        return accountId;
    }

    public synchronized void setAccountId(final Long accountId) {
        this.accountId = accountId;
    }

    public synchronized Boolean getClosed() {
        return closed;
    }

    public synchronized void setClosed(final Boolean closed) {
        this.closed = closed;
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(final String id) {
        this.id = id;
    }

    public synchronized List<OrderRunnerChange> getOrc() {
        return orc == null ? null : new ArrayList<>(orc);
    }

    public synchronized void setOrc(final List<OrderRunnerChange> orc) {
        this.orc = orc == null ? null : new ArrayList<>(orc);
    }
}
