package info.fmro.betty.entities;

import java.util.ArrayList;
import java.util.List;

public class MarketProfitAndLoss {
    private String marketId;
    private Double commissionApplied;
    private List<RunnerProfitAndLoss> profitAndLosses;

    public MarketProfitAndLoss() {
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized void setMarketId(final String marketId) {
        this.marketId = marketId;
    }

    public synchronized Double getCommissionApplied() {
        return commissionApplied;
    }

    public synchronized void setCommissionApplied(final Double commissionApplied) {
        this.commissionApplied = commissionApplied;
    }

    public synchronized List<RunnerProfitAndLoss> getProfitAndLosses() {
        return profitAndLosses == null ? null : new ArrayList<>(profitAndLosses);
    }

    public synchronized void setProfitAndLosses(final List<RunnerProfitAndLoss> profitAndLosses) {
        this.profitAndLosses = profitAndLosses == null ? null : new ArrayList<>(profitAndLosses);
    }
}
