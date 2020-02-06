package info.fmro.betty.betapi;

import com.google.gson.reflect.TypeToken;
import info.fmro.betty.entities.CurrentOrderSummary;
import info.fmro.betty.entities.CurrentOrderSummaryReport;
import info.fmro.betty.entities.EventResult;
import info.fmro.betty.entities.MarketBook;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.entities.AccountFundsResponse;
import info.fmro.shared.entities.CancelExecutionReport;
import info.fmro.shared.entities.CancelInstruction;
import info.fmro.shared.entities.ClearedOrderSummary;
import info.fmro.shared.entities.ClearedOrderSummaryReport;
import info.fmro.shared.entities.CurrencyRate;
import info.fmro.shared.entities.EventTypeResult;
import info.fmro.shared.entities.MarketFilter;
import info.fmro.shared.entities.MarketTypeResult;
import info.fmro.shared.entities.PlaceExecutionReport;
import info.fmro.shared.entities.PlaceInstruction;
import info.fmro.shared.entities.PriceProjection;
import info.fmro.shared.entities.RunnerId;
import info.fmro.shared.entities.TimeRange;
import info.fmro.shared.enums.ApiNgAccountOperation;
import info.fmro.shared.enums.ApiNgOperation;
import info.fmro.shared.enums.BetStatus;
import info.fmro.shared.enums.GroupBy;
import info.fmro.shared.enums.MarketProjection;
import info.fmro.shared.enums.MarketSort;
import info.fmro.shared.enums.MatchProjection;
import info.fmro.shared.enums.OrderBy;
import info.fmro.shared.enums.OrderProjection;
import info.fmro.shared.enums.Side;
import info.fmro.shared.enums.SortDir;
import info.fmro.shared.utility.Generic;
import org.apache.http.client.ResponseHandler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"UtilityClass", "OverlyCoupledClass"})
public final class ApiNgRescriptOperations {
    private static final Logger logger = LoggerFactory.getLogger(ApiNgRescriptOperations.class);
    public static final String FILTER = "filter";
    public static final String LOCALE = "locale";
    public static final String SORT = "sort";
    public static final String MAX_RESULTS = "maxResults";
    public static final String MARKET_IDS = "marketIds";
    public static final String MARKET_ID = "marketId";
    public static final String INSTRUCTIONS = "instructions";
    public static final String CUSTOMER_REF = "customerRef";
    public static final String MARKET_PROJECTION = "marketProjection";
    public static final String PRICE_PROJECTION = "priceProjection";
    public static final String MATCH_PROJECTION = "matchProjection";
    public static final String ORDER_PROJECTION = "orderProjection";
    public static final String BET_IDS = "betIds";
    public static final String DATE_RANGE = "dateRange";
    public static final String ORDER_BY = "orderBy";
    public static final String SORT_DIR = "sortDir";
    public static final String FROM_RECORD = "fromRecord";
    public static final String RECORD_COUNT = "recordCount";
    public static final String BET_STATUS = "betStatus";
    public static final String EVENT_TYPE_IDS = "eventTypeIds";
    public static final String EVENT_IDS = "eventIds";
    public static final String RUNNER_IDS = "runnerIds";
    public static final String SIDE = "side";
    public static final String SETTLED_DATE_RANGE = "settledDateRange";
    public static final String GROUP_BY = "groupBy";
    public static final String INCLUDE_ITEM_DESCRIPTION = "includeItemDescription";
    public static final String CURRENCY_CODE = "currencyCode";
    public static final String localeString = Locale.getDefault().toString();

    @Contract(pure = true)
    private ApiNgRescriptOperations() {
    }

