package info.fmro.betty.entities;

import java.util.Objects;

public class RunnerId {

    private String marketId; // MarketId alias String
    private Long selectionId; // SelectionId alias Long
    private Double handicap; // Handicap alias Double

    public RunnerId() {
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized void setMarketId(String marketId) {
        this.marketId = marketId;
    }

    public synchronized Long getSelectionId() {
        return selectionId;
    }

    public synchronized void setSelectionId(Long selectionId) {
        this.selectionId = selectionId;
    }

    public synchronized Double getHandicap() {
        return handicap;
    }

    public synchronized void setHandicap(Double handicap) {
        this.handicap = handicap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RunnerId runnerId = (RunnerId) o;
        return Objects.equals(marketId, runnerId.marketId) &&
               Objects.equals(selectionId, runnerId.selectionId) &&
               Objects.equals(handicap, runnerId.handicap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(marketId, selectionId, handicap);
    }
}
