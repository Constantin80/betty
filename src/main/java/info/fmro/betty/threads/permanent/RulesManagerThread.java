package info.fmro.betty.threads.permanent;

import info.fmro.betty.betapi.HttpUtil;
import info.fmro.betty.main.VarsIO;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.ClientHandler;
import info.fmro.betty.threads.LaunchCommandThread;
import info.fmro.shared.enums.CommandType;
import info.fmro.shared.logic.ManagedMarket;
import info.fmro.shared.logic.RulesManager;
import info.fmro.shared.objects.SharedStatics;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

public class RulesManagerThread
//        extends RulesManager
        implements Runnable, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(RulesManagerThread.class);
    @Serial
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

            if (Statics.resetTestMarker || SharedStatics.resetTestMarker.get()) {
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
        while (!SharedStatics.mustStop.get() && !cachesAreLive) {
            try {
                Generic.threadSleepSegmented(1_000L, 100L, SharedStatics.mustStop);
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

        if (SharedStatics.mustStop.get()) { // program is stopping, nothing to be done
        } else {
            if (cachesAreLive) {
                Generic.threadSleepSegmented(2_000L, 10L, SharedStatics.mustStop); // a small sleep to allow for cache update
            } else {
                logger.error("rulesManager continues without live caches: {} {} {} {}", whileCounter, ClientHandler.threadsWithOcmCommandReceived.get(), ClientHandler.threadsWithMcmCommandReceived.get(), ClientHandler.nMcmCommandsNeeded.get());
//            Generic.threadSleepSegmented(180_000L, 10L, Statics.mustStop); // a long sleep, after which the program will continue
            }
        }

        boolean hasReachedEndOfSleep = true; // full run first time
        while (!SharedStatics.mustStop.get()) {
            try {
//                if (this.rulesManager.orderCacheHasReset.get()) {
//                    this.rulesManager.resetOrderCacheObjects(); // the method resets the AtomicBoolean
//                }
                if (this.rulesManager.newOrderMarketCreated.get()) {
                    logger.info("checking newOrderMarketCreated");
                    this.rulesManager.addManagedMarketsForExistingOrders(Statics.marketCataloguesMap, Statics.eventsMap); // this method resets the AtomicBoolean
                }
                if (this.rulesManager.marketsMapModified.getAndSet(false)) {
                    SharedStatics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.streamMarkets));
                    this.rulesManager.calculateMarketLimits(Statics.safetyLimits.existingFunds, Statics.marketCataloguesMap);
                }
                if (this.rulesManager.newAddManagedRunnerCommand.get()) { // this method resets the AtomicBoolean
                    this.rulesManager.addManagedRunnerCommands(Statics.marketCataloguesMap, Statics.safetyLimits.existingFunds, Statics.eventsMap);
                }
                if (!this.rulesManager.marketsToCheck.isEmpty()) { // run just on the marketsToCheck
//                    logger.info("manageMarket marketsToCheck: {}", this.rulesManager.marketsToCheck);

                    //                    final HashSet<RulesManagerStringObject> objectsSet = this.marketsToCheck.copy();
//                    this.marketsToCheck.removeAll(objectsSet); // the atomicBoolean marker will get reset if marketsToCheck becomes empty
//
//                    final Collection<String> marketIds = new HashSet<>(2);
//                    for (final RulesManagerStringObject stringObject : objectsSet) {
//                        final String marketId = stringObject.getMarketId();
//                        marketIds.add(marketId);
//                    }

//                    checkMarketsAreAssociatedWithEvents();
                    this.rulesManager.addManagedMarketsForExistingOrders(Statics.marketCataloguesMap, Statics.eventsMap); // this can modify the marketsMapModified marker
                    //calculateMarketLimits(marketIds);
//                    final boolean iHaveCalculatedTheLimits;
                    if (this.rulesManager.marketsMapModified.getAndSet(false)) {
                        SharedStatics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.streamMarkets));
                        this.rulesManager.calculateMarketLimits(Statics.safetyLimits.existingFunds, Statics.marketCataloguesMap);
//                        iHaveCalculatedTheLimits = true;
                    } else { // nothing to be done, will only calculate limits if marketsMapModified
//                        iHaveCalculatedTheLimits = false;
                    }

                    do {
                        @NotNull final Map.Entry<String, Long> entry = this.rulesManager.marketsToCheck.poll();
                        final String marketId = entry.getKey();
                        final Long checkMarketRequestStamp = entry.getValue();
//                        final long checkMarketRequestStamp = value == null ? 0L : value;
                        final ManagedMarket managedMarket = this.rulesManager.markets.get(marketId);
                        if (managedMarket == null) {
                            logger.error("null managedMarket while checking markets for marketId: {}", marketId);
                        } else {
                            managedMarket.lastCheckMarketRequestStamp.add(checkMarketRequestStamp);
                            this.rulesManager.manageMarket(managedMarket, Statics.safetyLimits.speedLimit, Statics.safetyLimits.existingFunds, HttpUtil.sendPostRequestRescriptMethod, Statics.marketCataloguesMap, false);
                        }
                    } while (!this.rulesManager.marketsToCheck.isEmpty());
                }
                if (hasReachedEndOfSleep) { // full run
                    this.rulesManager.stampTimeLastFullCheck();

//                    checkMarketsAreAssociatedWithEvents();
                    this.rulesManager.addManagedMarketsForExistingOrders(Statics.marketCataloguesMap, Statics.eventsMap);
                    this.rulesManager.calculateMarketLimits(Statics.safetyLimits.existingFunds, Statics.marketCataloguesMap);
                    for (final ManagedMarket managedMarket : this.rulesManager.markets.valuesCopy()) {
                        this.rulesManager.manageMarket(managedMarket, Statics.safetyLimits.speedLimit, Statics.safetyLimits.existingFunds, HttpUtil.sendPostRequestRescriptMethod, Statics.marketCataloguesMap, false);
                    }
                }

                hasReachedEndOfSleep = Generic.threadSleepSegmented(this.rulesManager.timeTillNextFullRun(), 10L, this.rulesManager.marketsToCheckExist, this.rulesManager.marketsMapModified, this.rulesManager.newOrderMarketCreated,
                                                                    this.rulesManager.newAddManagedRunnerCommand, SharedStatics.mustStop); // , this.rulesManager.orderCacheHasReset
                this.rulesManager.marketsToCheckExist.set(false); // reset, should become true when another command is added
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside rulesManager loop", throwable);
            }
        } // end while

        logger.debug("ruleManager thread ends");
    }
}
