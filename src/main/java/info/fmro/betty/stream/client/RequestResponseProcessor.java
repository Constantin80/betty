package info.fmro.betty.stream.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.definitions.AuthenticationMessage;
import info.fmro.betty.stream.definitions.ConnectionMessage;
import info.fmro.betty.stream.definitions.ErrorCode;
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
import info.fmro.betty.stream.protocol.ConnectionStatus;
import info.fmro.betty.stream.protocol.ConnectionStatusChangeEvent;
import info.fmro.betty.stream.protocol.MixInResponseMessage;
import info.fmro.betty.stream.protocol.RequestResponse;
import info.fmro.betty.stream.protocol.SubscriptionHandler;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    //    private ConnectionMessage connectionMessage;
    private final HashMap<Integer, RequestResponse> tasks = new HashMap<>();
    private final ScheduledExecutorService tasksMaintenance;

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
        tasksMaintenance = Executors.newSingleThreadScheduledExecutor();
    }

    private synchronized void successfulAuth() {
        setStatus(ConnectionStatus.AUTHENTICATED);
        client.isAuth.set(true);
    }

    private synchronized void setStatus(ConnectionStatus value) {
        if (value == status) { //no-op
        } else {
            final ConnectionStatusChangeEvent args = new ConnectionStatusChangeEvent(this, status, value);
            logger.info("[{}]ESAClient: Status changed {} -> {}", client.id, status, value);
            status = value;

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
            logger.error("[{}]Exception during event dispatch", client.id, e);
        }
    }

    public synchronized int getTraceChangeTruncation() {
        return traceChangeTruncation;
    }

    public synchronized void setTraceChangeTruncation(int traceChangeTruncation) {
        this.traceChangeTruncation = traceChangeTruncation;
        logger.info("[{}]stream processor messages will be truncated to size: {}", client.id, this.traceChangeTruncation);
    }

    public synchronized long getLastRequestTime() {
        return lastRequestTime;
    }

    public synchronized long getLastResponseTime() {
        return lastResponseTime;
    }

    private synchronized void resetProcessor() {
//        final ConnectionException cancelException = new ConnectionException("[{}]Connection reset - task cancelled");
//        connectionMessage.setException(cancelException);
        tasksMaintenance();
//        resetConnectionMessage();

        final Collection<RequestResponse> tasksValuesCopy = new ArrayList<>(tasks.values());
        tasks.clear(); // can be refilled by this method

        for (RequestResponse task : tasksValuesCopy) {
//            final RequestOperationType operationType = task.getRequestOperationType();
//            final RequestMessage requestMessage = task.getRequest();
            repeatTask(task);
        }
//        tasks = new ConcurrentHashMap<>();
    }

