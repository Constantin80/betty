package info.fmro.betty.safebet;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebWindow;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.background.JavaScriptJobManager;
import com.ximpleware.AutoPilot;
import com.ximpleware.EOFException;
import com.ximpleware.EncodingException;
import com.ximpleware.EntityException;
import com.ximpleware.NavException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import info.fmro.betty.main.Betty;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.SavePageThread;
import info.fmro.betty.utility.Formulas;
import info.fmro.betty.utility.WebScraperMethods;
import info.fmro.shared.objects.AverageLogger;
import info.fmro.shared.objects.AverageLoggerInterface;
import info.fmro.shared.objects.RecordedMaxValue;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("PackageVisibleField")
public class ScraperPermanentThread
        implements Runnable, AverageLoggerInterface {
    private static final Logger logger = LoggerFactory.getLogger(ScraperPermanentThread.class);
    public static final BrowserVersion defaultBrowserVersion = BrowserVersion.FIREFOX_68;
    public static final boolean defaultSingleLogger = true;
    public static final long DEFAULT_DELAY = 500L;
    public static final int defaultCacheMaxSize = 200;
    private final BrowserVersion browserVersion;
    public final long delayGetScraperEvents;
    public final String threadId, cacheFileName, saveFolder;
    final AtomicLong lastTimedPageSave = new AtomicLong(); // only changed for the saves with timer, the others don't use it at all
    public final AtomicLong lastGetScraperEvents = new AtomicLong();
    final AtomicLong lastPageGet = new AtomicLong(), lastUpdatedScraperEvent = new AtomicLong();
    final AtomicInteger timedScraperCounter = new AtomicInteger(), majorScrapingError = new AtomicInteger();
    public final AtomicBoolean mustSavePage = new AtomicBoolean(), mustRefreshPage = new AtomicBoolean();
    final RecordedMaxValue listSizeMaxValue = new RecordedMaxValue(Generic.MINUTE_LENGTH_MILLISECONDS * 10L), liveMaxValue = new RecordedMaxValue(Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
    private final int cacheMaxSize;
    @Nullable
    public final AverageLogger averageLogger, averageLoggerFull;
    private final boolean singleLogger;
    private int loggerCounter;

//    public ScraperPermanentThread(final String id) {
//        this(id, DEFAULT_DELAY, defaultBrowserVersion, defaultCacheMaxSize, defaultSingleLogger);
//    }
//
//    public ScraperPermanentThread(final String id, final BrowserVersion browserVersion) {
//        this(id, DEFAULT_DELAY, browserVersion, defaultCacheMaxSize, defaultSingleLogger);
//    }
//
//    public ScraperPermanentThread(final String id, final long delay) {
//        this(id, delay, defaultBrowserVersion, defaultCacheMaxSize, defaultSingleLogger);
//    }
//
//    public ScraperPermanentThread(final String id, final long delay, final BrowserVersion browserVersion) {
//        this(id, delay, browserVersion, defaultCacheMaxSize, defaultSingleLogger);
//    }

    ScraperPermanentThread(final String id, final long delay, final BrowserVersion browserVersion, final int cacheMaxSize, final boolean singleLogger) {
        this.threadId = id;
        this.cacheFileName = Statics.DATA_FOLDER_NAME + "/cache" + id + ".txt";
        this.saveFolder = "pages." + id;
        this.delayGetScraperEvents = delay;
        this.browserVersion = browserVersion;
        this.cacheMaxSize = cacheMaxSize;
        this.singleLogger = singleLogger;
        //noinspection ThisEscapedInObjectConstruction
        this.averageLogger = new AverageLogger(this, this.threadId + " ran {}({}) times average/max: listSize {}/{} nScraped {}/{} took {}/{} ms of which {}/{} ms getting htmlPage",
                                               this.threadId + " ran {}({}) times", 4);
        //noinspection ThisEscapedInObjectConstruction
        this.averageLoggerFull =
                this.singleLogger ? null : new AverageLogger(this, this.threadId + " full ran {}({}) times average/max: listSize {}/{} nScraped {}/{} took {}/{} ms of which {}/{} ms getting htmlPage",
                                                             this.threadId + " full ran {}({}) times", 4);
    }

    @Override
    public int getExpectedRuns() {
        final double factor;
        if (this.singleLogger) {
            factor = 1d;
        } else if (this.loggerCounter % 2 == 0) {
            factor = .9d;
        } else {
            factor = .1d;
        }
        this.loggerCounter++;
        //noinspection NumericCastThatLosesPrecision
        return (int) (Statics.DELAY_PRINT_AVERAGES / Statics.betradarScraperThread.delayGetScraperEvents * factor);
    }

    void getScraperEventsInner(final long startTime, final boolean fullRun, final boolean checkAll, final AutoPilot autoPilot, final VTDNav vtdNav, final AtomicInteger listSize, final AtomicInteger scrapedEventsCounter)
            throws XPathParseException, XPathEvalException, NavException { // should be overriden
    }

    private void getScraperEvents(@NotNull final HtmlPage htmlPage) {
        final int currentCounter = this.timedScraperCounter.getAndIncrement();
        final boolean checkAll = currentCounter % 100 == 0;
        final boolean fullRun = !checkAll && currentCounter % 10 == 0;
        Formulas.lastGetScraperEventsStamp(this.lastGetScraperEvents, this.delayGetScraperEvents);

        final long startTime = System.currentTimeMillis();
        final String xmlString = htmlPage.asXml();
        final long afterAsXmlTime = System.currentTimeMillis();

        @Nullable final byte xmlByteArray[];
        if (xmlString == null) {
            logger.error("STRANGE {} xmlString while getting xmlByteArray from: {}", this.threadId, xmlString);
            xmlByteArray = null;
        } else {
            xmlByteArray = xmlString.getBytes(StandardCharsets.UTF_8);
        }

//        try {
//            xmlByteArray = xmlString.getBytes(StandardCharsets.UTF_8);
//        } catch (NullPointerException nullPointerException) { // catches xmlString == null case
//            logger.error("STRANGE {} nullPointerException while getting xmlByteArray from: {}", this.threadId, xmlString, nullPointerException);
//            xmlByteArray = null;
//        }

        final AtomicInteger listSize = new AtomicInteger(-1);
        final AtomicInteger scrapedEventsCounter = new AtomicInteger();
        if (xmlByteArray != null) {
            final VTDGen vtdGen = new VTDGen();
            vtdGen.setDoc(xmlByteArray);
            try {
                vtdGen.parse(true); // set namespace awareness to true
            } catch (EOFException eOFException) {
                logger.error("{} eOFException in vtdGen.parse from: {}", this.threadId, xmlString, eOFException);
            } catch (EntityException entityException) {
                logger.error("{} entityException in vtdGen.parse from: {}", this.threadId, xmlString, entityException);
            } catch (EncodingException e) {
                logger.error("{} EncodingException in vtdGen.parse from: {}", this.threadId, xmlString, e);
            } catch (com.ximpleware.ParseException parseException) { // full path to ParseException is necessary as there exists another ParseException class
                logger.error("{} parseException in vtdGen.parse from: {}", this.threadId, xmlString, parseException);
            }

            final VTDNav vtdNav = vtdGen.getNav();
            final AutoPilot autoPilot = new AutoPilot(vtdNav);
//            autoPilot.selectElementNS("*", "*");

            try {
                getScraperEventsInner(startTime, fullRun, checkAll, autoPilot, vtdNav, listSize, scrapedEventsCounter);
            } catch (XPathParseException xPathParseException) {
                logger.error("{} xPathParseException in autoPilot", this.threadId, xPathParseException);
            } catch (XPathEvalException xPathEvalException) {
                logger.error("{} xPathEvalException in autoPilot", this.threadId, xPathEvalException);
            } catch (NavException navException) {
                logger.error("{} navException in autoPilot", this.threadId, navException);
            }
        } else {
            logger.error("STRANGE {} null xmlByteArray for: {}", this.threadId, xmlString);
        }

        final long currentTime = System.currentTimeMillis();
        final long totalRunTime = currentTime - startTime;
        final long timeGettingPage = afterAsXmlTime - startTime;
        if ((fullRun || checkAll) && !this.singleLogger) {
            if (this.averageLoggerFull != null) {
                this.averageLoggerFull.addRecords(listSize.get(), scrapedEventsCounter.get(), totalRunTime, timeGettingPage);
            }
        } else {
            if (this.averageLogger != null) {
                this.averageLogger.addRecords(listSize.get(), scrapedEventsCounter.get(), totalRunTime, timeGettingPage);
            }
        }
        if (Statics.debugLevel.check(2, 188)) {
            final String fullRunString = fullRun ? " fullRun" : "";
            final String checkAllString = checkAll ? " checkAll" : "";
            logger.info("{} getScraperEvents{}{} listSize/scraped: {}/{} took: {} ms of which {} ms getting htmlPage", this.threadId, checkAllString, fullRunString, listSize.get(), scrapedEventsCounter.get(), totalRunTime, timeGettingPage);
        }

        long neededExtraDelay;
        if (checkAll) {
            neededExtraDelay = 0L;
        } else if (fullRun) {
            neededExtraDelay = totalRunTime - this.delayGetScraperEvents;
        } else {
            neededExtraDelay = (totalRunTime << 1) - this.delayGetScraperEvents;
        }
        if (neededExtraDelay > 0) {
            if (neededExtraDelay > 1_000L) {
                if (neededExtraDelay > 2_000L) {
                    logger.warn("{} reducing big neededExtraDelay from {}ms to 1000ms", this.threadId, neededExtraDelay); // also warn, as this happens often due to small clock jumps
                } else {
                    logger.warn("{} reducing neededExtraDelay from {}ms to 1000ms", this.threadId, neededExtraDelay);
                }
                neededExtraDelay = 1_000L; // maximum; it sometimes gets higher during clock jumps
            }

            Formulas.addAndGetLastGetScraperEvents(this.lastGetScraperEvents, neededExtraDelay); // avoids scraper using all processor
            final String fullRunString = fullRun ? "fullRun " : "";
//            final String checkAllString = checkAll ? "checkAll " : ""; // checkAll always false when extra delay applied
            logger.warn("{} extra scraper {}{}delay applied: {} ms", this.threadId, "", fullRunString, neededExtraDelay); // fairly large processor usage, needs at least warn
        }
    }

    private long timedGetScraperEvents(final HtmlPage htmlPage) {
        long timeForNext = Formulas.getLastGetScraperEvents(this.lastGetScraperEvents);
        long timeTillNext = timeForNext - System.currentTimeMillis();
        if (timeTillNext <= 0) {
            getScraperEvents(htmlPage);
            //noinspection ReuseOfLocalVariable
            timeForNext = Formulas.getLastGetScraperEvents(this.lastGetScraperEvents);

            timeTillNext = timeForNext - System.currentTimeMillis();
        } else { // nothing to be done
        }
        return timeTillNext;
    }

    @Nullable
    public HtmlPage getHtmlPage(final WebClient webClient) { // has to be overriden
        return null;
    }

    void pageManipulation(final WebClient webClient, final HtmlPage htmlPage)
            throws IOException { // should be overriden if used; should contain condional checks for running it
    }

    String endSavePrefix() { // can be overriden to change the prefix
        return this.saveFolder + "/end";
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod", "NestedTryStatement"})
    @Override
    public void run() {
        @Nullable WebClient newWebClient = null;
        @Nullable GetPageThread getPageThread = null;
        Thread threadSave = null;
        do {
            Thread threadGet = null;
            @Nullable WebClient webClient = null;
            Cache cache = null;
            this.mustRefreshPage.set(false); // is just getting a new client/page

            try {
                if (newWebClient != null) {
                    webClient = newWebClient; // webClient has previously been closed in finally
                    cache = webClient.getCache();
                    newWebClient = null;
                } else {
                    webClient = WebScraperMethods.getNewWebClient(this.browserVersion, this.threadId);
                    cache = WebScraperMethods.initializeCache(webClient, this.cacheFileName, this.cacheMaxSize, this.threadId);
                }

                final long pageGetTime = System.currentTimeMillis();
                this.lastUpdatedScraperEvent.set(pageGetTime); // updated at every page get
                this.lastPageGet.set(pageGetTime);
                final HtmlPage htmlPage, threadHtmlPage = getPageThread != null ? getPageThread.getHtmlPage() : null;
                if (getPageThread != null && threadHtmlPage != null) {
                    htmlPage = threadHtmlPage;
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
                                Betty.programSleeps(Statics.mustSleep, Statics.mustStop, this.threadId + " scraper");
                            }
                            if (Statics.debugLevel.check(3, 187)) {
                                logger.info("{} javaScriptJobCount: {} cache size: {}", this.threadId, javaScriptJobManager.getJobCount(), cache.getSize());
                            }

                            if ((this.mustRefreshPage.get() || javaScriptJobManager.getJobCount() <= 0) && threadGet == null) {
                                // close window and reload page is the only reliable way for refresh
                                if (threadSave != null && threadSave.isAlive()) {
                                    Generic.alreadyPrintedMap.logOnce(Statics.debugLevel.check(3, 196), Generic.MINUTE_LENGTH_MILLISECONDS * 5L, logger, LogLevel.WARN, "{} threadSave still alive: won't refresh yet", this.threadId);
                                } else {
                                    logger.info("{} mustRefreshPage encountered", this.threadId);

                                    newWebClient = WebScraperMethods.getNewWebClient(this.browserVersion, this.threadId);
                                    WebScraperMethods.initializeCache(newWebClient, cache, this.cacheMaxSize, this.threadId);
                                    getPageThread = new GetPageThread(this, newWebClient);

                                    threadGet = new Thread(getPageThread);
                                    threadGet.start();
                                }
                            }
                            if (getPageThread != null && getPageThread.isHasRun()) {
                                final long timeSincePreviousPageGet = System.currentTimeMillis() - this.lastPageGet.get();
                                if (timeSincePreviousPageGet >= 20_000L) {
                                    break; // this will break out of the inner loop, which will cause window close, clear cache, and the page loaded again in the outer loop
                                } else { // wait a bit, avoid throttle; no need to print message, as that would flood the log
                                }
                            }
                            if (this.mustSavePage.get()) {
                                logger.info("{} mustSavePage encountered", this.threadId);
                                WebScraperMethods.savePage(htmlPage, this.mustSavePage, this.saveFolder + "/save", this.threadId);
                            }

                            final long timeToSleep;

                            timeToSleep = timedGetScraperEvents(htmlPage);

                            Generic.threadSleepSegmented(timeToSleep, 100L, Statics.mustStop, this.mustSavePage, this.mustRefreshPage);
//                        } catch (RejectedExecutionException rejectedExecutionException) {
//                            logger.error("exception in {} scraper inner loop", threadId, rejectedExecutionException);
//                            if (!Statics.mustStop.get()) {
//                                Generic.threadSleep(100L); // avoid throttle
//                            }
                        } catch (RuntimeException | IOException exception) {
                            logger.error("exception in {} scraper inner loop", this.threadId, exception);
                            if (!Statics.mustStop.get()) {
                                Generic.threadSleep(100L); // avoid throttle
                            }
                        } catch (Throwable throwable) { // inner safety net
                            logger.error("exception in {} scraper safety net inner loop", this.threadId, throwable);
                            if (!Statics.mustStop.get()) {
                                Generic.threadSleep(100L); // avoid throttle
                            }
                        }
                    } // end inner while
                    logger.info("{} scraper page finished executing: {} {}", this.threadId, htmlPage, htmlPage.getTitleText());

                    if (threadSave != null && threadSave.isAlive()) {
                        if (Statics.mustStop.get()) {
                            logger.info("parallel save thread still alive in {} scraperThread", this.threadId);
                        } else {
                            logger.error("parallel save thread still alive in {} scraperThread", this.threadId);
                        }

                        try {
                            threadSave.join();
                        } catch (InterruptedException interruptedException) {
                            logger.error("STRANGE interruptedException in threadSave join in {} scraperThread", this.threadId, interruptedException);
                        }
                        if (Statics.mustStop.get()) {
                            logger.info("parallel save thread finally dead in {} scraperThread", this.threadId);
                        } else {
                            logger.error("parallel save thread finally dead in {} scraperThread", this.threadId);
                        }
                    }
                    final SavePageThread savePageThread = new SavePageThread(this, endSavePrefix(), webClient, htmlPage, cache);
                    threadSave = new Thread(savePageThread);
                    threadSave.start();
                    webClient = null; // avoids closeAllWindows from finally
                } else {
                    Generic.threadSleepSegmented(1_000L, 100L, Statics.mustStop); // avoid throttle
                }
            } catch (Throwable throwable) { // safety net
                logger.error("exception in {} scraper safety net", this.threadId, throwable);

                try {
                    if (cache != null) {
                        WebScraperMethods.clearCache(cache, this.cacheFileName, this.threadId);
                    }
                } catch (Throwable innerThrowable) { // safety net inside safety net
                    logger.error("exception in {} scraper safety net's safety net", this.threadId, innerThrowable);
                }
            } finally {
                if (webClient != null) {
                    webClient.close();
                }
                if (threadGet != null && threadGet.isAlive()) {
                    if (!Statics.mustStop.get()) {
                        logger.error("parallel get thread still alive in {} scraperThread", this.threadId);
                    }
                    try {
                        threadGet.join();
                    } catch (InterruptedException interruptedException) {
                        logger.error("STRANGE interruptedException in threadGet join in {} scraperThread", this.threadId, interruptedException);
                    }
                    if (!Statics.mustStop.get()) {
                        logger.error("parallel get thread finally dead in {} scraperThread", this.threadId);
                    }
                } // end if threadGet
            } // end finally
        } while (!Statics.mustStop.get());
        if (threadSave != null && threadSave.isAlive()) {
            logger.info("parallel save thread still alive in {} scraperThread end", this.threadId);
            try {
                threadSave.join();
            } catch (InterruptedException interruptedException) {
                logger.error("STRANGE interruptedException in threadSave join in {} scraperThread end", this.threadId, interruptedException);
            }
            logger.info("parallel save thread finally dead in {} scraperThread end", this.threadId);
        } // end if threadSave

        if (newWebClient != null) {
            newWebClient.close();
        }

        logger.debug("{} scraper thread ends", this.threadId);
    }
}
