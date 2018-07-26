package info.fmro.betty.stream.protocol;

/**
 * Created by mulveyJ on 11/07/2016.
 */
public class ConnectionException
        extends Exception {
    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
