package info.fmro.betty.entities;

public class CompetitionResult {

    private Competition competition;
    private Integer marketCount;
    private String competitionRegion;

    public CompetitionResult() {
    }

    public synchronized Competition getCompetition() {
        return competition;
    }

    public synchronized void setCompetition(Competition competition) {
        this.competition = competition;
    }

    public synchronized Integer getMarketCount() {
        return marketCount;
    }

    public synchronized void setMarketCount(Integer marketCount) {
        this.marketCount = marketCount;
    }

    public synchronized String getCompetitionRegion() {
        return competitionRegion;
    }

    public synchronized void setCompetitionRegion(String competitionRegion) {
        this.competitionRegion = competitionRegion;
    }
}
