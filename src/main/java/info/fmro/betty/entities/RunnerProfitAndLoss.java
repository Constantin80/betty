package info.fmro.betty.entities;

public class RunnerProfitAndLoss {
    private Long selectionId; // SelectionId alias Long
    private Double ifWin;
    private Double ifLose;
    @SuppressWarnings("unused")
    private Double ifPlace;

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

    public synchronized void setSelectionId(final Long selectionId) {
        this.selectionId = selectionId;
    }

    public synchronized Double getIfWin() {
        return this.ifWin;
    }

    public synchronized void setIfWin(final Double ifWin) {
        this.ifWin = ifWin;
    }

    public synchronized Double getIfLose() {
        return this.ifLose;
    }

    public synchronized void setIfLose(final Double ifLose) {
        this.ifLose = ifLose;
    }
}
