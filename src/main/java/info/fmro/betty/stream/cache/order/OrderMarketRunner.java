package info.fmro.betty.stream.cache.order;

import info.fmro.betty.objects.Exposure;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.objects.TwoDoubles;
import info.fmro.betty.stream.cache.util.PriceSizeLadder;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.definitions.Order;
import info.fmro.betty.stream.definitions.OrderRunnerChange;
import info.fmro.betty.stream.definitions.Side;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OrderMarketRunner
        implements Serializable { // amounts are in account currency (EUR)
    private static final long serialVersionUID = 6359709424181107081L;
    private static final Logger logger = LoggerFactory.getLogger(OrderMarketRunner.class);
    //    private final OrderMarket orderMarket;
    private final String marketId;
    private final RunnerId runnerId;
    private final PriceSizeLadder layMatches = PriceSizeLadder.newLay();
    private final PriceSizeLadder backMatches = PriceSizeLadder.newBack();
    private final Map<String, Order> unmatchedOrders = new ConcurrentHashMap<>(); // only place where orders are permanently stored
    private double matchedBackExposure, matchedLayExposure, unmatchedBackExposure, unmatchedBackProfit, unmatchedLayExposure, unmatchedLayProfit, tempBackExposure, tempBackProfit, tempLayExposure, tempLayProfit, tempBackCancel, tempLayCancel;

    public OrderMarketRunner(final String marketId, final RunnerId runnerId) {
        if (marketId == null || runnerId == null) {
            logger.error("bad arguments when creating OrderMarketRunner: {} {}", marketId, Generic.objectToString(runnerId));
        } else { // no error message, constructor continues normally
        }
        this.marketId = marketId;
        this.runnerId = runnerId;
    }

    public synchronized void onOrderRunnerChange(final OrderRunnerChange orderRunnerChange) {
        Statics.ordersThread.reportStreamChange(this, orderRunnerChange); // needs to happen at the start of the method, before I modify this object

        final boolean isImage = Boolean.TRUE.equals(orderRunnerChange.getFullImage());

        if (isImage) {
            this.unmatchedOrders.clear();
        }

        if (orderRunnerChange.getUo() != null) {
            for (final Order order : orderRunnerChange.getUo()) {
                this.unmatchedOrders.put(order.getId(), order);
            }
        }
        this.layMatches.onPriceChange(isImage, orderRunnerChange.getMl());
        this.backMatches.onPriceChange(isImage, orderRunnerChange.getMb());
    }

    public synchronized Order getUnmatchedOrder(final String betId) {
        @Nullable final Order returnValue;

        if (betId != null) {
            if (this.unmatchedOrders != null) {
                returnValue = this.unmatchedOrders.get(betId);
            } else {
                returnValue = null;
            }
        } else {
            logger.error("null betId in getUnmatchedOrder");
            returnValue = null;
        }
        return returnValue;
    }

    public synchronized double getMatchedSize(final Side side, final double price) {
        final double matchedSize;
        if (side == Side.B) {
            matchedSize = this.backMatches.getMatchedSize(price);
        } else if (side == Side.L) {
            matchedSize = this.layMatches.getMatchedSize(price);
        } else {
            logger.error("unknown Side in getMatchedSize for: {} {}", side, price);
            matchedSize = 0d;
        }
        return matchedSize;
    }

    public synchronized RunnerId getRunnerId() {
        return this.runnerId;
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    public synchronized PriceSizeLadder getBackMatches() {
        return this.backMatches.copy();
    }

    public synchronized PriceSizeLadder getLayMatches() {
        return this.layMatches.copy();
    }

    public synchronized HashMap<String, Order> getUnmatchedOrders() {
        return new HashMap<>(this.unmatchedOrders);
    }

    public synchronized double getMatchedBackExposure() {
        return this.matchedBackExposure;
    }

    public synchronized double getMatchedLayExposure() {
        return this.matchedLayExposure;
    }

    public synchronized double getUnmatchedBackExposure() {
        return this.unmatchedBackExposure;
    }

    public synchronized double getUnmatchedBackProfit() {
        return this.unmatchedBackProfit;
    }

    public synchronized double getUnmatchedLayExposure() {
        return this.unmatchedLayExposure;
    }

    public synchronized double getUnmatchedLayProfit() {
        return this.unmatchedLayProfit;
    }

    public synchronized double getTempBackExposure() {
        return this.tempBackExposure;
    }

    public synchronized double getTempBackProfit() {
        return this.tempBackProfit;
    }

    public synchronized double getTempLayExposure() {
        return this.tempLayExposure;
    }

    public synchronized double getTempLayProfit() {
        return this.tempLayProfit;
    }

    public synchronized double getTempBackCancel() {
        return this.tempBackCancel;
    }

    public synchronized double getTempLayCancel() {
        return this.tempLayCancel;
    }

//    public synchronized Exposure getExposure() {
//        final TwoDoubles matchedExposure = getMatchedExposure();
//        final double matchedBackExposure = matchedExposure.getFirstDouble();
//        final double matchedLayExposure = matchedExposure.getSecondDouble();
//
//        final TwoDoubles unmatchedExposure = getUnmatchedExposureAndProfit();
//        final double unmatchedBackExposure = unmatchedExposure.getFirstDouble();
//        final double unmatchedLayExposure = unmatchedExposure.getSecondDouble();
//
//        final Exposure result = new Exposure(matchedBackExposure, matchedLayExposure, matchedBackExposure + unmatchedBackExposure, matchedLayExposure + unmatchedLayExposure);
//
//        return result;
//    }

    public synchronized void getExposure(final Exposure exposure) {
        if (exposure != null) {
            this.getMatchedExposure(); // updates matchedBackExposure and matchedLayExposure
            this.getUnmatchedExposureAndProfit(); // updates unmatchedBackExposure/Profit and unmatchedLayExposure/Profit
            Statics.ordersThread.checkTemporaryOrdersExposure(this.marketId, this.runnerId, this);

            exposure.setBackMatchedExposure(this.matchedBackExposure);
            exposure.setLayMatchedExposure(this.matchedLayExposure);
            exposure.setBackTotalExposure(this.matchedBackExposure + this.unmatchedBackExposure + this.tempBackExposure);
            exposure.setLayTotalExposure(this.matchedLayExposure + this.unmatchedLayExposure + this.tempLayExposure);
            exposure.setBackUnmatchedProfit(this.unmatchedBackProfit + this.tempBackProfit);
            exposure.setLayUnmatchedProfit(this.unmatchedLayProfit + this.tempLayProfit);
            exposure.setTempBackCancel(this.tempBackCancel);
            exposure.setTempLayCancel(this.tempLayCancel);
            exposure.timeStamp();
        } else {
            logger.error("null exposure in getExposure for: {}", Generic.objectToString(this));
        }
    }

//    public synchronized TwoDoubles getTotalExposurePair() {
//        final TwoDoubles matchedExposure = getMatchedExposure();
//        final double matchedBackExposure = matchedExposure.getFirstDouble();
//        final double matchedLayExposure = matchedExposure.getSecondDouble();
//
//        final TwoDoubles unmatchedExposure = getUnmatchedExposureAndProfit();
//        final double unmatchedBackExposure = unmatchedExposure.getFirstDouble();
//        final double unmatchedLayExposure = unmatchedExposure.getSecondDouble();
//
//        return new TwoDoubles(matchedBackExposure + unmatchedBackExposure, matchedLayExposure + unmatchedLayExposure);
//    }

    private synchronized void resetUnmatchedExposureAndProfit() {
        this.unmatchedBackExposure = 0d;
        this.unmatchedLayExposure = 0d;
        this.unmatchedBackProfit = 0d;
        this.unmatchedLayProfit = 0d;
    }

//    public synchronized void resetTemporaryAmounts() {
//        this.tempBackExposure = 0d;
//        this.tempLayExposure = 0d;
//        this.tempBackProfit = 0d;
//        this.tempLayProfit = 0d;
//        this.tempBackCancel = 0d;
//        this.tempLayCancel = 0d;
//    }

    private synchronized void getUnmatchedExposureAndProfit() {
        resetUnmatchedExposureAndProfit();
        for (final Order order : this.unmatchedOrders.values()) {
            order.calculateExposureAndProfit();
            this.unmatchedBackExposure += order.getBackExposure();
            this.unmatchedLayExposure += order.getLayExposure();
            this.unmatchedBackProfit += order.getBackProfit();
            this.unmatchedLayProfit += order.getLayProfit();
        }
    }

    private synchronized void getMatchedExposure() {
        final TwoDoubles backProfitExposurePair = this.backMatches.getBackProfitExposurePair();
        final double backProfit = backProfitExposurePair.getFirstDouble();
        final double backExposure = backProfitExposurePair.getSecondDouble();
        final TwoDoubles layProfitExposurePair = this.layMatches.getBackProfitExposurePair(); // same method, just reverse the result pair
        final double layExposure = layProfitExposurePair.getFirstDouble();
        final double layProfit = layProfitExposurePair.getSecondDouble();

        this.matchedBackExposure = backExposure - layProfit;
        this.matchedLayExposure = layExposure - backProfit;
    }

    public synchronized int cancelUnmatchedAmounts(final double backExcessExposure, final double layExcessExposure) {
        // best odds are removed first, as they're most unlikely to get matched
        // always the newer orders are removed first
        int exposureHasBeenModified = 0;
        if (backExcessExposure >= .1d) {
            final ArrayList<Order> backOrdersSorted =
                    this.unmatchedOrders.
                                                values().
                                                stream().
                                                filter(p -> p.getSide() == Side.B).
                                                sorted(Comparator.comparing(Order::getP, Comparator.reverseOrder()).
                                                        thenComparing(Order::getPd, Comparator.reverseOrder())).
                                                collect(Collectors.toCollection(ArrayList::new)); // order by reversed price, then reversed placed date
            double excessExposureLeft = backExcessExposure;
            for (final Order order : backOrdersSorted) {
                excessExposureLeft = order.removeBackExposure(this.marketId, this.runnerId, excessExposureLeft);
                exposureHasBeenModified++;
                if (excessExposureLeft <= 0d) {
                    break;
                } else { // some exposure still left, will continue the loop
                }
            }
        } else { // no excess exposure present, nothing to do
        }

        if (layExcessExposure >= .1d) {
            final ArrayList<Order> layOrdersSorted =
                    this.unmatchedOrders.
                                                values().
                                                stream().
                                                filter(p -> p.getSide() == Side.L).
                                                sorted(Comparator.comparing(Order::getP).
                                                        thenComparing(Order::getPd, Comparator.reverseOrder())).
                                                collect(Collectors.toCollection(ArrayList::new)); // order by price, then reversed placed date
            double excessExposureLeft = layExcessExposure;
            for (final Order order : layOrdersSorted) {
                excessExposureLeft = order.removeLayExposure(this.marketId, this.runnerId, excessExposureLeft);
                exposureHasBeenModified++;
                if (excessExposureLeft <= 0d) {
                    break;
                } else { // some exposure still left, will continue the loop
                }
            }
        } else { // no excess exposure present, nothing to do
        }

        return exposureHasBeenModified;
    }

    public synchronized int balanceTotalAmounts(final double backLimit, final double layLimit, final double toBeUsedBackOdds, final double toBeUsedLayOdds, final double backExcessExposure, final double layExcessExposure) {
        int exposureHasBeenModified = 0;
        final double backUnmatchedExposureToBeCanceled = Math.min(backExcessExposure, this.unmatchedBackExposure), layUnmatchedExposureToBeCanceled = Math.min(layExcessExposure, this.unmatchedLayExposure);
        exposureHasBeenModified += cancelUnmatchedAmounts(backUnmatchedExposureToBeCanceled, layUnmatchedExposureToBeCanceled);
        exposureHasBeenModified += balanceMatchedAmounts(backLimit, layLimit, toBeUsedBackOdds, toBeUsedLayOdds, backExcessExposure - backUnmatchedExposureToBeCanceled, layExcessExposure - layUnmatchedExposureToBeCanceled);

        return exposureHasBeenModified;
    }

    public synchronized int balanceMatchedAmounts(final double backLimit, final double layLimit, final double toBeUsedBackOdds, final double toBeUsedLayOdds, final double backExcessExposure, final double layExcessExposure) {
        int exposureHasBeenModified = 0;
        if (backExcessExposure >= .1d) { // I need to place a lay bet, with profit that would cancel the excess
            // backExcessExposure - newLayProfit = newLayExposure
            // backExcessExposure - sizeToPlace = sizeToPlace*(toBeUsedLayOdds-1d)
            // backExcessExposure = sizeToPlace * toBeUsedLayOdds
            if (placeOrder(backLimit, layLimit, Side.L, toBeUsedLayOdds, backExcessExposure / toBeUsedLayOdds) > 0d) {
                exposureHasBeenModified++;
            } else { // no modification, nothing to be done
            }
        } else { // no excess exposure present, nothing to do
        }

        if (layExcessExposure >= .1d) { // I need to place a back bet, with profit that would cancel the excess
            // layExcessExposure - newBackProfit = newBackExposure
            // layExcessExposure - sizeToPlace*(toBeUsedBackOdds-1d) = sizeToPlace
            // layExcessExposure = sizeToPlace * toBeUsedBackOdds
            if (placeOrder(backLimit, layLimit, Side.B, toBeUsedBackOdds, layExcessExposure / toBeUsedBackOdds) > 0d) {
                exposureHasBeenModified++;
            } else { // no modification, nothing to be done
            }
        } else { // no excess exposure present, nothing to do
        }
        return exposureHasBeenModified;
    }

    public synchronized double placeOrder(final double backLimit, final double layLimit, final Side side, final double price, final double size) {
        // exposure.setBackTotalExposure(matchedBackExposure + unmatchedBackExposure + tempBackExposure);
        // exposure.setLayTotalExposure(matchedLayExposure + unmatchedLayExposure + tempLayExposure);
        final double sizePlaced;

        if (side == Side.B) {
            final double backTotalExposure = this.matchedBackExposure + this.unmatchedBackExposure + this.tempBackExposure;
            final double availableBackExposure = Math.max(0d, backLimit - backTotalExposure);
            sizePlaced = Math.min(availableBackExposure, size);
        } else if (side == Side.L) {
            final double layTotalExposure = this.matchedLayExposure + this.unmatchedLayExposure + this.tempLayExposure;
            final double availableLayExposure = Math.max(0d, layLimit - layTotalExposure);
            sizePlaced = Math.min(availableLayExposure / (price - 1d), size);
        } else {
            logger.error("unknown side {} {} {} during placeOrder for: {}", side, price, size, Generic.objectToString(this));
            sizePlaced = 0d;
        }

        return Statics.ordersThread.addPlaceOrder(this.marketId, this.runnerId, side, price, sizePlaced);
    }

    public synchronized double cancelUnmatchedExceptExcessOnTheOtherSide(final Side side, final double excessOnTheOtherSide) {
        // worst odds are kept, as they're most likely to get matched
        // older orders are kept
        double excessOnTheOtherSideRemaining = excessOnTheOtherSide;
        if (side == null) {
            logger.error("null side in cancelUnmatchedExceptExcessOnTheOtherSide for: {} {}", excessOnTheOtherSide, Generic.objectToString(this));
        } else if (side == Side.B) {
            final ArrayList<Order> backOrdersSorted =
                    this.unmatchedOrders.
                                                values().
                                                stream().
                                                filter(p -> p.getSide() == Side.B).
                                                sorted(Comparator.comparing(Order::getP).
                                                        thenComparing(Order::getPd)).
                                                collect(Collectors.toCollection(ArrayList::new)); // order by price, then placed date
            for (final Order order : backOrdersSorted) {
                final double price = order.getP();
                final double sizeRemaining = order.getSr();
                final double excessPresentInOrder = price * sizeRemaining;
                if (excessPresentInOrder <= 0d) {
                    logger.error("order with zero or negative amount left in cancelUnmatchedExceptExcessOnTheOtherSide for: {} {} {} {} {} {}", price, sizeRemaining, side, excessOnTheOtherSide, Generic.objectToString(order), Generic.objectToString(this));
                } else if (excessOnTheOtherSideRemaining < .1d) { // no more excess left, everything from now on will be canceled
                    order.cancelOrder(this.marketId, this.runnerId);
                } else if (excessOnTheOtherSideRemaining >= excessPresentInOrder) { // keeping this order, and reducing the excessOnTheOtherSideRemaining
                    excessOnTheOtherSideRemaining -= excessPresentInOrder;
                } else { // excessOnTheOtherSideRemaining < excessPresentInOrder; partial cancelOrder
                    if (order.cancelOrder(this.marketId, this.runnerId, excessOnTheOtherSideRemaining / price)) {
                        excessOnTheOtherSideRemaining = 0d;
                    } else { // cancelOrder failed for some reason, nothing to be done
                    }
                }
            } // end for
        } else if (side == Side.L) {
            final ArrayList<Order> layOrdersSorted =
                    this.unmatchedOrders.
                                                values().
                                                stream().
                                                filter(p -> p.getSide() == Side.L).
                                                sorted(Comparator.comparing(Order::getP, Comparator.reverseOrder()).
                                                        thenComparing(Order::getPd)).
                                                collect(Collectors.toCollection(ArrayList::new)); // order by reversed price, then placed date
            for (final Order order : layOrdersSorted) {
                final double price = order.getP();
                final double sizeRemaining = order.getSr();
                final double excessPresentInOrder = price * sizeRemaining;
                if (excessPresentInOrder <= 0d) {
                    logger.error("order with zero or negative amount left in cancelUnmatchedExceptExcessOnTheOtherSide for: {} {} {} {} {} {}", price, sizeRemaining, side, excessOnTheOtherSide, Generic.objectToString(order), Generic.objectToString(this));
                } else if (excessOnTheOtherSideRemaining < .1d) { // no more excess left, everything from now on will be canceled
                    order.cancelOrder(this.marketId, this.runnerId);
                } else if (excessOnTheOtherSideRemaining >= excessPresentInOrder) { // keeping this order, and reducing the excessOnTheOtherSideRemaining
                    excessOnTheOtherSideRemaining -= excessPresentInOrder;
                } else { // excessOnTheOtherSideRemaining < excessPresentInOrder; partial cancelOrder
                    if (order.cancelOrder(this.marketId, this.runnerId, excessOnTheOtherSideRemaining / price)) {
                        excessOnTheOtherSideRemaining = 0d;
                    } else { // cancelOrder failed for some reason, nothing to be done
                    }
                }
            } // end for
        } else {
            logger.error("unknown side in cancelUnmatchedExceptExcessOnTheOtherSide for: {} {} {}", side, excessOnTheOtherSide, Generic.objectToString(this));
        }
        return excessOnTheOtherSideRemaining;
    }

    public synchronized int cancelUnmatched() { // cancel all unmatched orders
        return cancelUnmatched(null, 0d);
    }

    public synchronized int cancelUnmatched(final Side sideToCancel) { // cancel all unmatched orders on that side
        return cancelUnmatched(sideToCancel, 0d);
    }

    public synchronized int cancelUnmatched(final Side sideToCancel, final double worstNotCanceledOdds) {
        int modifications = 0;
        if (this.runnerId == null) {
            logger.error("null runnerId in orderMarketRunner.cancelAllUnmatched: {}", Generic.objectToString(this));
        } else {
            for (final Order order : this.unmatchedOrders.values()) {
                if (order == null) {
                    logger.error("null order in cancelAllUnmatched for: {}", Generic.objectToString(this));
                } else {
                    final Side side = order.getSide();
                    final Double price = order.getP();
                    final Double size = order.getSr();
                    final String betId = order.getId();
                    if (side == null || price == null || size == null || betId == null) {
                        logger.error("null order attributes in cancelAllUnmatched for: {} {} {} {} {}", side, price, size, betId, Generic.objectToString(order));
                    } else {
                        final boolean shouldCancelOrder;
                        if (sideToCancel == null) { // cancel all orders
                            shouldCancelOrder = true;
                        } else if (sideToCancel != side) { // not the right side
                            shouldCancelOrder = false;
                        } else if (worstNotCanceledOdds == 0d) { // right side, and odds don't matter
                            shouldCancelOrder = true;
                        } else if (Formulas.oddsAreWorse(side, worstNotCanceledOdds, price)) { // odds matter and are worse
                            shouldCancelOrder = true;
                        } else { // odds matter but are not worse
                            shouldCancelOrder = false;
                        }

                        if (shouldCancelOrder) {
                            modifications += Generic.booleanToInt(order.cancelOrder(this.marketId, this.runnerId));
                        } else { // won't cancel, nothing to be done
                        }
                    }
                }
            } // end for
        }
        return modifications;
    }

    public synchronized int cancelUnmatchedTooGoodOdds(@NotNull final Side sideToCancel, final double worstOddsThatAreGettingCanceled) {
        int modifications = 0;
        if (this.runnerId == null || worstOddsThatAreGettingCanceled <= 0d) {
            logger.error("null runnerId or bogus worstOddsThatAreGettingCanceled in orderMarketRunner.cancelUnmatchedTooGoodOdds: {} {} {}", sideToCancel, worstOddsThatAreGettingCanceled, Generic.objectToString(this));
        } else {
            for (final Order order : this.unmatchedOrders.values()) {
                if (order == null) {
                    logger.error("null order in cancelUnmatchedTooGoodOdds for: {}", Generic.objectToString(this));
                } else {
                    final Side side = order.getSide();
                    final Double price = order.getP();
                    final Double size = order.getSr();
                    final String betId = order.getId();
                    if (side == null || price == null || size == null || betId == null) {
                        logger.error("null order attributes in cancelUnmatchedTooGoodOdds for: {} {} {} {} {}", side, price, size, betId, Generic.objectToString(order));
                    } else {
                        final boolean shouldCancelOrder;
                        if (sideToCancel != side) { // not the right side
                            shouldCancelOrder = false;
                        } else if (!Formulas.oddsAreWorse(side, worstOddsThatAreGettingCanceled, price)) { // odds matter and are same or better
                            shouldCancelOrder = true;
                        } else { // odds matter but are not worse
                            shouldCancelOrder = false;
                        }

                        if (shouldCancelOrder) {
                            modifications += Generic.booleanToInt(order.cancelOrder(this.marketId, this.runnerId));
                        } else { // won't cancel, nothing to be done
                        }
                    }
                }
            } // end for
        }
        return modifications;
    }

    public synchronized TreeMap<Double, Double> getUnmatchedBackAmounts() {
        final TreeMap<Double, Double> result = new TreeMap<>(Comparator.naturalOrder());
        for (final Order order : this.unmatchedOrders.values()) {
            if (order == null) {
                logger.error("null order in getUnmatchedBackAmounts for: {}", Generic.objectToString(this));
            } else {
                final Side side = order.getSide();
                if (side == null) {
                    logger.error("null side in getUnmatchedBackAmounts for: {} {}", Generic.objectToString(order), Generic.objectToString(this));
                } else if (side == Side.B) {
                    final Double price = order.getP(), remainingSize = order.getSr();
                    if (price == null || remainingSize == null) {
                        logger.error("null price or remainingSize in getUnmatchedBackAmounts for: {} {}", Generic.objectToString(order), Generic.objectToString(this));
                    } else {
                        final Double existingMapValue = result.get(price);
                        final double existingMapValuePrimitive = existingMapValue == null ? 0d : existingMapValue;
                        result.put(price, existingMapValuePrimitive + remainingSize);
                    }
                } else { // wrong side, nothing to be done
                }
            }
        }
        return result;
    }

    public synchronized TreeMap<Double, Double> getUnmatchedLayAmounts() {
        final TreeMap<Double, Double> result = new TreeMap<>(Comparator.reverseOrder());
        for (final Order order : this.unmatchedOrders.values()) {
            if (order == null) {
                logger.error("null order in getUnmatchedLayAmounts for: {}", Generic.objectToString(this));
            } else {
                final Side side = order.getSide();
                if (side == null) {
                    logger.error("null side in getUnmatchedLayAmounts for: {} {}", Generic.objectToString(order), Generic.objectToString(this));
                } else if (side == Side.L) {
                    final Double price = order.getP(), remainingSize = order.getSr();
                    if (price == null || remainingSize == null) {
                        logger.error("null price or remainingSize in getUnmatchedLayAmounts for: {} {}", Generic.objectToString(order), Generic.objectToString(this));
                    } else {
                        final Double existingMapValue = result.get(price);
                        final double existingMapValuePrimitive = existingMapValue == null ? 0d : existingMapValue;
                        result.put(price, existingMapValuePrimitive + remainingSize);
                    }
                } else { // wrong side, nothing to be done
                }
            }
        }
        return result;
    }

    public synchronized void setTempBackExposure(final double tempBackExposure) {
        this.tempBackExposure = tempBackExposure;
    }

    public synchronized void setTempBackProfit(final double tempBackProfit) {
        this.tempBackProfit = tempBackProfit;
    }

    public synchronized void setTempLayExposure(final double tempLayExposure) {
        this.tempLayExposure = tempLayExposure;
    }

    public synchronized void setTempLayProfit(final double tempLayProfit) {
        this.tempLayProfit = tempLayProfit;
    }

    public synchronized void setTempBackCancel(final double tempBackCancel) {
        this.tempBackCancel = tempBackCancel;
    }

    public synchronized void setTempLayCancel(final double tempLayCancel) {
        this.tempLayCancel = tempLayCancel;
    }
}
