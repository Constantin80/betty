package info.fmro.betty.entities;

public class CancelInstruction {
    private String betId;
    private Double sizeReduction;

    public CancelInstruction() {
    }

    public synchronized String getBetId() {
        return betId;
    }

    public synchronized void setBetId(final String betId) {
        this.betId = betId;
    }

    public synchronized Double getSizeReduction() {
        return sizeReduction;
    }

    public synchronized void setSizeReduction(final Double sizeReduction) {
        this.sizeReduction = sizeReduction;
    }
}
