package info.fmro.betty.stream.client;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

class ReaderThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(ReaderThread.class);
    private final Client client;
    @SuppressWarnings("FieldHasSetterButNoGetter")
    private BufferedReader bufferedReader;
    private final LinkedList<String> linesList = new LinkedList<>();
    @SuppressWarnings("PackageVisibleField")
    final AtomicBoolean bufferNotEmpty = new AtomicBoolean();

    ReaderThread(final Client client) {
        super();
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

    synchronized String pollLine() {
        final String result = this.linesList.poll();

        if (this.linesList.isEmpty()) {
            this.bufferNotEmpty.set(false);
        }

        logger.info("client[{}] polled line: {}", this.client.id, result);

        return result;
    }

    private String readLine()
            throws IOException { // this can't be synchronized, as it contains a blocking method, and it would block all the other synchronized calls
        return this.bufferedReader.readLine();
    }

    @SuppressWarnings("OverlyNestedMethod")
    @Override
    public void run() {
        String line;
        while (!Statics.mustStop.get()) {
            try {
                if (this.client.socketIsConnected.get() && !this.client.streamError.get()) {
                    //noinspection NestedTryStatement
                    try {
                        line = this.readLine();
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
