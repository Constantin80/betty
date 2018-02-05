package info.fmro.betty.entities;

import java.io.Serializable;
import java.util.Date;

public class ItemDescription
        implements Serializable {

    private static final long serialVersionUID = -148182567572921589L;
    private String eventTypeDesc;
    private String eventDesc;
    private String marketDesc;
    private Date marketStartTime;
    private String runnerDesc;
    private Integer numberOfWinners;

    public ItemDescription() {
    }

    public synchronized String getEventTypeDesc() {
        return eventTypeDesc;
    }

    public synchronized void setEventTypeDesc(String eventTypeDesc) {
        this.eventTypeDesc = eventTypeDesc;
    }

    public synchronized String getEventDesc() {
        return eventDesc;
    }

    public synchronized void setEventDesc(String eventDesc) {
        this.eventDesc = eventDesc;
    }

    public synchronized String getMarketDesc() {
        return marketDesc;
    }

    public synchronized void setMarketDesc(String marketDesc) {
        this.marketDesc = marketDesc;
    }

    public synchronized Date getMarketStartTime() {
        return marketStartTime == null ? null : (Date) marketStartTime.clone();
    }

    public synchronized void setMarketStartTime(Date marketStartTime) {
        this.marketStartTime = marketStartTime == null ? null : (Date) marketStartTime.clone();
    }

    public synchronized String getRunnerDesc() {
        return runnerDesc;
    }

    public synchronized void setRunnerDesc(String runnerDesc) {
        this.runnerDesc = runnerDesc;
    }

    public synchronized Integer getNumberOfWinners() {
        return numberOfWinners;
    }

    public synchronized void setNumberOfWinners(Integer numberOfWinners) {
        this.numberOfWinners = numberOfWinners;
    }
}
