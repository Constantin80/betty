package info.fmro.betty.logic;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.logic.BetFrequencyLimit;
import info.fmro.shared.logic.ExistingFunds;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class SafetyLimits
//        extends ExistingFunds
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(SafetyLimits.class);
    private static final long serialVersionUID = 9100257467817248236L;
    private static final double reserveFraction = .5d; // reserve is 50% of total (but starts at 500 and only increases)
    //    private final HashMap<AtomicDouble, Long> tempReserveMap = new HashMap<>(0);
    // private final HashSet<AtomicDouble> localUsedBalanceSet = new HashSet<>(0);
    public final BetFrequencyLimit speedLimit = new BetFrequencyLimit();
    public final ExistingFunds existingFunds = new ExistingFunds();

    public synchronized void copyFrom(final SafetyLimits safetyLimits) {
//        if (!this.tempReserveMap.isEmpty()) {
//            logger.error("not empty map in SafetyLimits copyFrom: {}", Generic.objectToString(this));
//        }
//        this.tempReserveMap.clear();
//        this.tempReserveMap.putAll(safetyLimits.tempReserveMap);

//        if (!this.tempInstructionsListMap.isEmpty() || !this.eventAmounts.isEmpty() || !this.marketAmounts.isEmpty() || !this.runnerAmounts.isEmpty()) {
//            logger.error("not empty map in SafetyLimits copyFrom: {}", Generic.objectToString(this));
//        }

        if (safetyLimits == null) {
            logger.error("null safetyLimits in copyFrom for: {}", Generic.objectToString(this));
        } else {
//            Generic.updateObject(this, safetyLimits);

//            this.tempInstructionsListMap.clear();
//            this.tempInstructionsListMap.putAll(safetyLimits.tempInstructionsListMap);
//            this.eventAmounts.clear();
//            this.eventAmounts.putAll(safetyLimits.eventAmounts);
//            this.marketAmounts.clear();
//            this.marketAmounts.putAll(safetyLimits.marketAmounts);
//            this.runnerAmounts.clear();
//            this.runnerAmounts.putAll(safetyLimits.runnerAmounts);
//            this.startedGettingOrders = safetyLimits.startedGettingOrders;
            this.existingFunds.copyFrom(safetyLimits.existingFunds);
//            this.setAvailableFunds(safetyLimits.getAvailableFunds());
//            this.setExposure(safetyLimits.getExposure());
//            this.setTotalFunds(safetyLimits.getTotalFunds());
//            this.currencyRate.set(safetyLimits.currencyRate.get());
//            this.setReserve(safetyLimits.getReserve());
            this.speedLimit.copyFrom(safetyLimits.speedLimit);
//            this.rulesManager.copyFrom(safetyLimits.rulesManager);
        }

        //noinspection FloatingPointEquality
        if (this.existingFunds.currencyRate.get() == 1d || this.existingFunds.currencyRate.get() == 0d) { // likely default values
            Statics.timeStamps.setLastListCurrencyRates(0L); // get currencyRate as soon as possible
        }
    }

    public synchronized double getAvailableLimit() {
        return this.existingFunds.getAvailableFunds() - this.existingFunds.getReserve() - 0.01d; // leave 1 cent, to avoid errors
    }

    public synchronized boolean processFunds(final double newAvailableFunds, final double newExposure) {
        this.existingFunds.setAvailableFunds(newAvailableFunds);
        this.existingFunds.setExposure(newExposure);
        this.existingFunds.setTotalFunds(newAvailableFunds - newExposure); // exposure is a negative number

        final double newReserve = Math.floor(this.existingFunds.getTotalFunds() * SafetyLimits.reserveFraction);
//        final double newReserve = Math.floor(availableFunds * SafetyLimits.reserveFraction);

        return this.existingFunds.setReserve(newReserve);
    }
}
