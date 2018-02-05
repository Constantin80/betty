package info.fmro.betty.entities;

public class CountryCodeResult {

    private String countryCode;
    private Integer marketCount;

    public CountryCodeResult() {
    }

    public synchronized String getCountryCode() {
        return countryCode;
    }

    public synchronized void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public synchronized Integer getMarketCount() {
        return marketCount;
    }

    public synchronized void setMarketCount(Integer marketCount) {
        this.marketCount = marketCount;
    }
}
