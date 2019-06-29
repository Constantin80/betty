package info.fmro.betty.entities;

public class TimeRangeResult {
    private TimeRange timeRange;
    private Integer marketCount;

    public TimeRangeResult() {
    }

    public synchronized TimeRange getTimeRange() {
        return timeRange;
    }

    public synchronized void setTimeRange(final TimeRange timeRange) {
        this.timeRange = timeRange;
    }

    public synchronized Integer getMarketCount() {
        return marketCount;
    }

    public synchronized void setMarketCount(final Integer marketCount) {
        this.marketCount = marketCount;
    }
}
