package info.fmro.betty.stream.definitions;

public class OrderSubscriptionMessage
        extends RequestMessage {
    private String clk; // Token value (non-null) should be stored and passed in a MarketSubscriptionMessage to resume subscription (in case of disconnect)
    private Long conflateMs; // Conflate Milliseconds - the conflation rate (may differ from that requested if subscription is delayed)
    private Long heartbeatMs; // Heartbeat Milliseconds - the heartbeat rate (may differ from requested: bounds are 500 to 30000)
    private String initialClk; // Token value (non-null) should be stored and passed in a MarketSubscriptionMessage to resume subscription (in case of disconnect)
    private OrderFilter orderFilter;
    private Boolean segmentationEnabled = true; // Segmentation Enabled - allow the server to send large sets of data in segments, instead of a single block

    public OrderSubscriptionMessage() {
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

    public synchronized OrderFilter getOrderFilter() {
        return orderFilter;
    }

    public synchronized void setOrderFilter(OrderFilter orderFilter) {
        this.orderFilter = orderFilter;
    }

    public synchronized Boolean getSegmentationEnabled() {
        return segmentationEnabled;
    }

    public synchronized void setSegmentationEnabled(Boolean segmentationEnabled) {
        this.segmentationEnabled = segmentationEnabled;
    }
}
