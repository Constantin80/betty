package info.fmro.betty.entities;

import info.fmro.betty.enums.RollupModel;

public class ExBestOffersOverrides {

    private Integer bestPricesDepth;
    private RollupModel rollupModel;
    private Integer rollupLimit;
    private Double rollupLiabilityThreshold;
    private Integer rollupLiabilityFactor;

    public ExBestOffersOverrides() {
    }

    public synchronized Integer getBestPricesDepth() {
        return bestPricesDepth;
    }

    public synchronized void setBestPricesDepth(Integer bestPricesDepth) {
        this.bestPricesDepth = bestPricesDepth;
    }

    public synchronized RollupModel getRollupModel() {
        return rollupModel;
    }

    public synchronized void setRollupModel(RollupModel rollupModel) {
        this.rollupModel = rollupModel;
    }

    public synchronized Integer getRollupLimit() {
        return rollupLimit;
    }

    public synchronized void setRollupLimit(Integer rollupLimit) {
        this.rollupLimit = rollupLimit;
    }

    public synchronized Double getRollupLiabilityThreshold() {
        return rollupLiabilityThreshold;
    }

    public synchronized void setRollupLiabilityThreshold(Double rollupLiabilityThreshold) {
        this.rollupLiabilityThreshold = rollupLiabilityThreshold;
    }

    public synchronized Integer getRollupLiabilityFactor() {
        return rollupLiabilityFactor;
    }

    public synchronized void setRollupLiabilityFactor(Integer rollupLiabilityFactor) {
        this.rollupLiabilityFactor = rollupLiabilityFactor;
    }
}