    @NotNull
    public static HashSet<CurrentOrderSummary> listCurrentOrders(final Set<String> betIds, final Set<String> marketIds, final OrderProjection orderProjection, final TimeRange placedDateRange, final OrderBy orderBy, final SortDir sortDir,
                                                                 final int fromRecord, final int recordCount, final String appKeyString, final RescriptResponseHandler rescriptResponseHandler) {
        final HashSet<CurrentOrderSummary> currentOrderSummarySet = new HashSet<>(16, 0.75f); // empty in the beginning; its size will be used in the loop
        boolean moreAvailable;
        int counterWhile = 0;
        do {
            final int localFromRecord = fromRecord + currentOrderSummarySet.size();
            final CurrentOrderSummaryReport currentOrderSummaryReport = listCurrentOrdersReport(betIds, marketIds, orderProjection, placedDateRange, orderBy, sortDir, localFromRecord, recordCount, appKeyString, rescriptResponseHandler);

            if (currentOrderSummaryReport != null) {
                final List<CurrentOrderSummary> currentOrderSummaryList = currentOrderSummaryReport.getCurrentOrders();
                if (currentOrderSummaryList != null) {
                    currentOrderSummarySet.addAll(currentOrderSummaryList);
                } else {
                    logger.error("null currentOrderSummaryList in listCurrentOrders for: {}", Generic.objectToString(currentOrderSummaryReport));
                }
                moreAvailable = currentOrderSummaryReport.getMoreAvailable();
            } else {
                if (Statics.mustStop.get() && Statics.needSessionToken.get()) { // normal to happen during program stop, if not logged in
                } else {
                    logger.error("null currentOrderSummaryReport in listCurrentOrders");
                }
                moreAvailable = false;
            }
            counterWhile++;
        } while (moreAvailable && counterWhile < 100);
        if (counterWhile >= 100) {
            logger.error("too many iterations in listCurrentOrders while: {} {} {} {}", counterWhile, moreAvailable, fromRecord, recordCount);
        }

        return currentOrderSummarySet;
    }

