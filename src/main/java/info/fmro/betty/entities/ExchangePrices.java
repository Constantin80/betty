package info.fmro.betty.entities;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExchangePrices
        implements Serializable {
    private static final long serialVersionUID = -5031011635000589001L;
    private final ArrayList<PriceSize> availableToBack;
    private final ArrayList<PriceSize> availableToLay;
    private final ArrayList<PriceSize> tradedVolume;

    public ExchangePrices(@NotNull final List<? extends PriceSize> availableToBack, @NotNull final List<? extends PriceSize> availableToLay, @NotNull final List<? extends PriceSize> tradedVolume) {
        this.availableToBack = new ArrayList<>(availableToBack);
        this.availableToLay = new ArrayList<>(availableToLay);
        this.tradedVolume = new ArrayList<>(tradedVolume);
    }

    public synchronized List<PriceSize> getAvailableToBack() {
        return new ArrayList<>(this.availableToBack);
    }

    public synchronized List<PriceSize> getAvailableToLay() {
        return new ArrayList<>(this.availableToLay);
    }

    public synchronized List<PriceSize> getTradedVolume() {
        return new ArrayList<>(this.tradedVolume);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.availableToBack);
        hash = 37 * hash + Objects.hashCode(this.availableToLay);
        hash = 37 * hash + Objects.hashCode(this.tradedVolume);
        return hash;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
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
