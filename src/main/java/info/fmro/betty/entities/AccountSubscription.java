package info.fmro.betty.entities;

import java.util.ArrayList;
import java.util.List;

public class AccountSubscription {
    private List<SubscriptionTokenInfo> subscriptionTokens; // List of subscription token details
    private String applicationName; // Application name
    private String applicationVersionId; // Application version Id

    public AccountSubscription() {
    }

    public synchronized List<SubscriptionTokenInfo> getSubscriptionTokens() {
        return subscriptionTokens == null ? null : new ArrayList<>(subscriptionTokens);
    }

    public synchronized void setSubscriptionTokens(final List<SubscriptionTokenInfo> subscriptionTokens) {
        this.subscriptionTokens = subscriptionTokens == null ? null : new ArrayList<>(subscriptionTokens);
    }

    public synchronized String getApplicationName() {
        return applicationName;
    }

    public synchronized void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    public synchronized String getApplicationVersionId() {
        return applicationVersionId;
    }

    public synchronized void setApplicationVersionId(final String applicationVersionId) {
        this.applicationVersionId = applicationVersionId;
    }
}
