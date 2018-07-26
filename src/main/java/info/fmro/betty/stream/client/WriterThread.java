package info.fmro.betty.stream.client;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
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
    private String authLine;
    private long lastAuthSentStamp;
    private LinkedList<String> linesList = new LinkedList<>();
    public final AtomicBoolean bufferNotEmpty = new AtomicBoolean();

    WriterThread(Client client) {
        this.client = client;
    }

    synchronized void setBufferedWriter(BufferedWriter bufferedWriter) {
        if (this.bufferedWriter != null) {
            try {
                this.bufferedWriter.close();
            } catch (IOException e) {
                logger.error("error while closing buffer in setBufferedWriter", e);
            }
        }
        this.bufferedWriter = bufferedWriter;
    }

    public synchronized void setAuthLine(String line) {
        if (authLine != null) {
            logger.error("previous authLine not null in setAuthLine: {} {}", authLine, line);
        } else if (line != null) {
            authLine = line;
            bufferNotEmpty.set(true);
        } else {
            logger.error("trying to set null line in setAuthLine");
        }
    }

    private synchronized String getAuthLine() {
        final String result = authLine;
        if (authLine == null) {
            if (isLastAuthRecent() || lastAuthSentStamp == 0L) {
                logger.info("returning null line in getAuthLine: {}", timeSinceLastAuth());
            } else {
                logger.error("returning null line in getAuthLine: {}", timeSinceLastAuth());
            }
        } else {
            authLine = null;
            if (linesList.isEmpty()) {
                bufferNotEmpty.set(false);
            }
        }
        if (result != null) {
            lastAuthSentStamp();
        }

        return result;
    }

    private synchronized void lastAuthSentStamp() {
        lastAuthSentStamp = System.currentTimeMillis();
    }

    private synchronized boolean isLastAuthRecent() {
        return isLastAuthRecent(1_000L); // default
    }

    private synchronized boolean isLastAuthRecent(long recentPeriod) {
        return timeSinceLastAuth() <= recentPeriod;
    }

    private synchronized long timeSinceLastAuth() {
        return System.currentTimeMillis() - lastAuthSentStamp;
    }

    public synchronized void addLine(String line) {
        linesList.add(line);
        bufferNotEmpty.set(true);
    }

    private synchronized String pollLine() {
        final String result;

        if (client.isAuth.get()) {
            if (authLine == null) {
                result = linesList.poll();
            } else {
                logger.error("client isAuth and I have authLine in writerThread: {} {} {} {} {}", client.isStopped.get(), client.socketIsConnected.get(), client.streamIsConnected.get(),
                             client.isAuth.get(), authLine);
                result = getAuthLine(); // I'll send the authLine anyway
            }
        } else {
            result = getAuthLine();
            if (result == null) {
                if (isLastAuthRecent() || lastAuthSentStamp == 0L) {
                    logger.info("client is not auth and I have don't have authLine in writerThread: {} {} {} {} {}", client.isStopped.get(), client.socketIsConnected.get(), client.streamIsConnected.get(),
                                client.isAuth.get(), timeSinceLastAuth());
                } else {
                    logger.error("client is not auth and I have don't have authLine in writerThread: {} {} {} {} {}", client.isStopped.get(), client.socketIsConnected.get(), client.streamIsConnected.get(),
                                 client.isAuth.get(), timeSinceLastAuth());
                }
            }
        }

        if (linesList.isEmpty()) {
            bufferNotEmpty.set(false);
        }

        return result;
    }

    private synchronized void sendLine(String line) {
        if (line != null) {
            try {
                bufferedWriter.write(line + CRLF);
                bufferedWriter.flush();
//                logger.info("sent line: {}", line);
            } catch (IOException e) {
                logger.error("Error sending to socket", e);
                //Forcibly break the socket which will then trigger a reconnect if configured
                client.setStreamError(true);
            }
        } else {
            if (isLastAuthRecent() || lastAuthSentStamp == 0L) {
                logger.info("won't send null line in stream writerThread: {}", timeSinceLastAuth());
            } else {
                logger.error("won't send null line in stream writerThread: {}", timeSinceLastAuth());
            }
        }
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (client.streamIsConnected.get() && !client.streamError.get()) {
                    if (bufferNotEmpty.get()) {
                        final String line = pollLine();
                        sendLine(line);
                        if (!client.isAuth.get()) {
                            Generic.threadSleepSegmented(500L, 10L, client.isAuth, Statics.mustStop);
                        }
                    }
                } else {
                    Generic.threadSleepSegmented(500L, 10L, client.streamIsConnected, Statics.mustStop);
                }

                Generic.threadSleepSegmented(5_000L, 10L, bufferNotEmpty, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside Client writerThread loop", throwable);
            }
        } // end while
    }
}