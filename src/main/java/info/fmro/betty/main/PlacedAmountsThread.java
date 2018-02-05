package info.fmro.betty.main;

import info.fmro.betty.entities.ClearedOrderSummary;
import info.fmro.betty.entities.CurrentOrderSummary;
import info.fmro.betty.enums.BetStatus;
import info.fmro.betty.enums.OrderBy;
import info.fmro.betty.enums.OrderProjection;
import info.fmro.betty.enums.SortDir;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlacedAmountsThread
        implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PlacedAmountsThread.class);
    public static final AtomicBoolean shouldCheckAmounts = new AtomicBoolean();
    private long timeStamp;

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "placed amounts thread");
                }
                GetLiveMarketsThread.waitForSessionToken("PlacedAmountsThread main");

                this.timeStamp();
                Statics.safetyLimits.startingGettingOrders();

                final RescriptResponseHandler rescriptResponseHandlerCurrent = new RescriptResponseHandler();
                final HashSet<CurrentOrderSummary> currentOrderSummarySet = ApiNgRescriptOperations.listCurrentOrders(null, null, OrderProjection.ALL, null, OrderBy.BY_PLACE_TIME,
                        SortDir.EARLIEST_TO_LATEST, 0, 0, Statics.appKey.get(), rescriptResponseHandlerCurrent);

                final RescriptResponseHandler rescriptResponseHandlerCleared = new RescriptResponseHandler();
                final HashSet<ClearedOrderSummary> clearedOrderSummarySet = ApiNgRescriptOperations.listClearedOrders(BetStatus.SETTLED, null, null, null, null, null, null, null,
                        null, true, 0, 0, Statics.appKey.get(), rescriptResponseHandlerCleared);

                Statics.safetyLimits.addOrderSummaries(currentOrderSummarySet, clearedOrderSummarySet);

                long timeToSleep = this.timeStamp + 30_000L - System.currentTimeMillis();

//                long timeToSleep = checkOrderList();
//
                Generic.threadSleepSegmented(timeToSleep, 100L, shouldCheckAmounts, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside CancelOrdersThread loop", throwable);
            }
        }

        logger.info("CancelOrdersThread ends");
    }

//    public synchronized boolean shouldCheckAmounts() {
//        final boolean modified;
//
//        modified = !this.shouldCheckAmounts.getAndSet(true);
//
//        return modified;
//    }
    public synchronized void timeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

}

//public class CancelOrdersThread
//        implements Runnable {
//
//    private static final Logger logger = LoggerFactory.getLogger(CancelOrdersThread.class);
//    public static final long minimumSpacing = 1_000L;
//    public static final Set<Long> cancelOrderRunTimesSet = Collections.synchronizedSet(new HashSet<Long>(8));
//    public static final AtomicLong timeLastCancelOrder = new AtomicLong();
//    public static final AtomicBoolean newOrderAdded = new AtomicBoolean();
//
//    public static synchronized void addOrder(long delay) { // synchronized on class
//        final long currentTime = System.currentTimeMillis();
//        final long primitiveLastCancelOrder = timeLastCancelOrder.get();
//        final long expectedExecutionTime = currentTime + delay;
//        final long minimumExecutionTime = primitiveLastCancelOrder + minimumSpacing;
//
//        cancelOrderRunTimesSet.add(Math.max(expectedExecutionTime, minimumExecutionTime));
//        newOrderAdded.set(true);
//    }
//    public static synchronized long checkOrderList() { // synchronized on class
//        long timeToSleep;
//        if (!cancelOrderRunTimesSet.isEmpty()) {
//            long firstRunTime = 0L;
//            for (long runTime : cancelOrderRunTimesSet) {
//                firstRunTime = firstRunTime == 0L ? runTime : Math.min(firstRunTime, runTime);
//            }
//
//            final long primitiveLastCancelOrder = timeLastCancelOrder.get();
//            final long minimumExecutionTime = primitiveLastCancelOrder + minimumSpacing;
//            final long currentTime = System.currentTimeMillis();
//            final long nextRunTime = Math.max(Math.max(firstRunTime, minimumExecutionTime), currentTime);
//            final long timeTillNextRun = nextRunTime - currentTime;
//            if (timeTillNextRun <= 0L) {
//                timeToSleep = minimumSpacing;
//
//                timeLastCancelOrder.set(nextRunTime);
//
//                Iterator<Long> iterator = cancelOrderRunTimesSet.iterator();
//                while (iterator.hasNext()) {
//                    long value = iterator.next();
//                    if (value <= nextRunTime) {
//                        iterator.remove();
//                    }
//                } // end while
//
//                Statics.threadPoolExecutor.execute(new RescriptOpThread<>()); // cancel orders thread
//            } else {
//                timeToSleep = timeTillNextRun;
//            }
//        } else { // no orders
//            timeToSleep = Generic.HOUR_LENGTH_MILLISECONDS;
//        }
//
//        newOrderAdded.set(false);
//        return timeToSleep;
//    }
//}
