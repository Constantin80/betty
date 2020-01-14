package info.fmro.betty.logic;

import info.fmro.shared.enums.RulesManagerModificationCommand;
import info.fmro.shared.stream.objects.SerializableObjectModification;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.cache.market.Market;
import info.fmro.betty.stream.cache.market.MarketRunner;
import info.fmro.betty.stream.cache.order.OrderMarket;
import info.fmro.betty.stream.cache.order.OrderMarketRunner;
import info.fmro.shared.stream.objects.RunnerId;
import info.fmro.betty.stream.cache.util.Utils;
import info.fmro.betty.stream.definitions.Order;
import info.fmro.shared.stream.enums.Side;
import info.fmro.shared.utility.Formulas;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass"})
public class ManagedRunner
        extends Exposure
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ManagedRunner.class);
    private static final long serialVersionUID = 3553997020269888719L;
    private final String marketId;
    private final RunnerId runnerId;
    //    private final long selectionId;
    //    private final double handicap; // The handicap associated with the runner in case of Asian handicap markets (e.g. marketTypes ASIAN_HANDICAP_DOUBLE_LINE, ASIAN_HANDICAP_SINGLE_LINE) null otherwise.
    private double backAmountLimit, layAmountLimit; // amountLimits at 0d by default, which means no betting unless modified; negative limit means no limit
    private double minBackOdds = 1_001d, maxLayOdds = 1.0001d; // defaults are unusable, which means no betting unless modified
    private double toBeUsedBackOdds = 1_001d, toBeUsedLayOdds = 1.0001d;
    private double proportionOfMarketLimitPerRunner, idealBackExposure;
    private transient MarketRunner marketRunner;
    @Nullable
    private transient OrderMarketRunner orderMarketRunner;

    ManagedRunner(final String marketId, final RunnerId runnerId) {
        super();
        this.marketId = marketId;
        this.runnerId = runnerId;
    }

    private synchronized void attachRunner() {
        if (this.marketRunner == null) {
            final Market market = Utils.getMarket(this.marketId);
            attachRunner(market);
        } else { // I already have the marketRunner, nothing to be done
        }
    }

    synchronized void attachRunner(final Market market) {
        if (this.marketRunner == null) {
            if (market == null) {
                logger.error("null market in attachRunner for: {}", Generic.objectToString(this)); // I'll just print the error message; this error shouldn't happen and I don't think it's properly fixable
            } else {
                this.marketRunner = market.getMarketRunner(this.runnerId);
                if (this.marketRunner == null) {
                    logger.error("no marketRunner found in attachRunner for: {} {}", Generic.objectToString(this), Generic.objectToString(market));
                }
            }
        } else { // I already have the marketRunner, nothing to be done
        }
    }

    private synchronized void attachOrderRunner() {
        if (this.orderMarketRunner == null) {
            final OrderMarket orderMarket = Utils.getOrderMarket(this.marketId);
            attachOrderRunner(orderMarket);
        } else { // I already have the orderMarketRunner, nothing to be done
        }
    }

    synchronized void attachOrderRunner(final OrderMarket orderMarket) {
        if (this.orderMarketRunner == null) {
            if (orderMarket == null) {
                logger.error("null orderMarket in attachOrderRunner for: {}", Generic.objectToString(this)); // I'll just print the error message; this error shouldn't happen and I don't think it's properly fixable
            } else {
                this.orderMarketRunner = orderMarket.getOrderMarketRunner(this.runnerId);
                if (this.orderMarketRunner == null) { // normal, it means no orders exist for this managedRunner, nothing else to be done
                }
            }
        } else { // I already have the orderMarketRunner, nothing to be done
        }
    }

    @SuppressWarnings("WeakerAccess")
    public synchronized MarketRunner getMarketRunner() {
        attachRunner();
        return this.marketRunner;
    }

    @SuppressWarnings("WeakerAccess")
    public synchronized OrderMarketRunner getOrderMarketRunner() {
        attachOrderRunner();
        return this.orderMarketRunner;
    }

    synchronized void processOrders() {
        getOrderMarketRunner(); // updates the orderMarketRunner
        if (this.orderMarketRunner == null) {
            logger.error("null orderMarketRunner in ManagedRunner.processOrders for: {}", Generic.objectToString(this));
        } else {
            final RunnerId orderRunnerId = this.orderMarketRunner.getRunnerId();
            final RunnerId localRunnerId = this.runnerId;
            if (localRunnerId.equals(orderRunnerId)) {
//                this.updateExposure(orderMarketRunner.getExposure());
                this.orderMarketRunner.getExposure(this); // updates the exposure into this object
            } else {
                logger.error("not equal runnerIds in ManagedRunner.processOrders for: {} {} {}", Generic.objectToString(orderRunnerId), Generic.objectToString(localRunnerId), Generic.objectToString(this));
            }
        }
    }

    synchronized void resetOrderMarketRunner() {
        this.orderMarketRunner = null;
    }

    private synchronized void resetToBeUsedOdds() { // reset to unusable values
        this.toBeUsedBackOdds = 1_001d;
        this.toBeUsedLayOdds = 1d;
    }

    synchronized int checkRunnerLimits() { // check the back/lay exposure limits for the runner, to make sure there's no error
        int exposureHasBeenModified = 0;
        getOrderMarketRunner(); // updates the orderMarketRunner
        if (this.orderMarketRunner != null) {
            final double backTotalExposure = this.getBackTotalExposure(), layTotalExposure = this.getLayTotalExposure();
            if (this.backAmountLimit + .1d < backTotalExposure || this.layAmountLimit + .1d < layTotalExposure) {
                logger.error("exposure limit has been breached back:{} {} lay:{} {} for runner: {}", this.backAmountLimit, backTotalExposure, this.layAmountLimit, layTotalExposure, Generic.objectToString(this));
                final double backExcessExposure = Math.max(0d, backTotalExposure - this.backAmountLimit), layExcessExposure = Math.max(0d, layTotalExposure - this.layAmountLimit);
                final double backMatchedExposure = this.getBackMatchedExposure(), layMatchedExposure = this.getLayMatchedExposure();
                final double backExcessMatchedExposure = Math.max(0d, backMatchedExposure - this.backAmountLimit), layExcessMatchedExposure = Math.max(0d, layMatchedExposure - this.layAmountLimit);
                exposureHasBeenModified += this.orderMarketRunner.cancelUnmatchedAmounts(backExcessExposure, layExcessExposure);

                if (backExcessMatchedExposure >= .1d || layExcessMatchedExposure >= .1d) {
                    logger.error("matched exposure has breached the limit back:{} {} lay:{} {} for runner: {}", this.backAmountLimit, backMatchedExposure, this.layAmountLimit, layMatchedExposure, Generic.objectToString(this));
                    exposureHasBeenModified += this.orderMarketRunner.balanceMatchedAmounts(this.getBackAmountLimit(), this.getLayAmountLimit(), this.getToBeUsedBackOdds(), this.getToBeUsedLayOdds(), backExcessMatchedExposure, layExcessMatchedExposure);
                } else { // matched amounts don't break the limits, nothing to be done
                }

                if (exposureHasBeenModified > 0) {
                    this.processOrders();
                } else {
                    logger.error("exposureHasBeenModified {} not positive in checkRunnerLimits, while exposure limit is breached", exposureHasBeenModified);
                }
            } else { // no limit breach, nothing to do
            }
        } else {
            logger.error("null orderMarketRunner in checkRunnerLimits: {}", Generic.objectToString(this));
        }
        return exposureHasBeenModified;
    }

    synchronized int removeExposure() { // check the back/lay exposure limits for the runner, to make sure there's no error
        int exposureHasBeenModified = 0;
        getOrderMarketRunner(); // updates the orderMarketRunner
        if (this.orderMarketRunner != null) {
            final double backMatchedExposure = this.getBackMatchedExposure(), layMatchedExposure = this.getLayMatchedExposure();
            final double backMatchedExcessExposure, layMatchedExcessExposure;
            if (backMatchedExposure > layMatchedExposure) {
                backMatchedExcessExposure = backMatchedExposure - layMatchedExposure;
                layMatchedExcessExposure = 0d;
            } else {
                backMatchedExcessExposure = 0d;
                layMatchedExcessExposure = layMatchedExposure - backMatchedExposure;
            }

            if (backMatchedExcessExposure < .1d && layMatchedExcessExposure < .1d) {
                exposureHasBeenModified += this.orderMarketRunner.cancelUnmatched();
            } else if (backMatchedExcessExposure >= .1d) {
                exposureHasBeenModified += this.orderMarketRunner.cancelUnmatched(Side.B);
                // matchedBackExposure, matchedLayExposure, unmatchedBackExposure, unmatchedBackProfit, unmatchedLayExposure, unmatchedLayProfit, tempBackExposure, tempBackProfit, tempLayExposure, tempLayProfit, tempBackCancel, tempLayCancel;
                final double backExcessExposureAfterTempIsConsidered =
                        backMatchedExcessExposure + this.orderMarketRunner.getTempBackExposure() + this.orderMarketRunner.getTempBackProfit() - this.orderMarketRunner.getTempLayExposure() - this.orderMarketRunner.getTempLayProfit();
                if (backExcessExposureAfterTempIsConsidered < .1d) {
                    exposureHasBeenModified += this.orderMarketRunner.cancelUnmatched(Side.L);
                } else {
                    final double excessOnTheOtherSideRemaining = this.orderMarketRunner.cancelUnmatchedExceptExcessOnTheOtherSide(Side.L, backExcessExposureAfterTempIsConsidered);
                    if (excessOnTheOtherSideRemaining < backExcessExposureAfterTempIsConsidered) {
                        exposureHasBeenModified++;
                    } else { // no modification was made
                    }

                    if (excessOnTheOtherSideRemaining >= .1d) {
                        exposureHasBeenModified += this.orderMarketRunner.balanceMatchedAmounts(this.getBackAmountLimit(), this.getLayAmountLimit(), this.getToBeUsedBackOdds(), this.getToBeUsedLayOdds(), excessOnTheOtherSideRemaining, 0d);
                    } else { // problem solved, no more adjustments needed
                    }
                }
            } else if (layMatchedExcessExposure >= .1d) {
                exposureHasBeenModified += this.orderMarketRunner.cancelUnmatched(Side.L);
                // matchedBackExposure, matchedLayExposure, unmatchedBackExposure, unmatchedBackProfit, unmatchedLayExposure, unmatchedLayProfit, tempBackExposure, tempBackProfit, tempLayExposure, tempLayProfit, tempBackCancel, tempLayCancel;
                final double layExcessExposureAfterTempIsConsidered =
                        layMatchedExcessExposure + this.orderMarketRunner.getTempLayExposure() + this.orderMarketRunner.getTempLayProfit() - this.orderMarketRunner.getTempBackExposure() - this.orderMarketRunner.getTempBackProfit();
                if (layExcessExposureAfterTempIsConsidered < .1d) {
                    exposureHasBeenModified += this.orderMarketRunner.cancelUnmatched(Side.B);
                } else {
                    final double excessOnTheOtherSideRemaining = this.orderMarketRunner.cancelUnmatchedExceptExcessOnTheOtherSide(Side.B, layExcessExposureAfterTempIsConsidered);
                    if (excessOnTheOtherSideRemaining < layExcessExposureAfterTempIsConsidered) {
                        exposureHasBeenModified++;
                    } else { // no modification was made
                    }

                    if (excessOnTheOtherSideRemaining >= .1d) {
                        exposureHasBeenModified += this.orderMarketRunner.balanceMatchedAmounts(this.getBackAmountLimit(), this.getLayAmountLimit(), this.getToBeUsedBackOdds(), this.getToBeUsedLayOdds(), 0d, excessOnTheOtherSideRemaining);
                    } else { // problem solved, no more adjustments needed
                    }
                }
            } else {
                logger.error("this branch should not be reached in removeExposure: {} {} {}", backMatchedExcessExposure, layMatchedExcessExposure, Generic.objectToString(this));
            }
        } else {
            logger.error("null orderMarketRunner in checkRunnerLimits: {}", Generic.objectToString(this));
        }
        return exposureHasBeenModified;
    }

    synchronized int calculateOdds(final double marketCalculatedLimit) { // updates toBeUsedBackOdds and toBeUsedLayOdds
        int exposureHasBeenModified = 0;
        resetToBeUsedOdds();
        this.getMarketRunner(); // updates the marketRunner
        this.getOrderMarketRunner(); // updates the orderMarketRunner
        if (this.marketRunner == null) {
            logger.error("trying to calculateOdds with null marketRunner for: {}", Generic.objectToString(this));
        } else {
            final HashMap<String, Order> unmatchedOrders = this.orderMarketRunner == null ? null : this.orderMarketRunner.getUnmatchedOrders();

            final double existingBackOdds = this.marketRunner.getBestAvailableBackPrice(unmatchedOrders, this.getBackAmountLimit(marketCalculatedLimit));
            final double existingLayOdds = this.marketRunner.getBestAvailableLayPrice(unmatchedOrders, this.getLayAmountLimit(marketCalculatedLimit));

            final double layNStepDifferentOdds = existingLayOdds == 0 ? 1_000d : info.fmro.shared.utility.Formulas.getNStepDifferentOdds(existingLayOdds, -1);
            final double backNStepDifferentOdds = existingBackOdds == 0 ? 1.01d : info.fmro.shared.utility.Formulas.getNStepDifferentOdds(existingBackOdds, 1);

            this.toBeUsedBackOdds = Math.max(this.getMinBackOdds(), layNStepDifferentOdds);
            this.toBeUsedLayOdds = Math.min(this.getMaxLayOdds(), backNStepDifferentOdds);

            // I'll allow unusable odds
//            this.toBeUsedBackOdds = Math.min(Math.max(this.toBeUsedBackOdds, 1.01d), 1_000d);
//            this.toBeUsedLayOdds = Math.min(Math.max(this.toBeUsedLayOdds, 1.01d), 1_000d);
        }
        if (this.orderMarketRunner != null) {
            // send order to cancel all back bets at worse odds than to be used ones / send order to cancel all back bets
            exposureHasBeenModified += info.fmro.shared.utility.Formulas.oddsAreUsable(this.toBeUsedBackOdds) ? this.orderMarketRunner.cancelUnmatched(Side.B, this.toBeUsedBackOdds) : this.orderMarketRunner.cancelUnmatched(Side.B);

            // send order to cancel all lay bets at worse odds than to be used ones / send order to cancel all lay bets
            exposureHasBeenModified += info.fmro.shared.utility.Formulas.oddsAreUsable(this.toBeUsedLayOdds) ? this.orderMarketRunner.cancelUnmatched(Side.L, this.toBeUsedLayOdds) : this.orderMarketRunner.cancelUnmatched(Side.L);
        } else {
            logger.error("null orderMarketRunner in calculateOdds: {}", Generic.objectToString(this));
        }

        if (this.marketRunner != null && this.orderMarketRunner != null) {
            exposureHasBeenModified += this.cancelHardToReachOrders();
        } else { // error messages were printed previously, nothing to be done
        }

        return exposureHasBeenModified; // exposure will be recalculated outside the method, based on the return value indicating modifications have been made; but might be useless, as orders are just placed, not yet executed
    }

    private synchronized int cancelHardToReachOrders() {
        // find orders at good odds, but that are useless now as they are unlikely to be reached, and cancel them, updating runner exposure; the algorithm may not be simple
        int modifications = 0;
        this.getMarketRunner(); // updates the marketRunner
        this.getOrderMarketRunner(); // updates the orderMarketRunner

        if (this.orderMarketRunner == null || this.marketRunner == null) {
            logger.error("null orderMarketRunner or marketRunner in cancelHardToReachOrders for: {}", Generic.objectToString(this));
        } else {
            // back
            final TreeMap<Double, Double> unmatchedBackAmounts = this.orderMarketRunner.getUnmatchedBackAmounts(), availableLayAmounts = this.marketRunner.getAvailableToLay();
            Formulas.removeOwnAmountsFromAvailableTreeMap(availableLayAmounts, unmatchedBackAmounts);
            final NavigableSet<Double> unmatchedBackPrices = unmatchedBackAmounts.descendingKeySet();
            double worstOddsThatAreGettingCanceledBack = 0d;
            for (final Double unmatchedPrice : unmatchedBackPrices) {
                if (unmatchedPrice == null) {
                    logger.error("null unmatchedPrice in cancelHardToReachOrders for: {} {}", Generic.objectToString(unmatchedBackAmounts), Generic.objectToString(this));
                } else {
                    final Double unmatchedAmount = unmatchedBackAmounts.get(unmatchedPrice);
                    final double unmatchedAmountPrimitive = unmatchedAmount == null ? 0d : unmatchedAmount;
                    final SortedMap<Double, Double> smallerPriceAvailableAmounts = availableLayAmounts.headMap(unmatchedPrice);
                    double smallerSum = 0d;
                    for (final Double smallerAmount : smallerPriceAvailableAmounts.values()) {
                        final double smallerAmountPrimitive = smallerAmount == null ? 0d : smallerAmount;
                        smallerSum += smallerAmountPrimitive;
                    }

                    // simple condition for deciding that my amount can't be reached
                    if (smallerSum > 2d * unmatchedAmountPrimitive) {
                        worstOddsThatAreGettingCanceledBack = unmatchedPrice; // only the last price will matter, all odds that are better or same will get canceled
                    } else {
                        break; // breaks when 1 amount is not removed, and the next ones won't be removed
                    }
                }
            } // end for
            if (worstOddsThatAreGettingCanceledBack == 0d) { // nothing to be done
            } else {
                modifications += this.orderMarketRunner.cancelUnmatchedTooGoodOdds(Side.B, worstOddsThatAreGettingCanceledBack);
            }

            // lay
            final TreeMap<Double, Double> unmatchedLayAmounts = this.orderMarketRunner.getUnmatchedLayAmounts(), availableBackAmounts = this.marketRunner.getAvailableToBack();
            Formulas.removeOwnAmountsFromAvailableTreeMap(availableBackAmounts, unmatchedLayAmounts);
            final NavigableSet<Double> unmatchedLayPrices = unmatchedLayAmounts.descendingKeySet();
            double worstOddsThatAreGettingCanceledLay = 0d;
            for (final Double unmatchedPrice : unmatchedLayPrices) {
                if (unmatchedPrice == null) {
                    logger.error("null unmatchedPrice in cancelHardToReachOrders for: {} {}", Generic.objectToString(unmatchedLayAmounts), Generic.objectToString(this));
                } else {
                    final Double unmatchedAmount = unmatchedLayAmounts.get(unmatchedPrice);
                    final double unmatchedAmountPrimitive = unmatchedAmount == null ? 0d : unmatchedAmount;
                    final SortedMap<Double, Double> higherPriceAvailableAmounts = availableBackAmounts.tailMap(unmatchedPrice, false);
                    double higherSum = 0d;
                    for (final Double higherAmount : higherPriceAvailableAmounts.values()) {
                        final double higherAmountPrimitive = higherAmount == null ? 0d : higherAmount;
                        higherSum += higherAmountPrimitive;
                    }

                    // simple condition for deciding that my amount can't be reached
                    if (higherSum > 2d * unmatchedAmountPrimitive) {
                        worstOddsThatAreGettingCanceledLay = unmatchedPrice; // only the last price will matter, all odds that are better or same will get canceled
                    } else {
                        break; // breaks when 1 amount is not removed, and the next ones won't be removed
                    }
                }
            } // end for
            if (worstOddsThatAreGettingCanceledLay == 0d) { // nothing to be done
            } else {
                modifications += this.orderMarketRunner.cancelUnmatchedTooGoodOdds(Side.L, worstOddsThatAreGettingCanceledLay);
            }
        }

        return modifications;
    }

    public synchronized double getToBeUsedBackOdds() {
        return this.toBeUsedBackOdds;
    }

    public synchronized double getToBeUsedLayOdds() {
        return this.toBeUsedLayOdds;
    }

    public synchronized double getTotalValue() {
        final double result;
        if (this.getMarketRunner() != null) {
            result = this.marketRunner.getTvEUR();
        } else {
            logger.error("no marketRunner present in getTotalValue for: {}", Generic.objectToString(this));
            result = 0d;
        }
        return result;
    }

    synchronized double getLastTradedPrice() {
        final double result;
        if (this.getMarketRunner() != null) {
            result = this.marketRunner.getLtp();
        } else {
            logger.error("no marketRunner present in getLastTradedPrice for: {}", Generic.objectToString(this));
            result = 0d;
        }
        return result;
    }

    public synchronized RunnerId getRunnerId() {
        return this.runnerId;
    }

    public synchronized Long getSelectionId() {
        return this.runnerId.getSelectionId();
    }

    public synchronized Double getHandicap() {
        return this.runnerId.getHandicap();
    }

    public synchronized double getBackAmountLimit() {
        return this.backAmountLimit;
    }

    @Contract(pure = true)
    private synchronized double getBackAmountLimit(final double marketCalculatedLimit) {
        final double result;
        if (marketCalculatedLimit < 0) {
            result = this.backAmountLimit;
        } else if (this.backAmountLimit < 0) {
            result = marketCalculatedLimit;
        } else {
            result = Math.min(marketCalculatedLimit, this.backAmountLimit);
        }
        return result;
    }

    public synchronized double getLayAmountLimit() {
        return this.layAmountLimit;
    }

    @Contract(pure = true)
    private synchronized double getLayAmountLimit(final double marketCalculatedLimit) {
        final double result;
        if (marketCalculatedLimit < 0) {
            result = this.layAmountLimit;
        } else if (this.layAmountLimit < 0) {
            result = marketCalculatedLimit;
        } else {
            result = Math.min(marketCalculatedLimit, this.layAmountLimit);
        }
        return result;
    }

    synchronized double getMinBackOdds() {
        return this.minBackOdds;
    }

    synchronized double getMaxLayOdds() {
        return this.maxLayOdds;
    }

    synchronized void setProportionOfMarketLimitPerRunner(final double proportionOfMarketLimitPerRunner) {
        if (proportionOfMarketLimitPerRunner >= 0d) {
            this.proportionOfMarketLimitPerRunner = proportionOfMarketLimitPerRunner;
        } else {
            logger.error("trying to set negative proportionOfMarketLimitPerRunner {} for: {} {}", proportionOfMarketLimitPerRunner, this.proportionOfMarketLimitPerRunner, Generic.objectToString(this));
        }
    }

    synchronized double getProportionOfMarketLimitPerRunner() {
        return this.proportionOfMarketLimitPerRunner;
    }

    synchronized double addIdealBackExposure(final double idealBackExposureToBeAdded) {
        return setIdealBackExposure(getIdealBackExposure() + idealBackExposureToBeAdded);
    }

    synchronized double setIdealBackExposure(final double newIdealBackExposure) {
        final double newExposureAssigned;
        if (newIdealBackExposure >= 0d) {
            if (Formulas.oddsAreUsable(this.minBackOdds)) {
                if (newIdealBackExposure >= this.backAmountLimit) {
                    newExposureAssigned = this.backAmountLimit - this.idealBackExposure;
                    this.idealBackExposure = this.backAmountLimit;
                } else {
                    newExposureAssigned = newIdealBackExposure - this.idealBackExposure;
                    this.idealBackExposure = newIdealBackExposure;
                }
            } else { // won't place back bets, I won't set the idealBackExposure
                this.idealBackExposure = 0d;
                newExposureAssigned = 0d; // limit in this case should be 0d, so it is reached
            }
        } else {
            logger.error("trying to set negative idealBackExposure {} for: {} {}", newIdealBackExposure, this.idealBackExposure, Generic.objectToString(this));
            this.idealBackExposure = 0d;
            newExposureAssigned = 0d; // in case of this strange error, I'll also return 0d, as I don't want to further try to setIdealBackExposure
        }
        return newExposureAssigned;
    }

    synchronized double getIdealBackExposure() {
        return this.idealBackExposure;
    }

    private synchronized boolean setBackAmountLimit(final double newBackAmountLimit) {
        final boolean modified;
        if (Double.isNaN(newBackAmountLimit)) {
            modified = false;
        } else  //noinspection FloatingPointEquality
            if (this.backAmountLimit == newBackAmountLimit) {
                modified = false;
            } else {
                this.backAmountLimit = newBackAmountLimit;
                modified = true;
            }

        if (modified) {
            Statics.rulesManager.listOfQueues.send(new SerializableObjectModification<>(RulesManagerModificationCommand.setBackAmountLimit, this.backAmountLimit));
            Statics.rulesManager.rulesHaveChanged.set(true);
        }
        return modified;
    }

    private synchronized boolean setLayAmountLimit(final double newLayAmountLimit) {
        final boolean modified;
        if (Double.isNaN(newLayAmountLimit)) {
            modified = false;
        } else  //noinspection FloatingPointEquality
            if (this.layAmountLimit == newLayAmountLimit) {
                modified = false;
            } else {
                this.layAmountLimit = newLayAmountLimit;
                modified = true;
            }

        if (modified) {
            Statics.rulesManager.listOfQueues.send(new SerializableObjectModification<>(RulesManagerModificationCommand.setLayAmountLimit, this.layAmountLimit));
            Statics.rulesManager.rulesHaveChanged.set(true);
        }
        return modified;
    }

    private synchronized boolean setMinBackOdds(final double newMinBackOdds) {
        final boolean modified;
        if (Double.isNaN(newMinBackOdds)) {
            modified = false;
        } else  //noinspection FloatingPointEquality
            if (this.minBackOdds == newMinBackOdds) {
                modified = false;
            } else {
                this.minBackOdds = newMinBackOdds;
                modified = true;
            }

        if (modified) {
            Statics.rulesManager.listOfQueues.send(new SerializableObjectModification<>(RulesManagerModificationCommand.setMinBackOdds, this.minBackOdds));
            Statics.rulesManager.rulesHaveChanged.set(true);
        }
        return modified;
    }

    private synchronized boolean setMaxLayOdds(final double newMaxLayOdds) {
        final boolean modified;
        if (Double.isNaN(newMaxLayOdds)) {
            modified = false;
        } else  //noinspection FloatingPointEquality
            if (this.maxLayOdds == newMaxLayOdds) {
                modified = false;
            } else {
                this.maxLayOdds = newMaxLayOdds;
                modified = true;
            }

        if (modified) {
            Statics.rulesManager.listOfQueues.send(new SerializableObjectModification<>(RulesManagerModificationCommand.setMaxLayOdds, this.maxLayOdds));
            Statics.rulesManager.rulesHaveChanged.set(true);
        }
        return modified;
    }

    public synchronized int update(final double newMinBackOdds, final double newMaxLayOdds, final double newBackAmountLimit, final double newLayAmountLimit) {
        int modified = 0;
        modified += Generic.booleanToInt(this.setMinBackOdds(newMinBackOdds));
        modified += Generic.booleanToInt(this.setMaxLayOdds(newMaxLayOdds));
        modified += Generic.booleanToInt(this.setBackAmountLimit(newBackAmountLimit));
        modified += Generic.booleanToInt(this.setLayAmountLimit(newLayAmountLimit));
        return modified;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ManagedRunner that = (ManagedRunner) obj;
        return Objects.equals(this.marketId, that.marketId) &&
               Objects.equals(this.runnerId, that.runnerId);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.marketId, this.runnerId);
    }
}
