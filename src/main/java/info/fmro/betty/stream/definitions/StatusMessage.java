package info.fmro.betty.stream.definitions;

public class StatusMessage
        extends ResponseMessage {
    private Boolean connectionClosed; // Is the connection now closed
    private String connectionId; // The connection id
    private ErrorCode errorCode; // The type of error in case of a failure
    private String errorMessage; // Additional message in case of a failure
    private StatusCode statusCode; // The status of the last request

    public StatusMessage() {
    }

    public synchronized Boolean getConnectionClosed() {
        return connectionClosed;
    }

    public synchronized void setConnectionClosed(Boolean connectionClosed) {
        this.connectionClosed = connectionClosed;
    }

    public synchronized String getConnectionId() {
        return connectionId;
    }

    public synchronized void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }

    public synchronized ErrorCode getErrorCode() {
        return errorCode;
    }

    public synchronized void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }

    public synchronized String getErrorMessage() {
        return errorMessage;
    }

    public synchronized void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public synchronized StatusCode getStatusCode() {
        return statusCode;
    }

    public synchronized void setStatusCode(StatusCode statusCode) {
        this.statusCode = statusCode;
    }
}
