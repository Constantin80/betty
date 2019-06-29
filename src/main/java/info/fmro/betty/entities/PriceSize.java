package info.fmro.betty.entities;

import java.io.Serializable;
import java.util.Objects;

public class PriceSize
        implements Serializable {
    private static final long serialVersionUID = 6795917492745798841L;
    private Double price;
    private Double size; // info.fmro.betty.entities.PriceSize has size in EUR

    public PriceSize() {
    }

    public synchronized Double getPrice() {
        return price;
    }

    public synchronized Double getSize() {
        return size;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PriceSize priceSize = (PriceSize) o;
        return Objects.equals(price, priceSize.price) &&
               Objects.equals(size, priceSize.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(price, size);
    }
}
