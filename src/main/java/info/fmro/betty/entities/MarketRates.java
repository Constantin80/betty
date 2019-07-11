package info.fmro.betty.entities;

@SuppressWarnings("unused")
class MarketRates {
    private Double marketBaseRate;
    private Boolean discountAllowed;

    public synchronized Double getMarketBaseRate() {
        return this.marketBaseRate;
    }

    public synchronized void setMarketBaseRate(final Double marketBaseRate) {
        this.marketBaseRate = marketBaseRate;
    }

    public synchronized Boolean getDiscountAllowed() {
        return this.discountAllowed;
    }

    public synchronized void setDiscountAllowed(final Boolean discountAllowed) {
        this.discountAllowed = discountAllowed;
    }
}
