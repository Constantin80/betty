package info.fmro.betty.stream.definitions;

import info.fmro.betty.enums.RunnerStatus;

import java.io.Serializable;
import java.util.Date;

// objects of this class are read from the stream
public class RunnerDefinition
        implements Serializable {
    private static final long serialVersionUID = -6127210092733234930L;
    private Double adjustmentFactor;
    private Double bsp; // Betfair Starting Price
    private Double hc; // Handicap - the handicap of the runner (selection) (null if not applicable)
    private Long id; // Selection Id - the id of the runner (selection)
    private Date removalDate;
    private Integer sortPriority;
    private RunnerStatus status;

    public RunnerDefinition() {
    }

    public synchronized Double getAdjustmentFactor() {
        return adjustmentFactor;
    }

    public synchronized void setAdjustmentFactor(final Double adjustmentFactor) {
        this.adjustmentFactor = adjustmentFactor;
    }

    public synchronized Double getBsp() {
        return bsp;
    }

    public synchronized void setBsp(final Double bsp) {
        this.bsp = bsp;
    }

    public synchronized Double getHc() {
        return hc;
    }

    public synchronized void setHc(final Double hc) {
        this.hc = hc;
    }

    public synchronized Long getId() {
        return id;
    }

    public synchronized void setId(final Long id) {
        this.id = id;
    }

    public synchronized Date getRemovalDate() {
        return removalDate == null ? null : (Date) removalDate.clone();
    }

    public synchronized void setRemovalDate(final Date removalDate) {
        this.removalDate = removalDate == null ? null : (Date) removalDate.clone();
    }

    public synchronized Integer getSortPriority() {
        return sortPriority;
    }

    public synchronized void setSortPriority(final Integer sortPriority) {
        this.sortPriority = sortPriority;
    }

    public synchronized RunnerStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(final RunnerStatus status) {
        this.status = status;
    }
}
