package info.fmro.betty.stream.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.definitions.AuthenticationMessage;
import info.fmro.betty.stream.definitions.ConnectionMessage;
import info.fmro.betty.stream.definitions.HeartbeatMessage;
import info.fmro.betty.stream.definitions.MarketChange;
import info.fmro.betty.stream.definitions.MarketChangeMessage;
import info.fmro.betty.stream.definitions.MarketSubscriptionMessage;
import info.fmro.betty.stream.definitions.OrderChangeMessage;
import info.fmro.betty.stream.definitions.OrderMarketChange;
import info.fmro.betty.stream.definitions.OrderSubscriptionMessage;
import info.fmro.betty.stream.definitions.RequestMessage;
import info.fmro.betty.stream.definitions.RequestOperationType;
import info.fmro.betty.stream.definitions.ResponseMessage;
import info.fmro.betty.stream.definitions.StatusMessage;
import info.fmro.betty.stream.protocol.ChangeMessage;
import info.fmro.betty.stream.protocol.ChangeMessageFactory;
import info.fmro.betty.stream.protocol.ConnectionException;
import info.fmro.betty.stream.protocol.ConnectionStatus;
import info.fmro.betty.stream.protocol.ConnectionStatusChangeEvent;
import info.fmro.betty.stream.protocol.FutureResponse;
import info.fmro.betty.stream.protocol.MixInResponseMessage;
import info.fmro.betty.stream.protocol.RequestResponse;
import info.fmro.betty.stream.protocol.SubscriptionHandler;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Created by mulveyj on 07/07/2016.
 */
public class RequestResponseProcessor
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseProcessor.class);
    private final Client client;
    private final ObjectMapper objectMapper;
    private final AtomicInteger nextId = new AtomicInteger();
    private FutureResponse<ConnectionMessage> connectionMessage = new FutureResponse<>();
    private ConcurrentHashMap<Integer, RequestResponse> tasks = new ConcurrentHashMap<>();

    //subscription handlers
    private SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> marketSubscriptionHandler;
    private SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> orderSubscriptionHandler;

    private ConnectionStatus status = ConnectionStatus.STOPPED;
