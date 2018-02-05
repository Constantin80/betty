package info.fmro.betty.entities;

public class VenueResult {

    private String venue;
    private Integer marketCount;

    public VenueResult() {
    }

    public synchronized String getVenue() {
        return venue;
    }

    public synchronized void setVenue(String venue) {
        this.venue = venue;
    }

    public synchronized Integer getMarketCount() {
        return marketCount;
    }

    public synchronized void setMarketCount(Integer marketCount) {
        this.marketCount = marketCount;
    }
}
