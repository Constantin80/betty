package info.fmro.betty.stream.client;

import info.fmro.betty.main.GetLiveMarketsThread;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.definitions.AuthenticationMessage;
import info.fmro.betty.stream.definitions.ConnectionMessage;
import info.fmro.betty.stream.definitions.HeartbeatMessage;
import info.fmro.betty.stream.definitions.MarketDataFilter;
import info.fmro.betty.stream.definitions.MarketSubscriptionMessage;
import info.fmro.betty.stream.definitions.OrderSubscriptionMessage;
import info.fmro.betty.stream.protocol.ConnectionStatus;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

// Order for synchronization: 1:Client, 2:Processor, 3:ReaderThread, WriterThread
public class Client
        extends Thread {
    // errorMessage=You have exceeded your max connection limit which is: 10 connection(s).You currently have: 11 active connection(s).
    // this errorMessage seem to trigger much later than 11 connections; triggers with 70, not with 68; started 23, sleep 3 min, start 5 more, didn't trigger
    public static final int nClients = 32;
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    public final int id;
    private final String hostName;
    private final int port;

    public final RequestResponseProcessor processor;
    public final ReaderThread readerThread;
    public final WriterThread writerThread;
    private final ScheduledExecutorService keepAliveTimer;

    public final long timeout = 30L * 1000L;
    private final long keepAliveHeartbeat = 60L * 60L * 1000L;

    public final AtomicBoolean isStopped = new AtomicBoolean(true);
    public final AtomicBoolean socketIsConnected = new AtomicBoolean();
    public final AtomicBoolean streamIsConnected = new AtomicBoolean();
    public final AtomicBoolean isAuth = new AtomicBoolean();

    public final AtomicLong startedStamp = new AtomicLong();
    public final AtomicBoolean streamError = new AtomicBoolean();

    private Socket socket;

    public final AtomicLong conflateMs = new AtomicLong();
    public final AtomicLong heartbeatMs = new AtomicLong();
    public final MarketDataFilter marketDataFilter = null; // not used

    public Client(int id, String hostName, int port) {
        this.id = id;
        this.hostName = hostName;
        this.port = port;

        processor = new RequestResponseProcessor(this);
        readerThread = new ReaderThread(this);
        writerThread = new WriterThread(this);
        keepAliveTimer = Executors.newSingleThreadScheduledExecutor();

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

    void setStreamError(boolean newValue) { // not synchronized, as it's not needed and I have sleep inside, and synchronization would lead to deadlocks
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
            logger.error("processor thread already alive");
        } else {
            processor.start();
        }
        if (readerThread.isAlive()) {
            logger.error("reader thread already alive");
        } else {
            readerThread.start();
        }
        if (writerThread.isAlive()) {
            logger.error("writer thread already alive");
        } else {
            writerThread.start();
        }
        keepAliveTimer.scheduleAtFixedRate(this::keepAliveCheck, timeout, timeout, TimeUnit.MILLISECONDS);
    }

    private synchronized void startClient() {
        if (isStopped.get()) {
            do {
                connectSocket();

                connectAuthenticateAndResubscribe();

                if (socketIsConnected.get() && streamIsConnected.get() && isAuth.get()) {
                    isStopped.set(false);
                    setStreamError(false);
                } else {
                    logger.error("something went wrong during startClient, disconnecting: {} {} {}", socketIsConnected.get(), streamIsConnected.get(), isAuth.get());
                    disconnect();
                    Generic.threadSleepSegmented(10_000L, 100L, Statics.mustStop);
                }
            } while (isStopped.get() && !Statics.mustStop.get());
        } else {
            logger.error("trying to start an already started client");
        }
        this.startedStamp.set(System.currentTimeMillis());
    }

//    private synchronized void setSocketIsConnected(boolean newValue) {
//        externalSocketIsConnected.set( newValue);
//    }

    private synchronized void connectSocket() {
        if (!socketIsConnected.get()) {
            try {
                logger.info("ESAClient: Opening socket to: {}:{}", hostName, port);
                if (socket != null && !socket.isClosed()) {
                    logger.error("previous socket not closed in connectSocket, disconnecting");
                    disconnect();
                }
                socket = createSocket(hostName, port);
                socket.setReceiveBufferSize(1024 * 1000 * 2); //shaves about 20s off firehose image.

                readerThread.setBufferedReader(new BufferedReader(new InputStreamReader(socket.getInputStream())));
                writerThread.setBufferedWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));

                socketIsConnected.set(true);
            } catch (IOException e) {
                logger.error("Failed to connect streamClient", e);
            }
        } else {
            logger.error("trying to connect a socket that is already connected");
        }
    }

    private synchronized Socket createSocket(String hostName, int port) {
        if (port != 443) {
            logger.error("streamClient trying to connect on a port other than 443, I'll still use SSL");
        }

        final SocketFactory factory = SSLSocketFactory.getDefault();
        SSLSocket newSocket = null;
        while (newSocket == null && !Statics.mustStop.get()) {
            try {
                newSocket = (SSLSocket) factory.createSocket(hostName, port);
                newSocket.setSoTimeout((int) timeout);
                newSocket.startHandshake();
            } catch (IOException e) {
                logger.error("IOException in streamClient.createSocket", e);
            }
            if (newSocket == null) {
                Generic.threadSleepSegmented(5_000L, 100L, Statics.mustStop);
            }
        } // end while

        return newSocket;
    }

    private synchronized void connectAuthenticateAndResubscribe() {
        if (streamIsConnected.get()) {
            logger.error("connecting a stream that is already connected");
        }

        ConnectionMessage result = null;
        try {
            result = processor.getConnectionMessage().get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Exception in streamClient.connectAuthenticateAndResubscribe", e);
        }

        if (result == null) { //timeout
            logger.error("No connection message in streamClient.connectAuthenticateAndResubscribe");
        } else {
            streamIsConnected.set(true);
            authenticateAndResubscribe();
        }
    }

    private synchronized void authenticateAndResubscribe() {
        if (Statics.needSessionToken.get()) {
            GetLiveMarketsThread.waitForSessionToken("stream client");
        }

        if (isAuth.get()) {
            logger.error("auth on a stream that is already auth");
        }

        final AuthenticationMessage authenticationMessage = new AuthenticationMessage();
        authenticationMessage.setAppKey(Statics.appKey.get());
        authenticationMessage.setSession(Statics.sessionTokenObject.getSessionToken());
        ClientCommands.waitFor(this, processor.authenticate(authenticationMessage));
        if (socketIsConnected.get() && streamIsConnected.get()) {
            isAuth.set(true);
            resubscribe();
        } else {
            logger.error("something went wrong in streamClient before auth: {} {}", socketIsConnected.get(), streamIsConnected);
        }
    }

    private synchronized void resubscribe() {
        //Resub markets
        final MarketSubscriptionMessage marketSubscription = processor.getMarketResubscribeMessage();
        if (marketSubscription != null) {
            logger.info("Resubscribe to market subscription.");
            ClientCommands.subscribeMarkets(this, marketSubscription);
        }

        //Resub orders
        final OrderSubscriptionMessage orderSubscription = processor.getOrderResubscribeMessage();
        if (orderSubscription != null) {
            logger.info("Resubscribe to order subscription.");
            ClientCommands.subscribeOrders(this, orderSubscription);
        }
    }

    private synchronized void shutdownClient() {
        shutdownKeepAliveTimer();

        disconnect(true);

        processor.stopped();
    }

    private synchronized void disconnect() {
        disconnect(false);
    }

    private synchronized void disconnect(boolean stoppingClient) {
        if (socket != null) {
            if (socket.isClosed()) {
                if (stoppingClient) {
                    logger.info("socket already closed in streamClient.disconnect during stopping");
                } else {
                    logger.error("socket already closed in streamClient.disconnect");
                }
            } else {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.error("Exception in close socket in streamClient.disconnect", e);
                }
            }

            socket = null;
        } else {
            if (stoppingClient) {
                logger.info("tried to disconnect a null socket during stopping");
            } else {
                logger.error("tried to disconnect a null socket");
            }
        }

        isStopped.set(true);
        socketIsConnected.set(false);
        streamIsConnected.set(false);
        isAuth.set(false);

        processor.disconnected();
    }

    private synchronized void shutdownKeepAliveTimer() {
        if (keepAliveTimer != null) {
            keepAliveTimer.shutdown();
        } else {
            logger.info("tried to shutdown null keepAliveTimer");
        }
    }

    private synchronized void keepAliveCheck() {
        if (processor.getStatus() == ConnectionStatus.SUBSCRIBED) { //connection looks up
            if (processor.getLastRequestTime() + keepAliveHeartbeat < System.currentTimeMillis()) { //send a heartbeat to server to keep networks open
                logger.info("Last Request Time is longer than {}: Sending Keep Alive Heartbeat", keepAliveHeartbeat);
                heartbeat();
            } else if (processor.getLastResponseTime() + timeout < System.currentTimeMillis()) {
                logger.info("Last Response Time is longer than timeout {}: Sending Keep Alive Heartbeat", timeout);
                heartbeat();
            }
        }
    }

    private synchronized void heartbeat() {
        ClientCommands.waitFor(this, processor.heartbeat(new HeartbeatMessage()));
    }

    //todo errors in out.txt; after I get the stream going, find a way to get above 1k markets, using 2 or more clients; get all markets list, probably from events, and test this split market hose
    @Override
    public void run() {
        startThreads(); // used only once

        while (!Statics.mustStop.get()) {
            try {
                if (isStopped.get()) {
                    if (writerThread.bufferNotEmpty.get()) {
                        startClient();
                    } else {
                        Generic.threadSleepSegmented(5_000L, 10L, writerThread.bufferNotEmpty, Statics.mustStop);
                    }
                } else if (streamError.get()) {
                    disconnect();
                    startClient();
                }

                Generic.threadSleepSegmented(5_000L, 10L, isStopped, streamError, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside Client loop", throwable);
            }
        } // end while

        try {
            if (readerThread != null) {
                if (readerThread.isAlive()) {
                    logger.info("joining readerThread");
                    readerThread.join();
                }
            } else {
                logger.error("null readerThread during stream end");
            }
            if (writerThread != null) {
                if (writerThread.isAlive()) {
                    logger.info("joining writerThread");
                    writerThread.join();
                }
            } else {
                logger.error("null writerThread during stream end");
            }
        } catch (InterruptedException e) {
            logger.error("interrupted exception at the end of clientThread", e);
        }

        shutdownClient(); // only called once

        if (keepAliveTimer != null) {
            try {
                if (!keepAliveTimer.awaitTermination(1L, TimeUnit.MINUTES)) {
                    logger.error("keepAliveTimer hanged");
                    final List<Runnable> runnableList = keepAliveTimer.shutdownNow();
                    if (!runnableList.isEmpty()) {
                        logger.error("keepAliveTimer not commenced: {}", runnableList.size());
                    }
                }
            } catch (InterruptedException e) {
                logger.error("InterruptedException during awaitTermination during stream end", e);
            }
        } else {
            logger.error("keepAliveTimer null during stream end");
        }

        logger.info("streamClient thread ends");
    }
}