//    private synchronized void resetConnectionMessage() {
//        if (connectionMessage != null) {
//            logger.error("[{}]resetConnectionMessage has valid unparsed message: {}", client.id, Generic.objectToString(connectionMessage));
//            connectionMessage = null;
//        } else { // already null, nothing to be done
//        }
//    }

    public synchronized void disconnected() {
        setStatus(ConnectionStatus.DISCONNECTED);
        resetProcessor();
    }

    public synchronized void stopped() {
        marketSubscriptionHandler = null;
        orderSubscriptionHandler = null;
        setStatus(ConnectionStatus.STOPPED);
        resetProcessor();
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

    public synchronized void resubscribe() {
        final boolean marketSubscriptionAlreadyExists = similarTaskExists(RequestOperationType.marketSubscription);
        if (!marketSubscriptionAlreadyExists) {
            //Resub markets
            final MarketSubscriptionMessage marketSubscription = getMarketResubscribeMessage();
            if (marketSubscription != null) {
                logger.info("[{}]Resubscribe to market subscription.", client.id);
                ClientCommands.subscribeMarkets(client, marketSubscription);
            }
        }

        final boolean orderSubscriptionAlreadyExists = similarTaskExists(RequestOperationType.orderSubscription);
        if (!orderSubscriptionAlreadyExists) {
            //Resub orders
            final OrderSubscriptionMessage orderSubscription = getOrderResubscribeMessage();
            if (orderSubscription != null) {
                logger.info("[{}]Resubscribe to order subscription.", client.id);
                ClientCommands.subscribeOrders(client, orderSubscription);
            }
        }
    }

//    public synchronized void authAndResubscribe() {
//        final AuthenticationMessage authenticationMessage = new AuthenticationMessage();
//        authenticationMessage.setAppKey(Statics.appKey.get());
//        authenticationMessage.setSession(Statics.sessionTokenObject.getSessionToken());
////        ClientCommands.waitFor(client, authenticate(authenticationMessage));
//
//        authenticate(authenticationMessage);
//        Generic.threadSleepSegmented(client.timeout, 10L, client.isAuth, Statics.mustStop);
//
//        if (client.socketIsConnected.get() && client.streamIsConnected.get() && client.isAuth.get()) {
//            resubscribe();
//            client.isAuth.set(true); // after resubscribe, else I might get some racing condition bugs
//        } else {
//            if (!Statics.mustStop.get()) {
//                logger.error("something went wrong in streamClient before auth[{}]: {} {}", client.id, client.socketIsConnected.get(), client.streamIsConnected.get());
//            } else { // normal behavior to get this error after stop
//            }
//        }
//    }

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

//    public synchronized ConnectionMessage getConnectionMessage() {
//        return connectionMessage;
//    }

    public synchronized void authenticate(AuthenticationMessage message) {
        createHeader(message, RequestOperationType.authentication);
        sendMessage(message, success -> successfulAuth(), true);
    }

    public synchronized void heartbeat(HeartbeatMessage message) {
        heartbeat(message, true);
    }

    public synchronized void heartbeat(HeartbeatMessage message, boolean needsHeader) {
        if (needsHeader) {
            createHeader(message, RequestOperationType.heartbeat);
        }
        sendMessage(message, null);
    }

    public synchronized void marketSubscription(MarketSubscriptionMessage message) {
        marketSubscription(message, true);
    }

    public synchronized void marketSubscription(MarketSubscriptionMessage message, boolean needsHeader) {
        if (needsHeader) {
            createHeader(message, RequestOperationType.marketSubscription);
        }
        final SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> newSub = new SubscriptionHandler<>(message, false);
        sendMessage(message, success -> setMarketSubscriptionHandler(newSub));
    }

    public synchronized void orderSubscription(OrderSubscriptionMessage message) {
        orderSubscription(message, true);
    }

    public synchronized void orderSubscription(OrderSubscriptionMessage message, boolean needsHeader) {
        if (needsHeader) {
            createHeader(message, RequestOperationType.orderSubscription);
        }
        final SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> newSub = new SubscriptionHandler<>(message, false);
        sendMessage(message, success -> setOrderSubscriptionHandler(newSub));
    }

    private synchronized void sendMessage(RequestMessage message, Consumer<RequestResponse> onSuccess) {
        sendMessage(message, onSuccess, false);
    }

    private synchronized void sendMessage(RequestMessage message, Consumer<RequestResponse> onSuccess, boolean isAuthLine) {
        final int messageId = message.getId();
        final RequestResponse requestResponse = new RequestResponse(message, onSuccess);

        //store a future task
        tasks.put(messageId, requestResponse);

        //serialize message & send
        String line = null;
        try {
            line = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            //should never happen
            logger.error("[{}]Failed to marshall json: {}", client.id, Generic.objectToString(message), e);
        }
        logger.info("[{}]Client->ESA: {}", client.id, line);

        if (isAuthLine) {
            client.writerThread.setAuthLine(line);
        } else {
            client.writerThread.addLine(line);
        }

        //time
        lastRequestTime = System.currentTimeMillis();
//        return requestResponse.getFuture();

        tasksMaintenance();
    }

    /**
     * Sets the header on the message and assigns an id
     *
     * @param msg
     * @param op
     * @return The id
     */
    private synchronized void createHeader(RequestMessage msg, RequestOperationType op) {
        setMessageId(msg);
        msg.setOp(op);
    }

    private synchronized void setMessageId(RequestMessage msg) {
        final int id = nextId.incrementAndGet();
        msg.setId(id);
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
            logger.error("[{}]IOException in receiveLine: {}", client.id, line, e);
//            e.printStackTrace();
        }
        lastResponseTime = System.currentTimeMillis();
        if (message != null) {
            switch (message.getOp()) {
                case connection:
                    logger.info("[{}]ESA->Client: {}", client.id, line);
                    processConnectionMessage((ConnectionMessage) message);
                    break;
                case status:
                    logger.info("[{}]ESA->Client: {}", client.id, line);
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
                    logger.error("[{}]ESA->Client: Unknown message type: {}, message:{}", client.id, message.getOp(), line);
                    break;
            }
        } else { // message null, error was already printed, nothing to be done
        }
        return message;
    }

    private synchronized void traceChange(String line) {
        if (traceChangeTruncation != 0) {
            logger.info("[{}]ESA->Client: {}", client.id, line.substring(0, Math.min(traceChangeTruncation, line.length())));
        }
    }

    private synchronized void processOrderChangeMessage(OrderChangeMessage message) {
        ChangeMessage<OrderMarketChange> change = ChangeMessageFactory.ToChangeMessage(client.id, message);
        if (orderSubscriptionHandler == null) {
            logger.error("[{}]null orderSubscriptionHandler for: {}", client.id, Generic.objectToString(message));
        } else {
            change = orderSubscriptionHandler.processChangeMessage(change);
            if (change != null) {
                Statics.orderCache.onOrderChange(change);
            }
        }
    }

    private synchronized void processMarketChangeMessage(MarketChangeMessage message) {
        ChangeMessage<MarketChange> change = ChangeMessageFactory.ToChangeMessage(client.id, message);
        if (marketSubscriptionHandler == null) {
            logger.error("[{}]null marketSubscriptionHandler for: {}", client.id, Generic.objectToString(message));
        } else {
            change = marketSubscriptionHandler.processChangeMessage(change);

            if (change != null) {
                Statics.marketCache.onMarketChange(change);
            }
        }
    }

    private synchronized void processStatusMessage(StatusMessage statusMessage) {
        if (statusMessage.getId() == null) {
            //async status / status for a message that couldn't be decoded
            logger.error("[{}]Error Status Notification: {}", client.id, Generic.objectToString(statusMessage));
        } else {
            final RequestResponse task = tasks.get(statusMessage.getId());
            if (task == null) {
                //shouldn't happen
                logger.error("[{}]Status Notification with no task: {}", client.id, Generic.objectToString(statusMessage));
            } else {
                //unwind task
                task.processStatusMessage(statusMessage);
            }
        }
        tasksMaintenance();
    }

