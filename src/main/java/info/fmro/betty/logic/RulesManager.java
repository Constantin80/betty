package info.fmro.betty.logic;

import info.fmro.shared.enums.RulesManagerModificationCommand;
import info.fmro.betty.main.VarsIO;
import info.fmro.shared.stream.objects.ListOfQueues;
import info.fmro.betty.objects.ManagedEventsMap;
import info.fmro.shared.stream.objects.RulesManagerModification;
import info.fmro.betty.objects.RulesManagerStringObject;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.cache.util.Utils;
import info.fmro.betty.stream.client.ClientHandler;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.stream.objects.StreamObjectInterface;
import info.fmro.shared.utility.SynchronizedMap;
import info.fmro.shared.utility.SynchronizedSafeSet;
import info.fmro.shared.utility.SynchronizedSet;
import org.apache.commons.lang3.SerializationUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"WeakerAccess", "OverlyComplexClass", "ClassWithTooManyMethods"})
public class RulesManager
        extends Thread
        implements Serializable, StreamObjectInterface {
    private static final Logger logger = LoggerFactory.getLogger(RulesManager.class);
    private static final long serialVersionUID = -3496383465286913313L;
    public static final long fullCheckPeriod = Generic.MINUTE_LENGTH_MILLISECONDS;
    public transient ListOfQueues listOfQueues = new ListOfQueues();
    public final ManagedEventsMap events = new ManagedEventsMap(); // managedEvents are permanently stored only here
    public final SynchronizedMap<String, ManagedMarket> markets = new SynchronizedMap<>(); // managedMarkets are permanently stored only here
    public final SynchronizedSafeSet<RulesManagerStringObject> marketsToCheck = new SynchronizedSafeSet<>();
    public final SynchronizedSet<String> addManagedRunnerCommands = new SynchronizedSet<>();
    public final AtomicBoolean newAddManagedRunnerCommand = new AtomicBoolean();
    public final AtomicBoolean newOrderMarketCreated = new AtomicBoolean();
    public final AtomicBoolean marketsToCheckExist = new AtomicBoolean();
    public final AtomicBoolean marketsMapModified = new AtomicBoolean();
    public final AtomicLong marketsToCheckStamp = new AtomicLong();
    public final AtomicLong addManagedMarketsForExistingOrdersStamp = new AtomicLong();
    public final AtomicBoolean rulesHaveChanged = new AtomicBoolean();
    public final AtomicBoolean orderCacheHasReset = new AtomicBoolean();
    private long timeLastFullCheck;

    private Integer testMarker; // this variable should be the last declared and not be primitive, to attempt to have it serialized last

    // todo test with 1 runner, no cross runner matching; amountLimit on back and lay, limit on market & event; time limit; min odds for back and max odds for lay, with bets depending on prices existing on that runner
    // todo code beautification and simple tests, to prepare for the far more complicated integration tests

    private void readObject(@NotNull final java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.listOfQueues = new ListOfQueues();
    }

    public synchronized RulesManager getCopy() {
        return SerializationUtils.clone(this);
    }

    public synchronized int runAfterReceive() {
        return 0;
    }

    public synchronized void runBeforeSend() {
    }

    public synchronized boolean copyFrom(final RulesManager other) {
        final boolean readSuccessful;
        if (!this.events.isEmpty() || !this.markets.isEmpty()) {
            logger.error("not empty map in RulesManager copyFrom: {}", Generic.objectToString(this));
            readSuccessful = false;
        } else {
            if (other == null) {
                logger.error("null other in copyFrom for: {}", Generic.objectToString(this));
                readSuccessful = false;
            } else {
                this.events.copyFrom(other.events);
//                    this.events.clear();
//                    this.events.putAll(other.events);

                this.markets.clear();
                this.markets.putAll(other.markets.copy());

                this.setTestMarker(other.testMarker);

                if (Statics.resetTestMarker) {
                    logger.error("resetTestMarker {} , will still exit the program after reset", this.testMarker);
                    final boolean objectModified = this.setTestMarker(Statics.TEST_MARKER);
                    if (objectModified) {
                        VarsIO.writeSettings();
                    }
                    readSuccessful = false; // in order to exit the program after reset
                } else {
                    readSuccessful = this.testMarker != null && this.testMarker == Statics.TEST_MARKER; // testMarker needs to have a certain value
                }

                if (this.markets.isEmpty()) { // map still empty, no modification was made
                } else {
                    this.rulesHaveChanged.set(true);
                    this.marketsMapModified.set(true);
                }

                final int nQueues = this.listOfQueues.size();
                if (nQueues == 0) { // normal case, nothing to be done
                } else {
                    logger.error("existing queues during RulesManager.copyFrom: {} {}", nQueues, Generic.objectToString(this));
                    this.listOfQueues.send(this.getCopy());
                }
            }
        }
        associateMarketsWithEvents();

        return readSuccessful;
    }

    private synchronized void associateMarketsWithEvents() {
        for (final ManagedMarket managedMarket : this.markets.valuesCopy()) {
            managedMarket.getParentEvent(); // this does the reciprocal association as well, by adding the markets in the managedEvent objects
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized boolean checkMarketsAreAssociatedWithEvents() {
        boolean foundError = false;
        for (final ManagedMarket managedMarket : this.markets.valuesCopy()) {
            if (!managedMarket.parentEventIsSet() || !managedMarket.parentEventHasTheMarketAdded() || !managedMarket.parentEventHasTheMarketIdAdded()) { // error messages are printed inside the methods
                managedMarket.getParentEvent(); // this should solve the problem
                foundError = true;
            }
        }
        return foundError;
    }

//    public synchronized ManagedEvent getParentEvent(String eventId) {
//        ManagedEvent managedEvent = this.events.get(eventId);
//        if (managedEvent == null) {
//            managedEvent = new ManagedEvent(eventId);
//            this.events.put(eventId, managedEvent);
//            Statics.rulesManager.rulesHaveChanged.set(true);
//        } else { // I got the event and I'll return it, nothing else to be done
//        }
//
//        return managedEvent;
//    }

    private synchronized boolean setTestMarker(final int newValue) {
        final boolean modified;
        if (this.testMarker != null && this.testMarker == newValue) {
            modified = false;
        } else {
            this.testMarker = newValue;
            modified = true;
        }

        return modified;
    }

    private synchronized ManagedEvent addManagedEvent(final String eventId) {
        final ManagedEvent managedEvent;
        if (this.events.containsKey(eventId)) {
            managedEvent = this.events.get(eventId);
        } else {
            managedEvent = new ManagedEvent(eventId);
            this.listOfQueues.send(new RulesManagerModification(RulesManagerModificationCommand.addManagedEvent, eventId, managedEvent));
            this.events.put(eventId, managedEvent);
            this.rulesHaveChanged.set(true);
        }
        return managedEvent;
    }

    private synchronized ManagedMarket addManagedMarket(final String marketId) {
        final ManagedMarket managedMarket;
        if (this.markets.containsKey(marketId)) {
            managedMarket = this.markets.get(marketId);
        } else {
            managedMarket = new ManagedMarket(marketId);
            this.listOfQueues.send(new RulesManagerModification(RulesManagerModificationCommand.addManagedMarket, marketId, managedMarket));
            this.markets.put(marketId, managedMarket);
            checkMarketsAreAssociatedWithEvents();
            this.rulesHaveChanged.set(true);
            this.marketsMapModified.set(true);
        }
        return managedMarket;
    }

    private synchronized void addManagedRunner(final String marketId, final long selectionId, final Double handicap, final double minBackOdds, final double maxLayOdds, final double backAmountLimit, final double layAmountLimit,
                                               final double marketAmountLimit, final double eventAmountLimit) {
        final ManagedMarket managedMarket = this.addManagedMarket(marketId);
        if (managedMarket != null) {
            managedMarket.updateRunner(selectionId, handicap, minBackOdds, maxLayOdds, backAmountLimit, layAmountLimit);
            managedMarket.setAmountLimit(marketAmountLimit);
            if (Double.isNaN(eventAmountLimit)) { // nothing to do, checked here for performance reason
            } else {
                final ManagedEvent managedEvent = managedMarket.getParentEvent();
                managedEvent.setAmountLimit(eventAmountLimit);
            }
        } else {
            logger.error("null managedMarket in addManagedRunner for: {} {} {} {} {} {} {} {} {}", marketId, selectionId, handicap, minBackOdds, maxLayOdds, backAmountLimit, layAmountLimit, marketAmountLimit, eventAmountLimit);
        }
    }

    @SuppressWarnings("unused")
    private synchronized ManagedMarket removeManagedMarket(final String marketId) {
        @Nullable final ManagedMarket managedMarket;
        if (this.markets.containsKey(marketId)) {
            this.listOfQueues.send(new RulesManagerModification(RulesManagerModificationCommand.removeManagedMarket, marketId));
            managedMarket = this.markets.remove(marketId);
            this.rulesHaveChanged.set(true);
            this.marketsMapModified.set(true);
        } else {
            managedMarket = null;
        }
        return managedMarket;
    }

    public synchronized void executeCommand(@NotNull final String commandString) {
        if (commandString.startsWith("event ")) {
            final String eventString = commandString.substring("event ".length()).trim();
            if (eventString.contains(" ")) {
                final String eventId = eventString.substring(0, eventString.indexOf(' '));
                final String eventCommand = eventString.substring(eventString.indexOf(' ') + " ".length()).trim();
                if (eventCommand.startsWith("amountLimit")) {
                    String amountLimit = eventCommand.substring("amountLimit".length()).trim();
                    if (!amountLimit.isEmpty() && amountLimit.charAt(0) == '=') {
                        amountLimit = amountLimit.substring("=".length()).trim();
                    } else { // nothing to do on this branch
                    }
                    double doubleValue;
                    try {
                        doubleValue = Double.valueOf(amountLimit);
                    } catch (NumberFormatException e) {
                        logger.error("NumberFormatException while getting doubleValue for amountLimit in executeCommand: {} {}", commandString, amountLimit, e);
                        doubleValue = Double.NaN;
                    }
                    if (Double.isNaN(doubleValue)) { // error message was already printed
                    } else {
                        final ManagedEvent managedEvent = addManagedEvent(eventId);
                        managedEvent.setAmountLimit(doubleValue);
                    }
                } else {
                    logger.error("unknown eventCommand in executeCommand: {} {}", eventCommand, commandString);
                }
            } else {
                logger.error("strange eventString in executeCommand: {} {}", eventString, commandString);
            }
        } else {
            logger.error("unknown command in executeCommand: {}", commandString);
        }
    }

    private synchronized boolean isMarketsToCheckStampRecent() { // stamp is updated, but not checked for anything yet
        return isMarketsToCheckStampRecent(500L); // default period
    }

    private synchronized boolean isMarketsToCheckStampRecent(final long recentPeriod) {
        return timeSinceMarketsToCheckStamp() <= recentPeriod;
    }

    private synchronized long timeSinceMarketsToCheckStamp() {
        final long currentTime = System.currentTimeMillis();
        final long stampTime = this.marketsToCheckStamp.get();
        return currentTime - stampTime;
    }

    private synchronized long timeSinceFullRun() {
        final long currentTime = System.currentTimeMillis();
        return currentTime - this.timeLastFullCheck;
    }

    private synchronized long timeTillNextFullRun() {
        final long result;
//        if (this.timeLastFullCheck <= 0) {
//            result = 0;
//        } else {
        result = fullCheckPeriod - timeSinceFullRun(); // negative values are acceptable
//        }
        return result;
    }

    private synchronized void calculateMarketLimits() {
        this.marketsMapModified.set(false);
        final double totalLimit = Statics.safetyLimits.getTotalLimit();
        Utils.calculateMarketLimits(totalLimit, this.markets.valuesCopy(), true, true);

        for (final ManagedEvent managedEvent : this.events.valuesCopy()) {
            managedEvent.calculateMarketLimits();
        }
    }

//    public synchronized void calculateMarketLimits(Collection<String> marketIds) {
//        final int nMarketIds = marketIds.size();
//        int nMarketCalculatedLimits = 0;
//        for (ManagedEvent managedEvent : this.events.valuesCopy()) {
//            boolean neededEvent = false;
//            for (String marketId : marketIds) {
//                if (managedEvent.marketIds.contains(marketId)) {
//                    neededEvent = true;
//                    nMarketCalculatedLimits++; // no break, as there might be more than one market attached to the same event and I should count that
//                } else { // marketId not contained in this event, nothing to be done
//                }
//            }
//            if (neededEvent) {
//                managedEvent.calculateMarketLimits();
//            } else { // I don't need to calculateMarketLimits for this event
//            }
//            if (nMarketCalculatedLimits >= nMarketIds) {
//                break;
//            }
//        }
//    }

    private synchronized void manageMarket(final ManagedMarket managedMarket) {
        if (managedMarket == null) {
            logger.error("null managedMarket to check in RulesManager");
            this.markets.removeValueAll(null);
            this.listOfQueues.send(this.getCopy());
            Statics.rulesManager.rulesHaveChanged.set(true);
        } else {
            managedMarket.manage();
        }
    }

    private synchronized void addManagedMarketsForExistingOrders() {
        final long currentTime = System.currentTimeMillis();
        final long stampTime = this.addManagedMarketsForExistingOrdersStamp.get();
        final long timeSinceStamp = currentTime - stampTime;
        if (this.newOrderMarketCreated.getAndSet(false) || timeSinceStamp > Generic.MINUTE_LENGTH_MILLISECONDS * 5L) {
            this.addManagedMarketsForExistingOrdersStamp.set(currentTime);
            final HashSet<String> orderMarkets = Statics.orderCache.getOrderMarketKeys();
            final Set<String> managedMarkets = this.markets.keySetCopy();
            orderMarkets.removeAll(managedMarkets);
            if (orderMarkets.isEmpty()) { // no new markets; nothing to be done, this should be the taken branch almost all the time
            } else {
                for (final String marketId : orderMarkets) {
                    logger.warn("adding new managed market in addManagedMarketsForExistingOrders: {}", marketId);
                    addManagedMarket(marketId);
                }
            }
        } else { // I won't run this method too often; nothing to be done
        }
    }

    private synchronized void resetOrderCacheObjects() {
        this.orderCacheHasReset.set(false);
        for (final ManagedMarket managedMarket : this.markets.valuesCopy()) {
            managedMarket.resetOrderCacheObjects();
        }
    }

    private synchronized void addManagedRunnerCommands() {
        this.newAddManagedRunnerCommand.set(false);
        final HashSet<String> setCopy = this.addManagedRunnerCommands.clear();

        for (final String addManagedRunnerCommand : setCopy) {
            parseAddManagedRunnerCommand(addManagedRunnerCommand);
        }
    }

    @SuppressWarnings("OverlyNestedMethod")
    private synchronized void parseAddManagedRunnerCommand(final String addManagedRunnerCommand) {
        // String:marketId long:selectionId Double:handicap(default:null)
        // double optionals:minBackOdds maxLayOdds backAmountLimit layAmountLimit marketAmountLimit eventAmountLimit
        final String[] argumentsArray = Generic.splitStringAroundSpaces(addManagedRunnerCommand);
        final int arrayLength = argumentsArray.length;
        if (arrayLength >= 2 && arrayLength <= 9) {
            final String marketId = argumentsArray[0];
            final String selectionIdString = argumentsArray[1];
            long selectionId = -1L;
            try {
                selectionId = Long.parseLong(selectionIdString);
            } catch (NumberFormatException e) {
                logger.error("NumberFormatException while parsing selectionId in parseAddManagedRunnerCommand for: {} {}", selectionIdString, addManagedRunnerCommand);
            }

            if (selectionId < 0L) { // error message already printed, nothing to be done
            } else {
                @Nullable Double handicap = Double.NaN;
                double minBackOdds = Double.NaN, maxLayOdds = Double.NaN, backAmountLimit = Double.NaN, layAmountLimit = Double.NaN, marketAmountLimit = Double.NaN, eventAmountLimit = Double.NaN;
                boolean errorExists = false;
                for (int i = 2; i < arrayLength; i++) {
                    switch (i) {
                        case 2:
                            if ("null".equals(argumentsArray[i])) {
                                handicap = null;
                            } else {
                                handicap = Generic.parseDouble(argumentsArray[i]);
                                if (Double.isNaN(handicap)) {
                                    errorExists = true;
                                }
                            }
                            break;
                        case 3:
                            minBackOdds = Generic.parseDouble(argumentsArray[i]);
                            if (Double.isNaN(minBackOdds)) {
                                errorExists = true;

                            }
                            break;
                        case 4:
                            maxLayOdds = Generic.parseDouble(argumentsArray[i]);
                            if (Double.isNaN(maxLayOdds)) {
                                errorExists = true;

                            }
                            break;
                        case 5:
                            backAmountLimit = Generic.parseDouble(argumentsArray[i]);
                            if (Double.isNaN(backAmountLimit)) {
                                errorExists = true;

                            }
                            break;
                        case 6:
                            layAmountLimit = Generic.parseDouble(argumentsArray[i]);
                            if (Double.isNaN(layAmountLimit)) {
                                errorExists = true;

                            }
                            break;
                        case 7:
                            marketAmountLimit = Generic.parseDouble(argumentsArray[i]);
                            if (Double.isNaN(marketAmountLimit)) {
                                errorExists = true;

                            }
                            break;
                        case 8:
                            eventAmountLimit = Generic.parseDouble(argumentsArray[i]);
                            if (Double.isNaN(eventAmountLimit)) {
                                errorExists = true;

                            }
                            break;
                        default:
                            logger.error("default switch branch entered inside parseAddManagedRunnerCommand for: {} {} {} {}", i, arrayLength, argumentsArray[i], addManagedRunnerCommand);
                            errorExists = true;
                            break;
                    }
                    if (errorExists) {
                        break;
                    }
                }
                if (errorExists) {
                    logger.error("bogus addManagedRunnerCommand: {} {}", arrayLength, addManagedRunnerCommand);
                } else {
                    addManagedRunner(marketId, selectionId, handicap, minBackOdds, maxLayOdds, backAmountLimit, layAmountLimit, marketAmountLimit, eventAmountLimit);
                }
            }
        } else {
            logger.error("wrong arrayLength in parseAddManagedRunnerCommand for: {} {}", arrayLength, addManagedRunnerCommand);
        }
    }

    @Override
    public void run() {
        boolean cachesAreLive = false, startingTheCachesTakesTooLong = false;
        final long startTime = System.currentTimeMillis();
        int whileCounter = 0;
        while (!Statics.mustStop.get() && !cachesAreLive) {
            try {
                Generic.threadSleepSegmented(1_000L, 100L, Statics.mustStop);
                cachesAreLive = ClientHandler.threadsWithOcmCommandReceived.get() >= 1 && ClientHandler.threadsWithMcmCommandReceived.get() >= ClientHandler.nMcmCommandsNeeded.get();

                if (startingTheCachesTakesTooLong) {
                    if (whileCounter % 30 == 0) {
                        logger.error("startingTheCachesTakesTooLong {}: {} {} {}", whileCounter, ClientHandler.threadsWithOcmCommandReceived.get(), ClientHandler.threadsWithMcmCommandReceived.get(), ClientHandler.nMcmCommandsNeeded.get());
                    } else { // I won't print error message too often, nothing to be done on this branch
                    }
                } else {
                    final long currentTime = System.currentTimeMillis();
                    final long timeSinceStart = currentTime - startTime;
                    if (!cachesAreLive && timeSinceStart > 3L * Generic.MINUTE_LENGTH_MILLISECONDS) {
                        startingTheCachesTakesTooLong = true;
                    }
                }

                whileCounter++;
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside rulesManager initial loop", throwable);
            }
        }

        if (Statics.mustStop.get()) { // program is stopping, nothing to be done
        } else {
            if (cachesAreLive) {
                Generic.threadSleepSegmented(2_000L, 10L, Statics.mustStop); // a small sleep to allow for cache update
            } else {
                logger.error("rulesManager continues without live caches: {} {} {} {}", whileCounter, ClientHandler.threadsWithOcmCommandReceived.get(), ClientHandler.threadsWithMcmCommandReceived.get(), ClientHandler.nMcmCommandsNeeded.get());
//            Generic.threadSleepSegmented(180_000L, 10L, Statics.mustStop); // a long sleep, after which the program will continue
            }
        }

        boolean hasReachedEndOfSleep = true; // full run first time
        while (!Statics.mustStop.get()) {
            try {
                if (this.orderCacheHasReset.get()) {
                    resetOrderCacheObjects(); // the method resets the AtomicBoolean
                }
                if (this.rulesHaveChanged.get()) {
                    Generic.threadSleepSegmented(500L, 50L, Statics.mustStop); // small sleep against multiple quick modifications that lead to writes
                    VarsIO.writeSettings(); // this method sets the AtomicBoolean to false
                }
                if (this.newOrderMarketCreated.get()) {
                    addManagedMarketsForExistingOrders(); // this method resets the AtomicBoolean
                }
                if (this.marketsMapModified.get()) {
                    calculateMarketLimits(); // this method resets the AtomicBoolean
                }
                if (this.newAddManagedRunnerCommand.get()) {
                    addManagedRunnerCommands(); // this method resets the AtomicBoolean
                }
                if (this.marketsToCheckExist.get()) { // run just on the marketsToCheck
                    final HashSet<RulesManagerStringObject> objectsSet = this.marketsToCheck.copy();
                    this.marketsToCheck.removeAll(objectsSet); // the atomicBoolean marker will get reset if marketsToCheck becomes empty

                    final Collection<String> marketIds = new HashSet<>(2);
                    for (final RulesManagerStringObject stringObject : objectsSet) {
                        final String marketId = stringObject.getMarketId();
                        marketIds.add(marketId);
                    }

                    checkMarketsAreAssociatedWithEvents();
                    addManagedMarketsForExistingOrders(); // this can modify the marketsMapModified AtomicBoolean marker
                    //calculateMarketLimits(marketIds);
                    if (this.marketsMapModified.get()) {
                        calculateMarketLimits(); // this method resets the AtomicBoolean
                    } else { // nothing to be done, will only calculated limits if marketsMapModified
                    }
                    for (final String marketId : marketIds) {
                        final ManagedMarket managedMarket = this.markets.get(marketId);
                        manageMarket(managedMarket);
                    }
                }
                if (hasReachedEndOfSleep) { // full run
                    this.timeLastFullCheck = System.currentTimeMillis();

                    checkMarketsAreAssociatedWithEvents();
                    addManagedMarketsForExistingOrders();
                    calculateMarketLimits(); // this method resets the marketsMapModified AtomicBoolean
                    for (final ManagedMarket managedMarket : this.markets.valuesCopy()) {
                        manageMarket(managedMarket);
                    }
                }

                hasReachedEndOfSleep = Generic.threadSleepSegmented(timeTillNextFullRun(), 10L, this.marketsToCheckExist, this.marketsMapModified, this.rulesHaveChanged, this.orderCacheHasReset, this.newOrderMarketCreated, this.newAddManagedRunnerCommand,
                                                                    Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside rulesManager loop", throwable);
            }
        } // end while

        logger.info("ruleManager thread ends");
    }
}
