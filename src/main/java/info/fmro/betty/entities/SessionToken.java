package info.fmro.betty.entities;

public class SessionToken {

    private String sessionToken;
    private String loginStatus;

    public SessionToken() {
    }

    public synchronized String getSessionToken() {
        return sessionToken;
    }

    public synchronized void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public synchronized int getSessionTokenLength() {
        return sessionToken == null ? -1 : sessionToken.length();
    }

    public synchronized String getLoginStatus() {
        return loginStatus;
    }

    public synchronized void setLoginStatus(String loginStatus) {
        this.loginStatus = loginStatus;
    }
}
