package info.fmro.betty.entities;

import info.fmro.betty.enums.AccountAPINGExceptionErrorCode;
import java.io.Serializable;

public class AccountAPINGException
        extends Throwable
        implements Serializable {

    private static final long serialVersionUID = -7836726656077478149L;
    private String errorDetails, requestUUID;
    private AccountAPINGExceptionErrorCode errorCode;

    public AccountAPINGException() {
        super((Throwable) null);
    }

    public AccountAPINGException(String errorDetails, AccountAPINGExceptionErrorCode errorCode, String requestUUID) {
        super((Throwable) null);
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
        this.requestUUID = requestUUID;
    }

    public synchronized String getErrorDetails() {
        return errorDetails;
    }

    public synchronized void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public synchronized AccountAPINGExceptionErrorCode getErrorCode() {
        return errorCode;
    }

    public synchronized void setErrorCode(AccountAPINGExceptionErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public synchronized String getRequestUUID() {
        return requestUUID;
    }

    public synchronized void setRequestUUID(String requestUUID) {
        this.requestUUID = requestUUID;
    }
}
