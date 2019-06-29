package info.fmro.betty.entities;

import info.fmro.betty.enums.APINGExceptionErrorCode;

import java.io.Serializable;

public class APINGException
        extends Throwable
        implements Serializable {
    private static final long serialVersionUID = -4176578382637642300L;
    private String errorDetails, requestUUID;
    private APINGExceptionErrorCode errorCode;

    public APINGException() {
        super((Throwable) null);
    }

    public APINGException(final String errorDetails, final APINGExceptionErrorCode errorCode, final String requestUUID) {
        super((Throwable) null);
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
        this.requestUUID = requestUUID;
    }

    public synchronized String getErrorDetails() {
        return errorDetails;
    }

    public synchronized void setErrorDetails(final String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public synchronized APINGExceptionErrorCode getErrorCode() {
        return errorCode;
    }

    public synchronized void setErrorCode(final APINGExceptionErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public synchronized String getRequestUUID() {
        return requestUUID;
    }

    public synchronized void setRequestUUID(final String requestUUID) {
        this.requestUUID = requestUUID;
    }
}
