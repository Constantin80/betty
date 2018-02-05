package info.fmro.betty.entities;

public class CurrencyRate {

    private String currencyCode; // Three letter ISO 4217 code
    private Double rate; // Exchange rate for the currency specified in the request

    public CurrencyRate() {
    }

    public synchronized String getCurrencyCode() {
        return currencyCode;
    }

    public synchronized void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public synchronized Double getRate() {
        return rate;
    }

    public synchronized void setRate(Double rate) {
        this.rate = rate;
    }
}
