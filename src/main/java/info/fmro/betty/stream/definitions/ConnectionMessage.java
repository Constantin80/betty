package info.fmro.betty.stream.definitions;

import java.io.Serializable;

// objects of this class are read from the stream
public class ConnectionMessage
        extends ResponseMessage
        implements Serializable {
    private static final long serialVersionUID = 189022449161940813L;
    private String connectionId; // The connection id

    public ConnectionMessage() {
    }

    public synchronized String getConnectionId() {
        return connectionId;
    }

    public synchronized void setConnectionId(final String connectionId) {
        this.connectionId = connectionId;
    }
}
