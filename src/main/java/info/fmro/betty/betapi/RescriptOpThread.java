package info.fmro.betty.betapi;

import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.logic.SafetyLimits;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.permanent.PlacedAmountsThread;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.entities.AccountFundsResponse;
import info.fmro.shared.entities.CancelExecutionReport;
import info.fmro.shared.entities.CurrencyRate;
import info.fmro.shared.entities.LimitOrder;
import info.fmro.shared.entities.MarketFilter;
import info.fmro.shared.entities.PlaceExecutionReport;
import info.fmro.shared.entities.PlaceInstruction;
import info.fmro.shared.enums.ExecutionReportStatus;
import info.fmro.shared.enums.MarketProjection;
import info.fmro.shared.enums.OperationType;
import info.fmro.shared.enums.Side;
import info.fmro.shared.objects.OrderPrice;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.apache.http.client.ResponseHandler;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ClassWithTooManyConstructors")
public class RescriptOpThread<T>
        implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RescriptOpThread.class);
    private final OperationType operation;
    private String marketId;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Set<? super T> returnSet;
    private HashSet<String> eventIdsSet;
    private TreeSet<String> marketIdsSet;
    private EnumSet<MarketProjection> marketProjectionsSet;
    private SafetyLimits safetyLimits;
    private List<PlaceInstruction> placeInstructionsList;
    private AtomicInteger threadCounter;
    @SuppressWarnings("BooleanVariableAlwaysNegated")
    private boolean isStartedGettingOrders;

    @Contract(pure = true)
    public RescriptOpThread(final Set<? super T> returnSet, final HashSet<String> eventIdsSet, final EnumSet<MarketProjection> marketProjectionsSet) {
        this.operation = OperationType.listMarketCatalogue;
        this.returnSet = new HashSet<>(returnSet);
        this.eventIdsSet = new HashSet<>(eventIdsSet);
        this.marketProjectionsSet = EnumSet.copyOf(marketProjectionsSet);
    }

    @Contract(pure = true)
    public RescriptOpThread(final Set<? super T> returnSet, final TreeSet<String> marketIdsSet, final EnumSet<MarketProjection> marketProjectionsSet) {
        this.operation = OperationType.listMarketCatalogue;
        this.returnSet = new HashSet<>(returnSet);
        this.marketIdsSet = new TreeSet<>(marketIdsSet);
        this.marketProjectionsSet = EnumSet.copyOf(marketProjectionsSet);
    }

    @Contract(pure = true)
    public RescriptOpThread(final SafetyLimits safetyLimits, final AtomicInteger threadCounter) {
        this.operation = OperationType.getAccountFunds;
        this.safetyLimits = safetyLimits;
        this.threadCounter = threadCounter;
    }

    @Contract(pure = true)
    public RescriptOpThread(final SafetyLimits safetyLimits) {
        this.operation = OperationType.listCurrencyRates;
        this.safetyLimits = safetyLimits;
    }

    @Contract(pure = true)
    public RescriptOpThread(final String marketId, final List<PlaceInstruction> placeInstructionsList, final boolean isStartedGettingOrders) {
        this.operation = OperationType.oldPlaceOrders;
        this.marketId = marketId;
        this.placeInstructionsList = new ArrayList<>(placeInstructionsList);
        this.isStartedGettingOrders = isStartedGettingOrders;
    }

    @Contract(pure = true)
    public RescriptOpThread() {
        this.operation = OperationType.cancelAllOrders;
    }

    @Override
    @SuppressWarnings({"unchecked", "OverlyComplexMethod", "OverlyCoupledMethod", "OverlyLongMethod", "OverlyNestedMethod"})
    public void run() {
        //noinspection SwitchStatementDensity
        switch (this.operation) {
            case listMarketCatalogue:
                if (this.returnSet != null && (this.eventIdsSet != null && !this.eventIdsSet.isEmpty()) || (this.marketIdsSet != null && !this.marketIdsSet.isEmpty())) {
                    MarketFilter marketFilter = new MarketFilter();
                    marketFilter.setEventIds(this.eventIdsSet);
                    marketFilter.setMarketIds(this.marketIdsSet);

                    final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
                    List<MarketCatalogue> marketCatalogueList = ApiNgRescriptOperations.listMarketCatalogue(marketFilter, this.marketProjectionsSet, null, 200, Statics.appKey.get(), rescriptResponseHandler);

                    if (marketCatalogueList != null) {
                        this.returnSet.addAll((List<T>) marketCatalogueList);
                    } else { // no results returned
                        if (rescriptResponseHandler.isTooMuchData()) {
                            logger.error("tooMuchData while getting marketCatalogues from: {}", Generic.objectToString(this.eventIdsSet));
                            for (final String eventId : this.eventIdsSet) {
                                final Set<String> singleEventSet = new HashSet<>(2);
                                singleEventSet.add(eventId);
                                marketFilter = new MarketFilter();
                                marketFilter.setEventIds(singleEventSet);

                                marketCatalogueList = ApiNgRescriptOperations.listMarketCatalogue(marketFilter, this.marketProjectionsSet, null, 200, Statics.appKey.get(), rescriptResponseHandler);

                                if (marketCatalogueList != null) {
                                    this.returnSet.addAll((List<T>) marketCatalogueList);
                                } else {
                                    logger.error("marketCatalogueList null while getting single event: {} {}", eventId, rescriptResponseHandler.isTooMuchData());
                                }
                            } // end for
                        } else if (Statics.mustStop.get() && Statics.needSessionToken.get()) { // normal case, no results returned because I couldn't get sessionToken and program ended
                        } else {
                            logger.error("marketCatalogueList null and not tooMuchData for: {}", Generic.objectToString(this.eventIdsSet));
                        }
                    } // end else
                } else {
                    logger.error("null or empty variables in RescriptOpThread listMarketCatalogue: {}", Generic.objectToString(this.eventIdsSet));
                }
                break;
            case getAccountFunds:
                try {
                    if (this.safetyLimits != null) {
                        final ResponseHandler<String> rescriptAccountResponseHandler = new RescriptAccountResponseHandler();
                        final AccountFundsResponse accountFundsResponse = ApiNgRescriptOperations.getAccountFunds(Statics.appKey.get(), rescriptAccountResponseHandler);

                        if (accountFundsResponse != null) {
                            final Double availableToBetBalance = accountFundsResponse.getAvailableToBetBalance();
                            final Double exposure = accountFundsResponse.getExposure();
                            if (availableToBetBalance != null && exposure != null) {
                                this.safetyLimits.processFunds(availableToBetBalance, exposure);
                                // atomicAvailableToBetBalance.set(availableToBetBalance - Statics.safetyLimits.getReserve());

                                Generic.alreadyPrintedMap.logOnce(Statics.debugLevel.check(3, 102), logger, LogLevel.INFO, "accountFundsResponse: {}", Generic.objectToString(accountFundsResponse));
                            } else {
                                logger.error("availableToBetBalance or exposure null for: {}", Generic.objectToString(accountFundsResponse));
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
                    if (this.threadCounter != null) {
                        this.threadCounter.getAndDecrement();
                    } else {
                        logger.error("STRANGE threadCounter null for getAccountFunds");
                    }
                }
                break;
            case listCurrencyRates:
                if (this.safetyLimits != null) {
                    final ResponseHandler<String> rescriptAccountResponseHandler = new RescriptAccountResponseHandler();
                    final List<CurrencyRate> currencyRates = ApiNgRescriptOperations.listCurrencyRates(Statics.appKey.get(), rescriptAccountResponseHandler);

                    this.safetyLimits.setCurrencyRate(currencyRates, Statics.mustStop, Statics.needSessionToken);
                } else {
                    logger.error("null or empty variables in RescriptOpThread listCurrencyRates");
                }
                break;
            case oldPlaceOrders:
                logger.error("oldPlaceOrders is an old operation from the old disabled safeBets support; it needs heavy modifications and it should not be used");
                if (this.marketId != null && this.placeInstructionsList != null) {
                    final String customerRef = null;
                    final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
                    final PlaceExecutionReport placeExecutionReport;

                    final double pricesArray[] = new double[this.placeInstructionsList.size()];
                    int counter = 0;
                    for (final PlaceInstruction placeInstruction : this.placeInstructionsList) {
                        final Side side = placeInstruction.getSide();
                        if (side == Side.BACK) {
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

                        placeExecutionReport = ApiNgRescriptOperations.placeOrders(this.marketId, this.placeInstructionsList, customerRef, Statics.appKey.get(), rescriptResponseHandler);

                        Statics.timeLastFundsOp.set(System.currentTimeMillis());
                        if (!Statics.fundsQuickRun.getAndSet(true)) {
                            logger.info("fundsQuickRun starts");
                        }

                        if (placeExecutionReport != null) {
                            final ExecutionReportStatus executionReportStatus = placeExecutionReport.getStatus();
                            if (executionReportStatus == ExecutionReportStatus.SUCCESS) {
                                logger.info("successful order placing market: {} list: {} report: {}", this.marketId, Generic.objectToString(this.placeInstructionsList), Generic.objectToString(placeExecutionReport));
                            } else {
                                logger.error("executionReportStatus not successful {} in {} for: {} {}", executionReportStatus, Generic.objectToString(placeExecutionReport), this.marketId, Generic.objectToString(this.placeInstructionsList));
                            }
                        } else {
                            // temporary removal until 2nd scraper
                            logger.error("null placeExecutionReport for: {} {}", this.marketId, Generic.objectToString(this.placeInstructionsList));
                        }

                        if (!this.isStartedGettingOrders && this.safetyLimits.isStartedGettingOrders()) {
                            // will add the list again; being added twice is less dangerous than not being added at all
                            final String eventId = info.fmro.shared.utility.Formulas.getEventIdOfMarketId(this.marketId, Statics.marketCataloguesMap);
                            this.safetyLimits.addInstructionsList(eventId, this.marketId, this.placeInstructionsList);
                        } else { // added at beginning, and no need to add again
                        }
                    } else { // Statics.notPlacingOrders || Statics.denyBetting.get()
                        logger.warn("order placing denied {} {}: marketId = {}, placeInstructionsList = {}", Statics.notPlacingOrders, Statics.denyBetting.get(), this.marketId, Generic.objectToString(this.placeInstructionsList));
                    }

                    Statics.pendingOrdersThread.addCancelAllOrder(0L);

                    Generic.threadSleep(100L); // avoid some double posting right after the order is placed; setting the delay too large might pose other problems

                    //noinspection ReuseOfLocalVariable
                    counter = 0;
                    for (final PlaceInstruction placeInstruction : this.placeInstructionsList) {
                        final LimitOrder limitOrder = placeInstruction.getLimitOrder();
                        if (pricesArray[counter] > 0d) {
                            limitOrder.setPrice(pricesArray[counter]);
                        }

                        final OrderPrice orderPrice = new OrderPrice(this.marketId, placeInstruction.getSelectionId(), placeInstruction.getSide(), limitOrder.getPrice());
                        final double orderSize = limitOrder.getSize();
                        Formulas.removeInstruction(orderPrice, orderSize);

                        counter++;
                    } // end for
                } else {
                    logger.error("STRANGE null or empty variables in RescriptOpThread placeOrders");
                }
                break;
            case cancelAllOrders:
                logger.error("cancelAllOrders is an old operation from the old disabled safeBets support; it should not be used");
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
                logger.error("unknown operation in RescriptOpThread: {}", this.operation);
                break;
        } // end switch
    }
}
