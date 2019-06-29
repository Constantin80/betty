package info.fmro.betty.entities;

public class ReplaceInstruction {
    private String betId;
    private Double newPrice;

    public ReplaceInstruction() {
    }

    public synchronized String getBetId() {
        return betId;
    }

    public synchronized void setBetId(final String betId) {
        this.betId = betId;
    }

    public synchronized Double getNewPrice() {
        return newPrice;
    }

    public synchronized void setNewPrice(final Double newPrice) {
        this.newPrice = newPrice;
    }
}
