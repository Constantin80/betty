package info.fmro.betty.entities;

public class MarketTypeResult {

    private String marketType;
    private Integer marketCount;

    public MarketTypeResult() {
    }

    public synchronized String getMarketType() {
        return marketType;
    }

    public synchronized void setMarketType(String marketType) {
        this.marketType = marketType;
    }

    public synchronized Integer getMarketCount() {
        return marketCount;
    }

    public synchronized void setMarketCount(Integer marketCount) {
        this.marketCount = marketCount;
    }
}
