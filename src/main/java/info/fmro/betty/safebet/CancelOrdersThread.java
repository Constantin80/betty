package info.fmro.betty.safebet;

import info.fmro.betty.betapi.ApiNgRescriptOperations;
import info.fmro.betty.betapi.RescriptResponseHandler;
import info.fmro.shared.entities.CancelExecutionReport;
import info.fmro.shared.entities.CancelInstruction;
import info.fmro.shared.enums.ExecutionReportStatus;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.objects.TemporaryOrder;
import info.fmro.betty.threads.permanent.PlacedAmountsThread;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CancelOrdersThread
        implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CancelOrdersThread.class);
    private final String marketId;
    private final List<CancelInstruction> cancelInstructionsList;
    private final TemporaryOrder temporaryOrder;

    @Contract(pure = true)
    public CancelOrdersThread(final String marketId, @NotNull final List<CancelInstruction> cancelInstructionsList, final TemporaryOrder temporaryOrder) {
        this.marketId = marketId;
        this.cancelInstructionsList = new ArrayList<>(cancelInstructionsList);
        this.temporaryOrder = temporaryOrder;
    }

    @Override
    public void run() {
        final boolean success;
        if (this.marketId != null && !this.cancelInstructionsList.isEmpty() && this.cancelInstructionsList.size() <= 60) {
            final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
            final CancelExecutionReport cancelExecutionReport = ApiNgRescriptOperations.cancelOrders(this.marketId, this.cancelInstructionsList, null, Statics.appKey.get(), rescriptResponseHandler);

            if (cancelExecutionReport != null) {
                final ExecutionReportStatus executionReportStatus = cancelExecutionReport.getStatus();
                if (executionReportStatus == ExecutionReportStatus.SUCCESS) {
                    logger.info("canceled orders: {}", Generic.objectToString(cancelExecutionReport));

                    PlacedAmountsThread.shouldCheckAmounts.set(true);

                    success = true;
                } else {
                    logger.error("!!!no success in cancelOrders: {} {} {}", Generic.objectToString(cancelExecutionReport), this.marketId, Generic.objectToString(this.cancelInstructionsList));
                    success = false;
                }
            } else {
                logger.error("!!!failed to cancelOrders: {} {}", this.marketId, Generic.objectToString(this.cancelInstructionsList));
                success = false;
            }
        } else {
            logger.error("STRANGE null or empty variables in CancelOrdersThread: {} {}", this.marketId, Generic.objectToString(this.cancelInstructionsList));
            success = false;
        }

        if (success) { // nothing to be done, I just act in case of failure
        } else {
            this.temporaryOrder.setExpirationTime(System.currentTimeMillis() + Generic.MINUTE_LENGTH_MILLISECONDS);
        }
    }
}
