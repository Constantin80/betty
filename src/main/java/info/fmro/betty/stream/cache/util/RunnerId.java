package info.fmro.betty.stream.cache.util;

import java.util.Objects;

public class RunnerId {
    private final Long selectionId;
    private final Double handicap;

    public RunnerId(Long selectionId, Double handicap) {
        this.selectionId = selectionId;
        this.handicap = handicap;
    }

    public synchronized Long getSelectionId() {
        return selectionId;
    }

    public synchronized Double getHandicap() {
        return handicap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RunnerId runnerId = (RunnerId) o;
        return Objects.equals(selectionId, runnerId.selectionId) &&
               Objects.equals(handicap, runnerId.handicap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(selectionId, handicap);
    }
    
//    @Override
//    public synchronized boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        }
//        if (o == null || getClass() != o.getClass()) {
//            return false;
//        }
//
//        RunnerId runnerId = (RunnerId) o;
//
//        if (selectionId != runnerId.selectionId) {
//            return false;
//        }
//        return handicap != null ? handicap.equals(runnerId.handicap) : runnerId.handicap == null;
//    }
//
//    @Override
//    public synchronized int hashCode() {
//        int result = (int) (selectionId ^ (selectionId >>> 32));
//        result = 31 * result + (handicap != null ? handicap.hashCode() : 0);
//        return result;
//    }

    @Override
    public synchronized String toString() {
        return "RunnerId{" +
               "selectionId=" + selectionId +
               ", handicap=" + handicap +
               '}';
    }
}
