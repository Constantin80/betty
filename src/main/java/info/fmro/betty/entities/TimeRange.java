package info.fmro.betty.entities;

import java.io.Serializable;
import java.util.Date;

public class TimeRange
        implements Serializable {
    private static final long serialVersionUID = -2987284947265873972L;
    private Date from;
    private Date to;

    public TimeRange() {
    }

    public synchronized Date getFrom() {
        return from == null ? null : (Date) from.clone();
    }

    public synchronized void setFrom(final Date from) {
        this.from = from == null ? null : (Date) from.clone();
    }

    public synchronized Date getTo() {
        return to == null ? null : (Date) to.clone();
    }

    public synchronized void setTo(final Date to) {
        this.to = to == null ? null : (Date) to.clone();
    }
}
