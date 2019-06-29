package info.fmro.betty.main;

import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import info.fmro.betty.utility.WebScraperMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavePageThread
        implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SavePageThread.class);
    public final ScraperThread scraperThread;
    public final String savePrefix;
    public final WebClient webClient;
    public final HtmlPage htmlPage;
    public final Cache cache;
    @SuppressWarnings("PublicField")
    public boolean hasRun;

    public SavePageThread(final ScraperThread scraperThread, final String savePrefix, final WebClient webClient, final HtmlPage htmlPage, final Cache cache) {
        this.scraperThread = scraperThread;
        this.savePrefix = savePrefix;
        this.webClient = webClient;
        this.htmlPage = htmlPage;
        this.cache = cache;
    }

    @Override
    public void run() {
        try {
            if (savePrefix != null) {
                WebScraperMethods.savePage(this.htmlPage, this.scraperThread.mustSavePage, savePrefix, scraperThread.threadId);
            }
            WebScraperMethods.clearCache(this.cache, this.scraperThread.cacheFileName, scraperThread.threadId);
        } finally {
            if (this.webClient != null) {
                this.webClient.close();
            }
            this.hasRun = true;
        }
    }
}
