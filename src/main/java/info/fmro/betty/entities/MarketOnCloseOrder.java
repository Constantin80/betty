package info.fmro.betty.entities;

import java.io.Serializable;

public class MarketOnCloseOrder
        implements Serializable {

    private static final long serialVersionUID = -8981250957935689408L;
    private Double liability;

    public MarketOnCloseOrder() {
    }

    public synchronized Double getLiability() {
        return liability;
    }

    public synchronized void setLiability(Double liability) {
        this.liability = liability;
    }
}
