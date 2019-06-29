package info.fmro.betty.stream.definitions;

public class AuthenticationMessage
        extends RequestMessage {
    private String appKey; // This is your application key to identify your application
    private String session; // The session token generated from API login.

    public AuthenticationMessage() {
    }

    public synchronized String getAppKey() {
        return appKey;
    }

    public synchronized void setAppKey(final String appKey) {
        this.appKey = appKey;
    }

    public synchronized String getSession() {
        return session;
    }

    public synchronized void setSession(final String session) {
        this.session = session;
    }
}
