package info.fmro.betty.stream.cache.util;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

class LevelPriceSize
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(LevelPriceSize.class);
    private static final long serialVersionUID = 996457594745721075L;
    private final int level;
    private final double price;
    private final double size;

    LevelPriceSize(final List<Double> levelPriceSize) {
        if (levelPriceSize != null) {
            final int listSize = levelPriceSize.size();
            if (listSize == 3) {
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
                logger.error("wrong size {} for levelPriceSize list in LevelPriceSize object creation: {}", listSize, Generic.objectToString(levelPriceSize));
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

    synchronized int getLevel() {
        return this.level;
    }

    public synchronized double getPrice() {
        return this.price;
    }

    @Contract(pure = true)
    private synchronized double getSize() {
        return this.size;
    }

    public synchronized double getSizeEUR() {
        return getSize() * Statics.safetyLimits.currencyRate.get();
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final LevelPriceSize that = (LevelPriceSize) obj;
        return this.level == that.level &&
               Double.compare(that.price, this.price) == 0 &&
               Double.compare(that.size, this.size) == 0;
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.level, this.price, this.size);
    }

    @Override
    public synchronized String toString() {
        return this.level + ":" + this.size + "@" + this.price;
    }
}
