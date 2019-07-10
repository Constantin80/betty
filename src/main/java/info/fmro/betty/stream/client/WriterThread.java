package info.fmro.betty.stream.client;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class WriterThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(WriterThread.class);
    public static final String CRLF = "\r\n";
    private final Client client;
    private BufferedWriter bufferedWriter;
    @Nullable
    private String authLine;
    private long lastAuthSentStamp;
    private final LinkedList<String> linesList = new LinkedList<>();
    public final AtomicBoolean bufferNotEmpty = new AtomicBoolean();

    WriterThread(final Client client) {
        this.client = client;
    }

    synchronized void setBufferedWriter(final BufferedWriter bufferedWriter) {
        if (this.bufferedWriter != null) {
            try {
                this.bufferedWriter.close();
            } catch (IOException e) {
                logger.error("[{}]error while closing buffer in setBufferedWriter", this.client.id, e);
            }
        }
        this.bufferedWriter = bufferedWriter;
    }

    public synchronized void setAuthLine(final String line) {
        if (this.authLine != null) {
            logger.error("[{}]previous authLine not null in setAuthLine: {} {}", this.client.id, this.authLine, line);
        } else if (line != null) {
            this.authLine = line;
            this.bufferNotEmpty.set(true);
        } else {
            logger.error("[{}]trying to set null line in setAuthLine", this.client.id);
        }
    }

    private synchronized String getAuthLine() {
        final String result = this.authLine;
        if (this.authLine == null) {
            if (Statics.needSessionToken.get()) { // normal, nothing to be done
            } else if (isLastAuthRecent() || this.lastAuthSentStamp == 0L || Statics.sessionTokenObject.isRecent()) { // normal, no need to print
//                logger.info("[{}]returning null line in getAuthLine: {}", client.id, timeSinceLastAuth());
            } else {
                logger.error("[{}]returning null line in getAuthLine: {}", this.client.id, timeSinceLastAuth());
            }
        } else {
            this.authLine = null;
            if (this.linesList.isEmpty()) {
                this.bufferNotEmpty.set(false);
            }
        }
        if (result != null) {
            lastAuthSentStamp();
        }

        return result;
    }

    private synchronized void lastAuthSentStamp() {
        this.lastAuthSentStamp = System.currentTimeMillis();
    }

    private synchronized boolean isLastAuthRecent() {
        return isLastAuthRecent(5_000L); // default
    }

    private synchronized boolean isLastAuthRecent(final long recentPeriod) {
        return timeSinceLastAuth() <= recentPeriod;
    }

    private synchronized long timeSinceLastAuth() {
        return System.currentTimeMillis() - this.lastAuthSentStamp;
    }

    public synchronized void addLine(final String line) {
        addLine(line, false);
    }

    public synchronized void addLine(final String line, final boolean addFirst) {
        if (addFirst) {
            this.linesList.addFirst(line);
        } else {
            this.linesList.add(line);
        }
        this.bufferNotEmpty.set(true);
    }

    private synchronized String pollLine() {
        final String result;

        if (this.client.isAuth.get()) {
            if (this.authLine == null) {
                result = this.linesList.poll();
            } else {
                logger.error("[{}]client isAuth and I have authLine in writerThread: {} {} {} {} {}", this.client.id, this.client.isStopped.get(), this.client.socketIsConnected.get(), this.client.streamIsConnected.get(), this.client.isAuth.get(),
                             this.authLine);
                result = getAuthLine(); // I'll send the authLine anyway
            }
        } else {
            result = getAuthLine();
            if (result == null) {
                if (Statics.needSessionToken.get()) { // normal, nothing to be done
                } else if (isLastAuthRecent() || this.lastAuthSentStamp == 0L || Statics.sessionTokenObject.isRecent()) { // normal, no need to print
//                    logger.info("[{}]client is not auth and I have don't have authLine in writerThread: {} {} {} {} {}", client.id, client.isStopped.get(), client.socketIsConnected.get(), client.streamIsConnected.get(), client.isAuth.get(),
//                                timeSinceLastAuth());
                } else {
                    logger.error("[{}]client is not auth and I have don't have authLine in writerThread: {} {} {} {} {}", this.client.id, this.client.isStopped.get(), this.client.socketIsConnected.get(), this.client.streamIsConnected.get(), this.client.isAuth.get(),
                                 timeSinceLastAuth());
                }
            }
        }

        if (this.linesList.isEmpty()) {
            this.bufferNotEmpty.set(false);
        }

        return result;
    }

    private synchronized void sendLine(final String line) {
        if (line != null) {
            try {
                this.bufferedWriter.write(line + CRLF);
                this.bufferedWriter.flush();
//                logger.info("[{}]sent line: {}", client.id, line);
            } catch (IOException e) {
                logger.error("[{}]Error sending to socket", this.client.id, e);
                //Forcibly break the socket which will then trigger a reconnect if configured
                this.client.setStreamError(true);
                if (isAuthLine(line)) { // I won't resend auth line, auth is retried anyway
                } else {
                    addLine(line, true); // resend later
                }
            }
        } else {
            if (Statics.needSessionToken.get()) { // normal, nothing to be done
            } else if (isLastAuthRecent() || this.lastAuthSentStamp == 0L || Statics.sessionTokenObject.isRecent()) { // normal, no need to print
//                logger.info("[{}]won't send null line in stream writerThread: {}", client.id, timeSinceLastAuth());
            } else {
                logger.error("[{}]won't send null line in stream writerThread: {}", this.client.id, timeSinceLastAuth());
            }
        }
    }

    private synchronized boolean isAuthLine(final String line) {
        // ,"op":"authentication","appKey":
        return line.contains(",\"op\":\"authentication\",\"appKey\":");
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (this.client.streamIsConnected.get() && !this.client.streamError.get()) {
                    if (this.bufferNotEmpty.get()) {
                        final String line = pollLine();
                        sendLine(line);
                        if (!this.client.isAuth.get()) {
                            Generic.threadSleepSegmented(500L, 10L, this.client.isAuth, Statics.mustStop);
                        }
                    }
                } else {
                    Generic.threadSleepSegmented(500L, 10L, this.client.streamIsConnected, Statics.mustStop);
                }

                Generic.threadSleepSegmented(5_000L, 10L, this.bufferNotEmpty, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("[{}]STRANGE ERROR inside Client writerThread loop", this.client.id, throwable);
            }
        } // end while
    }
}
