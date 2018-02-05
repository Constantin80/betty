package info.fmro.betty.objects;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AverageLoggerTest {

    private static final Logger logger = LoggerFactory.getLogger(AverageLoggerTest.class);

    @Rule
    @SuppressWarnings("PublicField")
    public TestRule watchman = new TestWatcher() {
        @Override
        public void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

//    @Test
//    public void testGetMarketBooksExpectedRuns()
//            throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        AverageLogger instance = new AverageLogger();
//        Method method = AverageLogger.class.getDeclaredMethod("getMarketBooksExpectedRuns", double.class, double.class);
//        method.setAccessible(true);
//        int expResult = 0;
//        int result = (int) method.invoke(instance, 0d, 0d);
//        assertEquals(expResult, result);
//
//        expResult = 500;
//        result = (int) method.invoke(instance, 56d, 5d);
//        assertEquals(expResult, result);
//    }
}
