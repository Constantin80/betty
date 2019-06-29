package info.fmro.betty.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

public class ComparatorMarketPrices
        implements Comparator<Double> {
    private static final Logger logger = LoggerFactory.getLogger(ComparatorMarketPrices.class);

    @Override
    public int compare(final Double left, final Double right) {
        final int result;
        if (left == right) {
            result = 0;
        } else if (left == null || left.isNaN()) {
            logger.error("bad left value in ComparatorMarketPrices: {} {}", left, right);
            result = 1;
        } else if (right == null || right.isNaN()) {
            logger.error("bad right value in ComparatorMarketPrices: {} {}", left, right);
            result = -1;
        } else if (left == 0d) {
            result = 1;
        } else if (right == 0d) {
            result = -1;
        } else if (left < 1.01d || right < 1.01d) {
            logger.error("value too small in ComparatorMarketPrices: {} {}", left, right);
            result = -Double.compare(left, right);
        } else {
            if (left > 1000d || right > 1000d) {
                logger.error("value too large in ComparatorMarketPrices: {} {}", left, right);
            }

            result = Double.compare(left, right);
        }

        return result;
    }
}
