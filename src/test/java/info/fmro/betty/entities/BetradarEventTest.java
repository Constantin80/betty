package info.fmro.betty.entities;

import info.fmro.betty.safebet.BetradarEvent;
import info.fmro.shared.utility.Generic;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class BetradarEventTest {
    @Test
    void update() {
        try {
            final BetradarEvent firstObject = new BetradarEvent(1_220L, 0L), secondObject = new BetradarEvent(1_220L, 0L);
            Generic.fillRandom(firstObject);
            Generic.fillRandom(secondObject);

            firstObject.update(secondObject);
            // some Ignorable fields get set in the firstObject by the update method
            assertThat(firstObject).as("updated object different").isEqualToIgnoringGivenFields(secondObject, "ignoredExpiration", "ignored", "setIgnoredStamp");
            final long currentTime = System.currentTimeMillis();
            if (firstObject.isIgnored()) {
                assertTrue(Math.abs(firstObject.getIgnoredExpiration() - currentTime) < Generic.DAY_LENGTH_MILLISECONDS * 3_650L, "ignoredExpiration ignored=true");
                assertTrue(Math.abs(firstObject.getSetIgnoredStamp() - currentTime) < 10_000L, "setIgnoredStamp ignored=true");
            } else {
                assertEquals(0L, firstObject.getIgnoredExpiration(), "ignoredExpiration ignored=false");
                assertEquals(0L, firstObject.getSetIgnoredStamp(), "setIgnoredStamp ignored=false");
            }
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
