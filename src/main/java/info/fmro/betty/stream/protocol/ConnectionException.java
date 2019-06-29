package info.fmro.betty.stream.protocol;

/**
 * Created by mulveyJ on 11/07/2016.
 */
public class ConnectionException
        extends Exception {
    public ConnectionException(final String message) {
        super(message);
    }

    public ConnectionException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
