package info.fmro.betty.stream.client;

import info.fmro.betty.main.GetLiveMarketsThread;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.definitions.AuthenticationMessage;
import info.fmro.betty.stream.definitions.FilterFlag;
import info.fmro.betty.stream.definitions.MarketDataFilter;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// Order for synchronization: 1:Client, 2:Processor, 3:ReaderThread, WriterThread
public class Client
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public final int id;
    private final String hostName;
    private final int port;

    public final RequestResponseProcessor processor;
    public final ReaderThread readerThread;
    public final WriterThread writerThread;
//    private final ScheduledExecutorService keepAliveTimer;

    public final long timeout = 30L * 1000L;
    public final long keepAliveHeartbeat = 60L * 60L * 1000L;

    public final AtomicBoolean isStopped = new AtomicBoolean(true);
    public final AtomicBoolean socketIsConnected = new AtomicBoolean();
    public final AtomicBoolean streamIsConnected = new AtomicBoolean();
    public final AtomicBoolean isAuth = new AtomicBoolean();

    public final AtomicLong startedStamp = new AtomicLong();
    public final AtomicBoolean streamError = new AtomicBoolean();

    private Socket socket;

    public final AtomicLong conflateMs = new AtomicLong();
    public final AtomicLong heartbeatMs = new AtomicLong();
    public final MarketDataFilter marketDataFilter = new MarketDataFilter(FilterFlag.EX_ALL_OFFERS, FilterFlag.EX_TRADED, FilterFlag.EX_TRADED_VOL, FilterFlag.EX_LTP, FilterFlag.EX_MARKET_DEF);

    public Client(final int id, final String hostName, final int port) {
        this.id = id;
        this.hostName = hostName;
        this.port = port;

        processor = new RequestResponseProcessor(this);
        readerThread = new ReaderThread(this);
        writerThread = new WriterThread(this);
//        keepAliveTimer = Executors.newSingleThreadScheduledExecutor();

//        setChangeHandler(this);
    }

    /**
     * ClientCache is abstracted via this hook (enabling replacement)
     * <p>
     * //     * @param changeHandler
     */
//    private synchronized void setChangeHandler(ChangeMessageHandler changeHandler) {
//        processor.setChangeHandler(changeHandler);
//    }

    void setStreamError(final boolean newValue) { // not synchronized, as it's not needed and I have sleep inside, and synchronization would lead to deadlocks
        if (newValue == false) {
            streamError.set(newValue);
        } else {
            if (System.currentTimeMillis() > startedStamp.get() + 500L) {
                streamError.set(newValue);
            } else { // setReaderError might be related to the start itself, maybe error was fixed by the start; nothing to be done
            }
            Generic.threadSleepSegmented(500L, 50L, Statics.mustStop);
        }
    }

    private synchronized void startThreads() {
        if (processor.isAlive()) {
            logger.error("[{}]processor thread already alive", this.id);
        } else {
            processor.start();
        }
        if (readerThread.isAlive()) {
            logger.error("[{}]reader thread already alive", this.id);
        } else {
            readerThread.start();
        }
        if (writerThread.isAlive()) {
            logger.error("[{}]writer thread already alive", this.id);
        } else {
            writerThread.start();
        }
//        keepAliveTimer.scheduleAtFixedRate(processor::keepAliveCheck, timeout, timeout, TimeUnit.MILLISECONDS);
    }

    private synchronized boolean startClient() {
        final boolean attemptedToStart;
        if (writerThread.bufferNotEmpty.get() || processor.hasValidHandler()) {
            if (isStopped.get()) {
                do {
                    connectSocket();

                    connectAuthenticateAndResubscribe();

                    if (socketIsConnected.get() && streamIsConnected.get() && isAuth.get()) {
                        isStopped.set(false);
                        setStreamError(false);
                    } else {
                        if (!Statics.mustStop.get()) {
                            if (Statics.sessionTokenObject.isRecent()) {
                                logger.info("something went wrong during startClient, disconnecting[{}]: {} {} {}", this.id, socketIsConnected.get(), streamIsConnected.get(), isAuth.get());
                            } else {
                                logger.error("something went wrong during startClient, disconnecting[{}]: {} {} {}", this.id, socketIsConnected.get(), streamIsConnected.get(), isAuth.get());
                            }
                        } else { // normal behavior to get this error after stop
                        }
                        disconnect();
                        Generic.threadSleepSegmented(10_000L, 100L, Statics.mustStop);
                    }
                } while (isStopped.get() && !Statics.mustStop.get());
            } else {
                logger.error("[{}]trying to start an already started client", this.id);
            }
            this.startedStamp.set(System.currentTimeMillis());
            attemptedToStart = true;
        } else { // no command in buffer and no handler to resub, won't attempt to start
            attemptedToStart = false;
        }
        return attemptedToStart;
    }

