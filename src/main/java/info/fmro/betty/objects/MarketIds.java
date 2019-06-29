package info.fmro.betty.objects;

import com.google.common.collect.Lists;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MarketIds {

    private static final Logger logger = LoggerFactory.getLogger(MarketIds.class);
    private List<String> marketIdsList;
    private List<HashSet<String>> marketIdsSetsList;
    private int splitSize = 200;

    public MarketIds(final List<MarketCatalogue> marketCataloguesList) {
        init(marketCataloguesList);
    }

    public MarketIds(final List<MarketCatalogue> marketCataloguesList, final int splitSize) {
        this.splitSize = splitSize;
        init(marketCataloguesList);
    }

    private synchronized void init(final List<MarketCatalogue> marketCataloguesList) {
        if (marketCataloguesList != null) {
            this.marketIdsList = new ArrayList<>(marketCataloguesList.size());
            for (MarketCatalogue marketCatalogue : marketCataloguesList) {
                this.marketIdsList.add(marketCatalogue.getMarketId());
            }
        } else {
            logger.error("STRANGE marketCataloguesList null in MarketIds init, timeStamp={}", System.currentTimeMillis());
        }

        List<List<String>> marketIdsListsList = Lists.partition(marketIdsList, splitSize);
        this.marketIdsSetsList = new ArrayList<>(marketIdsListsList.size());
        for (List<String> marketIdsListLocal : marketIdsListsList) {
            this.marketIdsSetsList.add(new HashSet<>(marketIdsListLocal));
        }
    }

    public synchronized int getMarketIdsListSize() {
        return marketIdsList == null ? -1 : marketIdsList.size();
    }

    public synchronized List<HashSet<String>> getMarketIdsSetsList() {
        final List<HashSet<String>> result;

        if (this.marketIdsSetsList == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.marketIdsSetsList.size());
            for (final HashSet<String> set : this.marketIdsSetsList) {
                if (set == null) {
                    logger.error("null element found in marketIdsSetsList during getMarketIdsSetsList for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new HashSet<>(set));
                }
            }
        }

        return result;
    }
}
