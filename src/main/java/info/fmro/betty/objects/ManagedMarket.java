package info.fmro.betty.objects;

import info.fmro.betty.enums.MarketBettingType;
import info.fmro.betty.stream.cache.market.Market;
import info.fmro.betty.stream.cache.order.OrderMarket;
import info.fmro.betty.stream.cache.order.OrderMarketRunner;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.cache.util.Utils;
import info.fmro.betty.stream.definitions.MarketDefinition;
import info.fmro.betty.stream.definitions.Order;
import info.fmro.betty.stream.definitions.Side;
import info.fmro.betty.utility.ComparatorMarketPrices;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class ManagedMarket
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(ManagedMarket.class);
    private static final long serialVersionUID = -7958840665816144122L;
    public static final long recentCalculatedLimitPeriod = 30_000L;
    public final AtomicBoolean cancelAllUnmatchedBets = new AtomicBoolean();
    private final HashMap<RunnerId, ManagedRunner> runners = new HashMap<>(); // this is the only place where managedRunners are stored permanently
    private final HashMap<RunnerId, Double> runnerMatchedExposure = new HashMap<>(), runnerTotalExposure = new HashMap<>();
    private final String id; // marketId
    private String parentEventId;
    private double amountLimit = -1d, calculatedLimit, marketMatchedExposure = Double.NaN, marketTotalExposure = Double.NaN, matchedBackExposureSum, totalBackExposureSum;
    private long timeMarketGoesLive, calculatedLimitStamp, manageMarketStamp;
    private boolean marketAlmostLive;
    private final long almostLivePeriod = Generic.HOUR_LENGTH_MILLISECONDS;
    private transient ManagedEvent parentEvent;
    private transient Market market;
    private transient OrderMarket orderMarket;
    private transient ArrayList<ManagedRunner> runnersOrderedList = new ArrayList<>(this.runners.values());

    public ManagedMarket(final String id) {
        this.id = id;
        this.parentEventId = Formulas.getEventIdOfMarketId(this.id);

        runnersOrderedList.sort(Comparator.comparing(ManagedRunner::getLastTradedPrice, new ComparatorMarketPrices()));
    }

    private void readObject(final java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        runnersOrderedList = new ArrayList<>(this.runners.values());
        runnersOrderedList.sort(Comparator.comparing(ManagedRunner::getLastTradedPrice, new ComparatorMarketPrices()));
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized String getParentEventId() {
        if (parentEventId == null) {
            this.parentEventId = Formulas.getEventIdOfMarketId(this.id);
            if (!(this.parentEventId == null)) {
                Statics.rulesManager.rulesHaveChanged.set(true);
            } else {
                logger.error("parentEventId not found for managedMarket: {}", Generic.objectToString(this));
            }
        } else { // I already have parentId, nothing to be done
        }

        return this.parentEventId;
    }

    public synchronized ManagedEvent getParentEvent() {
        if (parentEvent == null) {
            this.parentEvent = Statics.rulesManager.events.get(this.getParentEventId());
//            this.parentEvent.addManagedMarket(this);

            final String marketId = this.getId();
            parentEvent.marketsMap.put(marketId, this);
            if (parentEvent.marketIds.add(marketId)) {
                Statics.rulesManager.rulesHaveChanged.set(true);
            }
        } else { // I already have parentEvent, nothing to be done
        }

        return this.parentEvent;
    }

    public synchronized boolean parentEventIsSet() { // used for testing if the parentEvent has been set, to detect bugs
        final boolean result = parentEvent != null;

        if (!result) {
            logger.error("false result in parentEventIsSet for: {}", Generic.objectToString(this));
        } else { // no error, nothing to be done, will return result
        }

        return result;
    }

    public synchronized boolean parentEventHasTheMarketAdded() { // used for testing, to detect bugs
        final boolean result;
        if (parentEvent != null) {
            result = parentEvent.marketIds.contains(this.id);
        } else {
            result = false;
        }

        if (!result) {
            logger.error("false result in parentEventHasTheMarketAdded for: {}", Generic.objectToString(this));
        } else { // no error, nothing to be done, will return result
        }

        return result;
    }

    public synchronized boolean parentEventHasTheMarketIdAdded() { // used for testing, to detect bugs
        final boolean result;
        if (parentEvent != null) {
            result = parentEvent.marketIds.contains(this.id);
        } else {
            result = false;
        }

        if (!result) {
            logger.error("false result in parentEventHasTheMarketIdAdded for: {}", Generic.objectToString(this));
        } else { // no error, nothing to be done, will return result
        }

        return result;
    }

//    private synchronized double getIdealBackExposureSum() {
//        return idealBackExposureSum;
//    }
//
//    private synchronized void setIdealBackExposureSum(final double idealBackExposureSum) {
//        this.idealBackExposureSum = idealBackExposureSum;
//    }

    private synchronized double getMatchedBackExposureSum() {
        return matchedBackExposureSum;
    }

    private synchronized void setMatchedBackExposureSum(final double matchedBackExposureSum) {
        this.matchedBackExposureSum = matchedBackExposureSum;
    }

    private synchronized double getTotalBackExposureSum() {
        return totalBackExposureSum;
    }

    private synchronized void setTotalBackExposureSum(final double totalBackExposureSum) {
        this.totalBackExposureSum = totalBackExposureSum;
    }

    public synchronized boolean defaultExposureValuesExist() {
        return Double.isNaN(marketMatchedExposure) || Double.isNaN(marketTotalExposure);
    }

    public synchronized double getMarketMatchedExposure() {
        return marketMatchedExposure;
    }

    public synchronized double getMarketTotalExposure() {
        return marketTotalExposure;
    }

    public synchronized void resetOrderCacheObjects() {
        this.orderMarket = null;
        for (ManagedRunner managedRunner : runners.values()) {
            managedRunner.resetOrderMarketRunner();
        }
    }

    private synchronized ManagedRunner addRunner(@NotNull final RunnerId runnerId) {
        final ManagedRunner managedRunner = new ManagedRunner(id, runnerId);
        return addRunner(runnerId, managedRunner);
    }

    private synchronized ManagedRunner addRunner(@NotNull final ManagedRunner managedRunner) {
        final RunnerId runnerId = managedRunner.getRunnerId();
        return addRunner(runnerId, managedRunner);
    }

    private synchronized ManagedRunner addRunner(final RunnerId runnerId, @NotNull final ManagedRunner managedRunner) {
        final ManagedRunner previousValue = runners.put(runnerId, managedRunner); // runners.put needs to be before runnersOrderedList.addAll

        managedRunner.attachRunner(market);

        runnersOrderedList.clear();
        runnersOrderedList.addAll(this.runners.values());
        runnersOrderedList.sort(Comparator.comparing(ManagedRunner::getLastTradedPrice, new ComparatorMarketPrices()));

        return previousValue;
    }

    private synchronized void attachMarket() { // this is run periodically, as it's contained in the manage method, that is run periodically
        if (market == null) {
            market = Statics.marketCache.getMarket(id);
        } else { // I already have the market, nothing to be done
        }
        if (market == null) {
            logger.error("no market found in ManagedMarket for: {} {}", id, Generic.objectToString(this));
        } else {
            for (ManagedRunner managedRunner : runners.values()) {
                managedRunner.attachRunner(market);
            }
            final HashSet<RunnerId> runnerIds = market.getRunnerIds();
            for (RunnerId runnerId : runnerIds) {
                if (runnerId != null) {
                    if (runners.containsKey(runnerId)) { // already exists, nothing to be done
                    } else { // managedRunner does not exist, I'll generate it; this is done initially, but also later if runners are added
//                            final ManagedRunner managedRunner = new ManagedRunner(longDoublePair.getSelectionId(), longDoublePair.getHandicap());
//                            runners.put(longDoublePair, managedRunner);
//                            managedRunner.attachRunner(market);
                        addRunner(runnerId);
                    }
                } else {
                    logger.error("null runnerId for orderMarket: {}", Generic.objectToString(market));
                }
            } // end for
        }
    }

    private synchronized void attachOrderMarket() { // this is run periodically, as it's contained in the manage method, that is run periodically
        if (orderMarket == null) {
            orderMarket = Statics.orderCache.getOrderMarket(id);
        } else { // I already have the market, nothing to be done on this branch
        }
        if (orderMarket == null) { // normal, it means no orders exist for this managedMarket, nothing else to be done
        } else {
            for (ManagedRunner managedRunner : runners.values()) {
                managedRunner.attachOrderRunner(orderMarket);
            }
            final HashSet<RunnerId> runnerIds = orderMarket.getRunnerIds();
            for (RunnerId runnerId : runnerIds) {
                if (runnerId != null) {
                    if (runners.containsKey(runnerId)) { // already exists, nothing to be done
                    } else { // managedRunner does not exist, this is an error that shouldn't happen and can't be properly fixed on this branch
                        logger.error("managedRunner does not exist for existing orderMarketRunner: {} {} {}", Generic.objectToString(runnerId), Generic.objectToString(runners), Generic.objectToString(this));
                    }
                } else {
                    logger.error("null runnerId for orderMarket: {}", Generic.objectToString(orderMarket));
                }
            } // end for
        }
    }

    private synchronized boolean isMarketAlmostLive() {
        if (marketAlmostLive) { // already almostLive, won't recheck
        } else {
            final MarketDefinition marketDefinition = market.getMarketDefinition();
            final Boolean inPlay = marketDefinition.getInPlay();
            if (inPlay != null && inPlay) {
                marketAlmostLive = inPlay;
            } else {
                final Date marketTime = marketDefinition.getMarketTime(); // I hope this is market start time, I'll test
                if (marketTime != null) {
                    final long marketTimeMillis = marketTime.getTime();
                    final long currentTime = System.currentTimeMillis();
                    if (marketTimeMillis + almostLivePeriod >= currentTime) {
                        marketAlmostLive = true;
                    }
                } else {
                    logger.error("null marketTime during isMarketAlmostLive for: {} {}", id, Generic.objectToString(marketDefinition));
                }
            }
            if (marketAlmostLive) {
                logger.info("managed market {} is almost live", id);
            }
        }
        return marketAlmostLive;
    }

    public synchronized double getTotalValue() {
        final double result;
        if (market != null) {
            result = market.getTvEUR();
        } else {
            logger.error("no market present in getTotalValue for: {}", Generic.objectToString(this));
            result = 0d;
        }
        return result;
    }

    private synchronized boolean checkTwoWayMarketLimitsValid() {
        // maxBackLimit on one runner needs to be equal to maxLayLimit on the other, and vice-versa; same with odds being usable or unusable; max odds on back roughly inversely proportional to lay on the other runner, and vice-versa
        boolean isValid;
        if (runnersOrderedList == null || runnersOrderedList.size() != 2) {
            logger.error("null or wrong size runnersOrderedList in checkTwoWayMarketLimitsValid for: {} {}", Generic.objectToString(runnersOrderedList), Generic.objectToString(this));
            isValid = false;
        } else {
            try {
                final ManagedRunner firstRunner = runnersOrderedList.get(0);
                final ManagedRunner secondRunner = runnersOrderedList.get(1);
                if (firstRunner == null || secondRunner == null) {
                    logger.error("null runner in checkTwoWayMarketLimitsValid for: {} {} {} {}", Generic.objectToString(firstRunner), Generic.objectToString(secondRunner), Generic.objectToString(runnersOrderedList), Generic.objectToString(this));
                    isValid = false;
                } else {
                    final double firstBackAmountLimit = firstRunner.getBackAmountLimit(), secondBackAmountLimit = secondRunner.getBackAmountLimit(),
                            firstLayAmountLimit = firstRunner.getLayAmountLimit(), secondLayAmountLimit = secondRunner.getLayAmountLimit();
                    if (firstBackAmountLimit != secondLayAmountLimit || secondBackAmountLimit != firstLayAmountLimit) {
                        logger.error("not equal amountLimits in checkTwoWayMarketLimitsValid for: {} {} {} {} {} {} {} {}", firstBackAmountLimit, secondBackAmountLimit, firstLayAmountLimit, secondLayAmountLimit, Generic.objectToString(firstRunner),
                                     Generic.objectToString(secondRunner), Generic.objectToString(runnersOrderedList), Generic.objectToString(this));
                        isValid = false;
                    } else {
                        final double firstMinBackOdds = firstRunner.getMinBackOdds(), secondMinBackOdds = secondRunner.getMinBackOdds(), firstMaxLayOdds = firstRunner.getMaxLayOdds(), secondMaxLayOdds = secondRunner.getMaxLayOdds();
                        if (Formulas.oddsAreUsable(firstMinBackOdds) != Formulas.oddsAreUsable(secondMaxLayOdds) || Formulas.oddsAreUsable(secondMinBackOdds) != Formulas.oddsAreUsable(firstMaxLayOdds)) {
                            logger.error("not equal oddsAreUsable in checkTwoWayMarketLimitsValid for: {} {} {} {} {} {} {} {}", firstMinBackOdds, secondMinBackOdds, firstMaxLayOdds, secondMaxLayOdds, Generic.objectToString(firstRunner),
                                         Generic.objectToString(secondRunner), Generic.objectToString(runnersOrderedList), Generic.objectToString(this));
                            isValid = false;
                        } else {
                            if (Formulas.oddsAreInverse(firstMinBackOdds, secondMaxLayOdds) && Formulas.oddsAreInverse(firstMaxLayOdds, secondMinBackOdds)) {
                                isValid = true;
                            } else {
                                logger.error("odds are not inverse in checkTwoWayMarketLimitsValid for: {} {} {} {} {} {} {} {}", firstMinBackOdds, secondMinBackOdds, firstMaxLayOdds, secondMaxLayOdds, Generic.objectToString(firstRunner),
                                             Generic.objectToString(secondRunner), Generic.objectToString(runnersOrderedList), Generic.objectToString(this));
                                isValid = false;
                            }
                        }
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                logger.error("IndexOutOfBoundsException in checkTwoWayMarketLimitsValid for: {} {}", Generic.objectToString(runnersOrderedList), Generic.objectToString(this), e);
                isValid = false;
            }
        }
        return isValid;
    }

    private synchronized int balanceTwoRunnerMarket(@NotNull final ManagedRunner firstRunner, @NotNull final ManagedRunner secondRunner, @NotNull final OrderMarketRunner firstOrderRunner, @NotNull final OrderMarketRunner secondOrderRunner,
                                                    @NotNull final List<Side> sideList, final double excessMatchedExposure) {
        int modifications = 0;
        if (sideList.size() != 2 || sideList.contains(null)) {
            logger.error("bogus sideList for balanceTwoRunnerMarket: {} {} {} {} {} {}", Generic.objectToString(sideList), Generic.objectToString(firstRunner), Generic.objectToString(secondRunner), Generic.objectToString(firstOrderRunner),
                         Generic.objectToString(secondOrderRunner), excessMatchedExposure);
        } else {
            final @NotNull Side firstSide = sideList.get(0), secondSide = sideList.get(1);
            if (firstSide.equals(Side.B) && secondSide.equals(Side.L)) {
                modifications += firstOrderRunner.cancelUnmatched(Side.L);
                modifications += secondOrderRunner.cancelUnmatched(Side.B);
                final @NotNull List<Double> exposuresToBePlaced = Utils.getExposureToBePlacedForTwoWayMarket(firstRunner, secondRunner, firstOrderRunner, secondOrderRunner, sideList, excessMatchedExposure);
                final double firstExposureToBePlaced = exposuresToBePlaced.get(0), secondExposureToBePlaced = exposuresToBePlaced.get(1);
                if (firstExposureToBePlaced <= 0d) {
                    modifications += firstOrderRunner.cancelUnmatchedAmounts(-firstExposureToBePlaced, 0d);
                } else {
                    modifications += firstOrderRunner.placeOrder(firstRunner.getBackAmountLimit(), firstRunner.getLayAmountLimit(), Side.B, firstRunner.getToBeUsedBackOdds(), firstExposureToBePlaced);
                }
                if (secondExposureToBePlaced <= 0d) {
                    modifications += secondOrderRunner.cancelUnmatchedAmounts(0d, -secondExposureToBePlaced);
                } else {
                    modifications += secondOrderRunner.placeOrder(secondRunner.getBackAmountLimit(), secondRunner.getLayAmountLimit(), Side.L, secondRunner.getToBeUsedLayOdds(), secondExposureToBePlaced);
                }
            } else if (firstSide.equals(Side.L) && secondSide.equals(Side.B)) {
                modifications += firstOrderRunner.cancelUnmatched(Side.B);
                modifications += secondOrderRunner.cancelUnmatched(Side.L);
                final @NotNull List<Double> exposuresToBePlaced = Utils.getExposureToBePlacedForTwoWayMarket(firstRunner, secondRunner, firstOrderRunner, secondOrderRunner, sideList, excessMatchedExposure);
                final double firstExposureToBePlaced = exposuresToBePlaced.get(0), secondExposureToBePlaced = exposuresToBePlaced.get(1);
                if (firstExposureToBePlaced <= 0d) {
                    modifications += firstOrderRunner.cancelUnmatchedAmounts(0d, -firstExposureToBePlaced);
                } else {
                    modifications += firstOrderRunner.placeOrder(firstRunner.getBackAmountLimit(), firstRunner.getLayAmountLimit(), Side.L, firstRunner.getToBeUsedLayOdds(), firstExposureToBePlaced);
                }
                if (secondExposureToBePlaced <= 0d) {
                    modifications += secondOrderRunner.cancelUnmatchedAmounts(-secondExposureToBePlaced, 0d);
                } else {
                    modifications += secondOrderRunner.placeOrder(secondRunner.getBackAmountLimit(), secondRunner.getLayAmountLimit(), Side.B, secondRunner.getToBeUsedBackOdds(), secondExposureToBePlaced);
                }
            } else {
                logger.error("bogus sides for balanceTwoRunnerMarket: {} {} {} {} {} {}", Generic.objectToString(sideList), Generic.objectToString(firstRunner), Generic.objectToString(secondRunner), Generic.objectToString(firstOrderRunner),
                             Generic.objectToString(secondOrderRunner), excessMatchedExposure);
            }
        }
        return modifications;
    }

    private synchronized int useNewLimitOnTwoRunnerMarket(@NotNull final ManagedRunner firstRunner, @NotNull final ManagedRunner secondRunner, @NotNull final OrderMarketRunner firstOrderRunner, @NotNull final OrderMarketRunner secondOrderRunner,
                                                          @NotNull final List<Side> sideList, final double availableLimit) {
        int modifications = 0;
        if (sideList.size() != 2 || sideList.contains(null)) {
            logger.error("bogus sideList for useNewLimitOnTwoRunnerMarket: {} {} {} {} {} {}", Generic.objectToString(sideList), Generic.objectToString(firstRunner), Generic.objectToString(secondRunner), Generic.objectToString(firstOrderRunner),
                         Generic.objectToString(secondOrderRunner), availableLimit);
        } else {
            final @NotNull Side firstSide = sideList.get(0), secondSide = sideList.get(1);
            if (firstSide.equals(Side.B) && secondSide.equals(Side.L)) {
                final @NotNull List<Double> exposuresToBePlaced = Utils.getAmountsToBePlacedForTwoWayMarket(firstRunner, secondRunner, firstOrderRunner, secondOrderRunner, sideList, availableLimit);
                final double firstExposureToBePlaced = exposuresToBePlaced.get(0), secondExposureToBePlaced = exposuresToBePlaced.get(1);
                if (firstExposureToBePlaced <= 0d) {
                    modifications += firstOrderRunner.cancelUnmatchedAmounts(-firstExposureToBePlaced, 0d);
                } else {
                    modifications += firstOrderRunner.placeOrder(firstRunner.getBackAmountLimit(), firstRunner.getLayAmountLimit(), Side.B, firstRunner.getToBeUsedBackOdds(), firstExposureToBePlaced);
                }
                if (secondExposureToBePlaced <= 0d) {
                    modifications += secondOrderRunner.cancelUnmatchedAmounts(0d, -secondExposureToBePlaced);
                } else {
                    modifications += secondOrderRunner.placeOrder(secondRunner.getBackAmountLimit(), secondRunner.getLayAmountLimit(), Side.L, secondRunner.getToBeUsedLayOdds(), secondExposureToBePlaced);
                }
            } else if (firstSide.equals(Side.L) && secondSide.equals(Side.B)) {
                final @NotNull List<Double> exposuresToBePlaced = Utils.getAmountsToBePlacedForTwoWayMarket(firstRunner, secondRunner, firstOrderRunner, secondOrderRunner, sideList, availableLimit);
                final double firstExposureToBePlaced = exposuresToBePlaced.get(0), secondExposureToBePlaced = exposuresToBePlaced.get(1);
                if (firstExposureToBePlaced <= 0d) {
                    modifications += firstOrderRunner.cancelUnmatchedAmounts(0d, -firstExposureToBePlaced);
                } else {
                    modifications += firstOrderRunner.placeOrder(firstRunner.getBackAmountLimit(), firstRunner.getLayAmountLimit(), Side.L, firstRunner.getToBeUsedLayOdds(), firstExposureToBePlaced);
                }
                if (secondExposureToBePlaced <= 0d) {
                    modifications += secondOrderRunner.cancelUnmatchedAmounts(-secondExposureToBePlaced, 0d);
                } else {
                    modifications += secondOrderRunner.placeOrder(secondRunner.getBackAmountLimit(), secondRunner.getLayAmountLimit(), Side.B, secondRunner.getToBeUsedBackOdds(), secondExposureToBePlaced);
                }
            } else {
                logger.error("bogus sides for useNewLimitOnTwoRunnerMarket: {} {} {} {} {} {}", Generic.objectToString(sideList), Generic.objectToString(firstRunner), Generic.objectToString(secondRunner), Generic.objectToString(firstOrderRunner),
                             Generic.objectToString(secondOrderRunner), availableLimit);
            }
        }
        return modifications;
    }

    private synchronized int removeExposure() { // assumes market and runners exposure has been updated
        int modifications = 0;
        if (marketTotalExposure == Double.NaN) {
            logger.error("marketTotalExposure not initialized in removeExposure for: {}", Generic.objectToString(this));
        } else if (marketTotalExposure < .1) { // exposure too small, nothing to be done
        } else {
            final int size = runnersOrderedList.size();
            // in all cases, existing unmatched bets on the wrong side are canceled, bets on the right side are kept within the necessary amount, and if extra is needed it is added
            if (size == 2) { // special case
                if (checkTwoWayMarketLimitsValid()) {
                    // if valid, balance both runners at the same time, with back on one and lay on the other, with different factors used in calculating the amounts
                    // the factors are the price of toBeUsedOdds, and which of the toBeUsedOdds is more profitable; those two should be enough for now; also lay bets should be given slight priority over back bets, as other gamblers like to back rather than lay
                    // in the special case, market wide exposure is used, rather than the one on each runner
                    final ManagedRunner firstRunner = runnersOrderedList.get(0), secondRunner = runnersOrderedList.get(1);
                    final double backLayMatchedExposure = firstRunner.getBackMatchedExposure() + secondRunner.getLayMatchedExposure(), layBackMatchedExposure = firstRunner.getLayMatchedExposure() + secondRunner.getBackMatchedExposure(),
                            excessMatchedExposure = Math.abs(backLayMatchedExposure - layBackMatchedExposure);
                    final OrderMarketRunner firstOrderRunner = firstRunner.getOrderMarketRunner(), secondOrderRunner = secondRunner.getOrderMarketRunner();
                    if (firstOrderRunner == null || secondOrderRunner == null) {
                        logger.error("null OrderMarketRunner during removeExposure for: {} {} {}", Generic.objectToString(firstOrderRunner), Generic.objectToString(secondOrderRunner), Generic.objectToString(this));
                        modifications += cancelAllUnmatchedBets();
                    } else if (excessMatchedExposure < .1d) {
                        modifications += cancelAllUnmatchedBets();
                    } else if (backLayMatchedExposure > layBackMatchedExposure) {
                        modifications += balanceTwoRunnerMarket(firstRunner, secondRunner, firstOrderRunner, secondOrderRunner, List.of(Side.L, Side.B), excessMatchedExposure); // I'll use unmatched exposure, equal to excessMatchedExposure, on lay/back
                    } else { // backLayMatchedExposure < layBackMatchedExposure
                        modifications += balanceTwoRunnerMarket(firstRunner, secondRunner, firstOrderRunner, secondOrderRunner, List.of(Side.B, Side.L), excessMatchedExposure); // I'll use unmatched exposure, equal to excessMatchedExposure, on back/lay
                    }
                } else { // if not valid, error message and take action, with all order canceling
                    logger.error("checkTwoWayMarketLimitsValid false in removeExposure for: {}", Generic.objectToString(this));
                    modifications += cancelAllUnmatchedBets();
                }
            } else {
                for (ManagedRunner managedRunner : runnersOrderedList) { // only the exposure on the runner is considered, not the market wide exposure
                    modifications += managedRunner.removeExposure();
                } // end for
            }
        }
        return modifications;
    }

    private synchronized int useTheNewLimit() {
        int modifications = 0;
        if (marketTotalExposure == Double.NaN) {
            logger.error("marketTotalExposure not initialized in useTheNewLimit for: {}", Generic.objectToString(this));
        } else {
            // variant of removeExposure, or the other way around ... major difference is that in this case the overall calculatedLimit matters, this time it's not about individual runners
            final int size = runnersOrderedList.size();
            if (size == 2) { // special case
                // this one is easy, most similar to the removeExposure situation
                // the factors for splitting the unmatched exposure between runners are the price of toBeUsedOdds, which of the toBeUsedOdds is more profitable, and the size of existing unmatched bets
                // also lay bets should be given slight priority over back bets, as other gamblers like to back rather than lay
                if (checkTwoWayMarketLimitsValid()) {
                    final ManagedRunner firstRunner = runnersOrderedList.get(0), secondRunner = runnersOrderedList.get(1);
                    final double backLayExposure = firstRunner.getBackTotalExposure() + secondRunner.getLayTotalExposure(), layBackExposure = firstRunner.getLayTotalExposure() + secondRunner.getBackTotalExposure();
                    final double availableBackLayLimit = this.calculatedLimit - backLayExposure, availableLayBackLimit = this.calculatedLimit - layBackExposure;
                    final OrderMarketRunner firstOrderRunner = firstRunner.getOrderMarketRunner(), secondOrderRunner = secondRunner.getOrderMarketRunner();
                    if (firstOrderRunner == null || secondOrderRunner == null) {
                        logger.error("null OrderMarketRunner during useTheNewLimit for: {} {} {}", Generic.objectToString(firstOrderRunner), Generic.objectToString(secondOrderRunner), Generic.objectToString(this));
                        modifications += cancelAllUnmatchedBets();
                    } else {
                        if (availableBackLayLimit != 0d) {
                            modifications += useNewLimitOnTwoRunnerMarket(firstRunner, secondRunner, firstOrderRunner, secondOrderRunner, List.of(Side.L, Side.B), availableBackLayLimit);
                        } else { // availableLimit is 0d, nothing to be done
                        }
                        if (availableLayBackLimit != 0d) {
                            modifications += useNewLimitOnTwoRunnerMarket(firstRunner, secondRunner, firstOrderRunner, secondOrderRunner, List.of(Side.B, Side.L), availableLayBackLimit);
                        } else { // availableLimit is 0d, nothing to be done
                        }
                    }
                } else { // if not valid, error message and take action, with all order canceling
                    logger.error("checkTwoWayMarketLimitsValid false in useTheNewLimit for: {}", Generic.objectToString(this));
                    modifications += cancelAllUnmatchedBets();
                }
            } else {
                // calculate the splitting proportion for each runner: the factors for splitting the unmatched exposure between runners are the price of toBeUsedOdds, and the size of existing unmatched bets
                // total exposure only exists as total back exposure on each runner, and can be increased by lay on that runner and back on other runners
                // each runner has total back exposure, and ideally it should be equal to the calculatedLimit, but has to obey the per runner limits as well

                calculateIdealBackExposureList();

                // new limit for lay exposure on the runner will be calculated by proportion * calculatedLimit; runner limits are also considered
                // calculate the amounts that need to be added or subtracted on the back side of that runner, for the new lay exposure limit
                // place the needed orders and recalculate exposure
                if (Math.abs(this.calculatedLimit - this.marketTotalExposure) < .1d) { // potential extra lay bets are placed after the conditional, nothing to be done here
                } else if (this.calculatedLimit > this.marketTotalExposure) { // placing extra bets, starting with back
                    if (this.calculatedLimit >= this.totalBackExposureSum + .1d) { // placing extra back bets
                        final double extraBackBetsToBePlaced = Math.min(this.calculatedLimit - this.marketTotalExposure, this.calculatedLimit - this.totalBackExposureSum);
                        final double availableIdealBackExposure = calculateAvailableIdealBackExposureSum();
                        if (availableIdealBackExposure <= 0d) {
                            logger.error("negative or zero availableIdealBackExposure: {} {} {} {} {} {}", availableIdealBackExposure, extraBackBetsToBePlaced, this.totalBackExposureSum, this.calculatedLimit, this.marketTotalExposure,
                                         Generic.objectToString(this));
                        } else {
                            final double proportionOfAvailableIdealBackExposureToBeUsed = Math.min(1d, extraBackBetsToBePlaced / availableIdealBackExposure);
                            for (final ManagedRunner managedRunner : this.runnersOrderedList) {
                                final double amountToPlaceOnBack = (managedRunner.getIdealBackExposure() - managedRunner.getBackTotalExposure()) * proportionOfAvailableIdealBackExposureToBeUsed;
                                if (amountToPlaceOnBack > .1d) {
                                    if (Statics.ordersThread.addPlaceOrder(this.id, managedRunner.getRunnerId(), Side.B, managedRunner.getToBeUsedBackOdds(), amountToPlaceOnBack) > 0d) {
                                        modifications++;
                                    } else { // no modification made, nothing to be done
                                    }
                                } else { // amount negative or too small, won't place anything
                                }
                            } // end for
                        }
                    } else { // potential extra lay bets are placed after the conditional, nothing to be done here
                    }
                } else { // this.calculatedLimit < this.marketTotalExposure; removing bets
                    if (this.totalBackExposureSum >= this.calculatedLimit + .1d) { // removing back bets
                        final double backBetsToBeRemoved = Math.min(this.marketTotalExposure - this.calculatedLimit, this.totalBackExposureSum - this.calculatedLimit);
                        final double excessiveBackExposureOverIdeal = calculateExcessiveBackExposureOverIdealSum();
                        if (excessiveBackExposureOverIdeal <= 0d) {
                            logger.error("negative or zero excessiveBackExposureOverIdeal: {} {} {} {} {} {}", excessiveBackExposureOverIdeal, backBetsToBeRemoved, this.totalBackExposureSum, this.calculatedLimit, this.marketTotalExposure,
                                         Generic.objectToString(this));
                        } else {
                            final double proportionOfExcessiveExposureToBeRemoved = Math.min(1d, backBetsToBeRemoved / excessiveBackExposureOverIdeal);
                            for (final ManagedRunner managedRunner : this.runnersOrderedList) {
                                final double amountToRemoveFromBack = (managedRunner.getBackTotalExposure() - managedRunner.getIdealBackExposure()) * proportionOfExcessiveExposureToBeRemoved;
                                if (amountToRemoveFromBack > 0d) {
                                    final OrderMarketRunner orderMarketRunner = managedRunner.getOrderMarketRunner();
                                    if (orderMarketRunner == null) {
                                        logger.error("null orderMarketRunner while positive amountToRemoveFromBack for: {} {} {}", amountToRemoveFromBack, Generic.objectToString(managedRunner), Generic.objectToString(this));
                                    } else {
                                        modifications += orderMarketRunner
                                                .balanceTotalAmounts(managedRunner.getBackAmountLimit(), managedRunner.getLayAmountLimit(), managedRunner.getToBeUsedBackOdds(), managedRunner.getToBeUsedLayOdds(), amountToRemoveFromBack, 0d);
                                    }
                                } else { // amount negative, won't remove anything
                                }
                            } // end for
                        }
                    } else { // potential lay bets are removed after the conditional, nothing to be done here
                    }
                }
                if (modifications > 0) {
                    calculateExposure();
                    modifications = 0;
                } else { // no need to calculateExposure
                }

                // I can still place or remove some lay bets
                // use the calculatedLimit and modify the bets on the lay side of the runner, considering the runner limit as well
                // place the orders, recalculating exposure now shouldn't be necessary
                for (final ManagedRunner managedRunner : this.runnersOrderedList) {
                    final double availableLayLimit = managedRunner.getLayAmountLimit() - managedRunner.getLayTotalExposure();
                    final double availableMarketLimit = this.calculatedLimit - this.totalBackExposureSum + managedRunner.getBackTotalExposure() - managedRunner.getLayTotalExposure();
                    final double minimumAvailableLimit = Math.min(availableLayLimit, availableMarketLimit);
                    if (minimumAvailableLimit > .1d) {
                        final double toBeUsedLayOdds = managedRunner.getToBeUsedLayOdds();
                        if (Formulas.oddsAreUsable(toBeUsedLayOdds)) {
                            final double amountToPlaceOnLay = minimumAvailableLimit / (toBeUsedLayOdds - 1d);
                            if (Statics.ordersThread.addPlaceOrder(this.id, managedRunner.getRunnerId(), Side.L, toBeUsedLayOdds, amountToPlaceOnLay) > 0d) {
                                modifications++;
                            } else { // no modification made, nothing to be done
                            }
                        } else { // odds unusable, nothing to be done
                        }
                    } else if (minimumAvailableLimit < -.1d) {
                        final OrderMarketRunner orderMarketRunner = managedRunner.getOrderMarketRunner();
                        if (orderMarketRunner == null) {
                            logger.error("null orderMarketRunner while negative minimumAvailableLimit for: {} {} {}", minimumAvailableLimit, Generic.objectToString(managedRunner), Generic.objectToString(this));
                        } else {
                            modifications += orderMarketRunner.balanceTotalAmounts(managedRunner.getBackAmountLimit(), managedRunner.getLayAmountLimit(), managedRunner.getToBeUsedBackOdds(), managedRunner.getToBeUsedLayOdds(), 0d, -minimumAvailableLimit);
                        }
                    } else { // difference too small, nothing to be done
                    }
                }
            }
        }
        return modifications;
    }

    private synchronized double calculateAvailableIdealBackExposureSum() { // I will only add the positive amounts
        double availableIdealBackExposureSum = 0d;
        for (final ManagedRunner managedRunner : this.runnersOrderedList) {
            final double availableIdealBackExposure = managedRunner.getIdealBackExposure() - managedRunner.getBackTotalExposure();
            if (availableIdealBackExposure > 0d) {
                availableIdealBackExposureSum += availableIdealBackExposure;
            } else { // I won't add negative amounts, nothing to be done
            }
        }
        return availableIdealBackExposureSum;
    }

    private synchronized double calculateExcessiveBackExposureOverIdealSum() { // I will only add the positive amounts
        double excessiveBackExposureOverIdealSum = 0d;
        for (final ManagedRunner managedRunner : this.runnersOrderedList) {
            final double excessiveBackExposureOverIdeal = managedRunner.getBackTotalExposure() - managedRunner.getIdealBackExposure();
            if (excessiveBackExposureOverIdeal > 0d) {
                excessiveBackExposureOverIdealSum += excessiveBackExposureOverIdeal;
            } else { // I won't add negative amounts, nothing to be done
            }
        }
        return excessiveBackExposureOverIdealSum;
    }

    private synchronized void calculateProportionOfMarketLimitPerRunnerList() { // calculated proportions depend on the toBeUsedBackOdds
        final double sumOfStandardAmounts = runnersOrderedList.stream().filter(x -> Formulas.oddsAreUsable(x.getToBeUsedBackOdds())).mapToDouble(x -> 1d / (x.getToBeUsedBackOdds() - 1d)).sum();
        for (final ManagedRunner managedRunner : this.runnersOrderedList) { // sumOfStandardAmounts should always be != 0d if at least one oddsAreUsable
            final double proportion = Formulas.oddsAreUsable(managedRunner.getToBeUsedBackOdds()) ? 1d / (managedRunner.getToBeUsedBackOdds() - 1d) / sumOfStandardAmounts : 0d;
            managedRunner.setProportionOfMarketLimitPerRunner(proportion);
        }
    }

    private synchronized void calculateIdealBackExposureList() {
        calculateProportionOfMarketLimitPerRunnerList();
        // reset idealBackExposure
        for (final ManagedRunner managedRunner : this.runnersOrderedList) {
            managedRunner.setIdealBackExposure(0d);
        }
        double exposureLeftToBeAssigned = this.calculatedLimit;
        final ArrayList<ManagedRunner> runnersThatCanStillBeAssignedExposure = new ArrayList<>(this.runnersOrderedList);
        int whileCounter = 0;
        while (exposureLeftToBeAssigned >= .1d && !runnersThatCanStillBeAssignedExposure.isEmpty() && whileCounter < 100) {
            final double exposureLeftToBeAssignedAtBeginningOfLoopIteration = exposureLeftToBeAssigned;
            final ArrayList<ManagedRunner> runnersToRemove = new ArrayList<>();
            // calculate total proportion of remaining runners; initially it should be 1d
            double totalProportionSumForRemainingRunners = 0d;
            for (final ManagedRunner managedRunner : runnersThatCanStillBeAssignedExposure) {
                totalProportionSumForRemainingRunners += managedRunner.getProportionOfMarketLimitPerRunner();
            }
            if (totalProportionSumForRemainingRunners > 0d) {
                for (final ManagedRunner managedRunner : runnersThatCanStillBeAssignedExposure) {
                    final double idealExposure = exposureLeftToBeAssignedAtBeginningOfLoopIteration * managedRunner.getProportionOfMarketLimitPerRunner() / totalProportionSumForRemainingRunners;
                    final double assignedExposure = managedRunner.addIdealBackExposure(idealExposure);
                    exposureLeftToBeAssigned -= assignedExposure;
                    if (assignedExposure < idealExposure || assignedExposure == 0d) {
                        runnersToRemove.add(managedRunner);
                    } else { // all exposure has been added, this runner might still be usable in further iterations of the while loop
                    }
                }
            } else {
                logger.error("bogus totalProportionSumForRemainingRunners {} for: {} {} {} {}", totalProportionSumForRemainingRunners, exposureLeftToBeAssigned, whileCounter, Generic.objectToString(runnersThatCanStillBeAssignedExposure),
                             Generic.objectToString(this));
                break;
            }

            runnersThatCanStillBeAssignedExposure.removeAll(runnersToRemove);
            whileCounter++;
        }
        if (exposureLeftToBeAssigned >= .1d && !runnersThatCanStillBeAssignedExposure.isEmpty()) {
            logger.error("runnersThatCanStillBeAssignedExposure not empty: {} {} {} {}", whileCounter, exposureLeftToBeAssigned, Generic.objectToString(runnersThatCanStillBeAssignedExposure), Generic.objectToString(this));
        } else { // no error, nothing to print
        }
//        updateIdealBackExposureSum();
    }

    public synchronized double getMaxMarketLimit() {
        final double result;
        final double safetyLimit = Statics.safetyLimits.getDefaultMarketLimit(id);
        if (this.amountLimit >= 0) {
            result = Math.min(this.amountLimit, safetyLimit);
        } else {
            result = safetyLimit;
        }
        return result;
    }

    private synchronized boolean isCalculatedLimitRecent() {
        final long currentTime = System.currentTimeMillis();
        final long timeSinceStamp = currentTime - this.calculatedLimitStamp;
        return timeSinceStamp <= recentCalculatedLimitPeriod;
    }

    private synchronized void calculatedLimitStamp() {
        final long currentTime = System.currentTimeMillis();
        if (currentTime > this.calculatedLimitStamp) {
            this.calculatedLimitStamp = currentTime;
        } else {
            logger.error("currentTime {} is not greater than calculatedLimitStamp {} and difference is: {}", currentTime, this.calculatedLimitStamp, currentTime - this.calculatedLimitStamp);
        }
    }

    public synchronized boolean setCalculatedLimit(final double newLimit, final boolean limitCanBeIncreased) {
        final boolean modified;
        if (limitCanBeIncreased || newLimit < this.calculatedLimit) {
            modified = setCalculatedLimit(newLimit);
        } else {
            modified = false;
        }

        return modified;
    }

    private synchronized boolean setCalculatedLimit(final double newLimit) {
        final boolean modified;

        if (this.calculatedLimit != 0) {
            final double difference = Math.abs(newLimit - this.calculatedLimit);
            final double differenceProportion = difference / this.calculatedLimit;
            if (difference >= 2 || differenceProportion >= .02d) {
                modified = true;
            } else {
                modified = false;
            }
        } else if (newLimit != this.calculatedLimit) {
            modified = true;
        } else { // both are zero
            modified = false;
        }

        if (modified) {
            this.calculatedLimit = newLimit;
            final double maxLimit = this.getMaxMarketLimit();
            if (this.calculatedLimit > maxLimit) {
                this.calculatedLimit = maxLimit;
            }
            if (this.calculatedLimit < 0d) {
                logger.error("trying to set negative calculated limit {} in setCalculatedLimit for: {}", this.calculatedLimit, Generic.objectToString(this));
                this.calculatedLimit = 0d;
            }
        } else { // nothing to do, won't modify the value
        }
        calculatedLimitStamp(); // I'll stamp even in the 2 cases where modified is false, because the limit has been recalculated and is valid, there's just no reason to update the value

        return modified;
    }

    private synchronized double getCalculatedLimit() {
        final double result;

        if (isCalculatedLimitRecent()) {
            result = this.calculatedLimit;
        } else {
            // I calculated this before, in the managedEvent, during rulesManager loop
            logger.error("failure to calculate limits in getCalculatedLimit for: {}", Generic.objectToString(this));
            result = 0d;
        }

        return result;
    }

    private synchronized boolean updateRunnerExposure() {
        final boolean success;
        for (ManagedRunner managedRunner : this.runners.values()) {
            managedRunner.resetExposure();
        }

//        final OrderMarket orderMarket = Statics.orderCache.getOrderMarket(this.id);
        if (orderMarket == null) { // this is a normal branch, no orders are placed on this market
            // I won't update the exposure in this method, so nothing to be done in this branch
            success = true;
        } else {
            final ArrayList<OrderMarketRunner> orderMarketRunners = orderMarket.getOrderMarketRunners();
            if (orderMarketRunners == null) { // this should never happen
                success = false;
                logger.error("null orderMarketRunners in orderMarket during calculateExposure for: {}", Generic.objectToString(orderMarket));
            } else {
                boolean error = false;
                for (OrderMarketRunner orderMarketRunner : orderMarketRunners) {
                    final RunnerId runnerId = orderMarketRunner.getRunnerId();
                    if (runnerId == null) {
                        logger.error("null runnerId in orderMarketRunner: {}", Generic.objectToString(orderMarketRunner));
                        error = true;
                        break;
                    } else {
                        final ManagedRunner managedRunner = this.runners.get(runnerId);
                        if (managedRunner == null) {
                            logger.error("null managedRunner for runnerId {} in manageMarket: {}", Generic.objectToString(runnerId), Generic.objectToString(this));
                            error = true;
                            break;
                        } else {
                            managedRunner.processOrders(orderMarketRunner);
                        }
                    }
                } // end for
                if (error) {
                    success = false;
                } else { // I won't calculate exposure in this method, so nothing to be done on this branch
                    success = true;
                }
            }
        }

        return success;
    }

    public synchronized void calculateExposure() {
        if (!isSupported()) { // for not supported I can't calculate the exposure
            logger.error("trying to calculateExposure on unSupported managedMarket, nothing will be done: {}", Generic.objectToString(this));
        } else {
            final boolean success = updateRunnerExposure();

            marketMatchedExposure = Double.NaN;
            marketTotalExposure = Double.NaN;
            if (success) {
                this.updateExposureSums();

                for (ManagedRunner managedRunner : this.runners.values()) {
                    if (Double.isNaN(marketMatchedExposure)) {
                        marketMatchedExposure = calculateRunnerMatchedExposure(managedRunner);
                    } else {
                        marketMatchedExposure = Math.max(marketMatchedExposure, calculateRunnerMatchedExposure(managedRunner));
                    }
                    if (Double.isNaN(marketTotalExposure)) {
                        marketTotalExposure = calculateRunnerTotalExposure(managedRunner);
                    } else {
                        marketTotalExposure = Math.max(marketTotalExposure, calculateRunnerTotalExposure(managedRunner));
                    }
                } // end for
                if (Double.isNaN(marketMatchedExposure)) {
                    marketMatchedExposure = 0d;
                }
                if (Double.isNaN(marketTotalExposure)) {
                    marketTotalExposure = 0d;
                }
            } else { // nothing to do, default Double.NaN values will be retained
            }
        }
    }

    private synchronized void updateExposureSums() { // updates matchedBackExposureSum & totalBackExposureSum
        double matchedBackExposureSum = 0d, totalBackExposureSum = 0d;
        for (ManagedRunner managedRunner : this.runners.values()) {
            matchedBackExposureSum += managedRunner.getBackMatchedExposure();
            totalBackExposureSum += managedRunner.getBackTotalExposure();
        }
        this.setMatchedBackExposureSum(matchedBackExposureSum);
        this.setTotalBackExposureSum(totalBackExposureSum);
    }

//    private synchronized void updateIdealBackExposureSum() { // updates idealBackExposureSum
//        double idealBackExposureSum = 0d;
//        for (ManagedRunner managedRunner : this.runners.values()) {
//            idealBackExposureSum += managedRunner.getIdealBackExposure();
//        }
//        this.setIdealBackExposureSum(idealBackExposureSum);
//    }

    private synchronized double calculateRunnerMatchedExposure(final @NotNull ManagedRunner managedRunner) {
        final double exposure = managedRunner.getLayMatchedExposure() + this.matchedBackExposureSum - managedRunner.getBackMatchedExposure();
        final RunnerId runnerId = managedRunner.getRunnerId();
        this.runnerMatchedExposure.put(runnerId, exposure);

        return exposure;
    }

    private synchronized double calculateRunnerTotalExposure(final @NotNull ManagedRunner managedRunner) {
        final double exposure = managedRunner.getLayTotalExposure() + this.totalBackExposureSum - managedRunner.getBackTotalExposure();
        final RunnerId runnerId = managedRunner.getRunnerId();
        this.runnerTotalExposure.put(runnerId, exposure);

        return exposure;
    }

    private synchronized double getRunnerMatchedExposure(final RunnerId runnerId) {
        final Double exposureObject = this.runnerMatchedExposure.get(runnerId);
        final double exposure;
        if (exposureObject == null) {
            logger.error("null exposure during getRunnerMatchedExposure for {} in {}", Generic.objectToString(runnerId), Generic.objectToString(this));
            exposure = 0d;
        } else {
            exposure = exposureObject;
        }

        return exposure;
    }

    private synchronized double getRunnerTotalExposure(final RunnerId runnerId) {
        final Double exposureObject = this.runnerTotalExposure.get(runnerId);
        final double exposure;
        if (exposureObject == null) {
            logger.error("null exposure during getRunnerTotalExposure for {} in {}", Generic.objectToString(runnerId), Generic.objectToString(this));
            exposure = 0d;
        } else {
            exposure = exposureObject;
        }

        return exposure;
    }

    private synchronized boolean checkCancelAllUnmatchedBetsFlag() { // only runs if the AtomicBoolean flag is set, normally when due to an error I can't calculate exposure
        final boolean shouldRun = this.cancelAllUnmatchedBets.getAndSet(false);
        if (shouldRun) {
            cancelAllUnmatchedBets();
        } else { // nothing to be done, flag for cancelling is not set
        }
        return shouldRun;
    }

    private synchronized int cancelAllUnmatchedBets() { // cancel all unmatched bets, don't worry about exposure; generally used when, because of some error, I can't calculate exposure
        int modifications = 0;
        this.cancelAllUnmatchedBets.set(false);
        if (orderMarket == null) { // this is a normal branch, no orders are placed on this market, so nothing to be done in this branch
        } else {
            final @NotNull ArrayList<OrderMarketRunner> orderMarketRunners = orderMarket.getOrderMarketRunners();
            for (OrderMarketRunner orderMarketRunner : orderMarketRunners) {
                if (orderMarketRunner == null) {
                    logger.error("null orderMarketRunner in cancelAllUnmatchedBets for: {}", Generic.objectToString(orderMarket));
                } else {
                    final RunnerId runnerId = orderMarketRunner.getRunnerId();
                    if (runnerId == null) {
                        logger.error("null runnerId in orderMarketRunner: {}", Generic.objectToString(orderMarketRunner));
                    } else {
                        final @NotNull HashMap<String, Order> unmatchedOrders = orderMarketRunner.getUnmatchedOrders();
                        for (Order order : unmatchedOrders.values()) {
                            if (order == null) {
                                logger.error("null order in cancelAllUnmatchedBets for: {}", Generic.objectToString(orderMarket));
                            } else {
                                final Side side = order.getSide();
                                final Double price = order.getP();
                                final Double size = order.getSr();
                                final String betId = order.getId();
                                if (side == null || price == null || size == null || betId == null) {
                                    logger.error("null order attributes in cancelAllUnmatchedBets for: {} {} {} {} {}", side, price, size, betId, Generic.objectToString(order));
                                } else {
                                    modifications += Generic.booleanToInt(order.cancelOrder(this.id, runnerId));
                                }
                            }
                        } // end for
                    }
                }
            } // end for
        }
        return modifications;
    }

    public synchronized boolean isSupported() {
        final boolean result;
        if (this.market == null) {
            result = false;
            logger.error("trying to run managedMarket isSupported without attached market for: {}", Generic.objectToString(this));
        } else {
            final MarketDefinition marketDefinition = this.market.getMarketDefinition();
            if (marketDefinition == null) {
                result = false;
                logger.error("marketDefinition null while run managedMarket isSupported for: {}", Generic.objectToString(this));
            } else {
                final MarketBettingType marketBettingType = marketDefinition.getBettingType();
                final Integer nWinners = marketDefinition.getNumberOfWinners();
                if (MarketBettingType.ODDS.equals(marketBettingType) && nWinners != null && nWinners == 1) {
                    result = true;
                } else {
                    result = false;
                    logger.error("unsupported managedMarket: {}", Generic.objectToString(this));
                }
            }
        }

        return result;
    }

//    private synchronized boolean hasMarketBeenManagedRecently() {
//        final long currentTime = System.currentTimeMillis();
//        return currentTime - this.manageMarketStamp > 5_000L;
//    }

    private synchronized void manageMarketStamp() {
        final long currentTime = System.currentTimeMillis();
        manageMarketStamp(currentTime);
    }

    private synchronized void manageMarketStamp(final long currentTime) {
        this.manageMarketStamp = currentTime;
    }

    // check that the limit bets per hour is not reached; only place bets if it's not reached; error message if limit reached; depending on how close to the limit I am, only orders with certain priority will be placed
    // priority depends on the type of modification and on the amount; some urgent orders might be placed in any case
    // manage market timeStamp; recent is 5 seconds; some non urgent actions that add towards hourly order limit will only be done if non recent, and the stamp will only get updated on this branch
    // the solution I found was to set the manageMarketPeriod in the BetFrequencyLimit class, depending on how close to the hourly limit I am
    public synchronized void manage() {
        final long currentTime = System.currentTimeMillis();
        final long timeSinceLastManageMarketStamp = currentTime - this.manageMarketStamp;
        if (timeSinceLastManageMarketStamp >= Statics.safetyLimits.speedLimit.getManageMarketPeriod(this.calculatedLimit)) {
            manageMarketStamp(currentTime);
            attachMarket();
            if (market != null) {
                attachOrderMarket();
                if (!isSupported()) { // for not supported I can't calculate the limit
                    logger.error("trying to manage unSupported managedMarket, nothing will be done: {}", Generic.objectToString(this));
                } else {
                    if (checkCancelAllUnmatchedBetsFlag()) { // all unmatched bets have been canceled already, not much more to be done
                    } else {
//                    final double calculatedLimit = this.getCalculatedLimit();
                        int exposureHasBeenModified = 0;
                        for (ManagedRunner runner : this.runners.values()) {
                            exposureHasBeenModified += runner.calculateOdds(this.id, calculatedLimit); // also removes unmatched orders at worse odds, and hardToReachOrders
                        }
                        if (exposureHasBeenModified > 0) {
                            calculateExposure();
                            exposureHasBeenModified = 0;
                        } else { // no need to calculateExposure
                        }

                        if (isMarketAlmostLive()) {
                            removeExposure();
                        } else {
                            for (ManagedRunner runner : this.runners.values()) {
                                exposureHasBeenModified += runner.checkRunnerLimits();
                            }
                            if (exposureHasBeenModified > 0) {
                                calculateExposure();
                                exposureHasBeenModified = 0;
                            } else { // no need to calculateExposure
                            }

                            exposureHasBeenModified += useTheNewLimit();
                        }
                    }
                }
            } else { // error message was logged elsewhere, nothing to be done
            }
        } else { // not enough time has passed since last manage, nothing to be done
        }
    }

    @Override
    public synchronized boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ManagedMarket that = (ManagedMarket) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(id);
    }
}
