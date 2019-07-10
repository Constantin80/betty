package info.fmro.betty.entities;

public class SessionToken {
    private String sessionToken;
    private String loginStatus;

    public synchronized String getSessionToken() {
        return this.sessionToken;
    }

    public synchronized void setSessionToken(final String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public synchronized int getSessionTokenLength() {
        return this.sessionToken == null ? -1 : this.sessionToken.length();
    }

    public synchronized String getLoginStatus() {
        return this.loginStatus;
    }

    public synchronized void setLoginStatus(final String loginStatus) {
        this.loginStatus = loginStatus;
    }
}
