package info.fmro.betty.entities;

import java.io.Serializable;

public class LimitOnCloseOrder
        implements Serializable {
    private static final long serialVersionUID = 6616031776518338843L;
    private Double liability;
    private Double price;

    public LimitOnCloseOrder() {
    }

    public synchronized Double getLiability() {
        return liability;
    }

    public synchronized void setLiability(final Double liability) {
        this.liability = liability;
    }

    public synchronized Double getPrice() {
        return price;
    }

    public synchronized void setPrice(final Double price) {
        this.price = price;
    }
}
