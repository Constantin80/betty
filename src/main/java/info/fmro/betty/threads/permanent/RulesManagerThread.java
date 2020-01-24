package info.fmro.betty.threads.permanent;

import info.fmro.betty.main.VarsIO;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.client.ClientHandler;
import info.fmro.shared.logic.ManagedMarket;
import info.fmro.shared.logic.RulesManager;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class RulesManagerThread
        extends RulesManager
        implements Runnable, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(RulesManagerThread.class);
    private static final long serialVersionUID = 7059620547591940095L;
    // shouldn't have serializable fields, as those could conflict with the testMarker from RulesManager class

    public synchronized boolean copyFrom(final RulesManagerThread other) {
        final boolean readSuccessful;
        if (!this.events.isEmpty() || !this.markets.isEmpty()) {
            logger.error("not empty map in RulesManager copyFrom: {}", Generic.objectToString(this));
            readSuccessful = false;
        } else {
            if (other == null) {
                logger.error("null other in copyFrom for: {}", Generic.objectToString(this));
                readSuccessful = false;
            } else {
                Generic.updateObject(this, other);

//                this.events.copyFrom(other.events);
//                this.markets.clear();
//                this.markets.putAll(other.markets.copy());
//                // likely forgot addManagedRunnerCommands

                this.setTestMarker(other.getTestMarker());

                if (Statics.resetTestMarker) {
                    logger.error("resetTestMarker {} , will still exit the program after reset", getTestMarker());
                    final boolean objectModified = this.setTestMarker(Statics.TEST_MARKER);
                    if (objectModified) {
                        VarsIO.writeSettings();
                    }
                    readSuccessful = false; // in order to exit the program after reset
                } else {
                    readSuccessful = getTestMarker() != null && getTestMarker() == Statics.TEST_MARKER; // testMarker needs to have a certain value
                }

                if (this.markets.isEmpty()) { // map still empty, no modification was made
                } else {
                    this.rulesHaveChanged.set(true);
                    this.marketsMapModified.set(true);
                }
            }
        }
//        associateMarketsWithEvents( marketCataloguesMap);

        final int nQueues = this.listOfQueues.size();
        if (nQueues == 0) { // normal case, nothing to be done
        } else {
            logger.error("existing queues during RulesManager.copyFrom: {} {}", nQueues, Generic.objectToString(this));
            this.listOfQueues.send(this.getCopy());
        }

        return readSuccessful;
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
                if (this.newOrderMarketCreated.get()) {
                    addManagedMarketsForExistingOrders(Statics.orderCache); // this method resets the AtomicBoolean
                }
                if (this.marketsMapModified.get()) {
                    calculateMarketLimits(Statics.pendingOrdersThread, Statics.orderCache, Statics.safetyLimits, Statics.marketCataloguesMap); // this method resets the AtomicBoolean
                }
                if (this.newAddManagedRunnerCommand.get()) {
                    addManagedRunnerCommands(Statics.marketCataloguesMap, Statics.pendingOrdersThread, Statics.orderCache, Statics.safetyLimits); // this method resets the AtomicBoolean
                }
                if (!this.marketsToCheck.isEmpty()) { // run just on the marketsToCheck
//                    final HashSet<RulesManagerStringObject> objectsSet = this.marketsToCheck.copy();
//                    this.marketsToCheck.removeAll(objectsSet); // the atomicBoolean marker will get reset if marketsToCheck becomes empty
//
//                    final Collection<String> marketIds = new HashSet<>(2);
//                    for (final RulesManagerStringObject stringObject : objectsSet) {
//                        final String marketId = stringObject.getMarketId();
//                        marketIds.add(marketId);
//                    }

//                    checkMarketsAreAssociatedWithEvents();
                    addManagedMarketsForExistingOrders(Statics.orderCache); // this can modify the marketsMapModified AtomicBoolean marker
                    //calculateMarketLimits(marketIds);
                    if (this.marketsMapModified.get()) {
                        calculateMarketLimits(Statics.pendingOrdersThread, Statics.orderCache, Statics.safetyLimits, Statics.marketCataloguesMap); // this method resets the AtomicBoolean
                    } else { // nothing to be done, will only calculated limits if marketsMapModified
                    }

                    do {
                        final String marketId = this.marketsToCheck.poll();
                        final ManagedMarket managedMarket = this.markets.get(marketId);
                        manageMarket(managedMarket, Statics.marketCache, Statics.orderCache, Statics.pendingOrdersThread, Statics.safetyLimits.currencyRate, Statics.safetyLimits.speedLimit, Statics.safetyLimits);
                    } while (!this.marketsToCheck.isEmpty());
                }
                if (hasReachedEndOfSleep) { // full run
                    stampTimeLastFullCheck();

//                    checkMarketsAreAssociatedWithEvents();
                    addManagedMarketsForExistingOrders(Statics.orderCache);
                    calculateMarketLimits(Statics.pendingOrdersThread, Statics.orderCache, Statics.safetyLimits, Statics.marketCataloguesMap); // this method resets the marketsMapModified AtomicBoolean
                    for (final ManagedMarket managedMarket : this.markets.valuesCopy()) {
                        manageMarket(managedMarket, Statics.marketCache, Statics.orderCache, Statics.pendingOrdersThread, Statics.safetyLimits.currencyRate, Statics.safetyLimits.speedLimit, Statics.safetyLimits);
                    }
                }

                hasReachedEndOfSleep = Generic.threadSleepSegmented(timeTillNextFullRun(), 10L, this.marketsToCheckExist, this.marketsMapModified, this.rulesHaveChanged, this.orderCacheHasReset, this.newOrderMarketCreated,
                                                                    this.newAddManagedRunnerCommand, Statics.mustStop);
                this.marketsToCheckExist.set(false); // reset, should become true when another command is added
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside rulesManager loop", throwable);
            }
        } // end while

        logger.info("ruleManager thread ends");
    }
}
