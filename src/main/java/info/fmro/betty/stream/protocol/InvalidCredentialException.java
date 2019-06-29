package info.fmro.betty.stream.protocol;

/**
 * Created by mulveyj on 08/07/2016.
 */
public class InvalidCredentialException
        extends Exception {
    private static final long serialVersionUID = -2368243618695042927L;

    public InvalidCredentialException(final String message) {
        super(message);
    }
}
