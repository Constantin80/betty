package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CancelOrdersThread
        implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CancelOrdersThread.class);
    public static final long minimumSpacing = 1_000L;
    public static final Set<Long> cancelOrderRunTimesSet = Collections.synchronizedSet(new HashSet<Long>(8));
    public static final AtomicLong timeLastCancelOrder = new AtomicLong();
    public static final AtomicBoolean newOrderAdded = new AtomicBoolean();

    public static synchronized void addOrder(long delay) { // synchronized on class
        final long currentTime = System.currentTimeMillis();
        final long primitiveLastCancelOrder = timeLastCancelOrder.get();
        final long expectedExecutionTime = currentTime + delay;
        final long minimumExecutionTime = primitiveLastCancelOrder + minimumSpacing;

        cancelOrderRunTimesSet.add(Math.max(expectedExecutionTime, minimumExecutionTime));
        newOrderAdded.set(true);
    }

    public static synchronized long checkOrderList() { // synchronized on class
        long timeToSleep;
        if (!cancelOrderRunTimesSet.isEmpty()) {
            long firstRunTime = 0L;
            for (long runTime : cancelOrderRunTimesSet) {
                firstRunTime = firstRunTime == 0L ? runTime : Math.min(firstRunTime, runTime);
            }

            final long primitiveLastCancelOrder = timeLastCancelOrder.get();
            final long minimumExecutionTime = primitiveLastCancelOrder + minimumSpacing;
            final long currentTime = System.currentTimeMillis();
            final long nextRunTime = Math.max(Math.max(firstRunTime, minimumExecutionTime), currentTime);
            final long timeTillNextRun = nextRunTime - currentTime;
            if (timeTillNextRun <= 0L) {
                timeToSleep = minimumSpacing;

                timeLastCancelOrder.set(nextRunTime);

                Iterator<Long> iterator = cancelOrderRunTimesSet.iterator();
                while (iterator.hasNext()) {
                    long value = iterator.next();
                    if (value <= nextRunTime) {
                        iterator.remove();
                    }
                } // end while

                Statics.threadPoolExecutor.execute(new RescriptOpThread<>()); // cancel orders thread
            } else {
                timeToSleep = timeTillNextRun;
            }
        } else { // no orders
            timeToSleep = Generic.HOUR_LENGTH_MILLISECONDS;
        }

        newOrderAdded.set(false);
        return timeToSleep;
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "cancel orders thread");
                }
                GetLiveMarketsThread.waitForSessionToken("CancelOrdersThread main");

                long timeToSleep = checkOrderList();

                Generic.threadSleepSegmented(timeToSleep, 100L, newOrderAdded, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside CancelOrdersThread loop", throwable);
            }
        }

        logger.info("CancelOrdersThread ends");
    }
}
