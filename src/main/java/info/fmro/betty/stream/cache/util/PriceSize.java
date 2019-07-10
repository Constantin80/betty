package info.fmro.betty.stream.cache.util;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.objects.TwoDoubles;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class PriceSize
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(PriceSize.class);
    private static final long serialVersionUID = 6795917492745798841L;
    private final double price;
    private double size; // info.fmro.betty.stream.cache.util.PriceSize has size in GBP

//    public PriceSize() {
//    }

//    public PriceSize(Double price, Double size) {
//        this.price = price;
//        this.size = size;
//    }

    public PriceSize(final List<Double> priceSize) {
        if (priceSize != null) {
            final int size = priceSize.size();
            if (size == 2) {
                final Double priceObject = priceSize.get(0), sizeObject = priceSize.get(1);
                if (priceObject == null || sizeObject == null) {
                    logger.error("null Double in priceSize list in PriceSize object creation: {} {} {}", priceObject, sizeObject, Generic.objectToString(priceSize));
                    this.price = 0d;
                    this.size = 0d;
                } else {
                    this.price = priceObject;
                    this.size = sizeObject;
                }
            } else {
                logger.error("wrong size {} for priceSize list in PriceSize object creation: {}", size, Generic.objectToString(priceSize));
                this.price = 0d;
                this.size = 0d;
            }
        } else {
            logger.error("null priceSize list in PriceSize object creation");
            this.price = 0d;
            this.size = 0d;
        }
    }

    public synchronized double getPrice() {
        return this.price;
    }

    private synchronized double getSize() {
        return this.size;
    }

    public synchronized double getSizeEUR() {
        return getSize() * Statics.safetyLimits.currencyRate.get();
    }

    public synchronized TwoDoubles getBackProfitExposurePair() { // this works for back; for lay profit and exposure are reversed
        final double profit, exposure;

        if (this.price == 0d || this.size == 0d) { // error message was probably printed during creation
            profit = 0d;
            exposure = 0d;
        } else if (this.price <= 1d) {
            logger.error("bogus price {} in PriceSize for: {}", this.price, Generic.objectToString(this));
            this.size = 0d;
            profit = 0d;
            exposure = 0d;
        } else {
            profit = Formulas.layExposure(this.price, this.size);
            exposure = this.size;
        }

        return new TwoDoubles(profit, exposure);
    }

    synchronized void removeAmountGBP(final double sizeToRemove) { // package private method
//        if (this.size == null) {
//            logger.error("null size in PriceSize for: {}", Generic.objectToString(this));
//        } else
        if (this.size < 0d) {
            logger.error("negative size {} in PriceSize for: {}", this.size, Generic.objectToString(this));
        } else if (sizeToRemove < 0d) {
            logger.error("negative sizeToRemove {} in PriceSize.removeAmount for: {}", sizeToRemove, Generic.objectToString(this));
        } else {
            this.size -= sizeToRemove;
            if (this.size < 0d) {
                this.size = 0d;
            } else { // new size is fine, nothing to be done
            }
        }
    }

    @Override
    public synchronized boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PriceSize priceSize = (PriceSize) o;
        return Double.compare(priceSize.price, this.price) == 0 &&
               Double.compare(priceSize.size, this.size) == 0;
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.price, this.size);
    }
}
