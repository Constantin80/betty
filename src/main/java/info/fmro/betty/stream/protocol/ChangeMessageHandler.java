package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.MarketChange;
import info.fmro.betty.stream.definitions.OrderMarketChange;
import info.fmro.betty.stream.definitions.StatusMessage;

/**
 * This interface abstracts connection & cache implementation.
 * Created by mulveyj on 07/07/2016.
 */
public interface ChangeMessageHandler {
    void onOrderChange(ChangeMessage<OrderMarketChange> change);

    void onMarketChange(ChangeMessage<MarketChange> change);

    void onErrorStatusNotification(StatusMessage message);
}
