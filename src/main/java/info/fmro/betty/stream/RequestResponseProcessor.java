package info.fmro.betty.stream;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.objects.SharedStatics;
import info.fmro.shared.stream.definitions.AuthenticationMessage;
import info.fmro.shared.stream.definitions.ConnectionMessage;
import info.fmro.shared.stream.definitions.HeartbeatMessage;
import info.fmro.shared.stream.definitions.MarketChange;
import info.fmro.shared.stream.definitions.MarketChangeMessage;
import info.fmro.shared.stream.definitions.MarketSubscriptionMessage;
import info.fmro.shared.stream.definitions.OrderChangeMessage;
import info.fmro.shared.stream.definitions.OrderMarketChange;
import info.fmro.shared.stream.definitions.OrderSubscriptionMessage;
import info.fmro.shared.stream.definitions.RequestMessage;
import info.fmro.shared.stream.definitions.ResponseMessage;
import info.fmro.shared.stream.definitions.StatusMessage;
import info.fmro.shared.stream.enums.ErrorCode;
import info.fmro.shared.stream.enums.RequestOperationType;
import info.fmro.shared.stream.enums.StatusCode;
import info.fmro.shared.stream.protocol.ChangeMessage;
import info.fmro.shared.stream.protocol.ChangeMessageFactory;
import info.fmro.shared.stream.protocol.ConnectionStatus;
import info.fmro.shared.stream.protocol.MixInResponseMessage;
import info.fmro.shared.stream.protocol.RequestResponse;
import info.fmro.shared.stream.protocol.SubscriptionHandler;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass", "OverlyCoupledClass"})
class RequestResponseProcessor
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponseProcessor.class);
    private final Client client;
    private final ObjectMapper objectMapper;
    private final AtomicInteger nextId = new AtomicInteger();
    private final HashMap<Integer, RequestResponse> tasks = new HashMap<>(4);
    private final HashSet<String> marketsSet;

    //subscription handlers
    @Nullable
    private SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> marketSubscriptionHandler;
    @Nullable
    private SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> orderSubscriptionHandler;
    private final Map<Integer, Long> previousIds = new HashMap<>(1);

    private ConnectionStatus status = ConnectionStatus.STOPPED;

    private long lastRequestTime = Long.MAX_VALUE;
    private long lastResponseTime = Long.MAX_VALUE;

    private int traceChangeTruncation; // size to which some info messages from the processor are truncated

    @SuppressWarnings("TransientFieldInNonSerializableClass")
    private transient boolean mcmCommandReceived;
    @SuppressWarnings("TransientFieldInNonSerializableClass")
    private transient boolean ocmCommandReceived;

    RequestResponseProcessor(final Client client) {
        super();
        this.client = client;
        this.marketsSet = new HashSet<>(4);
        this.objectMapper = new ObjectMapper();

        this.objectMapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE); // remove visibility of everything, including getters/setters
        this.objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY); // add full visibility for fields

        this.objectMapper.addMixIn(ResponseMessage.class, MixInResponseMessage.class);
