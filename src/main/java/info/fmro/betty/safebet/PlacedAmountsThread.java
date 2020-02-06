package info.fmro.betty.safebet;

import info.fmro.betty.betapi.ApiNgRescriptOperations;
import info.fmro.betty.betapi.RescriptResponseHandler;
import info.fmro.betty.entities.CurrentOrderSummary;
import info.fmro.betty.main.Betty;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.permanent.GetLiveMarketsThread;
import info.fmro.shared.entities.ClearedOrderSummary;
import info.fmro.shared.enums.BetStatus;
import info.fmro.shared.enums.OrderBy;
import info.fmro.shared.enums.OrderProjection;
import info.fmro.shared.enums.SortDir;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

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
                Statics.safeBetSafetyLimits.startingGettingOrders();

                final RescriptResponseHandler rescriptResponseHandlerCurrent = new RescriptResponseHandler();
                final HashSet<CurrentOrderSummary> currentOrderSummarySet = ApiNgRescriptOperations.listCurrentOrders(null, null, OrderProjection.ALL, null, OrderBy.BY_PLACE_TIME, SortDir.EARLIEST_TO_LATEST, 0,
                                                                                                                      0, Statics.appKey.get(), rescriptResponseHandlerCurrent);

                final RescriptResponseHandler rescriptResponseHandlerCleared = new RescriptResponseHandler();
                final HashSet<ClearedOrderSummary> clearedOrderSummarySet = ApiNgRescriptOperations.listClearedOrders(BetStatus.SETTLED, null, null, null, null, null, null, null,
                                                                                                                      null, true, 0, 0, Statics.appKey.get(), rescriptResponseHandlerCleared);

                Statics.safeBetSafetyLimits.addOrderSummaries(currentOrderSummarySet, clearedOrderSummarySet);

                final long timeToSleep = this.timeStamp + 30_000L - System.currentTimeMillis();

//                long timeToSleep = checkCancelAllOrdersList();
//
                Generic.threadSleepSegmented(timeToSleep, 100L, shouldCheckAmounts, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside PlacedAmountsThread loop", throwable);
            }
        }

        logger.info("PlacedAmountsThread ends");
    }

    //    public synchronized boolean shouldCheckAmounts() {
//        final boolean modified;
//
//        modified = !this.shouldCheckAmounts.getAndSet(true);
//
//        return modified;
//    }
    private void timeStamp() { // synchronization most likely not needed
        this.timeStamp = System.currentTimeMillis();
    }
}
