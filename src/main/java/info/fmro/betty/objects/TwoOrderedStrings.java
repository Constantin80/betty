package info.fmro.betty.objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class TwoOrderedStrings
        implements Comparable<TwoOrderedStrings> {
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
        return this.first;
    }

    public synchronized String getSecond() {
        return this.second;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public synchronized int compareTo(@NotNull final TwoOrderedStrings o) {
        //noinspection ConstantConditions
        if (o == null) {
            return AFTER;
        }
        if (this == o) {
            return EQUAL;
        }

        if (this.getClass() != o.getClass()) {
            return this.getClass().hashCode() < o.getClass().hashCode() ? BEFORE : AFTER;
        }
        if (!Objects.equals(this.first, o.first)) {
            if (this.first == null) {
                return BEFORE;
            }
            if (o.first == null) {
                return AFTER;
            }
            return this.first.compareTo(o.first);
        }
        if (!Objects.equals(this.second, o.second)) {
            if (this.second == null) {
                return BEFORE;
            }
            if (o.second == null) {
                return AFTER;
            }
            return this.second.compareTo(o.second);
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

    @Contract(value = "null -> false", pure = true)
    @Override
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
