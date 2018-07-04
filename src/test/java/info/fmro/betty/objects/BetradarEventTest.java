package info.fmro.betty.objects;

import info.fmro.betty.enums.MatchStatus;
import info.fmro.betty.main.ApiDefault;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BetradarEventTest
        extends ApiDefault {
    private static final long startTime = System.currentTimeMillis();

    public BetradarEventTest() {
    }

    @Test
    void getHomeScore() {
        BetradarEvent instance = new BetradarEvent(1, startTime);
        int expResult = -1;
        int result = instance.getHomeScore();
        assertEquals(expResult, result, "1");

        BetradarEvent secondInstance = new BetradarEvent(1, startTime);
        instance.update(secondInstance);
        expResult = -1;
        result = instance.getHomeScore();
        assertEquals(expResult, result, "2");

        secondInstance = new BetradarEvent(1, startTime);
        secondInstance.setHomeScore(1);
        instance.update(secondInstance);
        expResult = 1;
        result = instance.getHomeScore();
        assertEquals(expResult, result, "3");
    }

    @Test
    void setHomeScore() {
        BetradarEvent instance = new BetradarEvent(1, startTime);
        instance.setHomeScore(1);
        int result = instance.getHomeScore();
        int expResult = 1;
        assertEquals(expResult, result);
    }

    @Test
    void getHomeHtScore() {
        BetradarEvent instance = new BetradarEvent(1, startTime);
        int expResult = -1;
        int result = instance.getHomeHtScore();
        assertEquals(expResult, result, "1");

        BetradarEvent secondInstance = new BetradarEvent(1, startTime);
        instance.update(secondInstance);
        expResult = -1;
        result = instance.getHomeHtScore();
        assertEquals(expResult, result, "2");

        secondInstance = new BetradarEvent(1, startTime);
        secondInstance.setHomeHtScore(1);
        instance.update(secondInstance);
        expResult = 1;
        result = instance.getHomeHtScore();
        assertEquals(expResult, result, "3");
    }

    @Test
    void setHomeHtScore() {
        BetradarEvent instance = new BetradarEvent(1, startTime);
        instance.setHomeHtScore(1);
        int result = instance.getHomeHtScore();
        int expResult = 1;
        assertEquals(expResult, result);
    }

    @Test
    void update() {
        BetradarEvent scraperEvent = new BetradarEvent(1, startTime);
        BetradarEvent instance = new BetradarEvent(1, startTime);
        int expResult = 0;
        int result = instance.update(scraperEvent);
        assertEquals(expResult, result);
    }

    @Test
    void errors() {
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
