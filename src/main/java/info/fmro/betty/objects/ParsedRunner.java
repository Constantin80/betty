package info.fmro.betty.objects;

import info.fmro.betty.enums.ParsedRunnerType;

import java.io.Serializable;
import java.util.Objects;

public class ParsedRunner
        implements Serializable, Comparable<ParsedRunner> {

    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = 1449977052034070871L;
    private final Long selectionId;
    private final Double handicap;
    private ParsedRunnerType parsedRunnerType;

    public ParsedRunner(Long selectionId, Double handicap) {
        this.selectionId = selectionId;
        this.handicap = handicap;
    }

    public ParsedRunner(Long selectionId, Double handicap, ParsedRunnerType parsedRunnerType) {
        this.selectionId = selectionId;
        this.handicap = handicap;
        this.parsedRunnerType = parsedRunnerType;
    }

    public synchronized Double getHandicap() {
        return handicap;
    }

    public synchronized Long getSelectionId() {
        return selectionId;
    }

    public synchronized ParsedRunnerType getParsedRunnerType() {
        return parsedRunnerType;
    }

    public synchronized void setParsedRunnerType(ParsedRunnerType parsedRunnerType) {
        this.parsedRunnerType = parsedRunnerType;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(ParsedRunner other) {
        if (other == null) {
            return AFTER;
        }
        if (this == other) {
            return EQUAL;
        }

        if (this.getClass() != other.getClass()) {
            if (this.getClass().hashCode() < other.getClass().hashCode()) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (!Objects.equals(this.selectionId, other.selectionId)) {
            if (this.selectionId == null) {
                return BEFORE;
            }
            if (other.selectionId == null) {
                return AFTER;
            }
            return this.selectionId.compareTo(other.selectionId);
        }
        if (!Objects.equals(this.handicap, other.handicap)) {
            if (this.handicap == null) {
                return BEFORE;
            }
            if (other.handicap == null) {
                return AFTER;
            }
            return this.handicap.compareTo(other.handicap);
        }
        return EQUAL;
    }

    @Override
    public synchronized boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ParsedRunner that = (ParsedRunner) o;
        return Objects.equals(selectionId, that.selectionId) &&
               Objects.equals(handicap, that.handicap);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(selectionId, handicap);
    }
}
