package info.fmro.betty.utility;

import info.fmro.betty.betapi.ApiNgRescriptOperations;
import info.fmro.betty.betapi.RescriptResponseHandler;
import info.fmro.shared.entities.CurrentOrderSummary;
import info.fmro.shared.entities.EventResult;
import info.fmro.shared.entities.MarketBook;
import info.fmro.shared.entities.MarketCatalogue;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.entities.ClearedOrderSummary;
import info.fmro.shared.entities.ExBestOffersOverrides;
import info.fmro.shared.entities.MarketDescription;
import info.fmro.shared.entities.MarketFilter;
import info.fmro.shared.entities.MarketTypeResult;
import info.fmro.shared.entities.PriceProjection;
import info.fmro.shared.enums.BetStatus;
import info.fmro.shared.enums.MarketProjection;
import info.fmro.shared.enums.OrderBy;
import info.fmro.shared.enums.OrderProjection;
import info.fmro.shared.enums.PriceData;
import info.fmro.shared.enums.RollupModel;
import info.fmro.shared.enums.SortDir;
import info.fmro.shared.stream.cache.market.Market;
import info.fmro.shared.stream.cache.order.OrderMarket;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"UtilityClass", "ReuseOfLocalVariable"})
public final class DebuggingMethods {
    private static final Logger logger = LoggerFactory.getLogger(DebuggingMethods.class);

    @Contract(pure = true)
    private DebuggingMethods() {
    }

    public static void printCachedOrders() {
        final Iterable<OrderMarket> orderMarkets = Statics.orderCache.getOrderMarkets();
        for (final OrderMarket orderMarket : orderMarkets) {
            logger.info("listing orderMarket: {}", Generic.objectToString(orderMarket));
        }
    }

    public static void printCachedMarkets(@NotNull final String marketIds) {
        final String[] marketIdsArray = marketIds.split(" ");
        for (final String marketId : marketIdsArray) {
            final Market market = Statics.marketCache.getMarket(marketId);
            final String toPrint = market == null ? "null " + marketId : Generic.objectToString(market);
            logger.info("listing market: {}", toPrint);
        }
    }

