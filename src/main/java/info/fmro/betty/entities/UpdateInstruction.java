package info.fmro.betty.entities;

import info.fmro.betty.enums.PersistenceType;

public class UpdateInstruction {
    private String betId;
    private PersistenceType newPersistenceType;

    public UpdateInstruction() {
    }

    public synchronized String getBetId() {
        return betId;
    }

    public synchronized void setBetId(final String betId) {
        this.betId = betId;
    }

    public synchronized PersistenceType getNewPersistenceType() {
        return newPersistenceType;
    }

    public synchronized void setNewPersistenceType(final PersistenceType newPersistenceType) {
        this.newPersistenceType = newPersistenceType;
    }
}
