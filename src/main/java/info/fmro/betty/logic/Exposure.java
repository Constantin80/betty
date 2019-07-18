package info.fmro.betty.logic;

import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class Exposure
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(Exposure.class);
    private static final long serialVersionUID = 281818769916579900L;
    public static final long recentPeriod = 5_000L;
    private double backMatchedExposure, layMatchedExposure, backTotalExposure, layTotalExposure, backUnmatchedProfit, layUnmatchedProfit, tempBackCancel, tempLayCancel;
    private long timeStamp;

    @Contract(pure = true)
    Exposure() {
    }

    synchronized void resetExposure() {
        this.backMatchedExposure = 0d;
        this.layMatchedExposure = 0d;
        this.backTotalExposure = 0d;
        this.layTotalExposure = 0d;
        this.backUnmatchedProfit = 0d;
        this.layUnmatchedProfit = 0d;
        this.tempBackCancel = 0d;
        this.tempLayCancel = 0d;
        this.timeStamp = 0L;
    }

    public synchronized void updateExposure(final Exposure source) {
        if (source == null) {
            logger.error("null source in Exposure update for: {}", Generic.objectToString(this));
        } else {
            this.setBackMatchedExposure(source.getBackMatchedExposure());
            this.setLayMatchedExposure(source.getLayMatchedExposure());
            this.setBackTotalExposure(source.getBackTotalExposure());
            this.setLayTotalExposure(source.getLayTotalExposure());
            this.setBackUnmatchedProfit(source.getBackUnmatchedProfit());
            this.setLayUnmatchedProfit(source.getLayUnmatchedProfit());
            this.setTempBackCancel(source.getTempBackCancel());
            this.setTempLayCancel(source.getTempLayCancel());
            timeStamp(source.getTimeStamp());
        }
    }

    public synchronized void timeStamp() {
        final long currentTime = System.currentTimeMillis();
        timeStamp(currentTime);
    }

    private synchronized void timeStamp(final long currentTime) {
        this.timeStamp = currentTime;
    }

    private synchronized long getTimeStamp() {
        return this.timeStamp;
    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized boolean isRecent() {
        final long currentTime = System.currentTimeMillis();
        return isRecent(currentTime);
    }

    private synchronized boolean isRecent(final long currentTime) {
        final long timeSinceUpdate = currentTime - this.timeStamp;
        final boolean isRecent = timeSinceUpdate <= recentPeriod;
        if (!isRecent) {
            logger.warn("non recent Exposure {} for: {}", timeSinceUpdate, Generic.objectToString(this));
        }

        return isRecent;
    }

    public synchronized void setBackMatchedExposure(final double backMatchedExposure) {
        this.backMatchedExposure = backMatchedExposure;
    }

    public synchronized void setLayMatchedExposure(final double layMatchedExposure) {
        this.layMatchedExposure = layMatchedExposure;
    }

    public synchronized void setBackTotalExposure(final double backTotalExposure) {
        this.backTotalExposure = backTotalExposure;
    }

    public synchronized void setLayTotalExposure(final double layTotalExposure) {
        this.layTotalExposure = layTotalExposure;
    }

    public synchronized void setBackUnmatchedProfit(final double backUnmatchedProfit) {
        this.backUnmatchedProfit = backUnmatchedProfit;
    }

    public synchronized void setLayUnmatchedProfit(final double layUnmatchedProfit) {
        this.layUnmatchedProfit = layUnmatchedProfit;
    }

    public synchronized void setTempBackCancel(final double tempBackCancel) {
        this.tempBackCancel = tempBackCancel;
    }

    public synchronized void setTempLayCancel(final double tempLayCancel) {
        this.tempLayCancel = tempLayCancel;
    }

    synchronized double getBackMatchedExposure() {
        isRecent(); // this prints a warning if not recent, but I'll return the value anyway
        return this.backMatchedExposure;
    }

    synchronized double getLayMatchedExposure() {
        isRecent(); // this prints a warning if not recent, but I'll return the value anyway
        return this.layMatchedExposure;
    }

    synchronized double getBackTotalExposure() {
        isRecent(); // this prints a warning if not recent, but I'll return the value anyway
        return this.backTotalExposure;
    }

    synchronized double getLayTotalExposure() {
        isRecent(); // this prints a warning if not recent, but I'll return the value anyway
        return this.layTotalExposure;
    }

    private synchronized double getBackUnmatchedProfit() {
        isRecent(); // this prints a warning if not recent, but I'll return the value anyway
        return this.backUnmatchedProfit;
    }

    private synchronized double getLayUnmatchedProfit() {
        isRecent(); // this prints a warning if not recent, but I'll return the value anyway
        return this.layUnmatchedProfit;
    }

    private synchronized double getTempBackCancel() {
        isRecent(); // this prints a warning if not recent, but I'll return the value anyway
        return this.tempBackCancel;
    }

    private synchronized double getTempLayCancel() {
        isRecent(); // this prints a warning if not recent, but I'll return the value anyway
        return this.tempLayCancel;
    }
}
