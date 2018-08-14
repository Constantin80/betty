package info.fmro.betty.objects;

import com.google.common.util.concurrent.AtomicDouble;
import info.fmro.betty.entities.ClearedOrderSummary;
import info.fmro.betty.entities.CurrencyRate;
import info.fmro.betty.entities.CurrentOrderSummary;
import info.fmro.betty.entities.ExchangePrices;
import info.fmro.betty.entities.LimitOrder;
import info.fmro.betty.entities.MarketBook;
import info.fmro.betty.entities.PlaceInstruction;
import info.fmro.betty.entities.PriceSize;
import info.fmro.betty.entities.Runner;
import info.fmro.betty.enums.CommandType;
import info.fmro.betty.enums.MarketStatus;
import info.fmro.betty.enums.OrderType;
import info.fmro.betty.enums.PersistenceType;
import info.fmro.betty.enums.RunnerStatus;
import info.fmro.betty.enums.Side;
import info.fmro.betty.main.CancelOrdersThread;
import info.fmro.betty.main.LaunchCommandThread;
import info.fmro.betty.main.MaintenanceThread;
import info.fmro.betty.main.RescriptOpThread;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import info.fmro.shared.utility.SynchronizedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class SafetyLimits
        implements Serializable {
    private static final long specialLimitInitialPeriod = 120_000L;
    private static final Logger logger = LoggerFactory.getLogger(SafetyLimits.class);
    private static final long serialVersionUID = 9100257467817248236L;
    private static final PersistenceType persistenceType = PersistenceType.LAPSE; // sometimes PersistenceType.PERSIST causes an order placing error
    private static final OrderType orderType = OrderType.LIMIT;
    private static final double reserveFraction = .5d; // reserve is 50% of total (but starts at 500 and only increases)
    private static final double initialSafeRunnerLimitFraction = .1d; // max bet during the initial period, as fraction of total; applied for safeRunners
    private static final double safeEventLimitFraction = .2d; // max bet per safe event
    public final AtomicDouble currencyRate = new AtomicDouble(1d); // GBP/EUR, 1.1187000274658203 right now, on 13-08-2018; default 1d
    //    private final HashMap<AtomicDouble, Long> tempReserveMap = new HashMap<>(0);
    // private final HashSet<AtomicDouble> localUsedBalanceSet = new HashSet<>(0);
//    private double reserve = 500d; // default value; will always be truncated to int; can only increase
    private double reserve = 0d; // default value; will always be truncated to int; can only increase; reduced to 0 for now and will be kept such until liquidity reaches 1k
    private double availableFunds; // total amount available on the account; it includes the reserve
    private final HashMap<String, Double> eventAmounts = new HashMap<>(16, 0.75F); // eventId, totalAmount
    private final HashMap<String, Double> marketAmounts = new HashMap<>(16, 0.75F); // marketId, totalAmount
    private final HashMap<SafeRunner, Double> runnerAmounts = new HashMap<>(16, 0.75F); // safeRunner, totalAmount
    private final HashMap<String, HashMap<String, List<PlaceInstruction>>> tempInstructionsListMap = new HashMap<>(2, 0.75F);
    private boolean startedGettingOrders;

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized void copyFrom(SafetyLimits safetyLimits) {
//        if (!this.tempReserveMap.isEmpty()) {
//            logger.error("not empty map in SafetyLimits copyFrom: {}", Generic.objectToString(this));
//        }
//        this.tempReserveMap.clear();
//        this.tempReserveMap.putAll(safetyLimits.tempReserveMap);

        if (!this.tempInstructionsListMap.isEmpty() || !this.eventAmounts.isEmpty() || !this.marketAmounts.isEmpty() || !this.runnerAmounts.isEmpty()) {
            logger.error("not empty maps in SafetyLimits copyFrom: {}", Generic.objectToString(this));
        }

        this.tempInstructionsListMap.clear();
        if (safetyLimits.tempInstructionsListMap != null) {
            this.tempInstructionsListMap.putAll(safetyLimits.tempInstructionsListMap);
        } else {
            logger.error("null tempInstructionsListMap in SafetyLimits copyFrom: {}", Generic.objectToString(safetyLimits));
        }
        this.eventAmounts.clear();
        if (safetyLimits.eventAmounts != null) {
            this.eventAmounts.putAll(safetyLimits.eventAmounts);
        } else {
            logger.error("null eventAmounts in SafetyLimits copyFrom: {}", Generic.objectToString(safetyLimits));
        }
        this.marketAmounts.clear();
        if (safetyLimits.marketAmounts != null) {
            this.marketAmounts.putAll(safetyLimits.marketAmounts);
        } else {
            logger.error("null marketAmounts in SafetyLimits copyFrom: {}", Generic.objectToString(safetyLimits));
        }
        this.runnerAmounts.clear();
        if (safetyLimits.runnerAmounts != null) {
            this.runnerAmounts.putAll(safetyLimits.runnerAmounts);
        } else {
            logger.error("null runnerAmounts in SafetyLimits copyFrom: {}", Generic.objectToString(safetyLimits));
        }
        this.startedGettingOrders = safetyLimits.startedGettingOrders;
        this.availableFunds = safetyLimits.availableFunds;
        this.currencyRate.set(safetyLimits.currencyRate.get());
        this.setReserve(safetyLimits.reserve);

        if (currencyRate.get() == 1d || currencyRate.get() == 0d) { // likely default values
            Statics.timeStamps.setLastListCurrencyRates(0L); // get currencyRate as soon as possible
        }
    }

    public synchronized void createPlaceInstructionList(List<Runner> runnersList, SynchronizedSet<SafeRunner> safeRunnersSet, MarketBook marketBook, String marketId, long startTime, long endTime, MarketStatus marketStatus, boolean inplay, int betDelay,
                                                        long timePreviousMarketBookCheck) {
        final List<PlaceInstruction> placeInstructionsList = new ArrayList<>(0);

        for (Runner runner : runnersList) {
            if (runner != null) {
                final Long runnerIdObject = runner.getSelectionId();
                if (runnerIdObject != null) {
                    final long runnerId = runnerIdObject;
                    SafeRunner safeRunner = null;
                    final HashSet<SafeRunner> setCopy = safeRunnersSet.copy();
                    for (SafeRunner loopSafeRunner : setCopy) {
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
                        Side side = safeRunner.getSide();
                        final ExchangePrices exchangePrices = runner.getEx();
                        if (exchangePrices != null) {
                            List<PriceSize> safePriceSizesList;
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
                                    BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "market with bets, marketCatalogue map");
                                    MaintenanceThread.removeFromSecondaryMaps(marketId);
                                } else {
                                    Statics.safeMarketsImportantMap.put(marketId, endTime);
                                    if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId, startTime)) {
                                        BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "market with bets after importantMap add, marketCatalogue map");
                                        MaintenanceThread.removeFromSecondaryMaps(marketId);
//                                    } else if (safeRunner.isIgnored(startTime)) {
//                                        logger.error("ignored safeRunner with bets after importantMap add: {} {}", marketId,
//                                                     Generic.objectToString(safeRunner));
                                    } else {
                                        for (PriceSize priceSize : safePriceSizesList) {
                                            final double price = priceSize.getPrice();
                                            final double size = priceSize.getSize();

                                            if (marketStatus == MarketStatus.OPEN) {
                                                Generic.alreadyPrintedMap.logOnce(Statics.debugLevel.check(2, 171), logger, LogLevel.INFO,
                                                                                  "safe bet in market: {} marketStatus: {} inplay={} betDelay={} runner: {} runnerStatus: {} side: {} price: {} size: {}", marketId, marketStatus, inplay, betDelay, runnerId,
                                                                                  runnerStatus, side, price, size);

                                                final SafeBet safeBet = new SafeBet(marketId, marketStatus, inplay, betDelay, runnerId, runnerStatus, price, size, side);

                                                Statics.safeBetsMap.addAndGetSafeBetStats(marketId, safeBet, endTime, timePreviousMarketBookCheck);

                                                if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId, startTime)) {
                                                    BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, startTime, "market with bets after safeBet add, marketCatalogue map");
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
                                                    oversizedPlaceInstruction = Statics.safetyLimits.processOpenMarket(safeRunner, runner, side, price, size, betDelay, marketId, runnerId, placeInstructionsList, previousOversizedRealAmount,
                                                                                                                       oversizedPlaceInstruction);
                                                }
                                            } else { // market not open, can't place bets
                                                Generic.alreadyPrintedMap.logOnce(logger, LogLevel.INFO, "safe bet in not active market: {} marketStatus: {} inplay={} betDelay={} runner: {} runnerStatus: {} side: {} price: {} size: {}",
                                                                                  marketId, marketStatus, inplay, betDelay, runnerId, runnerStatus, side, price, size);
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
                    logger.error("null runnerIdLong in getMarketBooks for: {} {}", Generic.objectToString(runner),
                                 Generic.objectToString(marketBook));
                }
            } else {
                logger.error("null runner in getMarketBooks for: {}", Generic.objectToString(marketBook));
            }
        } // end for runnersList

        if (placeInstructionsList.size() > 0) {
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
                final String eventId = Formulas.getEventIdOfMarketId(marketId);
                final boolean isStartedGettingOrders = addInstructionsList(eventId, marketId, placeInstructionsList);

                Statics.threadPoolExecutorImportant.execute(new RescriptOpThread<>(marketId, placeInstructionsList, isStartedGettingOrders)); // place order thread
            }
        } else { // no safe bets found, nothing to be done
        }
    }

    private synchronized PlaceInstruction processOpenMarket(SafeRunner safeRunner, Runner runner, Side side, double price, double size, int betDelay, String marketId, long runnerId, List<PlaceInstruction> placeInstructionsList,
                                                            AtomicDouble previousOversizedRealAmount, PlaceInstruction oversizedPlaceInstruction) {
        final HashSet<Long> orderDelaysSet = new HashSet<>(0);
        final Double handicap = runner.getHandicap();
        final double localAvailableFunds = Statics.safetyLimits.getAvailableFunds(safeRunner);
        PlaceInstruction returnOversizedPlaceInstruction = oversizedPlaceInstruction;
        boolean currentOrderIsOversized = false;
        double localUsedBalance = 0d;

        if (BlackList.notExistOrIgnored(Statics.marketCataloguesMap, marketId)) {
            BlackList.printNotExistOrBannedErrorMessages(Statics.marketCataloguesMap, marketId, "processOpenMarket, marketCatalogue map");
        } else if (localAvailableFunds < 2d || (side == Side.LAY && localAvailableFunds < 2d * (price - 1d))) {
            logger.warn("not enough funds to place bets: {} {} {}", side, price, localAvailableFunds);

            CancelOrdersThread.addOrder(0L);
        } else {
            double orderSize;
            long orderDelay;
            if (side == Side.BACK) {
                if (size <= localAvailableFunds) {
                    orderSize = size;
                    if (orderSize + 0.0101d <= localAvailableFunds) {
                        // extra 1 cent, and a bit for double imprecision
                        orderSize += 0.0101d;
                    }
                    if (orderSize < 2d) {
                        orderSize = 2d;
                        orderDelay = (long) betDelay * 1000L + 2001L;
                        logger.info("will place oversized order {} {}", orderSize, size);
                        currentOrderIsOversized = true;

                        // RescriptOpThread<?> rescriptOpCancelThread = new RescriptOpThread<>(overSizedOrderDelay, null);
                        // rescriptOpCancelThread.start();
                        // Statics.rescriptOpThreadsSet.add(rescriptOpCancelThread);
                    } else {
                        orderDelay = (long) betDelay * 1000L + 10001L; // for orders placed too late to match
                    }
                } else {
                    // orderSize = Generic.truncateDouble(localAvailableToBetBalance, 2); // truncate done in LimitOrder class
                    orderSize = localAvailableFunds;
                    orderDelay = (long) betDelay * 1000L + 10001L;
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
                        orderDelay = (long) betDelay * 1000L + 2001L;
                        logger.info("will place oversized order {} {}", orderSize, size);
                        currentOrderIsOversized = true;

                        // RescriptOpThread<?> rescriptOpCancelThread = new RescriptOpThread<>(overSizedOrderDelay, null);
                        // rescriptOpCancelThread.start();
                        // Statics.rescriptOpThreadsSet.add(rescriptOpCancelThread);
                    } else {
                        orderDelay = (long) betDelay * 1000L + 10001L; // for orders placed too late to match
                    }
                } else {
                    // orderSize = Generic.truncateDouble(maximumBet, 2); // truncate is done in LimitOrder class
                    orderSize = maximumBet;
                    orderDelay = (long) betDelay * 1000L + 10001L;
                }
                // localAvailableToBetBalance -= orderSize * (price - 1d);
            } else {
                logger.error("STRANGE unknown side while building LimitOrder: {} {} {}", side, Generic.objectToString(runner));
                orderSize = -1d;
                orderDelay = 0L;
            }

            if (orderSize >= 2d) {
                LimitOrder limitOrder = new LimitOrder();
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
                        localUsedBalance += orderSize * (price - 1d);
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

                OrderPrice orderPrice = new OrderPrice(marketId, runnerId, side, price);

                boolean shouldAddInstruction = Formulas.shouldAddInstruction(orderPrice, orderSize);
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

                            CancelOrdersThread.addOrder(orderDelay);
                        }
                    } else { // in current setup, delayed cancel order is always placed
                        logger.error("overSizedOrderDelay {} ms for: {}", orderDelay, Generic.objectToString(placeInstruction));
                    }

                    // no orderDelay change needed; this support is no longer very important anyway
                    double newSize = 0d;
                    final double previousRealAmount = previousOversizedRealAmount.get();
                    if (previousRealAmount > 0d && returnOversizedPlaceInstruction != null) {
                        LimitOrder previousLimitOrder = returnOversizedPlaceInstruction.getLimitOrder();
                        final double previousPrice = previousLimitOrder.getPrice();

                        if (//(side.equals(Side.BACK) && previousPrice > limitOrder.getPrice()) ||
                            //(
                                side.equals(Side.BACK) || (side.equals(Side.LAY) && previousPrice < limitOrder.getPrice())) //)
                        { // in case of Side.BACK, limitOrder.getPrice() gets modified before and after placing the order, so it can't be used as if condition
                            final double previousOrderSize = previousLimitOrder.getSize();

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
                                    localUsedBalance += newSize - limitOrder.getSize() - previousOrderSize;
                                    logger.info("{} newSize: {} fromSize: {} size: {} price: {} previousOrderSize: {} previousRealAmount: {} previousPrice: {}", side.name(), newSize, limitOrder.getSize(), size, price, previousOrderSize, previousRealAmount,
                                                previousPrice);
                                    limitOrder.setSize(newSize);
                                    OrderPrice previousOrderPrice = new OrderPrice(marketId, runnerId, side, previousPrice);
                                    Formulas.removeInstruction(previousOrderPrice, previousOrderSize);
                                    placeInstructionsList.remove(returnOversizedPlaceInstruction);
                                    break;
                                case LAY:
                                    boolean betterToModify;
                                    if (currentOrderIsOversized) {
                                        newSize = previousRealAmount + size;
                                        if (newSize < 2d) {
                                            betterToModify = true;
                                        } else {
                                            double sizeDifference = newSize - limitOrder.getSize();
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
                                        localUsedBalance += (newSize - limitOrder.getSize()) * (price - 1d) - previousOrderSize * (previousPrice - 1d);
                                        logger.info("{} newSize: {} fromSize: {} size: {} price: {} previousOrderSize: {} previousRealAmount: {} previousPrice: {}", side.name(), newSize, limitOrder.getSize(), size, price, previousOrderSize,
                                                    previousRealAmount, previousPrice);
                                        limitOrder.setSize(newSize);
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

    public synchronized double getAvailableFunds(SafeRunner safeRunner) {
        double returnValue;

        if (safeRunner == null) {
            returnValue = 0d;
            logger.error("null safeRunner in SafetyLimits during getAvailableFunds: {}", Generic.objectToString(this));
        } else {
            // overall limit
            returnValue = this.availableFunds - this.getReserve() - 0.01d; // leave 1 cent, to avoid errors

            // initial limit per safeRunner
            if (safeRunner.getMinScoreScrapers() <= 2 && System.currentTimeMillis() - safeRunner.getAddedStamp() <= SafetyLimits.specialLimitInitialPeriod) {
                returnValue = Math.min(returnValue, this.availableFunds * SafetyLimits.initialSafeRunnerLimitFraction - getRunnerAmount(safeRunner));
//                    safeRunner.getPlacedAmount());
            } else { // returnValue was already set before if, no need to apply initial limit
            }

            // permanent limit per safe event
            final String marketId = safeRunner.getMarketId();
            final String eventId = Formulas.getEventIdOfMarketId(marketId);
            if (eventId == null) {
                returnValue = Math.min(returnValue, 0d);
                logger.error("null eventId for marketId {} in SafetyLimits during getAvailableFunds: {}", marketId, Generic.objectToString(safeRunner));
            } else {
                returnValue = Math.min(returnValue, this.availableFunds * SafetyLimits.safeEventLimitFraction - getEventAmount(eventId));
            }
        }

        return returnValue;
    }

    public synchronized double getReserve() {
        double returnValue = this.reserve;

//        if (!tempReserveMap.isEmpty()) {
//            // long currentTime = System.currentTimeMillis();
//            final Set<AtomicDouble> keys = tempReserveMap.keySet();
//            for (AtomicDouble key : keys) {
//                returnValue += key.get();
//            }
//
//            // Iterator<Entry<AtomicDouble, Long>> iterator = tempReserveMap.entrySet().iterator();
//            // while (iterator.hasNext()) {
//            //     Entry<AtomicDouble, Long> entry = iterator.next();
//            // if (entry.getValue() < currentTime) {
//            // iterator.remove(); // I remove when I process funds; this partly avoids errors with tempReserve removed and availableFunds not updated
//            // } else {
//            //     returnValue += entry.getKey().get();
//            // removed logging as well in order to optimise speed
//            //     if (Statics.debugLevel.check(3, 159)) {
//            //         logger.info("tempReserve: {} {}", returnValue, this.reserve);
//            //     }
//            // }
//            // }
//        }
        // if (!localUsedBalanceSet.isEmpty()) {
        //     for (AtomicDouble localUsedBalance : localUsedBalanceSet) {
        //         returnValue += localUsedBalance.get();
        //         logger.info("localReserve: {} {}", returnValue, this.reserve);
        //     }
        // }
        return returnValue;
    }

    public synchronized boolean setReserve(double reserve) {
        boolean modified;

        double truncatedValue = Math.floor(reserve);
        if (truncatedValue > this.reserve) {
            logger.info("modifying reserve value {} to {}", this.reserve, truncatedValue);
            this.reserve = truncatedValue;
            modified = true;
        } else {
            modified = false;
        }
        return modified;
    }

    //    public synchronized void addTempReserve(AtomicDouble tempReserve, long timeToLive) {
//        this.tempReserveMap.put(tempReserve, System.currentTimeMillis() + timeToLive);
//    }
    // public synchronized void addTempReserve(AtomicDouble tempReserve, long timeToLive, AtomicDouble localUsedBalance) {
    //     removeLocalUsedBalance(localUsedBalance);
    //     addTempReserve(tempReserve, timeToLive);
    // }
    // public synchronized void addLocalUsedBalance(AtomicDouble localUsedBalance) {
    //     this.localUsedBalanceSet.add(localUsedBalance);
    // }
    // public synchronized void removeLocalUsedBalance(AtomicDouble localUsedBalance) {
    //     this.localUsedBalanceSet.remove(localUsedBalance);
    // }
    public synchronized boolean processFunds(double availableFunds) {
        this.availableFunds = availableFunds;
        final double newReserve = Math.floor(availableFunds * SafetyLimits.reserveFraction);

//        if (!tempReserveMap.isEmpty()) {
//            final long currentTime = System.currentTimeMillis();
//            final Iterator<Entry<AtomicDouble, Long>> iterator = tempReserveMap.entrySet().iterator();
//            while (iterator.hasNext()) {
//                final Entry<AtomicDouble, Long> entry = iterator.next();
//                if (entry.getValue() < currentTime) {
//                    iterator.remove();
//                } else { // nothing to be done here
//                }
//            }
//        }
        return setReserve(newReserve);
    }

    public synchronized boolean addInstructionsList(String eventId, String marketId, List<PlaceInstruction> placeInstructionsList) {
        if (placeInstructionsList == null) {
            logger.error("null placeInstructionsList in SafetyLimits addInstructionsList: {} {}", eventId, marketId);
        } else {
            if (this.startedGettingOrders) {
                if (tempInstructionsListMap.containsKey(eventId)) {
                    final HashMap<String, List<PlaceInstruction>> existingHashMap = tempInstructionsListMap.get(eventId);
                    if (existingHashMap.containsKey(marketId)) {
                        final List<PlaceInstruction> existingList = existingHashMap.get(marketId);
                        existingList.addAll(placeInstructionsList);
                    } else {
                        existingHashMap.put(marketId, new ArrayList<>(placeInstructionsList));
                    }
                } else {
                    final HashMap<String, List<PlaceInstruction>> addedHashMap = new HashMap<>(2, 0.75F);
                    addedHashMap.put(marketId, new ArrayList<>(placeInstructionsList));
                    tempInstructionsListMap.put(eventId, addedHashMap);
                }
            } else {
                // not during gettingOrders, nothing to be done on this branch
            }
            addAmountFromPlaceInstructionList(eventId, marketId, placeInstructionsList);
        }
        return this.isStartedGettingOrders();
    }

    public synchronized double getEventAmount(String eventId) {
        final double amount;
        final Double amountObject = this.eventAmounts.get(eventId);
        if (amountObject == null) {
            amount = 0;
        } else {
            amount = amountObject;
        }
        return amount;
    }

    public synchronized double getRunnerAmount(SafeRunner safeRunner) {
        final double amount;
        final Double amountObject = this.runnerAmounts.get(safeRunner);
        if (amountObject == null) {
            amount = 0;
        } else {
            amount = amountObject;
        }
        return amount;
    }

    private synchronized double addAmountFromPlaceInstructionList(String eventId, String marketId, List<PlaceInstruction> placeInstructionsList) {
        final double addedAmount;
        if (placeInstructionsList == null) {
            addedAmount = 0d;
            logger.error("null placeInstructionsList in SafetyLimits addAmountFromPlaceInstructionList for: {} {} {}", eventId, marketId, Generic.objectToString(this));
        } else {
            final boolean isEachWayMarket = Formulas.isEachWayMarketType(marketId);
            addedAmount = getAmountFromPlaceInstructionList(placeInstructionsList, isEachWayMarket);
            addAmountToDoubleMap(eventId, addedAmount, this.eventAmounts);
            addAmountToDoubleMap(marketId, addedAmount, this.marketAmounts);

            for (PlaceInstruction placeInstruction : placeInstructionsList) {
                final double instructionAddedAmount = placeInstruction.getPlacedAmount(isEachWayMarket);
                final SafeRunner safeRunner = new SafeRunner(marketId, placeInstruction.getSelectionId(), placeInstruction.getSide());
                addAmountToDoubleMap(safeRunner, instructionAddedAmount, this.runnerAmounts);
            }
        }
        return addedAmount;
    }

    public synchronized void addOrderSummaries(HashSet<CurrentOrderSummary> currentOrderSummaries, HashSet<ClearedOrderSummary> clearedOrderSummaries) {
        this.eventAmounts.clear();
        this.marketAmounts.clear();
        this.runnerAmounts.clear();
        final Set<Entry<String, HashMap<String, List<PlaceInstruction>>>> tempInstructionsListMapEntrySet = tempInstructionsListMap.entrySet();
        for (Entry<String, HashMap<String, List<PlaceInstruction>>> mainEntry : tempInstructionsListMapEntrySet) {
            final String eventId = mainEntry.getKey();
            final HashMap<String, List<PlaceInstruction>> eventInstructionsListMap = mainEntry.getValue();
            if (eventInstructionsListMap != null) {
                final Set<Entry<String, List<PlaceInstruction>>> eventInstructionsListMapEntrySet = eventInstructionsListMap.entrySet();
                for (Entry<String, List<PlaceInstruction>> eventEntry : eventInstructionsListMapEntrySet) {
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
            for (CurrentOrderSummary currentOrderSummary : currentOrderSummaries) {
                addAmountFromCurrentOrderSummary(currentOrderSummary, newMarketIds);
            }
            if (newMarketIds.size() > 0) {
                logger.info("new marketIds to check from addOrderSummaries: {} launch: findMarkets", newMarketIds.size());
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, newMarketIds));
            } else { // no new marketIds found, nothing to do
            }
        }
        if (clearedOrderSummaries == null) {
            logger.error("null clearedOrderSummaries in SafetyLimits addOrderSummaries");
        } else {
            for (ClearedOrderSummary clearedOrderSummary : clearedOrderSummaries) {
                addAmountFromClearedOrderSummary(clearedOrderSummary);
            }
        }
        tempInstructionsListMap.clear();
        if (!this.startedGettingOrders) {
            logger.error("startedGettingOrders false in addOrderSummaries in SafetyLimits");
        }
        this.startedGettingOrders = false;
    }

    public synchronized boolean isStartedGettingOrders() {
        return startedGettingOrders;
    }

    private synchronized double getAmountFromPlaceInstructionList(List<PlaceInstruction> placeInstructionsList, boolean isEachWayMarket) {
        double totalAmount;
        if (placeInstructionsList == null) {
            logger.error("null placeInstructionsList in SafetyLimits getAmountFromPlaceInstructionList: {}", Generic.objectToString(this));
            totalAmount = 0d;
        } else {
            totalAmount = 0d;
            for (PlaceInstruction placeInstruction : placeInstructionsList) {
                final double singleAmount = placeInstruction.getPlacedAmount(isEachWayMarket);
                totalAmount += singleAmount;
            }
        }
        return totalAmount;
    }

    public synchronized double getMarketAmount(String marketId) {
        final double amount;
        final Double amountObject = this.marketAmounts.get(marketId);
        if (amountObject == null) {
            amount = 0;
        } else {
            amount = amountObject;
        }
        return amount;
    }

    public synchronized boolean startingGettingOrders() {
        final boolean modified;
        if (this.startedGettingOrders) {
            logger.error("startedGettingOrders already true in SafetyLimits ... normal in the beginning, and only if previous program run didn't end normally");
            modified = false;
        } else {
            modified = true;
            if (!tempInstructionsListMap.isEmpty()) {
                logger.error("tempInstructionsListMap size {} in startedGettingOrders: {}", tempInstructionsListMap.size(), Generic.objectToString(tempInstructionsListMap));
            }
        }
        this.startedGettingOrders = true;
        return modified;
    }

    private synchronized <T> double addAmountToDoubleMap(T key, double addedAmount, HashMap<T, Double> map) {
        final Double existingAmountObject = map.get(key);
        final double existingAmount;
        if (existingAmountObject == null) {
            existingAmount = 0d;
        } else {
            existingAmount = existingAmountObject;
        }
        final double totalAmount = existingAmount + addedAmount;
        map.put(key, totalAmount);
        return totalAmount;
    }

    private synchronized double getAmountFromCurrentOrderSummary(CurrentOrderSummary currentOrderSummary) {
        final double totalAmount;
        if (currentOrderSummary == null) {
            logger.error("null currentOrderSummary in SafetyLimits getAmountFromCurrentOrderSummary");
            totalAmount = 0d;
        } else {
            totalAmount = currentOrderSummary.getPlacedAmount();
        }
        return totalAmount;
    }

    private synchronized double addAmountFromCurrentOrderSummary(CurrentOrderSummary currentOrderSummary, TreeSet<String> newMarketIds) {
        final String marketId = currentOrderSummary.getMarketId();
        final String eventId = currentOrderSummary.getEventId();
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

    private synchronized double getAmountFromClearedOrderSummary(ClearedOrderSummary clearedOrderSummary) {
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

    private synchronized double addAmountFromClearedOrderSummary(ClearedOrderSummary clearedOrderSummary) {
        final String eventId = clearedOrderSummary.getEventId();
        final String marketId = clearedOrderSummary.getMarketId();
        final double addedAmount = getAmountFromClearedOrderSummary(clearedOrderSummary);
        addAmountToDoubleMap(eventId, addedAmount, this.eventAmounts);
        addAmountToDoubleMap(marketId, addedAmount, this.marketAmounts);

        final SafeRunner safeRunner = new SafeRunner(marketId, clearedOrderSummary.getSelectionId(), clearedOrderSummary.getSide());
        addAmountToDoubleMap(safeRunner, addedAmount, this.runnerAmounts);

        return addedAmount;
    }

    public synchronized void setCurrencyRate(List<CurrencyRate> currencyRates) {
        if (currencyRates != null) {
            for (CurrencyRate currencyRate : currencyRates) {
                final String currencyCode = currencyRate.getCurrencyCode();
                if (Objects.equals(currencyCode, "EUR")) {
                    final Double rate = currencyRate.getRate();
                    if (rate != null) {
                        this.currencyRate.set(rate);
                    } else {
                        logger.error("null rate for: {}", Generic.objectToString(currencyRates));
                    }
                    break;
                } else { // I only need EUR rate, nothing to be done with the rest
                }
            } // end for
        } else {
            if (Statics.mustStop.get() && Statics.needSessionToken.get()) { // normal to happen during program stop, if not logged in
            } else {
                logger.error("currencyRates null");
            }
        }
    }
}
