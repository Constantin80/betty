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
    private LinkedList<String> linesList = new LinkedList<>();
    public final AtomicBoolean bufferNotEmpty = new AtomicBoolean();

    ReaderThread(final Client client) {
        this.client = client;
    }

    synchronized void setBufferedReader(final BufferedReader bufferedReader) {
        if (this.bufferedReader != null) {
            try {
                this.bufferedReader.close();
            } catch (IOException e) {
                logger.error("[{}]error while closing buffer in setBufferedReader", client.id, e);
            }
        }
        this.bufferedReader = bufferedReader;
    }

    private synchronized void addLine(final String line) {
        linesList.add(line);
        bufferNotEmpty.set(true);
//        logger.info("[{}]added line: {}", client.id, line);
    }

    public synchronized String pollLine() {
        final String result = linesList.poll();

        if (linesList.isEmpty()) {
            bufferNotEmpty.set(false);
        }

        logger.info("client[{}] polled line: {}", client.id, result);

        return result;
    }

    @Override
    public void run() {
        String line;
        while (!Statics.mustStop.get()) {
            try {
                if (client.socketIsConnected.get() && !client.streamError.get()) {
                    try {
                        line = bufferedReader.readLine();
                        if (line == null) {
//                        throw new IOException("Socket closed - EOF");
                            if (!Statics.mustStop.get()) {
                                if (Statics.needSessionToken.get() || Statics.sessionTokenObject.isRecent()) {
                                    logger.info("[{}]line null in streamReaderThread", client.id);
                                } else {
                                    logger.error("[{}]line null in streamReaderThread", client.id);
                                }
                            }
                            client.setStreamError(true);
                        } else {
                            addLine(line);
//                            processor.receiveLine(line);
                        }
                    } catch (IOException e) {
                        if (!Statics.mustStop.get()) {
                            if (client.socketIsConnected.get()) {
                                logger.error("[{}]IOException in streamReaderThread", client.id, e);
                            } else {
                                logger.info("[{}]IOException in disconnected streamReaderThread: {}", client.id, e);
                            }
                        }
                        client.setStreamError(true);
//                    disconnected();
                    }
                } else {
                    Generic.threadSleepSegmented(500L, 10L, client.socketIsConnected, Statics.mustStop);
                }
            } catch (Throwable throwable) {
                logger.error("[{}]STRANGE ERROR inside Client readerThread loop", client.id, throwable);
            }
        } // end while
    }
}
