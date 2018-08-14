package info.fmro.betty.enums;

public enum ApiNgAccountOperation {
    CREATEDEVELOPERAPPKEYS("createDeveloperAppKeys"),
    GETDEVELOPERAPPKEYS("getDeveloperAppKeys"),
    GETACCOUNTFUNDS("getAccountFunds"),
    GETACCOUNTDETAILS("getAccountDetails"),
    GETVENDORCLIENTID("getVendorClientId"),
    GETAPPLICATIONSUBSCRIPTIONTOKEN("getApplicationSubscriptionToken"),
    ACTIVATEAPPLICATIONSUBSCRIPTION("activateApplicationSubscription"),
    CANCELAPPLICATIONSUBSCRIPTION("cancelApplicationSubscription"),
    LISTAPPLICATIONSUBSCRIPTIONTOKENS("listApplicationSubscriptionTokens"),
    LISTACCOUNTSUBSCRIPTIONTOKENS("listAccountSubscriptionTokens"),
    GETAPPLICATIONSUBSCRIPTIONHISTORY("getApplicationSubscriptionHistory"),
    GETACCOUNTSTATEMENT("getAccountStatement"),
    LISTCURRENCYRATES("listCurrencyRates");

    private final String operationName;

    private ApiNgAccountOperation(String operationName) {
        this.operationName = operationName;
    }

    public synchronized String getOperationName() {
        return operationName;
    }
}
