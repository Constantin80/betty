package info.fmro.betty.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitOrderTest {

    private static final Logger logger = LoggerFactory.getLogger(LimitOrderTest.class);

    @Rule
    @SuppressWarnings("PublicField")
    public TestRule watchman = new TestWatcher() {
        @Override
        public void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

    @Test
    public void testGetSize() {
        LimitOrder instance = new LimitOrder();
        double expResult;
        double result;
        assertTrue("1", null == instance.getSize());

        expResult = 123456.12d;
        instance.setSize(123456.1234556d);
        result = instance.getSize();
        assertTrue("1", expResult == result);
    }

    @Test
    public void testSetSize() {
        LimitOrder instance = new LimitOrder();

        double size = 3.62378947368421d;
        instance.setSize(size);
        String result = instance.getSizeString();
        String expResult = "3.62";
        assertEquals("1", expResult, result);

        size = 3.62978947368421d;
        instance.setSize(size);
        result = instance.getSizeString();
        expResult = "3.62";
        assertEquals("2", expResult, result);

        size = .62978947368421d;
        instance.setSize(size);
        result = instance.getSizeString();
        expResult = "0.62";
        assertEquals("3", expResult, result);

        size = 1000d;
        instance.setSize(size);
        result = instance.getSizeString();
        expResult = "1000.0";
        assertEquals("4", expResult, result);

        size = 0d;
        instance.setSize(size);
        result = instance.getSizeString();
        expResult = "0.0";
        assertEquals("5", expResult, result);
    }

    // @Test
    // public void testGetPrice() {
    //     LimitOrder instance = new LimitOrder();
    //     Double expResult = null;
    //     Double result = instance.getPrice();
    //     assertEquals(expResult, result);
    // }
    // @Test
    // public void testSetPrice() {
    //     Double price = null;
    //     LimitOrder instance = new LimitOrder();
    //     instance.setPrice(price);
    // }
    // @Test
    // public void testGetPersistenceType() {
    //     LimitOrder instance = new LimitOrder();
    //     PersistenceType expResult = null;
    //     PersistenceType result = instance.getPersistenceType();
    //     assertEquals(expResult, result);
    // }
    // @Test
    // public void testSetPersistenceType() {
    //     PersistenceType persistenceType = null;
    //     LimitOrder instance = new LimitOrder();
    //     instance.setPersistenceType(persistenceType);
    // }
}
