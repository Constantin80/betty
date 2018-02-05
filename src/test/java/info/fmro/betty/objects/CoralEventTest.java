package info.fmro.betty.objects;

import info.fmro.betty.enums.MatchStatus;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoralEventTest {

    private static final Logger logger = LoggerFactory.getLogger(CoralEventTest.class);

    @Rule
    @SuppressWarnings("PublicField")
    public TestRule watchman = new TestWatcher() {
        @Override
        public void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

    @Test
    public void testUpdateSeconds() {
        CoralEvent instance = new CoralEvent(0L, 1L), secondInstance = new CoralEvent(0L, 1L);
        int result = instance.update(secondInstance);
        int expResult = 0;
        assertEquals("first", expResult, result);

        secondInstance.setSeconds(3);
        result = instance.update(secondInstance);
        expResult = 1;
        assertEquals("second", expResult, result);
    }

    @Test
    public void testSetMatchStatus() {
        CoralEvent instance = new CoralEvent(0L, 1L);
        instance.setHomeScore(1);
        instance.setHomeTeam("a");
        instance.setAwayScore(2);
        instance.setAwayTeam("b");
        instance.setMatchStatus(MatchStatus.HALF_TIME);
        long result = instance.errors();
        long expResult = 0L;
        assertEquals("first", expResult, result);
//
//        secondInstance.setSeconds(3);
//        result = instance.update(secondInstance);
//        expResult = 1;
//        assertEquals("second", expResult, result);
    }
}
