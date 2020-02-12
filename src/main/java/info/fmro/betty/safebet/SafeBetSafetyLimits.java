package info.fmro.betty.safebet;

import com.google.common.util.concurrent.AtomicDouble;
import info.fmro.betty.betapi.RescriptOpThread;
import info.fmro.shared.entities.CurrentOrderSummary;
import info.fmro.shared.entities.MarketBook;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.LaunchCommandThread;
import info.fmro.betty.threads.permanent.MaintenanceThread;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.entities.ClearedOrderSummary;
import info.fmro.shared.entities.ExchangePrices;
import info.fmro.shared.entities.LimitOrder;
import info.fmro.shared.entities.PlaceInstruction;
import info.fmro.shared.entities.PriceSize;
import info.fmro.shared.entities.Runner;
import info.fmro.shared.enums.CommandType;
import info.fmro.shared.enums.MarketStatus;
import info.fmro.shared.enums.OrderType;
import info.fmro.shared.enums.PersistenceType;
import info.fmro.shared.enums.RunnerStatus;
import info.fmro.shared.enums.Side;
import info.fmro.shared.objects.OrderPrice;
import info.fmro.shared.utility.BlackList;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import info.fmro.shared.utility.SynchronizedSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings({"OverlyComplexClass", "OverlyCoupledClass"})
public class SafeBetSafetyLimits
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(SafeBetSafetyLimits.class);
    private static final long serialVersionUID = -4727281604162673835L;
    private static final long specialLimitInitialPeriod = 120_000L;
    private static final PersistenceType persistenceType = PersistenceType.LAPSE; // sometimes PersistenceType.PERSIST causes an order placing error
    private static final OrderType orderType = OrderType.LIMIT;
    private static final double initialSafeRunnerLimitFraction = .1d; // max bet during the initial period, as fraction of total; applied for safeRunners
    private static final double safeEventLimitFraction = .2d; // max bet per safe event
    private final HashMap<String, Double> eventAmounts = new HashMap<>(16, 0.75F); // eventId, totalAmount
    private final HashMap<String, Double> marketAmounts = new HashMap<>(16, 0.75F); // marketId, totalAmount
    private final HashMap<SafeRunner, Double> runnerAmounts = new HashMap<>(16, 0.75F); // safeRunner, totalAmount
    private final HashMap<String, HashMap<String, List<PlaceInstruction>>> tempInstructionsListMap = new HashMap<>(2, 0.75F);
    private boolean startedGettingOrders;

    @SuppressWarnings({"OverlyLongMethod", "OverlyNestedMethod"})
    synchronized void createPlaceInstructionList(@NotNull final Iterable<? extends Runner> runnersList, final SynchronizedSet<SafeRunner> safeRunnersSet, final MarketBook marketBook, final String marketId, final long startTime, final long endTime,
                                                 final MarketStatus marketStatus, final boolean inPlay, final int betDelay, final long timePreviousMarketBookCheck) {
        logger.error("createPlaceInstructionList is an old method from the old disabled safeBets support; it needs heavy modifications and it should not be used");

        final List<PlaceInstruction> placeInstructionsList = new ArrayList<>(0);
        for (final Runner runner : runnersList) {
            if (runner != null) {
                final Long runnerIdObject = runner.getSelectionId();
                if (runnerIdObject != null) {
                    final long runnerId = runnerIdObject;
                    SafeRunner safeRunner = null;
                    final HashSet<SafeRunner> setCopy = safeRunnersSet.copy();
                    for (final SafeRunner loopSafeRunner : setCopy) {
                        if (loopSafeRunner != null) {
                            final long safeRunnerId = loopSafeRunner.getSelectionId();
                            if (runnerId == safeRunnerId) {
                                safeRunner = loopSafeRunner;
                                break;
//                                                                side = loopSafeRunner.getSide();
//                                                                if (side != null) {
//                                                                    break; // found it
//                                                                } else {
//                                                                    logger.error("null side in getMarketBooks for: {} {}", Generic.objectToString(loopSafeRunner),
//                                                                            Generic.objectToString(marketBook));
//                                                                }
                            }
                        } else {
                            logger.error("null safeRunner in getMarketBooks for: {}", Generic.objectToString(marketBook));
                        }
                    } // end for

                    if (safeRunner != null && !safeRunner.hasBeenRemoved()) { // found safe runner
                        final Side side = safeRunner.getSide();
                        final ExchangePrices exchangePrices = runner.getEx();
                        if (exchangePrices != null) {
                            @Nullable final List<PriceSize> safePriceSizesList;
                            switch (side) {
                                case BACK:
                                    safePriceSizesList = exchangePrices.getAvailableToBack();
                                    break;
                                case LAY:
                                    safePriceSizesList = exchangePrices.getAvailableToLay();
                                    break;
                                default:
                                    safePriceSizesList = null;
                                    logger.error("STRANGE unknown side {} in getMarketBooks for: {} {}", side, Generic.objectToString(runner), Generic.objectToString(marketBook));
                                    break;
                            } // end switch
                            if (safePriceSizesList != null && !safePriceSizesList.isEmpty()) { // this is a list of safe price/size pairs
                                final RunnerStatus runnerStatus = runner.getStatus();
                                final AtomicDouble previousOversizedRealAmount = new AtomicDouble();
                                PlaceInstruction oversizedPlaceInstruction = null;
                                if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId, startTime)) {
                                    BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "market with bets, marketCatalogue map", Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD);
                                    MaintenanceThread.removeFromSecondaryMaps(marketId);
                                } else {
                                    Statics.safeMarketsImportantMap.put(marketId, endTime);
                                    if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId, startTime)) {
                                        BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "market with bets after importantMap add, marketCatalogue map", Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD);
                                        MaintenanceThread.removeFromSecondaryMaps(marketId);
//                                    } else if (safeRunner.isIgnored(startTime)) {
//                                        logger.error("ignored safeRunner with bets after importantMap add: {} {}", marketId,
//                                                     Generic.objectToString(safeRunner));
                                    } else {
                                        for (final PriceSize priceSize : safePriceSizesList) {
                                            final double price = priceSize.getPrice();
                                            final double size = priceSize.getSize();

                                            if (marketStatus == MarketStatus.OPEN) {
                                                Generic.alreadyPrintedMap.logOnce(Statics.debugLevel.check(2, 171), logger, LogLevel.INFO,
                                                                                  "safe bet in market: {} marketStatus: {} inPlay={} betDelay={} runner: {} runnerStatus: {} side: {} price: {} size: {}", marketId, marketStatus, inPlay, betDelay, runnerId,
                                                                                  runnerStatus, side, price, size);

                                                final SafeBet safeBet = new SafeBet(marketId, marketStatus, inPlay, betDelay, runnerId, runnerStatus, price, size, side);

                                                Statics.safeBetsMap.addAndGetSafeBetStats(marketId, safeBet, endTime, timePreviousMarketBookCheck);

                                                if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId, startTime)) {
                                                    BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "market with bets after safeBet add, marketCatalogue map", Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD);
                                                    MaintenanceThread.removeFromSecondaryMaps(marketId);
                                                    break; // break from for
//                                                } else if (safeRunner.isIgnored(startTime)) {
//                                                    logger.error("ignored safeRunner with bets after safeBet add: {} {}", marketId,
//                                                                 Generic.objectToString(safeRunner));
//                                                    break; // break from for
                                                } else if (!safeRunner.sufficientScrapers()) {
                                                    logger.error("insufficientScrapers after safeBet add: {} {}", marketId, Generic.objectToString(safeRunner));
                                                    MaintenanceThread.removeSafeRunner(marketId, safeRunner);
                                                    break; // break from for
                                                } else {
                                                    oversizedPlaceInstruction = processOpenMarket(safeRunner, runner, side, price, size, betDelay, marketId, runnerId, placeInstructionsList, previousOversizedRealAmount, oversizedPlaceInstruction);
                                                }
                                            } else { // market not open, can't place bets
                                                Generic.alreadyPrintedMap
                                                        .logOnce(logger, LogLevel.INFO, "safe bet in not active market: {} marketStatus: {} inPlay={} betDelay={} runner: {} runnerStatus: {} side: {} price: {} size: {}", marketId, marketStatus, inPlay,
                                                                 betDelay, runnerId, runnerStatus, side, price, size);
                                            }
                                        } // end for safePriceSizesList
                                    }
                                } // end else
                            } else {
                                if (safePriceSizesList == null) {
                                    logger.error("null safePriceSizesList in getMarketBooks for: {} {} {}", side, Generic.objectToString(runner), Generic.objectToString(marketBook));
                                }
                            }
                        } else {
                            logger.error("null exchangePrices in getMarketBooks for: {} {}", Generic.objectToString(runner), Generic.objectToString(marketBook));
                        }
                    } else { // runner not safe
                        if (safeRunner == null) { // safeRunner null, normal behavior
                        } else if (safeRunner.hasBeenRemoved()) {
                            logger.warn("removed safeRunner processed in SafetyLimits; it should be a rare event: {}", Generic.objectToString(safeRunner));
                        } else {
                            logger.error("unknown testcase in SafetyLimits: {}", Generic.objectToString(safeRunner));
                        }
                    }
                } else {
                    logger.error("null runnerIdLong in getMarketBooks for: {} {}", Generic.objectToString(runner), Generic.objectToString(marketBook));
                }
            } else {
                logger.error("null runner in getMarketBooks for: {}", Generic.objectToString(marketBook));
            }
        } // end for runnersList

        if (placeInstructionsList.isEmpty()) { // no safe bets found, nothing to be done
        } else {
            // check moved to SafetyLimits
//                                            for (PlaceInstruction placeInstruction : placeInstructionsList) {
//                                                final LimitOrder limitOrder = placeInstruction.getLimitOrder();
//                                                final OrderPrice orderPrice = new OrderPrice(marketId, placeInstruction.getSelectionId(), placeInstruction.getSide(),
//                                                        limitOrder.getPrice());
//                                                final double orderSize = limitOrder.getSize();
//                                                synchronized (Statics.executingOrdersMap) { // add order to map
//                                                    double existingSize;
//                                                    if (Statics.executingOrdersMap.containsKey(orderPrice)) {
//                                                        existingSize = Statics.executingOrdersMap.get(orderPrice);
//                                                    } else {
//                                                        existingSize = 0;
//                                                    }
//                                                    Statics.executingOrdersMap.put(orderPrice, existingSize + orderSize);
//                                                } // end synchronized block
//                                            } // end for

            if (!Statics.notPlacingOrders && !Statics.denyBetting.get()) {
                final String eventId = info.fmro.shared.utility.Formulas.getEventIdOfMarketId(marketId, Statics.marketCataloguesMap);
                final boolean isStartedGettingOrders = addInstructionsList(eventId, marketId, placeInstructionsList);

                Statics.threadPoolExecutorImportant.execute(new RescriptOpThread<>(marketId, placeInstructionsList, isStartedGettingOrders)); // place order thread
            }
        }
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod"})
    private synchronized PlaceInstruction processOpenMarket(final SafeRunner safeRunner, @NotNull final Runner runner, final Side side, final double price, final double size, final int betDelay, final String marketId, final long runnerId,
                                                            final Collection<? super PlaceInstruction> placeInstructionsList, final AtomicDouble previousOversizedRealAmount, final PlaceInstruction oversizedPlaceInstruction) {
        logger.error("processOpenMarket is an old method from the old disabled safeBets support; it needs heavy modifications and it should not be used");

        final Collection<Long> orderDelaysSet = new HashSet<>(0);
        final Double handicap = runner.getHandicap();
        final double localAvailableFunds = getAvailableFundsForRunner(safeRunner);
        @Nullable PlaceInstruction returnOversizedPlaceInstruction = oversizedPlaceInstruction;
        boolean currentOrderIsOversized = false;
        //noinspection unused
        double localUsedBalance = 0d;

        if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId)) {
            BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, "processOpenMarket, marketCatalogue map", Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD);
        } else if (localAvailableFunds < 2d || (side == Side.LAY && localAvailableFunds < 2d * (price - 1d))) {
            logger.warn("not enough funds to place bets: {} {} {}", side, price, localAvailableFunds);
            Statics.pendingOrdersThread.addCancelAllOrder(0L);
        } else {
            double orderSize;
            final long orderDelay;
            if (side == Side.BACK) {
                if (size <= localAvailableFunds) {
                    orderSize = size;
                    if (orderSize + 0.0101d <= localAvailableFunds) {
                        // extra 1 cent, and a bit for double imprecision
                        orderSize += 0.0101d;
                    }
                    if (orderSize < 2d) {
                        orderSize = 2d;
                        orderDelay = betDelay * 1_000L + 2_001L;
                        logger.info("will place oversized order {} {}", orderSize, size);
                        currentOrderIsOversized = true;

                        // RescriptOpThread<?> rescriptOpCancelThread = new RescriptOpThread<>(overSizedOrderDelay, null);
                        // rescriptOpCancelThread.start();
                        // Statics.rescriptOpThreadsSet.add(rescriptOpCancelThread);
                    } else {
                        orderDelay = betDelay * 1_000L + 10_001L; // for orders placed too late to match
                    }
                } else {
                    // orderSize = Generic.truncateDouble(localAvailableToBetBalance, 2); // truncate done in LimitOrder class
                    orderSize = localAvailableFunds;
                    orderDelay = betDelay * 1_000L + 10_001L;
                }
                // localAvailableToBetBalance -= orderSize;
            } else if (side == Side.LAY) {
                final double maximumBet = localAvailableFunds / (price - 1d);
                if (size <= maximumBet) {
                    orderSize = size;
                    if (orderSize + 0.0101d <= maximumBet) { // extra 1 cent, and a bit for double imprecision
                        orderSize += 0.0101d;
                    }
                    if (orderSize < 2d) {
                        orderSize = 2d;
                        orderDelay = betDelay * 1_000L + 2_001L;
                        logger.info("will place oversized order {} {}", orderSize, size);
                        currentOrderIsOversized = true;

                        // RescriptOpThread<?> rescriptOpCancelThread = new RescriptOpThread<>(overSizedOrderDelay, null);
                        // rescriptOpCancelThread.start();
                        // Statics.rescriptOpThreadsSet.add(rescriptOpCancelThread);
                    } else {
                        orderDelay = betDelay * 1_000L + 10_001L; // for orders placed too late to match
                    }
                } else {
                    // orderSize = Generic.truncateDouble(maximumBet, 2); // truncate is done in LimitOrder class
                    orderSize = maximumBet;
                    orderDelay = betDelay * 1_000L + 10_001L;
                }
                // localAvailableToBetBalance -= orderSize * (price - 1d);
            } else {
                logger.error("STRANGE unknown side while building LimitOrder: {} {}", side, Generic.objectToString(runner));
                orderSize = -1d;
                orderDelay = 0L;
            }

            if (orderSize >= 2d) {
                final LimitOrder limitOrder = new LimitOrder();
                limitOrder.setPersistenceType(persistenceType);
                limitOrder.setPrice(price);
                limitOrder.setSize(orderSize);
                // final double limitOrderSize = limitOrder.getSize();
                switch (side) {
                    case BACK:
                        // if (localUsedBalance == 0d) {
                        //     Statics.safetyLimits.addLocalUsedBalance(localUsedBalance);
                        // }
                        localUsedBalance += orderSize;
                        break;
                    case LAY:
                        // if (localUsedBalance == 0d) {
                        //     Statics.safetyLimits.addLocalUsedBalance(localUsedBalance);
                        // }
                        localUsedBalance += info.fmro.shared.utility.Formulas.layExposure(price, orderSize);
                        break;
                    default:
                        logger.error("STRANGE unknown side in switch: {}", side);
                        break;
                }

                final PlaceInstruction placeInstruction = new PlaceInstruction();
                placeInstruction.setHandicap(handicap);
                placeInstruction.setOrderType(orderType);
                placeInstruction.setSelectionId(runnerId);
                placeInstruction.setSide(side);
                placeInstruction.setLimitOrder(limitOrder);

                final OrderPrice orderPrice = new OrderPrice(marketId, runnerId, side, price);

                final boolean shouldAddInstruction = Formulas.shouldAddInstruction(orderPrice, orderSize);
//                synchronized (Statics.executingOrdersMap) { // shouldAddInstruction check
//                    if (Statics.executingOrdersMap.containsKey(orderPrice)) {
//                        logger.warn("order already exists in executingOrdersMap: {}", Generic.objectToString(orderPrice));
//                        final double existingSize = Statics.executingOrdersMap.get(orderPrice);
//
//                        if (size - existingSize < .01) { // size - existingSize is the right condition
//                            logger.warn("order won't be executed again: {} {} {}", size, existingSize, limitOrderSize);
//                            shouldAddInstruction = false;
//                        } else {
//                            logger.warn("order will be executed with additional amount: {} {} {}", size, existingSize, limitOrderSize);
//                            Statics.executingOrdersMap.put(orderPrice, existingSize + limitOrderSize);
//                            shouldAddInstruction = true;
//                        }
//                    } else {
//                        logger.info("will add placeInstruction: {} {}", marketId, Generic.objectToString(placeInstruction));
//                        Statics.executingOrdersMap.put(orderPrice, limitOrderSize);
//                        shouldAddInstruction = true;
//                    }
//                } // end synchronized block

                if (shouldAddInstruction) {
                    if (orderDelay > 0) { // delay exists
                        if (orderDelaysSet.add(orderDelay)) { // delay was not already used
                            logger.info("adding order to cancel with delay {} ms", orderDelay);

                            Statics.pendingOrdersThread.addCancelAllOrder(orderDelay);
                        }
                    } else { // in current setup, delayed cancel order is always placed
                        logger.error("overSizedOrderDelay {} ms for: {}", orderDelay, Generic.objectToString(placeInstruction));
                    }

                    // no orderDelay change needed; this support is no longer very important anyway
                    double newSize = 0d;
                    final double previousRealAmount = previousOversizedRealAmount.get();
                    if (previousRealAmount > 0d && returnOversizedPlaceInstruction != null) {
                        final LimitOrder previousLimitOrder = returnOversizedPlaceInstruction.getLimitOrder();
                        final double previousPrice = previousLimitOrder.getPrice();

                        if (//(side.equals(Side.BACK) && previousPrice > limitOrder.getPrice()) ||
                            //(
                                side == Side.BACK || (side == Side.LAY && previousPrice < limitOrder.getPrice())) //)
                        { // in case of Side.BACK, limitOrder.getPrice() gets modified before and after placing the order, so it can't be used as if condition
                            final double previousOrderSize = previousLimitOrder.getSize();
                            //noinspection SwitchStatementDensity
                            switch (side) {
                                case BACK:
                                    if (currentOrderIsOversized) {
                                        newSize = previousRealAmount + size;
                                        if (newSize < 2d) {
                                            newSize = 2d;
                                        } else {
                                            currentOrderIsOversized = false;
                                        }
                                    } else {
                                        newSize = previousRealAmount + orderSize;
                                    }

                                    if (newSize > localAvailableFunds + previousOrderSize) {
                                        newSize = localAvailableFunds + previousOrderSize;
                                    }
                                    // if (localUsedBalance == 0d) {
                                    //     Statics.safetyLimits.addLocalUsedBalance(localUsedBalance);
                                    // }
                                    //noinspection UnusedAssignment
                                    localUsedBalance += newSize - limitOrder.getSize() - previousOrderSize;
                                    logger.info("{} newSize: {} fromSize: {} size: {} price: {} previousOrderSize: {} previousRealAmount: {} previousPrice: {}", side.name(), newSize, limitOrder.getSize(), size, price, previousOrderSize, previousRealAmount,
                                                previousPrice);
                                    limitOrder.setSize(newSize);
                                    @SuppressWarnings({"LocalCanBeFinal", "LocalVariableUsedAndDeclaredInDifferentSwitchBranches"}) OrderPrice previousOrderPrice = new OrderPrice(marketId, runnerId, side, previousPrice);
                                    Formulas.removeInstruction(previousOrderPrice, previousOrderSize);
                                    placeInstructionsList.remove(returnOversizedPlaceInstruction);
                                    break;
                                case LAY:
                                    final boolean betterToModify;
                                    if (currentOrderIsOversized) {
                                        newSize = previousRealAmount + size;
                                        if (newSize < 2d) {
                                            betterToModify = true;
                                        } else {
                                            final double sizeDifference = newSize - limitOrder.getSize();
                                            betterToModify = sizeDifference * (price - 1d) <= previousOrderSize * (previousPrice - 1d);
                                        }
                                    } else {
                                        betterToModify = previousRealAmount * (price - 1d) <= previousOrderSize * (previousPrice - 1d);
                                    }

                                    if (betterToModify) {
                                        if (currentOrderIsOversized) {
                                            newSize = previousRealAmount + size;
                                            if (newSize < 2d) {
                                                newSize = 2d;
                                            } else {
                                                currentOrderIsOversized = false;
                                            }
                                        } else {
                                            newSize = previousRealAmount + orderSize;
                                        }

                                        final double maximumBet = localAvailableFunds / (price - 1d) + previousOrderSize / (previousPrice - 1d);
                                        if (newSize > maximumBet) {
                                            newSize = maximumBet;
                                        }
                                        // if (localUsedBalance == 0d) {
                                        //     Statics.safetyLimits.addLocalUsedBalance(localUsedBalance);
                                        // }
                                        //noinspection OverlyComplexArithmeticExpression,UnusedAssignment
                                        localUsedBalance += (newSize - limitOrder.getSize()) * (price - 1d) - previousOrderSize * (previousPrice - 1d);
                                        logger.info("{} newSize: {} fromSize: {} size: {} price: {} previousOrderSize: {} previousRealAmount: {} previousPrice: {}", side.name(), newSize, limitOrder.getSize(), size, price, previousOrderSize,
                                                    previousRealAmount, previousPrice);
                                        limitOrder.setSize(newSize);
                                        //noinspection ReuseOfLocalVariable
                                        previousOrderPrice = new OrderPrice(marketId, runnerId, side, previousPrice);
                                        Formulas.removeInstruction(previousOrderPrice, previousOrderSize);
                                        placeInstructionsList.remove(returnOversizedPlaceInstruction);
                                    }
                                    break;
                                default:
                                    logger.error("STRANGE unknown side in switch2: {}", side);
                                    break;
                            } // end switch
                        } else {
                            logger.error("STRANGE bad price ordering: {} {}", Generic.objectToString(returnOversizedPlaceInstruction), Generic.objectToString(placeInstruction));
                        }
                    } else { // previous order was not oversized, oversized support won't be run
                    }
                    if (currentOrderIsOversized) {
                        if (newSize == 0d) {
                            previousOversizedRealAmount.set(size);
                        } else {
                            logger.info("still currentOrderIsOversized, replaced orderSize {} with newSize {} for: {} {}", orderSize, newSize, marketId, Generic.objectToString(placeInstruction));
                            previousOversizedRealAmount.addAndGet(size);
                        }
                        returnOversizedPlaceInstruction = placeInstruction;
                    } else {
                        returnOversizedPlaceInstruction = null;
                        previousOversizedRealAmount.set(0d);
                    }

//                    synchronized (Statics.executingOrdersMap) { // shouldAddInstruction check
//                        if (Statics.executingOrdersMap.containsKey(orderPrice)) {
//                            final double existingSize = Statics.executingOrdersMap.get(orderPrice);
//                            final double replacingSize = existingSize - orderSize + newSize;
//                            Statics.executingOrdersMap.put(orderPrice, replacingSize);
//                            logger.info("executingOrdersMap replacing existingSize {} with {} for: {} {}", existingSize, replacingSize, marketId,
//                                    Generic.objectToString(placeInstruction));
//                        } else {
//                            logger.error("executingOrdersMap doesn't contain {} for: {} {}", Generic.objectToString(orderPrice), marketId,
//                                    Generic.objectToString(placeInstruction));
//                        }
//                    } // end synchronized block
                    Formulas.addInstruction(orderPrice, limitOrder.getSize(), marketId, placeInstruction);
                    placeInstructionsList.add(placeInstruction);
                } else { // not added for some reason; error messages are in the previous block
                    //noinspection UnusedAssignment,ReuseOfLocalVariable
                    localUsedBalance = 0d; // else temp reserve is added for 100ms, which prevents placing of useful bets
                }
            } else {
                logger.error("STRANGE orderSize too small while building LimitOrder: {} {} {} {} {}", orderSize, localAvailableFunds, price, size, Generic.objectToString(runner));
            }
        }

        // algorithm has modified, localUsedBalance only contains one PlaceInstruction amount now
        // AtomicDouble totalListAmount = new AtomicDouble(QuickCheckThread.amountPlaceInstructionsList(placeInstructionsList));
        // Statics.safetyLimits.addTempReserve(totalListAmount, 100L, localUsedBalance); // temp reserve for 100ms, to avoid multiple order posting
        // if (Math.abs(totalListAmount.get() - localUsedBalance) > .01) {
        //     logger.error("different amounts between total and used: {} {}", totalListAmount.get(), localUsedBalance);
        // }
