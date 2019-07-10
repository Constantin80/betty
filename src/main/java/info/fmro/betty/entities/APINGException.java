package info.fmro.betty.entities;

import info.fmro.betty.enums.APINGExceptionErrorCode;

import java.io.Serializable;

public class APINGException
        extends Exception
        implements Serializable {
    private static final long serialVersionUID = -4176578382637642300L;
    private final String errorDetails, requestUUID;
    private final APINGExceptionErrorCode errorCode;

    public APINGException(final String errorDetails, final APINGExceptionErrorCode errorCode, final String requestUUID) {
        super();
        this.errorCode = errorCode;
        this.errorDetails = errorDetails;
        this.requestUUID = requestUUID;
    }

    public synchronized String getErrorDetails() {
        return this.errorDetails;
    }

    public synchronized APINGExceptionErrorCode getErrorCode() {
        return this.errorCode;
    }

    public synchronized String getRequestUUID() {
        return this.requestUUID;
    }
}
