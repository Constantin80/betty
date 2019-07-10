package info.fmro.betty.entities;

@SuppressWarnings("unused")
public class CountryCodeResult {
    private String countryCode;
    private Integer marketCount;

    public synchronized String getCountryCode() {
        return this.countryCode;
    }

    public synchronized void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    public synchronized Integer getMarketCount() {
        return this.marketCount;
    }

    public synchronized void setMarketCount(final Integer marketCount) {
        this.marketCount = marketCount;
    }
}
