package info.fmro.betty.entities;

import java.io.Serializable;

public class CurrencyRate
        implements Serializable {
    private static final long serialVersionUID = 9014962168930839468L;
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