    private static CurrentOrderSummaryReport listCurrentOrdersReport(final Set<String> betIds, final Set<String> marketIds, final OrderProjection orderProjection, final TimeRange placedDateRange, final OrderBy orderBy, final SortDir sortDir,
                                                                     final int fromRecord, final int recordCount, final String appKeyString, final RescriptResponseHandler rescriptResponseHandler) {
        final Map<String, Object> paramsHashMap = new HashMap<>(16, 0.75f);
        paramsHashMap.put(BET_IDS, betIds);
        paramsHashMap.put(MARKET_IDS, marketIds);
        paramsHashMap.put(ORDER_PROJECTION, orderProjection);
        paramsHashMap.put(DATE_RANGE, placedDateRange);
        paramsHashMap.put(ORDER_BY, orderBy);
        paramsHashMap.put(SORT_DIR, sortDir);
        paramsHashMap.put(FROM_RECORD, fromRecord);
        paramsHashMap.put(RECORD_COUNT, recordCount);
        final String responseString = makeRequest(ApiNgOperation.LISTCURRENTORDERS.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 200)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, CurrentOrderSummaryReport.class);
    }

    @NotNull
    public static HashSet<ClearedOrderSummary> listClearedOrders(final BetStatus betStatus, final Set<String> eventTypeIds, final Set<String> eventIds, final Set<String> marketIds, final Set<RunnerId> runnerIds, final Set<String> betIds, final Side side,
                                                                 final TimeRange settledDateRange, final GroupBy groupBy, final boolean includeItemDescription, final int fromRecord, final int recordCount, final String appKeyString,
                                                                 final RescriptResponseHandler rescriptResponseHandler) {
        final HashSet<ClearedOrderSummary> clearedOrderSummarySet = new HashSet<>(16, 0.75f); // empty in the beginning; its size will be used in the loop
        boolean moreAvailable;
        int counterWhile = 0;
        do {
            final int localFromRecord = fromRecord + clearedOrderSummarySet.size();
            final ClearedOrderSummaryReport clearedOrderSummaryReport =
                    listClearedOrdersReport(betStatus, eventTypeIds, eventIds, marketIds, runnerIds, betIds, side, settledDateRange, groupBy, includeItemDescription, localFromRecord, recordCount, appKeyString, rescriptResponseHandler);

            if (clearedOrderSummaryReport != null) {
                final List<ClearedOrderSummary> clearedOrderSummaryList = clearedOrderSummaryReport.getClearedOrders();
                if (clearedOrderSummaryList != null) {
                    clearedOrderSummarySet.addAll(clearedOrderSummaryList);
                } else {
                    logger.error("null clearedOrderSummaryList in listCurrentOrders for: {}", Generic.objectToString(clearedOrderSummaryReport));
                }
                moreAvailable = clearedOrderSummaryReport.getMoreAvailable();
            } else {
                if (Statics.mustStop.get() && Statics.needSessionToken.get()) { // normal to happen during program stop, if not logged in
                } else {
                    logger.error("null clearedOrderSummaryReport in listClearedOrders");
                }
                moreAvailable = false;
            }
            counterWhile++;
        } while (moreAvailable && counterWhile < 100);
        if (counterWhile >= 100) {
            logger.error("too many iterations in listClearedOrders while: {} {} {} {}", counterWhile, moreAvailable, fromRecord, recordCount);
        }

        return clearedOrderSummarySet;
    }

    private static ClearedOrderSummaryReport listClearedOrdersReport(final BetStatus betStatus, final Set<String> eventTypeIds, final Set<String> eventIds, final Set<String> marketIds, final Set<RunnerId> runnerIds, final Set<String> betIds,
                                                                     final Side side, final TimeRange settledDateRange, final GroupBy groupBy, final boolean includeItemDescription, final int fromRecord, final int recordCount, final String appKeyString,
                                                                     final RescriptResponseHandler rescriptResponseHandler) {
        final Map<String, Object> paramsHashMap = new HashMap<>(16, 0.75f);
        paramsHashMap.put(BET_STATUS, betStatus);
        paramsHashMap.put(EVENT_TYPE_IDS, eventTypeIds);
        paramsHashMap.put(EVENT_IDS, eventIds);
        paramsHashMap.put(MARKET_IDS, marketIds);
        paramsHashMap.put(RUNNER_IDS, runnerIds);
        paramsHashMap.put(BET_IDS, betIds);
        paramsHashMap.put(SIDE, side);
        paramsHashMap.put(SETTLED_DATE_RANGE, settledDateRange);
        paramsHashMap.put(GROUP_BY, groupBy);
        paramsHashMap.put(INCLUDE_ITEM_DESCRIPTION, includeItemDescription);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(FROM_RECORD, fromRecord);
        paramsHashMap.put(RECORD_COUNT, recordCount);
        final String responseString = makeRequest(ApiNgOperation.LISTCLEAREDORDERS.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 201)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, ClearedOrderSummaryReport.class);
    }

    public static List<MarketTypeResult> listMarketTypes(final MarketFilter marketFilter, final String appKeyString, final RescriptResponseHandler rescriptResponseHandler) {
        final Map<String, Object> paramsHashMap = new HashMap<>(4, 0.75f);
        paramsHashMap.put(FILTER, marketFilter);
        final String responseString = makeRequest(ApiNgOperation.LISTMARKETTYPES.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 114)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, new TypeToken<List<MarketTypeResult>>() {
        }.getType());
    }

    public static List<EventTypeResult> listEventTypes(final MarketFilter marketFilter, final String appKeyString, final RescriptResponseHandler rescriptResponseHandler) {
        final Map<String, Object> paramsHashMap = new HashMap<>(4, 0.75f);
        paramsHashMap.put(FILTER, marketFilter);
        final String responseString = makeRequest(ApiNgOperation.LISTEVENTTYPES.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 115)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, new TypeToken<List<EventTypeResult>>() {
        }.getType());
    }

    public static List<MarketBook> listMarketBook(final List<String> marketIdsList, final PriceProjection priceProjection, final OrderProjection orderProjection, final MatchProjection matchProjection, final String currencyCodeString,
                                                  final String appKeyString, final RescriptResponseHandler rescriptResponseHandler) {
        final Map<String, Object> paramsHashMap = new HashMap<>(8, 0.75f);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(MARKET_IDS, marketIdsList);
        paramsHashMap.put(PRICE_PROJECTION, priceProjection);
        paramsHashMap.put(ORDER_PROJECTION, orderProjection);
        paramsHashMap.put(MATCH_PROJECTION, matchProjection);
        paramsHashMap.put(CURRENCY_CODE, currencyCodeString);
        final String responseString = makeRequest(ApiNgOperation.LISTMARKETBOOK.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 116)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, new TypeToken<List<MarketBook>>() {
        }.getType());
    }

    public static List<EventResult> listEvents(final MarketFilter marketFilter, final String appKeyString, final RescriptResponseHandler rescriptResponseHandler) {
        final Map<String, Object> paramsHashMap = new HashMap<>(4, 0.75f);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(FILTER, marketFilter); // mandatory

        final String responseString = makeRequest(ApiNgOperation.LISTEVENTS.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 117)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, new TypeToken<List<EventResult>>() {
        }.getType());
    }

    public static List<MarketCatalogue> listMarketCatalogue(final MarketFilter marketFilter, final Set<MarketProjection> marketProjectionsSet, final MarketSort marketSort, final int maxResults, final String appKeyString,
                                                            final RescriptResponseHandler rescriptResponseHandler) {
        final Map<String, Object> paramsHashMap = new HashMap<>(8, 0.75f);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(FILTER, marketFilter);
        if (marketSort != null) {
            paramsHashMap.put(SORT, marketSort);
        }
        paramsHashMap.put(MAX_RESULTS, maxResults);
        if (marketProjectionsSet != null) {
            paramsHashMap.put(MARKET_PROJECTION, marketProjectionsSet);
        }
        final String responseString = makeRequest(ApiNgOperation.LISTMARKETCATALOGUE.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 118)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, new TypeToken<List<MarketCatalogue>>() {
        }.getType());
    }

    public static PlaceExecutionReport placeOrders(final String marketIdString, final Collection<PlaceInstruction> placeInstructionsList, final String customerRefString, final String appKeyString, final RescriptResponseHandler rescriptResponseHandler) {
        final Map<String, Object> paramsHashMap = new HashMap<>(8, 0.75f);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(MARKET_ID, marketIdString);
        paramsHashMap.put(INSTRUCTIONS, placeInstructionsList);
        paramsHashMap.put(CUSTOMER_REF, customerRefString);

        if (placeInstructionsList != null) {
            Statics.safetyLimits.speedLimit.newOrders(placeInstructionsList.size());
        } else {
            logger.error("null placeInstructionsList in placeOrders for: {}", marketIdString);
        }
        final String responseString = makeRequest(ApiNgOperation.PLACEORDERS.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 119)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, PlaceExecutionReport.class);
    }

    public static CancelExecutionReport cancelOrders(final String marketIdString, final List<CancelInstruction> cancelInstructionsList, final String customerRefString, final String appKeyString, final RescriptResponseHandler rescriptResponseHandler) {
        if (marketIdString == null || cancelInstructionsList == null) {
            logger.error("null marketIdString or cancelInstructionsList in cancelOrders for: {} {}", marketIdString, Generic.objectToString(cancelInstructionsList));
        } else if (cancelInstructionsList.size() > 60) {
            logger.error("too many {} instructions in cancelInstructionsList for: {} {}", cancelInstructionsList.size(), marketIdString, Generic.objectToString(cancelInstructionsList));
        }

        final Map<String, Object> paramsHashMap = new HashMap<>(8, 0.75f);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(MARKET_ID, marketIdString);
        paramsHashMap.put(INSTRUCTIONS, cancelInstructionsList);
        paramsHashMap.put(CUSTOMER_REF, customerRefString);
        final String responseString = makeRequest(ApiNgOperation.CANCELORDERS.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 120)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, CancelExecutionReport.class);
    }

    @SuppressWarnings("WeakerAccess")
    public static AccountFundsResponse getAccountFunds(final String appKeyString, final ResponseHandler<String> rescriptAccountResponseHandler) {
        final Map<String, Object> paramsHashMap = new HashMap<>(2, 0.75f);

        final String responseString = makeAccountRequest(ApiNgAccountOperation.GETACCOUNTFUNDS.getOperationName(), paramsHashMap, appKeyString, rescriptAccountResponseHandler);
        if (Statics.debugLevel.check(3, 121)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, AccountFundsResponse.class);
    }

    @SuppressWarnings("WeakerAccess")
    public static List<CurrencyRate> listCurrencyRates(final String appKeyString, final ResponseHandler<String> rescriptAccountResponseHandler) {
        final Map<String, Object> paramsHashMap = new HashMap<>(2, 0.75f);

        final String responseString = makeAccountRequest(ApiNgAccountOperation.LISTCURRENCYRATES.getOperationName(), paramsHashMap, appKeyString, rescriptAccountResponseHandler);
        if (Statics.debugLevel.check(3, 121)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, new TypeToken<List<CurrencyRate>>() {
        }.getType());
    }

    private static String makeRequest(final String operationString, @NotNull final Map<? super String, Object> paramsMap, final String appKeyString, final RescriptResponseHandler rescriptResponseHandler) {
        final String requestString;
        //Handling the Rescript request
        paramsMap.put("id", 1);

        requestString = JsonConverter.convertToJson(paramsMap);
        if (Statics.debugLevel.check(3, 122)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsMap, false, false), requestString, System.currentTimeMillis());
        }

        //We need to pass the "sendPostRequest" method a string in util format:  requestString
        return HttpUtil.sendPostRequestRescript(requestString, operationString, appKeyString, rescriptResponseHandler);
    }

    private static String makeAccountRequest(final String operationString, @NotNull final Map<? super String, Object> paramsMap, final String appKeyString, final ResponseHandler<String> rescriptAccountResponseHandler) {
        final String requestString;
        //Handling the Rescript request
        paramsMap.put("id", 1);

        requestString = JsonConverter.convertToJson(paramsMap);
        if (Statics.debugLevel.check(3, 123)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsMap, false, false), requestString, System.currentTimeMillis());
        }

        //We need to pass the "sendPostRequest" method a string in util format:  requestString
        return HttpUtil.sendPostRequestAccountRescript(requestString, operationString, appKeyString, rescriptAccountResponseHandler);
    }
}
