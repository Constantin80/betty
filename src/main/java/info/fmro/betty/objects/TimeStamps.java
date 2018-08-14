package info.fmro.betty.objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class TimeStamps
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(TimeStamps.class);
    private static final long serialVersionUID = 2521253086493558605L;
    private long lastObjectsSave, lastCleanScraperEventsMap, lastParseEventResultList, lastMapEventsToScraperEvents, lastGetMarketBooks, lastCleanSecondaryMaps, lastFindSafeRunners, lastStreamMarkets, lastGetAccountFunds, lastListCurrencyRates,
            lastFindInterestingMarkets, lastPrintDebug, lastPrintAverages, lastCleanTimedMaps, lastCheckAliases;

    public synchronized long getLastObjectsSave() {
        return lastObjectsSave;
    }

    public synchronized void setLastObjectsSave(long lastObjectsSave) {
        this.lastObjectsSave = lastObjectsSave;
    }

    public synchronized void lastObjectsSaveStamp() {
        this.lastObjectsSave = System.currentTimeMillis();
    }

    public synchronized void lastObjectsSaveStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastObjectsSave >= timeStamp) {
            this.lastObjectsSave = currentTime + timeStamp;
        } else {
            this.lastObjectsSave += timeStamp;
        }
    }

    public synchronized long getLastCleanScraperEventsMap() {
        return lastCleanScraperEventsMap;
    }

    public synchronized void setLastCleanScraperEventsMap(long lastCleanScraperEventsMap) {
        this.lastCleanScraperEventsMap = lastCleanScraperEventsMap;
    }

    public synchronized void lastCleanScraperEventsMapStamp() {
        this.lastCleanScraperEventsMap = System.currentTimeMillis();
    }

    public synchronized void lastCleanScraperEventsMapStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCleanScraperEventsMap >= timeStamp) {
            this.lastCleanScraperEventsMap = currentTime + timeStamp;
        } else {
            this.lastCleanScraperEventsMap += timeStamp;
        }
    }

    public synchronized long getLastParseEventResultList() {
        return lastParseEventResultList;
    }

    public synchronized void setLastParseEventResultList(long lastParseEventResultList) {
        this.lastParseEventResultList = lastParseEventResultList;
    }

    public synchronized void lastParseEventResultListStamp() {
        this.lastParseEventResultList = System.currentTimeMillis();
    }

    public synchronized void lastParseEventResultListStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastParseEventResultList >= timeStamp) {
            this.lastParseEventResultList = currentTime + timeStamp;
        } else {
            this.lastParseEventResultList += timeStamp;
        }
    }

    public synchronized long getLastMapEventsToScraperEvents() {
        return lastMapEventsToScraperEvents;
    }

    public synchronized void setLastMapEventsToScraperEvents(long lastMapEventsToScraperEvents) {
        this.lastMapEventsToScraperEvents = lastMapEventsToScraperEvents;
    }

    public synchronized void lastMapEventsToScraperEventsStamp() {
        this.lastMapEventsToScraperEvents = System.currentTimeMillis();
    }

    public synchronized void lastMapEventsToScraperEventsStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastMapEventsToScraperEvents >= timeStamp) {
            this.lastMapEventsToScraperEvents = currentTime + timeStamp;
        } else {
            this.lastMapEventsToScraperEvents += timeStamp;
        }
    }

    public synchronized long getLastGetMarketBooks() {
        return lastGetMarketBooks;
    }

    public synchronized void setLastGetMarketBooks(long lastGetMarketBooks) {
        this.lastGetMarketBooks = lastGetMarketBooks;
    }

    public synchronized void lastGetMarketBooksStamp() {
        this.lastGetMarketBooks = System.currentTimeMillis();
    }

    public synchronized void lastGetMarketBooksStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastGetMarketBooks >= timeStamp) {
            this.lastGetMarketBooks = currentTime + timeStamp;
        } else {
            this.lastGetMarketBooks += timeStamp;
        }
    }

    public synchronized long getLastCleanSecondaryMaps() {
        return lastCleanSecondaryMaps;
    }

    public synchronized void setLastCleanSecondaryMaps(long lastCleanSecondaryMaps) {
        this.lastCleanSecondaryMaps = lastCleanSecondaryMaps;
    }

    public synchronized void lastCleanSecondaryMapsStamp() {
        this.lastCleanSecondaryMaps = System.currentTimeMillis();
    }

    public synchronized void lastCleanSecondaryMapsStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCleanSecondaryMaps >= timeStamp) {
            this.lastCleanSecondaryMaps = currentTime + timeStamp;
        } else {
            this.lastCleanSecondaryMaps += timeStamp;
        }
    }

    public synchronized long getLastFindSafeRunners() {
        return lastFindSafeRunners;
    }

    public synchronized void setLastFindSafeRunners(long lastFindSafeRunners) {
        this.lastFindSafeRunners = lastFindSafeRunners;
    }

    public synchronized void lastFindSafeRunnersStamp() {
        this.lastFindSafeRunners = System.currentTimeMillis();
    }

    public synchronized void lastFindSafeRunnersStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastFindSafeRunners >= timeStamp) {
            this.lastFindSafeRunners = currentTime + timeStamp;
        } else {
            this.lastFindSafeRunners += timeStamp;
        }
    }

    public synchronized long getLastStreamMarkets() {
        return lastStreamMarkets;
    }

    public synchronized void setLastStreamMarkets(long lastStreamMarkets) {
        this.lastStreamMarkets = lastStreamMarkets;
    }

    public synchronized void lastStreamMarketsStamp() {
        this.lastStreamMarkets = System.currentTimeMillis();
    }

    public synchronized void lastStreamMarketsStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastStreamMarkets >= timeStamp) {
            this.lastStreamMarkets = currentTime + timeStamp;
        } else {
            this.lastStreamMarkets += timeStamp;
        }
    }

    public synchronized long getLastGetAccountFunds() {
        return lastGetAccountFunds;
    }

    public synchronized void setLastGetAccountFunds(long lastGetAccountFunds) {
        this.lastGetAccountFunds = lastGetAccountFunds;
    }

    public synchronized void lastGetAccountFundsStamp() {
        this.lastGetAccountFunds = System.currentTimeMillis();
    }

    public synchronized long getLastListCurrencyRates() {
        return lastListCurrencyRates;
    }

    public synchronized void setLastListCurrencyRates(long lastListCurrencyRates) {
        this.lastListCurrencyRates = lastListCurrencyRates;
    }

    public synchronized void lastListCurrencyRatesStamp() {
        this.lastListCurrencyRates = System.currentTimeMillis();
    }

    public synchronized long getLastFindInterestingMarkets() {
        return lastFindInterestingMarkets;
    }

    public synchronized void setLastFindInterestingMarkets(long lastFindInterestingMarkets) {
        this.lastFindInterestingMarkets = lastFindInterestingMarkets;
    }

    public synchronized void lastFindInterestingMarketsStamp() {
        this.lastFindInterestingMarkets = System.currentTimeMillis();
    }

    public synchronized void lastFindInterestingMarketsStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastFindInterestingMarkets >= timeStamp) {
            this.lastFindInterestingMarkets = currentTime + timeStamp;
        } else {
            this.lastFindInterestingMarkets += timeStamp;
        }
    }

    public synchronized long getLastPrintDebug() {
        return lastPrintDebug;
    }

    public synchronized void setLastPrintDebug(long lastPrintDebug) {
        this.lastPrintDebug = lastPrintDebug;
    }

    public synchronized void lastPrintDebugStamp() {
        this.lastPrintDebug = System.currentTimeMillis();
    }

    public synchronized void lastPrintDebugStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastPrintDebug >= timeStamp) {
            this.lastPrintDebug = currentTime + timeStamp;
        } else {
            this.lastPrintDebug += timeStamp;
        }
    }

    public synchronized long getLastPrintAverages() {
        return lastPrintAverages;
    }

    public synchronized void setLastPrintAverages(long lastPrintAverages) {
        this.lastPrintAverages = lastPrintAverages;
    }

    public synchronized void lastPrintAveragesStamp() {
        this.lastPrintAverages = System.currentTimeMillis();
    }

    public synchronized void lastPrintAveragesStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastPrintAverages >= timeStamp) {
            this.lastPrintAverages = currentTime + timeStamp;
        } else {
            this.lastPrintAverages += timeStamp;
        }
    }

    public synchronized long getLastCleanTimedMaps() {
        return lastCleanTimedMaps;
    }

    public synchronized void setLastCleanTimedMaps(long lastCleanTimedMaps) {
        this.lastCleanTimedMaps = lastCleanTimedMaps;
    }

    public synchronized void lastCleanTimedMapsStamp() {
        this.lastCleanTimedMaps = System.currentTimeMillis();
    }

    public synchronized void lastCleanTimedMapsStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCleanTimedMaps >= timeStamp) {
            this.lastCleanTimedMaps = currentTime + timeStamp;
        } else {
            this.lastCleanTimedMaps += timeStamp;
        }
    }

    public synchronized long getLastCheckAliases() {
        return lastCheckAliases;
    }

    public synchronized void setLastCheckAliases(long lastCheckAliases) {
        this.lastCheckAliases = lastCheckAliases;
    }

    public synchronized void lastCheckAliases() {
        this.lastCheckAliases = System.currentTimeMillis();
    }

    public synchronized void lastCheckAliasesStamp(long timeStamp) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCheckAliases >= timeStamp) {
            this.lastCheckAliases = currentTime + timeStamp;
        } else {
            this.lastCheckAliases += timeStamp;
        }
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized void copyFrom(TimeStamps timeStamps) {
        this.lastObjectsSave = timeStamps.lastObjectsSave;
        this.lastCleanScraperEventsMap = timeStamps.lastCleanScraperEventsMap;
        this.lastParseEventResultList = timeStamps.lastParseEventResultList;
        this.lastMapEventsToScraperEvents = timeStamps.lastMapEventsToScraperEvents;
        this.lastGetMarketBooks = timeStamps.lastGetMarketBooks;
        this.lastCleanSecondaryMaps = timeStamps.lastCleanSecondaryMaps;
        this.lastFindSafeRunners = timeStamps.lastFindSafeRunners;
        this.lastGetAccountFunds = timeStamps.lastGetAccountFunds;
        this.lastFindInterestingMarkets = timeStamps.lastFindInterestingMarkets;
        this.lastPrintDebug = timeStamps.lastPrintDebug;
        this.lastPrintAverages = timeStamps.lastPrintAverages;
        this.lastCleanTimedMaps = timeStamps.lastCleanTimedMaps;
        this.lastCheckAliases = timeStamps.lastCheckAliases;
    }
}
