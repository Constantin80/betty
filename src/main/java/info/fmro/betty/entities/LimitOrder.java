package info.fmro.betty.entities;

import info.fmro.betty.enums.PersistenceType;
import info.fmro.betty.enums.Side;
import info.fmro.shared.utility.Generic;
import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitOrder
        implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(LimitOrder.class);
    private static final long serialVersionUID = 4803372723542992243L;
    private static final DecimalFormat decimalFormat = new DecimalFormat("#0.0#");
    private static final RoundingMode roundingMode = RoundingMode.DOWN;
    private String size; // String instead of Double for 2 digit formatting
    private Double price;
    private PersistenceType persistenceType;

    public LimitOrder() {
    }

    static {
        decimalFormat.setRoundingMode(roundingMode);
    }

    public synchronized double getLiability(Side side, boolean isEachWayMarket) { // assumes the worst case
        final double liability;
        final Double sizeObject = this.getSize();
        double primitiveSize = sizeObject == null ? 0d : sizeObject;
        if (isEachWayMarket) {
            primitiveSize *= 2d;
        }
        final double primitivePrice;
        if (this.price == null) {
            logger.error("null price in LimitOrder during getLibility {} for: {}", side, Generic.objectToString(this));
            primitivePrice = 1000d; // assumes worst case
        } else {
            primitivePrice = this.price;
        }

        if (side == null) {
            logger.error("side null in LimitOrder.getLiability: {}", Generic.objectToString(this));
            liability = Math.max(primitiveSize, primitiveSize * (primitivePrice - 1d)); // assume the worst
        } else if (side.equals(Side.BACK)) {
            liability = primitiveSize;
        } else if (side.equals(Side.LAY)) {
            liability = primitiveSize * (primitivePrice - 1d);
        } else { // unsupported Side
            liability = Math.max(primitiveSize, primitiveSize * (primitivePrice - 1d)); // assume the worst
            logger.error("unsupported side {} in LimitOrder.getLiability for: {}", side, Generic.objectToString(this));
        }

        return liability;
    }

    public synchronized Double getSize() {
        Double returnValue;

        if (this.size == null) {
            returnValue = null;
        } else {
            try {
                returnValue = Double.valueOf(this.size);
            } catch (NumberFormatException numberFormatException) {
                returnValue = null;
                logger.error("NumberFormatException in LimitOrder.getSize for: {}", this.size, numberFormatException);
            }
        }

        return returnValue;
    }

    public synchronized void setSize(Double size) {
        if (size != null) {
            try {
                this.size = decimalFormat.format(size);
                // this.size = Math.floor(size * 100) / 100; // this is sometimes not exact
            } catch (IllegalArgumentException illegalArgumentException) {
                this.size = null;
                logger.error("illegalArgumentException in LimitOrder.setSize for: {}", size, illegalArgumentException);
            }
        } else {
            // logger.error("tried to set null size in LimitOrder: {} {} {}", size, this.size, Generic.objectToString(this));
            this.size = null;
        }
    }

    public synchronized String getSizeString() {
        return size;
    }

    public synchronized void setSizeString(String size) {
        this.size = size;
    }

    public synchronized Double getPrice() { // get cents
        return price;
    }

    public synchronized void setPrice(Double price) {
        this.price = price;
    }

    public synchronized PersistenceType getPersistenceType() {
        return persistenceType;
    }

    public synchronized void setPersistenceType(PersistenceType persistenceType) {
        this.persistenceType = persistenceType;
    }
}
