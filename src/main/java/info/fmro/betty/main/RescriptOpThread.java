package info.fmro.betty.main;

import info.fmro.betty.entities.AccountFundsResponse;
import info.fmro.betty.entities.CancelExecutionReport;
import info.fmro.betty.entities.CurrencyRate;
import info.fmro.betty.entities.LimitOrder;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.entities.MarketFilter;
import info.fmro.betty.entities.PlaceExecutionReport;
import info.fmro.betty.entities.PlaceInstruction;
import info.fmro.betty.enums.ExecutionReportStatus;
import info.fmro.betty.enums.MarketProjection;
import info.fmro.betty.enums.OperationType;
import info.fmro.betty.enums.Side;
import info.fmro.betty.objects.OrderPrice;
import info.fmro.betty.objects.SafetyLimits;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

public class RescriptOpThread<T>
        implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RescriptOpThread.class);
    private final OperationType operation;
    private String marketId;
    private Set<T> returnSet;
    private HashSet<String> eventIdsSet;
    private TreeSet<String> marketIdsSet;
    private HashSet<MarketProjection> marketProjectionsSet;
    private SafetyLimits safetyLimits;
    private List<PlaceInstruction> placeInstructionsList;
    private AtomicInteger threadCounter;
    private boolean isStartedGettingOrders;

    public RescriptOpThread(Set<T> returnSet, HashSet<String> eventIdsSet, HashSet<MarketProjection> marketProjectionsSet) {
        this.operation = OperationType.listMarketCatalogue;
        this.returnSet = returnSet;
        this.eventIdsSet = eventIdsSet;
        this.marketProjectionsSet = marketProjectionsSet;
    }

    public RescriptOpThread(Set<T> returnSet, TreeSet<String> marketIdsSet, HashSet<MarketProjection> marketProjectionsSet) {
        this.operation = OperationType.listMarketCatalogue;
        this.returnSet = returnSet;
        this.marketIdsSet = marketIdsSet;
        this.marketProjectionsSet = marketProjectionsSet;
    }

    public RescriptOpThread(SafetyLimits safetyLimits, AtomicInteger threadCounter) {
        this.operation = OperationType.getAccountFunds;
        this.safetyLimits = safetyLimits;
        this.threadCounter = threadCounter;
    }

    public RescriptOpThread(SafetyLimits safetyLimits) {
        this.operation = OperationType.listCurrencyRates;
        this.safetyLimits = safetyLimits;
    }

    public RescriptOpThread(String marketId, List<PlaceInstruction> placeInstructionsList, boolean isStartedGettingOrders) {
        this.operation = OperationType.placeOrders;
        this.marketId = marketId;
        this.placeInstructionsList = placeInstructionsList;
        this.isStartedGettingOrders = isStartedGettingOrders;
    }

    public RescriptOpThread() {
        this.operation = OperationType.cancelOrders;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        switch (operation) {
            case listMarketCatalogue:
                if (returnSet != null && (eventIdsSet != null && eventIdsSet.size() > 0) || (marketIdsSet != null && marketIdsSet.size() > 0)) {
                    MarketFilter marketFilter = new MarketFilter();
                    marketFilter.setEventIds(eventIdsSet);
                    marketFilter.setMarketIds(marketIdsSet);

                    final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
                    List<MarketCatalogue> marketCatalogueList = ApiNgRescriptOperations.listMarketCatalogue(marketFilter, marketProjectionsSet, null, 200, Statics.appKey.get(), rescriptResponseHandler);

                    if (marketCatalogueList != null) {
                        returnSet.addAll((List<T>) marketCatalogueList);
                    } else { // no results returned
                        if (rescriptResponseHandler.isTooMuchData()) {
                            logger.error("tooMuchData while getting marketCatalogues from: {}", Generic.objectToString(eventIdsSet));
                            for (String eventId : eventIdsSet) {
                                final HashSet<String> singleEventSet = new HashSet<>(2);
                                singleEventSet.add(eventId);
                                marketFilter = new MarketFilter();
                                marketFilter.setEventIds(singleEventSet);

                                marketCatalogueList = ApiNgRescriptOperations.listMarketCatalogue(marketFilter, marketProjectionsSet, null, 200, Statics.appKey.get(), rescriptResponseHandler);

                                if (marketCatalogueList != null) {
                                    returnSet.addAll((List<T>) marketCatalogueList);
                                } else {
                                    logger.error("marketCatalogueList null while getting single event: {} {}", eventId, rescriptResponseHandler.isTooMuchData());
                                }
                            } // end for
                        } else if (Statics.mustStop.get() && Statics.needSessionToken.get()) { // normal case, no results returned because I couldn't get sessionToken and program ended
                        } else {
                            logger.error("marketCatalogueList null and not tooMuchData for: {}", Generic.objectToString(eventIdsSet));
                        }
                    } // end else
                } else {
                    logger.error("null or empty variables in RescriptOpThread listMarketCatalogue: {}", Generic.objectToString(eventIdsSet));
                }
                break;
            case getAccountFunds:
                try {
                    if (safetyLimits != null) {
                        final RescriptAccountResponseHandler rescriptAccountResponseHandler = new RescriptAccountResponseHandler();
                        final AccountFundsResponse accountFundsResponse = ApiNgRescriptOperations.getAccountFunds(Statics.appKey.get(), Statics.sessionTokenObject.getSessionToken(), rescriptAccountResponseHandler);

                        if (accountFundsResponse != null) {
                            final Double availableToBetBalance = accountFundsResponse.getAvailableToBetBalance();
                            if (availableToBetBalance != null) {
                                safetyLimits.processFunds(availableToBetBalance);
                                // atomicAvailableToBetBalance.set(availableToBetBalance - Statics.safetyLimits.getReserve());

                                Generic.alreadyPrintedMap.logOnce(Statics.debugLevel.check(3, 102), logger, LogLevel.INFO, "accountFundsResponse: {}", Generic.objectToString(accountFundsResponse));
                            } else {
                                logger.error("availableToBetBalance null for: {}", Generic.objectToString(accountFundsResponse));
                            }
                        } else {
                            if (Statics.mustStop.get() && Statics.needSessionToken.get()) { // normal to happen during program stop, if not logged in
                            } else {
                                logger.error("accountFundsResponse null");
                            }
                        }
                    } else {
                        logger.error("null or empty variables in RescriptOpThread getAccountFunds");
                    }
                } finally {
                    if (threadCounter != null) {
                        threadCounter.getAndDecrement();
                    } else {
                        logger.error("STRANGE threadCounter null for getAccountFunds");
                    }
                }
                break;
            case listCurrencyRates:
                if (safetyLimits != null) {
                    final RescriptAccountResponseHandler rescriptAccountResponseHandler = new RescriptAccountResponseHandler();
                    final List<CurrencyRate> currencyRates = ApiNgRescriptOperations.listCurrencyRates(Statics.appKey.get(), Statics.sessionTokenObject.getSessionToken(), rescriptAccountResponseHandler);

                    safetyLimits.setCurrencyRate(currencyRates);
                } else {
                    logger.error("null or empty variables in RescriptOpThread listCurrencyRates");
                }
                break;
            case placeOrders:
                if (marketId != null && placeInstructionsList != null) {
                    String customerRef = null;
                    final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
                    PlaceExecutionReport placeExecutionReport;

                    final double pricesArray[] = new double[placeInstructionsList.size()];
                    int counter = 0;
                    for (PlaceInstruction placeInstruction : placeInstructionsList) {
                        final Side side = placeInstruction.getSide();
                        if (side.equals(Side.BACK)) {
                            final LimitOrder limitOrder = placeInstruction.getLimitOrder();
                            pricesArray[counter] = limitOrder.getPrice();
                            limitOrder.setPrice(1.01d);
                        }
                        counter++;
                    } // end for

                    Statics.timeLastFundsOp.set(System.currentTimeMillis());
                    if (!Statics.fundsQuickRun.getAndSet(true)) {
                        logger.info("fundsQuickRun starts");
                    }

                    if (!Statics.notPlacingOrders && !Statics.denyBetting.get()) {
//                        final boolean isStartedGettingOrders = Statics.placedAmounts.addInstructionsList(eventId, marketId, placeInstructionsList);

                        placeExecutionReport = ApiNgRescriptOperations.placeOrders(marketId, placeInstructionsList, customerRef, Statics.appKey.get(), rescriptResponseHandler);

                        Statics.timeLastFundsOp.set(System.currentTimeMillis());
                        if (!Statics.fundsQuickRun.getAndSet(true)) {
                            logger.info("fundsQuickRun starts");
                        }

                        if (placeExecutionReport != null) {
                            final ExecutionReportStatus executionReportStatus = placeExecutionReport.getStatus();
                            if (executionReportStatus == ExecutionReportStatus.SUCCESS) {
                                logger.info("successful order placing market: {} list: {} report: {}", marketId, Generic.objectToString(placeInstructionsList), Generic.objectToString(placeExecutionReport));
                            } else {
                                logger.error("executionReportStatus not successful {} in {} for: {} {}", executionReportStatus, Generic.objectToString(placeExecutionReport), marketId, Generic.objectToString(placeInstructionsList));
                            }
                        } else {
                            // temporary removal until 2nd scraper
                            logger.error("null placeExecutionReport for: {} {}", marketId, Generic.objectToString(placeInstructionsList));
                        }

                        if (!isStartedGettingOrders && safetyLimits.isStartedGettingOrders()) {
                            // will add the list again; being added twice is less dangerous than not being added at all
                            final String eventId = Formulas.getEventIdOfMarketId(marketId);
                            safetyLimits.addInstructionsList(eventId, marketId, placeInstructionsList);
                        } else { // added at beginning, and no need to add again
                        }
                    } else { // Statics.notPlacingOrders || Statics.denyBetting.get()
                        logger.warn("order placing denied {} {}: marketId = {}, placeInstructionsList = {}", Statics.notPlacingOrders, Statics.denyBetting.get(), marketId, Generic.objectToString(placeInstructionsList));
                    }

                    CancelOrdersThread.addOrder(0L);

                    Generic.threadSleep(100L); // avoid some double posting right after the order is placed; setting the delay too large might pose other problems

                    counter = 0;
                    for (PlaceInstruction placeInstruction : placeInstructionsList) {
                        final LimitOrder limitOrder = placeInstruction.getLimitOrder();
                        if (pricesArray[counter] > 0d) {
                            limitOrder.setPrice(pricesArray[counter]);
                        }

                        final OrderPrice orderPrice = new OrderPrice(marketId, placeInstruction.getSelectionId(), placeInstruction.getSide(), limitOrder.getPrice());
                        final double orderSize = limitOrder.getSize();
                        Formulas.removeInstruction(orderPrice, orderSize);

                        counter++;
                    } // end for
                } else {
                    logger.error("STRANGE null or empty variables in RescriptOpThread placeOrders");
                }
                break;
            case cancelOrders:
                final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
                Statics.timeLastFundsOp.set(System.currentTimeMillis());
                if (!Statics.fundsQuickRun.getAndSet(true)) {
                    logger.info("fundsQuickRun starts");
                }

                final CancelExecutionReport cancelExecutionReport = ApiNgRescriptOperations.cancelOrders(null, null, null, Statics.appKey.get(), rescriptResponseHandler);

                Statics.timeLastFundsOp.set(System.currentTimeMillis());
                if (!Statics.fundsQuickRun.getAndSet(true)) {
                    logger.info("fundsQuickRun starts");
                }

                if (cancelExecutionReport != null) {
                    final ExecutionReportStatus executionReportStatus = cancelExecutionReport.getStatus();
                    if (executionReportStatus == ExecutionReportStatus.SUCCESS) {
                        logger.info("canceled orders: {}", Generic.objectToString(cancelExecutionReport));

                        PlacedAmountsThread.shouldCheckAmounts.set(true);
                    } else {
                        logger.error("!!!no success in cancelOrders: {}", Generic.objectToString(cancelExecutionReport));
                    }
                } else {
                    logger.error("!!!failed to cancelOrders");
                }
                break;
            default:
                logger.error("unknown operation in RescriptOpThread: {}", operation);
                break;
        } // end switch
    }
}
