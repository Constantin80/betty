package info.fmro.betty.entities;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
class MarketProfitAndLoss {
    private String marketId;
    private Double commissionApplied;
    @Nullable
    private List<RunnerProfitAndLoss> profitAndLosses;

    public synchronized String getMarketId() {
        return this.marketId;
    }

    public synchronized void setMarketId(final String marketId) {
        this.marketId = marketId;
    }

    public synchronized Double getCommissionApplied() {
        return this.commissionApplied;
    }

    public synchronized void setCommissionApplied(final Double commissionApplied) {
        this.commissionApplied = commissionApplied;
    }

    @Nullable
    public synchronized List<RunnerProfitAndLoss> getProfitAndLosses() {
        return this.profitAndLosses == null ? null : new ArrayList<>(this.profitAndLosses);
    }

    public synchronized void setProfitAndLosses(final List<? extends RunnerProfitAndLoss> profitAndLosses) {
        this.profitAndLosses = profitAndLosses == null ? null : new ArrayList<>(profitAndLosses);
    }
}
