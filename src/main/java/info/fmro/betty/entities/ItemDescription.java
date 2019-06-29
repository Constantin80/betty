package info.fmro.betty.entities;

import java.io.Serializable;
import java.util.Date;

public class ItemDescription
        implements Serializable {
    private static final long serialVersionUID = -148182567572921589L;
    private String eventTypeDesc;
    private String eventDesc;
    private String marketDesc;
    private String marketType; // The market type e.g. MATCH_ODDS, PLACE, WIN etc.
    private Date marketStartTime;
    private String runnerDesc;
    private Integer numberOfWinners;
    private Double eachWayDivisor; // The divisor is returned for the marketType EACH_WAY only and refers to the fraction of the win odds at which the place portion of an each way bet is settled

    public ItemDescription() {
    }

    public synchronized String getEventTypeDesc() {
        return eventTypeDesc;
    }

    public synchronized void setEventTypeDesc(final String eventTypeDesc) {
        this.eventTypeDesc = eventTypeDesc;
    }

    public synchronized String getEventDesc() {
        return eventDesc;
    }

    public synchronized void setEventDesc(final String eventDesc) {
        this.eventDesc = eventDesc;
    }

    public synchronized String getMarketDesc() {
        return marketDesc;
    }

    public synchronized void setMarketDesc(final String marketDesc) {
        this.marketDesc = marketDesc;
    }

    public synchronized Date getMarketStartTime() {
        return marketStartTime == null ? null : (Date) marketStartTime.clone();
    }

    public synchronized void setMarketStartTime(final Date marketStartTime) {
        this.marketStartTime = marketStartTime == null ? null : (Date) marketStartTime.clone();
    }

    public synchronized String getRunnerDesc() {
        return runnerDesc;
    }

    public synchronized void setRunnerDesc(final String runnerDesc) {
        this.runnerDesc = runnerDesc;
    }

    public synchronized Integer getNumberOfWinners() {
        return numberOfWinners;
    }

    public synchronized void setNumberOfWinners(final Integer numberOfWinners) {
        this.numberOfWinners = numberOfWinners;
    }
}
