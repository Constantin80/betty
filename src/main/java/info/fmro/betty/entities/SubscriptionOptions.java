package info.fmro.betty.entities;

public class SubscriptionOptions {
    private Integer subscription_length; // How many days should a created subscription last for. Open ended subscription created if value not provided. Relevant only if createdSubscription is true.
    private String subscription_token; // An existing subscription token that the caller wishes to be activated instead of creating a new one. Ignored is createSubscription is true.
    private String client_reference; // Any client reference for this subscription token request.

    public SubscriptionOptions() {
    }
}
