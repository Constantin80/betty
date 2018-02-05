package info.fmro.betty.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IgnorableDatabaseThread
        implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(IgnorableDatabaseThread.class);
    public static final AtomicBoolean shouldCheckAmounts = new AtomicBoolean();
    private long timeStamp;

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "ignorable database thread");
                }
                GetLiveMarketsThread.waitForSessionToken("IgnorableDatabaseThread main");

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
                logger.error("STRANGE ERROR inside IgnorableDatabaseThread loop", throwable);
            }
        }

        logger.info("IgnorableDatabaseThread ends");
    }

    public synchronized void timeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

}
