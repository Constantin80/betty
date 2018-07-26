package info.fmro.betty.stream.cache.market;

import info.fmro.betty.stream.cache.util.Utils;
import info.fmro.betty.stream.definitions.MarketChange;
import info.fmro.betty.stream.protocol.ChangeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread safe cache of markets
 */
public class MarketCache {
    private static final Logger logger = LoggerFactory.getLogger(MarketCache.class);
    private final Map<String, Market> markets = new ConcurrentHashMap<>();

    //whether markets are automatically removed on close (default is True)
    private boolean isMarketRemovedOnClose = true; // default
    private boolean traceMarkets = false;

    //conflation indicates slow consumption
    private int conflatedCount;

    public MarketCache() {
    }

    public synchronized void traceMarkets() {
        traceMarkets = true;
    }

    public synchronized void onMarketChange(ChangeMessage<MarketChange> changeMessage) {
        if (changeMessage.isStartOfNewSubscription()) {
            //clear cache ... no clear anymore, because of multiple clients
//            markets.clear();
        }
        if (changeMessage.getItems() != null) {
            for (MarketChange marketChange : changeMessage.getItems()) {
                final Market market = onMarketChange(marketChange);

                if (isMarketRemovedOnClose && market.isClosed()) {
                    //remove on close
                    markets.remove(market.getMarketId());
                }

                dispatchMarketChanged(market, marketChange);
            } // end for
        }
    }

    private synchronized Market onMarketChange(MarketChange marketChange) {
        if (Boolean.TRUE.equals(marketChange.getCon())) {
            conflatedCount++;
        }
        final Market market = markets.computeIfAbsent(marketChange.getId(), k -> new Market(k));
        market.onMarketChange(marketChange);
        return market;
    }

    public synchronized int getConflatedCount() {
        return conflatedCount;
    }

    public synchronized void setConflatedCount(int conflatedCount) {
        this.conflatedCount = conflatedCount;
    }

    public synchronized boolean isMarketRemovedOnClose() {
        return isMarketRemovedOnClose;
    }

    public synchronized void setMarketRemovedOnClose(boolean marketRemovedOnClose) {
        isMarketRemovedOnClose = marketRemovedOnClose;
    }

    public synchronized Market getMarket(String marketId) {
        //queries by market id - the result is invariant for the lifetime of the market.
        return markets.get(marketId);
    }

    public synchronized Iterable<Market> getMarkets() {
        //all the cached markets
        return markets.values();
    }

    public synchronized int getCount() {
        //market count
        return markets.size();
    }

    // Event for each market change

    private synchronized void dispatchMarketChanged(Market market, MarketChange marketChange) {
        final MarketChangeEvent marketChangeEvent = new MarketChangeEvent(this);
        marketChangeEvent.setMarket(market);
        marketChangeEvent.setChange(marketChange);

        try {
            if (traceMarkets) {
                Utils.printMarket(marketChangeEvent.getSnap());
            }
        } catch (Exception e) {
            logger.error("Exception from event listener", e);
        }
    }
}
