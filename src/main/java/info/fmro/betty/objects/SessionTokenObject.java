package info.fmro.betty.objects;

import java.io.Serializable;

public class SessionTokenObject
        implements Serializable {

    private static final long serialVersionUID = -6100294880488404837L;
    private String sessionToken;
    private long timeStamp;

    public synchronized String getSessionToken() {
        return sessionToken;
    }

    public synchronized void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
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

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized void copyFrom(SessionTokenObject sessionTokenObject) {
        this.sessionToken = sessionTokenObject.sessionToken;
        this.timeStamp = sessionTokenObject.timeStamp;
    }
}
