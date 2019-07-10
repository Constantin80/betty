package info.fmro.betty.entities;

import info.fmro.betty.enums.BetTargetType;
import info.fmro.betty.enums.PersistenceType;
import info.fmro.betty.enums.Side;
import info.fmro.betty.enums.TimeInForce;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class LimitOrder
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(LimitOrder.class);
    private static final long serialVersionUID = 4803372723542992243L;
    private static final DecimalFormat decimalFormat = new DecimalFormat("#0.0#");
    private static final RoundingMode roundingMode = RoundingMode.DOWN;
    @Nullable
    private String size; // String instead of Double for 2 digit formatting
    private Double price;
    private PersistenceType persistenceType;
    @SuppressWarnings("unused")
    private TimeInForce timeInForce;
    @SuppressWarnings("unused")
    private Double minFillSize;
    @SuppressWarnings("unused")
    private BetTargetType betTargetType;
    @SuppressWarnings("unused")
    private Double betTargetSize;

    static {
        decimalFormat.setRoundingMode(roundingMode);
    }

    synchronized double getLiability(final Side side, final boolean isEachWayMarket) { // assumes the worst case
        final double liability;
        final Double sizeObject = this.getSize();
        double primitiveSize = sizeObject == null ? 0d : sizeObject;
        if (isEachWayMarket) {
            primitiveSize *= 2d;
        }
        final double primitivePrice;
        if (this.price == null) {
            logger.error("null price in LimitOrder during getLiability {} for: {}", side, Generic.objectToString(this));
            primitivePrice = 1_000d; // assumes worst case
        } else {
            primitivePrice = this.price;
        }

        if (side == null) {
            logger.error("side null in LimitOrder.getLiability: {}", Generic.objectToString(this));
            liability = Math.max(primitiveSize, primitiveSize * (primitivePrice - 1d)); // assume the worst
        } else if (side == Side.BACK) {
            liability = primitiveSize;
        } else if (side == Side.LAY) {
            liability = Formulas.layExposure(primitivePrice, primitiveSize);
        } else { // unsupported Side
            liability = Math.max(primitiveSize, primitiveSize * (primitivePrice - 1d)); // assume the worst
            logger.error("unsupported side {} in LimitOrder.getLiability for: {}", side, Generic.objectToString(this));
        }

        return liability;
    }

    public synchronized Double getSize() {
        @Nullable Double returnValue;

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

    public synchronized void setSize(final Double size) {
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

    @SuppressWarnings("SuspiciousGetterSetter")
    public synchronized String getSizeString() {
        return this.size;
    }

    @SuppressWarnings("SuspiciousGetterSetter")
    public synchronized void setSizeString(final String size) {
        this.size = size;
    }

    public synchronized Double getPrice() { // get cents
        return this.price;
    }

    public synchronized void setPrice(final Double price) {
        this.price = price;
    }

    public synchronized PersistenceType getPersistenceType() {
        return this.persistenceType;
    }

    public synchronized void setPersistenceType(final PersistenceType persistenceType) {
        this.persistenceType = persistenceType;
    }
}
