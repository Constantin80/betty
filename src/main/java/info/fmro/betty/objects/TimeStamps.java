package info.fmro.betty.objects;

import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

@SuppressWarnings({"ClassWithTooManyFields", "ClassWithTooManyMethods"})
public class TimeStamps
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(TimeStamps.class);
    private static final long serialVersionUID = 2521253086493558605L;
    private long lastObjectsSave, lastSettingsSave, lastCleanScraperEventsMap, lastParseEventResultList, lastMapEventsToScraperEvents, lastGetMarketBooks, lastCleanSecondaryMaps, lastFindSafeRunners, lastStreamMarkets, lastGetAccountFunds,
            lastListCurrencyRates, lastFindInterestingMarkets, lastPrintDebug, lastPrintAverages, lastCleanTimedMaps, lastCheckAliases;

    public synchronized long getLastObjectsSave() {
        return this.lastObjectsSave;
    }

    public synchronized void setLastObjectsSave(final long lastObjectsSave) {
        this.lastObjectsSave = lastObjectsSave;
    }

    public synchronized void lastObjectsSaveStamp() {
        this.lastObjectsSave = System.currentTimeMillis();
    }

    public synchronized void lastObjectsSaveStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastObjectsSave >= timeStamp) {
            this.lastObjectsSave = currentTime + timeStamp;
        } else {
            this.lastObjectsSave += timeStamp;
        }
    }

    public synchronized long getLastSettingsSave() {
        return this.lastSettingsSave;
    }

    public synchronized void setLastSettingsSave(final long lastSettingsSave) {
        this.lastSettingsSave = lastSettingsSave;
    }

    public synchronized void lastSettingsSaveStamp() {
        this.lastSettingsSave = System.currentTimeMillis();
    }

    public synchronized void lastSettingsSaveStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastSettingsSave >= timeStamp) {
            this.lastSettingsSave = currentTime + timeStamp;
        } else {
            this.lastSettingsSave += timeStamp;
        }
    }

    public synchronized long getLastCleanScraperEventsMap() {
        return this.lastCleanScraperEventsMap;
    }

    public synchronized void setLastCleanScraperEventsMap(final long lastCleanScraperEventsMap) {
        this.lastCleanScraperEventsMap = lastCleanScraperEventsMap;
    }

    public synchronized void lastCleanScraperEventsMapStamp() {
        this.lastCleanScraperEventsMap = System.currentTimeMillis();
    }

    public synchronized void lastCleanScraperEventsMapStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCleanScraperEventsMap >= timeStamp) {
            this.lastCleanScraperEventsMap = currentTime + timeStamp;
        } else {
            this.lastCleanScraperEventsMap += timeStamp;
        }
    }

    public synchronized long getLastParseEventResultList() {
        return this.lastParseEventResultList;
    }

    public synchronized void setLastParseEventResultList(final long lastParseEventResultList) {
        this.lastParseEventResultList = lastParseEventResultList;
    }

    public synchronized void lastParseEventResultListStamp() {
        this.lastParseEventResultList = System.currentTimeMillis();
    }

    public synchronized void lastParseEventResultListStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastParseEventResultList >= timeStamp) {
            this.lastParseEventResultList = currentTime + timeStamp;
        } else {
            this.lastParseEventResultList += timeStamp;
        }
    }

    public synchronized long getLastMapEventsToScraperEvents() {
        return this.lastMapEventsToScraperEvents;
    }

    public synchronized void setLastMapEventsToScraperEvents(final long lastMapEventsToScraperEvents) {
        this.lastMapEventsToScraperEvents = lastMapEventsToScraperEvents;
    }

    public synchronized void lastMapEventsToScraperEventsStamp() {
        this.lastMapEventsToScraperEvents = System.currentTimeMillis();
    }

    public synchronized void lastMapEventsToScraperEventsStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastMapEventsToScraperEvents >= timeStamp) {
            this.lastMapEventsToScraperEvents = currentTime + timeStamp;
        } else {
            this.lastMapEventsToScraperEvents += timeStamp;
        }
    }

    public synchronized long getLastGetMarketBooks() {
        return this.lastGetMarketBooks;
    }

    public synchronized void setLastGetMarketBooks(final long lastGetMarketBooks) {
        this.lastGetMarketBooks = lastGetMarketBooks;
    }

    public synchronized void lastGetMarketBooksStamp() {
        this.lastGetMarketBooks = System.currentTimeMillis();
    }

    public synchronized void lastGetMarketBooksStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastGetMarketBooks >= timeStamp) {
            this.lastGetMarketBooks = currentTime + timeStamp;
        } else {
            this.lastGetMarketBooks += timeStamp;
        }
    }

    public synchronized long getLastCleanSecondaryMaps() {
        return this.lastCleanSecondaryMaps;
    }

    public synchronized void setLastCleanSecondaryMaps(final long lastCleanSecondaryMaps) {
        this.lastCleanSecondaryMaps = lastCleanSecondaryMaps;
    }

    public synchronized void lastCleanSecondaryMapsStamp() {
        this.lastCleanSecondaryMaps = System.currentTimeMillis();
    }

    public synchronized void lastCleanSecondaryMapsStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCleanSecondaryMaps >= timeStamp) {
            this.lastCleanSecondaryMaps = currentTime + timeStamp;
        } else {
            this.lastCleanSecondaryMaps += timeStamp;
        }
    }

    public synchronized long getLastFindSafeRunners() {
        return this.lastFindSafeRunners;
    }

    public synchronized void setLastFindSafeRunners(final long lastFindSafeRunners) {
        this.lastFindSafeRunners = lastFindSafeRunners;
    }

    public synchronized void lastFindSafeRunnersStamp() {
        this.lastFindSafeRunners = System.currentTimeMillis();
    }

    public synchronized void lastFindSafeRunnersStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastFindSafeRunners >= timeStamp) {
            this.lastFindSafeRunners = currentTime + timeStamp;
        } else {
            this.lastFindSafeRunners += timeStamp;
        }
    }

    public synchronized long getLastStreamMarkets() {
        return this.lastStreamMarkets;
    }

    public synchronized void setLastStreamMarkets(final long lastStreamMarkets) {
        this.lastStreamMarkets = lastStreamMarkets;
    }

    public synchronized void lastStreamMarketsStamp() {
        this.lastStreamMarkets = System.currentTimeMillis();
    }

    public synchronized void lastStreamMarketsStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastStreamMarkets >= timeStamp) {
            this.lastStreamMarkets = currentTime + timeStamp;
        } else {
            this.lastStreamMarkets += timeStamp;
        }
    }

    public synchronized long getLastGetAccountFunds() {
        return this.lastGetAccountFunds;
    }

    public synchronized void setLastGetAccountFunds(final long lastGetAccountFunds) {
        this.lastGetAccountFunds = lastGetAccountFunds;
    }

    public synchronized void lastGetAccountFundsStamp() {
        this.lastGetAccountFunds = System.currentTimeMillis();
    }

    public synchronized long getLastListCurrencyRates() {
        return this.lastListCurrencyRates;
    }

    synchronized void setLastListCurrencyRates(@SuppressWarnings("SameParameterValue") final long lastListCurrencyRates) {
        this.lastListCurrencyRates = lastListCurrencyRates;
    }

    public synchronized void lastListCurrencyRatesStamp() {
        this.lastListCurrencyRates = System.currentTimeMillis();
    }

    public synchronized long getLastFindInterestingMarkets() {
        return this.lastFindInterestingMarkets;
    }

    public synchronized void setLastFindInterestingMarkets(final long lastFindInterestingMarkets) {
        this.lastFindInterestingMarkets = lastFindInterestingMarkets;
    }

    public synchronized void lastFindInterestingMarketsStamp() {
        this.lastFindInterestingMarkets = System.currentTimeMillis();
    }

    public synchronized void lastFindInterestingMarketsStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastFindInterestingMarkets >= timeStamp) {
            this.lastFindInterestingMarkets = currentTime + timeStamp;
        } else {
            this.lastFindInterestingMarkets += timeStamp;
        }
    }

    public synchronized long getLastPrintDebug() {
        return this.lastPrintDebug;
    }

    public synchronized void setLastPrintDebug(final long lastPrintDebug) {
        this.lastPrintDebug = lastPrintDebug;
    }

    public synchronized void lastPrintDebugStamp() {
        this.lastPrintDebug = System.currentTimeMillis();
    }

    public synchronized void lastPrintDebugStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastPrintDebug >= timeStamp) {
            this.lastPrintDebug = currentTime + timeStamp;
        } else {
            this.lastPrintDebug += timeStamp;
        }
    }

    public synchronized long getLastPrintAverages() {
        return this.lastPrintAverages;
    }

    public synchronized void setLastPrintAverages(final long lastPrintAverages) {
        this.lastPrintAverages = lastPrintAverages;
    }

    public synchronized void lastPrintAveragesStamp() {
        this.lastPrintAverages = System.currentTimeMillis();
    }

    public synchronized void lastPrintAveragesStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastPrintAverages >= timeStamp) {
            this.lastPrintAverages = currentTime + timeStamp;
        } else {
            this.lastPrintAverages += timeStamp;
        }
    }

    public synchronized long getLastCleanTimedMaps() {
        return this.lastCleanTimedMaps;
    }

    public synchronized void setLastCleanTimedMaps(final long lastCleanTimedMaps) {
        this.lastCleanTimedMaps = lastCleanTimedMaps;
    }

    public synchronized void lastCleanTimedMapsStamp() {
        this.lastCleanTimedMaps = System.currentTimeMillis();
    }

    public synchronized void lastCleanTimedMapsStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCleanTimedMaps >= timeStamp) {
            this.lastCleanTimedMaps = currentTime + timeStamp;
        } else {
            this.lastCleanTimedMaps += timeStamp;
        }
    }

    public synchronized long getLastCheckAliases() {
        return this.lastCheckAliases;
    }

    public synchronized void setLastCheckAliases(final long lastCheckAliases) {
        this.lastCheckAliases = lastCheckAliases;
    }

    public synchronized void lastCheckAliases() {
        this.lastCheckAliases = System.currentTimeMillis();
    }

    public synchronized void lastCheckAliasesStamp(final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastCheckAliases >= timeStamp) {
            this.lastCheckAliases = currentTime + timeStamp;
        } else {
            this.lastCheckAliases += timeStamp;
        }
    }

    public synchronized void copyFrom(final TimeStamps timeStamps) {
        if (timeStamps == null) {
            logger.error("null timeStamps in copyFrom for: {}", Generic.objectToString(this));
        } else {
            this.lastObjectsSave = timeStamps.lastObjectsSave;
            this.lastSettingsSave = timeStamps.lastSettingsSave;
            this.lastCleanScraperEventsMap = timeStamps.lastCleanScraperEventsMap;
            this.lastParseEventResultList = timeStamps.lastParseEventResultList;
            this.lastMapEventsToScraperEvents = timeStamps.lastMapEventsToScraperEvents;
            this.lastGetMarketBooks = timeStamps.lastGetMarketBooks;
            this.lastCleanSecondaryMaps = timeStamps.lastCleanSecondaryMaps;
            this.lastFindSafeRunners = timeStamps.lastFindSafeRunners;
            this.lastStreamMarkets = timeStamps.lastStreamMarkets;
            this.lastGetAccountFunds = timeStamps.lastGetAccountFunds;
            this.lastListCurrencyRates = timeStamps.lastListCurrencyRates;
            this.lastFindInterestingMarkets = timeStamps.lastFindInterestingMarkets;
            this.lastPrintDebug = timeStamps.lastPrintDebug;
            this.lastPrintAverages = timeStamps.lastPrintAverages;
            this.lastCleanTimedMaps = timeStamps.lastCleanTimedMaps;
            this.lastCheckAliases = timeStamps.lastCheckAliases;
        }
    }
}
