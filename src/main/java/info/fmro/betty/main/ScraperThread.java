package info.fmro.betty.main;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
import com.ximpleware.AutoPilot;
import com.ximpleware.EOFException;
import com.ximpleware.EntityException;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import info.fmro.betty.objects.AverageLogger;
import info.fmro.betty.objects.AverageLoggerInterface;
import info.fmro.betty.objects.RecordedMaxValue;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.betty.utility.WebScraperMethods;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ScraperThread
        implements Runnable, AverageLoggerInterface {

    private static final Logger logger = LoggerFactory.getLogger(ScraperThread.class);
    public static final BrowserVersion defaultBrowserVersion = BrowserVersion.FIREFOX_52;
    public static final boolean defaultSingleLogger = true;
    public static final long DEFAULT_DELAY = 500L;
    public static final int defaultCacheMaxSize = 200;
    public final BrowserVersion browserVersion;
    public final long DELAY_GETSCRAPEREVENTS;
    public final String threadId, cacheFileName, SAVE_FOLDER;
    public final AtomicLong lastTimedPageSave = new AtomicLong(); // only changed for the saves with timer, the others don't use it at all
    public final AtomicLong lastGetScraperEvents = new AtomicLong(), lastPageGet = new AtomicLong(), lastUpdatedScraperEvent = new AtomicLong();
    public final AtomicInteger timedScraperCounter = new AtomicInteger(), majorScrapingError = new AtomicInteger();
    public final AtomicBoolean mustSavePage = new AtomicBoolean(), mustRefreshPage = new AtomicBoolean();
    public final RecordedMaxValue listSizeMaxValue = new RecordedMaxValue(Generic.MINUTE_LENGTH_MILLISECONDS * 10L),
            liveMaxValue = new RecordedMaxValue(Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
    public final int cacheMaxSize;
    public final AverageLogger averageLogger, averageLoggerFull;
    public final boolean singleLogger;
    private int loggerCounter;

    public ScraperThread(String id) {
        this(id, DEFAULT_DELAY, defaultBrowserVersion, defaultCacheMaxSize, defaultSingleLogger);
    }

    public ScraperThread(String id, BrowserVersion browserVersion) {
        this(id, DEFAULT_DELAY, browserVersion, defaultCacheMaxSize, defaultSingleLogger);
    }

    public ScraperThread(String id, long delay) {
        this(id, delay, defaultBrowserVersion, defaultCacheMaxSize, defaultSingleLogger);
    }

    public ScraperThread(String id, long delay, BrowserVersion browserVersion) {
        this(id, delay, browserVersion, defaultCacheMaxSize, defaultSingleLogger);
    }

    public ScraperThread(String id, long delay, BrowserVersion browserVersion, int cacheMaxSize, boolean singleLogger) {
        this.threadId = id;
        this.cacheFileName = Statics.DATA_FOLDER_NAME + "/cache" + id + ".txt";
        this.SAVE_FOLDER = "pages." + id;
        this.DELAY_GETSCRAPEREVENTS = delay;
        this.browserVersion = browserVersion;
        this.cacheMaxSize = cacheMaxSize;
        this.singleLogger = singleLogger;
        this.averageLogger = new AverageLogger(this, this.threadId + " ran {}({}) times average/max: listSize {}/{} nScraped {}/{} took {}/{} ms of which {}/{} ms getting htmlPage", this.threadId + " ran {}({}) times", 4);
        if (this.singleLogger) {
            this.averageLoggerFull = null;
        } else {
            this.averageLoggerFull = new AverageLogger(this, this.threadId + " full ran {}({}) times average/max: listSize {}/{} nScraped {}/{} took {}/{} ms of which {}/{} ms getting htmlPage", this.threadId + " full ran {}({}) times", 4);
        }
    }

    @Override
    public int getExpectedRuns() {
        double factor;
        if (this.singleLogger) {
            factor = 1d;
        } else if (loggerCounter % 2 == 0) {
            factor = .9d;
        } else {
            factor = .1d;
        }
        loggerCounter++;
        return (int) (Statics.DELAY_PRINTAVERAGES / Statics.betradarScraperThread.DELAY_GETSCRAPEREVENTS * factor);
    }

    public void getScraperEventsInner(long startTime, boolean fullRun, boolean checkAll, AutoPilot autoPilot, VTDNav vtdNav, AtomicInteger listSize,
                                      AtomicInteger scrapedEventsCounter)
            throws XPathParseException, XPathEvalException, NavException { // should be overriden
    }

    public void getScraperEvents(HtmlPage htmlPage) {
        final int currentCounter = timedScraperCounter.getAndIncrement();
        boolean checkAll = currentCounter % 100 == 0;
        final boolean fullRun = !checkAll && currentCounter % 10 == 0;
        Formulas.lastGetScraperEventsStamp(lastGetScraperEvents, DELAY_GETSCRAPEREVENTS);

        final long startTime = System.currentTimeMillis();
        final String xmlString = htmlPage.asXml();
        final long afterAsXmlTime = System.currentTimeMillis();

        byte xmlByteArray[];
        try {
            xmlByteArray = xmlString.getBytes(Generic.UTF8_CHARSET);
        } catch (NullPointerException nullPointerException) { // catches xmlString == null case
            logger.error("STRANGE {} nullPointerException while getting xmlByteArray from: {}", threadId, xmlString, nullPointerException);
            xmlByteArray = null;
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            logger.error("STRANGE {} unsupportedEncodingException while getting xmlByteArray", threadId, unsupportedEncodingException);
            xmlByteArray = null;
        }

        AtomicInteger listSize = new AtomicInteger(-1);
        AtomicInteger scrapedEventsCounter = new AtomicInteger();
        if (xmlByteArray != null) {
            final VTDGen vtdGen = new VTDGen();
            vtdGen.setDoc(xmlByteArray);
            try {
                vtdGen.parse(true); // set namespace awareness to true
            } catch (EOFException eOFException) {
                logger.error("{} eOFException in vtdGen.parse from: {}", threadId, xmlString, eOFException);
            } catch (EntityException entityException) {
                logger.error("{} entityException in vtdGen.parse from: {}", threadId, xmlString, entityException);
            } catch (com.ximpleware.ParseException parseException) { // full path to ParseException is necessary as there exists another ParseException class
                logger.error("{} parseException in vtdGen.parse from: {}", threadId, xmlString, parseException);
            }

            final VTDNav vtdNav = vtdGen.getNav();
            final AutoPilot autoPilot = new AutoPilot(vtdNav);
//            autoPilot.selectElementNS("*", "*");

            try {
                getScraperEventsInner(startTime, fullRun, checkAll, autoPilot, vtdNav, listSize, scrapedEventsCounter);
            } catch (XPathParseException xPathParseException) {
                logger.error("{} xPathParseException in autoPilot", threadId, xPathParseException);
            } catch (XPathEvalException xPathEvalException) {
                logger.error("{} xPathEvalException in autoPilot", threadId, xPathEvalException);
            } catch (NavException navException) {
                logger.error("{} navException in autoPilot", threadId, navException);
            }
        } else {
            logger.error("STRANGE {} null xmlByteArray for: {}", threadId, xmlString);
        }

        final long currentTime = System.currentTimeMillis();
        final long totalRunTime = currentTime - startTime;
        final long timeGettingPage = afterAsXmlTime - startTime;
        if ((fullRun || checkAll) && !this.singleLogger) {
            this.averageLoggerFull.addRecords(listSize.get(), scrapedEventsCounter.get(), totalRunTime, timeGettingPage);
        } else {
            this.averageLogger.addRecords(listSize.get(), scrapedEventsCounter.get(), totalRunTime, timeGettingPage);
        }
        if (Statics.debugLevel.check(2, 188)) {
            final String fullRunString = fullRun ? " fullRun" : "";
            final String checkAllString = checkAll ? " checkAll" : "";
            logger.info("{} getScraperEvents{}{} listSize/scraped: {}/{} took: {} ms of which {} ms getting htmlPage", threadId, checkAllString, fullRunString, listSize.get(),
                        scrapedEventsCounter.get(), totalRunTime, timeGettingPage);
        }

        long neededExtraDelay;
        if (checkAll) {
            neededExtraDelay = 0L;
        } else if (fullRun) {
            neededExtraDelay = totalRunTime - DELAY_GETSCRAPEREVENTS;
        } else {
            neededExtraDelay = totalRunTime * 2 - DELAY_GETSCRAPEREVENTS;
        }
        if (neededExtraDelay > 0) {
            if (neededExtraDelay > 1_000L) {
                if (neededExtraDelay > 2_000L) {
                    logger.warn("{} reducing big neededExtraDelay from {}ms to 1000ms", threadId, neededExtraDelay); // also warn, as this happens often due to small clock jumps
                } else {
                    logger.warn("{} reducing neededExtraDelay from {}ms to 1000ms", threadId, neededExtraDelay);
                }
                neededExtraDelay = 1_000L; // maximum; it sometimes gets higher during clock jumps
            }

            Formulas.addAndGetLastGetScraperEvents(lastGetScraperEvents, neededExtraDelay); // avoids scraper using all processor
            final String fullRunString = fullRun ? "fullRun " : "";
            final String checkAllString = checkAll ? "checkAll " : "";
            logger.warn("{} extra scraper {}{}delay applied: {} ms", threadId, checkAllString, fullRunString, neededExtraDelay); // fairly large processor usage, needs at least warn
        }
    }

    public long timedGetScraperEvents(HtmlPage htmlPage) {
        long timeForNext = Formulas.getLastGetScraperEvents(lastGetScraperEvents);
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            getScraperEvents(htmlPage);
            timeForNext = Formulas.getLastGetScraperEvents(lastGetScraperEvents);

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    public HtmlPage getHtmlPage(WebClient webClient) { // has to be overriden
        return null;
    }

    public void pageManipulation(WebClient webClient, HtmlPage htmlPage)
            throws IOException { // should be overriden if used; should contain condional checks for running it
    }

    public String endSavePrefix() { // can be overriden to change the prefix
        return this.SAVE_FOLDER + "/end";
    }

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void run() {
        WebClient newWebClient = null;
        GetPageThread getPageThread = null;
        Thread threadSave = null;
        do {
            Thread threadGet = null;
            WebClient webClient = null;
            Cache cache = null;
            mustRefreshPage.set(false); // is just getting a new client/page

            try {
                if (newWebClient != null) {
                    webClient = newWebClient; // webClient has previously been closed in finally
                    cache = webClient.getCache();
                    newWebClient = null;
                } else {
                    webClient = WebScraperMethods.getNewWebClient(browserVersion, threadId);
                    cache = WebScraperMethods.initializeCache(webClient, cacheFileName, cacheMaxSize, threadId);
                }

                final long pageGetTime = System.currentTimeMillis();
                lastUpdatedScraperEvent.set(pageGetTime); // updated at every page get
                lastPageGet.set(pageGetTime);
                HtmlPage htmlPage;
                if (getPageThread != null && getPageThread.htmlPage != null) {
                    htmlPage = getPageThread.htmlPage;
                    getPageThread = null;
                } else {
                    if (getPageThread != null) {
                        getPageThread = null;
                        Generic.threadSleepSegmented(1_000L, 100L, Statics.mustStop); // avoid throttle
                    }
                    htmlPage = this.getHtmlPage(webClient);
                }

                if (htmlPage != null) {
                    final WebWindow webWindow = htmlPage.getEnclosingWindow();
                    final JavaScriptJobManager javaScriptJobManager = webWindow.getJobManager();

                    while (!Statics.mustStop.get()) {
                        try {
                            pageManipulation(webClient, htmlPage);

                            if (Statics.mustSleep.get()) {
                                Betty.programSleeps(Statics.mustSleep, Statics.mustStop, threadId + " scraper");
                            }
                            if (Statics.debugLevel.check(3, 187)) {
                                logger.info("{} javaScriptJobCount: {} cache size: {}", threadId, javaScriptJobManager.getJobCount(), cache.getSize());
                            }

                            if ((mustRefreshPage.get() || javaScriptJobManager.getJobCount() <= 0) && threadGet == null) {
                                // close window and reload page is the only reliable way for refresh
                                if (threadSave != null && threadSave.isAlive()) {
                                    Generic.alreadyPrintedMap.logOnce(Statics.debugLevel.check(3, 196), Generic.MINUTE_LENGTH_MILLISECONDS * 5L, logger, LogLevel.WARN,
                                                                      "{} threadSave still alive: won't refresh yet", threadId);
                                } else {
                                    logger.info("{} mustRefreshPage encountered", threadId);

                                    newWebClient = WebScraperMethods.getNewWebClient(browserVersion, threadId);
                                    WebScraperMethods.initializeCache(newWebClient, cache, cacheMaxSize, threadId);
                                    getPageThread = new GetPageThread(this, newWebClient);

                                    threadGet = new Thread(getPageThread);
                                    threadGet.start();
                                }
                            }
                            if (getPageThread != null && getPageThread.hasRun) {
                                long timeSincePreviousPageGet = System.currentTimeMillis() - lastPageGet.get();
                                if (timeSincePreviousPageGet >= 20_000L) {
                                    break; // this will break out of the inner loop, which will cause window close, clear cache, and the page loaded again in the outer loop
                                } else { // wait a bit, avoid throttle; no need to print message, as that would flood the log
                                }
                            }
                            if (mustSavePage.get()) {
                                logger.info("{} mustSavePage encountered", threadId);
                                WebScraperMethods.savePage(htmlPage, mustSavePage, SAVE_FOLDER + "/save", threadId);
                            }

                            long timeToSleep;

                            timeToSleep = timedGetScraperEvents(htmlPage);

                            Generic.threadSleepSegmented(timeToSleep, 100L, Statics.mustStop, mustSavePage, mustRefreshPage);
//                        } catch (RejectedExecutionException rejectedExecutionException) {
//                            logger.error("exception in {} scraper inner loop", threadId, rejectedExecutionException);
//                            if (!Statics.mustStop.get()) {
//                                Generic.threadSleep(100L); // avoid throttle
//                            }
                        } catch (RuntimeException | IOException exception) {
                            logger.error("exception in {} scraper inner loop", threadId, exception);
                            if (!Statics.mustStop.get()) {
                                Generic.threadSleep(100L); // avoid throttle
                            }
                        } catch (Throwable throwable) { // inner safety net
                            logger.error("exception in {} scraper safety net inner loop", threadId, throwable);
                            if (!Statics.mustStop.get()) {
                                Generic.threadSleep(100L); // avoid throttle
                            }
                        }
                    } // end inner while
                    logger.info("{} scraper page finished executing: {} {}", threadId, htmlPage.toString(), htmlPage.getTitleText());

                    if (threadSave != null && threadSave.isAlive()) {
                        if (Statics.mustStop.get()) {
                            logger.info("parallel save thread still alive in {} scraperThread", threadId);
                        } else {
                            logger.error("parallel save thread still alive in {} scraperThread", threadId);
                        }

                        try {
                            threadSave.join();
                        } catch (InterruptedException interruptedException) {
                            logger.error("STRANGE interruptedException in threadSave join in {} scraperThread", threadId, interruptedException);
                        }
                        if (Statics.mustStop.get()) {
                            logger.info("parallel save thread finally dead in {} scraperThread", threadId);
                        } else {
                            logger.error("parallel save thread finally dead in {} scraperThread", threadId);
                        }
                    }
                    SavePageThread savePageThread = new SavePageThread(this, endSavePrefix(), webClient, htmlPage, cache);
                    threadSave = new Thread(savePageThread);
                    threadSave.start();
                    webClient = null; // avoids closeAllWindows from finally
                } else {
                    Generic.threadSleepSegmented(1_000L, 100L, Statics.mustStop); // avoid throttle
                }
            } catch (Throwable throwable) { // safety net
                logger.error("exception in {} scraper safety net", threadId, throwable);

                try {
                    if (cache != null) {
                        WebScraperMethods.clearCache(cache, cacheFileName, threadId);
                    }
                } catch (Throwable innerThrowable) { // safety net inside safety net
                    logger.error("exception in {} scraper safety net's safety net", threadId, innerThrowable);
                }
            } finally {
                if (webClient != null) {
                    webClient.close();
                }
                if (threadGet != null && threadGet.isAlive()) {
                    if (!Statics.mustStop.get()) {
                        logger.error("parallel get thread still alive in {} scraperThread", threadId);
                    }
                    try {
                        threadGet.join();
                    } catch (InterruptedException interruptedException) {
                        logger.error("STRANGE interruptedException in threadGet join in {} scraperThread", threadId, interruptedException);
                    }
                    if (!Statics.mustStop.get()) {
                        logger.error("parallel get thread finally dead in {} scraperThread", threadId);
                    }
                } // end if threadGet
            } // end finally
        } while (!Statics.mustStop.get());
        if (threadSave != null && threadSave.isAlive()) {
            logger.info("parallel save thread still alive in {} scraperThread end", threadId);
            try {
                threadSave.join();
            } catch (InterruptedException interruptedException) {
                logger.error("STRANGE interruptedException in threadSave join in {} scraperThread end", threadId, interruptedException);
            }
            logger.info("parallel save thread finally dead in {} scraperThread end", threadId);
        } // end if threadSave

        if (newWebClient != null) {
            newWebClient.close();
        }

        logger.info("{} scraper thread ends", threadId);
    }
}
