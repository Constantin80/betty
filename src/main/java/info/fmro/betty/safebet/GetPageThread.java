package info.fmro.betty.safebet;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jetbrains.annotations.Contract;

class GetPageThread
        implements Runnable {
    private final ScraperPermanentThread scraperThread;
    private final WebClient webClient;
    private HtmlPage htmlPage;
    private boolean hasRun;

    @Contract(pure = true)
    GetPageThread(final ScraperPermanentThread scraperThread, final WebClient webClient) {
        this.scraperThread = scraperThread;
        this.webClient = webClient;
    }

    HtmlPage getHtmlPage() { // probably shouldn't synchronize this method
        return this.htmlPage;
    }

    boolean isHasRun() { // probably shouldn't synchronize this method
        return this.hasRun;
    }

    @Override
    public void run() {
        try {
            this.htmlPage = this.scraperThread.getHtmlPage(this.webClient);
        } finally {
            this.hasRun = true;
        }
    }
}
