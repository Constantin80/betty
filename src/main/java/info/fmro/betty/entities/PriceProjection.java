package info.fmro.betty.entities;

import info.fmro.betty.enums.PriceData;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class PriceProjection {
    @Nullable
    private Set<PriceData> priceData;
    private ExBestOffersOverrides exBestOffersOverrides;
    private Boolean virtualise;
    private Boolean rolloverStakes;

    @Nullable
    public synchronized Set<PriceData> getPriceData() {
        return this.priceData == null ? null : EnumSet.copyOf(this.priceData); // immutable
    }

    public synchronized void setPriceData(final Collection<PriceData> priceData) {
        this.priceData = priceData == null ? null : EnumSet.copyOf(priceData); // immutable
    }

    public synchronized ExBestOffersOverrides getExBestOffersOverrides() {
        return this.exBestOffersOverrides;
    }

    public synchronized void setExBestOffersOverrides(
            final ExBestOffersOverrides exBestOffersOverrides) {
        this.exBestOffersOverrides = exBestOffersOverrides;
    }

    public synchronized Boolean getVirtualise() {
        return this.virtualise;
    }

    public synchronized void setVirtualise(final Boolean virtualise) {
        this.virtualise = virtualise;
    }

    public synchronized Boolean getRolloverStakes() {
        return this.rolloverStakes;
    }

    public synchronized void setRolloverStakes(final Boolean rolloverStakes) {
        this.rolloverStakes = rolloverStakes;
    }
}
