package info.fmro.betty.stream.cache.util;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class LevelPriceSize
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(LevelPriceSize.class);
    private static final long serialVersionUID = 996457594745721075L;
    private final int level;
    private final double price;
    private final double size;

    public LevelPriceSize(final List<Double> levelPriceSize) {
        if (levelPriceSize != null) {
            final int size = levelPriceSize.size();
            if (size == 3) {
                final Double levelObject = levelPriceSize.get(0), priceObject = levelPriceSize.get(1), sizeObject = levelPriceSize.get(2);
                if (levelObject == null || priceObject == null || sizeObject == null) {
                    logger.error("null Double in levelPriceSize list in LevelPriceSize object creation: {} {} {} {}", levelObject, priceObject, sizeObject, Generic.objectToString(levelPriceSize));
                    this.level = 0;
                    this.price = 0d;
                    this.size = 0d;
                } else {
                    this.level = levelObject.intValue();
                    this.price = priceObject;
                    this.size = sizeObject;
                }
            } else {
                logger.error("wrong size {} for levelPriceSize list in LevelPriceSize object creation: {}", size, Generic.objectToString(levelPriceSize));
                this.level = 0;
                this.price = 0d;
                this.size = 0d;
            }
        } else {
            logger.error("null levelPriceSize list in LevelPriceSize object creation");
            this.level = 0;
            this.price = 0d;
            this.size = 0d;
        }
    }

//    public LevelPriceSize(int level, double price, double size) {
//        this.level = level;
//        this.price = price;
//        this.size = size;
//    }

    public synchronized int getLevel() {
        return level;
    }

    public synchronized double getPrice() {
        return price;
    }

    private synchronized double getSize() {
        return size;
    }

    public synchronized double getSizeEUR() {
        return getSize() * Statics.safetyLimits.currencyRate.get();
    }

    @Override
    public synchronized boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LevelPriceSize that = (LevelPriceSize) o;
        return level == that.level &&
               Double.compare(that.price, price) == 0 &&
               Double.compare(that.size, size) == 0;
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(level, price, size);
    }

    @Override
    public synchronized String toString() {
        return level + ":" + size + "@" + price;
    }
}
