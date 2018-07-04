package info.fmro.betty.main;

import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScraperThreadTest
        extends ApiDefault {
    @Test
    void testCache() {
        WebClient webClient = new WebClient();
        Cache cache = webClient.getCache();

        assertNotNull(cache);
    }
}
