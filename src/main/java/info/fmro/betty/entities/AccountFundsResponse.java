package info.fmro.betty.entities;

public class AccountFundsResponse {
    private Double availableToBetBalance; // Amount available to bet.
    private Double exposure; // Current exposure. Is a negative number
    private Double retainedCommission; // Sum of retained commission.
    private Double exposureLimit; // Exposure limit.
    private Double discountRate; // User Discount Rate.
    private Integer pointsBalance; // The Betfair points balance

    public AccountFundsResponse() {
    }

    public synchronized Double getAvailableToBetBalance() {
        return availableToBetBalance;
    }

    public synchronized void setAvailableToBetBalance(final Double availableToBetBalance) {
        this.availableToBetBalance = availableToBetBalance;
    }

    public synchronized Double getExposure() {
        return exposure;
    }

    public synchronized void setExposure(final Double exposure) {
        this.exposure = exposure;
    }

    public synchronized Double getRetainedCommission() {
        return retainedCommission;
    }

    public synchronized void setRetainedCommission(final Double retainedCommission) {
        this.retainedCommission = retainedCommission;
    }

    public synchronized Double getExposureLimit() {
        return exposureLimit;
    }

    public synchronized void setExposureLimit(final Double exposureLimit) {
        this.exposureLimit = exposureLimit;
    }

    public synchronized Double getDiscountRate() {
        return discountRate;
    }

    public synchronized void setDiscountRate(final Double discountRate) {
        this.discountRate = discountRate;
    }

    public synchronized Integer getPointsBalance() {
        return pointsBalance;
    }

    public synchronized void setPointsBalance(final Integer pointsBalance) {
        this.pointsBalance = pointsBalance;
    }
}