//        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // I'm curious what failures will apear now that I disabled this line
    }

    synchronized boolean hasValidHandler() {
        return this.marketSubscriptionHandler != null || this.orderSubscriptionHandler != null;
    }

    private synchronized int nMarkets() {
        return this.marketsSet.size();
    }

    @NotNull
    synchronized HashSet<String> getMarketsSet() {
        @NotNull final HashSet<String> returnValue;
        if (this.marketsSet == null) {
            logger.error("[{}]null marketsSet in processor", this.client.id);
            returnValue = new HashSet<>(2);
        } else {
            returnValue = new HashSet<>(this.marketsSet);
        }

        return returnValue;
    }

    @SuppressWarnings("UnusedReturnValue")
    synchronized boolean setMarketsSet(@NotNull final Collection<String> markets) {
        final int newSize = markets.size();
        final boolean modified;
        if (newSize <= 1_000) {
            this.marketsSet.clear();
            this.marketsSet.addAll(markets);
            modified = true;
        } else {
            logger.error("[{}]trying to set too many markets: {}", this.client.id, newSize);
            modified = false;
        }
        if (modified) {
            @NotNull final HashSet<String> localMarketsSet = getMarketsSet();
            if (localMarketsSet.isEmpty()) { // nothing to subscribe to
            } else {
                marketSubscription(ClientCommands.createMarketSubscriptionMessage(this.client, getMarketsSet()));
            }
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

    private synchronized ConnectionStatus getStatus() {
        return this.status;
    }

    public synchronized int getTraceChangeTruncation() {
        return this.traceChangeTruncation;
    }

    synchronized void setTraceChangeTruncation(final int traceChangeTruncation) {
        this.traceChangeTruncation = traceChangeTruncation;
        logger.info("[{}]stream processor messages will be truncated to size: {}", this.client.id, this.traceChangeTruncation);
    }

    @Contract(pure = true)
    private synchronized long getLastRequestTime() {
        return this.lastRequestTime;
    }

    @Contract(pure = true)
    private synchronized long getLastResponseTime() {
        return this.lastResponseTime;
    }

    private synchronized void resetProcessor() {
        tasksMaintenance();
//        resetConnectionMessage();

        final Iterable<RequestResponse> tasksValuesCopy = new ArrayList<>(this.tasks.values());
        this.tasks.clear(); // can be refilled by this method

        final long currentTime = System.currentTimeMillis();
        for (final RequestResponse task : tasksValuesCopy) {
            final int taskId = task.getId();
            this.previousIds.put(taskId, currentTime);
            repeatTask(task);
        }
//        tasks = new HashMap<>();

        if (this.mcmCommandReceived) {
            this.mcmCommandReceived = false;
            ClientHandler.threadsWithMcmCommandReceived.getAndDecrement();
        }
        if (this.ocmCommandReceived) {
            this.ocmCommandReceived = false;
            ClientHandler.threadsWithOcmCommandReceived.getAndDecrement();
        }
    }

    synchronized void disconnected(final boolean isShuttingDown) {
        if (this.status == ConnectionStatus.DISCONNECTED || (isShuttingDown && this.status == ConnectionStatus.STOPPED)) { // already disconnected, nothing to be done
        } else {
            setStatus(ConnectionStatus.DISCONNECTED);
            resetProcessor();
        }
    }

    synchronized void stopped() {
        if (this.status == ConnectionStatus.STOPPED) { // already stopped, nothing to be done
        } else {
            this.marketSubscriptionHandler = null;
            this.orderSubscriptionHandler = null;
            setStatus(ConnectionStatus.STOPPED);
            resetProcessor();
        }
    }

    @Nullable
    private synchronized MarketSubscriptionMessage getMarketResubscribeMessage() {
        @Nullable final MarketSubscriptionMessage resub;
        if (this.marketSubscriptionHandler != null) {
            resub = this.marketSubscriptionHandler.getSubscriptionMessage();
            resub.setInitialClk(this.marketSubscriptionHandler.getInitialClk());
            resub.setClk(this.marketSubscriptionHandler.getClk());
        } else {
            resub = null;
        }
        return resub;
    }

    @Nullable
    private synchronized OrderSubscriptionMessage getOrderResubscribeMessage() {
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

    synchronized void resubscribe() {
        final boolean marketSubscriptionDoesNotExist = similarTaskDoesNotExist(RequestOperationType.marketSubscription);
        if (marketSubscriptionDoesNotExist) {
            //Resub markets
            final MarketSubscriptionMessage marketSubscriptionMessage = getMarketResubscribeMessage();
            if (marketSubscriptionMessage != null) {
                logger.info("[{}]Resubscribe to market subscription.", this.client.id);
                marketSubscription(marketSubscriptionMessage);
            }
        }

        final boolean orderSubscriptionDoesNotExist = similarTaskDoesNotExist(RequestOperationType.orderSubscription);
        if (orderSubscriptionDoesNotExist) {
            //Resub orders
            final OrderSubscriptionMessage orderSubscriptionMessage = getOrderResubscribeMessage();
            if (orderSubscriptionMessage != null) {
                logger.info("[{}]Resubscribe to order subscription.", this.client.id);
                orderSubscription(orderSubscriptionMessage);
            }
        }
    }

    @Nullable
    public synchronized SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> getMarketSubscriptionHandler() {
        return this.marketSubscriptionHandler;
    }

    private synchronized void setMarketSubscriptionHandler(final SubscriptionHandler<MarketSubscriptionMessage, ChangeMessage<MarketChange>, MarketChange> newHandler) {
        if (this.marketSubscriptionHandler != null) {
            this.marketSubscriptionHandler.cancel();
        }
        this.marketSubscriptionHandler = newHandler;
        if (this.marketSubscriptionHandler != null) {
            setStatus(ConnectionStatus.SUBSCRIBED);
        }
    }

    @Nullable
    public synchronized SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> getOrderSubscriptionHandler() {
        return this.orderSubscriptionHandler;
    }

    private synchronized void setOrderSubscriptionHandler(final SubscriptionHandler<OrderSubscriptionMessage, ChangeMessage<OrderMarketChange>, OrderMarketChange> newHandler) {
        if (this.orderSubscriptionHandler != null) {
            this.orderSubscriptionHandler.cancel();
        }
        this.orderSubscriptionHandler = newHandler;
        if (this.orderSubscriptionHandler != null) {
            setStatus(ConnectionStatus.SUBSCRIBED);
        }
    }

    synchronized void authenticate(final AuthenticationMessage message) {
        createHeader(message, RequestOperationType.authentication);
        sendMessage(message, success -> successfulAuth(), true);
    }

    private synchronized void keepAliveCheck() {
        if (getStatus() == ConnectionStatus.SUBSCRIBED) { //connection looks up
            if (getLastRequestTime() + Client.keepAliveHeartbeat < System.currentTimeMillis()) { //send a heartbeat to server to keep networks open
                logger.info("[{}]Last Request Time is longer than {}: Sending Keep Alive Heartbeat", this.client.id, Client.keepAliveHeartbeat);
                heartbeat();
            } else if (getLastResponseTime() + Client.TIMEOUT < System.currentTimeMillis()) {
                logger.info("[{}]Last Response Time is longer than timeout {}: Sending Keep Alive Heartbeat", this.client.id, Client.TIMEOUT);
                heartbeat();
            }
        }
    }

    private synchronized void heartbeat() {
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

    private synchronized void sendMessage(@NotNull final RequestMessage message, final Consumer<RequestResponse> onSuccess, final boolean isAuthLine) {
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

    // Sets the header on the message and assigns an id
    private synchronized void createHeader(final RequestMessage msg, final RequestOperationType op) {
        setMessageId(msg);
        msg.setOp(op);
    }

    private synchronized void setMessageId(@NotNull final RequestMessage msg) {
        final int id = this.nextId.incrementAndGet();
        msg.setId(id);
    }

    // Processes an inbound (response) line of json
    @SuppressWarnings("UnusedReturnValue")
    private synchronized ResponseMessage processLine(final String line) {
        //clear last response
        ResponseMessage message = null;
        try {
            message = this.objectMapper.readValue(line, ResponseMessage.class);
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") IOException e) {
            logger.error("[{}]IOException in processLine: {}", this.client.id, line, e);
        }

        this.lastResponseTime = System.currentTimeMillis();
        if (message != null) {
            switch (message.getOp()) {
                case connection -> {
                    logger.info("[{}]ESA->Client: {}", this.client.id, line);
                    processConnectionMessage((ConnectionMessage) message);
                }
                case status -> {
                    logger.info("[{}]ESA->Client: {}", this.client.id, line);
                    processStatusMessage((StatusMessage) message);
                }
                case mcm -> {
                    traceChange(line);
                    processMarketChangeMessage((MarketChangeMessage) message);
                }
                case ocm -> {
                    traceChange(line);
                    processOrderChangeMessage((OrderChangeMessage) message);
                }
                default -> logger.error("[{}]ESA->Client: Unknown message type: {}, message:{}", this.client.id, message.getOp(), line);
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
                SharedStatics.orderCache.listOfQueues.send(message);
                SharedStatics.orderCache.onOrderChange(change, Statics.rulesManagerThread.rulesManager);
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
                    SharedStatics.marketCache.listOfQueues.send(message);
                    SharedStatics.marketCache.onMarketChange(change, Statics.rulesManagerThread.rulesManager, Statics.marketCataloguesMap);
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

    private synchronized void processStatusMessage(@NotNull final StatusMessage statusMessage) {
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
                        if (SharedStatics.needSessionToken.get() || SharedStatics.sessionTokenObject.isRecent()) {
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
                        SharedStatics.needSessionToken.set(true);
                        break;
                    case INVALID_CLOCK:
                    case UNEXPECTED_ERROR:
                    case INVALID_INPUT:
                    case INVALID_REQUEST:
                    case SUBSCRIPTION_LIMIT_EXCEEDED:
                    case CONNECTION_FAILED:
                    case TOO_MANY_REQUESTS:
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
        return isIdRecentlyRemoved(id, Generic.MINUTE_LENGTH_MILLISECONDS << 1); // default value
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

    private synchronized void processConnectionMessage(@SuppressWarnings("unused") final ConnectionMessage message) {
//        connectionMessage.setResponse(message);
        setStatus(ConnectionStatus.CONNECTED);
        this.client.streamIsConnected.set(true);
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod"})
    private synchronized void tasksMaintenance() {
        if (this.tasks.isEmpty()) { // no tasks, nothing to be done
        } else {
            final Collection<Integer> idsToRemove = new HashSet<>(2);
            int lastHeartbeat = 0, lastAuth = 0, lastOrderSub = 0, lastMarketSub = 0;
            for (final Integer id : this.tasks.keySet()) {
                if (id == null) {
                    logger.error("[{}]null key in tasks, will remove it", this.client.id);
                    idsToRemove.add(id);
                } else if (id <= 0) {
                    logger.error("[{}]wrong value key in tasks, will remove it: {} {}", this.client.id, id, Generic.objectToString(this.tasks.get(id)));
                    idsToRemove.add(id);
                } else {
                    final RequestResponse task = this.tasks.get(id);
                    final RequestOperationType operationType = task.getRequestOperationType();
                    //noinspection SwitchStatementDensity
                    switch (operationType) {
                        case heartbeat:
                            if (lastHeartbeat == 0) {
                                lastHeartbeat = id;
                            } else if (lastHeartbeat > id) {
                                logger.error("[{}]removing previous heartbeat: {} recent:{} {}", this.client.id, id, lastHeartbeat, Generic.objectToString(task));
                                idsToRemove.add(id);
                            } else if (lastHeartbeat < id) {
                                logger.error("[{}]removing previous heartbeat: {} recent:{} {}", this.client.id, lastHeartbeat, id, Generic.objectToString(task));
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
                                logger.error("[{}]removing previous auth: {} recent:{} {}", this.client.id, id, lastAuth, Generic.objectToString(task));
                                idsToRemove.add(id);
                            } else if (lastAuth < id) {
                                logger.error("[{}]removing previous auth: {} recent:{} {}", this.client.id, lastAuth, id, Generic.objectToString(task));
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
                                logger.error("[{}]removing previous orderSubscription: {} recent:{} {}", this.client.id, id, lastOrderSub, Generic.objectToString(task));
                                idsToRemove.add(id);
                            } else if (lastOrderSub < id) {
                                logger.error("[{}]removing previous orderSubscription: {} recent:{} {}", this.client.id, lastOrderSub, id, Generic.objectToString(task));
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
                                logger.error("[{}]removing previous marketSubscription: {} recent:{} {}", this.client.id, id, lastMarketSub, Generic.objectToString(task));
                                idsToRemove.add(id);
                            } else if (lastMarketSub < id) {
                                logger.error("[{}]removing previous marketSubscription: {} recent:{} {}", this.client.id, lastMarketSub, id, Generic.objectToString(task));
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

            if (idsToRemove.isEmpty()) { // no id to remove, nothing to be done
            } else {
                for (final Integer id : idsToRemove) {
//                    logger.info("[{}]removing task in tasksMaintenance: {} {}", this.client.id, id, Generic.objectToString(this.tasks.get(id)));
                    final long currentTime = System.currentTimeMillis();
                    this.previousIds.put(id, currentTime);
                    this.tasks.remove(id);
                }
                idsToRemove.clear();
            }

//            final HashSet<Integer> tasksKeySetCopy = new HashSet<>(tasks.keySet()); // I need to use copy here, as I add new values to the map inside the for, by using repeatTask(); not true anymore, with recent modifications
            final Collection<RequestResponse> tasksToRepeat = new HashSet<>(2);
            for (final Map.Entry<Integer, RequestResponse> entry : this.tasks.entrySet()) {
                final Integer id = entry.getKey();
                final RequestResponse task = entry.getValue();

                if (task.isTaskSuccessful()) {
                    idsToRemove.add(id);
                } else if (task.isTaskFinished()) { // tasks with error are not normally repeated, but in the future I might change this for some error codes
                    final StatusMessage statusMessage = task.getStatusMessage();
                    final ErrorCode errorCode = statusMessage.getErrorCode();
                    //noinspection EnhancedSwitchMigration
                    switch (errorCode) {
                        case NOT_AUTHORIZED:
                        case NO_APP_KEY:
                        case INVALID_APP_KEY:
                        case NO_SESSION:
                        case INVALID_SESSION_INFORMATION:
                            logger.info("{}, needSessionToken[{}]: {}", errorCode, this.client.id, Generic.objectToString(statusMessage));
                            SharedStatics.needSessionToken.set(true);
                            tasksToRepeat.add(task);
//                            repeatTask(operationType, task.getRequest());
                            break;
                        case INVALID_CLOCK:
                        case UNEXPECTED_ERROR:
                        case TIMEOUT:
                        case CONNECTION_FAILED:
                        case TOO_MANY_REQUESTS:
                        case MAX_CONNECTION_LIMIT_EXCEEDED:
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
                                    if (number > 1_000) {
                                        final int difference = number - 1_000;

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
                        //                            repeatTask(operationType, task.getRequest());
                        default:
                            logger.error("[{}]unknown errorCode {} for: {}", this.client.id, errorCode, Generic.objectToString(task));
                            break;
                    }
                    if (errorCode != ErrorCode.SUBSCRIPTION_LIMIT_EXCEEDED) {
                        this.client.setStreamError(true);
                    }
                    logger.info("[{}]removing failed task: {} {}", this.client.id, id, Generic.objectToString(task));
                    idsToRemove.add(id);
                } else if (task.isExpired()) {
                    if (SharedStatics.needSessionToken.get() || SharedStatics.sessionTokenObject.isRecent()) {
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

            if (idsToRemove.isEmpty()) { // no id to remove, nothing to be done
            } else {
                for (final Integer id : idsToRemove) {
//                    logger.info("[{}]removing task in tasksMaintenance end: {} {}", this.client.id, id, Generic.objectToString(this.tasks.get(id)));
                    final long currentTime = System.currentTimeMillis();
                    this.previousIds.put(id, currentTime);
                    this.tasks.remove(id);
                }
                idsToRemove.clear();
            }
            if (tasksToRepeat.isEmpty()) { // no tasks to repeat, nothing to be done
            } else {
                for (final RequestResponse task : tasksToRepeat) {
                    repeatTask(task);
                }
                tasksToRepeat.clear();
            }
        }
        if (this.previousIds.isEmpty()) { // no previous tasks, nothing to be done
        } else {
            this.previousIds.keySet().removeIf((Integer id) -> !isIdRecentlyRemoved(id, Generic.MINUTE_LENGTH_MILLISECONDS * 10L));
        }
    }

    private synchronized void repeatTask(@NotNull final RequestResponse task) {
        final RequestOperationType operationType = task.getRequestOperationType();
        final RequestMessage requestMessage = task.getRequest();
        final boolean similarTaskDoesNotExist = similarTaskDoesNotExist(operationType);
        switch (operationType) {
            case heartbeat: // I won't repeat expired heartbeat, as sometimes it seems no reply is sent if there's a lot of other stuff sent by the server; if there's need for it, another heartbeat will be sent
//                if (!similarTaskExists) {
//                    heartbeat((HeartbeatMessage) requestMessage, false);
//                }
                break;
            case authentication: // reauth should be done automatically, without reusing this message
                break;
            case orderSubscription:
                if (similarTaskDoesNotExist) {
                    orderSubscription((OrderSubscriptionMessage) requestMessage, false);
                }
                break;
            case marketSubscription:
                if (similarTaskDoesNotExist) {
                    marketSubscription((MarketSubscriptionMessage) requestMessage, false);
                }
                break;
            default:
                logger.error("[{}]unknown operation type in maintenance for: {} {}", this.client.id, operationType, Generic.objectToString(requestMessage));
                break;
        }
    }

    private synchronized boolean similarTaskDoesNotExist(final RequestOperationType operationType) {
        boolean similarNotFound = true;
        for (final RequestResponse task : this.tasks.values()) {
            final RequestOperationType existingOperation = task.getRequestOperationType();
            if (operationType == existingOperation) {
                similarNotFound = false;
                break;
            }
        }
        return similarNotFound;
    }

    @Override
    public void run() {
//        tasksMaintenance.scheduleAtFixedRate(this::tasksMaintenance, 2_000L, 2_000L, TimeUnit.MILLISECONDS);
        if (this.client.id == 0) {
            orderSubscription(ClientCommands.createOrderSubscriptionMessage(this.client));
        }

        int counter = 0;
        while (!SharedStatics.mustStop.get()) {
            try {
                if (this.client.readerThread.bufferNotEmpty.get()) {
                    final String line = this.client.readerThread.pollLine();
                    processLine(line);
                }

                Generic.threadSleepSegmented(5_000L, 10L, this.client.readerThread.bufferNotEmpty, SharedStatics.mustStop);
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
