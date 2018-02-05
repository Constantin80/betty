package info.fmro.betty.enums;

public enum AccountAPINGExceptionErrorCode {

    INVALID_INPUT_DATA("Invalid input data"),
    INVALID_SESSION_INFORMATION("The session token hasn't been provided, is invalid or has expired."),
    UNEXPECTED_ERROR("An unexpected internal error occurred that prevented successful request processing."),
    INVALID_APP_KEY("The application key passed is invalid or is not present"),
    SERVICE_BUSY("The service is currently too busy to service this request"),
    TIMEOUT_ERROR("Internal call to downstream service timed out"),
    DUPLICATE_APP_NAME("Duplicate application name"),
    APP_KEY_CREATION_FAILED("Creating application key version has failed"),
    APP_CREATION_FAILED("Application creation has been failed"),
    NO_SESSION("A session token header ('X-Authentication') has not been provided in the request"),
    NO_APP_KEY("An application key header ('X-Application') has not been provided in the request"),
    SUBSCRIPTION_EXPIRED("An application key is required for this operation"),
    INVALID_SUBSCRIPTION_TOKEN("The subscription token provided doesn't exist"),
    TOO_MANY_REQUESTS("Too many requests"),
    INVALID_CLIENT_REF("Invalid length for the client reference"),
    WALLET_TRANSFER_ERROR("There was a problem transferring funds between your wallets"),
    INVALID_VENDOR_CLIENT_ID("The vendor client ID is not subscribed to this application key");

    private final String message;

    private AccountAPINGExceptionErrorCode(String message) {
        this.message = message;
    }

    public synchronized String getMessage() {
        return message;
    }
}
