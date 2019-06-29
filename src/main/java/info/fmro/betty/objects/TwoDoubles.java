package info.fmro.betty.objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class TwoDoubles
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(TwoDoubles.class);
    private final double firstDouble, secondDouble;

    public TwoDoubles(final double firstDouble, final double secondDouble) {
        this.firstDouble = firstDouble;
        this.secondDouble = secondDouble;
    }

    public synchronized double getFirstDouble() {
        return firstDouble;
    }

    public synchronized double getSecondDouble() {
        return secondDouble;
    }

    @Override
    public synchronized boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final TwoDoubles that = (TwoDoubles) o;
        return Double.compare(that.firstDouble, firstDouble) == 0 &&
               Double.compare(that.secondDouble, secondDouble) == 0;
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(firstDouble, secondDouble);
    }
}
