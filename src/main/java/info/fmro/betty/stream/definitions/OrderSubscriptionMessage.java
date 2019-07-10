package info.fmro.betty.stream.definitions;

import java.io.Serializable;

public class OrderSubscriptionMessage
        extends RequestMessage
        implements Serializable {
    private static final long serialVersionUID = 4971337324003024539L;
    private String clk; // Token value (non-null) should be stored and passed in a MarketSubscriptionMessage to resume subscription (in case of disconnect)
    private Long conflateMs; // Conflate Milliseconds - the conflation rate (may differ from that requested if subscription is delayed)
    private Long heartbeatMs; // Heartbeat Milliseconds - the heartbeat rate (may differ from requested: bounds are 500 to 30000)
    private String initialClk; // Token value (non-null) should be stored and passed in a MarketSubscriptionMessage to resume subscription (in case of disconnect)
    private OrderFilter orderFilter;
    private Boolean segmentationEnabled = true; // Segmentation Enabled - allow the server to send large sets of data in segments, instead of a single block

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

    public synchronized OrderFilter getOrderFilter() {
        return this.orderFilter;
    }

    public synchronized void setOrderFilter(final OrderFilter orderFilter) {
        this.orderFilter = orderFilter;
    }

    public synchronized Boolean getSegmentationEnabled() {
        return this.segmentationEnabled;
    }

    public synchronized void setSegmentationEnabled(final Boolean segmentationEnabled) {
        this.segmentationEnabled = segmentationEnabled;
    }
}
