package info.fmro.betty.objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StampedDouble { // access to the value is stamped

    private static final Logger logger = LoggerFactory.getLogger(StampedDouble.class);
    private final double value;
    private long stamp;

    public StampedDouble(final double value, final long stamp) {
        this.value = value;
        this.stamp = stamp;
    }

    public StampedDouble(final double value) {
        this(value, System.currentTimeMillis());
    }

    public synchronized double getValue() {
        this.stamp();
        return value;
    }

    public synchronized double getValue(final long stamp) {
        this.setStamp(stamp);
        return value;
    }

    public synchronized long getStamp() {
        return stamp;
    }

    public synchronized void setStamp(final long stamp) {
        if (stamp > this.stamp) {
            this.stamp = stamp;
        } else {
            final long difference = this.stamp - stamp;
            if (difference > 100L) {
                logger.error("StampedDouble attempt to stamp with older by {} ms: value:{} old:{} attempt:{}", difference, this.value, this.stamp, stamp);
            } else { // difference too small to matter, might happen during very heavy load
            }
        }
    }

    public synchronized void stamp() {
        this.setStamp(System.currentTimeMillis());
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 59 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        return hash;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StampedDouble other = (StampedDouble) obj;
        return Double.doubleToLongBits(this.value) == Double.doubleToLongBits(other.value);
    }
}