//    private synchronized void processUncorrelatedStatus(StatusMessage statusMessage) {
//        logger.error("[{}]Error Status Notification: {}", client.id, Generic.objectToString(statusMessage));
////        changeHandler.onErrorStatusNotification(statusMessage);
//    }

    private synchronized void processConnectionMessage(ConnectionMessage message) {
//        connectionMessage.setResponse(message);
        setStatus(ConnectionStatus.CONNECTED);
        client.streamIsConnected.set(true);
    }

    private synchronized void tasksMaintenance() {
        if (!tasks.isEmpty()) {
            final HashSet<Integer> idsToRemove = new HashSet<>();
            int lastHeartbeat = 0, lastAuth = 0, lastOrderSub = 0, lastMarketSub = 0;
            for (Integer id : tasks.keySet()) {
                if (id == null) {
                    logger.error("[{}]null key in tasks", client.id);
                    idsToRemove.add(id);
                } else if (id <= 0) {
                    logger.error("[{}]wrong value key in tasks: {} {}", client.id, id, Generic.objectToString(tasks.get(id)));
                    idsToRemove.add(id);
                } else {
                    final RequestResponse task = tasks.get(id);
                    final RequestOperationType operationType = task.getRequestOperationType();
                    switch (operationType) {
                        case heartbeat:
                            if (lastHeartbeat == 0) {
                                lastHeartbeat = id;
                            } else if (lastHeartbeat > id) {
                                idsToRemove.add(id);
                            } else if (lastHeartbeat < id) {
                                idsToRemove.add(lastHeartbeat);
                                lastHeartbeat = id;
                            } else {
                                logger.error("[{}]this branch should never be reached: {} {}", client.id, lastHeartbeat, id);
                            }
                            break;
                        case authentication:
                            if (lastAuth == 0) {
                                lastAuth = id;
                            } else if (lastAuth > id) {
                                idsToRemove.add(id);
                            } else if (lastAuth < id) {
                                idsToRemove.add(lastAuth);
                                lastAuth = id;
                            } else {
                                logger.error("[{}]this branch should never be reached: {} {}", client.id, lastAuth, id);
                            }
                            break;
                        case orderSubscription:
                            if (lastOrderSub == 0) {
                                lastOrderSub = id;
                            } else if (lastOrderSub > id) {
                                idsToRemove.add(id);
                            } else if (lastOrderSub < id) {
                                idsToRemove.add(lastOrderSub);
                                lastOrderSub = id;
                            } else {
                                logger.error("[{}]this branch should never be reached: {} {}", client.id, lastOrderSub, id);
                            }
                            break;
                        case marketSubscription:
                            if (lastMarketSub == 0) {
                                lastMarketSub = id;
                            } else if (lastMarketSub > id) {
                                idsToRemove.add(id);
                            } else if (lastMarketSub < id) {
                                idsToRemove.add(lastMarketSub);
                                lastMarketSub = id;
                            } else {
                                logger.error("[{}]this branch should never be reached: {} {}", client.id, lastMarketSub, id);
                            }
                            break;
                        default:
                            logger.error("[{}]unknown operation type in tasksMaintenance for: {} {}", client.id, operationType, Generic.objectToString(task));
                            break;
                    } // end switch
                }
            } // end for

            if (!idsToRemove.isEmpty()) {
                for (Integer id : idsToRemove) {
                    logger.info("[{}]removing task in tasksMaintenance: {} {}", client.id, id, Generic.objectToString(tasks.get(id)));
                    tasks.remove(id);
                }
                idsToRemove.clear();
            } else { // no id to remove, nothing to be done
            }

//            final HashSet<Integer> tasksKeySetCopy = new HashSet<>(tasks.keySet()); // I need to use copy here, as I add new values to the map inside the for, by using repeatTask(); not true anymore, with recent modifications
            final HashSet<RequestResponse> tasksToRepeat = new HashSet<>(2);
            for (Integer id : tasks.keySet()) {
                final RequestResponse task = tasks.get(id);

                if (task.isTaskSuccessful()) {
                    idsToRemove.add(id);
                } else if (task.isTaskFinished()) { // tasks with error are not normally repeated, but in the future I might change this for some error codes
                    final StatusMessage statusMessage = task.getStatusMessage();
                    final ErrorCode errorCode = statusMessage.getErrorCode();

                    switch (errorCode) {
                        case NOT_AUTHORIZED:
                        case NO_APP_KEY:
                        case INVALID_APP_KEY:
                        case NO_SESSION:
                        case INVALID_SESSION_INFORMATION:
                            logger.info("{}, needSessionToken[{}]: {}", errorCode, client.id, Generic.objectToString(statusMessage));
                            Statics.needSessionToken.set(true);
                            tasksToRepeat.add(task);
//                            repeatTask(operationType, task.getRequest());
                            break;
                        case INVALID_CLOCK:
                        case UNEXPECTED_ERROR:
                        case TIMEOUT:
                            logger.error("{} in streamClient[{}]: {}", errorCode, client.id, Generic.objectToString(statusMessage));
                            tasksToRepeat.add(task);
//                            repeatTask(operationType, task.getRequest());
                            break;
                        case INVALID_INPUT:
                        case INVALID_REQUEST:
                            logger.error("{} in streamClient[{}]: {}", errorCode, client.id, Generic.objectToString(statusMessage));
                            break;
                        case SUBSCRIPTION_LIMIT_EXCEEDED:
                            logger.error("{} in streamClient[{}]: {}", errorCode, client.id, Generic.objectToString(statusMessage));
                            break;
                        case CONNECTION_FAILED:
                            logger.error("{} in streamClient[{}]: {}", errorCode, client.id, Generic.objectToString(statusMessage));
                            tasksToRepeat.add(task);
//                            repeatTask(operationType, task.getRequest());
                            break;
                        case MAX_CONNECTION_LIMIT_EXCEEDED:
                            logger.error("{} in streamClient[{}]: {}", errorCode, client.id, Generic.objectToString(statusMessage));
                            tasksToRepeat.add(task);
//                            repeatTask(operationType, task.getRequest());
                            break;
                        default:
                            logger.error("[{}]unknown errorCode {} for: {}", client.id, errorCode, Generic.objectToString(task));
                            break;
                    }
                    if (errorCode != ErrorCode.SUBSCRIPTION_LIMIT_EXCEEDED) {
                        client.setStreamError(true);
                    }
                    idsToRemove.add(id);
                } else if (task.isExpired()) {
                    logger.error("[{}]maintenance, task expired: {}", client.id, Generic.objectToString(task));
                    idsToRemove.add(id);
//                    final RequestMessage requestMessage = task.getRequest();
                    tasksToRepeat.add(task);
//                    repeatTask(operationType, requestMessage);
                } else { // task not finished and no expired, nothing to be done
                }
            } // end for

            if (!idsToRemove.isEmpty()) {
                for (Integer id : idsToRemove) {
                    logger.info("[{}]removing task in tasksMaintenance end: {} {}", client.id, id, Generic.objectToString(tasks.get(id)));
                    tasks.remove(id);
                }
                idsToRemove.clear();
            } else { // no id to remove, nothing to be done
            }
            if (!tasksToRepeat.isEmpty()) {
                for (RequestResponse task : tasksToRepeat) {
                    repeatTask(task);
                }
                tasksToRepeat.clear();
            } else { // no tasks to repeat, nothing to be done
            }
        } else { // no tasks, nothing to be done
        }
    }

    private synchronized void repeatTask(RequestResponse task) {
//        final RequestMessage newRequestMessage = new RequestMessage(requestMessage); // if I use the previous request message and modify it, I'll run into some very nasty bugs; not true
        final RequestOperationType operationType = task.getRequestOperationType();
        final RequestMessage requestMessage = task.getRequest();
        final boolean similarTaskExists = similarTaskExists(operationType);
        switch (operationType) {
            case heartbeat:
                if (!similarTaskExists) {
                    heartbeat((HeartbeatMessage) requestMessage, false);
                }
                break;
            case authentication: // reauth should be done automatically, without reusing this message
                break;
            case orderSubscription:
                if (!similarTaskExists) {
                    orderSubscription((OrderSubscriptionMessage) requestMessage, false);
                }
                break;
            case marketSubscription:
                if (!similarTaskExists) {
                    marketSubscription((MarketSubscriptionMessage) requestMessage, false);
                }
                break;
            default:
                logger.error("[{}]unknown operation type in maintenance for: {} {}", client.id, operationType, Generic.objectToString(requestMessage));
                break;
        }
    }

    private synchronized boolean similarTaskExists(RequestOperationType operationType) {
        boolean foundSimilar = false;
        for (RequestResponse task : tasks.values()) {
            final RequestOperationType existingOperation = task.getRequestOperationType();
            if (Objects.equals(operationType, existingOperation)) {
                foundSimilar = true;
                break;
            }
        }

        return foundSimilar;
    }

    @Override
    public void run() {
        tasksMaintenance.scheduleAtFixedRate(this::tasksMaintenance, 2_000L, 2_000L, TimeUnit.MILLISECONDS);

        while (!Statics.mustStop.get()) {
            try {
                if (client.readerThread.bufferNotEmpty.get()) {
                    final String line = client.readerThread.pollLine();
                    receiveLine(line);
                }

                Generic.threadSleepSegmented(5_000L, 10L, client.readerThread.bufferNotEmpty, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("[{}]STRANGE ERROR inside stream processor loop", client.id, throwable);
            }
        } // end while

        if (tasksMaintenance != null) {
            try {
                tasksMaintenance.shutdown();
                if (!tasksMaintenance.awaitTermination(1L, TimeUnit.MINUTES)) {
                    logger.error("[{}]tasksMaintenance hanged", client.id);
                    final List<Runnable> runnableList = tasksMaintenance.shutdownNow();
                    if (!runnableList.isEmpty()) {
                        logger.error("[{}]tasksMaintenance not commenced: {}", client.id, runnableList.size());
                    }
                }
            } catch (InterruptedException e) {
                logger.error("[{}]InterruptedException during tasksMaintenance awaitTermination during stream end", client.id, e);
            }
        } else {
            logger.error("[{}]tasksMaintenance null during stream end", client.id);
        }
    }
}
