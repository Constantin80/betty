package info.fmro.betty.main;

import com.gargoylesoftware.htmlunit.Cache;
import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ScraperPermanentThreadTest
        extends ApiDefault {
    @Test
    void testCache() {
        final Cache cache;
        try (final WebClient webClient = new WebClient()) {
            cache = webClient.getCache();
        }

        assertNotNull(cache);
    }
}
