package info.fmro.betty.entities;

@SuppressWarnings("unused")
public class CompetitionResult {
    private Competition competition;
    private Integer marketCount;
    private String competitionRegion;

    public synchronized Competition getCompetition() {
        return this.competition;
    }

    public synchronized void setCompetition(final Competition competition) {
        this.competition = competition;
    }

    public synchronized Integer getMarketCount() {
        return this.marketCount;
    }

    public synchronized void setMarketCount(final Integer marketCount) {
        this.marketCount = marketCount;
    }

    public synchronized String getCompetitionRegion() {
        return this.competitionRegion;
    }

    public synchronized void setCompetitionRegion(final String competitionRegion) {
        this.competitionRegion = competitionRegion;
    }
}
