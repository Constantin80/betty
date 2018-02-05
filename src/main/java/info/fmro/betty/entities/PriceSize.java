package info.fmro.betty.entities;

import java.io.Serializable;
import java.util.Objects;

public class PriceSize
        implements Serializable {

    private static final long serialVersionUID = 6795917492745798841L;
    private Double price;
    private Double size;

    public PriceSize() {
    }

    public PriceSize(Double price, Double size) {
        this.price = price;
        this.size = size;
    }

    public synchronized Double getPrice() {
        return price;
    }

//    public synchronized void setPrice(Double price) {
//        this.price = price;
//    }
    public synchronized Double getSize() {
        return size;
    }

//    public synchronized void setSize(Double size) {
//        this.size = size;
//    }
    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.price);
        hash = 61 * hash + Objects.hashCode(this.size);
        return hash;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PriceSize other = (PriceSize) obj;
        if (!Objects.equals(this.price, other.price)) {
            return false;
        }
        return Objects.equals(this.size, other.size);
    }
}
