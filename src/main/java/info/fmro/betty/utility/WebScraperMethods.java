package info.fmro.betty.utility;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TopLevelWindow;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.SerialClone;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings({"ClassWithTooManyMethods", "UtilityClass"})
public final class WebScraperMethods {
    private static final Logger logger = LoggerFactory.getLogger(WebScraperMethods.class);
    public static final String ONLY_PRINT_INFO_IF_FAIL_PREFIX = "(onlyPrintInfoIfFail)";
    public static final long defaultInitialWaitForScripts = 25_000L, defaultSecondaryWaitForScripts = 1_000L, defaultFinalWaitForScripts = 5_000L;
    public static final int defaultCacheMaxSize = 200;

    @Contract(pure = true)
    private WebScraperMethods() {
    }

//    public static boolean addSsoidCookie(WebClient webClient, String threadId) { // likely only needed for betfair scraper
//        boolean success;
//
//        while (Statics.needSessionToken.get() && !Statics.mustStop.get()) {
//            logger.info("{} web scraper waiting for sessionToken...", threadId);
//            Generic.threadSleep(100);
//        }
//        if (!Statics.needSessionToken.get()) {
//            final CookieManager cookieManager = webClient.getCookieManager();
//            final Cookie cookie = new Cookie(".betfair.com", "ssoid", Statics.sessionTokenObject.getSessionToken());
//            cookieManager.addCookie(cookie);
//            success = true;
//        } else {
//            success = false;
//        }
//
//        return success;
//    }

    public static boolean savePage(final HtmlPage htmlPage, final AtomicBoolean mustSavePage, final String threadId) {
        return savePage(htmlPage, mustSavePage, "pages/", "page", ".html", threadId);
    }

    public static boolean savePage(final HtmlPage htmlPage, final AtomicBoolean mustSavePage, final String prefix, final String threadId) {
        return savePage(htmlPage, mustSavePage, prefix, "page", ".html", threadId);
    }

    public static boolean savePage(final HtmlPage htmlPage, final AtomicBoolean mustSavePage, final String prefix, final String baseName, final String threadId) {
        return savePage(htmlPage, mustSavePage, prefix, baseName, ".html", threadId);
    }

    private static boolean savePage(final HtmlPage htmlPage, final AtomicBoolean mustSavePage, final String prefix, final String baseName, final String suffix, final String threadId) {
        boolean success;
        try {
            final long timeBegin = System.currentTimeMillis();
            final String fileName = Generic.tempFileName(prefix + baseName + suffix);
            //noinspection NestedTryStatement
            try {
                htmlPage.save(new File(fileName));
            } catch (@SuppressWarnings("ProhibitedExceptionCaught") NullPointerException nullPointerException) { // HtmlUnit has a NullPointerException bug, that I reported, in build 2.21
                logger.error("nullPointerException while saving htmlPage in file: {}", fileName, nullPointerException);
            }
            logger.info("{} saving page took {} ms {} {}", threadId, System.currentTimeMillis() - timeBegin, fileName, htmlPage);

            if (mustSavePage != null && mustSavePage.get()) {
                mustSavePage.set(false);
            }
            success = true;
        } catch (IOException iOException) {
            logger.error("{} iOException during save Page", threadId, iOException);
            success = false;
        }

        return success;
    }

    public static void clearCache(final Cache cache, final String threadId) {
        clearCache(cache, null, threadId);
    }

    public static void clearCache(final Cache cache, final String fileName, final String threadId) {
        if (cache != null) {
            if (fileName != null) {
                Generic.synchronizedWriteObjectToFile(cache, fileName);
            }
            final int cacheSize = cache.getSize();
            cache.clear();
            logger.info("{} cleared cache, previous size: {} , size after: {} , max size: {}", threadId, cacheSize, cache.getSize(), cache.getMaxSize());
        } else {
            logger.error("{} null cache in clearCache", threadId);
        }
    }

