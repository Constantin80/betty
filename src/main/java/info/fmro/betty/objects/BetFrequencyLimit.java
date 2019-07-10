package info.fmro.betty.objects;

import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class BetFrequencyLimit
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(BetFrequencyLimit.class);
    private static final long serialVersionUID = 1110075859608214163L;
    public static final long maxManageMarketPeriod = Generic.MINUTE_LENGTH_MILLISECONDS * 10L; // manage once every 5 minutes, maximum period possible
    public static final long minManageMarketPeriod = 1_000L; // throttle protection
    public static final long regularManageMarketPeriod = 30_000L; // regular period, which formulas then modify to ger the calculated period
    public static final int limitPerHour = 1_000;
    private long lastOrderStamp;
    private int nOrdersSinceReset;

    synchronized void copyFrom(final BetFrequencyLimit other) {
        if (other == null) {
            logger.error("null other in copyFrom for: {}", Generic.objectToString(this));
        } else {
            this.lastOrderStamp = other.lastOrderStamp;
            this.nOrdersSinceReset = other.nOrdersSinceReset;
        }
    }

    public synchronized int getNOrdersSinceReset() {
        return this.nOrdersSinceReset;
    }

    //    public synchronized boolean limitReached() {
//        final boolean limitReached;
//
//        if (this.nOrdersSinceReset < limitPerHour) {
//            limitReached = false;
//        } else {
//            if (checkNeedsReset()) {
//                limitReached = false;
//            } else {
//                limitReached = true;
//            }
//        }
//
//        return limitReached;
//    }

    synchronized long getManageMarketPeriod(final double marketCalculatedLimit) { // depends on how close I am to reaching the limit, and it should never allow reaching the limit
        final long manageMarketPeriod;
        final double totalAccountLimit = Statics.safetyLimits.getTotalLimit();
        if (totalAccountLimit <= 0d || marketCalculatedLimit <= 0d) {
            manageMarketPeriod = maxManageMarketPeriod;
        } else {
            final double proportionOfAccountLimitAllocatedToMarket = Math.min(marketCalculatedLimit / totalAccountLimit, 1d);
            if (marketCalculatedLimit > totalAccountLimit) {
                logger.error("bogus limits in getManageMarketPeriod: {} {}", marketCalculatedLimit, totalAccountLimit);
            } else { // no error, won't print anything
            }

            final double proportionOfLimitReached = this.nOrdersSinceReset / (limitPerHour * .9d);
            final double proportionThatHasPassedFromCurrentHour = proportionThatHasPassedFromCurrentHour();
//            if (proportionOfLimitReached >= proportionThatHasPassedFromCurrentHour) { // this includes the case when proportionThatHasPassedFromCurrentHour == 0d, so I don't need to check further down, before the division
//                checkNeedsReset();
//                //noinspection NumericCastThatLosesPrecision
//                manageMarketPeriod = Math.min(maxManageMarketPeriod, Math.max(minManageMarketPeriod, (long) (regularManageMarketPeriod / proportionOfAccountLimitAllocatedToMarket)));
//            } else { // proportionOfLimitReached < proportionThatHasPassedFromCurrentHour
            final double proportionOfLimitReachedFromCurrentHourSegment = Math.max(1d, proportionOfLimitReached / proportionThatHasPassedFromCurrentHour);
            //noinspection FloatingPointEquality
            if (proportionOfLimitReachedFromCurrentHourSegment == 1d) {
                checkNeedsReset();
            } else { // no need for check, as check will happen when new orders are added
            }
            //noinspection NumericCastThatLosesPrecision
            manageMarketPeriod =
                    Math.min(maxManageMarketPeriod, Math.max(minManageMarketPeriod,
                                                             (long) (regularManageMarketPeriod *
                                                                     StrictMath.pow(proportionOfLimitReachedFromCurrentHourSegment, 2d) /
                                                                     StrictMath.pow(proportionOfAccountLimitAllocatedToMarket, proportionOfLimitReachedFromCurrentHourSegment))));
            // formula looks fine, I might test it with different values and see if I like the results
//            }
        }

        return manageMarketPeriod;
    }

