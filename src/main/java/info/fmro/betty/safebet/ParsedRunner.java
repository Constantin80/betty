package info.fmro.betty.safebet;

import info.fmro.shared.enums.ParsedRunnerType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Objects;

class ParsedRunner
        implements Serializable, Comparable<ParsedRunner> {
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = 1449977052034070871L;
    private final Long selectionId;
    private final Double handicap;
    private ParsedRunnerType parsedRunnerType;

    @SuppressWarnings("unused")
    @Contract(pure = true)
    ParsedRunner(final Long selectionId, final Double handicap) {
        this.selectionId = selectionId;
        this.handicap = handicap;
    }

    @Contract(pure = true)
    ParsedRunner(final Long selectionId, final Double handicap, final ParsedRunnerType parsedRunnerType) {
        this.selectionId = selectionId;
        this.handicap = handicap;
        this.parsedRunnerType = parsedRunnerType;
    }

    public synchronized Double getHandicap() {
        return this.handicap;
    }

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

    public synchronized ParsedRunnerType getParsedRunnerType() {
        return this.parsedRunnerType;
    }

    public synchronized void setParsedRunnerType(final ParsedRunnerType parsedRunnerType) {
        this.parsedRunnerType = parsedRunnerType;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public synchronized int compareTo(@NotNull final ParsedRunner o) {
        //noinspection ConstantConditions
        if (o == null) {
            return AFTER;
        }
        if (this == o) {
            return EQUAL;
        }

        if (this.getClass() != o.getClass()) {
            return this.getClass().hashCode() < o.getClass().hashCode() ? BEFORE : AFTER;
        }
        if (!Objects.equals(this.selectionId, o.selectionId)) {
            if (this.selectionId == null) {
                return BEFORE;
            }
            if (o.selectionId == null) {
                return AFTER;
            }
            return this.selectionId.compareTo(o.selectionId);
        }
        if (!Objects.equals(this.handicap, o.handicap)) {
            if (this.handicap == null) {
                return BEFORE;
            }
            if (o.handicap == null) {
                return AFTER;
            }
            return this.handicap.compareTo(o.handicap);
        }
        return EQUAL;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ParsedRunner that = (ParsedRunner) obj;
        return Objects.equals(this.selectionId, that.selectionId) &&
               Objects.equals(this.handicap, that.handicap);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.selectionId, this.handicap);
    }
}
