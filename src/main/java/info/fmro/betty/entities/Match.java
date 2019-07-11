package info.fmro.betty.entities;

import info.fmro.betty.enums.Side;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

class Match
        implements Serializable {
    private static final long serialVersionUID = 5267424955233956792L;
    private String betId;
    private String matchId;
    private Side side;
    private Double price;
    private Double size;
    private Date matchDate;

    @SuppressWarnings("unused")
    @Contract(pure = true)
    private Match() {
    }

    @SuppressWarnings({"ConstructorWithTooManyParameters", "unused"})
    @Contract(pure = true)
    Match(final String betId, final String matchId, final Side side, final Double price, final Double size, @NotNull final Date matchDate) {
        this.betId = betId;
        this.matchId = matchId;
        this.side = side;
        this.price = price;
        this.size = size;
        this.matchDate = (Date) matchDate.clone();
    }

    public synchronized String getBetId() {
        return this.betId;
    }

    public synchronized String getMatchId() {
        return this.matchId;
    }

    public synchronized Side getSide() {
        return this.side;
    }

    public synchronized Double getPrice() {
        return this.price;
    }

    public synchronized Double getSize() {
        return this.size;
    }

    @Nullable
    public synchronized Date getMatchDate() {
        return this.matchDate == null ? null : (Date) this.matchDate.clone();
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
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
        final Match other = (Match) obj;
        if (!Objects.equals(this.betId, other.betId)) {
            return false;
        }
        if (!Objects.equals(this.matchId, other.matchId)) {
            return false;
        }
        if (this.side != other.side) {
            return false;
        }
        if (!Objects.equals(this.price, other.price)) {
            return false;
        }
        if (!Objects.equals(this.size, other.size)) {
            return false;
        }
        return Objects.equals(this.matchDate, other.matchDate);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public synchronized int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.betId);
        hash = 97 * hash + Objects.hashCode(this.matchId);
        hash = 97 * hash + Objects.hashCode(this.side);
        hash = 97 * hash + Objects.hashCode(this.price);
        hash = 97 * hash + Objects.hashCode(this.size);
        hash = 97 * hash + Objects.hashCode(this.matchDate);
        return hash;
    }
}