//        if (localUsedBalance > 0d) {
////            Statics.safetyLimits.addTempReserve(new AtomicDouble(localUsedBalance), 100L); // temp reserve for 100ms, to avoid multiple order posting
//            safeRunner.addPlacedAmount(localUsedBalance);
//        }
        return returnOversizedPlaceInstruction;
    }

    @SuppressWarnings("UnusedReturnValue")
    synchronized boolean startingGettingOrders() {
        final boolean modified;
        if (this.startedGettingOrders) {
            logger.error("startedGettingOrders already true in SafetyLimits ... normal in the beginning, and only if previous program run didn't end normally");
            modified = false;
        } else {
            modified = true;
            if (!this.tempInstructionsListMap.isEmpty()) {
                logger.error("tempInstructionsListMap size {} in startedGettingOrders: {}", this.tempInstructionsListMap.size(), Generic.objectToString(this.tempInstructionsListMap));
            }
        }
        this.startedGettingOrders = true;
        return modified;
    }

    private synchronized double getEventAmount(final String eventId) {
        final Double amountObject = this.eventAmounts.get(eventId);
        return amountObject == null ? 0 : amountObject;
    }

    private synchronized double getRunnerAmount(final SafeRunner safeRunner) {
        final Double amountObject = this.runnerAmounts.get(safeRunner);
        return amountObject == null ? 0 : amountObject;
    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized double addAmountFromPlaceInstructionList(final String eventId, final String marketId, final Iterable<? extends PlaceInstruction> placeInstructionsList) {
        final double addedAmount;
        if (placeInstructionsList == null) {
            addedAmount = 0d;
            logger.error("null placeInstructionsList in SafetyLimits addAmountFromPlaceInstructionList for: {} {} {}", eventId, marketId, Generic.objectToString(this));
        } else {
            final boolean isEachWayMarket = Formulas.isEachWayMarketType(marketId);
            addedAmount = getAmountFromPlaceInstructionList(placeInstructionsList, isEachWayMarket);
            addAmountToDoubleMap(eventId, addedAmount, this.eventAmounts);
            addAmountToDoubleMap(marketId, addedAmount, this.marketAmounts);

            for (final PlaceInstruction placeInstruction : placeInstructionsList) {
                final double instructionAddedAmount = placeInstruction.getPlacedAmount(isEachWayMarket);
                final SafeRunner safeRunner = new SafeRunner(marketId, placeInstruction.getSelectionId(), placeInstruction.getSide());
                addAmountToDoubleMap(safeRunner, instructionAddedAmount, this.runnerAmounts);
            }
        }
        return addedAmount;
    }

    synchronized void addOrderSummaries(final Iterable<CurrentOrderSummary> currentOrderSummaries, final Iterable<? extends ClearedOrderSummary> clearedOrderSummaries) {
        this.eventAmounts.clear();
        this.marketAmounts.clear();
        this.runnerAmounts.clear();
        final Set<Map.Entry<String, HashMap<String, List<PlaceInstruction>>>> tempInstructionsListMapEntrySet = this.tempInstructionsListMap.entrySet();
        for (final Map.Entry<String, HashMap<String, List<PlaceInstruction>>> mainEntry : tempInstructionsListMapEntrySet) {
            final String eventId = mainEntry.getKey();
            final HashMap<String, List<PlaceInstruction>> eventInstructionsListMap = mainEntry.getValue();
            if (eventInstructionsListMap != null) {
                final Set<Map.Entry<String, List<PlaceInstruction>>> eventInstructionsListMapEntrySet = eventInstructionsListMap.entrySet();
                for (final Map.Entry<String, List<PlaceInstruction>> eventEntry : eventInstructionsListMapEntrySet) {
                    final String marketId = eventEntry.getKey();
                    final List<PlaceInstruction> instructionsList = eventEntry.getValue();
                    addAmountFromPlaceInstructionList(eventId, marketId, instructionsList);
                }
            } else {
                logger.error("null eventInstructionsListMap in SafetyLimits addOrderSummaries for: {} {}", eventId, Generic.objectToString(this));
            }
        }
        if (currentOrderSummaries == null) {
            logger.error("null currentOrderSummaries in SafetyLimits addOrderSummaries");
        } else {
            final TreeSet<String> newMarketIds = new TreeSet<>();
            for (final CurrentOrderSummary currentOrderSummary : currentOrderSummaries) {
                addAmountFromCurrentOrderSummary(currentOrderSummary, newMarketIds);
            }
            if (newMarketIds.isEmpty()) { // no new marketIds found, nothing to do
            } else {
                logger.info("new marketIds to check from addOrderSummaries: {} launch: findMarkets", newMarketIds.size());
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, newMarketIds));
            }
        }
        if (clearedOrderSummaries == null) {
            logger.error("null clearedOrderSummaries in SafetyLimits addOrderSummaries");
        } else {
            for (final ClearedOrderSummary clearedOrderSummary : clearedOrderSummaries) {
                addAmountFromClearedOrderSummary(clearedOrderSummary);
            }
        }
        this.tempInstructionsListMap.clear();
        if (!this.startedGettingOrders) {
            logger.error("startedGettingOrders false in addOrderSummaries in SafetyLimits");
        }
        this.startedGettingOrders = false;
    }

    private synchronized double getAmountFromPlaceInstructionList(final Iterable<? extends PlaceInstruction> placeInstructionsList, final boolean isEachWayMarket) {
        double totalAmount;
        if (placeInstructionsList == null) {
            logger.error("null placeInstructionsList in SafetyLimits getAmountFromPlaceInstructionList: {}", Generic.objectToString(this));
            totalAmount = 0d;
        } else {
            totalAmount = 0d;
            for (final PlaceInstruction placeInstruction : placeInstructionsList) {
                final double singleAmount = placeInstruction.getPlacedAmount(isEachWayMarket);
                totalAmount += singleAmount;
            }
        }
        return totalAmount;
    }

    public synchronized double getMarketAmount(final String marketId) {
        final Double amountObject = this.marketAmounts.get(marketId);
        return amountObject == null ? 0 : amountObject;
    }

    private synchronized double getAmountFromCurrentOrderSummary(final CurrentOrderSummary currentOrderSummary) {
        final double totalAmount;
        if (currentOrderSummary == null) {
            logger.error("null currentOrderSummary in SafetyLimits getAmountFromCurrentOrderSummary");
            totalAmount = 0d;
        } else {
            totalAmount = currentOrderSummary.getPlacedAmount();
        }
        return totalAmount;
    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized double addAmountFromCurrentOrderSummary(@NotNull final CurrentOrderSummary currentOrderSummary, final Collection<? super String> newMarketIds) {
        final String marketId = currentOrderSummary.getMarketId();
        final String eventId = currentOrderSummary.getEventId(Statics.marketCataloguesMap);
//        if (eventId == null) {
//            eventId = Formulas.getEventIdOfMarketId(marketId);
//        } else { // I got the eventId, nothing more to do in this conditional
//        }

        final double addedAmount = getAmountFromCurrentOrderSummary(currentOrderSummary);

        if (eventId == null) {
//            logger.info("getting event of marketId {} in addAmountFromCurrentOrderSummary", marketId);
            newMarketIds.add(marketId);
        } else {
            addAmountToDoubleMap(eventId, addedAmount, this.eventAmounts);
        }

        if (marketId == null) {
            logger.error("bad error in addAmountFromCurrentOrderSummary, marketId is null: {}", Generic.objectToString(currentOrderSummary));
        } else {
            addAmountToDoubleMap(marketId, addedAmount, this.marketAmounts);

            final SafeRunner safeRunner = new SafeRunner(marketId, currentOrderSummary.getSelectionId(), currentOrderSummary.getSide());
            addAmountToDoubleMap(safeRunner, addedAmount, this.runnerAmounts);
        }

        return addedAmount;
    }

    private synchronized double getAmountFromClearedOrderSummary(final ClearedOrderSummary clearedOrderSummary) {
        final double totalAmount;
        if (clearedOrderSummary == null) {
            logger.error("null clearedOrderSummary in SafetyLimits getAmountFromClearedOrderSummary");
            totalAmount = 0d;
        } else {
            final Double profitObject = clearedOrderSummary.getProfit();
            if (profitObject != null) {
                totalAmount = -profitObject;
            } else {
                totalAmount = 0d;
                logger.error("null profit in clearedOrderSummary in SafetyLimits getAmountFromClearedOrderSummary: {}", Generic.objectToString(clearedOrderSummary));
            }
        }
        return totalAmount;
    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized double addAmountFromClearedOrderSummary(@NotNull final ClearedOrderSummary clearedOrderSummary) {
        final String eventId = clearedOrderSummary.getEventId();
        final String marketId = clearedOrderSummary.getMarketId();
        final double addedAmount = getAmountFromClearedOrderSummary(clearedOrderSummary);
        addAmountToDoubleMap(eventId, addedAmount, this.eventAmounts);
        addAmountToDoubleMap(marketId, addedAmount, this.marketAmounts);

        final SafeRunner safeRunner = new SafeRunner(marketId, clearedOrderSummary.getSelectionId(), clearedOrderSummary.getSide());
        addAmountToDoubleMap(safeRunner, addedAmount, this.runnerAmounts);

        return addedAmount;
    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized <T> double addAmountToDoubleMap(final T key, final double addedAmount, @NotNull final HashMap<T, Double> map) {
        final Double existingAmountObject = map.get(key);
        final double existingAmount = existingAmountObject == null ? 0d : existingAmountObject;
        final double totalAmount = existingAmount + addedAmount;
        map.put(key, totalAmount);
        return totalAmount;
    }

    private synchronized double getAvailableFundsForRunner(final SafeRunner safeRunner) {
        double returnValue;

        if (safeRunner == null) {
            returnValue = 0d;
            logger.error("null safeRunner in SafetyLimits during getAvailableFunds: {}", Generic.objectToString(this));
        } else {
            // overall limit
            returnValue = Statics.safetyLimits.getAvailableLimit();

            // initial limit per safeRunner
            if (safeRunner.getMinScoreScrapers() <= 2 && System.currentTimeMillis() - safeRunner.getAddedStamp() <= SafeBetSafetyLimits.specialLimitInitialPeriod) {
                returnValue = Math.min(returnValue, Statics.safetyLimits.existingFunds.getAvailableFunds() * SafeBetSafetyLimits.initialSafeRunnerLimitFraction - getRunnerAmount(safeRunner));
//                    safeRunner.getPlacedAmount());
            } else { // returnValue was already set before if, no need to apply initial limit
            }

            // permanent limit per safe event
            final String marketId = safeRunner.getMarketId();
            final String eventId = info.fmro.shared.utility.Formulas.getEventIdOfMarketId(marketId, Statics.marketCataloguesMap);
            if (eventId == null) {
                returnValue = Math.min(returnValue, 0d);
                logger.error("null eventId for marketId {} in SafetyLimits during getAvailableFunds: {}", marketId, Generic.objectToString(safeRunner));
            } else {
                returnValue = Math.min(returnValue, Statics.safetyLimits.existingFunds.getAvailableFunds() * SafeBetSafetyLimits.safeEventLimitFraction - getEventAmount(eventId));
            }
        }

        return returnValue;
    }

    public synchronized boolean addInstructionsList(final String eventId, final String marketId, final List<? extends PlaceInstruction> placeInstructionsList) {
        if (placeInstructionsList == null) {
            logger.error("null placeInstructionsList in SafetyLimits addInstructionsList: {} {}", eventId, marketId);
        } else {
            if (this.startedGettingOrders) {
                if (this.tempInstructionsListMap.containsKey(eventId)) {
                    final HashMap<String, List<PlaceInstruction>> existingHashMap = this.tempInstructionsListMap.get(eventId);
                    if (existingHashMap.containsKey(marketId)) {
                        final List<PlaceInstruction> existingList = existingHashMap.get(marketId);
                        existingList.addAll(placeInstructionsList);
                    } else {
                        existingHashMap.put(marketId, new ArrayList<>(placeInstructionsList));
                    }
                } else {
                    final HashMap<String, List<PlaceInstruction>> addedHashMap = new HashMap<>(2, 0.75F);
                    addedHashMap.put(marketId, new ArrayList<>(placeInstructionsList));
                    this.tempInstructionsListMap.put(eventId, addedHashMap);
                }
            } else {
                // not during gettingOrders, nothing to be done on this branch
            }
            addAmountFromPlaceInstructionList(eventId, marketId, placeInstructionsList);
        }
        return this.isStartedGettingOrders();
    }

    public synchronized boolean isStartedGettingOrders() {
        return this.startedGettingOrders;
    }
}
