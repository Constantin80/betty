package info.fmro.betty.entities;

import info.fmro.betty.enums.PriceData;
import java.util.HashSet;
import java.util.Set;

public class PriceProjection {

    private Set<PriceData> priceData;
    private ExBestOffersOverrides exBestOffersOverrides;
    private Boolean virtualise;
    private Boolean rolloverStakes;

    public PriceProjection() {
    }

    public synchronized Set<PriceData> getPriceData() {
        return priceData == null ? null : new HashSet<>(priceData);
    }

    public synchronized void setPriceData(Set<PriceData> priceData) {
        this.priceData = priceData == null ? null : new HashSet<>(priceData);
    }

    public synchronized ExBestOffersOverrides getExBestOffersOverrides() {
        return exBestOffersOverrides;
    }

    public synchronized void setExBestOffersOverrides(
            ExBestOffersOverrides exBestOffersOverrides) {
        this.exBestOffersOverrides = exBestOffersOverrides;
    }

    public synchronized Boolean isVirtualise() {
        return virtualise;
    }

    public synchronized void setVirtualise(Boolean virtualise) {
        this.virtualise = virtualise;
    }

    public synchronized Boolean isRolloverStakes() {
        return rolloverStakes;
    }

    public synchronized void setRolloverStakes(Boolean rolloverStakes) {
        this.rolloverStakes = rolloverStakes;
    }
}
