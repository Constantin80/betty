package info.fmro.betty.objects;

import info.fmro.betty.enums.MatchStatus;
import info.fmro.betty.main.ApiDefault;
import info.fmro.betty.safebet.CoralEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoralEventTest
        extends ApiDefault {
    @Test
    void updateSeconds() {
        final CoralEvent instance = new CoralEvent(0L, 1L);
        final CoralEvent secondInstance = new CoralEvent(0L, 1L);
        int result = instance.update(secondInstance);
        int expResult = 0;
        assertEquals(expResult, result, "first");

        secondInstance.setSeconds(3);
        result = instance.update(secondInstance);
        expResult = 1;
        assertEquals(expResult, result, "second");
    }

    @Test
    void setMatchStatus() {
        final CoralEvent instance = new CoralEvent(0L, 1L);
        instance.setHomeScore(1);
        instance.setHomeTeam("a");
        instance.setAwayScore(2);
        instance.setAwayTeam("b");
        instance.setMatchStatus(MatchStatus.HALF_TIME);
        final long result = instance.errors();
        final long expResult = 0L;
        assertEquals(expResult, result, "first");
//
//        secondInstance.setSeconds(3);
//        result = instance.update(secondInstance);
//        expResult = 1;
//        assertEquals(expResult, result, "second");
    }
}