//    private synchronized void setSocketIsConnected(boolean newValue) {
//        externalSocketIsConnected.set( newValue);
//    }

    private synchronized void connectSocket() {
        if (!socketIsConnected.get()) {
            try {
                logger.info("[{}]ESAClient: Opening socket to: {}:{}", this.id, hostName, port);
                if (socket != null && !socket.isClosed()) {
                    logger.error("[{}]previous socket not closed in connectSocket, disconnecting", this.id);
                    disconnect();
                }
                socket = createSocket(hostName, port);
                socket.setReceiveBufferSize(1024 * 1000 * 2); //shaves about 20s off firehose image.

                readerThread.setBufferedReader(new BufferedReader(new InputStreamReader(socket.getInputStream())));
                writerThread.setBufferedWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

                socketIsConnected.set(true);
                setStreamError(false); // I need to reset the error here as well, to get the readerThread going
            } catch (IOException e) {
                logger.error("[{}]Failed to connect streamClient", this.id, e);
            }
        } else {
            logger.error("[{}]trying to connect a socket that is already connected", this.id);
        }
    }

    private synchronized Socket createSocket(final String hostName, final int port) {
        if (port != 443) {
            logger.error("[{}]streamClient trying to connect on a port other than 443, I'll still use SSL", this.id);
        }

        final SocketFactory factory = SSLSocketFactory.getDefault();
        SSLSocket newSocket = null;
        while (newSocket == null && !Statics.mustStop.get()) {
            try {
                newSocket = (SSLSocket) factory.createSocket(hostName, port);
                newSocket.setSoTimeout((int) timeout);
                newSocket.startHandshake();
            } catch (IOException e) {
                logger.error("[{}]IOException in streamClient.createSocket", this.id, e);
            }
            if (newSocket == null) {
                Generic.threadSleepSegmented(5_000L, 100L, Statics.mustStop);
            }
        } // end while

        return newSocket;
    }

    private synchronized void connectAuthenticateAndResubscribe() {
        if (streamIsConnected.get()) {
            logger.error("[{}]connecting a stream that is already connected", this.id);
        }
        Generic.threadSleepSegmented(timeout, 50L, streamIsConnected, Statics.mustStop);

//        ConnectionMessage result = null;
//        try {
//            result = processor.getConnectionMessage().get(timeout, TimeUnit.MILLISECONDS);
//        } catch (InterruptedException | ExecutionException | TimeoutException e) {
//            logger.error("[{}]Exception in streamClient.connectAuthenticateAndResubscribe", this.id, e);
//        }

        if (!streamIsConnected.get()) { //timeout
            if (!Statics.mustStop.get()) {
                logger.error("[{}]No connection message in streamClient.connectAuthenticateAndResubscribe", this.id);
            }
        } else {
//            streamIsConnected.set(true);
            authenticateAndResubscribe();
        }
    }

    private synchronized void authenticateAndResubscribe() {
        if (Statics.needSessionToken.get()) {
            GetLiveMarketsThread.waitForSessionToken("stream client");
        }

        if (!Statics.mustStop.get()) {
            if (isAuth.get()) {
                logger.error("[{}]auth on a stream that is already auth", this.id);
            }

//            processor.authAndResubscribe();
            final AuthenticationMessage authenticationMessage = new AuthenticationMessage();
            authenticationMessage.setAppKey(Statics.appKey.get());
            authenticationMessage.setSession(Statics.sessionTokenObject.getSessionToken());
//        ClientCommands.waitFor(this, processor.authenticate(authenticationMessage));

            processor.authenticate(authenticationMessage);
            Generic.threadSleepSegmented(timeout, 10L, isAuth, Statics.mustStop);

            if (socketIsConnected.get() && streamIsConnected.get() && isAuth.get()) {
                processor.resubscribe();
                isAuth.set(true); // after resubscribe, else I might get some racing condition bugs
            } else {
                if (!Statics.mustStop.get()) {
                    if (Statics.sessionTokenObject.isRecent()) {
                        logger.info("something went wrong in streamClient before auth[{}]: {} {}", id, socketIsConnected.get(), streamIsConnected.get());
                    } else {
                        logger.error("something went wrong in streamClient before auth[{}]: {} {}", id, socketIsConnected.get(), streamIsConnected.get());
                    }
                } else { // normal behavior to get this error after stop
                }
            }
        } else { // program stopping, won't try to auth
        }
    }

    private synchronized void shutdownClient() {
//        shutdownKeepAliveTimer();

        disconnect(true);

        processor.stopped();
    }

    private synchronized void disconnect() {
        disconnect(false);
    }

    private synchronized void disconnect(final boolean stoppingClient) {
        if (socket != null) {
            if (socket.isClosed()) {
                if (stoppingClient) {
                    logger.info("[{}]socket already closed in streamClient.disconnect during stopping", this.id);
                } else {
                    logger.error("[{}]socket already closed in streamClient.disconnect", this.id);
                }
            } else {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error("[{}]Exception in close socket in streamClient.disconnect", this.id, e);
                }
            }

            socket = null;
        } else {
            if (stoppingClient) { // quite common, shouldn't print message
//                logger.info("[{}]tried to disconnect a null socket during stopping", this.id);
            } else {
                logger.error("[{}]tried to disconnect a null socket", this.id);
            }
        }

        isStopped.set(true);
        socketIsConnected.set(false);
        streamIsConnected.set(false);
        isAuth.set(false);

        processor.disconnected();
    }

