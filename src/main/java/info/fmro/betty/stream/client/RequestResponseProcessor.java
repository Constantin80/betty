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
import info.fmro.betty.stream.definitions.StatusCode;
import info.fmro.betty.stream.definitions.StatusMessage;
import info.fmro.betty.stream.protocol.ChangeMessage;
import info.fmro.betty.stream.protocol.ChangeMessageFactory;
import info.fmro.betty.stream.protocol.ConnectionStatus;
import info.fmro.betty.stream.protocol.MixInResponseMessage;
import info.fmro.betty.stream.protocol.RequestResponse;
import info.fmro.betty.stream.protocol.SubscriptionHandler;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    //    private final ScheduledExecutorService tasksMaintenance;
    private final HashSet<String> marketsSet;

    //subscription handlers
    @Nullable
    private SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> marketSubscriptionHandler;
    @Nullable
    private SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> orderSubscriptionHandler;
    private final Map<Integer, Long> previousIds = new HashMap<>(1);

    private ConnectionStatus status = ConnectionStatus.STOPPED;
//    private CopyOnWriteArrayList<ConnectionStatusListener> connectionStatusListeners = new CopyOnWriteArrayList<>();

    private long lastRequestTime = Long.MAX_VALUE;
    private long lastResponseTime = Long.MAX_VALUE;

    private int traceChangeTruncation; // size to which some info messages from the processor are truncated
    private transient boolean mcmCommandReceived, ocmCommandReceived;

    public RequestResponseProcessor(final Client client) {
        this.client = client;
        this.marketsSet = new HashSet<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.addMixIn(ResponseMessage.class, MixInResponseMessage.class);
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        tasksMaintenance = Executors.newSingleThreadScheduledExecutor();
    }

    public synchronized boolean hasValidHandler() {
        return this.marketSubscriptionHandler != null || this.orderSubscriptionHandler != null;
    }

    public synchronized int nMarkets() {
        return this.marketsSet.size();
    }

    public synchronized HashSet<String> getMarketsSet() {
        @Nullable final HashSet<String> returnValue;
        if (this.marketsSet == null) {
            logger.error("[{}]null marketsSet in processor", this.client.id);
            returnValue = null;
        } else {
            returnValue = new HashSet<>(this.marketsSet);
        }

        return this.marketsSet;
    }

    public synchronized boolean setMarketsSet(final Collection<String> markets) {
        final int newSize = markets.size();
        final boolean modified;
        if (newSize <= 1000) {
            this.marketsSet.clear();
            this.marketsSet.addAll(markets);
            modified = true;
        } else {
            logger.error("[{}]trying to set too many markets: {}", this.client.id, newSize);
            modified = false;
        }
        if (modified) {
            marketSubscription(ClientCommands.createMarketSubscriptionMessage(this.client, getMarketsSet()));
        }
        return modified;
    }

    private synchronized void removeMarketsFromSet(final int nMarkets) {
        if (nMarkets > 0) {
            final ArrayList<String> list = new ArrayList<>(this.marketsSet);
            final int listSize = list.size();
            final List<String> subList;
            if (nMarkets < listSize) {
                subList = list.subList(0, nMarkets - 1);
            } else {
                logger.error("[{}]bad nMarkets: {} {}", this.client.id, nMarkets, listSize);
                subList = list;
            }
            subList.clear();

            logger.info("[{}]removing {} markets from stream: before: {} after: {}", this.client.id, nMarkets, nMarkets(), list.size());
            setMarketsSet(list);
            ClientHandler.modifiedLists.add(this.client.id);
        } else {
            logger.error("[{}]bad nMarkets: {}", this.client.id, nMarkets);
        }
    }

    private synchronized void successfulAuth() {
        setStatus(ConnectionStatus.AUTHENTICATED);
        this.client.isAuth.set(true);
    }

    private synchronized void setStatus(final ConnectionStatus value) {
        if (value == this.status) { //no-op
        } else {
            logger.info("[{}]ESAClient: Status changed {} -> {}", this.client.id, this.status, value);
            this.status = value;

//            final ConnectionStatusChangeEvent args = new ConnectionStatusChangeEvent(this, status, value);
//            dispatchConnectionStatusChange(args);
        }
    }

    public synchronized ConnectionStatus getStatus() {
        return this.status;
    }

