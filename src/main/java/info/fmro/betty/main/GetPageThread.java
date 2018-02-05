package info.fmro.betty.main;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetPageThread
        implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(GetPageThread.class);
    public final ScraperThread scraperThread;
    public final WebClient webClient;
    @SuppressWarnings("PublicField")
    public HtmlPage htmlPage;
    @SuppressWarnings("PublicField")
    public boolean hasRun;

    public GetPageThread(ScraperThread scraperThread, WebClient webClient) {
        this.scraperThread = scraperThread;
        this.webClient = webClient;
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
