package info.fmro.betty.entities;

public class MarketLineRangeInfo {

    private Double maxUnitValue;
    private Double minUnitValue;
    private Double interval;
    private String marketUnit;

    public MarketLineRangeInfo() {
    }

    public synchronized Double getMaxUnitValue() {
        return maxUnitValue;
    }

    public synchronized void setMaxUnitValue(Double maxUnitValue) {
        this.maxUnitValue = maxUnitValue;
    }

    public synchronized Double getMinUnitValue() {
        return minUnitValue;
    }

    public synchronized void setMinUnitValue(Double minUnitValue) {
        this.minUnitValue = minUnitValue;
    }

    public synchronized Double getInterval() {
        return interval;
    }

    public synchronized void setInterval(Double interval) {
        this.interval = interval;
    }

    public synchronized String getMarketUnit() {
        return marketUnit;
    }

    public synchronized void setMarketUnit(String marketUnit) {
        this.marketUnit = marketUnit;
    }
}
