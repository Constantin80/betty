package info.fmro.betty.objects;

import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class BetFrequencyLimit
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(BetFrequencyLimit.class);
    private static final long serialVersionUID = 1110075859608214163L;
    public static final long maxManageMarketPeriod = Generic.MINUTE_LENGTH_MILLISECONDS * 5L; // manage once every 5 minutes, maximum period possible
    public static final long minManageMarketPeriod = 1_000L; // throttle protection
    public static final int limitPerHour = 1_000;
    private long lastOrderStamp;
    private int nOrdersSinceReset;

    public BetFrequencyLimit() {
    }

    public synchronized void copyFrom(final BetFrequencyLimit other) {
        if (other == null) {
            logger.error("null other in copyFrom for: {}", Generic.objectToString(this));
        } else {
            this.lastOrderStamp = other.lastOrderStamp;
            this.nOrdersSinceReset = other.nOrdersSinceReset;
        }
    }

    public synchronized int getNOrdersSinceReset() {
        return nOrdersSinceReset;
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

    public synchronized long getManageMarketPeriod(final double marketCalculatedLimit) { // depends on how close I am to reaching the limit, and it should never allow reaching the limit
        final long manageMarketPeriod;
        final double totalAccountLimit = Statics.safetyLimits.getTotalLimit();
        if (totalAccountLimit <= 0d) {
            manageMarketPeriod = maxManageMarketPeriod;
        } else {
            final double proportionOfAccountLimitAllocatedToMarket = Math.min(marketCalculatedLimit / totalAccountLimit, 1d);
            if (marketCalculatedLimit > totalAccountLimit) {
                logger.error("bogus limits in getManageMarketPeriod: {} {}", marketCalculatedLimit, totalAccountLimit);
            } else { // no error, won't print anything
            }

            final double proportionOfLimitReached = (double) this.nOrdersSinceReset / ((double) limitPerHour * .9d);
            final double proportionThatHasPassedFromCurrentHour = proportionThatHasPassedFromCurrentHour();
            if (proportionOfLimitReached >= proportionThatHasPassedFromCurrentHour) { // this includes the case when proportionThatHasPassedFromCurrentHour == 0d, so I don't need to check further down, before the division
                checkNeedsReset();
                manageMarketPeriod = maxManageMarketPeriod;
            } else { // proportionOfLimitReached < proportionThatHasPassedFromCurrentHour
                final double proportionOfLimitReachedFromCurrentHourSegment = proportionOfLimitReached / proportionThatHasPassedFromCurrentHour;
                manageMarketPeriod = Math.min(maxManageMarketPeriod, (long) ((double) minManageMarketPeriod + Math.pow(proportionOfLimitReachedFromCurrentHourSegment, 2) * (double) maxManageMarketPeriod));
            }
        }

        return manageMarketPeriod;
    }

    public synchronized boolean limitReached() {
        final double initialProportionOfNoConcern = 1d, proportionOfFullConcern = 1d, proportionOfLimitVersusHourSafetyMargin = -1d;
        return checkLimitReached(initialProportionOfNoConcern, proportionOfFullConcern, proportionOfLimitVersusHourSafetyMargin);
    }

    public synchronized boolean closeToLimitReached() {
        final double initialProportionOfNoConcern = .1d, proportionOfFullConcern = .95d, proportionOfLimitVersusHourSafetyMargin = .03d;
        return checkLimitReached(initialProportionOfNoConcern, proportionOfFullConcern, proportionOfLimitVersusHourSafetyMargin);
    }

    public synchronized boolean checkLimitReached(final double initialProportionOfNoConcern, final double proportionOfFullConcern, final double proportionOfLimitVersusHourSafetyMargin) {
        final boolean limitReached;
        final double proportionOfLimitReached = (double) this.nOrdersSinceReset / (double) limitPerHour;

        if (proportionOfLimitReached < initialProportionOfNoConcern) {
            limitReached = false;
        } else {
            final double proportionThatHasPassedFromCurrentHour = proportionThatHasPassedFromCurrentHour();
            if (proportionOfLimitReached < proportionOfFullConcern && proportionOfLimitReached + proportionOfLimitVersusHourSafetyMargin < proportionThatHasPassedFromCurrentHour) {
                // this doesn't include the beginning of the hour, then I'll get on the first branch: proportionOfLimitReached < initialProportionOfNoConcern
                limitReached = false;
            } else if (checkNeedsReset()) {
                limitReached = false;
            } else {
                limitReached = true;
            }
        }

        return limitReached;
    }

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

    public synchronized void newOrders(final int nOrders, final long orderStamp) {
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
        nOrdersSinceReset += nOrders;
    }

    private synchronized void hourlyReset() {
        this.nOrdersSinceReset = 0;
    }

    private synchronized boolean hourIncreased(final long newStamp) {
        return hourIncreased(newStamp, this.lastOrderStamp);
    }

    public static boolean hourIncreased(final long newStamp, final long oldStamp) {
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

    public static long getHour(final long timeStamp) {
        return timeStamp / Generic.HOUR_LENGTH_MILLISECONDS;
    }

    public static double proportionThatHasPassedFromCurrentHour(final long timeStamp) {
        return (double) (timeStamp % Generic.HOUR_LENGTH_MILLISECONDS) / (double) Generic.HOUR_LENGTH_MILLISECONDS;
    }

    public static double proportionThatHasPassedFromCurrentHour() {
        final long currentTime = System.currentTimeMillis();
        return proportionThatHasPassedFromCurrentHour(currentTime);
    }
}
