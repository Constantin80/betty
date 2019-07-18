package info.fmro.betty.threads;

import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import info.fmro.betty.safebet.ScraperPermanentThread;
import info.fmro.betty.utility.WebScraperMethods;
import org.jetbrains.annotations.Contract;

public class SavePageThread
        implements Runnable {
    private final ScraperPermanentThread scraperThread;
    private final String savePrefix;
    private final WebClient webClient;
    private final HtmlPage htmlPage;
    private final Cache cache;
    private boolean hasRun;

    @Contract(pure = true)
    public SavePageThread(final ScraperPermanentThread scraperThread, final String savePrefix, final WebClient webClient, final HtmlPage htmlPage, final Cache cache) {
        this.scraperThread = scraperThread;
        this.savePrefix = savePrefix;
        this.webClient = webClient;
        this.htmlPage = htmlPage;
        this.cache = cache;
    }

    public boolean isHasRun() { // probably no need for synchronization
        return this.hasRun;
    }

    @Override
    public void run() {
        try {
            if (this.savePrefix != null) {
                WebScraperMethods.savePage(this.htmlPage, this.scraperThread.mustSavePage, this.savePrefix, this.scraperThread.threadId);
            }
            WebScraperMethods.clearCache(this.cache, this.scraperThread.cacheFileName, this.scraperThread.threadId);
        } finally {
            if (this.webClient != null) {
                this.webClient.close();
            }
            this.hasRun = true;
        }
    }
}
