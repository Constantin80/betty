package info.fmro.betty.main;

import info.fmro.betty.entities.PlaceExecutionReport;
import info.fmro.betty.entities.PlaceInstruction;
import info.fmro.betty.entities.PlaceInstructionReport;
import info.fmro.betty.enums.ExecutionReportStatus;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.objects.TemporaryOrder;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PlaceOrdersThread
        implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(PlaceOrdersThread.class);
    private final String marketId;
    private final List<PlaceInstruction> placeInstructionsList;
    private final TemporaryOrder temporaryOrder;

    public PlaceOrdersThread(final String marketId, final List<PlaceInstruction> placeInstructionsList, @NotNull final TemporaryOrder temporaryOrder) {
        this.marketId = marketId;
        this.placeInstructionsList = placeInstructionsList;
        this.temporaryOrder = temporaryOrder;
    }

    @Override
    public void run() {
        final boolean success;
        if (marketId != null && placeInstructionsList != null && !placeInstructionsList.isEmpty() && this.temporaryOrder != null &&
            placeInstructionsList.size() == 1) { // && placeInstructionsList.size() <= 200 ; right now I only support 1 instruction per order
            final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
            final PlaceExecutionReport placeExecutionReport;

            if (!Statics.notPlacingOrders && !Statics.denyBetting.get()) {
                placeExecutionReport = ApiNgRescriptOperations.placeOrders(marketId, placeInstructionsList, null, Statics.appKey.get(), rescriptResponseHandler);

                if (placeExecutionReport != null) {
                    final ExecutionReportStatus executionReportStatus = placeExecutionReport.getStatus();
                    if (executionReportStatus == ExecutionReportStatus.SUCCESS) {
                        logger.info("successful order placing market: {} list: {} report: {}", marketId, Generic.objectToString(placeInstructionsList), Generic.objectToString(placeExecutionReport));

                        final List<PlaceInstructionReport> placeInstructionReports = placeExecutionReport.getInstructionReports();
                        if (placeInstructionReports != null && placeInstructionReports.size() == 1) { // only 1 instruction supported for now
                            final PlaceInstructionReport placeInstructionReport = placeInstructionReports.get(0);
                            if (placeInstructionReport != null) {
                                final String betId = placeInstructionReport.getBetId();
                                temporaryOrder.setBetId(betId);
                                success = true;
                            } else {
                                logger.error("null placeInstructionReport in PlaceOrdersThread: {} {} {} {}", marketId, Generic.objectToString(placeInstructionsList), Generic.objectToString(this.temporaryOrder),
                                             Generic.objectToString(placeInstructionReports));
                                success = false;
                            }
                        } else {
                            logger.error("null or empty placeInstructionReports in PlaceOrdersThread: {} {} {} {}", marketId, Generic.objectToString(placeInstructionsList), Generic.objectToString(this.temporaryOrder),
                                         Generic.objectToString(placeInstructionReports));
                            success = false;
                        }
                    } else {
                        logger.error("executionReportStatus not successful {} in {} for: {} {}", executionReportStatus, Generic.objectToString(placeExecutionReport), marketId, Generic.objectToString(placeInstructionsList));
                        success = false;
                    }
                } else {
                    // temporary removal until 2nd scraper
                    logger.error("null placeExecutionReport for: {} {}", marketId, Generic.objectToString(placeInstructionsList));
                    success = false;
                }
            } else { // Statics.notPlacingOrders || Statics.denyBetting.get()
                logger.warn("order placing denied {} {}: marketId = {}, placeInstructionsList = {}", Statics.notPlacingOrders, Statics.denyBetting.get(), marketId, Generic.objectToString(placeInstructionsList));
                success = false;
            }
        } else {
            logger.error("STRANGE null or empty variables in PlaceOrdersThread: {} {} {}", marketId, Generic.objectToString(placeInstructionsList), Generic.objectToString(this.temporaryOrder));
            success = false;
        }

        if (success) { // nothing to be done, I just act in case of failure
        } else {
            temporaryOrder.setExpirationTime(System.currentTimeMillis() + Generic.MINUTE_LENGTH_MILLISECONDS);
        }
    }
}
