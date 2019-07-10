package info.fmro.betty.entities;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;

public class ItemDescription
        implements Serializable {
    private static final long serialVersionUID = -148182567572921589L;
    private String eventTypeDesc;
    private String eventDesc;
    private String marketDesc;
    private String marketType; // The market type e.g. MATCH_ODDS, PLACE, WIN etc.
    @Nullable
    private Date marketStartTime;
    private String runnerDesc;
    private Integer numberOfWinners;
    private Double eachWayDivisor; // The divisor is returned for the marketType EACH_WAY only and refers to the fraction of the win odds at which the place portion of an each way bet is settled

    public synchronized String getEventTypeDesc() {
        return this.eventTypeDesc;
    }

    public synchronized void setEventTypeDesc(final String eventTypeDesc) {
        this.eventTypeDesc = eventTypeDesc;
    }

    public synchronized String getEventDesc() {
        return this.eventDesc;
    }

    public synchronized void setEventDesc(final String eventDesc) {
        this.eventDesc = eventDesc;
    }

    public synchronized String getMarketDesc() {
        return this.marketDesc;
    }

    public synchronized void setMarketDesc(final String marketDesc) {
        this.marketDesc = marketDesc;
    }

    public synchronized String getMarketType() {
        return this.marketType;
    }

    public synchronized void setMarketType(final String marketType) {
        this.marketType = marketType;
    }

    @Nullable
    public synchronized Date getMarketStartTime() {
        return this.marketStartTime == null ? null : (Date) this.marketStartTime.clone();
    }

    public synchronized void setMarketStartTime(final Date marketStartTime) {
        this.marketStartTime = marketStartTime == null ? null : (Date) marketStartTime.clone();
    }

    public synchronized String getRunnerDesc() {
        return this.runnerDesc;
    }

    public synchronized void setRunnerDesc(final String runnerDesc) {
        this.runnerDesc = runnerDesc;
    }

    public synchronized Integer getNumberOfWinners() {
        return this.numberOfWinners;
    }

    public synchronized void setNumberOfWinners(final Integer numberOfWinners) {
        this.numberOfWinners = numberOfWinners;
    }

    public synchronized Double getEachWayDivisor() {
        return this.eachWayDivisor;
    }

    public synchronized void setEachWayDivisor(final Double eachWayDivisor) {
        this.eachWayDivisor = eachWayDivisor;
    }
}