//    private CopyOnWriteArrayList<ConnectionStatusListener> connectionStatusListeners = new CopyOnWriteArrayList<>();

    private long lastRequestTime = Long.MAX_VALUE;
    private long lastResponseTime = Long.MAX_VALUE;

    private int traceChangeTruncation; // size to which some info messages from the processor are truncated

    public RequestResponseProcessor(Client client) {
        this.client = client;
        objectMapper = new ObjectMapper();
        objectMapper.addMixIn(ResponseMessage.class, MixInResponseMessage.class);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private synchronized void setStatus(ConnectionStatus value) {
        if (value == status) { //no-op
        } else {
            final ConnectionStatusChangeEvent args = new ConnectionStatusChangeEvent(this, status, value);
            status = value;
            logger.info("ESAClient: Status changed {} -> {}", status, value);

            dispatchConnectionStatusChange(args);
        }
    }

    public synchronized ConnectionStatus getStatus() {
        return status;
    }

    private synchronized void dispatchConnectionStatusChange(ConnectionStatusChangeEvent args) {
        try { // connectionStatusListeners are not used at the moment
//            connectionStatusListeners.forEach(c -> c.connectionStatusChange(args));
        } catch (Exception e) {
            logger.error("Exception during event dispatch", e);
        }
    }

    public synchronized int getTraceChangeTruncation() {
        return traceChangeTruncation;
    }

    public synchronized void setTraceChangeTruncation(int traceChangeTruncation) {
        this.traceChangeTruncation = traceChangeTruncation;
        logger.info("stream processor messages will be truncated to size: {}", this.traceChangeTruncation);
    }

    public synchronized long getLastRequestTime() {
        return lastRequestTime;
    }

    public synchronized long getLastResponseTime() {
        return lastResponseTime;
    }

    private synchronized void reset() {
        final ConnectionException cancelException = new ConnectionException("Connection reset - task cancelled");
        connectionMessage.setException(cancelException);
        connectionMessage = new FutureResponse<>();
        for (RequestResponse task : tasks.values()) {
            task.getFuture().setException(cancelException);
        }
        tasks = new ConcurrentHashMap<>();
    }

    public synchronized void disconnected() {
        setStatus(ConnectionStatus.DISCONNECTED);
        reset();
    }

    public synchronized void stopped() {
        marketSubscriptionHandler = null;
        orderSubscriptionHandler = null;
        setStatus(ConnectionStatus.STOPPED);
        reset();
    }

    public synchronized MarketSubscriptionMessage getMarketResubscribeMessage() {
        if (marketSubscriptionHandler != null) {
            final MarketSubscriptionMessage resub = marketSubscriptionHandler.getSubscriptionMessage();
            resub.setInitialClk(marketSubscriptionHandler.getInitialClk());
            resub.setClk(marketSubscriptionHandler.getClk());
            return resub;
        }
        return null;
    }

    public synchronized OrderSubscriptionMessage getOrderResubscribeMessage() {
        final OrderSubscriptionMessage resub;
        if (orderSubscriptionHandler != null) {
            resub = orderSubscriptionHandler.getSubscriptionMessage();
            resub.setInitialClk(orderSubscriptionHandler.getInitialClk());
            resub.setClk(orderSubscriptionHandler.getClk());
        } else {
            resub = null;
        }
        return resub;
    }

    public synchronized SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> getMarketSubscriptionHandler() {
        return marketSubscriptionHandler;
    }

    public synchronized void setMarketSubscriptionHandler(SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> newHandler) {
        if (marketSubscriptionHandler != null) {
            marketSubscriptionHandler.cancel();
        }
        marketSubscriptionHandler = newHandler;
        if (marketSubscriptionHandler != null) {
            setStatus(ConnectionStatus.SUBSCRIBED);
        }
    }

    public synchronized SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> getOrderSubscriptionHandler() {
        return orderSubscriptionHandler;
    }

    public synchronized void setOrderSubscriptionHandler(SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> newHandler) {
        if (orderSubscriptionHandler != null) {
            orderSubscriptionHandler.cancel();
        }
        orderSubscriptionHandler = newHandler;
        if (orderSubscriptionHandler != null) {
            setStatus(ConnectionStatus.SUBSCRIBED);
        }
    }

    public synchronized FutureResponse<ConnectionMessage> getConnectionMessage() {
        return connectionMessage;
    }

    public synchronized FutureResponse<StatusMessage> authenticate(AuthenticationMessage message) {
        header(message, RequestOperationType.authentication);
        return sendMessage(message, success -> setStatus(ConnectionStatus.AUTHENTICATED), true);
    }

    public synchronized FutureResponse<StatusMessage> heartbeat(HeartbeatMessage message) {
        header(message, RequestOperationType.heartbeat);
        return sendMessage(message, null);
    }

    public synchronized FutureResponse<StatusMessage> marketSubscription(MarketSubscriptionMessage message) {
        header(message, RequestOperationType.marketSubscription);
        final SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> newSub = new SubscriptionHandler<>(message, false);
        return sendMessage(message, success -> setMarketSubscriptionHandler(newSub));
    }

    public synchronized FutureResponse<StatusMessage> orderSubscription(OrderSubscriptionMessage message) {
        header(message, RequestOperationType.orderSubscription);
        final SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> newSub = new SubscriptionHandler<>(message, false);
        return sendMessage(message, success -> setOrderSubscriptionHandler(newSub));
    }

    private synchronized FutureResponse<StatusMessage> sendMessage(RequestMessage message, Consumer<RequestResponse> onSuccess) {
        return sendMessage(message, onSuccess, false);
    }

    private synchronized FutureResponse<StatusMessage> sendMessage(RequestMessage message, Consumer<RequestResponse> onSuccess, boolean isAuthLine) {
        int id = message.getId();
        final RequestResponse requestResponse = new RequestResponse(id, message, onSuccess);

        //store a future task
        tasks.put(id, requestResponse);

        //serialize message & send
        String line = null;
        try {
            line = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            //should never happen
            logger.error("Failed to marshall json: {}", Generic.objectToString(message), e);
        }
        logger.info("Client->ESA: " + line);

        if (isAuthLine) {
            client.writerThread.setAuthLine(line);
        } else {
            client.writerThread.addLine(line);
        }

        //time
        lastRequestTime = System.currentTimeMillis();

        return requestResponse.getFuture();
    }

    /**
     * Sets the header on the message and assigns an id
     *
     * @param msg
     * @param op
     * @return The id
     */
    private synchronized int header(RequestMessage msg, RequestOperationType op) {
        final int id = nextId.incrementAndGet();
        msg.setId(id);
        msg.setOp(op);
        return id;
    }

    /**
     * Processes an inbound (response) line of json
     *
     * @param line
     * @return
     */
    private synchronized ResponseMessage receiveLine(String line) {
        //clear last response
        ResponseMessage message = null;
        try {
            message = objectMapper.readValue(line, ResponseMessage.class);
        } catch (IOException e) {
            logger.error("IOException in receiveLine: {}", line, e);
//            e.printStackTrace();
        }
        lastResponseTime = System.currentTimeMillis();
        if (message != null) {
            switch (message.getOp()) {
                case connection:
                    logger.info("ESA->Client: {}", line);
                    processConnectionMessage((ConnectionMessage) message);
                    break;
                case status:
                    logger.info("ESA->Client: {}", line);
                    processStatusMessage((StatusMessage) message);
                    break;
                case mcm:
                    traceChange(line);
                    processMarketChangeMessage((MarketChangeMessage) message);
                    break;
                case ocm:
                    traceChange(line);
                    processOrderChangeMessage((OrderChangeMessage) message);
                    break;
                default:
                    logger.error("ESA->Client: Unknown message type: {}, message:{}", message.getOp(), line);
                    break;
            }
        } else { // message null, error was already printed, nothing to be done
        }
        return message;
    }

    private synchronized void traceChange(String line) {
        if (traceChangeTruncation != 0) {
            logger.info("ESA->Client: {}", line.substring(0, Math.min(traceChangeTruncation, line.length())));
        }
    }

    private synchronized void processOrderChangeMessage(OrderChangeMessage message) {
        ChangeMessage<OrderMarketChange> change = ChangeMessageFactory.ToChangeMessage(client.id, message);
        change = orderSubscriptionHandler.processChangeMessage(change);

        if (change != null) {
            Statics.orderCache.onOrderChange(change);
        }
    }

    private synchronized void processMarketChangeMessage(MarketChangeMessage message) {
        ChangeMessage<MarketChange> change = ChangeMessageFactory.ToChangeMessage(client.id, message);
        change = marketSubscriptionHandler.processChangeMessage(change);

        if (change != null) {
            Statics.marketCache.onMarketChange(change);
        }
    }

    private synchronized void processStatusMessage(StatusMessage statusMessage) {
        if (statusMessage.getId() == null) {
            //async status / status for a message that couldn't be decoded
            processUncorrelatedStatus(statusMessage);
        } else {
            final RequestResponse task = tasks.get(statusMessage.getId());
            if (task == null) {
                //shouldn't happen
                processUncorrelatedStatus(statusMessage);
            } else {
                //unwind task
                task.processStatusMessage(statusMessage);
            }
        }
    }

    private synchronized void processUncorrelatedStatus(StatusMessage statusMessage) {
        logger.error("Error Status Notification: {}", Generic.objectToString(statusMessage));
//        changeHandler.onErrorStatusNotification(statusMessage);
    }

    private synchronized void processConnectionMessage(ConnectionMessage message) {
        connectionMessage.setResponse(message);
        setStatus(ConnectionStatus.CONNECTED);
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (client.readerThread.bufferNotEmpty.get()) {
                    final String line = client.readerThread.pollLine();
                    receiveLine(line);
                }

                Generic.threadSleepSegmented(5_000L, 10L, client.readerThread.bufferNotEmpty, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside stream processor loop", throwable);
            }
        } // end while
    }
}
