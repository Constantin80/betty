package info.fmro.betty.main;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoralScraperThreadIT {

    private static final Logger logger = LoggerFactory.getLogger(CoralScraperThreadIT.class);

    @Rule
    @SuppressWarnings("PublicField")
    public TestRule watchman = new TestWatcher() {
        @Override
        public void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

//    @Test
//    public void testGetScraperEvents() {
//        System.out.println("getScraperEvents");
//        HtmlPage htmlPage = null;
//        CoralScraperThread.getScraperEvents(htmlPage);
//        fail("The test case is a prototype.");
//    }
//
//    @Test
//    public void testTimedGetScraperEvents() {
//        System.out.println("timedGetScraperEvents");
//        HtmlPage htmlPage = null;
//        long expResult = 0L;
//        long result = CoralScraperThread.timedGetScraperEvents(htmlPage);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }
    // test disabled for now; RDS blocking of Coral prevents any integration testing
    @Test
    public void testRun() {
        // dummy empty method
//        Generic.disableHTTPSValidation();
//        Generic.turnOffHtmlUnitLogger();
//
//        CoralScraperThread instance = new CoralScraperThread();
//        instance.run();
    }
}
