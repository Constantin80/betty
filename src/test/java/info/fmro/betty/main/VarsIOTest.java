package info.fmro.betty.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VarsIOTest {

    private static final Logger logger = LoggerFactory.getLogger(VarsIOTest.class);

    @Rule
    @SuppressWarnings("PublicField")
    public TestRule watchman = new TestWatcher() {
        @Override
        public void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

    @Test
    public void testGetUnescapedSplits_String_char() {
        String string = "ante\"ab\'c\"inter\"def\"post";
        char marker = '"';
        List<String> expResult = new ArrayList<>(Arrays.asList(new String[]{"ante", "ab'c", "inter", "def", "post"}));
        List<String> result = VarsIO.getUnescapedSplits(string, marker);
        assertEquals(expResult, result);

        string = "\"\",\"\"";
        marker = '"';
        expResult = new ArrayList<>(Arrays.asList(new String[]{","}));
        result = VarsIO.getUnescapedSplits(string, marker);
        assertEquals(expResult, result);
    }
}
