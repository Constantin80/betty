package info.fmro.betty.threads.permanent;

import info.fmro.betty.betapi.RescriptOpThread;
import info.fmro.betty.main.Betty;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.objects.SharedStatics;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class GetFundsThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(GetFundsThread.class);
    private static final AtomicInteger getAccountFundsThreadCounter = new AtomicInteger();

    private static long timedGetAccountFunds() {
        final long currentTime = System.currentTimeMillis();
        final long runInterval;
        final boolean newQuickRun = currentTime - Statics.timeLastFundsOp.get() <= 20_000L;
        final boolean quickRun = Statics.fundsQuickRun.getAndSet(newQuickRun);
        if (quickRun) {
            runInterval = 5_000L; // fundsQuickRun is no longer used; I increased interval from 0 to 5_000ms
            if (!newQuickRun) {
                logger.info("quickRun ending");
            }
        } else {
            runInterval = 10_000L;
            if (newQuickRun) {
                logger.error("possible racing problem, quickRun starting");
            }
        }

        long timeForNext = SharedStatics.timeStamps.getLastGetAccountFunds() + runInterval;
        long timeTillNext = timeForNext - currentTime;
        if (timeTillNext <= 0) {
            final int runningThreads = getAccountFundsThreadCounter.get();
            if (runningThreads == 0 || (quickRun && runningThreads <= 1)) {
                if (runningThreads == 1) {
                    Generic.threadSleep(100L); // so I don't start a second thread at almost the same time
                }
                SharedStatics.timeStamps.lastGetAccountFundsStamp();
                getAccountFundsThreadCounter.getAndIncrement();
                SharedStatics.threadPoolExecutor.execute(new RescriptOpThread<>(Statics.safetyLimits, getAccountFundsThreadCounter)); // get funds thread

                //noinspection ReuseOfLocalVariable
                timeForNext = SharedStatics.timeStamps.getLastGetAccountFunds() + runInterval;

                timeTillNext = timeForNext - currentTime;
            } else {
                if (Statics.debugLevel.check(2, 138)) {
                    logger.warn("didn't start another getAccountFunds thread, still running: {}", getAccountFundsThreadCounter.get());
                }
                if (quickRun) {
                    Generic.threadSleep(10L); // avoid throttle, timeTillNext is unusable during quickRun
                } else {
                    timeTillNext = 100L; // avoid throttle
                }
            }
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    private static long timedListCurrencyRates() { // server updates a few seconds after each exact hour; I'll get it at 3 minutes past exact hour
        final long currentTime = System.currentTimeMillis();
        long timeForNext = SharedStatics.timeStamps.getLastListCurrencyRates() + Generic.HOUR_LENGTH_MILLISECONDS;
        final long timeOverExactHour = timeForNext % Generic.HOUR_LENGTH_MILLISECONDS;
        timeForNext = timeForNext - timeOverExactHour + 3 * Generic.MINUTE_LENGTH_MILLISECONDS;

        long timeTillNext = timeForNext - currentTime;
        if (timeTillNext <= 0) {
            SharedStatics.timeStamps.lastListCurrencyRatesStamp();
            SharedStatics.threadPoolExecutor.execute(new RescriptOpThread<>(Statics.safetyLimits)); // listCurrencyRates thread

            timeTillNext = timeForNext - currentTime + Generic.HOUR_LENGTH_MILLISECONDS;
            if (timeTillNext < -Generic.HOUR_LENGTH_MILLISECONDS) { // hasn't been updated in a very long time
                timeTillNext = Generic.HOUR_LENGTH_MILLISECONDS;
            }
        } else { // nothing to be done
        }

        return timeTillNext;
    }

    @Override
    public void run() {
        while (!SharedStatics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, "getFunds thread");
                }
                GetLiveMarketsThread.waitForSessionToken("GetFundsThread main");

                long timeToSleep = Generic.MINUTE_LENGTH_MILLISECONDS * 5L; // maximum sleep

                if (Statics.connectingToBetfairServersDisabled) { // if stream module stopped, I'm disabling interrogating account funds and currency rates
                } else {
                    timeToSleep = Math.min(timeToSleep, timedGetAccountFunds());
                    timeToSleep = Math.min(timeToSleep, timedListCurrencyRates());
                }

                Generic.threadSleepSegmented(timeToSleep, 10L, SharedStatics.mustStop, Statics.fundsQuickRun);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside GetFundsThread loop", throwable);
            }
        }

        logger.debug("GetFundsThread ends");
    }
}
