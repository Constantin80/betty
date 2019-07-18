package info.fmro.betty.safebet;

import info.fmro.betty.enums.ParsedMarketType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;

public class ParsedMarket
        implements Serializable, Comparable<ParsedMarket> {
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = -2616558897137266131L;
    private final String marketId;
    private final ParsedMarketType parsedMarketType;
    private final HashSet<ParsedRunner> parsedRunnersSet; // the only place where ParsedRunners are stored

    public ParsedMarket(final String marketId, final ParsedMarketType parsedMarketType, final HashSet<ParsedRunner> parsedRunnersSet) {
        this.marketId = marketId;
        this.parsedMarketType = parsedMarketType;
        this.parsedRunnersSet = new HashSet<>(parsedRunnersSet);
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    public synchronized ParsedMarketType getParsedMarketType() {
        return this.parsedMarketType;
    }

    public synchronized HashSet<ParsedRunner> getParsedRunnersSet() {
        return new HashSet<>(this.parsedRunnersSet);
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public synchronized int compareTo(@NotNull final ParsedMarket o) {
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
        if (!Objects.equals(this.marketId, o.marketId)) {
            if (this.marketId == null) {
                return BEFORE;
            }
            if (o.marketId == null) {
                return AFTER;
            }
            return this.marketId.compareTo(o.marketId);
        }
        if (this.parsedMarketType != o.parsedMarketType) {
            if (this.parsedMarketType == null) {
                return BEFORE;
            }
            if (o.parsedMarketType == null) {
                return AFTER;
            }
            return this.parsedMarketType.compareTo(o.parsedMarketType);
        }
        if (!Objects.equals(this.parsedRunnersSet, o.parsedRunnersSet)) {
            //noinspection ConstantConditions
            if (this.parsedRunnersSet == null) {
                return BEFORE;
            }
            //noinspection ConstantConditions
            if (o.parsedRunnersSet == null) {
                return AFTER;
            }

            return BEFORE; // very primitive, hopefully it won't cause bugs; implementing compareTo for sets is not easy and it would likely consume too much resources
        }

        return EQUAL;
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
        final ParsedMarket other = (ParsedMarket) obj;
        if (!Objects.equals(this.marketId, other.marketId)) {
            return false;
        }
        if (this.parsedMarketType != other.parsedMarketType) {
            return false;
        }
        return Objects.equals(this.parsedRunnersSet, other.parsedRunnersSet);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.marketId);
        hash = 97 * hash + Objects.hashCode(this.parsedMarketType);
        hash = 97 * hash + Objects.hashCode(this.parsedRunnersSet);
        return hash;
    }
}
