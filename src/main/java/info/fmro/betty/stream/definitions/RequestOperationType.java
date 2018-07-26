package info.fmro.betty.stream.definitions;

public enum RequestOperationType {
    // The AuthenticationMessage - authenticates your connection.
    authentication,
    // The MarketSubscriptionMessage - subscribes to market changes.
    marketSubscription,
    // The OrderSubscriptionMessage - subscribes to order changes.
    orderSubscription,
    // The HeartbeatMessage - use if you need to keep a firewall open or want to test connectivity.
    heartbeat
}