    public static void closeTopLevelWindow(final Page htmlPage, final String threadId) {
        if (htmlPage != null) {
            final TopLevelWindow topLevelWindow = (TopLevelWindow) htmlPage.getEnclosingWindow().getTopWindow();
            topLevelWindow.close();
            logger.info("{} closed top level window for: {}", threadId, htmlPage);
        } else {
            logger.error("{} null htmlPage in closeTopLevelWindow", threadId);
        }
    }

    //    public static boolean refreshPage(HtmlPage htmlPage, AtomicBoolean mustRefreshPage, AtomicBoolean mustSavePage) {
//        return refreshPage(htmlPage, mustRefreshPage, mustSavePage, defaultInitialWaitForScripts, true, true);
//    }
//
//    public static boolean refreshPage(HtmlPage htmlPage, AtomicBoolean mustRefreshPage, AtomicBoolean mustSavePage, long waitForScripts) {
//        return refreshPage(htmlPage, mustRefreshPage, mustSavePage, waitForScripts, true, true);
//    }
//
//    public static boolean refreshPage(HtmlPage htmlPage, AtomicBoolean mustRefreshPage, AtomicBoolean mustSavePage, long waitForScripts, boolean saveBefore, boolean saveAfter) {
//        boolean success;
//
//        if (saveBefore) {
//            savePage(htmlPage, mustSavePage, "pages/beforerefresh", threadId);
//        }
//
//        // closeTopLevelWindow(htmlPage); // this closes the window and it cannot be reopened
//        clearCache(htmlPage.getWebClient().getCache(), threadId);
//
//        logger.info("using page refresh: {}", htmlPage.toString());
//        try {
//            final long timeBeforeRefresh = System.currentTimeMillis();
//            htmlPage.refresh();
//            logger.info("refresh took {} ms", System.currentTimeMillis() - timeBeforeRefresh);
//
//            if (mustRefreshPage.get()) {
//                mustRefreshPage.set(false);
//            }
//            success = true;
//        } catch (IOException iOException) {
//            logger.error("iOException for page refresh", iOException);
//            success = false;
//        }
//
//        if (waitForScripts >= 0) {
//            waitForScripts(htmlPage.getWebClient(), waitForScripts);
//        }
//
//        if (saveAfter) {
//            savePage(htmlPage, mustSavePage, "pages/afterrefresh", threadId);
//        }
//
//        return success;
//    }
    public static HtmlPage getPage(final WebClient webClient, final String savePrefix, final AtomicBoolean mustRefreshPage, final AtomicBoolean mustSavePage, final String url, final String threadId, final String... expressionXPaths) {
        return getPage(webClient, savePrefix, mustRefreshPage, mustSavePage, defaultInitialWaitForScripts, defaultSecondaryWaitForScripts, defaultFinalWaitForScripts, url, threadId, expressionXPaths);
    }

    public static HtmlPage getPage(final WebClient webClient, final String savePrefix, final AtomicBoolean mustRefreshPage, final AtomicBoolean mustSavePage, final long waitForScripts, final String url, final String threadId,
                                   final String... expressionXPaths) {
        return getPage(webClient, savePrefix, mustRefreshPage, mustSavePage, waitForScripts, defaultSecondaryWaitForScripts, defaultFinalWaitForScripts, url, threadId, expressionXPaths);
    }

