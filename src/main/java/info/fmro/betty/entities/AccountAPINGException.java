package info.fmro.betty.entities;

import info.fmro.betty.enums.AccountAPINGExceptionErrorCode;

import java.io.Serializable;

public class AccountAPINGException
        extends Exception
        implements Serializable {
    private static final long serialVersionUID = -7836726656077478149L;
    private final String errorDetails, requestUUID;
    private final AccountAPINGExceptionErrorCode errorCode;

    public AccountAPINGException(final String errorDetails, final AccountAPINGExceptionErrorCode errorCode, final String requestUUID) {
        super();
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
        this.requestUUID = requestUUID;
    }

    public synchronized String getErrorDetails() {
        return this.errorDetails;
    }

    public synchronized AccountAPINGExceptionErrorCode getErrorCode() {
        return this.errorCode;
    }

    public synchronized String getRequestUUID() {
        return this.requestUUID;
    }
}
