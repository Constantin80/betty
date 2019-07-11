package info.fmro.betty.entities;

@SuppressWarnings("unused")
class TimeRangeResult {
    private TimeRange timeRange;
    private Integer marketCount;

    public synchronized TimeRange getTimeRange() {
        return this.timeRange;
    }

    public synchronized void setTimeRange(final TimeRange timeRange) {
        this.timeRange = timeRange;
    }

    public synchronized Integer getMarketCount() {
        return this.marketCount;
    }

    public synchronized void setMarketCount(final Integer marketCount) {
        this.marketCount = marketCount;
    }
}
