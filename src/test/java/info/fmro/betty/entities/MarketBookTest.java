package info.fmro.betty.entities;

import info.fmro.shared.utility.Generic;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class MarketBookTest {
    @Test
    void update()
            throws NoSuchFieldException {
        try {
            final MarketBook firstObject = new MarketBook("someId"), secondObject = new MarketBook("someId");
            Generic.fillRandom(firstObject);
            Generic.fillRandom(secondObject);

            final String marketId = firstObject.getMarketId();
            final Field field = MarketBook.class.getDeclaredField("marketId");
            field.setAccessible(true);
            field.set(secondObject, marketId);
            secondObject.setTimeStamp(firstObject.getTimeStamp() + 1L);

            firstObject.update(secondObject);
            assertThat(firstObject).as("updated object different").isEqualToIgnoringGivenFields(secondObject);
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
