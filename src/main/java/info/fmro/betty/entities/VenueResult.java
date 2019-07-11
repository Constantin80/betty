package info.fmro.betty.entities;

@SuppressWarnings("unused")
class VenueResult {
    private String venue;
    private Integer marketCount;

    public synchronized String getVenue() {
        return this.venue;
    }

    public synchronized void setVenue(final String venue) {
        this.venue = venue;
    }

    public synchronized Integer getMarketCount() {
        return this.marketCount;
    }

    public synchronized void setMarketCount(final Integer marketCount) {
        this.marketCount = marketCount;
    }
}
