package info.fmro.betty.entities;

import info.fmro.betty.main.ApiDefault;
import info.fmro.shared.entities.LimitOrder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class LimitOrderTest
        extends ApiDefault {
    @Test
    void getSize() {
        final LimitOrder instance = new LimitOrder();
        final double expResult;
        final double result;
        assertNull(instance.getSize(), "1");

        expResult = 123456.12d;
        instance.setSize(123456.1234556d);
        result = instance.getSize();
        assertEquals(expResult, result, "1");
    }

    @Test
    void setSize() {
        final LimitOrder instance = new LimitOrder();

        double size = 3.62378947368421d;
        instance.setSize(size);
        String result = instance.getSizeString();
        String expResult = "3.62";
        assertEquals(expResult, result, "1");

        size = 3.62978947368421d;
        instance.setSize(size);
        result = instance.getSizeString();
        expResult = "3.62";
        assertEquals(expResult, result, "2");

        size = .62978947368421d;
        instance.setSize(size);
        result = instance.getSizeString();
        expResult = "0.62";
        assertEquals(expResult, result, "3");

        size = 1000d;
        instance.setSize(size);
        result = instance.getSizeString();
        expResult = "1000.0";
        assertEquals(expResult, result, "4");

        size = 0d;
        instance.setSize(size);
        result = instance.getSizeString();
        expResult = "0.0";
        assertEquals(expResult, result, "5");
    }
}
