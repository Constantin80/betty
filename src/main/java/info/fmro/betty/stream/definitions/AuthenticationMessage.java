package info.fmro.betty.stream.definitions;

import java.io.Serializable;

public class AuthenticationMessage
        extends RequestMessage
        implements Serializable {
    private static final long serialVersionUID = 8829964936313620371L;
    private String appKey; // This is your application key to identify your application
    private String session; // The session token generated from API login.

    public synchronized String getAppKey() {
        return this.appKey;
    }

    public synchronized void setAppKey(final String appKey) {
        this.appKey = appKey;
    }

    public synchronized String getSession() {
        return this.session;
    }

    public synchronized void setSession(final String session) {
        this.session = session;
    }
}
