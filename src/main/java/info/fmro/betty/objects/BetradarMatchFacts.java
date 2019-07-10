package info.fmro.betty.objects;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

@SuppressWarnings({"ClassWithTooManyFields", "unused"})
public class BetradarMatchFacts
        implements Serializable {
    private static final long serialVersionUID = 755062000734275494L;
    private final String homeTeam, matchStatus, awayTeam;
    private final int homeScore, awayScore;
    private final ArrayList<? extends BetradarPeriodScore> periodScoresList;
    private final ArrayList<? extends BetradarMatchEvent> homeGoalsList, awayGoalsList, homeYellowCardsList, homeRedCardsList, awayYellowCardsList, awayRedCardsList, homeSubstitutionsList, awaySubstitutionsList;

    @SuppressWarnings("ConstructorWithTooManyParameters")
    public BetradarMatchFacts(final String homeTeam, final String matchStatus, final String awayTeam, final int homeScore, final int awayScore, final ArrayList<? extends BetradarPeriodScore> periodScoresList,
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

    public synchronized String getHomeTeam() {
        return this.homeTeam;
    }

    //    public synchronized void setHomeTeam(String homeTeam) {
//        this.homeTeam = homeTeam;
//    }
    public synchronized String getMatchStatus() {
        return this.matchStatus;
    }

    //    public synchronized void setMatchStatus(String matchStatus) {
//        this.matchStatus = matchStatus;
//    }
    public synchronized String getAwayTeam() {
        return this.awayTeam;
    }

    //    public synchronized void setAwayTeam(String awayTeam) {
//        this.awayTeam = awayTeam;
//    }
    public synchronized int getHomeScore() {
        return this.homeScore;
    }

    //    public synchronized void setHomeScore(int homeScore) {
//        this.homeScore = homeScore;
//    }
    public synchronized int getAwayScore() {
        return this.awayScore;
    }

    //    public synchronized void setAwayScore(int awayScore) {
//        this.awayScore = awayScore;
//    }
    @Nullable
    public synchronized ArrayList<BetradarPeriodScore> getPeriodScoresList() {
        return new ArrayList<>(this.periodScoresList);
    }

    //    public synchronized void setPeriodScoresList(ArrayList<BetradarPeriodScore> periodScoresList) {
//        this.periodScoresList = periodScoresList == null ? null : new ArrayList<>(periodScoresList);
//    }
    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getHomeGoalsList() {
        return new ArrayList<>(this.homeGoalsList);
    }

    //    public synchronized void setHomeGoalsList(ArrayList<BetradarMatchEvent> homeGoalsList) {
//        this.homeGoalsList = homeGoalsList == null ? null : new ArrayList<>(homeGoalsList);
//    }
    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getAwayGoalsList() {
        return new ArrayList<>(this.awayGoalsList);
    }

    //    public synchronized void setAwayGoalsList(ArrayList<BetradarMatchEvent> awayGoalsList) {
//        this.awayGoalsList = awayGoalsList == null ? null : new ArrayList<>(awayGoalsList);
//    }
    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getHomeYellowCardsList() {
        return new ArrayList<>(this.homeYellowCardsList);
    }

    //    public synchronized void setHomeYellowCardsList(ArrayList<BetradarMatchEvent> homeYellowCardsList) {
//        this.homeYellowCardsList = homeYellowCardsList == null ? null : new ArrayList<>(homeYellowCardsList);
//    }
    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getHomeRedCardsList() {
        return new ArrayList<>(this.homeRedCardsList);
    }

    //    public synchronized void setHomeRedCardsList(ArrayList<BetradarMatchEvent> homeRedCardsList) {
//        this.homeRedCardsList = homeRedCardsList == null ? null : new ArrayList<>(homeRedCardsList);
//    }
    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getAwayYellowCardsList() {
        return new ArrayList<>(this.awayYellowCardsList);
    }

    //    public synchronized void setAwayYellowCardsList(ArrayList<BetradarMatchEvent> awayYellowCardsList) {
//        this.awayYellowCardsList = awayYellowCardsList == null ? null : new ArrayList<>(awayYellowCardsList);
//    }
    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getAwayRedCardsList() {
        return new ArrayList<>(this.awayRedCardsList);
    }

    //    public synchronized void setAwayRedCardsList(ArrayList<BetradarMatchEvent> awayRedCardsList) {
//        this.awayRedCardsList = awayRedCardsList == null ? null : new ArrayList<>(awayRedCardsList);
//    }
    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getHomeSubstitutionsList() {
        return new ArrayList<>(this.homeSubstitutionsList);
    }

    //    public synchronized void setHomeSubstitutionsList(ArrayList<BetradarMatchEvent> homeSubstitutionsList) {
//        this.homeSubstitutionsList = homeSubstitutionsList == null ? null : new ArrayList<>(homeSubstitutionsList);
//    }
    @Nullable
    public synchronized ArrayList<BetradarMatchEvent> getAwaySubstitutionsList() {
        return new ArrayList<>(this.awaySubstitutionsList);
    }

    //    public synchronized void setAwaySubstitutionsList(ArrayList<BetradarMatchEvent> awaySubstitutionsList) {
//        this.awaySubstitutionsList = awaySubstitutionsList == null ? null : new ArrayList<>(awaySubstitutionsList);
//    }
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
        final BetradarMatchFacts other = (BetradarMatchFacts) obj;
        if (!Objects.equals(this.homeTeam, other.homeTeam)) {
            return false;
        }
        if (!Objects.equals(this.matchStatus, other.matchStatus)) {
            return false;
        }
        if (!Objects.equals(this.awayTeam, other.awayTeam)) {
            return false;
        }
        if (this.homeScore != other.homeScore) {
            return false;
        }
        if (this.awayScore != other.awayScore) {
            return false;
        }
        if (!Objects.equals(this.periodScoresList, other.periodScoresList)) {
            return false;
        }
        if (!Objects.equals(this.homeGoalsList, other.homeGoalsList)) {
            return false;
        }
        if (!Objects.equals(this.awayGoalsList, other.awayGoalsList)) {
            return false;
        }
        if (!Objects.equals(this.homeYellowCardsList, other.homeYellowCardsList)) {
            return false;
        }
        if (!Objects.equals(this.homeRedCardsList, other.homeRedCardsList)) {
            return false;
        }
        if (!Objects.equals(this.awayYellowCardsList, other.awayYellowCardsList)) {
            return false;
        }
        if (!Objects.equals(this.awayRedCardsList, other.awayRedCardsList)) {
            return false;
        }
        if (!Objects.equals(this.homeSubstitutionsList, other.homeSubstitutionsList)) {
            return false;
        }
        return Objects.equals(this.awaySubstitutionsList, other.awaySubstitutionsList);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 5;
        hash = 43 * hash + Objects.hashCode(this.homeTeam);
        hash = 43 * hash + Objects.hashCode(this.matchStatus);
        hash = 43 * hash + Objects.hashCode(this.awayTeam);
        hash = 43 * hash + this.homeScore;
        hash = 43 * hash + this.awayScore;
        hash = 43 * hash + Objects.hashCode(this.periodScoresList);
        hash = 43 * hash + Objects.hashCode(this.homeGoalsList);
        hash = 43 * hash + Objects.hashCode(this.awayGoalsList);
        hash = 43 * hash + Objects.hashCode(this.homeYellowCardsList);
        hash = 43 * hash + Objects.hashCode(this.homeRedCardsList);
        hash = 43 * hash + Objects.hashCode(this.awayYellowCardsList);
        hash = 43 * hash + Objects.hashCode(this.awayRedCardsList);
        hash = 43 * hash + Objects.hashCode(this.homeSubstitutionsList);
        hash = 43 * hash + Objects.hashCode(this.awaySubstitutionsList);
        return hash;
    }
}
