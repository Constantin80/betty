package info.fmro.betty.stream.cache.util;

import info.fmro.betty.logic.ManagedMarket;
import info.fmro.betty.logic.ManagedRunner;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.cache.market.Market;
import info.fmro.betty.stream.cache.order.OrderMarket;
import info.fmro.betty.stream.cache.order.OrderMarketRunner;
import info.fmro.shared.stream.enums.Side;
import info.fmro.shared.utility.Formulas;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings({"OverlyComplexClass", "UtilityClass"})
public final class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    @Contract(pure = true)
    private Utils() {
    }

    public static void calculateMarketLimits(final double maxTotalLimit, @NotNull final Iterable<? extends ManagedMarket> marketsSet, final boolean shouldCalculateExposure, final boolean marketLimitsCanBeIncreased) {
        double totalMatchedExposure = 0d, totalExposure = 0d, sumOfMaxMarketLimits = 0d;
        final Collection<ManagedMarket> marketsWithErrorCalculatingExposure = new HashSet<>(1), marketsWithExposureHigherThanTheirMaxLimit = new HashSet<>(1);
        for (final ManagedMarket managedMarket : marketsSet) {
            if (shouldCalculateExposure) {
                managedMarket.calculateExposure();
            } else { // no need to calculate exposure, it was just calculated previously
            }
            final double maxMarketLimit = managedMarket.getMaxMarketLimit();

            sumOfMaxMarketLimits += maxMarketLimit;
            if (managedMarket.defaultExposureValuesExist()) {
                totalMatchedExposure += maxMarketLimit;
                totalExposure += maxMarketLimit;
                marketsWithErrorCalculatingExposure.add(managedMarket); // this market should have all its unmatched bets cancelled, nothing else as I don't know the exposure
            } else {
                final double marketMatchedExposure = managedMarket.getMarketMatchedExposure();
                totalMatchedExposure += marketMatchedExposure;
                final double marketTotalExposure = managedMarket.getMarketTotalExposure();
                if (marketTotalExposure > maxMarketLimit) {
                    marketsWithExposureHigherThanTheirMaxLimit.add(managedMarket); // if total exposure > maxMarketLimit, reduce the exposure
                    totalExposure += Math.max(maxMarketLimit, marketMatchedExposure);
                    logger.error("managedMarket with total exposure {} higher than maxLimit {}: {}", marketTotalExposure, maxMarketLimit, Generic.objectToString(managedMarket));
                } else { // normal case
                    totalExposure += marketTotalExposure;
                }
            }
        } // end for

        @SuppressWarnings("unused") final double availableTotalExposure = maxTotalLimit - totalExposure; // can be positive or negative, not used for now, as I use the ConsideringOnlyMatched variant
        final double availableExposureInTheMarkets = sumOfMaxMarketLimits - totalExposure; // should be positive, else this might be an error
        final double availableTotalExposureConsideringOnlyMatched = maxTotalLimit - totalMatchedExposure; // can be positive or negative
        final double availableExposureInTheMarketsConsideringOnlyMatched = sumOfMaxMarketLimits - totalMatchedExposure; // should always be positive, else this is an error

        for (final ManagedMarket managedMarket : marketsWithErrorCalculatingExposure) {
            managedMarket.cancelAllUnmatchedBets.set(true);
        }
        for (final ManagedMarket managedMarket : marketsWithExposureHigherThanTheirMaxLimit) {
//            final double totalExposure = managedMarket.getMarketTotalExposure();
            final double maxLimit = managedMarket.getMaxMarketLimit();
//            final double reducedExposure = totalExposure - maxLimit;
//            availableExposureInTheMarkets += reducedExposure;
            // I don't think modifying the availableExposureInTheMarkets is needed, as the maxMarketLimit was already used when calculating in the case of these ExposureHigherThanTheirMaxLimit markets

            managedMarket.setCalculatedLimit(maxLimit, marketLimitsCanBeIncreased);
        }
        if (sumOfMaxMarketLimits <= maxTotalLimit) { // nothing to do
        } else if (totalMatchedExposure <= maxTotalLimit && availableExposureInTheMarketsConsideringOnlyMatched >= 0d) {
            if (availableExposureInTheMarkets < 0d) { // availableTotalExposure will be negative, guaranteed
                logger.error("availableExposureInTheMarkets negative in calculateMarketLimits: {} {} for: {} {} {} {}", sumOfMaxMarketLimits, totalExposure, maxTotalLimit, shouldCalculateExposure, marketLimitsCanBeIncreased,
                             Generic.objectToString(marketsSet));
            } else { // normal branch, nothing to do here
            }

            final double proportionOfAvailableMarketExposureThatWillBeUsed = availableExposureInTheMarketsConsideringOnlyMatched == 0d || availableTotalExposureConsideringOnlyMatched > availableExposureInTheMarketsConsideringOnlyMatched ? 1d :
                                                                             availableTotalExposureConsideringOnlyMatched / availableExposureInTheMarketsConsideringOnlyMatched;
            for (final ManagedMarket managedMarket : marketsSet) {
                if (marketsWithErrorCalculatingExposure.contains(managedMarket)) { // nothing to do, all unmatched bets on this market were previously been canceled
                } else { // calculatedLimit = (maxMarketLimit - matchedExposure) / proportionOfAvailableMarketExposureThatWillBeUsed
                    final double maxMarketLimit = managedMarket.getMaxMarketLimit();
                    final double matchedExposure = managedMarket.getMarketMatchedExposure();
                    final double calculatedLimit = (maxMarketLimit - matchedExposure) * proportionOfAvailableMarketExposureThatWillBeUsed + matchedExposure;
                    managedMarket.setCalculatedLimit(calculatedLimit, marketLimitsCanBeIncreased);
                }
            } // end for
        } else {
            logger.error("bad amounts in calculateMarketLimits: {} {} for: {} {} {} {}", totalMatchedExposure, availableExposureInTheMarketsConsideringOnlyMatched, maxTotalLimit, shouldCalculateExposure, marketLimitsCanBeIncreased,
                         Generic.objectToString(marketsSet));
            // minimize exposure in all markets, except those with error in calculating the exposure, where all unmatched will be canceled
            final double proportionMatchedExposureWithinLimit;
            if (totalMatchedExposure == 0d) {
                logger.error("bad totalMatchedExposure in calculateMarketLimits: {}", totalMatchedExposure);
                proportionMatchedExposureWithinLimit = 1d;
            } else {
                proportionMatchedExposureWithinLimit = maxTotalLimit / totalMatchedExposure;
            }
            for (final ManagedMarket managedMarket : marketsSet) {
                if (marketsWithErrorCalculatingExposure.contains(managedMarket)) { // nothing to do, all unmatched bets on this market were previously been canceled
                } else {
                    final double matchedExposure = managedMarket.getMarketMatchedExposure();
                    final double maxLimit = managedMarket.getMaxMarketLimit();
                    managedMarket.setCalculatedLimit(Math.min(matchedExposure * proportionMatchedExposureWithinLimit, maxLimit), marketLimitsCanBeIncreased);
                }
            } // end for
        }
    }

    public static Market getMarket(final String marketId) {
        return Statics.marketCache.getMarket(marketId);
    }

    public static OrderMarket getOrderMarket(final String marketId) {
        return Statics.orderCache.getOrderMarket(marketId);
    }

    public static List<Double> getExposureToBePlacedForTwoWayMarket(@NotNull final ManagedRunner firstRunner, @NotNull final ManagedRunner secondRunner, @NotNull final OrderMarketRunner firstOrderRunner, @NotNull final OrderMarketRunner secondOrderRunner,
                                                                    @NotNull final List<Side> sideList, final double excessMatchedExposure) {
        final List<Double> existingTempExposures, existingNonMatchedExposures, nonMatchedExposureLimitList, toBeUsedOdds, resultList;
        if (sideList.size() != 2 || sideList.contains(null)) {
            logger.error("bogus sideList for getExposureToBePlacedForTwoWayMarket: {} {} {} {} {} {}", Generic.objectToString(sideList), Generic.objectToString(firstRunner), Generic.objectToString(secondRunner), Generic.objectToString(firstOrderRunner),
                         Generic.objectToString(secondOrderRunner), excessMatchedExposure);
            resultList = List.of(0d, 0d);
        } else {
            final @NotNull Side firstSide = sideList.get(0), secondSide = sideList.get(1);
            if (firstSide == Side.B && secondSide == Side.L) {
                existingTempExposures = List.of(firstOrderRunner.getTempBackExposure(), secondOrderRunner.getTempLayExposure());
                existingNonMatchedExposures = List.of(firstOrderRunner.getUnmatchedBackExposure() + firstOrderRunner.getTempBackExposure(), secondOrderRunner.getUnmatchedLayExposure() + secondOrderRunner.getTempLayExposure());
                nonMatchedExposureLimitList = List.of(firstRunner.getBackAmountLimit() - firstOrderRunner.getMatchedBackExposure(), secondRunner.getLayAmountLimit() - secondOrderRunner.getMatchedLayExposure());
                toBeUsedOdds = List.of(firstRunner.getToBeUsedBackOdds(), secondRunner.getToBeUsedLayOdds());
                resultList = getExposureToBePlacedForTwoWayMarket(existingTempExposures, existingNonMatchedExposures, nonMatchedExposureLimitList, sideList, toBeUsedOdds, excessMatchedExposure);
            } else if (firstSide == Side.L && secondSide == Side.B) {
                existingTempExposures = List.of(firstOrderRunner.getTempLayExposure(), secondOrderRunner.getTempBackExposure());
                existingNonMatchedExposures = List.of(firstOrderRunner.getUnmatchedLayExposure() + firstOrderRunner.getTempLayExposure(), secondOrderRunner.getUnmatchedBackExposure() + secondOrderRunner.getTempBackExposure());
                nonMatchedExposureLimitList = List.of(firstRunner.getLayAmountLimit() - firstOrderRunner.getMatchedLayExposure(), secondRunner.getBackAmountLimit() - secondOrderRunner.getMatchedBackExposure());
                toBeUsedOdds = List.of(firstRunner.getToBeUsedLayOdds(), secondRunner.getToBeUsedBackOdds());
                resultList = getExposureToBePlacedForTwoWayMarket(existingTempExposures, existingNonMatchedExposures, nonMatchedExposureLimitList, sideList, toBeUsedOdds, excessMatchedExposure);
            } else {
                logger.error("bogus sides for getExposureToBePlacedForTwoWayMarket: {} {} {} {} {} {}", Generic.objectToString(sideList), Generic.objectToString(firstRunner), Generic.objectToString(secondRunner), Generic.objectToString(firstOrderRunner),
                             Generic.objectToString(secondOrderRunner), excessMatchedExposure);
                resultList = List.of(0d, 0d);
            }
        }
        return resultList;
    }

    private static List<Double> getExposureToBePlacedForTwoWayMarket(@NotNull final List<Double> existingTempExposures, @NotNull final List<Double> existingNonMatchedExposures, @NotNull final List<Double> nonMatchedExposureLimitList,
                                                                     @NotNull final List<Side> sideList, @NotNull final List<Double> toBeUsedOdds, final double excessMatchedExposure) {
        // nonMatchedExposure should include the tempExposure
        // positive or negative result, depending on existing unmatchedExposure; this differs from the following overloaded methods, when result can only be positive and does not depend on unmatchedExposure
        final List<Double> resultList;
        if (existingTempExposures.size() != 2 || existingNonMatchedExposures.size() != 2) {
            logger.error("bogus existingTempExposures or existingNonMatchedExposures for getExposureToBePlacedForTwoWayMarket: {} {} {} {} {} {}", Generic.objectToString(existingTempExposures), Generic.objectToString(existingNonMatchedExposures),
                         Generic.objectToString(nonMatchedExposureLimitList), Generic.objectToString(sideList), Generic.objectToString(toBeUsedOdds), excessMatchedExposure);
            resultList = List.of(0d, 0d);
        } else {
            final List<Double> exposureWithLimits = getExposureToBePlacedForTwoWayMarket(nonMatchedExposureLimitList, sideList, toBeUsedOdds, excessMatchedExposure);
            final double firstExposure = exposureWithLimits.get(0), secondExposure = exposureWithLimits.get(1);
            final double firstNonMatchedExposure = existingNonMatchedExposures.get(0), secondNonMatchedExposure = existingNonMatchedExposures.get(1);
            final double firstTempExposure = existingTempExposures.get(0), secondTempExposure = existingTempExposures.get(1);
            if (firstTempExposure > firstNonMatchedExposure || secondTempExposure > secondNonMatchedExposure) {
                logger.error("non inclusive nonMatchedExposures for: {} {} {} {} {} {}", Generic.objectToString(existingTempExposures), Generic.objectToString(existingNonMatchedExposures), Generic.objectToString(nonMatchedExposureLimitList),
                             Generic.objectToString(sideList), Generic.objectToString(toBeUsedOdds), excessMatchedExposure);
                resultList = List.of(-firstNonMatchedExposure, -secondNonMatchedExposure);
            } else if (firstExposure == 0d && secondExposure == 0d) { // will cancel all unmatched
                resultList = List.of(firstTempExposure - firstNonMatchedExposure, secondTempExposure - secondNonMatchedExposure);
            } else {
                final double totalNonMatchedExposure = firstNonMatchedExposure + secondNonMatchedExposure, totalTempExposure = firstTempExposure + secondTempExposure;
                final double totalExposure = firstExposure + secondExposure;

                if (totalExposure >= totalNonMatchedExposure) { // I'll place more orders
                    if (firstExposure < firstNonMatchedExposure) {
                        resultList = List.of(0d, totalExposure - totalNonMatchedExposure);
                    } else if (secondExposure < secondNonMatchedExposure) {
                        resultList = List.of(totalExposure - totalNonMatchedExposure, 0d);
                    } else {
                        resultList = List.of(firstExposure - firstNonMatchedExposure, secondExposure - secondNonMatchedExposure);
                    }
                } else { // I need to cancel some orders
                    if (totalExposure >= totalTempExposure) { // some unmatchedExposure will remain
                        if (firstExposure < firstTempExposure) {
                            resultList = List.of(firstTempExposure - firstNonMatchedExposure, totalExposure - secondNonMatchedExposure - firstTempExposure);
                        } else if (secondExposure < secondTempExposure) {
                            resultList = List.of(totalExposure - firstNonMatchedExposure - secondTempExposure, secondTempExposure - secondNonMatchedExposure);
                        } else {
                            resultList = List.of(firstExposure - firstNonMatchedExposure, secondExposure - secondNonMatchedExposure);
                        }
                    } else { // no unmatchedExposure can be left
                        resultList = List.of(firstTempExposure - firstNonMatchedExposure, secondTempExposure - secondNonMatchedExposure);
                    }
                }
            }
        }
        return resultList;
    }

    private static List<Double> getExposureToBePlacedForTwoWayMarket(@NotNull final List<Double> nonMatchedExposureLimitList, @NotNull final List<Side> sideList, @NotNull final List<Double> toBeUsedOdds, final double excessMatchedExposure) {
        // I apply the limits
        final List<Double> resultList;
        if (nonMatchedExposureLimitList.size() == 2) {
            final List<Double> exposureWithoutLimits = getExposureToBePlacedForTwoWayMarket(sideList, toBeUsedOdds, excessMatchedExposure);
            final double firstExposureWithoutLimit = exposureWithoutLimits.get(0), secondExposureWithoutLimit = exposureWithoutLimits.get(1);
            if (firstExposureWithoutLimit == 0d && secondExposureWithoutLimit == 0d) {
                resultList = exposureWithoutLimits;
            } else {
                final double firstExposureLimit = nonMatchedExposureLimitList.get(0), secondExposureLimit = nonMatchedExposureLimitList.get(1);
                final double totalExposureLimit = firstExposureLimit + secondExposureLimit, totalExposure = firstExposureWithoutLimit + secondExposureWithoutLimit;
                if (totalExposure > totalExposureLimit) {
                    resultList = List.of(firstExposureLimit, secondExposureLimit);
                } else {
                    if (firstExposureWithoutLimit > firstExposureLimit) {
                        resultList = List.of(firstExposureLimit, totalExposure - firstExposureLimit);
                    } else if (secondExposureWithoutLimit > secondExposureLimit) {
                        resultList = List.of(totalExposure - secondExposureLimit, secondExposureLimit);
                    } else {
                        resultList = List.of(firstExposureWithoutLimit, secondExposureWithoutLimit);
                    }
                }
            }
        } else {
            logger.error("bogus nonMatchedExposureLimitList for getExposureToBePlacedForTwoWayMarket: {} {} {} {}", Generic.objectToString(nonMatchedExposureLimitList), Generic.objectToString(sideList), Generic.objectToString(toBeUsedOdds),
                         excessMatchedExposure);
            resultList = List.of(0d, 0d);
        }
        return resultList;
    }

    private static List<Double> getExposureToBePlacedForTwoWayMarket(@NotNull final List<Side> sideList, @NotNull final List<Double> toBeUsedOdds, final double excessMatchedExposure) {
        // I'm getting the raw excessMatchedExposure, without considering existing exposure and limits
        // the factors are the price of toBeUsedOdds, and which of the toBeUsedOdds is more profitable; those two should be enough for now; also lay bets should be given slight priority over back bets, as other gamblers like to back rather than lay
        final List<Double> resultList;
        if (sideList.size() != 2 || toBeUsedOdds.size() != 2 || excessMatchedExposure <= 0d || sideList.contains(null)) {
            logger.error("bogus arguments for getExposureToBePlacedForTwoWayMarket: {} {} {}", Generic.objectToString(sideList), Generic.objectToString(toBeUsedOdds), excessMatchedExposure);
            resultList = List.of(0d, 0d);
        } else {
            final double firstToBeUsedOdds = toBeUsedOdds.get(0), secondToBeUsedOdds = toBeUsedOdds.get(1);
            final Side firstSide = sideList.get(0), secondSide = sideList.get(1);
            if (!info.fmro.shared.utility.Formulas.oddsAreUsable(firstToBeUsedOdds) || !info.fmro.shared.utility.Formulas.oddsAreUsable(secondToBeUsedOdds) || firstSide == secondSide) {
                logger.error("bogus internal arguments for getExposureToBePlacedForTwoWayMarket: {} {} {}", Generic.objectToString(sideList), Generic.objectToString(toBeUsedOdds), excessMatchedExposure);
                resultList = List.of(0d, 0d);
            } else {
                final double firstSmallerOddsBonus = Math.sqrt(Math.sqrt((secondToBeUsedOdds - 1d) / (firstToBeUsedOdds - 1d))); // double sqrt should be well balanced exposure, a limited advantage for the smaller odds
                final double firstAmountLayBonus = firstSide == Side.L ? 1.25d : .8d;

                final double firstAmountProfitability = firstSide == Side.B ? firstToBeUsedOdds - 1d : 1d / (firstToBeUsedOdds - 1d);
                final double secondAmountProfitability = secondSide == Side.B ? secondToBeUsedOdds - 1d : 1d / (secondToBeUsedOdds - 1d);
                final double firstAmountProfitabilityBonus;
                if (firstAmountProfitability > secondAmountProfitability) {
                    firstAmountProfitabilityBonus = 2d;
                } else //noinspection FloatingPointEquality
                    if (firstAmountProfitability == secondAmountProfitability) {
                        firstAmountProfitabilityBonus = 1d;
                    } else {
                        firstAmountProfitabilityBonus = .5d;
                    }

                final double firstOverSecondFinalProportion = firstSmallerOddsBonus * firstAmountLayBonus * firstAmountProfitabilityBonus;
                final double secondExposure = excessMatchedExposure / (firstOverSecondFinalProportion + 1d);
                final double firstExposure = secondExposure * firstOverSecondFinalProportion;

                resultList = List.of(firstExposure, secondExposure);
            }
        }
        return resultList;
    }

    public static List<Double> getAmountsToBePlacedForTwoWayMarket(@NotNull final ManagedRunner firstRunner, @NotNull final ManagedRunner secondRunner, @NotNull final OrderMarketRunner firstOrderRunner, @NotNull final OrderMarketRunner secondOrderRunner,
                                                                   @NotNull final List<Side> sideList, final double availableLimit) {
        final List<Double> existingUnmatchedExposures, existingNonMatchedExposures, availableLimitList, toBeUsedOdds, resultList;
        if (sideList.size() != 2 || sideList.contains(null)) {
            logger.error("bogus sideList for getAmountsToBePlacedForTwoWayMarket: {} {} {} {} {} {}", Generic.objectToString(sideList), Generic.objectToString(firstRunner), Generic.objectToString(secondRunner), Generic.objectToString(firstOrderRunner),
                         Generic.objectToString(secondOrderRunner), availableLimit);
            resultList = List.of(0d, 0d);
        } else {
            final @NotNull Side firstSide = sideList.get(0), secondSide = sideList.get(1);
            if (firstSide == Side.B && secondSide == Side.L) {
                existingUnmatchedExposures = List.of(firstOrderRunner.getUnmatchedBackExposure(), secondOrderRunner.getUnmatchedLayExposure());
                existingNonMatchedExposures = List.of(firstOrderRunner.getUnmatchedBackExposure() + firstOrderRunner.getTempBackExposure(), secondOrderRunner.getUnmatchedLayExposure() + secondOrderRunner.getTempLayExposure());
                availableLimitList = List.of(firstRunner.getBackAmountLimit() - firstOrderRunner.getMatchedBackExposure() - firstOrderRunner.getUnmatchedBackExposure() - firstOrderRunner.getTempBackExposure(),
                                             secondRunner.getLayAmountLimit() - secondOrderRunner.getMatchedLayExposure() - secondOrderRunner.getUnmatchedLayExposure() - secondOrderRunner.getTempLayExposure());
                toBeUsedOdds = List.of(firstRunner.getToBeUsedBackOdds(), secondRunner.getToBeUsedLayOdds());
                resultList = getAmountsToBePlacedForTwoWayMarket(existingUnmatchedExposures, existingNonMatchedExposures, availableLimitList, sideList, toBeUsedOdds, availableLimit);
            } else if (firstSide == Side.L && secondSide == Side.B) {
                existingUnmatchedExposures = List.of(firstOrderRunner.getUnmatchedLayExposure(), secondOrderRunner.getUnmatchedBackExposure());
                existingNonMatchedExposures = List.of(firstOrderRunner.getUnmatchedLayExposure() + firstOrderRunner.getTempLayExposure(), secondOrderRunner.getUnmatchedBackExposure() + secondOrderRunner.getTempBackExposure());
                availableLimitList = List.of(firstRunner.getLayAmountLimit() - firstOrderRunner.getMatchedLayExposure() - firstOrderRunner.getUnmatchedLayExposure() - firstOrderRunner.getTempLayExposure(),
                                             secondRunner.getBackAmountLimit() - secondOrderRunner.getMatchedBackExposure() - secondOrderRunner.getUnmatchedBackExposure() - secondOrderRunner.getTempBackExposure());
                toBeUsedOdds = List.of(firstRunner.getToBeUsedLayOdds(), secondRunner.getToBeUsedBackOdds());
                resultList = getAmountsToBePlacedForTwoWayMarket(existingUnmatchedExposures, existingNonMatchedExposures, availableLimitList, sideList, toBeUsedOdds, availableLimit);
            } else {
                logger.error("bogus sides for getAmountsToBePlacedForTwoWayMarket: {} {} {} {} {} {}", Generic.objectToString(sideList), Generic.objectToString(firstRunner), Generic.objectToString(secondRunner), Generic.objectToString(firstOrderRunner),
                             Generic.objectToString(secondOrderRunner), availableLimit);
                resultList = List.of(0d, 0d);
            }
        }
        return resultList;
    }

    private static List<Double> getAmountsToBePlacedForTwoWayMarket(@NotNull final List<Double> existingUnmatchedExposures, @NotNull final List<Double> existingNonMatchedExposures, @NotNull final List<Double> availableLimitList,
                                                                    @NotNull final List<Side> sideList, @NotNull final List<Double> toBeUsedOdds, final double availableLimit) {
        // I apply the limits
        final List<Double> resultList;
        if (availableLimitList.size() == 2) {
            final List<Double> exposureWithoutLimits = getAmountsToBePlacedForTwoWayMarket(existingUnmatchedExposures, existingNonMatchedExposures, sideList, toBeUsedOdds, availableLimit);
            final double firstExposureWithoutLimit = exposureWithoutLimits.get(0), secondExposureWithoutLimit = exposureWithoutLimits.get(1);
            if (firstExposureWithoutLimit <= 0d && secondExposureWithoutLimit <= 0d) { // negative or zero, so won't break limits
                resultList = exposureWithoutLimits;
            } else { // positive
                final double firstExposureLimit = availableLimitList.get(0), secondExposureLimit = availableLimitList.get(1);
                final double totalExposureLimit = firstExposureLimit + secondExposureLimit, totalNewExposure = firstExposureWithoutLimit + secondExposureWithoutLimit;
                if (totalNewExposure > totalExposureLimit) {
                    resultList = List.of(firstExposureLimit, secondExposureLimit);
                } else {
                    if (firstExposureWithoutLimit > firstExposureLimit) {
                        resultList = List.of(firstExposureLimit, totalNewExposure - firstExposureLimit);
                    } else if (secondExposureWithoutLimit > secondExposureLimit) {
                        resultList = List.of(totalNewExposure - secondExposureLimit, secondExposureLimit);
                    } else {
                        resultList = exposureWithoutLimits;
                    }
                }
            }
        } else {
            logger.error("bogus availableLimitList for getAmountsToBePlacedForTwoWayMarket: {} {} {} {}", Generic.objectToString(availableLimitList), Generic.objectToString(sideList), Generic.objectToString(toBeUsedOdds), availableLimit);
            resultList = List.of(0d, 0d);
        }
        return resultList;
    }

    private static List<Double> getAmountsToBePlacedForTwoWayMarket(@NotNull final List<Double> existingUnmatchedExposures, @NotNull final List<Double> existingNonMatchedExposures, @NotNull final List<Side> sideList,
                                                                    @NotNull final List<Double> toBeUsedOdds, final double availableLimit) {
        // I'm getting the raw availableLimit, without considering existing exposure and limits
        // the factors are the price of toBeUsedOdds, and which of the toBeUsedOdds is more profitable; those two should be enough for now; also lay bets should be given slight priority over back bets, as other gamblers like to back rather than lay
        final List<Double> resultList;
        if (sideList.size() != 2 || toBeUsedOdds.size() != 2 || sideList.contains(null) || availableLimit == 0d) {
            logger.error("bogus arguments for getAmountsToBePlacedForTwoWayMarket: {} {} {} {} {}", Generic.objectToString(existingUnmatchedExposures), Generic.objectToString(existingNonMatchedExposures), Generic.objectToString(sideList),
                         Generic.objectToString(toBeUsedOdds), availableLimit);
            resultList = List.of(0d, 0d);
        } else {
            final double firstToBeUsedOdds = toBeUsedOdds.get(0), secondToBeUsedOdds = toBeUsedOdds.get(1);
            final Side firstSide = sideList.get(0), secondSide = sideList.get(1);
            if (!info.fmro.shared.utility.Formulas.oddsAreUsable(firstToBeUsedOdds) || !Formulas.oddsAreUsable(secondToBeUsedOdds) || firstSide == secondSide) {
                logger.error("bogus internal arguments for getAmountsToBePlacedForTwoWayMarket: {} {} {}", Generic.objectToString(sideList), Generic.objectToString(toBeUsedOdds), availableLimit);
                resultList = List.of(0d, 0d);
            } else {
                final double firstSmallerOddsBonus = Math.sqrt(Math.sqrt((secondToBeUsedOdds - 1d) / (firstToBeUsedOdds - 1d))); // double sqrt should be well balanced exposure, a limited advantage for the smaller odds
                final double firstAmountLayBonus = firstSide == Side.L ? 1.25d : .8d;

                final double firstAmountProfitability = firstSide == Side.B ? firstToBeUsedOdds - 1d : 1d / (firstToBeUsedOdds - 1d);
                final double secondAmountProfitability = secondSide == Side.B ? secondToBeUsedOdds - 1d : 1d / (secondToBeUsedOdds - 1d);
                final double firstAmountProfitabilityBonus;
                if (firstAmountProfitability > secondAmountProfitability) {
                    firstAmountProfitabilityBonus = 2d;
                } else //noinspection FloatingPointEquality
                    if (firstAmountProfitability == secondAmountProfitability) {
                        firstAmountProfitabilityBonus = 1d;
                    } else {
                        firstAmountProfitabilityBonus = .5d;
                    }

                final double firstOverSecondFinalProportion = firstSmallerOddsBonus * firstAmountLayBonus * firstAmountProfitabilityBonus;
                final double firstUnmatchedExposure = existingUnmatchedExposures.get(0), secondUnmatchedExposure = existingUnmatchedExposures.get(1), totalUnmatchedExposure = firstUnmatchedExposure + secondUnmatchedExposure,
                        firstNonMatchedExposure = existingNonMatchedExposures.get(0), secondNonMatchedExposure = existingNonMatchedExposures.get(1);
                final double addExposureOnSecond, addExposureOnFirst;
                if (availableLimit > 0d) {
                    double resultingNonMatchedExposureOnSecond = (availableLimit + firstNonMatchedExposure + secondNonMatchedExposure) / (firstOverSecondFinalProportion + 1d);
                    resultingNonMatchedExposureOnSecond = Math.max(secondNonMatchedExposure, resultingNonMatchedExposureOnSecond);
                    resultingNonMatchedExposureOnSecond = Math.min(secondNonMatchedExposure + availableLimit, resultingNonMatchedExposureOnSecond);
                    addExposureOnSecond = resultingNonMatchedExposureOnSecond - secondNonMatchedExposure;
                    addExposureOnFirst = availableLimit - addExposureOnSecond;
                } else { // < 0d
                    if (-availableLimit >= totalUnmatchedExposure) {
                        addExposureOnSecond = -secondUnmatchedExposure;
                        addExposureOnFirst = -firstUnmatchedExposure;
                    } else { // some unmatched exposure will be left
                        double resultingUnmatchedExposureOnSecond = (availableLimit + firstUnmatchedExposure + secondUnmatchedExposure) / (firstOverSecondFinalProportion + 1d);
                        resultingUnmatchedExposureOnSecond = Math.min(secondUnmatchedExposure, resultingUnmatchedExposureOnSecond);
                        addExposureOnSecond = resultingUnmatchedExposureOnSecond - secondUnmatchedExposure;
                        addExposureOnFirst = availableLimit - addExposureOnSecond;
                    }
                }
                resultList = List.of(addExposureOnFirst, addExposureOnSecond);
            }
        }
        return resultList;
    }

}
