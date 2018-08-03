package info.fmro.betty.main;

import com.google.gson.reflect.TypeToken;
import info.fmro.betty.entities.AccountFundsResponse;
import info.fmro.betty.entities.CancelExecutionReport;
import info.fmro.betty.entities.CancelInstruction;
import info.fmro.betty.entities.ClearedOrderSummary;
import info.fmro.betty.entities.ClearedOrderSummaryReport;
import info.fmro.betty.entities.CurrentOrderSummary;
import info.fmro.betty.entities.CurrentOrderSummaryReport;
import info.fmro.betty.entities.EventResult;
import info.fmro.betty.entities.EventTypeResult;
import info.fmro.betty.entities.MarketBook;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.entities.MarketFilter;
import info.fmro.betty.entities.MarketTypeResult;
import info.fmro.betty.entities.PlaceExecutionReport;
import info.fmro.betty.entities.PlaceInstruction;
import info.fmro.betty.entities.PriceProjection;
import info.fmro.betty.entities.RunnerId;
import info.fmro.betty.entities.TimeRange;
import info.fmro.betty.enums.ApiNgAccountOperation;
import info.fmro.betty.enums.ApiNgOperation;
import info.fmro.betty.enums.BetStatus;
import info.fmro.betty.enums.GroupBy;
import info.fmro.betty.enums.MarketProjection;
import info.fmro.betty.enums.MarketSort;
import info.fmro.betty.enums.MatchProjection;
import info.fmro.betty.enums.OrderBy;
import info.fmro.betty.enums.OrderProjection;
import info.fmro.betty.enums.Side;
import info.fmro.betty.enums.SortDir;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ApiNgRescriptOperations {

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

    public static final String localeString = Locale.getDefault().toString();

    private ApiNgRescriptOperations() {
    }

    public static HashSet<CurrentOrderSummary> listCurrentOrders(Set<String> betIds, Set<String> marketIds, OrderProjection orderProjection, TimeRange placedDateRange, OrderBy orderBy, SortDir sortDir, int fromRecord, int recordCount, String appKeyString,
                                                                 RescriptResponseHandler rescriptResponseHandler) {

        final HashSet<CurrentOrderSummary> currentOrderSummarySet = new HashSet<>(16, 0.75f); // empty in the beginning; its size will be used in the loop
        boolean moreAvailable;
        int counterWhile = 0;
        do {
            int localFromRecord = fromRecord + currentOrderSummarySet.size();
            final CurrentOrderSummaryReport currentOrderSummaryReport = listCurrentOrdersReport(betIds, marketIds, orderProjection, placedDateRange, orderBy, sortDir, localFromRecord, recordCount, appKeyString, rescriptResponseHandler);

            if (currentOrderSummaryReport != null) {
                currentOrderSummarySet.addAll(currentOrderSummaryReport.getCurrentOrders());
                moreAvailable = currentOrderSummaryReport.isMoreAvailable();
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

    public static CurrentOrderSummaryReport listCurrentOrdersReport(Set<String> betIds, Set<String> marketIds, OrderProjection orderProjection, TimeRange placedDateRange, OrderBy orderBy, SortDir sortDir, int fromRecord, int recordCount,
                                                                    String appKeyString, RescriptResponseHandler rescriptResponseHandler) {
        final HashMap<String, Object> paramsHashMap = new HashMap<>(16, 0.75f);
        paramsHashMap.put(BET_IDS, betIds);
        paramsHashMap.put(MARKET_IDS, marketIds);
        paramsHashMap.put(ORDER_PROJECTION, orderProjection);
        paramsHashMap.put(DATE_RANGE, placedDateRange);
        paramsHashMap.put(ORDER_BY, orderBy);
        paramsHashMap.put(SORT_DIR, sortDir);
        paramsHashMap.put(FROM_RECORD, fromRecord);
        paramsHashMap.put(RECORD_COUNT, recordCount);
        final String responseString = ApiNgRescriptOperations.makeRequest(ApiNgOperation.LISTCURRENTORDERS.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 200)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        final CurrentOrderSummaryReport containerCurrentOrderSummaryReport = JsonConverter.convertFromJson(responseString, CurrentOrderSummaryReport.class);

        return containerCurrentOrderSummaryReport;
    }

    public static HashSet<ClearedOrderSummary> listClearedOrders(BetStatus betStatus, Set<String> eventTypeIds, Set<String> eventIds, Set<String> marketIds, Set<RunnerId> runnerIds, Set<String> betIds, Side side, TimeRange settledDateRange,
                                                                 GroupBy groupBy, boolean includeItemDescription, int fromRecord, int recordCount, String appKeyString, RescriptResponseHandler rescriptResponseHandler) {

        final HashSet<ClearedOrderSummary> clearedOrderSummarySet = new HashSet<>(16, 0.75f); // empty in the beginning; its size will be used in the loop
        boolean moreAvailable;
        int counterWhile = 0;
        do {
            int localFromRecord = fromRecord + clearedOrderSummarySet.size();
            final ClearedOrderSummaryReport clearedOrderSummaryReport =
                    listClearedOrdersReport(betStatus, eventTypeIds, eventIds, marketIds, runnerIds, betIds, side, settledDateRange, groupBy, includeItemDescription, localFromRecord, recordCount, appKeyString, rescriptResponseHandler);

            if (clearedOrderSummaryReport != null) {
                clearedOrderSummarySet.addAll(clearedOrderSummaryReport.getClearedOrders());
                moreAvailable = clearedOrderSummaryReport.isMoreAvailable();
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

    public static ClearedOrderSummaryReport listClearedOrdersReport(BetStatus betStatus, Set<String> eventTypeIds, Set<String> eventIds, Set<String> marketIds, Set<RunnerId> runnerIds, Set<String> betIds, Side side, TimeRange settledDateRange,
                                                                    GroupBy groupBy, boolean includeItemDescription, int fromRecord, int recordCount, String appKeyString, RescriptResponseHandler rescriptResponseHandler) {
        final HashMap<String, Object> paramsHashMap = new HashMap<>(16, 0.75f);
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
        final String responseString = ApiNgRescriptOperations.makeRequest(ApiNgOperation.LISTCLEAREDORDERS.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 201)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        final ClearedOrderSummaryReport containerClearedOrderSummaryReport = JsonConverter.convertFromJson(responseString, ClearedOrderSummaryReport.class);

        return containerClearedOrderSummaryReport;
    }

    public static List<MarketTypeResult> listMarketTypes(MarketFilter marketFilter, String appKeyString, RescriptResponseHandler rescriptResponseHandler) {
        final HashMap<String, Object> paramsHashMap = new HashMap<>(4, 0.75f);
        paramsHashMap.put(FILTER, marketFilter);
        final String responseString = ApiNgRescriptOperations.makeRequest(ApiNgOperation.LISTMARKETTYPES.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 114)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        final List<MarketTypeResult> marketTypeResultList = JsonConverter.convertFromJson(responseString, new TypeToken<List<MarketTypeResult>>() {
        }.getType());

        return marketTypeResultList;
    }

    public static List<EventTypeResult> listEventTypes(MarketFilter marketFilter, String appKeyString, RescriptResponseHandler rescriptResponseHandler) {
        final HashMap<String, Object> paramsHashMap = new HashMap<>(4, 0.75f);
        paramsHashMap.put(FILTER, marketFilter);
        final String responseString = ApiNgRescriptOperations.makeRequest(ApiNgOperation.LISTEVENTTYPES.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 115)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        final List<EventTypeResult> containerEventTypeResult = JsonConverter.convertFromJson(responseString, new TypeToken<List<EventTypeResult>>() {
        }.getType());

        return containerEventTypeResult;
    }

    public static List<MarketBook> listMarketBook(List<String> marketIdsList, PriceProjection priceProjection, OrderProjection orderProjection, MatchProjection matchProjection, String currencyCodeString, String appKeyString,
                                                  RescriptResponseHandler rescriptResponseHandler) {
        final HashMap<String, Object> paramsHashMap = new HashMap<>(8, 0.75f);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(MARKET_IDS, marketIdsList);
        paramsHashMap.put(PRICE_PROJECTION, priceProjection);
        paramsHashMap.put(ORDER_PROJECTION, orderProjection);
        paramsHashMap.put(MATCH_PROJECTION, matchProjection);
        final String responseString = ApiNgRescriptOperations.makeRequest(ApiNgOperation.LISTMARKETBOOK.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 116)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        final List<MarketBook> containerMarketBook = JsonConverter.convertFromJson(responseString, new TypeToken<List<MarketBook>>() {
        }.getType());

        return containerMarketBook;
    }

    public static List<EventResult> listEvents(MarketFilter marketFilter, String appKeyString, RescriptResponseHandler rescriptResponseHandler) {
        final HashMap<String, Object> paramsHashMap = new HashMap<>(4, 0.75f);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(FILTER, marketFilter); // mandatory

        final String responseString = ApiNgRescriptOperations.makeRequest(ApiNgOperation.LISTEVENTS.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 117)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        final List<EventResult> eventResultsList = JsonConverter.convertFromJson(responseString, new TypeToken<List<EventResult>>() {
        }.getType());

        return eventResultsList;
    }

    public static List<MarketCatalogue> listMarketCatalogue(MarketFilter marketFilter, Set<MarketProjection> marketProjectionsSet, MarketSort marketSort, int maxResults, String appKeyString, RescriptResponseHandler rescriptResponseHandler) {
        final HashMap<String, Object> paramsHashMap = new HashMap<>(8, 0.75f);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(FILTER, marketFilter);
        if (marketSort != null) {
            paramsHashMap.put(SORT, marketSort);
        }
        paramsHashMap.put(MAX_RESULTS, maxResults);
        if (marketProjectionsSet != null) {
            paramsHashMap.put(MARKET_PROJECTION, marketProjectionsSet);
        }
        final String responseString = ApiNgRescriptOperations.makeRequest(ApiNgOperation.LISTMARKETCATALOGUE.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 118)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        final List<MarketCatalogue> containerMarketCatalogue = JsonConverter.convertFromJson(responseString, new TypeToken<List<MarketCatalogue>>() {
        }.getType());

        return containerMarketCatalogue;
    }

    public static PlaceExecutionReport placeOrders(String marketIdString, List<PlaceInstruction> placeInstructionsList, String customerRefString, String appKeyString, RescriptResponseHandler rescriptResponseHandler) {
        final HashMap<String, Object> paramsHashMap = new HashMap<>(8, 0.75f);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(MARKET_ID, marketIdString);
        paramsHashMap.put(INSTRUCTIONS, placeInstructionsList);
        paramsHashMap.put(CUSTOMER_REF, customerRefString);
        final String responseString = ApiNgRescriptOperations.makeRequest(ApiNgOperation.PLACEORDERS.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 119)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, PlaceExecutionReport.class);
    }

    public static CancelExecutionReport cancelOrders(String marketIdString, List<CancelInstruction> cancelInstructionsList, String customerRefString, String appKeyString, RescriptResponseHandler rescriptResponseHandler) {
        final HashMap<String, Object> paramsHashMap = new HashMap<>(8, 0.75f);
        // paramsHashMap.put(LOCALE, localeString);
        paramsHashMap.put(MARKET_ID, marketIdString);
        paramsHashMap.put(INSTRUCTIONS, cancelInstructionsList);
        paramsHashMap.put(CUSTOMER_REF, customerRefString);
        final String responseString = ApiNgRescriptOperations.makeRequest(ApiNgOperation.CANCELORDERS.getOperationName(), paramsHashMap, appKeyString, rescriptResponseHandler);
        if (Statics.debugLevel.check(3, 120)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, CancelExecutionReport.class);
    }

    public static AccountFundsResponse getAccountFunds(String appKeyString, String ssoIdString, RescriptAccountResponseHandler rescriptAccountResponseHandler) {
        final HashMap<String, Object> paramsHashMap = new HashMap<>(2, 0.75f);

        final String responseString = ApiNgRescriptOperations.makeAccountRequest(ApiNgAccountOperation.GETACCOUNTFUNDS.getOperationName(), paramsHashMap, appKeyString,
                                                                                 rescriptAccountResponseHandler);
        if (Statics.debugLevel.check(3, 121)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsHashMap, false, false), responseString, System.currentTimeMillis());
        }

        return JsonConverter.convertFromJson(responseString, AccountFundsResponse.class);
    }

    public static String makeRequest(String operationString, Map<String, Object> paramsMap, String appKeyString, RescriptResponseHandler rescriptResponseHandler) {
        final String requestString;
        //Handling the Rescript request
        paramsMap.put("id", 1);

        requestString = JsonConverter.convertToJson(paramsMap);
        if (Statics.debugLevel.check(3, 122)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsMap, false, false), requestString, System.currentTimeMillis());
        }

        //We need to pass the "sendPostRequest" method a string in util format:  requestString
        final String responseString = HttpUtil.sendPostRequestRescript(requestString, operationString, appKeyString, rescriptResponseHandler);

        return responseString;
    }

    public static String makeAccountRequest(String operationString, Map<String, Object> paramsMap, String appKeyString, RescriptAccountResponseHandler rescriptAccountResponseHandler) {
        final String requestString;
        //Handling the Rescript request
        paramsMap.put("id", 1);

        requestString = JsonConverter.convertToJson(paramsMap);
        if (Statics.debugLevel.check(3, 123)) {
            logger.info("params: {} Response: {} timeStamp={}", Generic.objectToString(paramsMap, false, false), requestString, System.currentTimeMillis());
        }

        //We need to pass the "sendPostRequest" method a string in util format:  requestString
        final String responseString = HttpUtil.sendPostRequestAccountRescript(requestString, operationString, appKeyString, rescriptAccountResponseHandler);

        return responseString;
    }
}
