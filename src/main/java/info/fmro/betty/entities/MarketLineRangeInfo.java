package info.fmro.betty.entities;

import java.io.Serializable;

public class MarketLineRangeInfo
        implements Serializable {
    private static final long serialVersionUID = -4018323838006232985L;
    private Double maxUnitValue;
    private Double minUnitValue;
    private Double interval;
    private String marketUnit;

    public MarketLineRangeInfo() {
    }

    public synchronized Double getMaxUnitValue() {
        return maxUnitValue;
    }

    public synchronized void setMaxUnitValue(final Double maxUnitValue) {
        this.maxUnitValue = maxUnitValue;
    }

    public synchronized Double getMinUnitValue() {
        return minUnitValue;
    }

    public synchronized void setMinUnitValue(final Double minUnitValue) {
        this.minUnitValue = minUnitValue;
    }

    public synchronized Double getInterval() {
        return interval;
    }

    public synchronized void setInterval(final Double interval) {
        this.interval = interval;
    }

    public synchronized String getMarketUnit() {
        return marketUnit;
    }

    public synchronized void setMarketUnit(final String marketUnit) {
        this.marketUnit = marketUnit;
    }
}
