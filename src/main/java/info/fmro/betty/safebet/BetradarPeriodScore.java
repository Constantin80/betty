package info.fmro.betty.safebet;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

class BetradarPeriodScore
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 9030682989422139021L;
    private final String periodName;
    private final int homeScore, awayScore;

    @SuppressWarnings("unused")
    BetradarPeriodScore(final String periodName, final int homeScore, final int awayScore) {
        this.periodName = periodName;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
    }

    public String getPeriodName() {
        return this.periodName;
    }

    public int getHomeScore() {
        return this.homeScore;
    }

    public int getAwayScore() {
        return this.awayScore;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final BetradarPeriodScore that = (BetradarPeriodScore) obj;
        return this.homeScore == that.homeScore &&
               this.awayScore == that.awayScore &&
               Objects.equals(this.periodName, that.periodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.periodName, this.homeScore, this.awayScore);
    }
}
