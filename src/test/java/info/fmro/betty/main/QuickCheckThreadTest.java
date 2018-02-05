package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickCheckThreadTest {

    private static final Logger logger = LoggerFactory.getLogger(QuickCheckThreadTest.class);

    @Rule
    @SuppressWarnings("PublicField")
    public TestRule watchman = new TestWatcher() {
        @Override
        public void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

    @Test
    public void testPopNRunsMarketBook() {
        long currentTime = System.currentTimeMillis();
        int expResult = 0;
        int result = QuickCheckThread.popNRunsMarketBook(currentTime);
        assertEquals(expResult, result);

        int size = 12;
        int marketsPerOperation = Statics.N_ALL;
        QuickCheckThread.nThreadsMarketBook.put(System.currentTimeMillis(), (int) Math.ceil((double) size / marketsPerOperation));
        expResult = 2;
        result = QuickCheckThread.popNRunsMarketBook(currentTime);
        assertEquals(expResult, result);
    }
}
