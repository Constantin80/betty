package info.fmro.betty.objects;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;

public class SafeMarket
        implements Serializable, Comparable<SafeMarket> {

    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = -2040357407452705786L;
    private final String marketId; // market that contains the runner
    private HashSet<SafeRunner> safeRunnersSet = new HashSet<>(2, 0.75f);

    public SafeMarket(String marketId) {
        this.marketId = marketId;
    }

    public SafeMarket(String marketId, HashSet<SafeRunner> safeRunnersSet) {
        this.marketId = marketId;
        this.safeRunnersSet = safeRunnersSet;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized HashSet<SafeRunner> getSafeRunnersSet() {
        return safeRunnersSet == null ? null : new HashSet<>(safeRunnersSet);
    }

    public synchronized void setSafeRunnersSet(HashSet<SafeRunner> safeRunnersSet) {
        this.safeRunnersSet = safeRunnersSet == null ? null : new HashSet<>(safeRunnersSet);
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(SafeMarket other) {
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
        if (!Objects.equals(this.marketId, other.marketId)) {
            if (this.marketId == null) {
                return BEFORE;
            }
            if (other.marketId == null) {
                return AFTER;
            }
            return this.marketId.compareTo(other.marketId);
        }

        return EQUAL;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SafeMarket other = (SafeMarket) obj;
        return Objects.equals(this.marketId, other.marketId);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.marketId);
        return hash;
    }
}
