package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetFundsThread
        extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(GetFundsThread.class);
    private static final AtomicInteger getAccountFundsThreadCounter = new AtomicInteger();

    public static long timedGetAccountFunds() {
        long currentTime = System.currentTimeMillis();
        long runInterval;
        boolean newQuickRun = currentTime - Statics.timeLastFundsOp.get() <= 20000L;
        boolean quickRun = Statics.fundsQuickRun.getAndSet(newQuickRun);
        if (quickRun) {
            runInterval = 0L;
            if (!newQuickRun) {
                logger.info("quickRun ending");
            }
        } else {
            runInterval = 1000L;
            if (newQuickRun) {
                logger.error("possible racing problem, quickRun starting");
            }
        }

        long timeForNext = Statics.timeStamps.getLastGetAccountFunds() + runInterval;
        long timeTillNext = timeForNext - currentTime;
        if (timeTillNext <= 0) {
            int runningThreads = getAccountFundsThreadCounter.get();
            if (runningThreads == 0 || (quickRun && runningThreads <= 1)) {
                if (runningThreads == 1) {
                    Generic.threadSleep(100L); // so I don't start a second thread at almost the same time
                }
                Statics.timeStamps.lastGetAccountFundsStamp();
                getAccountFundsThreadCounter.getAndIncrement();
                Statics.threadPoolExecutor.execute(new RescriptOpThread<>(Statics.safetyLimits, getAccountFundsThreadCounter)); // get funds thread

                timeForNext = Statics.timeStamps.getLastGetAccountFunds() + runInterval;

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

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "getFunds thread");
                }
                GetLiveMarketsThread.waitForSessionToken("GetFundsThread main");

                long timeToSleep;

                timeToSleep = timedGetAccountFunds();

                Generic.threadSleepSegmented(timeToSleep, 10L, Statics.mustStop, Statics.fundsQuickRun);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside GetFundsThread loop", throwable);
            }
        }

        logger.info("GetFundsThread ends");
    }
}
