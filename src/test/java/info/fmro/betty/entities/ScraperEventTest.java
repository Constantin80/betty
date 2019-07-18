package info.fmro.betty.entities;

import info.fmro.betty.safebet.ScraperEvent;
import info.fmro.shared.utility.Generic;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class ScraperEventTest {
    @Test
    void update()
            throws NoSuchFieldException {
        try {
            final ScraperEvent firstObject = new ScraperEvent("objectId", 0L), secondObject = new ScraperEvent("objectId", 0L);
            Generic.fillRandom(firstObject);
            Generic.fillRandom(secondObject);

            final Long eventId = firstObject.getEventId();
            Field field = ScraperEvent.class.getDeclaredField("eventId");
            field.setAccessible(true);
            field.set(secondObject, eventId);
            final String objectId = firstObject.getObjectId();
            field = ScraperEvent.class.getDeclaredField("objectId");
            field.setAccessible(true);
            field.set(secondObject, objectId);

            final long currentTime = System.currentTimeMillis();
            firstObject.setTimeStamp(currentTime - 10_000L);
            firstObject.setHomeScoreTimeStamp(currentTime - 10_000L);
            firstObject.setAwayScoreTimeStamp(currentTime - 10_000L);
            firstObject.setHomeScore(-1);
            firstObject.setAwayScore(-1);
            firstObject.setHomeHtScore(-1);
            firstObject.setAwayHtScore(-1);
            final long initialMatchedTimeStamp = firstObject.getMatchedTimeStamp();
            final String initialMatchedEventId = firstObject.getMatchedEventId();

            secondObject.setTimeStamp(currentTime - 1_000L);
            if (firstObject.getTimeStamp() >= secondObject.getTimeStamp()) { // timeStamp can only increase, so the previous assignment might not work
                secondObject.setTimeStamp(firstObject.getTimeStamp() + 1L);
            }
            secondObject.setHomeScoreTimeStamp(currentTime - 1_000L);
            secondObject.setAwayScoreTimeStamp(currentTime - 1_000L);
            secondObject.setMatchedTimeStamp(currentTime - 1_000L);
            secondObject.setHomeScore(1);
            secondObject.setAwayScore(1);
            secondObject.setHomeHtScore(1);
            secondObject.setAwayHtScore(1);
            secondObject.setHtIgnored(true);

            firstObject.update(secondObject);

            // fillRandom does not initialize the fields of the super Ignorable class, but other methods might
            assertThat(firstObject).as("updated object different").isEqualToIgnoringGivenFields(secondObject, "matchedEventId", "matchedTimeStamp", "ignored", "ignoredExpiration", "resetIgnoredStamp", "setIgnoredStamp");

            assertEquals(initialMatchedTimeStamp, firstObject.getMatchedTimeStamp(), "matchedTimeStamp");
            assertEquals(initialMatchedEventId, firstObject.getMatchedEventId(), "matchedEventId");
            assertTrue(firstObject.getResetIgnoredStamp() >= secondObject.getResetIgnoredStamp(),
                       "resetIgnoredStamp " + firstObject.getResetIgnoredStamp() + " " + secondObject.getResetIgnoredStamp() + " " + firstObject.isIgnored() + " " + secondObject.isIgnored());
            if (secondObject.isIgnored()) {
                assertTrue(firstObject.isIgnored(), "ignored");
            }
            assertTrue(firstObject.getIgnoredExpiration() >= secondObject.getIgnoredExpiration(), "ignoredExpiration");
            assertTrue(firstObject.getSetIgnoredStamp() >= secondObject.getSetIgnoredStamp(), "setIgnoredStamp");
        } catch (IllegalAccessException e) {
            fail("IllegalAccessException");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            fail("InvocationTargetException");
            e.printStackTrace();
        } catch (InstantiationException e) {
            fail("InstantiationException");
            e.printStackTrace();
        }
    }
}
