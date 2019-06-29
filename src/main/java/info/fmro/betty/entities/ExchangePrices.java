package info.fmro.betty.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExchangePrices
        implements Serializable {
    private static final long serialVersionUID = -5031011635000589001L;
    private ArrayList<PriceSize> availableToBack;
    private ArrayList<PriceSize> availableToLay;
    private ArrayList<PriceSize> tradedVolume;

    public ExchangePrices() {
    }

    public ExchangePrices(final List<PriceSize> availableToBack, final List<PriceSize> availableToLay, final List<PriceSize> tradedVolume) {
        this.availableToBack = new ArrayList<>(availableToBack);
        this.availableToLay = new ArrayList<>(availableToLay);
        this.tradedVolume = new ArrayList<>(tradedVolume);
    }

    public synchronized List<PriceSize> getAvailableToBack() {
        return availableToBack == null ? null : new ArrayList<>(availableToBack);
    }

    //    public synchronized void setAvailableToBack(List<PriceSize> availableToBack) {
//        this.availableToBack = availableToBack == null ? null : new ArrayList<>(availableToBack);
//    }
    public synchronized List<PriceSize> getAvailableToLay() {
        return availableToLay == null ? null : new ArrayList<>(availableToLay);
    }

    //    public synchronized void setAvailableToLay(List<PriceSize> availableToLay) {
//        this.availableToLay = availableToLay == null ? null : new ArrayList<>(availableToLay);
//    }
    public synchronized List<PriceSize> getTradedVolume() {
        return tradedVolume == null ? null : new ArrayList<>(tradedVolume);
    }

    //    public synchronized void setTradedVolume(List<PriceSize> tradedVolume) {
//        this.tradedVolume = tradedVolume == null ? null : new ArrayList<>(tradedVolume);
//    }
    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.availableToBack);
        hash = 37 * hash + Objects.hashCode(this.availableToLay);
        hash = 37 * hash + Objects.hashCode(this.tradedVolume);
        return hash;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ExchangePrices other = (ExchangePrices) obj;
        if (!Objects.equals(this.availableToBack, other.availableToBack)) {
            return false;
        }
        if (!Objects.equals(this.availableToLay, other.availableToLay)) {
            return false;
        }
        return Objects.equals(this.tradedVolume, other.tradedVolume);
    }
}
