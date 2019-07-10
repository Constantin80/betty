package info.fmro.betty.entities;

public class MarketTypeResult {
    private String marketType;
    private Integer marketCount;

    public synchronized String getMarketType() {
        return this.marketType;
    }

    public synchronized void setMarketType(final String marketType) {
        this.marketType = marketType;
    }

    public synchronized Integer getMarketCount() {
        return this.marketCount;
    }

    public synchronized void setMarketCount(final Integer marketCount) {
        this.marketCount = marketCount;
    }
}
