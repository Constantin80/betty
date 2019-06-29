package info.fmro.betty.stream.cache.util;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.objects.TwoDoubles;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

public class PriceSizeLadder
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(PriceSizeLadder.class);
    private static final long serialVersionUID = -5267061740378308330L;
    private final TreeMap<Double, PriceSize> priceToSize;

    private PriceSizeLadder(final Comparator<? super Double> comparator) {
        if (!comparator.equals(Comparator.reverseOrder()) && !comparator.equals(Comparator.naturalOrder())) {
            logger.error("not supported comparator type in PriceSizeLadder; this check is included to make sure the used comparator is serializable: {}", comparator.toString());
        }
        priceToSize = new TreeMap<>(comparator);
    }

    public static PriceSizeLadder newBack() {
        return new PriceSizeLadder(Comparator.reverseOrder());
    }

    public static PriceSizeLadder newLay() {
        return new PriceSizeLadder(Comparator.naturalOrder());
    }

    public synchronized PriceSizeLadder copy() {
        final PriceSizeLadder result;
        Comparator<? super Double> comparator = this.priceToSize.comparator();
        if (comparator == null) {
            logger.error("null comparator in PriceSizeLadder.copy for: {}", Generic.objectToString(this));
            comparator = Comparator.naturalOrder();
        } else { // normal case, everything is fine, nothing to be done
        }
        result = new PriceSizeLadder(comparator);
        result.updateTreeMap(this.priceToSize);

        return result;
    }

    public synchronized TreeMap<Double, Double> getSimpleTreeMap() {
        final TreeMap<Double, Double> result = new TreeMap<>(this.priceToSize.comparator());

        for (final PriceSize priceSize : this.priceToSize.values()) {
            if (priceSize == null) {
                logger.error("null priceSize in getSimpleTreeMap for: {}", Generic.objectToString(this));
            } else {
                result.put(priceSize.getPrice(), priceSize.getSizeEUR());
            }
        }

        return result;
    }

    public synchronized double getMatchedSize(final double price) {
        final double matchedSize;

        if (this.priceToSize != null) {
            if (this.priceToSize.containsKey(price)) {
                final PriceSize priceSize = this.priceToSize.get(price);
                if (priceSize != null) {
                    final Double foundSize = priceSize.getSizeEUR();
                    if (foundSize != null) {
                        matchedSize = foundSize;
                    } else {
                        logger.error("foundSize null in getMatchedSize for: {} {} {}", price, Generic.objectToString(priceSize), Generic.objectToString(this));
                        matchedSize = 0d;
                    }
                } else {
                    logger.error("priceSize null in getMatchedSize for: {} {}", price, Generic.objectToString(this));
                    matchedSize = 0d;
                }
            } else { // normal case, proper price not found
                matchedSize = 0d;
            }
        } else {
            logger.error("null priceToSize in getMatchedSize for: {} {}", price, Generic.objectToString(this));
            matchedSize = 0d;
        }

        return matchedSize;
    }

    private synchronized void updateTreeMap(final TreeMap<Double, PriceSize> map) {
        this.priceToSize.putAll(map);
    }

    public synchronized void onPriceChange(final boolean isImage, final List<List<Double>> prices) {
        if (isImage) {
            priceToSize.clear();
        }
        if (prices != null) {
            for (List<Double> price : prices) {
                final PriceSize priceSize = new PriceSize(price);
                if (priceSize.getSizeEUR() == 0.0d) {
                    priceToSize.remove(priceSize.getPrice());
                } else {
                    priceToSize.put(priceSize.getPrice(), priceSize);
                }
            }
        }
    }

    public synchronized double getBestPrice(final double calculatedLimit) {
        double result = 0d;
        if (priceToSize == null) {
            logger.error("null priceToSize in getBestPrice for: {}", Generic.objectToString(this));
            result = 0d;
        } else if (priceToSize.isEmpty()) {
            result = 0d;
        } else {
            final double minimumAmountConsideredSignificant = Math.min(calculatedLimit * .05d, 10d); // these defaults are rather basic
            for (PriceSize priceSize : this.priceToSize.values()) {
                if (priceSize == null) {
                    logger.error("null priceSize in getBestPrice {} for: {}", calculatedLimit, Generic.objectToString(this));
                } else {
                    final Double size = priceSize.getSizeEUR();
                    if (size != null && size >= minimumAmountConsideredSignificant) {
                        final Double price = priceSize.getPrice();
                        result = price == null ? 0d : price;
                        break;
                    }
                }
            }
//            final Double firstKey = priceToSize.firstKey();
//            result = firstKey == null ? 0d : firstKey;
        }
        return result;
    }

    public synchronized TwoDoubles getBackProfitExposurePair() { // this works for back; for lay profit and exposure are reversed
        double profit = 0d, exposure = 0d;
        for (PriceSize priceSize : priceToSize.values()) {
            final TwoDoubles twoDoubles = priceSize.getBackProfitExposurePair();
            profit += twoDoubles.getFirstDouble();
            exposure += twoDoubles.getSecondDouble();
        }

        return new TwoDoubles(profit, exposure);
    }

    public synchronized void removeAmountEUR(final Double price, final double sizeToRemove) {
        final double sizeToRemoveGBP = sizeToRemove / Statics.safetyLimits.currencyRate.get();
        removeAmountGBP(price, sizeToRemoveGBP);
    }

    private synchronized void removeAmountGBP(final Double price, final double sizeToRemove) {
        if (this.priceToSize.containsKey(price)) {
            final PriceSize priceSize = this.priceToSize.get(price);
            if (priceSize == null) {
                logger.error("null priceSize for price {} sizeToRemove {} in PriceSizeLadder.removeAmount for: {}", price, sizeToRemove, Generic.objectToString(this));
            } else {
                priceSize.removeAmountGBP(sizeToRemove);
            }
        } else {
            logger.error("price {} sizeToRemove {} not found in PriceSizeLadder.removeAmount for: {}", price, sizeToRemove, Generic.objectToString(this));
        }
    }

    @Override
    public synchronized String toString() {
        return "{" + priceToSize.values() + '}';
    }
}
