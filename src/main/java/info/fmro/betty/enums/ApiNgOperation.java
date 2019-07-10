package info.fmro.betty.enums;

import org.jetbrains.annotations.Contract;

@SuppressWarnings("SpellCheckingInspection")
public enum ApiNgOperation {
    LISTEVENTTYPES("listEventTypes"),
    LISTCOMPETITIONS("listCompetitions"),
    LISTTIMERANGES("listTimeRanges"),
    LISTEVENTS("listEvents"),
    LISTMARKETTYPES("listMarketTypes"),
    LISTCOUNTRIES("listCountries"),
    LISTVENUES("listVenues"),
    LISTMARKETCATALOGUE("listMarketCatalogue"),
    LISTMARKETBOOK("listMarketBook"),
    LISTMARKETPROFITANDLOSS("listMarketProfitAndLoss"),
    LISTCURRENTORDERS("listCurrentOrders"),
    LISTCLEAREDORDERS("listClearedOrders"),
    PLACEORDERS("placeOrders"),
    CANCELORDERS("cancelOrders"),
    REPLACEORDERS("replaceOrders"),
    UPDATEORDERS("updateOrders");

    private final String operationName;

    @Contract(pure = true)
    ApiNgOperation(final String operationName) {
        this.operationName = operationName;
    }

    @Contract(pure = true)
    public synchronized String getOperationName() {
        return this.operationName;
    }
}
