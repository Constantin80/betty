package info.fmro.betty.utility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Comparator;

public class ComparatorMarketPrices
        implements Comparator<Double>, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ComparatorMarketPrices.class);
    private static final long serialVersionUID = 3658851992618501057L;

    @Override
    public synchronized int compare(final Double o1, final Double o2) {
        final int result;
        //noinspection NumberEquality
        if (o1 == o2) {
            result = 0;
        } else if (o1 == null || o1.isNaN()) {
            logger.error("bad left value in ComparatorMarketPrices: {} {}", o1, o2);
            result = 1;
        } else if (o2 == null || o2.isNaN()) {
            logger.error("bad right value in ComparatorMarketPrices: {} {}", o1, o2);
            result = -1;
        } else if (o1 == 0d) {
            result = 1;
        } else if (o2 == 0d) {
            result = -1;
        } else if (o1 < 1.01d || o2 < 1.01d) {
            logger.error("value too small in ComparatorMarketPrices: {} {}", o1, o2);
            result = -Double.compare(o1, o2);
        } else {
            if (o1 > 1_000d || o2 > 1_000d) {
                logger.error("value too large in ComparatorMarketPrices: {} {}", o1, o2);
            }
            result = Double.compare(o1, o2);
        }

        return result;
    }
}
