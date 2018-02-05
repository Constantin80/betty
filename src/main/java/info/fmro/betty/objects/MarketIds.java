package info.fmro.betty.objects;

import com.google.common.collect.Lists;
import info.fmro.betty.entities.MarketCatalogue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarketIds {

    private static final Logger logger = LoggerFactory.getLogger(MarketIds.class);
    private List<String> marketIdsList;
    private List<HashSet<String>> marketIdsSetsList;
    private int splitSize = 200;

    public MarketIds(List<MarketCatalogue> marketCataloguesList) {
        init(marketCataloguesList);
    }

    public MarketIds(List<MarketCatalogue> marketCataloguesList, int splitSize) {
        this.splitSize = splitSize;
        init(marketCataloguesList);
    }

    private synchronized void init(List<MarketCatalogue> marketCataloguesList) {
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
        return this.marketIdsSetsList == null ? null : new ArrayList<>(this.marketIdsSetsList);
    }
}
