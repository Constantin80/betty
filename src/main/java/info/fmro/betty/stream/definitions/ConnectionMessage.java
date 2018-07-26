package info.fmro.betty.stream.definitions;

public class ConnectionMessage
        extends ResponseMessage {
    private String connectionId; // The connection id

    public ConnectionMessage() {
    }

    public synchronized String getConnectionId() {
        return connectionId;
    }

    public synchronized void setConnectionId(String connectionId) {
        this.connectionId = connectionId;
    }
}