//    private synchronized void shutdownKeepAliveTimer() {
//        if (keepAliveTimer != null) {
//            keepAliveTimer.shutdown();
//        } else {
//            logger.info("[{}]tried to shutdown null keepAliveTimer", this.id);
//        }
//    }

    @Override
    public void run() {
        startThreads(); // used only once

        while (!Statics.mustStop.get()) {
            try {
                if (isStopped.get()) {
                    final boolean hasAttemptedStart = startClient();
                    if (!hasAttemptedStart) {
                        Generic.threadSleepSegmented(5_000L, 10L, writerThread.bufferNotEmpty, Statics.mustStop);
                    }
                } else if (streamError.get()) {
                    disconnect(); // can attempt restart on the next loop iteration
//                    if (!startClient()) {
//                        Generic.threadSleepSegmented(5_000L, 10L, writerThread.bufferNotEmpty, Statics.mustStop);
//                    }
                }

                Generic.threadSleepSegmented(5_000L, 10L, isStopped, streamError, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("[{}]STRANGE ERROR inside Client loop", this.id, throwable);
            }
        } // end while

        try {
            if (readerThread != null) {
                if (readerThread.isAlive()) {
                    logger.info("[{}]joining readerThread", this.id);
                    readerThread.join();
                }
            } else {
                logger.error("[{}]null readerThread during stream end", this.id);
            }
            if (writerThread != null) {
                if (writerThread.isAlive()) {
                    logger.info("[{}]joining writerThread", this.id);
                    writerThread.join();
                }
            } else {
                logger.error("[{}]null writerThread during stream end", this.id);
            }
        } catch (InterruptedException e) {
            logger.error("[{}]interrupted exception at the end of clientThread", this.id, e);
        }

        shutdownClient(); // only called once

//        if (keepAliveTimer != null) {
//            try {
//                if (!keepAliveTimer.awaitTermination(1L, TimeUnit.MINUTES)) {
//                    logger.error("[{}]keepAliveTimer hanged", this.id);
//                    final List<Runnable> runnableList = keepAliveTimer.shutdownNow();
//                    if (!runnableList.isEmpty()) {
//                        logger.error("[{}]keepAliveTimer not commenced: {}", this.id, runnableList.size());
//                    }
//                }
//            } catch (InterruptedException e) {
//                logger.error("[{}]InterruptedException during awaitTermination during stream end", this.id, e);
//            }
//        } else {
//            logger.error("[{}]keepAliveTimer null during stream end", this.id);
//        }

        logger.info("[{}]streamClient thread ends", this.id);
    }
}
