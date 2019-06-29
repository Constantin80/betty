package info.fmro.betty.stream.definitions;

import java.io.Serializable;

public class MarketSubscriptionMessage
        extends RequestMessage
        implements Serializable {
    private static final long serialVersionUID = -5514747219419338428L;
    private String clk; // Token value (non-null) should be stored and passed in a MarketSubscriptionMessage to resume subscription (in case of disconnect)
    private Long conflateMs; // Conflate Milliseconds - the conflation rate (may differ from that requested if subscription is delayed)
    private Long heartbeatMs; // Heartbeat Milliseconds - the heartbeat rate (may differ from requested: bounds are 500 to 30000)
    private String initialClk; // Token value (non-null) should be stored and passed in a MarketSubscriptionMessage to resume subscription (in case of disconnect)
    private MarketDataFilter marketDataFilter;
    private MarketFilter marketFilter;
    private Boolean segmentationEnabled = true; // Segmentation Enabled - allow the server to send large sets of data in segments, instead of a single block

    public MarketSubscriptionMessage() {
    }

    public synchronized String getClk() {
        return clk;
    }

    public synchronized void setClk(final String clk) {
        this.clk = clk;
    }

    public synchronized Long getConflateMs() {
        return conflateMs;
    }

    public synchronized void setConflateMs(final Long conflateMs) {
        this.conflateMs = conflateMs;
    }

    public synchronized Long getHeartbeatMs() {
        return heartbeatMs;
    }

    public synchronized void setHeartbeatMs(final Long heartbeatMs) {
        this.heartbeatMs = heartbeatMs;
    }

    public synchronized String getInitialClk() {
        return initialClk;
    }

    public synchronized void setInitialClk(final String initialClk) {
        this.initialClk = initialClk;
    }

    public synchronized MarketDataFilter getMarketDataFilter() {
        return marketDataFilter;
    }

    public synchronized void setMarketDataFilter(final MarketDataFilter marketDataFilter) {
        this.marketDataFilter = marketDataFilter;
    }

    public synchronized MarketFilter getMarketFilter() {
        return marketFilter;
    }

    public synchronized void setMarketFilter(final MarketFilter marketFilter) {
        this.marketFilter = marketFilter;
    }

    public synchronized Boolean getSegmentationEnabled() {
        return segmentationEnabled;
    }

    public synchronized void setSegmentationEnabled(final Boolean segmentationEnabled) {
        this.segmentationEnabled = segmentationEnabled;
    }
}