    public static HtmlPage getPage(@NotNull final WebClient webClient, final String savePrefix, final AtomicBoolean mustRefreshPage, final AtomicBoolean mustSavePage, final long waitForScripts, final long secondaryWaitForScripts,
                                   final long finalWaitForScripts, final String url, final String threadId, final String... expressionXPaths) {
        @Nullable HtmlPage htmlPage;
        final long beginLoadPageTime = System.currentTimeMillis();
        try {
            htmlPage = webClient.getPage(url);
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") IOException | FailingHttpStatusCodeException exception) {
            logger.error("{} exception in getPage for url: {}", threadId, url, exception);
            htmlPage = null;
        }
        if (htmlPage != null) {
            logger.info("{} page finished loading in {}ms: {} {}", threadId, System.currentTimeMillis() - beginLoadPageTime, htmlPage, htmlPage.getTitleText());

            waitForScripts(webClient, waitForScripts, threadId);

            if (expressionXPaths != null && expressionXPaths.length > 0) {
                for (final String expressionXPath : expressionXPaths) {
                    htmlPage = clickElements(webClient, mustRefreshPage, htmlPage, secondaryWaitForScripts, expressionXPath, threadId, savePrefix, mustSavePage);
                } // end for
                waitForScripts(webClient, finalWaitForScripts, threadId);
            } else { // no secondary elements to be clicked
            }

            if (savePrefix != null) {
                savePage(htmlPage, mustSavePage, savePrefix, threadId);
            }
            // no longer necessary, as I now set this at beginning of outer while in ScraperPermanentThread
//            if (mustRefreshPage.get()) {
//                mustRefreshPage.set(false);
//            }
        } // end if htmlPage != null

        return htmlPage;
    }

    public static HtmlPage clickElement(final WebClient webClient, final AtomicBoolean mustRefreshPage, final HtmlPage htmlPage, final String expressionXPath, final String threadId, final String savePrefix, final AtomicBoolean mustSavePage) {
        return clickElement(webClient, mustRefreshPage, htmlPage, defaultSecondaryWaitForScripts, expressionXPath, threadId, savePrefix, mustSavePage);
    }

    private static HtmlPage clickElement(final WebClient webClient, final AtomicBoolean mustRefreshPage, final HtmlPage htmlPage, final long secondaryWaitForScripts, @NotNull final String expressionXPath,
                                         final String threadId, final String savePrefix, final AtomicBoolean mustSavePage) {
        // clicks only the first found element
        HtmlPage page = htmlPage;
        String xPath = expressionXPath;
        final boolean onlyPrintInfoIfFail;
        if (xPath.startsWith(ONLY_PRINT_INFO_IF_FAIL_PREFIX)) {
            onlyPrintInfoIfFail = true;
            xPath = xPath.substring(ONLY_PRINT_INFO_IF_FAIL_PREFIX.length());
        } else {
            onlyPrintInfoIfFail = false;
        }

        final HtmlElement htmlElement = page.getFirstByXPath(xPath);
        if (htmlElement != null) {
            if (Statics.debugLevel.check(3, 129)) {
                logger.info("{} htmlElement: {}", threadId, htmlElement.asXml());
            }
            try {
                page = htmlElement.click();
            } catch (IOException iOException) {
                logger.error("{} iOException while clicking on htmlElement", threadId, iOException);
            }

            waitForScripts(webClient, secondaryWaitForScripts, threadId);
        } else {
            if (onlyPrintInfoIfFail) {
                logger.warn("{} htmlElement not found for: {}", threadId, xPath);
            } else {
                logger.error("{} htmlElement not found, will save page, for: {}", threadId, xPath);
                savePage(page, mustSavePage, savePrefix, threadId);
                mustRefreshPage.set(true);
            }
        }
        return page;
    }

    public static HtmlPage clickElements(final WebClient webClient, final AtomicBoolean mustRefreshPage, final HtmlPage htmlPage, final String expressionXPath, final String threadId, final String savePrefix, final AtomicBoolean mustSavePage) {
        return clickElements(webClient, mustRefreshPage, htmlPage, defaultSecondaryWaitForScripts, expressionXPath, threadId, savePrefix, mustSavePage);
    }

