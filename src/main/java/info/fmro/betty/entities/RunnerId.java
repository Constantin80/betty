package info.fmro.betty.entities;

import java.util.Objects;

public class RunnerId {
    private String marketId; // The id of the market bet on
    private Long selectionId; // The id of the selection bet on
    private Double handicap; // The handicap associated with the runner in case of asian handicap markets, otherwise returns '0.0'.

    public RunnerId() {
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized void setMarketId(final String marketId) {
        this.marketId = marketId;
    }

    public synchronized Long getSelectionId() {
        return selectionId;
    }

    public synchronized void setSelectionId(final Long selectionId) {
        this.selectionId = selectionId;
    }

    public synchronized Double getHandicap() {
        return handicap;
    }

    public synchronized void setHandicap(final Double handicap) {
        this.handicap = handicap;
    }

    @Override
    public synchronized boolean equals(final Object o) {
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
    public synchronized int hashCode() {
        return Objects.hash(marketId, selectionId, handicap);
    }
}
