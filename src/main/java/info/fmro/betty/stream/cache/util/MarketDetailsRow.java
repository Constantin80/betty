package info.fmro.betty.stream.cache.util;

public class MarketDetailsRow {
    private String marketId;
    private long selectionId;
    private double batbPrice;
    private double batbSize;
    private double batlPrice;
    private double batlSize;

    public MarketDetailsRow(String marketId, long selectionId, double batbPrice, double batbSize, double batlPrice, double batlSize) {
        this.marketId = marketId;
        this.selectionId = selectionId;
        this.batbPrice = batbPrice;
        this.batbSize = batbSize;
        this.batlPrice = batlPrice;
        this.batlSize = batlSize;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized long getSelectionId() {
        return selectionId;
    }

    public synchronized double getBatbPrice() {
        return batbPrice;
    }

    public synchronized double getBatbSize() {
        return batbSize;
    }

    public synchronized double getBatlPrice() {
        return batlPrice;
    }

    public synchronized double getBatlSize() {
        return batlSize;
    }
}
