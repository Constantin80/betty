package info.fmro.betty.stream.definitions;

public enum ResponseOperationType {
    // The ConnectionMessage sent on your connection.
    connection,
    // The StatusMessage (returned in response to every RequestMessage)
    status,
    // The MarketChangeMessage that carries the initial image and updates to markets that you have subscribed to.
    mcm,
    // The OrderChangeMessage that carries the initial image and updates to orders that you have subscribed to.
    ocm,
}
