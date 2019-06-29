package info.fmro.betty.enums;

public enum APINGExceptionErrorCode {
    TOO_MUCH_DATA("The operation requested too much data, exceeding the Market Data Request Limits."),
    INVALID_INPUT_DATA("Invalid input data"),
    INVALID_SESSION_INFORMATION("The session token hasn't been provided, is invalid or has expired."),
    NO_APP_KEY("An application key header ('X-Application') has not been provided in the request"),
    NO_SESSION("A session token header ('X-Authentication') has not been provided in the request"),
    UNEXPECTED_ERROR("An unexpected internal error occurred that prevented successful request processing."),
    INVALID_APP_KEY("The application key passed is invalid or is not present"),
    TOO_MANY_REQUESTS("There are too many pending requests e.g. a listMarketBook with Order/Match projections is limited to 3 concurrent requests. The error also applies to " +
                      "listCurrentOrders, listMarketProfitAndLoss and listClearedOrders if you have 3 or more requests currently in execution"),
    SERVICE_BUSY("The service is currently too busy to service this request"),
    TIMEOUT_ERROR("Internal call to downstream service timed out"),
    REQUEST_SIZE_EXCEEDS_LIMIT("The request exceeds the request size limit. Requests are limited to a total of 250 betId’s/marketId’s (or a combination of both)"),
    ACCESS_DENIED("The calling client is not permitted to perform the specific action e.g. the using a Delayed App Key when placing bets or attempting to place a bet from a " +
                  "restricted jurisdiction.");

    private final String message;

    private APINGExceptionErrorCode(final String message) {
        this.message = message;
    }

    public synchronized String getMessage() {
        return message;
    }
}
