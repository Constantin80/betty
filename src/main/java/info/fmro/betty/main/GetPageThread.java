package info.fmro.betty.main;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jetbrains.annotations.Contract;

class GetPageThread
        implements Runnable {
    private final ScraperThread scraperThread;
    private final WebClient webClient;
    private HtmlPage htmlPage;
    private boolean hasRun;

    @Contract(pure = true)
    GetPageThread(final ScraperThread scraperThread, final WebClient webClient) {
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
