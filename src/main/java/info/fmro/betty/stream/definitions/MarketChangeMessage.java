package info.fmro.betty.stream.definitions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketChangeMessage
        extends ResponseMessage {
    private String clk; // Token value (non-null) should be stored and passed in a MarketSubscriptionMessage to resume subscription (in case of disconnect)
    private Long conflateMs; // Conflate Milliseconds - the conflation rate (may differ from that requested if subscription is delayed)
    private ChangeType ct; // Change Type - set to indicate the type of change - if null this is a delta)
    private Long heartbeatMs; // Heartbeat Milliseconds - the heartbeat rate (may differ from requested: bounds are 500 to 30000)
    private String initialClk; // Token value (non-null) should be stored and passed in a MarketSubscriptionMessage to resume subscription (in case of disconnect)
    private List<MarketChange> mc; // MarketChanges - the modifications to markets (will be null on a heartbeat
    private Date pt; // Publish Time (in millis since epoch) that the changes were generated
    private SegmentType segmentType; // Segment Type - if the change is split into multiple segments, this denotes the beginning and end of a change, and segments in between. Will be null if data is not segmented
    private Integer status; // Stream status: set to null if the exchange stream data is up to date and 503 if the downstream services are experiencing latencies

    public MarketChangeMessage() {
    }

    public synchronized String getClk() {
        return clk;
    }

    public synchronized void setClk(String clk) {
        this.clk = clk;
    }

    public synchronized Long getConflateMs() {
        return conflateMs;
    }

    public synchronized void setConflateMs(Long conflateMs) {
        this.conflateMs = conflateMs;
    }

    public synchronized ChangeType getCt() {
        return ct;
    }

    public synchronized void setCt(ChangeType ct) {
        this.ct = ct;
    }

    public synchronized Long getHeartbeatMs() {
        return heartbeatMs;
    }

    public synchronized void setHeartbeatMs(Long heartbeatMs) {
        this.heartbeatMs = heartbeatMs;
    }

    public synchronized String getInitialClk() {
        return initialClk;
    }

    public synchronized void setInitialClk(String initialClk) {
        this.initialClk = initialClk;
    }

    public synchronized List<MarketChange> getMc() {
        return mc == null ? null : new ArrayList<>(mc);
    }

    public synchronized void setMc(List<MarketChange> mc) {
        this.mc = mc == null ? null : new ArrayList<>(mc);
    }

    public synchronized Date getPt() {
        return pt == null ? null : (Date) pt.clone();
    }

    public synchronized void setPt(Date pt) {
        this.pt = pt == null ? null : (Date) pt.clone();
    }

    public synchronized SegmentType getSegmentType() {
        return segmentType;
    }

    public synchronized void setSegmentType(SegmentType segmentType) {
        this.segmentType = segmentType;
    }

    public synchronized Integer getStatus() {
        return status;
    }

    public synchronized void setStatus(Integer status) {
        this.status = status;
    }
}
