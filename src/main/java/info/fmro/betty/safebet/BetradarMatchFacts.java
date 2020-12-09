package info.fmro.betty.safebet;

import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings("unused")
class BetradarMatchFacts
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 755062000734275494L;
    private final String homeTeam, matchStatus, awayTeam;
    private final int homeScore, awayScore;
    private final ArrayList<? extends BetradarPeriodScore> periodScoresList;
    private final ArrayList<? extends BetradarMatchEvent> homeGoalsList, awayGoalsList, homeYellowCardsList, homeRedCardsList, awayYellowCardsList, awayRedCardsList, homeSubstitutionsList, awaySubstitutionsList;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    BetradarMatchFacts(final String homeTeam, final String matchStatus, final String awayTeam, final int homeScore, final int awayScore, final ArrayList<? extends BetradarPeriodScore> periodScoresList,
                       final ArrayList<? extends BetradarMatchEvent> homeGoalsList, final ArrayList<? extends BetradarMatchEvent> awayGoalsList, final ArrayList<? extends BetradarMatchEvent> homeYellowCardsList,
                       final ArrayList<? extends BetradarMatchEvent> homeRedCardsList, final ArrayList<? extends BetradarMatchEvent> awayYellowCardsList, final ArrayList<? extends BetradarMatchEvent> awayRedCardsList,
                       final ArrayList<? extends BetradarMatchEvent> homeSubstitutionsList, final ArrayList<? extends BetradarMatchEvent> awaySubstitutionsList) {
        this.homeTeam = homeTeam;
        this.matchStatus = matchStatus;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.periodScoresList = new ArrayList<>(periodScoresList);
        this.homeGoalsList = new ArrayList<>(homeGoalsList);
        this.awayGoalsList = new ArrayList<>(awayGoalsList);
        this.homeYellowCardsList = new ArrayList<>(homeYellowCardsList);
        this.homeRedCardsList = new ArrayList<>(homeRedCardsList);
        this.awayYellowCardsList = new ArrayList<>(awayYellowCardsList);
        this.awayRedCardsList = new ArrayList<>(awayRedCardsList);
        this.homeSubstitutionsList = new ArrayList<>(homeSubstitutionsList);
        this.awaySubstitutionsList = new ArrayList<>(awaySubstitutionsList);
    }

    public String getHomeTeam() {
        return this.homeTeam;
    }

    public String getMatchStatus() {
        return this.matchStatus;
    }

    public String getAwayTeam() {
        return this.awayTeam;
    }

    public int getHomeScore() {
        return this.homeScore;
    }

    public int getAwayScore() {
        return this.awayScore;
    }

    @Nullable
    public synchronized ArrayList<BetradarPeriodScore> getPeriodScoresList() {
        return new ArrayList<>(this.periodScoresList);
    }

    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getHomeGoalsList() {
        return new ArrayList<>(this.homeGoalsList);
    }

    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getAwayGoalsList() {
        return new ArrayList<>(this.awayGoalsList);
    }

    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getHomeYellowCardsList() {
        return new ArrayList<>(this.homeYellowCardsList);
    }

    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getHomeRedCardsList() {
        return new ArrayList<>(this.homeRedCardsList);
    }

    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getAwayYellowCardsList() {
        return new ArrayList<>(this.awayYellowCardsList);
    }

    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getAwayRedCardsList() {
        return new ArrayList<>(this.awayRedCardsList);
    }

    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getHomeSubstitutionsList() {
        return new ArrayList<>(this.homeSubstitutionsList);
    }

    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getAwaySubstitutionsList() {
        return new ArrayList<>(this.awaySubstitutionsList);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final BetradarMatchFacts that = (BetradarMatchFacts) obj;
        return this.homeScore == that.homeScore &&
               this.awayScore == that.awayScore &&
               Objects.equals(this.homeTeam, that.homeTeam) &&
               Objects.equals(this.matchStatus, that.matchStatus) &&
               Objects.equals(this.awayTeam, that.awayTeam) &&
               Objects.equals(this.periodScoresList, that.periodScoresList) &&
               Objects.equals(this.homeGoalsList, that.homeGoalsList) &&
               Objects.equals(this.awayGoalsList, that.awayGoalsList) &&
               Objects.equals(this.homeYellowCardsList, that.homeYellowCardsList) &&
               Objects.equals(this.homeRedCardsList, that.homeRedCardsList) &&
               Objects.equals(this.awayYellowCardsList, that.awayYellowCardsList) &&
               Objects.equals(this.awayRedCardsList, that.awayRedCardsList) &&
               Objects.equals(this.homeSubstitutionsList, that.homeSubstitutionsList) &&
               Objects.equals(this.awaySubstitutionsList, that.awaySubstitutionsList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.homeTeam, this.matchStatus, this.awayTeam, this.homeScore, this.awayScore, this.periodScoresList, this.homeGoalsList, this.awayGoalsList, this.homeYellowCardsList, this.homeRedCardsList, this.awayYellowCardsList,
                            this.awayRedCardsList, this.homeSubstitutionsList, this.awaySubstitutionsList);
    }
}