    public static void listCurrentOrders() {
        final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        @NotNull final HashSet<CurrentOrderSummary> currentOrderSummarySet =
                ApiNgRescriptOperations.listCurrentOrders(null, null, OrderProjection.ALL, null, OrderBy.BY_PLACE_TIME, SortDir.EARLIEST_TO_LATEST, 0, 0, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("listCurrentOrders size: {}", currentOrderSummarySet.size());
        logger.info("listCurrentOrders: {}", Generic.objectToString(currentOrderSummarySet));
    }

    public static void listClearedOrders() {
        final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        final HashSet<ClearedOrderSummary> clearedOrderSummarySet =
                ApiNgRescriptOperations.listClearedOrders(BetStatus.SETTLED, null, null, null, null, null, null, null, null, true, 0, 0,
                                                          Statics.appKey.get(), rescriptResponseHandler);

        logger.info("listClearedOrders size: {}", clearedOrderSummarySet.size());
        logger.info("listClearedOrders: {}", Generic.objectToString(clearedOrderSummarySet));
    }

    public static void findNewMarketTypes() {
        final Set<String> eventTypeIdsSet = new HashSet<>(2, 0.75f);
        eventTypeIdsSet.add("1"); // soccer
        final MarketFilter marketFilter = new MarketFilter();
        marketFilter.setEventTypeIds(eventTypeIdsSet);

        final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        final List<MarketTypeResult> marketTypeResultList = ApiNgRescriptOperations.listMarketTypes(marketFilter, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("marketTypeResultList size: {}", marketTypeResultList == null ? null : marketTypeResultList.size());

        int counterNewMarketType = 0;
        if (marketTypeResultList != null) {
            for (final MarketTypeResult marketTypeResult : marketTypeResultList) {
                final String marketType = marketTypeResult.getMarketType();
                if (Statics.marketTypes.add(marketType)) {
                    logger.info("new marketType: {}", Generic.objectToString(marketTypeResult));
                    counterNewMarketType++;
                }
            } // end for
        } // end if
        logger.info("new marketTypes: {}", counterNewMarketType);
    }

    public static void printMarket(final String marketId) {
        final List<String> marketIdsList = Collections.singletonList(marketId);
        final PriceProjection priceProjection = new PriceProjection();
        // priceProjection.setVirtualise(true);
        final EnumSet<PriceData> priceDataSet = EnumSet.of(PriceData.EX_ALL_OFFERS);
        // priceDataSet.add(PriceData.SP_AVAILABLE);
        // priceDataSet.add(PriceData.SP_TRADED);
//        priceDataSet.add(PriceData.EX_ALL_OFFERS);
        // priceDataSet.add(PriceData.EX_TRADED);
        priceProjection.setPriceData(priceDataSet);

        RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        final List<MarketBook> marketBookList = ApiNgRescriptOperations.listMarketBook(marketIdsList, priceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("printing marketBookList EX_ALL_OFFERS for {}: {}", marketId, Generic.objectToString(marketBookList));

//        priceProjection = new PriceProjection();
//        // priceProjection.setVirtualise(true);
//        priceDataSet = new HashSet<>(8, 0.75f);
//        // priceDataSet.add(PriceData.SP_AVAILABLE);
//        // priceDataSet.add(PriceData.SP_TRADED);
//        priceDataSet.add(PriceData.EX_BEST_OFFERS);
//        // priceDataSet.add(PriceData.EX_TRADED);
//        priceProjection.setPriceData(priceDataSet);
//        ExBestOffersOverrides exBestOffersOverrides = new ExBestOffersOverrides();
//        exBestOffersOverrides.setBestPricesDepth(2);
//        exBestOffersOverrides.setRollupModel(RollupModel.STAKE);
//        exBestOffersOverrides.setRollupLimit(0);
//        priceProjection.setExBestOffersOverrides(exBestOffersOverrides);
//
//        rescriptResponseHandler = new RescriptResponseHandler();
//        marketBookList = ApiNgRescriptOperations.listMarketBook(marketIdsList, priceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);
//
//        logger.info("printing marketBookList EX_BEST_OFFERS 2 limit 0 for {}: {}", marketId, Generic.objectToString(marketBookList));
        final MarketFilter marketFilter = new MarketFilter();
        final Set<String> marketIds = new HashSet<>(2, 0.75f);
        marketIds.add(marketId);
        marketFilter.setMarketIds(marketIds);
        final EnumSet<MarketProjection> marketProjectionsSet =
                EnumSet.of(MarketProjection.COMPETITION, MarketProjection.EVENT, MarketProjection.EVENT_TYPE, MarketProjection.MARKET_DESCRIPTION, MarketProjection.RUNNER_DESCRIPTION, MarketProjection.MARKET_START_TIME);
//        marketProjectionsSet.add(MarketProjection.COMPETITION);
//        marketProjectionsSet.add(MarketProjection.EVENT);
//        marketProjectionsSet.add(MarketProjection.EVENT_TYPE);
//        marketProjectionsSet.add(MarketProjection.MARKET_DESCRIPTION);
//        marketProjectionsSet.add(MarketProjection.RUNNER_DESCRIPTION);
//        marketProjectionsSet.add(MarketProjection.MARKET_START_TIME);
        // marketProjectionsSet.add(MarketProjection.RUNNER_METADATA); // gives strange recursion error when printing with objectToString

        rescriptResponseHandler = new RescriptResponseHandler();
        final List<MarketCatalogue> marketCatalogueList = ApiNgRescriptOperations.listMarketCatalogue(marketFilter, marketProjectionsSet, null, 200, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("printing marketCatalogueList for {}: {}", marketId, Generic.objectToString(marketCatalogueList));

        rescriptResponseHandler = new RescriptResponseHandler();
        final List<MarketTypeResult> marketTypeResultList = ApiNgRescriptOperations.listMarketTypes(marketFilter, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("printing marketTypeResultList for {}: {}", marketId, Generic.objectToString(marketTypeResultList));
    }

    public static void printEvent(final String eventId) {
        MarketFilter marketFilter = new MarketFilter();
        final Set<String> eventIds = new HashSet<>(2, 0.75f);
        eventIds.add(eventId);
        marketFilter.setEventIds(eventIds);

        RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        final List<EventResult> eventResultList = ApiNgRescriptOperations.listEvents(marketFilter, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("printing eventResultList for {}: {}", eventId, Generic.objectToString(eventResultList));

        rescriptResponseHandler = new RescriptResponseHandler();
        final List<MarketTypeResult> marketTypeResultList = ApiNgRescriptOperations.listMarketTypes(marketFilter, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("printing marketTypeResultList for {}: {}", eventId, Generic.objectToString(marketTypeResultList));

        final EnumSet<MarketProjection> marketProjectionsSet =
                EnumSet.of(MarketProjection.COMPETITION, MarketProjection.EVENT, MarketProjection.EVENT_TYPE, MarketProjection.MARKET_DESCRIPTION, MarketProjection.RUNNER_DESCRIPTION, MarketProjection.MARKET_START_TIME);
//        marketProjectionsSet.add(MarketProjection.COMPETITION);
//        marketProjectionsSet.add(MarketProjection.EVENT);
//        marketProjectionsSet.add(MarketProjection.EVENT_TYPE);
//        marketProjectionsSet.add(MarketProjection.MARKET_DESCRIPTION);
//        marketProjectionsSet.add(MarketProjection.RUNNER_DESCRIPTION);
//        marketProjectionsSet.add(MarketProjection.MARKET_START_TIME);

        marketFilter = new MarketFilter();
        marketFilter.setEventIds(eventIds);

        rescriptResponseHandler = new RescriptResponseHandler();
        final List<MarketCatalogue> marketCatalogueList = ApiNgRescriptOperations.listMarketCatalogue(marketFilter, marketProjectionsSet, null, 200, Statics.appKey.get(), rescriptResponseHandler);

        if (marketCatalogueList != null) {
            final HashSet<String> unknownMarketTypesSet = new HashSet<>(0, 0.75f), unknownMarketNamesSet = new HashSet<>(0, 0.75f);
            for (final MarketCatalogue marketCatalogue : marketCatalogueList) {
                final MarketDescription marketDescription = marketCatalogue.getDescription();
                final String marketType = marketDescription.getMarketType();
                if (marketType == null) {
                    logger.info("printing marketCatalogue for {} null marketType: {}", eventId, Generic.objectToString(marketCatalogue));
                    if (!Statics.marketNullTypes.contains(marketCatalogue.getMarketName())) {
                        unknownMarketNamesSet.add(marketCatalogue.getMarketName());
                    }
                } else {
                    logger.info("printing marketCatalogueList for {} {}: {}", eventId, marketType, Generic.objectToString(marketCatalogue));
                    if (!Statics.marketTypes.contains(marketType)) {
                        unknownMarketTypesSet.add(marketType);
                    }
                }
            } // end for
            logger.info("event: {} unknownMarketTypesSet size: {} unknownMarketNamesSet size: {}", eventId, unknownMarketTypesSet.size(), unknownMarketNamesSet.size());
            if (!unknownMarketTypesSet.isEmpty()) {
                logger.info("event: {} unknownMarketTypesSet: {}", eventId, unknownMarketTypesSet); // Generic.objectToString doesn't prins collections of String rigth
            }
            if (!unknownMarketNamesSet.isEmpty()) {
                logger.info("event: {} unknownMarketNamesSet: {}", eventId, unknownMarketNamesSet); // Generic.objectToString doesn't prins collections of String rigth
            }
        } else {
            logger.error("marketCatalogueList null in printEvent");
        }
    }

    public static void printMarketType(final String marketType) {
        final Set<String> eventTypeIdsSet = new HashSet<>(2, 0.75f);
        eventTypeIdsSet.add("1"); // soccer
        final Set<String> marketTypeCodes = new HashSet<>(2, 0.75f);
        marketTypeCodes.add(marketType);
        final MarketFilter marketFilter = new MarketFilter();
        marketFilter.setMarketTypeCodes(marketTypeCodes);
        marketFilter.setEventTypeIds(eventTypeIdsSet);

        RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        final List<EventResult> eventResultList = ApiNgRescriptOperations.listEvents(marketFilter, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("printing eventResultList for {}: {}", marketType, Generic.objectToString(eventResultList));

        final EnumSet<MarketProjection> marketProjectionsSet =
                EnumSet.of(MarketProjection.COMPETITION, MarketProjection.EVENT, MarketProjection.EVENT_TYPE, MarketProjection.MARKET_DESCRIPTION, MarketProjection.RUNNER_DESCRIPTION, MarketProjection.MARKET_START_TIME);
//        marketProjectionsSet.add(MarketProjection.COMPETITION);
//        marketProjectionsSet.add(MarketProjection.EVENT);
//        marketProjectionsSet.add(MarketProjection.EVENT_TYPE);
//        marketProjectionsSet.add(MarketProjection.MARKET_DESCRIPTION);
//        marketProjectionsSet.add(MarketProjection.RUNNER_DESCRIPTION);
//        marketProjectionsSet.add(MarketProjection.MARKET_START_TIME);

        rescriptResponseHandler = new RescriptResponseHandler();
        final List<MarketCatalogue> marketCatalogueList = ApiNgRescriptOperations.listMarketCatalogue(marketFilter, marketProjectionsSet, null, 200, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("printing marketCatalogueList for {}: {}", marketType, Generic.objectToString(marketCatalogueList));
    }

    public static void weightTest() {
        weightTest(null);
    }

    @SuppressWarnings("OverlyLongMethod")
    private static void weightTest(final String argMarketId) {
        // test ex all offers for weight 17/16 -> 11/12 ids; wieght 17 as expected
        // test EX_BEST_OFFERS 10 for weight 11/12/14/15 -> 18/16/14/13; weight 11
        // test EX_BEST_OFFERS 1 for weight 2/3/4/5 -> 100/66/50/40; weight 2
        // test EX_BEST_OFFERS 2 for weight 2 -> 100/101; weight 2

        final String marketId;
        // I have to assign this manually if I don't use a method argument
        marketId = argMarketId == null ? "1.115707687" : argMarketId;

        // marketIdsList = Arrays.asList(marketId);
        PriceProjection priceProjection = new PriceProjection();
        EnumSet<PriceData> priceDataSet = EnumSet.of(PriceData.EX_ALL_OFFERS);
//        priceDataSet.add(PriceData.EX_ALL_OFFERS);
        priceProjection.setPriceData(priceDataSet);

        List<String> marketIdsList = new ArrayList<>(11);
        for (int i = 0; i < 11; i++) {
            marketIdsList.add(marketId);
        }

        RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        ApiNgRescriptOperations.listMarketBook(marketIdsList, priceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("EX_ALL_OFFERS 11 ids tooMuchData: {}", rescriptResponseHandler.isTooMuchData());
        marketIdsList = new ArrayList<>(12);
        for (int i = 0; i < 12; i++) {
            marketIdsList.add(marketId);
        }

        rescriptResponseHandler = new RescriptResponseHandler();
        ApiNgRescriptOperations.listMarketBook(marketIdsList, priceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("EX_ALL_OFFERS 12 ids tooMuchData: {}", rescriptResponseHandler.isTooMuchData());

        priceProjection = new PriceProjection();
        priceDataSet = EnumSet.of(PriceData.EX_BEST_OFFERS);
//        priceDataSet.add(PriceData.EX_BEST_OFFERS);
        priceProjection.setPriceData(priceDataSet);
        ExBestOffersOverrides exBestOffersOverrides = new ExBestOffersOverrides();
        exBestOffersOverrides.setBestPricesDepth(10);
        exBestOffersOverrides.setRollupModel(RollupModel.STAKE);
        exBestOffersOverrides.setRollupLimit(0);
        priceProjection.setExBestOffersOverrides(exBestOffersOverrides);

        marketIdsList = new ArrayList<>(18);
        for (int i = 0; i < 18; i++) {
            marketIdsList.add(marketId);
        } // end for

        rescriptResponseHandler = new RescriptResponseHandler();
        ApiNgRescriptOperations.listMarketBook(marketIdsList, priceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("EX_BEST_OFFERS depth=10 18 ids tooMuchData: {}", rescriptResponseHandler.isTooMuchData());
        marketIdsList = new ArrayList<>(19);
        for (int i = 0; i < 19; i++) {
            marketIdsList.add(marketId);
        } // end for

        rescriptResponseHandler = new RescriptResponseHandler();
        ApiNgRescriptOperations.listMarketBook(marketIdsList, priceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("EX_BEST_OFFERS depth=10 19 ids tooMuchData: {}", rescriptResponseHandler.isTooMuchData());

        priceProjection = new PriceProjection();
        priceDataSet = EnumSet.of(PriceData.EX_BEST_OFFERS);
//        priceDataSet.add(PriceData.EX_BEST_OFFERS);
        priceProjection.setPriceData(priceDataSet);
        exBestOffersOverrides = new ExBestOffersOverrides();
        exBestOffersOverrides.setBestPricesDepth(1);
        exBestOffersOverrides.setRollupModel(RollupModel.STAKE);
        exBestOffersOverrides.setRollupLimit(1000000);
        priceProjection.setExBestOffersOverrides(exBestOffersOverrides);

        marketIdsList = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            marketIdsList.add(marketId);
        } // end for

        rescriptResponseHandler = new RescriptResponseHandler();
        ApiNgRescriptOperations.listMarketBook(marketIdsList, priceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("EX_BEST_OFFERS depth=1 100 ids tooMuchData: {}", rescriptResponseHandler.isTooMuchData());
        marketIdsList = new ArrayList<>(101);
        for (int i = 0; i < 101; i++) {
            marketIdsList.add(marketId);
        } // end for

        rescriptResponseHandler = new RescriptResponseHandler();
        ApiNgRescriptOperations.listMarketBook(marketIdsList, priceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("EX_BEST_OFFERS depth=1 101 ids tooMuchData: {}", rescriptResponseHandler.isTooMuchData());

        priceProjection = new PriceProjection();
        priceDataSet = EnumSet.of(PriceData.EX_BEST_OFFERS);
//        priceDataSet.add(PriceData.EX_BEST_OFFERS);
        priceProjection.setPriceData(priceDataSet);
        exBestOffersOverrides = new ExBestOffersOverrides();
        exBestOffersOverrides.setBestPricesDepth(2);
        exBestOffersOverrides.setRollupModel(RollupModel.STAKE);
        exBestOffersOverrides.setRollupLimit(0);
        priceProjection.setExBestOffersOverrides(exBestOffersOverrides);

        marketIdsList = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            marketIdsList.add(marketId);
        } // end for

        rescriptResponseHandler = new RescriptResponseHandler();
        ApiNgRescriptOperations.listMarketBook(marketIdsList, priceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("EX_BEST_OFFERS depth=2 100 ids tooMuchData: {}", rescriptResponseHandler.isTooMuchData());
        marketIdsList = new ArrayList<>(101);
        for (int i = 0; i < 101; i++) {
            marketIdsList.add(marketId);
        } // end for

        rescriptResponseHandler = new RescriptResponseHandler();
        ApiNgRescriptOperations.listMarketBook(marketIdsList, priceProjection, null, null, null, Statics.appKey.get(), rescriptResponseHandler);

        logger.info("EX_BEST_OFFERS depth=2 101 ids tooMuchData: {}", rescriptResponseHandler.isTooMuchData());
    }
}