//    private synchronized void dispatchConnectionStatusChange(ConnectionStatusChangeEvent args) {
//        try { // connectionStatusListeners are not used at the moment
////            connectionStatusListeners.forEach(c -> c.connectionStatusChange(args));
//        } catch (Exception e) {
//            logger.error("[{}]Exception during event dispatch", client.id, e);
//        }
//    }

    public synchronized int getTraceChangeTruncation() {
        return this.traceChangeTruncation;
    }

    public synchronized void setTraceChangeTruncation(final int traceChangeTruncation) {
        this.traceChangeTruncation = traceChangeTruncation;
        logger.info("[{}]stream processor messages will be truncated to size: {}", this.client.id, this.traceChangeTruncation);
    }

    public synchronized long getLastRequestTime() {
        return this.lastRequestTime;
    }

    public synchronized long getLastResponseTime() {
        return this.lastResponseTime;
    }

    private synchronized void resetProcessor() {
//        final ConnectionException cancelException = new ConnectionException("[{}]Connection reset - task cancelled");
//        connectionMessage.setException(cancelException);
        tasksMaintenance();
//        resetConnectionMessage();

        final Iterable<RequestResponse> tasksValuesCopy = new ArrayList<>(this.tasks.values());
        this.tasks.clear(); // can be refilled by this method

        final long currentTime = System.currentTimeMillis();
        for (final RequestResponse task : tasksValuesCopy) {
//            final RequestOperationType operationType = task.getRequestOperationType();
//            final RequestMessage requestMessage = task.getRequest();
            final int taskId = task.getId();
            this.previousIds.put(taskId, currentTime);
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
        this.marketSubscriptionHandler = null;
        this.orderSubscriptionHandler = null;
        setStatus(ConnectionStatus.STOPPED);
        resetProcessor();
    }

    @Nullable
    public synchronized MarketSubscriptionMessage getMarketResubscribeMessage() {
        if (this.marketSubscriptionHandler != null) {
            final MarketSubscriptionMessage resub = this.marketSubscriptionHandler.getSubscriptionMessage();
            resub.setInitialClk(this.marketSubscriptionHandler.getInitialClk());
            resub.setClk(this.marketSubscriptionHandler.getClk());
            return resub;
        }
        return null;
    }

    public synchronized OrderSubscriptionMessage getOrderResubscribeMessage() {
        @Nullable final OrderSubscriptionMessage resub;
        if (this.orderSubscriptionHandler != null) {
            resub = this.orderSubscriptionHandler.getSubscriptionMessage();
            resub.setInitialClk(this.orderSubscriptionHandler.getInitialClk());
            resub.setClk(this.orderSubscriptionHandler.getClk());
        } else {
            resub = null;
        }
        return resub;
    }

    public synchronized void resubscribe() {
        final boolean marketSubscriptionAlreadyExists = similarTaskExists(RequestOperationType.marketSubscription);
        if (!marketSubscriptionAlreadyExists) {
            //Resub markets
            final MarketSubscriptionMessage marketSubscriptionMessage = getMarketResubscribeMessage();
            if (marketSubscriptionMessage != null) {
                logger.info("[{}]Resubscribe to market subscription.", this.client.id);
//                ClientCommands.createMarketSubscriptionMessage(client, marketSubscriptionMessage);
                marketSubscription(marketSubscriptionMessage);
            }
        }

        final boolean orderSubscriptionAlreadyExists = similarTaskExists(RequestOperationType.orderSubscription);
        if (!orderSubscriptionAlreadyExists) {
            //Resub orders
            final OrderSubscriptionMessage orderSubscriptionMessage = getOrderResubscribeMessage();
            if (orderSubscriptionMessage != null) {
                logger.info("[{}]Resubscribe to order subscription.", this.client.id);
//                ClientCommands.createOrderSubscriptionMessage(client, orderSubscriptionMessage);
                orderSubscription(orderSubscriptionMessage);
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
        return this.marketSubscriptionHandler;
    }

    public synchronized void setMarketSubscriptionHandler(final SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> newHandler) {
        if (this.marketSubscriptionHandler != null) {
            this.marketSubscriptionHandler.cancel();
        }
        this.marketSubscriptionHandler = newHandler;
        if (this.marketSubscriptionHandler != null) {
            setStatus(ConnectionStatus.SUBSCRIBED);
        }
    }

    public synchronized SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> getOrderSubscriptionHandler() {
        return this.orderSubscriptionHandler;
    }

    public synchronized void setOrderSubscriptionHandler(final SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> newHandler) {
        if (this.orderSubscriptionHandler != null) {
            this.orderSubscriptionHandler.cancel();
        }
        this.orderSubscriptionHandler = newHandler;
        if (this.orderSubscriptionHandler != null) {
            setStatus(ConnectionStatus.SUBSCRIBED);
        }
    }

//    public synchronized ConnectionMessage getConnectionMessage() {
//        return connectionMessage;
//    }

    public synchronized void authenticate(final AuthenticationMessage message) {
        createHeader(message, RequestOperationType.authentication);
        sendMessage(message, success -> successfulAuth(), true);
    }

    public synchronized void keepAliveCheck() {
        if (getStatus() == ConnectionStatus.SUBSCRIBED) { //connection looks up
            if (getLastRequestTime() + this.client.keepAliveHeartbeat < System.currentTimeMillis()) { //send a heartbeat to server to keep networks open
                logger.info("[{}]Last Request Time is longer than {}: Sending Keep Alive Heartbeat", this.client.id, this.client.keepAliveHeartbeat);
                heartbeat();
            } else if (getLastResponseTime() + this.client.timeout < System.currentTimeMillis()) {
                logger.info("[{}]Last Response Time is longer than timeout {}: Sending Keep Alive Heartbeat", this.client.id, this.client.timeout);
                heartbeat();
            }
        }
    }

    private synchronized void heartbeat() {
//        ClientCommands.waitFor(this, processor.heartbeat(new HeartbeatMessage()));
        heartbeat(new HeartbeatMessage());
    }

    private synchronized void heartbeat(final HeartbeatMessage message) {
        heartbeat(message, true);
    }

    private synchronized void heartbeat(final HeartbeatMessage message, final boolean needsHeader) {
        if (needsHeader) {
            createHeader(message, RequestOperationType.heartbeat);
        }
        sendMessage(message, null);
    }

    private synchronized void marketSubscription(final MarketSubscriptionMessage message) {
        marketSubscription(message, true);
    }

    private synchronized void marketSubscription(final MarketSubscriptionMessage message, final boolean needsHeader) {
        if (needsHeader) {
            createHeader(message, RequestOperationType.marketSubscription);
        }
        final SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> newSub = new SubscriptionHandler<>(message, false);
        sendMessage(message, success -> setMarketSubscriptionHandler(newSub));
    }

    private synchronized void orderSubscription(final OrderSubscriptionMessage message) {
        orderSubscription(message, true);
    }

    private synchronized void orderSubscription(final OrderSubscriptionMessage message, final boolean needsHeader) {
        if (needsHeader) {
            createHeader(message, RequestOperationType.orderSubscription);
        }
        final SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> newSub = new SubscriptionHandler<>(message, false);
        sendMessage(message, success -> setOrderSubscriptionHandler(newSub));
    }

    private synchronized void sendMessage(final RequestMessage message, final Consumer<RequestResponse> onSuccess) {
        sendMessage(message, onSuccess, false);
    }

    private synchronized void sendMessage(final RequestMessage message, final Consumer<RequestResponse> onSuccess, final boolean isAuthLine) {
        final int messageId = message.getId();
        final RequestResponse requestResponse = new RequestResponse(message, onSuccess);

        //store a future task
        this.tasks.put(messageId, requestResponse);

        //serialize message & send
        String line = null;
        try {
            line = this.objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            //should never happen
            logger.error("[{}]Failed to marshall json: {}", this.client.id, Generic.objectToString(message), e);
        }
        logger.info("[{}]Client->ESA: {}", this.client.id, line);

        if (isAuthLine) {
            this.client.writerThread.setAuthLine(line);
        } else {
            this.client.writerThread.addLine(line);
        }

        //time
        this.lastRequestTime = System.currentTimeMillis();
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
    private synchronized void createHeader(final RequestMessage msg, final RequestOperationType op) {
        setMessageId(msg);
        msg.setOp(op);
    }

    private synchronized void setMessageId(final RequestMessage msg) {
        final int id = this.nextId.incrementAndGet();
        msg.setId(id);
    }

    /**
     * Processes an inbound (response) line of json
     *
     * @param line
     * @return
     */
    private synchronized ResponseMessage receiveLine(final String line) {
        //clear last response
        ResponseMessage message = null;
        try {
            message = this.objectMapper.readValue(line, ResponseMessage.class);
        } catch (IOException e) {
            logger.error("[{}]IOException in receiveLine: {}", this.client.id, line, e);
//            e.printStackTrace();
        }
        this.lastResponseTime = System.currentTimeMillis();
        if (message != null) {
            switch (message.getOp()) {
                case connection:
                    logger.info("[{}]ESA->Client: {}", this.client.id, line);
                    processConnectionMessage((ConnectionMessage) message);
                    break;
                case status:
                    logger.info("[{}]ESA->Client: {}", this.client.id, line);
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
                    logger.error("[{}]ESA->Client: Unknown message type: {}, message:{}", this.client.id, message.getOp(), line);
                    break;
            }
        } else { // message null, error was already printed, nothing to be done
        }
        return message;
    }

    private synchronized void traceChange(final String line) {
        if (this.traceChangeTruncation != 0) {
            logger.info("[{}]ESA->Client: {}", this.client.id, line.substring(0, Math.min(this.traceChangeTruncation, line.length())));
        }
    }

    private synchronized void processOrderChangeMessage(final OrderChangeMessage message) {
        if (!this.ocmCommandReceived) {
            this.ocmCommandReceived = true;
            ClientHandler.threadsWithOcmCommandReceived.getAndIncrement();
        }

        ChangeMessage<OrderMarketChange> change = ChangeMessageFactory.ToChangeMessage(this.client.id, message);
        if (this.orderSubscriptionHandler == null) {
            logger.error("[{}]null orderSubscriptionHandler for: {}", this.client.id, Generic.objectToString(message, "mc"));
        } else {
            change = this.orderSubscriptionHandler.processChangeMessage(change);
            if (change != null) {
                Statics.orderCache.onOrderChange(change);
            }
        }
    }

    private synchronized void processMarketChangeMessage(final MarketChangeMessage message) {
        if (!this.mcmCommandReceived) {
            this.mcmCommandReceived = true;
            ClientHandler.threadsWithMcmCommandReceived.getAndIncrement();
        }

        ChangeMessage<MarketChange> change = ChangeMessageFactory.ToChangeMessage(this.client.id, message);
        final int id = message.getId();
        if (this.marketSubscriptionHandler == null) {
            if (isIdRecentlyRemoved(id)) {
                logger.info("[{}]null marketSubscriptionHandler for: {}", this.client.id, id);
            } else {
                logger.error("[{}]null marketSubscriptionHandler for: {}", this.client.id, Generic.objectToString(message, "mc"));
            }
        } else {
            final int handlerId = this.marketSubscriptionHandler.getSubscriptionId();
            if (handlerId == id) {
                change = this.marketSubscriptionHandler.processChangeMessage(change);

                if (change != null) {
                    Statics.marketCache.onMarketChange(change);
                }
            } else {
                if (isIdRecentlyRemoved(id)) {
                    logger.info("[{}]obsolete marketSubscriptionHandler attempt for: {}", this.client.id, id);
                } else {
                    logger.error("[{}]obsolete marketSubscriptionHandler attempt for: {}", this.client.id, Generic.objectToString(message, "mc"));
                }
            }
        }
    }

    private synchronized void processStatusMessage(final StatusMessage statusMessage) {
        final Integer id = statusMessage.getId();
        if (id == null) {
            //async status / status for a message that couldn't be decoded
            final StatusCode statusCode = statusMessage.getStatusCode();
            if (statusCode == StatusCode.SUCCESS) {
                logger.error("[{}]{} in Error Status Notification: {}", this.client.id, statusCode, Generic.objectToString(statusMessage));
            } else if (statusCode == StatusCode.FAILURE) {
                final ErrorCode errorCode = statusMessage.getErrorCode();
                switch (errorCode) {
                    case TIMEOUT:
                        if (Statics.needSessionToken.get() || Statics.sessionTokenObject.isRecent()) {
                            logger.info("{} Error Status Notification in streamClient[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                        } else {
                            logger.error("{} Error Status Notification in streamClient[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                        }
                        break;
                    case NOT_AUTHORIZED:
                    case NO_APP_KEY:
                    case INVALID_APP_KEY:
                    case NO_SESSION:
                    case INVALID_SESSION_INFORMATION:
                        logger.info("{}, Error Status Notification needSessionToken[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                        Statics.needSessionToken.set(true);
                        break;
                    case INVALID_CLOCK:
                    case UNEXPECTED_ERROR:
                    case INVALID_INPUT:
                    case INVALID_REQUEST:
                    case SUBSCRIPTION_LIMIT_EXCEEDED:
                    case CONNECTION_FAILED:
                    case MAX_CONNECTION_LIMIT_EXCEEDED:
                        logger.error("{} Error Status Notification in streamClient[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                        break;
                    default:
                        logger.error("[{}]unknown errorCode {} Error Status Notification for: {}", this.client.id, errorCode, Generic.objectToString(statusMessage));
                        break;
                }
            } else { // includes null
                logger.error("[{}]unknown StatusCode {} in Error Status Notification: {}", this.client.id, statusCode, Generic.objectToString(statusMessage));
            }
            logger.error("[{}]Error Status Notification: {}", this.client.id, Generic.objectToString(statusMessage));
        } else {
            final RequestResponse task = this.tasks.get(id);
            if (task == null) {
                //shouldn't happen
                if (isIdRecentlyRemoved(id)) {
                    logger.info("[{}]Status Notification with no task: {}", this.client.id, id);
                } else {
                    logger.error("[{}]Status Notification with no task: {}", this.client.id, Generic.objectToString(statusMessage));
                }
            } else {
                //unwind task
                task.processStatusMessage(statusMessage);
            }
        }
        tasksMaintenance();
    }

    private synchronized boolean isIdRecentlyRemoved(final int id) {
        return isIdRecentlyRemoved(id, 120_000L); // default value
    }

    private synchronized boolean isIdRecentlyRemoved(final int id, final long recentPeriod) {
        final boolean isRecent;
        final Long storedValue = this.previousIds.get(id);
        if (storedValue == null) {
            isRecent = false;
        } else {
            final long currentTime = System.currentTimeMillis();
            final long timeSinceRemoved = currentTime - storedValue;
            isRecent = timeSinceRemoved <= recentPeriod;
        }

        return isRecent;
    }

//    private synchronized void processUncorrelatedStatus(StatusMessage statusMessage) {
//        logger.error("[{}]Error Status Notification: {}", client.id, Generic.objectToString(statusMessage));
////        changeHandler.onErrorStatusNotification(statusMessage);
//    }

    private synchronized void processConnectionMessage(final ConnectionMessage message) {
//        connectionMessage.setResponse(message);
        setStatus(ConnectionStatus.CONNECTED);
        this.client.streamIsConnected.set(true);
    }

    private synchronized void tasksMaintenance() {
        if (!this.tasks.isEmpty()) {
            final Collection<Integer> idsToRemove = new HashSet<>();
            int lastHeartbeat = 0, lastAuth = 0, lastOrderSub = 0, lastMarketSub = 0;
            for (final Integer id : this.tasks.keySet()) {
                if (id == null) {
                    logger.error("[{}]null key in tasks", this.client.id);
                    idsToRemove.add(id);
                } else if (id <= 0) {
                    logger.error("[{}]wrong value key in tasks: {} {}", this.client.id, id, Generic.objectToString(this.tasks.get(id)));
                    idsToRemove.add(id);
                } else {
                    final RequestResponse task = this.tasks.get(id);
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
                                logger.error("[{}]this branch should never be reached: {} {}", this.client.id, lastHeartbeat, id);
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
                                logger.error("[{}]this branch should never be reached: {} {}", this.client.id, lastAuth, id);
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
                                logger.error("[{}]this branch should never be reached: {} {}", this.client.id, lastOrderSub, id);
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
                                logger.error("[{}]this branch should never be reached: {} {}", this.client.id, lastMarketSub, id);
                            }
                            break;
                        default:
                            logger.error("[{}]unknown operation type in tasksMaintenance for: {} {}", this.client.id, operationType, Generic.objectToString(task));
                            break;
                    } // end switch
                }
            } // end for

            if (!idsToRemove.isEmpty()) {
                for (final Integer id : idsToRemove) {
                    logger.info("[{}]removing task in tasksMaintenance: {} {}", this.client.id, id, Generic.objectToString(this.tasks.get(id)));
                    final long currentTime = System.currentTimeMillis();
                    this.previousIds.put(id, currentTime);
                    this.tasks.remove(id);
                }
                idsToRemove.clear();
            } else { // no id to remove, nothing to be done
            }

//            final HashSet<Integer> tasksKeySetCopy = new HashSet<>(tasks.keySet()); // I need to use copy here, as I add new values to the map inside the for, by using repeatTask(); not true anymore, with recent modifications
            final Collection<RequestResponse> tasksToRepeat = new HashSet<>(2);
            for (final Integer id : this.tasks.keySet()) {
                final RequestResponse task = this.tasks.get(id);

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
                            logger.info("{}, needSessionToken[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                            Statics.needSessionToken.set(true);
                            tasksToRepeat.add(task);
//                            repeatTask(operationType, task.getRequest());
                            break;
                        case INVALID_CLOCK:
                        case UNEXPECTED_ERROR:
                        case TIMEOUT:
                            logger.error("{} in streamClient[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                            tasksToRepeat.add(task);
//                            repeatTask(operationType, task.getRequest());
                            break;
                        case INVALID_INPUT:
                        case INVALID_REQUEST:
                            logger.error("{} in streamClient[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                            break;
                        case SUBSCRIPTION_LIMIT_EXCEEDED:
                            logger.error("{} in streamClient[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                            final String errorMessage = statusMessage.getErrorMessage();
                            // trying to subscribe to 1001 markets
                            final String beginMarker = "trying to subscribe to ", endMarker = " markets";
                            final int beginMarkerIndex = errorMessage.indexOf(beginMarker);
                            final int endMarkerIndex = errorMessage.indexOf(endMarker);
                            if (beginMarkerIndex >= 0 && endMarkerIndex > beginMarkerIndex) {
                                final String numberString = errorMessage.substring(beginMarkerIndex + beginMarker.length(), endMarkerIndex);
                                try {
                                    final int number = Integer.parseInt(numberString);
                                    if (number > 1000) {
                                        final int difference = number - 1000;

                                        removeMarketsFromSet(difference);
                                    } else {
                                        logger.error("bad number in streamClient[{}] for: {} {}", this.client.id, number, errorMessage);
                                    }
                                } catch (NumberFormatException e) {
                                    logger.error("NumberFormatException in streamClient[{}] for: {} {}", this.client.id, numberString, errorMessage, e);
                                }
                            } else {
                                logger.error("bad errorMessage in streamClient[{}]: {} {} {}", this.client.id, beginMarkerIndex, endMarkerIndex, errorMessage);
                            }
                            break;
                        case CONNECTION_FAILED:
                            logger.error("{} in streamClient[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                            tasksToRepeat.add(task);
//                            repeatTask(operationType, task.getRequest());
                            break;
                        case MAX_CONNECTION_LIMIT_EXCEEDED:
                            logger.error("{} in streamClient[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                            tasksToRepeat.add(task);
//                            repeatTask(operationType, task.getRequest());
                            break;
                        default:
                            logger.error("[{}]unknown errorCode {} for: {}", this.client.id, errorCode, Generic.objectToString(task));
                            break;
                    }
                    if (errorCode != ErrorCode.SUBSCRIPTION_LIMIT_EXCEEDED) {
                        this.client.setStreamError(true);
                    }
                    idsToRemove.add(id);
                } else if (task.isExpired()) {
                    if (Statics.needSessionToken.get() || Statics.sessionTokenObject.isRecent()) {
                        logger.info("[{}]maintenance, task expired: {}", this.client.id, id);
                    } else {
                        logger.error("[{}]maintenance, task expired: {}", this.client.id, Generic.objectToString(task));
                    }
                    idsToRemove.add(id);
//                    final RequestMessage requestMessage = task.getRequest();
                    tasksToRepeat.add(task);
//                    repeatTask(operationType, requestMessage);
                } else { // task not finished and no expired, nothing to be done
                }
            } // end for

            if (!idsToRemove.isEmpty()) {
                for (final Integer id : idsToRemove) {
                    logger.info("[{}]removing task in tasksMaintenance end: {} {}", this.client.id, id, Generic.objectToString(this.tasks.get(id)));
                    final long currentTime = System.currentTimeMillis();
                    this.previousIds.put(id, currentTime);
                    this.tasks.remove(id);
                }
                idsToRemove.clear();
            } else { // no id to remove, nothing to be done
            }
            if (!tasksToRepeat.isEmpty()) {
                for (final RequestResponse task : tasksToRepeat) {
                    repeatTask(task);
                }
                tasksToRepeat.clear();
            } else { // no tasks to repeat, nothing to be done
            }
        } else { // no tasks, nothing to be done
        }
        if (!this.previousIds.isEmpty()) {
            this.previousIds.keySet().removeIf((Integer id) -> !isIdRecentlyRemoved(id, Generic.MINUTE_LENGTH_MILLISECONDS * 10L));
//            final Iterator<Integer> iterator = previousIds.keySet().iterator();
//            while (iterator.hasNext()) {
//                final Integer id = iterator.next();
//                if (!isIdRecentlyRemoved(id, Generic.MINUTE_LENGTH_MILLISECONDS * 10L)) {
//                    iterator.remove();
//                }
//            }
        } else { // no previous tasks, nothing to be done
        }
    }

    private synchronized void repeatTask(final RequestResponse task) {
//        final RequestMessage newRequestMessage = new RequestMessage(requestMessage); // if I use the previous request message and modify it, I'll run into some very nasty bugs; not true
        final RequestOperationType operationType = task.getRequestOperationType();
        final RequestMessage requestMessage = task.getRequest();
        final boolean similarTaskExists = similarTaskExists(operationType);
        switch (operationType) {
            case heartbeat: // I won't repeat expired heartbeat, as sometimes it seems no reply is sent if there's a lot of other stuff sent by the server; if there's need for it, another heartbeat will be sent
//                if (!similarTaskExists) {
//                    heartbeat((HeartbeatMessage) requestMessage, false);
//                }
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
                logger.error("[{}]unknown operation type in maintenance for: {} {}", this.client.id, operationType, Generic.objectToString(requestMessage));
                break;
        }
    }

    private synchronized boolean similarTaskExists(final RequestOperationType operationType) {
        boolean foundSimilar = false;
        for (final RequestResponse task : this.tasks.values()) {
            final RequestOperationType existingOperation = task.getRequestOperationType();
            if (operationType == existingOperation) {
                foundSimilar = true;
                break;
            }
        }

        return foundSimilar;
    }

    @Override
    public void run() {
//        tasksMaintenance.scheduleAtFixedRate(this::tasksMaintenance, 2_000L, 2_000L, TimeUnit.MILLISECONDS);
        if (this.client.id == 0) {
            orderSubscription(ClientCommands.createOrderSubscriptionMessage(this.client));
        }

        int counter = 0;
        while (!Statics.mustStop.get()) {
            try {
                if (this.client.readerThread.bufferNotEmpty.get()) {
                    final String line = this.client.readerThread.pollLine();
                    receiveLine(line);
                }

                Generic.threadSleepSegmented(5_000L, 10L, this.client.readerThread.bufferNotEmpty, Statics.mustStop);
                tasksMaintenance();
                if (counter % 6 == 0) {
                    keepAliveCheck();
                }
                counter++;
            } catch (Throwable throwable) {
                logger.error("[{}]STRANGE ERROR inside stream processor loop", this.client.id, throwable);
            }
        } // end while

//        if (tasksMaintenance != null) {
//            try {
//                tasksMaintenance.shutdown();
//                if (!tasksMaintenance.awaitTermination(1L, TimeUnit.MINUTES)) {
//                    logger.error("[{}]tasksMaintenance hanged", client.id);
//                    final List<Runnable> runnableList = tasksMaintenance.shutdownNow();
//                    if (!runnableList.isEmpty()) {
//                        logger.error("[{}]tasksMaintenance not commenced: {}", client.id, runnableList.size());
//                    }
//                }
//            } catch (InterruptedException e) {
//                logger.error("[{}]InterruptedException during tasksMaintenance awaitTermination during stream end", client.id, e);
//            }
//        } else {
//            logger.error("[{}]tasksMaintenance null during stream end", client.id);
//        }
    }
}
