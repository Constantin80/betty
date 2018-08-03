package info.fmro.betty.objects;

import java.io.Serializable;

public class SessionTokenObject
        implements Serializable {
    private static final long serialVersionUID = -6100294880488404837L;
    public static final long defaultRecentPeriod = 1_000L;
    private String sessionToken;
    private long timeStamp;

    public synchronized String getSessionToken() {
        return sessionToken;
    }

    public synchronized void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
        timeStamp();
    }

    public synchronized long getTimeStamp() {
        return timeStamp;
    }

    public synchronized void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public synchronized void timeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    public synchronized boolean isRecent() {
        return isRecent(defaultRecentPeriod);
    }

    public synchronized boolean isRecent(long recentPeriod) {
        final long currentTime = System.currentTimeMillis();
        return currentTime - timeStamp <= recentPeriod;
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized void copyFrom(SessionTokenObject sessionTokenObject) {
        this.sessionToken = sessionTokenObject.sessionToken;
        this.timeStamp = sessionTokenObject.timeStamp;
    }
}
