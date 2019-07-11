package info.fmro.betty.entities;

import java.io.Serializable;

@SuppressWarnings("unused")
class KeyLineSelection
        implements Serializable {
    private static final long serialVersionUID = -5313979741755803610L;
    private Long selectionId;
    private Double handicap;

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

    public synchronized void setSelectionId(final Long selectionId) {
        this.selectionId = selectionId;
    }

    public synchronized Double getHandicap() {
        return this.handicap;
    }

    public synchronized void setHandicap(final Double handicap) {
        this.handicap = handicap;
    }
}
