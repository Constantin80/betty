package info.fmro.betty.enums;

public enum InstructionReportErrorCode {
    INVALID_BET_SIZE,
    INVALID_RUNNER,
    BET_TAKEN_OR_LAPSED,
    BET_IN_PROGRESS,
    RUNNER_REMOVED,
    MARKET_NOT_OPEN_FOR_BETTING,
    LOSS_LIMIT_EXCEEDED,
    MARKET_NOT_OPEN_FOR_BSP_BETTING,
    INVALID_PRICE_EDIT,
    INVALID_ODDS,
    INSUFFICIENT_FUNDS,
    INVALID_PERSISTENCE_TYPE,
    ERROR_IN_MATCHER,
    INVALID_BACK_LAY_COMBINATION,
    ERROR_IN_ORDER,
    INVALID_BID_TYPE,
    INVALID_BET_ID,
    CANCELLED_NOT_PLACED,
    RELATED_ACTION_FAILED,
    NO_ACTION_REQUIRED,
    TIME_IN_FORCE_CONFLICT, // You may only specify a time in force on either the place request OR on individual limit order instructions (not both), since the implied behaviors are incompatible.
    UNEXPECTED_PERSISTENCE_TYPE, // You have specified a persistence type for a FILL_OR_KILL order, which is nonsensical because no umatched portion can remain after the order has been placed.
    INVALID_ORDER_TYPE, // You have specified a time in force of FILL_OR_KILL, but have included a non-LIMIT order type.
    UNEXPECTED_MIN_FILL_SIZE, // You have specified a minFillSize on a limit order, where the limit order's time in force is not FILL_OR_KILL. Using minFillSize is not supported where the time in force of the request (as opposed to an order) is FILL_OR_KILL.
    INVALID_CUSTOMER_ORDER_REF, // The supplied customer order reference is too long.
    INVALID_MIN_FILL_SIZE // The minFillSize must be greater than zero and less than or equal to the order's size. The minFillSize cannot be less than the minimum bet size for your currency
}
