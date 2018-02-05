package info.fmro.betty.utility;

import info.fmro.shared.utility.LogLevel;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.AlreadyPrintedMap;
import info.fmro.shared.utility.Generic;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormulasTest {

    private static final Logger logger = LoggerFactory.getLogger(FormulasTest.class);

    @Rule
    @SuppressWarnings("PublicField")
    public TestRule watchman = new TestWatcher() {
        @Override
        public void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

    @BeforeClass
    public static void setUpClass() {
        logger.info("{} being run...");
    }

    @Test
    public void testLogOnce() { // due to the inexact nature of system timer, this test could, rarely, fail
        String result = Generic.alreadyPrintedMap.logOnce(false, 500L, logger, LogLevel.ERROR, "test");
        String expectedResult = "test";
        assertEquals("1", expectedResult, result);
        result = Generic.alreadyPrintedMap.logOnce(false, 500L, logger, LogLevel.ERROR, "test");
        assertNull("2", result);
        Generic.threadSleep(1_000L);
        result = Generic.alreadyPrintedMap.logOnce(false, 500L, logger, LogLevel.ERROR, "test");
        assertEquals("3", expectedResult, result);
    }

    @Test
    public void testPrintAliases() {

        assertTrue("1", Formulas.fullAliasesMap.containsKey("paris st g"));
        assertTrue("2", Formulas.fullAliasesMap.containsValue("paris saint germain"));

//        Logger newLogger = LoggerFactory.getLogger(FormulasTest.class);
//        newLogger.info("new");
//        Field field = Generic.class.getField("logger");
//        field.setAccessible(true);
//        Logger genericLogger = (Logger) field.get(null);
//        genericLogger.info("generic");
    }

//    @Test
//    @SuppressWarnings("unchecked")
//    public void testPrintChars() {
//        char c = 97;
//        logger.info("char: {} code: {}", c, (int) c);
//    }
    @Test
    @SuppressWarnings("unchecked")
    public void testGetMaxMultiple() {
        ArrayList<Integer> list = new ArrayList<>(5), expected = new ArrayList<>(5);
        list.add(1);
        list.add(null);
        list.add(2);
        list.add(-2);
        list.add(null);

        Integer result = Formulas.getMaxMultiple(list, -1, Statics.nullComparator, 2);
        assertEquals(result, null);

        list.clear();
        list.add(1);
        list.add(null);
        list.add(2);
        list.add(-2);
        result = Formulas.getMaxMultiple(list, -1, Statics.nullComparator, 2);
        assertEquals((int) result, -1);

        list.clear();
        list.add(1);
        list.add(null);
        list.add(1);
        list.add(2);
        list.add(-2);
        result = Formulas.getMaxMultiple(list, -1, Statics.nullComparator, 2);
        assertEquals((int) result, 1);
    }

    @Test
    public void testParseTeamString() {
        String teamString = "Bucharest";
        String expResult = "bucuresti";
        String result = Formulas.parseTeamString(teamString);
        assertEquals("1", expResult, result);

        teamString = "LA Galaxy";
        expResult = "los angeles galaxy";
        result = Formulas.parseTeamString(teamString);
        assertEquals("2", expResult, result);

        teamString = "D. C. Utd";
        expResult = "washington dc united";
        result = Formulas.parseTeamString(teamString);
        assertEquals("3", expResult, result);

        teamString = "CA Independiente";
        expResult = "ca independiente";
        result = Formulas.parseTeamString(teamString);
        assertEquals("4", expResult, result);

        teamString = "I.Rivadavia";
        expResult = "independiente rivadavia";
        result = Formulas.parseTeamString(teamString);
        assertEquals("5", expResult, result);

        teamString = "Paris St-G (W)";
        expResult = "paris saint germain";
        result = Formulas.parseTeamString(teamString);
        assertEquals("6", expResult, result);

        teamString = "fc vaajakoski";
        expResult = "fcv vaajakoski";
        result = Formulas.parseTeamString(teamString);
        assertEquals("7", expResult, result);

        teamString = "fcv";
        expResult = "fcv v";
        result = Formulas.parseTeamString(teamString);
        assertEquals("8", expResult, result);
    }

    @Test
    public void testMatchTeams() {
        String firstString = "Concordia Chiajna";
        String secondString = "Concordia C";
        double result = Formulas.matchTeams(firstString, secondString);
        assertTrue("1 " + result, result > Statics.highThreshold);

        firstString = "Dinamo Bucharest";
        secondString = "Bucuresti";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue("2 " + result, result > .7);

        firstString = "Los Angeles Galaxy II";
        secondString = "LA Galaxy";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue("3 " + result, result > Statics.highThreshold);

        firstString = "Luch-Energiya";
        secondString = "Luch-E. Vladivostok";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue("4 " + result, result > Statics.highThreshold);

        firstString = "CA Independiente";
        secondString = "I.Rivadavia";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue("5 " + result, result > Statics.threshold);

        firstString = "Su Pham TDTT TP HCM University";
        secondString = "Su Pham HCM Uni";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue("6 " + result, result > Statics.threshold);

        firstString = "Lyon (W)";
        secondString = "Lyon";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue("7 " + result, result > .95);

        firstString = "Paris St-G (W)";
        secondString = "PSG";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue("8 " + result, result > .95);

        firstString = "fc vaajakoski";
        secondString = "fcv";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue("9 " + result, result > .95);
    }
}
