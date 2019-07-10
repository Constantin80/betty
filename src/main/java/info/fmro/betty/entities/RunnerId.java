package info.fmro.betty.entities;

import org.jetbrains.annotations.Contract;

import java.util.Objects;

public class RunnerId {
    @SuppressWarnings("unused")
    private String marketId; // The id of the market bet on
    @SuppressWarnings("unused")
    private Long selectionId; // The id of the selection bet on
    @SuppressWarnings("unused")
    private Double handicap; // The handicap associated with the runner in case of asian handicap markets, otherwise returns '0.0'.

    public synchronized String getMarketId() {
        return this.marketId;
    }

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

    public synchronized Double getHandicap() {
        return this.handicap;
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final RunnerId runnerId = (RunnerId) obj;
        return Objects.equals(this.marketId, runnerId.marketId) &&
               Objects.equals(this.selectionId, runnerId.selectionId) &&
               Objects.equals(this.handicap, runnerId.handicap);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.marketId, this.selectionId, this.handicap);
    }
}
