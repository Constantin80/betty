package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.ErrorCode;
import info.fmro.betty.stream.definitions.StatusMessage;

/**
 * Created by mulveyj on 07/07/2016.
 */
public class StatusException
        extends Exception {
    private static final long serialVersionUID = -2172703483618150002L;
    private final ErrorCode errorCode;
    private final String errorMessage;

    public StatusException(final StatusMessage message) {
        super(message.getErrorCode() + ": " + message.getErrorMessage());
        this.errorCode = message.getErrorCode();
        this.errorMessage = message.getErrorMessage();
    }

    public synchronized ErrorCode getErrorCode() {
        return this.errorCode;
    }

    public synchronized String getErrorMessage() {
        return this.errorMessage;
    }
}