//    public synchronized boolean limitReached() {
//        final double initialProportionOfNoConcern = 1d, proportionOfFullConcern = 1d, proportionOfLimitVersusHourSafetyMargin = -1d;
//        return checkLimitReached(initialProportionOfNoConcern, proportionOfFullConcern, proportionOfLimitVersusHourSafetyMargin);
//    }
//
//    public synchronized boolean closeToLimitReached() {
//        final double initialProportionOfNoConcern = .1d, proportionOfFullConcern = .95d, proportionOfLimitVersusHourSafetyMargin = .03d;
//        return checkLimitReached(initialProportionOfNoConcern, proportionOfFullConcern, proportionOfLimitVersusHourSafetyMargin);
//    }
//
//    private synchronized boolean checkLimitReached(final double initialProportionOfNoConcern, final double proportionOfFullConcern, final double proportionOfLimitVersusHourSafetyMargin) {
//        final boolean limitReached;
//        final double proportionOfLimitReached = (double) this.nOrdersSinceReset / limitPerHour;
//
//        if (proportionOfLimitReached < initialProportionOfNoConcern) {
//            limitReached = false;
//        } else {
//            final double proportionThatHasPassedFromCurrentHour = proportionThatHasPassedFromCurrentHour();
//            // this doesn't include the beginning of the hour, then I'll get on the first branch: proportionOfLimitReached < initialProportionOfNoConcern
//            limitReached = (!(proportionOfLimitReached < proportionOfFullConcern) || !(proportionOfLimitReached + proportionOfLimitVersusHourSafetyMargin < proportionThatHasPassedFromCurrentHour)) && !checkNeedsReset();
//        }
//
//        return limitReached;
//    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized boolean checkNeedsReset() {
        final long currentTime = System.currentTimeMillis();
        return checkNeedsReset(currentTime);
    }

    private synchronized boolean checkNeedsReset(final long timeStamp) {
        final boolean resetDone;
        if (hourIncreased(timeStamp)) {
            hourlyReset();
            resetDone = true;
        } else { // hour not increased, no reset
            resetDone = false;
        }

        return resetDone;
    }

    public synchronized void newOrders(final int nOrders) {
        final long stamp = System.currentTimeMillis();
        newOrders(nOrders, stamp);
    }

    private synchronized void newOrders(final int nOrders, final long orderStamp) {
        if (orderStamp > this.lastOrderStamp) {
            checkNeedsReset(orderStamp);
            this.lastOrderStamp = orderStamp;
        } else if (orderStamp == this.lastOrderStamp) { // could happen
        } else { // orderStamp < this.lastOrderStamp; this case can happen due to many reasons, but the backward time difference should be small
            final long backwardDifference = this.lastOrderStamp - orderStamp;
            if (backwardDifference > 2_000L) {
                logger.error("large backward difference in newOrder: {} {} {}", backwardDifference, this.lastOrderStamp, orderStamp);
            } else { // small difference, normal
            }
            // if this happens very close to the exact hour, some small counting errors can occur, but those are acceptable and small counting errors will occur anyway
        }
        this.nOrdersSinceReset += nOrders;
    }

    private synchronized void hourlyReset() {
        this.nOrdersSinceReset = 0;
    }

    private synchronized boolean hourIncreased(final long newStamp) {
        return hourIncreased(newStamp, this.lastOrderStamp);
    }

    private static boolean hourIncreased(final long newStamp, final long oldStamp) {
        final boolean hasIncreased;

        if (newStamp < oldStamp) {
            logger.error("newStamp < oldStamp in hourIncreased for: {} {}", newStamp, oldStamp);
            hasIncreased = false;
        } else if (newStamp == oldStamp) {
            hasIncreased = false;
        } else { // newStamp > oldStamp
            final long oldHour = getHour(oldStamp);
            final long newHour = getHour(newStamp);
            if (oldHour == newHour) {
                hasIncreased = false;
            } else if (oldHour < newHour) {
                hasIncreased = true;
            } else { // oldHour > newHour
                logger.error("oldHour > newHour in hourIncreased for: {} {} {} {}", oldHour, newHour, oldStamp, newStamp);
                hasIncreased = false;
            }
        }
        return hasIncreased;
    }

    @Contract(pure = true)
    private static long getHour(final long timeStamp) {
        return timeStamp / Generic.HOUR_LENGTH_MILLISECONDS;
    }

    @Contract(pure = true)
    private static double proportionThatHasPassedFromCurrentHour(final long timeStamp) {
        return (double) (timeStamp % Generic.HOUR_LENGTH_MILLISECONDS) / Generic.HOUR_LENGTH_MILLISECONDS;
    }

    private static double proportionThatHasPassedFromCurrentHour() {
        final long currentTime = System.currentTimeMillis();
        return proportionThatHasPassedFromCurrentHour(currentTime);
    }
}
