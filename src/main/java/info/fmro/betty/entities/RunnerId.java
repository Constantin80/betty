package info.fmro.betty.entities;

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
}
