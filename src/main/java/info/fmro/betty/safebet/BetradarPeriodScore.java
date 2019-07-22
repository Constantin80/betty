package info.fmro.betty.safebet;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.Objects;

class BetradarPeriodScore
        implements Serializable {
    private static final long serialVersionUID = 9030682989422139021L;
    private final String periodName;
    private final int homeScore, awayScore;

    @SuppressWarnings("unused")
    BetradarPeriodScore(final String periodName, final int homeScore, final int awayScore) {
        this.periodName = periodName;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public synchronized String getPeriodName() {
        return this.periodName;
    }

    public synchronized int getHomeScore() {
        return this.homeScore;
    }

    public synchronized int getAwayScore() {
        return this.awayScore;
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
        final BetradarPeriodScore other = (BetradarPeriodScore) obj;
        if (!Objects.equals(this.periodName, other.periodName)) {
            return false;
        }
        if (this.homeScore != other.homeScore) {
            return false;
        }
        return this.awayScore == other.awayScore;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 73 * hash + Objects.hashCode(this.periodName);
        hash = 73 * hash + this.homeScore;
        hash = 73 * hash + this.awayScore;
        return hash;
    }
}