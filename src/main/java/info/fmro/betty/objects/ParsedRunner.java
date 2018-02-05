package info.fmro.betty.objects;

import info.fmro.betty.enums.ParsedRunnerType;
import java.io.Serializable;
import java.util.Objects;

public class ParsedRunner
        implements Serializable, Comparable<ParsedRunner> {

    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = 1449977052034070871L;
    private final Long selectionId;
    private ParsedRunnerType parsedRunnerType;

    public ParsedRunner(Long selectionId) {
        this.selectionId = selectionId;
    }

    public ParsedRunner(Long selectionId, ParsedRunnerType parsedRunnerType) {
        this.selectionId = selectionId;
        this.parsedRunnerType = parsedRunnerType;
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

        return EQUAL;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 3;
        hash = 83 * hash + (int) (this.selectionId ^ (this.selectionId >>> 32));
        return hash;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParsedRunner other = (ParsedRunner) obj;
        return Objects.equals(this.selectionId, other.selectionId);
    }
}
