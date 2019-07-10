package info.fmro.betty.enums;

import org.jetbrains.annotations.Contract;

@SuppressWarnings("SpellCheckingInspection")
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

    @Contract(pure = true)
    ApiNgAccountOperation(final String operationName) {
        this.operationName = operationName;
    }

    @Contract(pure = true)
    public synchronized String getOperationName() {
        return this.operationName;
    }
}
