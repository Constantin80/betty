package info.fmro.betty.enums;

public enum MatchStatus {
    // Postponed, Not started, Start delayed, 1st half, Half Time, 2nd half, Awaiting extra time, 1st extra, Extra time halftime, 2nd extra, AET, Penalties, AP, Ended
    NOT_STARTED,
    POSTPONED,
    START_DELAYED,
    CANCELLED,
    FIRST_HALF,
    HALF_TIME,
    SECOND_HALF,
    AWAITING_ET,
    OVERTIME, // no minutes are normally shown; also used for Coral ET, as this includes those whole ET period
    FIRST_ET,
    ET_HALF_TIME,
    SECOND_ET,
    AFTER_ET,
    AWAITING_PEN,
    PENALTIES,
    AFTER_PEN,
    ENDED,
    INTERRUPTED,
    ABANDONED,
    UNKNOWN;

    public synchronized boolean hasStarted() {
        int ordinal = ordinal();
        return ordinal >= FIRST_HALF.ordinal() && ordinal <= ABANDONED.ordinal();
    }
}
