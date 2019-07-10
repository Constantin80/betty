package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.ChangeType;
import info.fmro.betty.stream.definitions.SegmentType;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mulveyj on 07/07/2016.
 */
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

    public ChangeMessage(final int clientId) {
        this.clientId = clientId;
        this.arrivalTime = new Date(System.currentTimeMillis());
    }

    public synchronized int getClientId() {
        return this.clientId;
    }

    /**
     * Start of new subscription (not resubscription)
     *
     * @return
     */
    public synchronized boolean isStartOfNewSubscription() {
        return this.changeType == ChangeType.SUB_IMAGE &&
               (this.segmentType == SegmentType.NONE || this.segmentType == SegmentType.SEG_START);
    }

    /**
     * Start of subscription / resubscription
     *
     * @return
     */
    public synchronized boolean isStartOfRecovery() {
        return (this.changeType == ChangeType.SUB_IMAGE || this.changeType == ChangeType.RESUB_DELTA) &&
               (this.segmentType == SegmentType.NONE || this.segmentType == SegmentType.SEG_START);
    }

    /**
     * End of subscription / resubscription
     *
     * @return
     */
    public synchronized boolean isEndOfRecovery() {
        return (this.changeType == ChangeType.SUB_IMAGE || this.changeType == ChangeType.RESUB_DELTA) &&
               (this.segmentType == SegmentType.NONE || this.segmentType == SegmentType.SEG_END);
    }

    public synchronized ChangeType getChangeType() {
        return this.changeType;
    }

    public synchronized void setChangeType(final ChangeType changeType) {
        this.changeType = changeType;
    }

    @Nullable
    public synchronized Date getArrivalTime() {
        return this.arrivalTime == null ? null : (Date) this.arrivalTime.clone();
    }

    public synchronized void setArrivalTime(final Date arrivalTime) {
        this.arrivalTime = arrivalTime == null ? null : (Date) arrivalTime.clone();
    }

    @Nullable
    public synchronized Date getPublishTime() {
        return this.publishTime == null ? null : (Date) this.publishTime.clone();
    }

    public synchronized void setPublishTime(final Date publishTime) {
        this.publishTime = publishTime == null ? null : (Date) publishTime.clone();
    }

    public synchronized Integer getId() {
        return this.id;
    }

    public synchronized void setId(final Integer id) {
        this.id = id;
    }

    public synchronized String getClk() {
        return this.clk;
    }

    public synchronized void setClk(final String clk) {
        this.clk = clk;
    }

    public synchronized String getInitialClk() {
        return this.initialClk;
    }

    public synchronized void setInitialClk(final String initialClk) {
        this.initialClk = initialClk;
    }

    public synchronized Long getHeartbeatMs() {
        return this.heartbeatMs;
    }

    public synchronized void setHeartbeatMs(final Long heartbeatMs) {
        this.heartbeatMs = heartbeatMs;
    }

    public synchronized Long getConflateMs() {
        return this.conflateMs;
    }

    public synchronized void setConflateMs(final Long conflateMs) {
        this.conflateMs = conflateMs;
    }

    @Nullable
    public synchronized List<T> getItems() {
        return this.items == null ? null : new ArrayList<>(this.items);
    }

    public synchronized void setItems(final List<? extends T> items) {
        this.items = items == null ? null : new ArrayList<>(items);
    }

    public synchronized SegmentType getSegmentType() {
        return this.segmentType;
    }

    public synchronized void setSegmentType(final SegmentType segmentType) {
        this.segmentType = segmentType;
    }
}
