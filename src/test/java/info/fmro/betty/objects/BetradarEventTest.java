package info.fmro.betty.objects;

import info.fmro.betty.main.ApiDefault;
import info.fmro.betty.safebet.BetradarEvent;
import info.fmro.shared.enums.MatchStatus;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BetradarEventTest
        extends ApiDefault {
    private static final long startTime = System.currentTimeMillis();

    @Test
    void getHomeScore() {
        final BetradarEvent instance = new BetradarEvent(1, startTime);
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
        final BetradarEvent instance = new BetradarEvent(1, startTime);
        instance.setHomeScore(1);
        final int result = instance.getHomeScore();
        final int expResult = 1;
        assertEquals(expResult, result);
    }

    @Test
    void getHomeHtScore() {
        final BetradarEvent instance = new BetradarEvent(1, startTime);
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
        final BetradarEvent instance = new BetradarEvent(1, startTime);
        instance.setHomeHtScore(1);
        final int result = instance.getHomeHtScore();
        final int expResult = 1;
        assertEquals(expResult, result);
    }

    @Test
    void update() {
        final BetradarEvent scraperEvent = new BetradarEvent(1, startTime);
        final BetradarEvent instance = new BetradarEvent(1, startTime);
        final int expResult = 0;
        final int result = instance.update(scraperEvent);
        assertEquals(expResult, result);
    }

    @Test
    void errors() {
        final BetradarEvent scraperEvent = new BetradarEvent(1, startTime);
        scraperEvent.timeStamp();
        scraperEvent.setStartTime(new Date());
        scraperEvent.setHomeTeam("Barcelona");
        scraperEvent.setAwayTeam("Real Madrid");
        final String classModifiers = "status_recentChange";
        scraperEvent.setClassModifiers(classModifiers);
        scraperEvent.setMatchStatus(MatchStatus.NOT_STARTED);
        final long expResult = 614400;
        final long result = scraperEvent.errors();
        assertEquals(expResult, result);
    }
}
