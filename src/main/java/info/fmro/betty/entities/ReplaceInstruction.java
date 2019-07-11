package info.fmro.betty.entities;

@SuppressWarnings("unused")
class ReplaceInstruction {
    private String betId;
    private Double newPrice;

    public synchronized String getBetId() {
        return this.betId;
    }

    public synchronized void setBetId(final String betId) {
        this.betId = betId;
    }

    public synchronized Double getNewPrice() {
        return this.newPrice;
    }

    public synchronized void setNewPrice(final Double newPrice) {
        this.newPrice = newPrice;
    }
}
