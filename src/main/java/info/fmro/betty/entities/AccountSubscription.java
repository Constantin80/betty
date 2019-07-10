package info.fmro.betty.entities;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class AccountSubscription {
    @Nullable
    private List<SubscriptionTokenInfo> subscriptionTokens; // List of subscription token details
    private String applicationName; // Application name
    private String applicationVersionId; // Application version Id

    @Nullable
    public synchronized List<SubscriptionTokenInfo> getSubscriptionTokens() {
        return this.subscriptionTokens == null ? null : new ArrayList<>(this.subscriptionTokens);
    }

    public synchronized void setSubscriptionTokens(final List<? extends SubscriptionTokenInfo> subscriptionTokens) {
        this.subscriptionTokens = subscriptionTokens == null ? null : new ArrayList<>(subscriptionTokens);
    }

    public synchronized String getApplicationName() {
        return this.applicationName;
    }

    public synchronized void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    public synchronized String getApplicationVersionId() {
        return this.applicationVersionId;
    }

    public synchronized void setApplicationVersionId(final String applicationVersionId) {
        this.applicationVersionId = applicationVersionId;
    }
}
