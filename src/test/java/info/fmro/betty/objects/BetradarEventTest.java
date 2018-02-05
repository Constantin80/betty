package info.fmro.betty.objects;

import info.fmro.betty.enums.MatchStatus;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetradarEventTest {

    private static final Logger logger = LoggerFactory.getLogger(BetradarEventTest.class);
    private static final long startTime = System.currentTimeMillis();

    @Rule
    @SuppressWarnings("PublicField")
    public TestRule watchman = new TestWatcher() {
        @Override
        public void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

    public BetradarEventTest() {
    }

    @Test
    public void testGetHomeScore() {
        BetradarEvent instance = new BetradarEvent(1, startTime);
        int expResult = -1;
        int result = instance.getHomeScore();
        assertEquals(expResult, result);

        BetradarEvent secondInstance = new BetradarEvent(1, startTime);
        instance.update(secondInstance);
        expResult = -1;
        result = instance.getHomeScore();
        assertEquals(expResult, result);

        secondInstance = new BetradarEvent(1, startTime);
        secondInstance.setHomeScore(1);
        instance.update(secondInstance);
        expResult = 1;
        result = instance.getHomeScore();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetHomeScore() {
        BetradarEvent instance = new BetradarEvent(1, startTime);
        instance.setHomeScore(1);
        int result = instance.getHomeScore();
        int expResult = 1;
        assertEquals(expResult, result);
    }

    @Test
    public void testGetHomeHtScore() {
        BetradarEvent instance = new BetradarEvent(1, startTime);
        int expResult = -1;
        int result = instance.getHomeHtScore();
        assertEquals(expResult, result);

        BetradarEvent secondInstance = new BetradarEvent(1, startTime);
        instance.update(secondInstance);
        expResult = -1;
        result = instance.getHomeHtScore();
        assertEquals(expResult, result);

        secondInstance = new BetradarEvent(1, startTime);
        secondInstance.setHomeHtScore(1);
        instance.update(secondInstance);
        expResult = 1;
        result = instance.getHomeHtScore();
        assertEquals(expResult, result);
    }

    @Test
    public void testSetHomeHtScore() {
        BetradarEvent instance = new BetradarEvent(1, startTime);
        instance.setHomeHtScore(1);
        int result = instance.getHomeHtScore();
        int expResult = 1;
        assertEquals(expResult, result);
    }

    @Test
    public void testUpdate() {
        BetradarEvent scraperEvent = new BetradarEvent(1, startTime);
        BetradarEvent instance = new BetradarEvent(1, startTime);
        int expResult = 0;
        int result = instance.update(scraperEvent);
        assertEquals(expResult, result);
    }

    @Test
    public void testErrors() {
        BetradarEvent scraperEvent = new BetradarEvent(1, startTime);
        scraperEvent.timeStamp();
        scraperEvent.setStartTime(new Date());
        scraperEvent.setHomeTeam("Barcelona");
        scraperEvent.setAwayTeam("Real Madrid");
        String classModifiers = "status_recentChange";
        scraperEvent.setClassModifiers(classModifiers);
        scraperEvent.setMatchStatus(MatchStatus.NOT_STARTED);
        long expResult = 614400;
        long result = scraperEvent.errors();
        assertEquals(expResult, result);
    }
}
