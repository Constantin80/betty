package info.fmro.betty.enums;

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

    private ApiNgOperation(final String operationName) {
        this.operationName = operationName;
    }

    public synchronized String getOperationName() {
        return operationName;
    }
}
