package info.fmro.betty.stream.definitions;

import info.fmro.betty.enums.RunnerStatus;

import java.util.Date;

public class RunnerDefinition {
    private Double adjustmentFactor;
    private Double bsp;
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

    public synchronized void setAdjustmentFactor(Double adjustmentFactor) {
        this.adjustmentFactor = adjustmentFactor;
    }

    public synchronized Double getBsp() {
        return bsp;
    }

    public synchronized void setBsp(Double bsp) {
        this.bsp = bsp;
    }

    public synchronized Double getHc() {
        return hc;
    }

    public synchronized void setHc(Double hc) {
        this.hc = hc;
    }

    public synchronized Long getId() {
        return id;
    }

    public synchronized void setId(Long id) {
        this.id = id;
    }

    public synchronized Date getRemovalDate() {
        return removalDate == null ? null : (Date) removalDate.clone();
    }

    public synchronized void setRemovalDate(Date removalDate) {
        this.removalDate = removalDate == null ? null : (Date) removalDate.clone();
    }

    public synchronized Integer getSortPriority() {
        return sortPriority;
    }

    public synchronized void setSortPriority(Integer sortPriority) {
        this.sortPriority = sortPriority;
    }

    public synchronized RunnerStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(RunnerStatus status) {
        this.status = status;
    }
}