    public static HtmlPage clickElements(final WebClient webClient, final AtomicBoolean mustRefreshPage, final HtmlPage htmlPage, final long secondaryWaitForScripts, @NotNull final String expressionXPath, final String threadId, final String savePrefix,
                                         final AtomicBoolean mustSavePage) {
        // clicks all found elements
        HtmlPage page = htmlPage;
        String xPath = expressionXPath;
        final boolean onlyPrintInfoIfFail;
        if (xPath.startsWith(ONLY_PRINT_INFO_IF_FAIL_PREFIX)) {
            onlyPrintInfoIfFail = true;
            xPath = xPath.substring(ONLY_PRINT_INFO_IF_FAIL_PREFIX.length());
        } else {
            onlyPrintInfoIfFail = false;
        }

        final List<?> htmlElements = page.getByXPath(xPath);
        if (htmlElements.isEmpty()) {
            if (onlyPrintInfoIfFail) {
                logger.warn("{} {} htmlElements found for: {}", threadId, htmlElements.size(), xPath);
            } else {
                logger.error("{} {} htmlElements found, will save page, for: {}", threadId, htmlElements.size(), xPath);
                savePage(page, mustSavePage, savePrefix, threadId);
                mustRefreshPage.set(true);
            }
        } else {
            for (final Object htmlElement : htmlElements) {
                if (htmlElement != null) {
                    if (Statics.debugLevel.check(3, 129)) {
                        logger.info("{} htmlElement: {}", threadId, ((DomNode) htmlElement).asXml());
                    }
                    try {
                        page = ((DomElement) htmlElement).click();
                    } catch (IOException iOException) {
                        logger.error("{} iOException while clicking on htmlElement", threadId, iOException);
                    }

                    waitForScripts(webClient, secondaryWaitForScripts, threadId);
                } else {
                    // no onlyPrintInfoIfFail support here, as htmlElements.size should be zero if element is not found
                    // on this branch it's always an error, probably related to real time update of the page by JavaScript
                    logger.error("{} htmlElement not found inside loop, will save page, for: {}", threadId, xPath);
                    savePage(page, mustSavePage, savePrefix, threadId);
                    mustRefreshPage.set(true);
                }
            } // end for
        }
        return page;
    }

    public static void waitForScripts(final WebClient webClient, final String threadId) {
        waitForScripts(webClient, defaultInitialWaitForScripts, threadId);
    }

    public static void waitForScripts(final WebClient webClient, final long waitForScripts, final String threadId) {
        if (waitForScripts >= 0) {
            final long beginExecuteScriptsTime = System.currentTimeMillis();
            final int jobsStillActive = webClient.waitForBackgroundJavaScriptStartingBefore(waitForScripts);
            logger.info("{} waitForBackgroundJavaScriptStartingBefore({}) finished in {} ms , {} jobsStillActive", threadId, waitForScripts, System.currentTimeMillis() - beginExecuteScriptsTime, jobsStillActive);
        }
    }

    public static ArrayList<String> getAlertsList(@NotNull final WebClient webClient) {
        final ArrayList<String> collectedAlertsList = new ArrayList<>(0);
        final AlertHandler collectingAlertHandler = new CollectingAlertHandler(collectedAlertsList);
        webClient.setAlertHandler(collectingAlertHandler);

        return collectedAlertsList;
    }

    public static Cache initializeCache(final WebClient webClient, final String threadId) {
        return initializeCache(webClient, null, null, defaultCacheMaxSize, threadId);
    }

    public static Cache initializeCache(final WebClient webClient, final String fileName, final String threadId) {
        return initializeCache(webClient, fileName, null, defaultCacheMaxSize, threadId);
    }

    public static Cache initializeCache(final WebClient webClient, final String fileName, final int cacheMaxSize, final String threadId) {
        return initializeCache(webClient, fileName, null, cacheMaxSize, threadId);
    }

    public static Cache initializeCache(final WebClient webClient, final Cache sourceCache, final String threadId) {
        return initializeCache(webClient, null, sourceCache, defaultCacheMaxSize, threadId);
    }

    public static Cache initializeCache(final WebClient webClient, final Cache sourceCache, final int cacheMaxSize, final String threadId) {
        return initializeCache(webClient, null, sourceCache, cacheMaxSize, threadId);
    }

    public static Cache initializeCache(final WebClient webClient, final int cacheMaxSize, final String threadId) {
        return initializeCache(webClient, null, null, cacheMaxSize, threadId);
    }

