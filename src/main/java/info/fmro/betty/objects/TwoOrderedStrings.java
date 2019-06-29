package info.fmro.betty.objects;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwoOrderedStrings
        implements Comparable<TwoOrderedStrings> {

    private static final Logger logger = LoggerFactory.getLogger(TwoOrderedStrings.class);
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private final String first, second;

    public TwoOrderedStrings(final String first, final String second) {
        if (first == null) {
            this.first = first;
            this.second = second;
        } else if (second == null) {
            this.first = second;
            this.second = first;
        } else if (first.compareTo(second) <= 0) {
            this.first = first;
            this.second = second;
        } else {
            this.first = second;
            this.second = first;
        }
    }

    public synchronized String getFirst() {
        return first;
    }

    public synchronized String getSecond() {
        return second;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(final TwoOrderedStrings other) {
        if (other == null) {
            return AFTER;
        }
        if (this == other) {
            return EQUAL;
        }

        if (this.getClass() != other.getClass()) {
            if (this.getClass().hashCode() < other.getClass().hashCode()) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (!Objects.equals(this.first, other.first)) {
            if (this.first == null) {
                return BEFORE;
            }
            if (other.first == null) {
                return AFTER;
            }
            return this.first.compareTo(other.first);
        }
        if (!Objects.equals(this.second, other.second)) {
            if (this.second == null) {
                return BEFORE;
            }
            if (other.second == null) {
                return AFTER;
            }
            return this.second.compareTo(other.second);
        }

        return EQUAL;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.first);
        hash = 17 * hash + Objects.hashCode(this.second);
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
        final TwoOrderedStrings other = (TwoOrderedStrings) obj;
        if (!Objects.equals(this.first, other.first)) {
            return false;
        }
        return Objects.equals(this.second, other.second);
    }
}
