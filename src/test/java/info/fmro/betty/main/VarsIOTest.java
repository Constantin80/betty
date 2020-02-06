package info.fmro.betty.main;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VarsIOTest
        extends ApiDefault {
    @Test
    void getUnescapedSplits_String_char() {
        String initialString = "ante\"ab'c\"inter\"def\"post";
        char marker = '"';
        List<String> expResult = new ArrayList<>(Arrays.asList("ante", "ab'c", "inter", "def", "post"));
        List<String> result = VarsIO.getUnescapedSplits(initialString, marker);
        assertEquals(expResult, result, "1");

        initialString = "\"\",\"\"";
        marker = '"';
        expResult = new ArrayList<>(Collections.singletonList(","));
        result = VarsIO.getUnescapedSplits(initialString, marker);
        assertEquals(expResult, result, "2");
    }
}
