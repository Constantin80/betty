package info.fmro.betty.stream.definitions;

import info.fmro.shared.stream.objects.StreamObjectInterface;
import info.fmro.shared.stream.definitions.ResponseMessage;
import info.fmro.shared.stream.enums.ChangeType;
import info.fmro.shared.stream.enums.SegmentType;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// objects of this class are read from the stream
public class OrderChangeMessage
        extends ResponseMessage
        implements Serializable, StreamObjectInterface {
    private static final long serialVersionUID = 5357844283995226019L;
    private String clk; // Token value (non-null) should be stored and passed in a MarketSubscriptionMessage to resume subscription (in case of disconnect)
    private Long conflateMs; // Conflate Milliseconds - the conflation rate (may differ from that requested if subscription is delayed)
    private ChangeType ct; // Change Type - set to indicate the type of change - if null this is a delta)
    private Long heartbeatMs; // Heartbeat Milliseconds - the heartbeat rate (may differ from requested: bounds are 500 to 30000)
    private String initialClk; // Token value (non-null) should be stored and passed in a MarketSubscriptionMessage to resume subscription (in case of disconnect)
    @Nullable
    private List<OrderMarketChange> oc; // MarketChanges - the modifications to markets (will be null on a heartbeat
    @Nullable
    private Date pt; // Publish Time (in millis since epoch) that the changes were generated
    private SegmentType segmentType; // Segment Type - if the change is split into multiple segments, this denotes the beginning and end of a change, and segments in between. Will be null if data is not segmented
    private Integer status; // Stream status: set to null if the exchange stream data is up to date and 503 if the downstream services are experiencing latencies

    public synchronized OrderChangeMessage getCopy() {
        return SerializationUtils.clone(this);
    }

    public synchronized int runAfterReceive() {
        return 0;
    }

    public synchronized void runBeforeSend() {
    }

    public synchronized String getClk() {
        return this.clk;
    }

    public synchronized void setClk(final String clk) {
        this.clk = clk;
    }

    public synchronized Long getConflateMs() {
        return this.conflateMs;
    }

    public synchronized void setConflateMs(final Long conflateMs) {
        this.conflateMs = conflateMs;
    }

    public synchronized ChangeType getCt() {
        return this.ct;
    }

    public synchronized void setCt(final ChangeType ct) {
        this.ct = ct;
    }

    public synchronized Long getHeartbeatMs() {
        return this.heartbeatMs;
    }

    public synchronized void setHeartbeatMs(final Long heartbeatMs) {
        this.heartbeatMs = heartbeatMs;
    }

    public synchronized String getInitialClk() {
        return this.initialClk;
    }

    public synchronized void setInitialClk(final String initialClk) {
        this.initialClk = initialClk;
    }

    @Nullable
    public synchronized List<OrderMarketChange> getOc() {
        return this.oc == null ? null : new ArrayList<>(this.oc);
    }

    public synchronized void setOc(final List<? extends OrderMarketChange> oc) {
        this.oc = oc == null ? null : new ArrayList<>(oc);
    }

    @Nullable
    public synchronized Date getPt() {
        return this.pt == null ? null : (Date) this.pt.clone();
    }

    public synchronized void setPt(final Date pt) {
        this.pt = pt == null ? null : (Date) pt.clone();
    }

    public synchronized SegmentType getSegmentType() {
        return this.segmentType;
    }

    public synchronized void setSegmentType(final SegmentType segmentType) {
        this.segmentType = segmentType;
    }

    public synchronized Integer getStatus() {
        return this.status;
    }

    public synchronized void setStatus(final Integer status) {
        this.status = status;
    }
}
