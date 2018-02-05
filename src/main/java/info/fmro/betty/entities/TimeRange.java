package info.fmro.betty.entities;

import java.util.Date;

public class TimeRange {

    private Date from;
    private Date to;

    public TimeRange() {
    }

    public synchronized Date getFrom() {
        return from == null ? null : (Date) from.clone();
    }

    public synchronized void setFrom(Date from) {
        this.from = from == null ? null : (Date) from.clone();
    }

    public synchronized Date getTo() {
        return to == null ? null : (Date) to.clone();
    }

    public synchronized void setTo(Date to) {
        this.to = to == null ? null : (Date) to.clone();
    }
}
