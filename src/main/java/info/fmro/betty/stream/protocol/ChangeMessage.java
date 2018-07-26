package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.ChangeType;
import info.fmro.betty.stream.definitions.SegmentType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mulveyj on 07/07/2016.
 */
public class ChangeMessage<T> {
    private Date arrivalTime;
    private Date publishTime;
    private final int clientId;
    private Integer id;
    private String clk;
    private String initialClk;
    private Long heartbeatMs;
    private Long conflateMs;
    private List<T> items;
    private SegmentType segmentType;
    private ChangeType changeType;

    public ChangeMessage(int clientId) {
        this.clientId = clientId;
        arrivalTime = new Date(System.currentTimeMillis());
    }

    public synchronized int getClientId() {
        return clientId;
    }

    /**
     * Start of new subscription (not resubscription)
     *
     * @return
     */
    public synchronized boolean isStartOfNewSubscription() {
        return changeType == ChangeType.SUB_IMAGE &&
               (segmentType == SegmentType.NONE || segmentType == SegmentType.SEG_START);
    }

    /**
     * Start of subscription / resubscription
     *
     * @return
     */
    public synchronized boolean isStartOfRecovery() {
        return (changeType == ChangeType.SUB_IMAGE || changeType == ChangeType.RESUB_DELTA) &&
               (segmentType == SegmentType.NONE || segmentType == SegmentType.SEG_START);
    }

    /**
     * End of subscription / resubscription
     *
     * @return
     */
    public synchronized boolean isEndOfRecovery() {
        return (changeType == ChangeType.SUB_IMAGE || changeType == ChangeType.RESUB_DELTA) &&
               (segmentType == SegmentType.NONE || segmentType == SegmentType.SEG_END);
    }

    public synchronized ChangeType getChangeType() {
        return changeType;
    }

    public synchronized void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public synchronized Date getArrivalTime() {
        return arrivalTime == null ? null : (Date) arrivalTime.clone();
    }

    public synchronized void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime == null ? null : (Date) arrivalTime.clone();
    }

    public synchronized Date getPublishTime() {
        return publishTime == null ? null : (Date) publishTime.clone();
    }

    public synchronized void setPublishTime(Date publishTime) {
        this.publishTime = publishTime == null ? null : (Date) publishTime.clone();
    }

    public synchronized Integer getId() {
        return id;
    }

    public synchronized void setId(Integer id) {
        this.id = id;
    }

    public synchronized String getClk() {
        return clk;
    }

    public synchronized void setClk(String clk) {
        this.clk = clk;
    }

    public synchronized String getInitialClk() {
        return initialClk;
    }

    public synchronized void setInitialClk(String initialClk) {
        this.initialClk = initialClk;
    }

    public synchronized Long getHeartbeatMs() {
        return heartbeatMs;
    }

    public synchronized void setHeartbeatMs(Long heartbeatMs) {
        this.heartbeatMs = heartbeatMs;
    }

    public synchronized Long getConflateMs() {
        return conflateMs;
    }

    public synchronized void setConflateMs(Long conflateMs) {
        this.conflateMs = conflateMs;
    }

    public synchronized List<T> getItems() {
        return items == null ? null : new ArrayList<>(items);
    }

    public synchronized void setItems(List<T> items) {
        this.items = items == null ? null : new ArrayList<>(items);
    }

    public synchronized SegmentType getSegmentType() {
        return segmentType;
    }

    public synchronized void setSegmentType(SegmentType segmentType) {
        this.segmentType = segmentType;
    }
}
