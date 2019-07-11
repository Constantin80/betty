package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.enums.ChangeType;
import info.fmro.betty.stream.enums.SegmentType;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChangeMessage<T>
        implements Serializable {
    private static final long serialVersionUID = -5486319357290474083L;
    @Nullable
    private Date arrivalTime;
    @Nullable
    private Date publishTime;
    private final int clientId;
    private Integer id;
    private String clk;
    private String initialClk;
    private Long heartbeatMs;
    private Long conflateMs;
    @Nullable
    private List<T> items;
    private SegmentType segmentType;
    private ChangeType changeType;

    ChangeMessage(final int clientId) {
        this.clientId = clientId;
        this.arrivalTime = new Date(System.currentTimeMillis());
    }

    public synchronized int getClientId() {
        return this.clientId;
    }

    // Start of new subscription (not resubscription)
    public synchronized boolean isStartOfNewSubscription() {
        return this.changeType == ChangeType.SUB_IMAGE && (this.segmentType == SegmentType.NONE || this.segmentType == SegmentType.SEG_START);
    }

    // Start of subscription / resubscription
    synchronized boolean isStartOfRecovery() {
        return (this.changeType == ChangeType.SUB_IMAGE || this.changeType == ChangeType.RESUB_DELTA) && (this.segmentType == SegmentType.NONE || this.segmentType == SegmentType.SEG_START);
    }

    // End of subscription / resubscription
    synchronized boolean isEndOfRecovery() {
        return (this.changeType == ChangeType.SUB_IMAGE || this.changeType == ChangeType.RESUB_DELTA) && (this.segmentType == SegmentType.NONE || this.segmentType == SegmentType.SEG_END);
    }

    synchronized ChangeType getChangeType() {
        return this.changeType;
    }

    synchronized void setChangeType(final ChangeType changeType) {
        this.changeType = changeType;
    }

    @Nullable
    synchronized Date getArrivalTime() {
        return this.arrivalTime == null ? null : (Date) this.arrivalTime.clone();
    }

    public synchronized void setArrivalTime(final Date arrivalTime) {
        this.arrivalTime = arrivalTime == null ? null : (Date) arrivalTime.clone();
    }

    @Nullable
    synchronized Date getPublishTime() {
        return this.publishTime == null ? null : (Date) this.publishTime.clone();
    }

    synchronized void setPublishTime(final Date publishTime) {
        this.publishTime = publishTime == null ? null : (Date) publishTime.clone();
    }

    public synchronized Integer getId() {
        return this.id;
    }

    public synchronized void setId(final Integer id) {
        this.id = id;
    }

    synchronized String getClk() {
        return this.clk;
    }

    synchronized void setClk(final String clk) {
        this.clk = clk;
    }

    synchronized String getInitialClk() {
        return this.initialClk;
    }

    synchronized void setInitialClk(final String initialClk) {
        this.initialClk = initialClk;
    }

    synchronized Long getHeartbeatMs() {
        return this.heartbeatMs;
    }

    synchronized void setHeartbeatMs(final Long heartbeatMs) {
        this.heartbeatMs = heartbeatMs;
    }

    synchronized Long getConflateMs() {
        return this.conflateMs;
    }

    synchronized void setConflateMs(final Long conflateMs) {
        this.conflateMs = conflateMs;
    }

    @Nullable
    public synchronized List<T> getItems() {
        return this.items == null ? null : new ArrayList<>(this.items);
    }

    synchronized void setItems(final List<? extends T> items) {
        this.items = items == null ? null : new ArrayList<>(items);
    }

    synchronized SegmentType getSegmentType() {
        return this.segmentType;
    }

    synchronized void setSegmentType(final SegmentType segmentType) {
        this.segmentType = segmentType;
    }
}