    private static Cache initializeCache(final WebClient webClient, final String fileName, final Cache sourceCache, final int cacheMaxSize, final String threadId) {
        @Nullable Cache cache;
        if (sourceCache != null) {
            cache = SerialClone.clone(sourceCache);
        } else if (fileName != null) {
            cache = (Cache) Generic.readObjectFromFile(fileName);
        } else {
            cache = null;
        }
        if (cache != null) {
            webClient.setCache(cache);
        } else {
            cache = webClient.getCache();
        }

        final int initialCacheMaxSize = cache.getMaxSize();
        cache.setMaxSize(cacheMaxSize);
        logger.info("{} cache maxSize: {} initial: {}", threadId, cache.getMaxSize(), initialCacheMaxSize);

        return cache;
    }

    public static WebClient getNewWebClient(final String threadId) {
        // return getNewWebClient(BrowserVersion.INTERNET_EXPLORER_8, threadId);
        return getNewWebClient(BrowserVersion.FIREFOX_52, threadId);
    }

    public static WebClient getNewWebClient(final BrowserVersion browserVersion, final String threadId) {
//        browserVersion.setApplicationCodeName(BrowserVersion.INTERNET_EXPLORER_8.getApplicationCodeName());
//        browserVersion.setApplicationMinorVersion(BrowserVersion.INTERNET_EXPLORER_8.getApplicationMinorVersion());
//        browserVersion.setApplicationName(BrowserVersion.INTERNET_EXPLORER_8.getApplicationName());
//        browserVersion.setApplicationVersion(BrowserVersion.INTERNET_EXPLORER_8.getApplicationVersion());
//        browserVersion.setBrowserLanguage(BrowserVersion.INTERNET_EXPLORER_8.getBrowserLanguage());
//        browserVersion.setBrowserVersion(BrowserVersion.INTERNET_EXPLORER_8.getBrowserVersionNumeric());
//        browserVersion.setCpuClass(BrowserVersion.INTERNET_EXPLORER_8.getCpuClass());
//        browserVersion.setCssAcceptHeader(BrowserVersion.INTERNET_EXPLORER_8.getCssAcceptHeader());
//        browserVersion.setHtmlAcceptHeader(BrowserVersion.INTERNET_EXPLORER_8.getHtmlAcceptHeader());
//        browserVersion.setImgAcceptHeader(BrowserVersion.INTERNET_EXPLORER_8.getImgAcceptHeader());
//        browserVersion.setPlatform(BrowserVersion.INTERNET_EXPLORER_8.getPlatform());
//        browserVersion.setScriptAcceptHeader(BrowserVersion.INTERNET_EXPLORER_8.getScriptAcceptHeader());
//        browserVersion.setSystemLanguage(BrowserVersion.INTERNET_EXPLORER_8.getSystemLanguage());
//        browserVersion.setUserAgent(BrowserVersion.INTERNET_EXPLORER_8.getUserAgent());
//        browserVersion.setUserLanguage(BrowserVersion.INTERNET_EXPLORER_8.getUserLanguage());
//        browserVersion.setVendor(BrowserVersion.INTERNET_EXPLORER_8.getVendor());
//        browserVersion.setXmlHttpRequestAcceptHeader(BrowserVersion.INTERNET_EXPLORER_8.getXmlHttpRequestAcceptHeader());

        final WebClient webClient = new WebClient(browserVersion);
        logger.info("{} browserVersion: {}", threadId, webClient.getBrowserVersion());
        final WebClientOptions webClientOptions = webClient.getOptions();
        webClientOptions.setUseInsecureSSL(true);
//        webClientOptions.setCssEnabled(false); // this might be the cause that disables the refresh pop-up on betradar
        webClientOptions.setThrowExceptionOnScriptError(false);
        webClientOptions.setThrowExceptionOnFailingStatusCode(false);
        webClientOptions.setRedirectEnabled(true);
        webClientOptions.setPopupBlockerEnabled(false);
        return webClient;
    }
}
