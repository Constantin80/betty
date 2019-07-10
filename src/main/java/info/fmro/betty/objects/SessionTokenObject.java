package info.fmro.betty.objects;

import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class SessionTokenObject
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(SessionTokenObject.class);
    private static final long serialVersionUID = -6100294880488404837L;
    public static final long defaultRecentPeriod = 1_000L;
    private String sessionToken;
    private long timeStamp;

    public synchronized String getSessionToken() {
        return this.sessionToken;
    }

    public synchronized void setSessionToken(final String sessionToken) {
        this.sessionToken = sessionToken;
        timeStamp();
    }

    public synchronized long getTimeStamp() {
        return this.timeStamp;
    }

    public synchronized void setTimeStamp(final long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public synchronized void timeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    public synchronized boolean isRecent() {
        return isRecent(defaultRecentPeriod);
    }

    private synchronized boolean isRecent(@SuppressWarnings("SameParameterValue") final long recentPeriod) {
        final long currentTime = System.currentTimeMillis();
        return currentTime - this.timeStamp <= recentPeriod;
    }

    public synchronized void copyFrom(final SessionTokenObject sessionTokenObject) {
        if (sessionTokenObject == null) {
            logger.error("null sessionTokenObject in copyFrom for: {}", Generic.objectToString(this));
        } else {
            this.sessionToken = sessionTokenObject.sessionToken;
            this.timeStamp = sessionTokenObject.timeStamp;
        }
    }
}
