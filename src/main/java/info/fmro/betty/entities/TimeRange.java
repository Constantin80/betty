package info.fmro.betty.entities;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Date;

public class TimeRange
        implements Serializable {
    private static final long serialVersionUID = -2987284947265873972L;
    @Nullable
    private Date from;
    @Nullable
    private Date to;

    @Nullable
    public synchronized Date getFrom() {
        return this.from == null ? null : (Date) this.from.clone();
    }

    public synchronized void setFrom(final Date from) {
        this.from = from == null ? null : (Date) from.clone();
    }

    @Nullable
    public synchronized Date getTo() {
        return this.to == null ? null : (Date) this.to.clone();
    }

    public synchronized void setTo(final Date to) {
        this.to = to == null ? null : (Date) to.clone();
    }
}
