package info.fmro.betty.threads.permanent;

import info.fmro.betty.betapi.RescriptOpThread;
import info.fmro.betty.main.Betty;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.objects.SharedStatics;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class PendingOrdersThread
        implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(PendingOrdersThread.class);
    public static final long minimumCancelAllOrderSpacing = 1_000L;
    private final Collection<Long> cancelAllOrdersRunTimesSet = new HashSet<>(8);
    private final AtomicLong timeLastCancelAllOrder = new AtomicLong();
    private final AtomicBoolean newCancelAllOrderAdded = new AtomicBoolean(); // newOrderAdded = new AtomicBoolean();

    public synchronized void addCancelAllOrder(final long delay) {
        logger.error("add cancelAllOrders command should never be used");
        final long currentTime = System.currentTimeMillis();
        final long primitiveLastCancelOrder = this.timeLastCancelAllOrder.get();
        final long expectedExecutionTime = currentTime + delay;
        final long minimumExecutionTime = primitiveLastCancelOrder + minimumCancelAllOrderSpacing;

        this.cancelAllOrdersRunTimesSet.add(Math.max(expectedExecutionTime, minimumExecutionTime));
        this.newCancelAllOrderAdded.set(true);
    }

    private synchronized long checkCancelAllOrdersList() {
        final long timeToSleep;
        if (this.cancelAllOrdersRunTimesSet.isEmpty()) { // no orders
            if (this.newCancelAllOrderAdded.get()) {
                logger.error("newCancelAllOrderAdded true and no orders present in checkCancelAllOrdersList");
            }
            timeToSleep = Generic.HOUR_LENGTH_MILLISECONDS;
        } else {
            logger.error("cancelAllOrders being used");
            long firstRunTime = 0L;
            for (final long runTime : this.cancelAllOrdersRunTimesSet) {
                firstRunTime = firstRunTime == 0L ? runTime : Math.min(firstRunTime, runTime);
            }

            final long primitiveLastCancelOrder = this.timeLastCancelAllOrder.get();
            final long minimumExecutionTime = primitiveLastCancelOrder + minimumCancelAllOrderSpacing;
            final long currentTime = System.currentTimeMillis();
            final long nextRunTime = Math.max(Math.max(firstRunTime, minimumExecutionTime), currentTime);
            final long timeTillNextRun = nextRunTime - currentTime;
            if (timeTillNextRun <= 0L) {
                timeToSleep = minimumCancelAllOrderSpacing;

                this.timeLastCancelAllOrder.set(nextRunTime);

                this.cancelAllOrdersRunTimesSet.removeIf(value -> value <= nextRunTime);

                SharedStatics.threadPoolExecutor.execute(new RescriptOpThread<>()); // cancel orders thread
            } else {
                timeToSleep = timeTillNextRun;
            }
        }

        this.newCancelAllOrderAdded.set(false);
        return timeToSleep;
    }

    @Override
    public void run() {
        while (!SharedStatics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, "cancel orders thread");
                }
                GetLiveMarketsThread.waitForSessionToken("PendingOrdersThread main");

                long timeToSleep = checkCancelAllOrdersList(); // not used, so won't affect timeToSleep, else I would get checkForExpiredOrders on its own thread
                timeToSleep = Math.min(timeToSleep, SharedStatics.orderCache.checkForExpiredOrders());

                Generic.threadSleepSegmented(timeToSleep, 100L, this.newCancelAllOrderAdded, SharedStatics.mustStop); // , this.newOrderAdded
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside PendingOrdersThread loop", throwable);
            }
        }

        logger.debug("PendingOrdersThread ends");
    }
}
