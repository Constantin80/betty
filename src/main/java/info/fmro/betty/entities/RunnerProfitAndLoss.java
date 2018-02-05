package info.fmro.betty.entities;

public class RunnerProfitAndLoss {

    private Long selectionId; // SelectionId alias Long
    private Double ifWin;
    private Double ifLose;

    public RunnerProfitAndLoss() {
    }

    public synchronized Long getSelectionId() {
        return selectionId;
    }

    public synchronized void setSelectionId(Long selectionId) {
        this.selectionId = selectionId;
    }

    public synchronized Double getIfWin() {
        return ifWin;
    }

    public synchronized void setIfWin(Double ifWin) {
        this.ifWin = ifWin;
    }

    public synchronized Double getIfLose() {
        return ifLose;
    }

    public synchronized void setIfLose(Double ifLose) {
        this.ifLose = ifLose;
    }
}
