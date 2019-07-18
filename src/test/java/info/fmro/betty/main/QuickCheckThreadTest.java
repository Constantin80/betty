package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.permanent.QuickCheckThread;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuickCheckThreadTest
        extends ApiDefault {
    @Test
    void popNRunsMarketBook() {
        final long currentTime = System.currentTimeMillis();
        int expResult = 0;
        int result = QuickCheckThread.popNRunsMarketBook(currentTime);
        assertEquals(expResult, result, "1");

        final int size = 12;
        final int marketsPerOperation = Statics.N_ALL;
        //noinspection NumericCastThatLosesPrecision,ConstantMathCall
        QuickCheckThread.nThreadsMarketBook.put(System.currentTimeMillis(), (int) Math.ceil((double) size / marketsPerOperation));
        expResult = 2;
        result = QuickCheckThread.popNRunsMarketBook(currentTime);
        assertEquals(expResult, result, "2");
    }
}
