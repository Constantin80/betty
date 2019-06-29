package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.MarketChange;
import info.fmro.betty.stream.definitions.OrderMarketChange;
import info.fmro.betty.stream.definitions.StatusMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullChangeHandler
        implements ChangeMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(NullChangeHandler.class);

    @Override
    public synchronized void onOrderChange(final ChangeMessage<OrderMarketChange> change) {
        logger.info("onOrderChange: " + change);
    }

    @Override
    public synchronized void onMarketChange(final ChangeMessage<MarketChange> change) {
        logger.info("onMarketChange: " + change);
    }

    @Override
    public synchronized void onErrorStatusNotification(final StatusMessage message) {
        logger.info("onErrorStatusNotification: " + message);
    }
}
