package info.fmro.betty.main;

import info.fmro.betty.entities.CancelInstruction;
import info.fmro.betty.entities.LimitOrder;
import info.fmro.betty.entities.PlaceInstruction;
import info.fmro.betty.enums.OrderType;
import info.fmro.betty.enums.PersistenceType;
import info.fmro.betty.enums.TemporaryOrderType;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.objects.TemporaryOrder;
import info.fmro.betty.stream.cache.order.OrderMarketRunner;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.definitions.Order;
import info.fmro.betty.stream.definitions.OrderRunnerChange;
import info.fmro.betty.stream.definitions.Side;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class OrdersThread
        implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(OrdersThread.class);
    public static final long minimumCancelAllOrderSpacing = 1_000L;
    private final Set<Long> cancelAllOrdersRunTimesSet = new HashSet<>(8);
    private final AtomicLong timeLastCancelAllOrder = new AtomicLong();
    private final AtomicBoolean newCancelAllOrderAdded = new AtomicBoolean(), newOrderAdded = new AtomicBoolean();
    private final ArrayList<TemporaryOrder> temporaryOrders = new ArrayList<>();

    public synchronized void reportStreamChange(final OrderMarketRunner orderMarketRunner, final OrderRunnerChange orderRunnerChange) {
        if (this.hasTemporaryOrders()) {
            final String marketId = orderMarketRunner.getMarketId();
            final RunnerId runnerId = orderMarketRunner.getRunnerId();
            if (marketId != null && runnerId != null) {
                final Iterator<TemporaryOrder> iterator = this.temporaryOrders.iterator();
                while (iterator.hasNext()) {
                    final TemporaryOrder temporaryOrder = iterator.next();
                    final String temporaryMarketId = temporaryOrder.getMarketId();
                    if (marketId.equals(temporaryMarketId)) {
                        final RunnerId tempRunnerId = temporaryOrder.getRunnerId();
                        if (runnerId.equals(tempRunnerId)) {
                            final TemporaryOrderType orderType = temporaryOrder.getType();
                            final String betId = temporaryOrder.getBetId();
                            final Side side = temporaryOrder.getSide();
                            final double price = temporaryOrder.getPrice();
                            final double size = temporaryOrder.getSize();

                            if (orderType.equals(TemporaryOrderType.PLACE)) {
                                if (betId != null) {
                                    final Order foundOrder = orderRunnerChange.getUnmatchedOrder(betId);
                                    if (foundOrder != null) {
                                        iterator.remove();
                                    } else { // unmatched order with betId not found, looking into matched orders
                                        final double existingSize = orderMarketRunner.getMatchedSize(side, price);
                                        final double newSize = orderRunnerChange.getMatchedSize(side, price);
                                        final double sizeModification = newSize - existingSize;
                                        final boolean areEqual = Math.abs(size - sizeModification) < .02d;
                                        if (areEqual) {
                                            iterator.remove();
                                        } else { // matched amount not found or not sufficient, won't remove the temporaryOrder
                                        }
                                    }
                                } else { // placeOrder command hasn't finished and no betId available yet; yet stream only returns once, so I'll use this branch too
                                    final double existingSize = orderMarketRunner.getMatchedSize(side, price);
                                    final double newSize = orderRunnerChange.getMatchedSize(side, price);
                                    final double sizeModification = newSize - existingSize;
                                    final boolean areEqual = Math.abs(size - sizeModification) < .02d;
                                    if (areEqual) {
                                        iterator.remove();
                                    } else { // matched amount not found or not sufficient, won't remove the temporaryOrder
                                    }
                                }
                            } else if (orderType.equals(TemporaryOrderType.CANCEL)) {
                                if (betId != null) {
                                    final Order foundOrder = orderRunnerChange.getUnmatchedOrder(betId);
                                    final Order previousOrderState = orderMarketRunner.getUnmatchedOrder(betId);
                                    if (foundOrder != null && previousOrderState != null) {
                                        final Double sizeReduction = temporaryOrder.getSizeReduction();
                                        final Double sizeRemaining = foundOrder.getSr();
                                        final Double previousSizeRemaining = previousOrderState.getSr();
                                        if (sizeRemaining == null || sizeRemaining == 0d || previousSizeRemaining == null || previousSizeRemaining == 0d) { // no size remaining, or the size remaining was zero, though the latter might not be normal
                                            iterator.remove();
                                        } else if (sizeReduction == null) { // the entire order should be canceled, but it's not completely canceled, else it should have entered the previous branch, might be normal, nothing to be done
                                        } else { // a certain amount canceled
                                            final Double sizeCanceled = foundOrder.getSc();
                                            final Double previousSizeCanceled = previousOrderState.getSc();
                                            final double sizePrimitive = sizeCanceled == null ? 0 : sizeCanceled;
                                            final double previousSizePrimitive = previousSizeCanceled == null ? 0 : previousSizeCanceled;
                                            final double sizeModification = sizePrimitive - previousSizePrimitive;
                                            final boolean areEqual = Math.abs(sizeReduction - sizeModification) < .02d;
                                            if (areEqual) {
                                                iterator.remove();
                                            } else { // canceled amount not found or not sufficient, won't remove the temporaryOrder
                                            }
                                        }
                                    } else { // proper betId not found, nothing to be done
                                    }
                                } else { // placeOrder command hasn't finished and no betId available yet; yet stream only returns once, so I'll use this branch too
                                    logger.error("null betId for CANCEL orderType in reportStreamChange: {}", Generic.objectToString(temporaryOrder));
                                }
                            } else {
                                logger.error("unknown TemporaryOrderType in reportStreamChange: {}", orderType);
                            }
                        } else { // not what I look for, nothing to be done
                        }
                    } else { // not what I look for, nothing to be done
                    }
                }
            } else {
                logger.error("null marketId or runnerId in reportStreamChange for: {} {} {} {}", marketId, runnerId, Generic.objectToString(orderMarketRunner), Generic.objectToString(orderRunnerChange));
            }
        } else { // no temporary orders, nothing to check
        }
    }

    public synchronized boolean hasTemporaryOrders() {
        return !temporaryOrders.isEmpty();
    }

    public synchronized void checkTemporaryOrdersExposure(final String marketId, final RunnerId runnerId, final OrderMarketRunner orderMarketRunner) {
//        orderMarketRunner.resetTemporaryAmounts();
        double tempBackExposure = 0d, tempLayExposure = 0d, tempBackProfit = 0d, tempLayProfit = 0d, tempBackCancel = 0d, tempLayCancel = 0d;
        for (TemporaryOrder temporaryOrder : this.temporaryOrders) {
            if (temporaryOrder.runnerEquals(marketId, runnerId)) {
                final TemporaryOrderType orderType = temporaryOrder.getType();
                final Side side = temporaryOrder.getSide();
                final double price = temporaryOrder.getPrice();
                final double size = temporaryOrder.getSize();

                if (orderType.equals(TemporaryOrderType.PLACE)) {
                    if (side.equals(Side.B)) {
                        tempBackExposure += size;
                        tempBackProfit += Formulas.layExposure(price, size);
                    } else if (side.equals(Side.L)) {
                        tempLayExposure += Formulas.layExposure(price, size);
                        tempLayProfit += size;
                    } else {
                        logger.error("unknown Side in checkTemporaryOrdersExposure place for: {} {}", side, price);
                    }
                } else if (orderType.equals(TemporaryOrderType.CANCEL)) {
                    Double sizeReduction = temporaryOrder.getSizeReduction();
                    if (sizeReduction == null) {
                        sizeReduction = size;
                    } else { // keeps non null value, nothing to do
                    }
                    if (side.equals(Side.B)) {
                        tempBackCancel += sizeReduction;
                    } else if (side.equals(Side.L)) {
                        tempLayCancel += Formulas.layExposure(price, sizeReduction);
                    } else {
                        logger.error("unknown Side in checkTemporaryOrdersExposure cancel for: {} {}", side, price);
                    }
                } else {
                    logger.error("unknown TemporaryOrderType in checkTemporaryOrdersExposure: {}", orderType);
                }
            } else { // temporaryOrder not interesting, nothing to be done
            }
        } // end for
        orderMarketRunner.setTempBackExposure(tempBackExposure);
        orderMarketRunner.setTempLayExposure(tempLayExposure);
        orderMarketRunner.setTempBackProfit(tempBackProfit);
        orderMarketRunner.setTempLayProfit(tempLayProfit);
        orderMarketRunner.setTempBackCancel(tempBackCancel);
        orderMarketRunner.setTempLayCancel(tempLayCancel);
    }

    public synchronized double addPlaceOrder(final String marketId, final RunnerId runnerId, final Side side, final double price, final double size) {
        // amounts, in general, will have 2 decimals rounded down (Generic.roundDoubleAmount)
        final double sizePlaced;
        if (marketId != null && runnerId != null && side != null && price > 0d && size > 0d && Formulas.oddsAreUsable(price)) {
            final TemporaryOrder temporaryOrder = new TemporaryOrder(marketId, runnerId, side, price, size);
            if (temporaryOrders.contains(temporaryOrder)) {
                logger.error("will not post duplicate placeOrder: {}", Generic.objectToString(temporaryOrder));
                sizePlaced = 0d;
            } else {
                final double sizeToPlace = Generic.roundDoubleAmount(size);

                if (sizeToPlace >= .01d) {
                    if (sizeToPlace >= 2d || Formulas.exposure(side, price, size) >= 2d) {
                        temporaryOrders.add(temporaryOrder);
                        newOrderAdded.set(true);

                        final LimitOrder limitOrder = new LimitOrder();
                        limitOrder.setPersistenceType(PersistenceType.LAPSE);
                        limitOrder.setPrice(price);
                        limitOrder.setSize(sizeToPlace);

                        final PlaceInstruction placeInstruction = new PlaceInstruction();
                        placeInstruction.setHandicap(runnerId.getHandicap());
                        placeInstruction.setOrderType(OrderType.LIMIT);
                        placeInstruction.setSelectionId(runnerId.getSelectionId());
                        placeInstruction.setSide(side.toStandardSide());
                        placeInstruction.setLimitOrder(limitOrder);

                        final List<PlaceInstruction> placeInstructionsList = new ArrayList<>(1);
                        placeInstructionsList.add(placeInstruction);

                        Statics.threadPoolExecutorImportant.execute(new PlaceOrdersThread(marketId, placeInstructionsList, temporaryOrder));
                        sizePlaced = sizeToPlace;
                    } else { // size too small to place order
                        sizePlaced = 0d;
                    }
                } else if (sizeToPlace == 0d) {
                    sizePlaced = 0d;
                } else {
                    logger.error("bogus too small but not zero value {} for sizeToPlace in addPlaceOrder for: {} {} {} {} {}", sizeToPlace, marketId, Generic.objectToString(runnerId), side, price, size);
                    sizePlaced = 0d;
                }
            }
        } else {
            logger.error("bogus arguments in addPlaceOrder: {} {} {} {} {}", marketId, runnerId, side, price, size);
            sizePlaced = 0d;
        }
        return sizePlaced;
    }

    public synchronized boolean addCancelOrder(final String marketId, final RunnerId runnerId, final Side side, final double price, final double size, final String betId, final Double sizeReduction) {
        // runnerId, side, price, size are needed to identify the order in the stream, or not, but they might have some use; size in this case is sizeRemaining
        final boolean success;
        if (marketId != null && betId != null && (sizeReduction == null || sizeReduction > 0d)) {
            final TemporaryOrder temporaryOrder = new TemporaryOrder(marketId, runnerId, side, price, size, betId, sizeReduction);
            if (temporaryOrders.contains(temporaryOrder)) {
                logger.error("will not post duplicate cancelOrder: {}", Generic.objectToString(temporaryOrder));
                success = false;
            } else {
                temporaryOrders.add(temporaryOrder);
                newOrderAdded.set(true);

                final CancelInstruction cancelInstruction = new CancelInstruction();
                cancelInstruction.setBetId(betId);
                cancelInstruction.setSizeReduction(sizeReduction);

                final List<CancelInstruction> cancelInstructionsList = new ArrayList<>(1);
                cancelInstructionsList.add(cancelInstruction);

                Statics.threadPoolExecutorImportant.execute(new CancelOrdersThread(marketId, cancelInstructionsList, temporaryOrder));
                success = true;
            }
        } else {
            logger.error("bogus arguments in addCancelOrder: {} {} {}", marketId, betId, sizeReduction);
            success = false;
        }
        return success;
    }

    public synchronized void addCancelAllOrder(final long delay) {
        logger.error("add cancelAllOrders command should never be used");
        final long currentTime = System.currentTimeMillis();
        final long primitiveLastCancelOrder = timeLastCancelAllOrder.get();
        final long expectedExecutionTime = currentTime + delay;
        final long minimumExecutionTime = primitiveLastCancelOrder + minimumCancelAllOrderSpacing;

        cancelAllOrdersRunTimesSet.add(Math.max(expectedExecutionTime, minimumExecutionTime));
        newCancelAllOrderAdded.set(true);
    }

    private synchronized long checkForExpiredOrders() {
        long timeToSleep = Generic.HOUR_LENGTH_MILLISECONDS; // initialized

        if (!temporaryOrders.isEmpty()) {
            final long currentTime = System.currentTimeMillis();
            final Iterator<TemporaryOrder> iterator = temporaryOrders.iterator();
            while (iterator.hasNext()) {
                final TemporaryOrder temporaryOrder = iterator.next();
                final long expirationTime = temporaryOrder.getExpirationTime();
                final long timeTillExpired = expirationTime - currentTime;

                if (timeTillExpired <= 0) {
                    logger.error("removing expired by {} ms temporaryOrder: {}", timeTillExpired, Generic.objectToString(temporaryOrder));
                    iterator.remove();
                } else {
                    timeToSleep = Math.min(timeToSleep, timeTillExpired);

                    if (temporaryOrder.isTooOld(currentTime)) {
                        Generic.alreadyPrintedMap.logOnce(5L * Generic.MINUTE_LENGTH_MILLISECONDS, logger, LogLevel.ERROR, "temporaryOrder too old: {} ms for: {}",
                                                          Generic.addCommas(currentTime - temporaryOrder.getCreationTime()), Generic.objectToString(temporaryOrder));
                    } else { // not too old, nothing to do
                    }
                }
            } // end while
        } else { // no orders, timeToSleep is already initialized, nothing to do
        }

        newOrderAdded.set(false);
        return timeToSleep;
    }

    private synchronized long checkCancelAllOrdersList() {
        long timeToSleep;
        if (!cancelAllOrdersRunTimesSet.isEmpty()) {
            logger.error("cancelAllOrders being used");
            long firstRunTime = 0L;
            for (long runTime : cancelAllOrdersRunTimesSet) {
                firstRunTime = firstRunTime == 0L ? runTime : Math.min(firstRunTime, runTime);
            }

            final long primitiveLastCancelOrder = timeLastCancelAllOrder.get();
            final long minimumExecutionTime = primitiveLastCancelOrder + minimumCancelAllOrderSpacing;
            final long currentTime = System.currentTimeMillis();
            final long nextRunTime = Math.max(Math.max(firstRunTime, minimumExecutionTime), currentTime);
            final long timeTillNextRun = nextRunTime - currentTime;
            if (timeTillNextRun <= 0L) {
                timeToSleep = minimumCancelAllOrderSpacing;

                timeLastCancelAllOrder.set(nextRunTime);

                final Iterator<Long> iterator = cancelAllOrdersRunTimesSet.iterator();
                while (iterator.hasNext()) {
                    final long value = iterator.next();
                    if (value <= nextRunTime) {
                        iterator.remove();
                    }
                } // end while

                Statics.threadPoolExecutor.execute(new RescriptOpThread<>()); // cancel orders thread
            } else {
                timeToSleep = timeTillNextRun;
            }
        } else { // no orders
            if (newCancelAllOrderAdded.get()) {
                logger.error("newCancelAllOrderAdded true and no orders present in checkCancelAllOrdersList");
            }
            timeToSleep = Generic.HOUR_LENGTH_MILLISECONDS;
        }

        newCancelAllOrderAdded.set(false);
        return timeToSleep;
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "cancel orders thread");
                }
                GetLiveMarketsThread.waitForSessionToken("OrdersThread main");

                long timeToSleep = checkCancelAllOrdersList();
                timeToSleep = Math.min(timeToSleep, checkForExpiredOrders());

                Generic.threadSleepSegmented(timeToSleep, 100L, newCancelAllOrderAdded, newOrderAdded, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside OrdersThread loop", throwable);
            }
        }

        logger.info("OrdersThread ends");
    }
}
