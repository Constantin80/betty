package info.fmro.betty.entities;

import info.fmro.betty.enums.RollupModel;

public class ExBestOffersOverrides {
    private Integer bestPricesDepth;
    private RollupModel rollupModel;
    private Integer rollupLimit;
    private Double rollupLiabilityThreshold;
    private Integer rollupLiabilityFactor;

    public synchronized Integer getBestPricesDepth() {
        return this.bestPricesDepth;
    }

    public synchronized void setBestPricesDepth(final Integer bestPricesDepth) {
        this.bestPricesDepth = bestPricesDepth;
    }

    public synchronized RollupModel getRollupModel() {
        return this.rollupModel;
    }

    public synchronized void setRollupModel(final RollupModel rollupModel) {
        this.rollupModel = rollupModel;
    }

    public synchronized Integer getRollupLimit() {
        return this.rollupLimit;
    }

    public synchronized void setRollupLimit(final Integer rollupLimit) {
        this.rollupLimit = rollupLimit;
    }

    public synchronized Double getRollupLiabilityThreshold() {
        return this.rollupLiabilityThreshold;
    }

    public synchronized void setRollupLiabilityThreshold(final Double rollupLiabilityThreshold) {
        this.rollupLiabilityThreshold = rollupLiabilityThreshold;
    }

    public synchronized Integer getRollupLiabilityFactor() {
        return this.rollupLiabilityFactor;
    }

    public synchronized void setRollupLiabilityFactor(final Integer rollupLiabilityFactor) {
        this.rollupLiabilityFactor = rollupLiabilityFactor;
    }
}
