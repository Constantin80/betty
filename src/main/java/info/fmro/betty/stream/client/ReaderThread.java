package info.fmro.betty.stream.client;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReaderThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ReaderThread.class);
    private final Client client;
    private BufferedReader bufferedReader;
    private final LinkedList<String> linesList = new LinkedList<>();
    public final AtomicBoolean bufferNotEmpty = new AtomicBoolean();

    ReaderThread(final Client client) {
        this.client = client;
    }

    synchronized void setBufferedReader(final BufferedReader bufferedReader) {
        if (this.bufferedReader != null) {
            try {
                this.bufferedReader.close();
            } catch (IOException e) {
                logger.error("[{}]error while closing buffer in setBufferedReader", this.client.id, e);
            }
        }
        this.bufferedReader = bufferedReader;
    }

    private synchronized void addLine(final String line) {
        this.linesList.add(line);
        this.bufferNotEmpty.set(true);
//        logger.info("[{}]added line: {}", client.id, line);
    }

    public synchronized String pollLine() {
        final String result = this.linesList.poll();

        if (this.linesList.isEmpty()) {
            this.bufferNotEmpty.set(false);
        }

        logger.info("client[{}] polled line: {}", this.client.id, result);

        return result;
    }

    @Override
    public void run() {
        String line;
        while (!Statics.mustStop.get()) {
            try {
                if (this.client.socketIsConnected.get() && !this.client.streamError.get()) {
                    try {
                        line = this.bufferedReader.readLine();
                        if (line == null) {
//                        throw new IOException("Socket closed - EOF");
                            if (!Statics.mustStop.get()) {
                                if (Statics.needSessionToken.get() || Statics.sessionTokenObject.isRecent()) {
                                    logger.info("[{}]line null in streamReaderThread", this.client.id);
                                } else {
                                    logger.error("[{}]line null in streamReaderThread", this.client.id);
                                }
                            }
                            this.client.setStreamError(true);
                        } else {
                            addLine(line);
//                            processor.receiveLine(line);
                        }
                    } catch (IOException e) {
                        if (!Statics.mustStop.get()) {
                            if (this.client.socketIsConnected.get()) {
                                logger.error("[{}]IOException in streamReaderThread", this.client.id, e);
                            } else {
                                logger.info("[{}]IOException in disconnected streamReaderThread: {}", this.client.id, e);
                            }
                        }
                        this.client.setStreamError(true);
//                    disconnected();
                    }
                } else {
                    Generic.threadSleepSegmented(500L, 10L, this.client.socketIsConnected, Statics.mustStop);
                }
            } catch (Throwable throwable) {
                logger.error("[{}]STRANGE ERROR inside Client readerThread loop", this.client.id, throwable);
            }
        } // end while
    }
}
