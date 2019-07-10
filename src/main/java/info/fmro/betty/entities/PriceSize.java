package info.fmro.betty.entities;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.Objects;

public class PriceSize
        implements Serializable {
    private static final long serialVersionUID = 6795917492745798841L;
    @SuppressWarnings("unused")
    private Double price;
    @SuppressWarnings("unused")
    private Double size; // info.fmro.betty.entities.PriceSize has size in EUR

    public synchronized Double getPrice() {
        return this.price;
    }

    public synchronized Double getSize() {
        return this.size;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PriceSize priceSize = (PriceSize) obj;
        return Objects.equals(this.price, priceSize.price) &&
               Objects.equals(this.size, priceSize.size);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.price, this.size);
    }
}
