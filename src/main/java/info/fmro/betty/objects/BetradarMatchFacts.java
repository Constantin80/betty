package info.fmro.betty.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class BetradarMatchFacts
        implements Serializable {
    private static final long serialVersionUID = 755062000734275494L;
    private final String homeTeam, matchStatus, awayTeam;
    private final int homeScore, awayScore;
    private final ArrayList<BetradarPeriodScore> periodScoresList;
    private final ArrayList<BetradarMatchEvent> homeGoalsList, awayGoalsList, homeYellowCardsList, homeRedCardsList, awayYellowCardsList, awayRedCardsList, homeSubstitutionsList, awaySubstitutionsList;

    public BetradarMatchFacts(final String homeTeam, final String matchStatus, final String awayTeam, final int homeScore, final int awayScore, final ArrayList<BetradarPeriodScore> periodScoresList, final ArrayList<BetradarMatchEvent> homeGoalsList, final ArrayList<BetradarMatchEvent> awayGoalsList,
                              final ArrayList<BetradarMatchEvent> homeYellowCardsList, final ArrayList<BetradarMatchEvent> homeRedCardsList, final ArrayList<BetradarMatchEvent> awayYellowCardsList, final ArrayList<BetradarMatchEvent> awayRedCardsList,
                              final ArrayList<BetradarMatchEvent> homeSubstitutionsList, final ArrayList<BetradarMatchEvent> awaySubstitutionsList) {
        this.homeTeam = homeTeam;
        this.matchStatus = matchStatus;
        this.awayTeam = awayTeam;
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        this.periodScoresList = periodScoresList;
        this.homeGoalsList = homeGoalsList;
        this.awayGoalsList = awayGoalsList;
        this.homeYellowCardsList = homeYellowCardsList;
        this.homeRedCardsList = homeRedCardsList;
        this.awayYellowCardsList = awayYellowCardsList;
        this.awayRedCardsList = awayRedCardsList;
        this.homeSubstitutionsList = homeSubstitutionsList;
        this.awaySubstitutionsList = awaySubstitutionsList;
    }

    public synchronized String getHomeTeam() {
        return homeTeam;
    }

    //    public synchronized void setHomeTeam(String homeTeam) {
//        this.homeTeam = homeTeam;
//    }
    public synchronized String getMatchStatus() {
        return matchStatus;
    }

    //    public synchronized void setMatchStatus(String matchStatus) {
//        this.matchStatus = matchStatus;
//    }
    public synchronized String getAwayTeam() {
        return awayTeam;
    }

    //    public synchronized void setAwayTeam(String awayTeam) {
//        this.awayTeam = awayTeam;
//    }
    public synchronized int getHomeScore() {
        return homeScore;
    }

    //    public synchronized void setHomeScore(int homeScore) {
//        this.homeScore = homeScore;
//    }
    public synchronized int getAwayScore() {
        return awayScore;
    }

    //    public synchronized void setAwayScore(int awayScore) {
//        this.awayScore = awayScore;
//    }
    public synchronized ArrayList<BetradarPeriodScore> getPeriodScoresList() {
        return periodScoresList == null ? null : new ArrayList<>(periodScoresList);
    }

    //    public synchronized void setPeriodScoresList(ArrayList<BetradarPeriodScore> periodScoresList) {
//        this.periodScoresList = periodScoresList == null ? null : new ArrayList<>(periodScoresList);
//    }
    public synchronized ArrayList<BetradarMatchEvent> getHomeGoalsList() {
        return homeGoalsList == null ? null : new ArrayList<>(homeGoalsList);
    }

    //    public synchronized void setHomeGoalsList(ArrayList<BetradarMatchEvent> homeGoalsList) {
//        this.homeGoalsList = homeGoalsList == null ? null : new ArrayList<>(homeGoalsList);
//    }
    public synchronized ArrayList<BetradarMatchEvent> getAwayGoalsList() {
        return awayGoalsList == null ? null : new ArrayList<>(awayGoalsList);
    }

    //    public synchronized void setAwayGoalsList(ArrayList<BetradarMatchEvent> awayGoalsList) {
//        this.awayGoalsList = awayGoalsList == null ? null : new ArrayList<>(awayGoalsList);
//    }
    public synchronized ArrayList<BetradarMatchEvent> getHomeYellowCardsList() {
        return homeYellowCardsList == null ? null : new ArrayList<>(homeYellowCardsList);
    }

    //    public synchronized void setHomeYellowCardsList(ArrayList<BetradarMatchEvent> homeYellowCardsList) {
//        this.homeYellowCardsList = homeYellowCardsList == null ? null : new ArrayList<>(homeYellowCardsList);
//    }
    public synchronized ArrayList<BetradarMatchEvent> getHomeRedCardsList() {
        return homeRedCardsList == null ? null : new ArrayList<>(homeRedCardsList);
    }

    //    public synchronized void setHomeRedCardsList(ArrayList<BetradarMatchEvent> homeRedCardsList) {
//        this.homeRedCardsList = homeRedCardsList == null ? null : new ArrayList<>(homeRedCardsList);
//    }
    public synchronized ArrayList<BetradarMatchEvent> getAwayYellowCardsList() {
        return awayYellowCardsList == null ? null : new ArrayList<>(awayYellowCardsList);
    }

    //    public synchronized void setAwayYellowCardsList(ArrayList<BetradarMatchEvent> awayYellowCardsList) {
//        this.awayYellowCardsList = awayYellowCardsList == null ? null : new ArrayList<>(awayYellowCardsList);
//    }
    public synchronized ArrayList<BetradarMatchEvent> getAwayRedCardsList() {
        return awayRedCardsList == null ? null : new ArrayList<>(awayRedCardsList);
    }

    //    public synchronized void setAwayRedCardsList(ArrayList<BetradarMatchEvent> awayRedCardsList) {
//        this.awayRedCardsList = awayRedCardsList == null ? null : new ArrayList<>(awayRedCardsList);
//    }
    public synchronized ArrayList<BetradarMatchEvent> getHomeSubstitutionsList() {
        return homeSubstitutionsList == null ? null : new ArrayList<>(homeSubstitutionsList);
    }

    //    public synchronized void setHomeSubstitutionsList(ArrayList<BetradarMatchEvent> homeSubstitutionsList) {
//        this.homeSubstitutionsList = homeSubstitutionsList == null ? null : new ArrayList<>(homeSubstitutionsList);
//    }
    public synchronized ArrayList<BetradarMatchEvent> getAwaySubstitutionsList() {
        return awaySubstitutionsList == null ? null : new ArrayList<>(awaySubstitutionsList);
    }

    //    public synchronized void setAwaySubstitutionsList(ArrayList<BetradarMatchEvent> awaySubstitutionsList) {
//        this.awaySubstitutionsList = awaySubstitutionsList == null ? null : new ArrayList<>(awaySubstitutionsList);
//    }
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
