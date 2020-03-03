package info.fmro.betty.threads.permanent;

import info.fmro.betty.main.VarsIO;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.ClientHandler;
import info.fmro.betty.threads.LaunchCommandThread;
import info.fmro.shared.enums.CommandType;
import info.fmro.shared.logic.ManagedMarket;
import info.fmro.shared.logic.RulesManager;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class RulesManagerThread
//        extends RulesManager
        implements Runnable, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(RulesManagerThread.class);
    private static final long serialVersionUID = 7059620547591940095L;
    public final RulesManager rulesManager = new RulesManager();
    // shouldn't have serializable fields, as those could conflict with the testMarker from RulesManager class

    public synchronized boolean copyFrom(final RulesManagerThread other) {
        final boolean readSuccessful;
        if (other == null) {
            logger.error("null other in copyFrom for: {}", Generic.objectToString(this));
            readSuccessful = false;
        } else {
            final boolean managerReadSuccessful = this.rulesManager.copyFrom(other.rulesManager);

            if (Statics.resetTestMarker) {
                logger.error("resetTestMarker {} to {} , will still exit the program after reset", this.rulesManager.getTestMarker(), Statics.TEST_MARKER);
                final boolean objectModified = this.rulesManager.setTestMarker(Statics.TEST_MARKER);
                if (objectModified) {
                    VarsIO.writeSettings();
                }
                readSuccessful = false; // in order to exit the program after reset
            } else if (managerReadSuccessful) {
                final Integer marker = this.rulesManager.getTestMarker();

                if (marker != null && marker == Statics.TEST_MARKER) {
                    readSuccessful = true;
                } else {
                    logger.error("wrong testMarker {} instead of {}", marker, Statics.TEST_MARKER);
                    readSuccessful = false;
                }
            } else { // read was not successful, no need to check if testMarker is correct
                readSuccessful = false;
            }
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
                        if (Statics.connectingToBetfairServersDisabled) { // no need to print any error message, it's normal
                        } else {
                            logger.error("startingTheCachesTakesTooLong {}: {} {} {}", whileCounter, ClientHandler.threadsWithOcmCommandReceived.get(), ClientHandler.threadsWithMcmCommandReceived.get(), ClientHandler.nMcmCommandsNeeded.get());
                        }
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
                if (this.rulesManager.orderCacheHasReset.get()) {
                    this.rulesManager.resetOrderCacheObjects(); // the method resets the AtomicBoolean
                }
                if (this.rulesManager.newOrderMarketCreated.get()) {
                    this.rulesManager.addManagedMarketsForExistingOrders(Statics.orderCache, Statics.marketCataloguesMap, Statics.marketCache, Statics.eventsMap); // this method resets the AtomicBoolean
                }
                if (this.rulesManager.marketsMapModified.getAndSet(false)) {
                    Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.streamMarkets));
                    this.rulesManager.calculateMarketLimits(Statics.pendingOrdersThread, Statics.orderCache, Statics.safetyLimits.existingFunds, Statics.marketCataloguesMap, Statics.marketCache);
                }
                if (this.rulesManager.newAddManagedRunnerCommand.get()) { // this method resets the AtomicBoolean
                    this.rulesManager.addManagedRunnerCommands(Statics.marketCataloguesMap, Statics.pendingOrdersThread, Statics.orderCache, Statics.safetyLimits.existingFunds, Statics.marketCache, Statics.eventsMap);
                }
                if (!this.rulesManager.marketsToCheck.isEmpty()) { // run just on the marketsToCheck
//                    final HashSet<RulesManagerStringObject> objectsSet = this.marketsToCheck.copy();
//                    this.marketsToCheck.removeAll(objectsSet); // the atomicBoolean marker will get reset if marketsToCheck becomes empty
//
//                    final Collection<String> marketIds = new HashSet<>(2);
//                    for (final RulesManagerStringObject stringObject : objectsSet) {
//                        final String marketId = stringObject.getMarketId();
//                        marketIds.add(marketId);
//                    }

//                    checkMarketsAreAssociatedWithEvents();
                    this.rulesManager.addManagedMarketsForExistingOrders(Statics.orderCache, Statics.marketCataloguesMap, Statics.marketCache, Statics.eventsMap); // this can modify the marketsMapModified AtomicBoolean marker
                    //calculateMarketLimits(marketIds);
                    if (this.rulesManager.marketsMapModified.getAndSet(false)) {
                        Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.streamMarkets));
                        this.rulesManager.calculateMarketLimits(Statics.pendingOrdersThread, Statics.orderCache, Statics.safetyLimits.existingFunds, Statics.marketCataloguesMap, Statics.marketCache);
                    } else { // nothing to be done, will only calculated limits if marketsMapModified
                    }

                    do {
                        final String marketId = this.rulesManager.marketsToCheck.poll();
                        final ManagedMarket managedMarket = this.rulesManager.markets.get(marketId);
                        this.rulesManager.manageMarket(managedMarket, Statics.marketCache, Statics.orderCache, Statics.pendingOrdersThread, Statics.safetyLimits.existingFunds.currencyRate, Statics.safetyLimits.speedLimit,
                                                       Statics.safetyLimits.existingFunds, Statics.marketCataloguesMap);
                    } while (!this.rulesManager.marketsToCheck.isEmpty());
                }
                if (hasReachedEndOfSleep) { // full run
                    this.rulesManager.stampTimeLastFullCheck();

//                    checkMarketsAreAssociatedWithEvents();
                    this.rulesManager.addManagedMarketsForExistingOrders(Statics.orderCache, Statics.marketCataloguesMap, Statics.marketCache, Statics.eventsMap);
                    this.rulesManager.calculateMarketLimits(Statics.pendingOrdersThread, Statics.orderCache, Statics.safetyLimits.existingFunds, Statics.marketCataloguesMap, Statics.marketCache);
                    for (final ManagedMarket managedMarket : this.rulesManager.markets.valuesCopy()) {
                        this.rulesManager.manageMarket(managedMarket, Statics.marketCache, Statics.orderCache, Statics.pendingOrdersThread, Statics.safetyLimits.existingFunds.currencyRate, Statics.safetyLimits.speedLimit,
                                                       Statics.safetyLimits.existingFunds, Statics.marketCataloguesMap);
                    }
                }

                hasReachedEndOfSleep = Generic.threadSleepSegmented(this.rulesManager.timeTillNextFullRun(), 10L, this.rulesManager.marketsToCheckExist, this.rulesManager.marketsMapModified, this.rulesManager.orderCacheHasReset,
                                                                    this.rulesManager.newOrderMarketCreated, this.rulesManager.newAddManagedRunnerCommand, Statics.mustStop);
                this.rulesManager.marketsToCheckExist.set(false); // reset, should become true when another command is added
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside rulesManager loop", throwable);
            }
        } // end while

        logger.info("ruleManager thread ends");
    }
}
