package info.fmro.betty.entities;

import info.fmro.shared.utility.Generic;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class EventTest {
    @Test
    void update()
            throws NoSuchFieldException {
        try {
            final Event firstObject = new Event("someId"), secondObject = new Event("someId");
            Generic.fillRandom(firstObject);
            Generic.fillRandom(secondObject);

            final String id = firstObject.getId();
            final Field field = Event.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(secondObject, id);
            secondObject.setTimeStamp(firstObject.getTimeStamp() + 1L);
            final long initialTimeFirstSeen = firstObject.getTimeFirstSeen();
            final long initialMatchedTimeStamp = firstObject.getMatchedTimeStamp();

            firstObject.update(secondObject);
            assertThat(firstObject).as("updated object different").isEqualToIgnoringGivenFields(secondObject, "marketCount", "timeFirstSeen", "matchedTimeStamp", "scraperEventIds");

            final int marketCount = secondObject.getMarketCount();
            if (marketCount >= 0) {
                assertEquals(marketCount, firstObject.getMarketCount(), "marketCount");
            }
            final long timeFirstSeen = secondObject.getTimeFirstSeen();
            if (timeFirstSeen > 0 && timeFirstSeen <= initialTimeFirstSeen) { // changes only to earlier seen times
                assertEquals(timeFirstSeen, firstObject.getTimeFirstSeen(), "timeFirstSeen");
            }
            assertEquals(initialMatchedTimeStamp, firstObject.getMatchedTimeStamp(), "matchedTimeStamp");
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
