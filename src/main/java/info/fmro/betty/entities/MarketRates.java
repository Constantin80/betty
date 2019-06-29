package info.fmro.betty.entities;

public class MarketRates {
    private Double marketBaseRate;
    private Boolean discountAllowed;

    public MarketRates() {
    }

    public synchronized Double getMarketBaseRate() {
        return marketBaseRate;
    }

    public synchronized void setMarketBaseRate(final Double marketBaseRate) {
        this.marketBaseRate = marketBaseRate;
    }

    public synchronized Boolean isDiscountAllowed() {
        return discountAllowed;
    }

    public synchronized void setDiscountAllowed(final Boolean discountAllowed) {
        this.discountAllowed = discountAllowed;
    }
}
