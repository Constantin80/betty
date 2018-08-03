package info.fmro.betty.utility;

import info.fmro.betty.main.ApiDefault;
import info.fmro.betty.objects.Statics;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormulasTest
        extends ApiDefault {

    @Test
    void printAliases() {
        assertTrue(Formulas.fullAliasesMap.containsKey("paris st g"), "1");
        assertTrue(Formulas.fullAliasesMap.containsValue("paris saint germain"), "2");

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
    void getMaxMultiple() {
        ArrayList<Integer> list = new ArrayList<>(5), expected = new ArrayList<>(5);
        list.add(1);
        list.add(null);
        list.add(2);
        list.add(-2);
        list.add(null);

        Integer result = Formulas.getMaxMultiple(list, -1, Statics.nullComparator, 2);
        assertEquals(result, null, "1");

        list.clear();
        list.add(1);
        list.add(null);
        list.add(2);
        list.add(-2);
        result = Formulas.getMaxMultiple(list, -1, Statics.nullComparator, 2);
        assertEquals((int) result, -1, "2");

        list.clear();
        list.add(1);
        list.add(null);
        list.add(1);
        list.add(2);
        list.add(-2);
        result = Formulas.getMaxMultiple(list, -1, Statics.nullComparator, 2);
        assertEquals((int) result, 1, "3");
    }

    @Test
    void parseTeamString() {
        String teamString = "Bucharest";
        String expResult = "bucuresti";
        String result = Formulas.parseTeamString(teamString);
        assertEquals(expResult, result, "1");

        teamString = "LA Galaxy";
        expResult = "los angeles galaxy";
        result = Formulas.parseTeamString(teamString);
        assertEquals(expResult, result, "2");

        teamString = "D. C. Utd";
        expResult = "washington dc united";
        result = Formulas.parseTeamString(teamString);
        assertEquals(expResult, result, "3");

        teamString = "CA Independiente";
        expResult = "ca independiente";
        result = Formulas.parseTeamString(teamString);
        assertEquals(expResult, result, "4");

        teamString = "I.Rivadavia";
        expResult = "independiente rivadavia";
        result = Formulas.parseTeamString(teamString);
        assertEquals(expResult, result, "5");

        teamString = "Paris St-G (W)";
        expResult = "paris saint germain";
        result = Formulas.parseTeamString(teamString);
        assertEquals(expResult, result, "6");

        teamString = "fc vaajakoski";
        expResult = "fcv vaajakoski";
        result = Formulas.parseTeamString(teamString);
        assertEquals(expResult, result, "7");

        teamString = "fcv";
        expResult = "fcv v";
        result = Formulas.parseTeamString(teamString);
        assertEquals(expResult, result, "8");

        teamString = "swindon s";
        expResult = "swindon supermarine";
        result = Formulas.parseTeamString(teamString);
        assertEquals(expResult, result, "9");
    }

    @Test
    void matchTeams() {
        String firstString = "Concordia Chiajna";
        String secondString = "Concordia C";
        double result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result > Statics.highThreshold, "1 " + result);

        firstString = "Dinamo Bucharest";
        secondString = "Bucuresti";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result > .7, "2 " + result);

        firstString = "Los Angeles Galaxy II";
        secondString = "LA Galaxy";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result > Statics.highThreshold, "3 " + result);

        firstString = "Luch-Energiya";
        secondString = "Luch-E. Vladivostok";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result > Statics.highThreshold, "4 " + result);

        firstString = "CA Independiente";
        secondString = "I.Rivadavia";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result > Statics.threshold, "5 " + result);

        firstString = "Su Pham TDTT TP HCM University";
        secondString = "Su Pham HCM Uni";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result > Statics.threshold, "6 " + result);

        firstString = "Lyon (W)";
        secondString = "Lyon";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result > .95, "7 " + result);

        firstString = "Paris St-G (W)";
        secondString = "PSG";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result > .95, "8 " + result);

        firstString = "fc vaajakoski";
        secondString = "fcv";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result > .95, "9 " + result);

        firstString = "Swindon S";
        secondString = "Swindon Supermarine";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result == .99, "10 " + result);

        firstString = "swindon supermarine";
        secondString = "Swindon Supermarine";
        result = Formulas.matchTeams(firstString, secondString);
        assertTrue(result == 1, "11 " + result);
    }
}
