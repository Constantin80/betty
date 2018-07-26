package info.fmro.betty.utility;

import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.EventType;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.entities.MarketDescription;
import info.fmro.betty.entities.PlaceInstruction;
import info.fmro.betty.main.ScraperThread;
import info.fmro.betty.main.VarsIO;
import info.fmro.betty.objects.BetradarEvent;
import info.fmro.betty.objects.CoralEvent;
import info.fmro.betty.objects.OrderPrice;
import info.fmro.betty.objects.ScraperEvent;
import info.fmro.betty.objects.StampedDouble;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.objects.TwoOrderedStrings;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.Ignorable;
import info.fmro.shared.utility.LogLevel;
import info.fmro.shared.utility.SynchronizedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class Formulas {

    private static final Logger logger = LoggerFactory.getLogger(Formulas.class);
    public static final Map<String, String> charactersMap = Collections.synchronizedMap(new LinkedHashMap<String, String>(128, 0.75f));
    public static final Map<String, String> aliasesMap = Collections.synchronizedMap(new LinkedHashMap<String, String>(64, 0.75f));
    public static final Map<String, String> fullAliasesMap = Collections.synchronizedMap(new LinkedHashMap<String, String>(4, 0.75f));
    public static final SynchronizedMap<TwoOrderedStrings, StampedDouble> matchesCache = new SynchronizedMap<>();

    private Formulas() {
    }

    static {
//        charactersMap.put("Ã¡", "a");
//        charactersMap.put("Ã¼", "u");
        charactersMap.put("µ", "u");
        charactersMap.put("à", "a");
        charactersMap.put("â", "a");
        charactersMap.put("ã", "a");
        charactersMap.put("ä", "a");
        charactersMap.put("å", "a");
        charactersMap.put("æ", "ae");
        charactersMap.put("ç", "c");
        charactersMap.put("è", "e");
        charactersMap.put("é", "e");
        charactersMap.put("ê", "e");
        charactersMap.put("ë", "e");
        charactersMap.put("ì", "i");
        charactersMap.put("í", "i");
        charactersMap.put("î", "i");
        charactersMap.put("ï", "i");
        charactersMap.put("ñ", "n");
        charactersMap.put("ò", "o");
        charactersMap.put("ó", "o");
        charactersMap.put("ô", "o");
        charactersMap.put("õ", "o");
        charactersMap.put("ö", "o");
        charactersMap.put("ø", "o");
        charactersMap.put("ù", "u");
        charactersMap.put("ú", "u");
        charactersMap.put("û", "u");
        charactersMap.put("ü", "u");
        charactersMap.put("ý", "y");
        charactersMap.put("ß", "b");
        charactersMap.put("á", "a");
        charactersMap.put("ā", "a");
        charactersMap.put("ă", "a");
        charactersMap.put("ą", "a");
        charactersMap.put("ć", "c");
        charactersMap.put("č", "c");
        charactersMap.put("đ", "d");
        charactersMap.put("ď", "d");
        charactersMap.put("ē", "e");
        charactersMap.put("ĕ", "e");
        charactersMap.put("ė", "e");
        charactersMap.put("ę", "e");
        charactersMap.put("ě", "e");
        charactersMap.put("ģ", "g");
        charactersMap.put("ī", "i");
        charactersMap.put("į", "i");
        charactersMap.put(Generic.createStringFromCodes(305), "i");
        charactersMap.put("ķ", "k");
        charactersMap.put("ĺ", "l");
        charactersMap.put("ļ", "l");
        charactersMap.put("ľ", "l");
        charactersMap.put("ł", "l");
        charactersMap.put("ń", "n");
        charactersMap.put("ņ", "n");
        charactersMap.put("ň", "n");
        charactersMap.put("ō", "o");
        charactersMap.put("ő", "o");
        charactersMap.put("œ", "oe");
        charactersMap.put("ŕ", "r");
        charactersMap.put("ŗ", "r");
        charactersMap.put("ř", "r");
        charactersMap.put("ş", "s");
        charactersMap.put("ś", "s");
        charactersMap.put("š", "s");
        charactersMap.put("ţ", "t");
        charactersMap.put("ť", "t");
        charactersMap.put("ű", "u");
        charactersMap.put("ų", "u");
        charactersMap.put("ź", "z");
        charactersMap.put("ż", "z");
        charactersMap.put("ž", "z");

        charactersMap.put("-", " ");
        charactersMap.put("/", " ");
        charactersMap.put("'", " ");
        charactersMap.put("`", " ");
        charactersMap.put("&", "and");
        charactersMap.put(".", " ");

        final int mapCapacity = Generic.getCollectionCapacity(charactersMap.size(), 0.75f);
        final LinkedHashMap<String, String> tempMap = new LinkedHashMap<>(mapCapacity, 0.75f);
        synchronized (charactersMap) { // apply toLowerCase().trim() to all keys & values
            final Set<String> keys = charactersMap.keySet();
            for (String key : keys) {
                final String value = charactersMap.get(key), modifiedKey = key.toLowerCase(Locale.ENGLISH), modifiedValue = value.toLowerCase(Locale.ENGLISH);
                if (key != modifiedKey) {
                    logger.error("key gets modified in charactersMap static parsing: {} - {}", key, modifiedKey);
                }
                if (value != modifiedValue) {
                    logger.error("value gets modified in charactersMap static parsing: {} - {}", value, modifiedValue);
                }

                tempMap.put(modifiedKey, modifiedValue);
            }

            charactersMap.clear();
            charactersMap.putAll(tempMap);
        } // end synchronized
    }

    static {
        VarsIO.checkAliasesFile(Statics.ALIASES_FILE_NAME, Statics.aliasesTimeStamp, Formulas.aliasesMap);
        VarsIO.checkAliasesFile(Statics.FULL_ALIASES_FILE_NAME, Statics.fullAliasesTimeStamp, Formulas.fullAliasesMap);
    }

    public static String removeStrangeChars(String teamString) {
        String modifiedString = null;
        if (teamString != null) {
            modifiedString = teamString.toLowerCase().trim();
            synchronized (charactersMap) {
                final Set<String> keys = charactersMap.keySet();
                for (String key : keys) {
                    while (modifiedString.contains(key)) {
                        modifiedString = Generic.quotedReplaceAll(modifiedString, key, charactersMap.get(key));
                    }
                }
            } // end synchronized
        } else {
            logger.error("null teamString in removeStrangeChars()");
        }

        return modifiedString;
    }

    public static boolean containsStrangeChars(String teamString) {
        boolean result = false;
        if (teamString != null) {
            synchronized (charactersMap) {
                final Set<String> keys = charactersMap.keySet();
                for (String key : keys) {
                    if (teamString.contains(key)) {
                        result = true;
                        break;
                    }
                } // end for
            } // end synchronized
        } else {
            logger.error("null teamString in containsStrangeChars()");
        }

        return result;
    }

    public static String parseTeamString(String teamString) {
        String modifiedString = null;
        if (teamString != null) {
//            modifiedString = teamString.toLowerCase().trim(); // before initial processing
//
//            synchronized (charactersMap) {
//                Set<String> keys = charactersMap.keySet();
//                for (String key : keys) {
//                    while (modifiedString.contains(key)) {
//                        modifiedString = Generic.quotedReplaceAll(modifiedString, key, charactersMap.get(key));
//                    }
//                }
//            } // end synchronized

            modifiedString = removeStrangeChars(teamString);

            if (modifiedString.endsWith(" fc")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" fc"));
            }
            if (modifiedString.endsWith(" football club")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" football club"));
            }
            if (modifiedString.endsWith(" futebol clube")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" futebol clube"));
            }
//            if (modifiedString.endsWith(" sc")) {
//                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" sc"));
//            }
            if (modifiedString.endsWith(" 2")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" 2")) + " ii";
            }
            if (modifiedString.endsWith(" b")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" b")) + " ii";
            }

            if (modifiedString.startsWith("fc ")) {
                modifiedString = modifiedString.substring("fc ".length());
            }
            if (modifiedString.startsWith("football club ")) {
                modifiedString = modifiedString.substring("football club ".length());
            }
            if (modifiedString.startsWith("futebol clube ")) {
                modifiedString = modifiedString.substring("futebol clube ".length());
            }
//            if (modifiedString.startsWith("fk ")) {
//                modifiedString = modifiedString.substring("fk ".length());
//            }
//            if (modifiedString.startsWith("ac ")) {
//                modifiedString = modifiedString.substring("ac ".length());
//            }
//            if (modifiedString.startsWith("as ")) {
//                modifiedString = modifiedString.substring("as ".length());
//            }
//            if (modifiedString.startsWith("ks ")) { // Klub Sportowy
//                modifiedString = modifiedString.substring("ks ".length());
//            }
//            if (modifiedString.startsWith("nk ")) { // Nogometni klub
//                modifiedString = modifiedString.substring("nk ".length());
//            }
//            if (modifiedString.startsWith("al ")) {
//                modifiedString = modifiedString.substring("al ".length());
//            }
            if (modifiedString.startsWith("u ")) {
                modifiedString = "universi " + modifiedString.substring("u ".length());
            }
            if (modifiedString.startsWith("uni ")) {
                modifiedString = "universi " + modifiedString.substring("uni ".length());
            }
            if (modifiedString.startsWith("univ ")) {
                modifiedString = "universi " + modifiedString.substring("univ ".length());
            }
            if (modifiedString.startsWith("dep ")) {
                modifiedString = "deportivo " + modifiedString.substring("dep ".length());
            }

            while (modifiedString.contains("(") && modifiedString.contains(")") && modifiedString.indexOf('(') < modifiedString.indexOf(')')) {
                modifiedString = modifiedString.substring(0, modifiedString.indexOf('(')) + modifiedString.substring(modifiedString.indexOf(')') + ")".length());
            }

//            if (modifiedString.endsWith(".")) {
//                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf('.'));
//            }
//
//            if (modifiedString.contains(". ")) {
//                modifiedString = Generic.quotedReplaceAll(modifiedString, ". ", " ");
//            }
//            if (modifiedString.contains(".")) {
//                modifiedString = Generic.quotedReplaceAll(modifiedString, ".", " ");
//            }
            while (modifiedString.contains(" fc ")) {
                modifiedString = Generic.quotedReplaceAll(modifiedString, " fc ", " ");
            }
            while (modifiedString.contains(" football club ")) {
                modifiedString = Generic.quotedReplaceAll(modifiedString, " football club ", " ");
            }
            while (modifiedString.contains(" futebol clube ")) {
                modifiedString = Generic.quotedReplaceAll(modifiedString, " futebol clube ", " ");
            }

            while (modifiedString.contains("  ")) {
                modifiedString = Generic.quotedReplaceAll(modifiedString, "  ", " ");
            }

            modifiedString = modifiedString.toLowerCase().trim(); // before full aliases check; trim is important
            synchronized (fullAliasesMap) {
                if (fullAliasesMap.containsKey(modifiedString)) {
                    modifiedString = fullAliasesMap.get(modifiedString);
                }
            } // end synchronized
            synchronized (aliasesMap) {
                Set<String> keys = aliasesMap.keySet();
                for (String key : keys) {
                    if (modifiedString.contains(key)) {
                        modifiedString = Generic.quotedReplaceAll(modifiedString, key, aliasesMap.get(key));
                    }
                }
            } // end synchronized
            modifiedString = modifiedString.toLowerCase().trim(); // after all processing, before final checks

//            if (!Generic.isPureAscii(modifiedString)) {
//                logger.error("non standard character found in: {} original: {}", modifiedString, teamString);
//            }
            final int length = modifiedString.length();
            for (int i = 0; i < length; i++) {
                final char c = modifiedString.charAt(i);
                if ((c < 48 || c > 57) && (c < 97 || c > 122) && c != 32) {
                    Generic.alreadyPrintedMap.logOnce(Generic.HOUR_LENGTH_MILLISECONDS, Formulas.logger, LogLevel.ERROR, "forbidden char:{} code:{} in parseTeamString: {}", c, (int) c,
                                                      teamString);
                }
            } // end for

            // logger.info("parseTeamString: {} to: {}", teamString, modifiedString);
            if (Statics.debugLevel.check(3, 101)) {
                logger.info("parseTeamString: {} to: {}", teamString, modifiedString);
            }
        } else {
            logger.error("null teamString in parseTeamString()");
        }

        return modifiedString;
    }

    public static String getCoreString(String teamString) {
        String modifiedString = null;
        if (teamString != null) {
            modifiedString = teamString.toLowerCase().trim(); // before initial processing

//            synchronized (charactersMap) {
//                Set<String> keys = charactersMap.keySet();
//                for (String key : keys) {
//                    while (modifiedString.contains(key)) {
//                        modifiedString = Generic.quotedReplaceAll(modifiedString, key, charactersMap.get(key));
//                    }
//                }
//            } // end synchronized
//            if (modifiedString.endsWith(" fc")) {
//                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" fc"));
//            }
            if (modifiedString.endsWith(" cf")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" cf"));
            }
            if (modifiedString.endsWith(" sc")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" sc"));
            }
            if (modifiedString.endsWith(" rs")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" rs"));
            }
            if (modifiedString.endsWith(" ii")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" ii"));
            }
            if (modifiedString.endsWith(" u19")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" u19"));
            }
            if (modifiedString.endsWith(" u20")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" u20"));
            }
            if (modifiedString.endsWith(" u21")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" u21"));
            }
            if (modifiedString.endsWith(" women")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" women"));
            }
            if (modifiedString.endsWith(" youth")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" youth"));
            }
            if (modifiedString.endsWith(" xi")) {
                modifiedString = modifiedString.substring(0, modifiedString.lastIndexOf(" xi"));
            }

//            if (modifiedString.startsWith("fc ")) {
//                modifiedString = modifiedString.substring("fc ".length());
//            }
            if (modifiedString.startsWith("fk ")) {
                modifiedString = modifiedString.substring("fk ".length());
            }
            if (modifiedString.startsWith("ac ")) {
                modifiedString = modifiedString.substring("ac ".length());
            }
            if (modifiedString.startsWith("as ")) {
                modifiedString = modifiedString.substring("as ".length());
            }
            if (modifiedString.startsWith("ks ")) { // Klub Sportowy
                modifiedString = modifiedString.substring("ks ".length());
            }
            if (modifiedString.startsWith("nk ")) { // Nogometni klub
                modifiedString = modifiedString.substring("nk ".length());
            }
            if (modifiedString.startsWith("ca ")) {
                modifiedString = modifiedString.substring("ca ".length());
            }
            if (modifiedString.startsWith("cd ")) {
                modifiedString = modifiedString.substring("cd ".length());
            }
            if (modifiedString.startsWith("cs ")) {
                modifiedString = modifiedString.substring("cs ".length());
            }
            if (modifiedString.startsWith("sc ")) {
                modifiedString = modifiedString.substring("sc ".length());
            }
            if (modifiedString.startsWith("ec ")) {
                modifiedString = modifiedString.substring("ec ".length());
            }
            if (modifiedString.startsWith("cf ")) {
                modifiedString = modifiedString.substring("cf ".length());
            }
//            if (modifiedString.startsWith("al ")) {
//                modifiedString = modifiedString.substring("al ".length());
//            }
//            if (modifiedString.startsWith("u ")) {
//                modifiedString = modifiedString.substring("u ".length());
//            }
//            if (modifiedString.startsWith("uni ")) {
//                modifiedString = modifiedString.substring("uni ".length());
//            }
//            if (modifiedString.startsWith("univ ")) {
//                modifiedString = modifiedString.substring("univ ".length());
//            }
            if (modifiedString.startsWith("universi ")) {
                modifiedString = modifiedString.substring("universi ".length());
            }
            if (modifiedString.startsWith("deportivo ")) {
                modifiedString = modifiedString.substring("deportivo ".length());
            }

//            while (modifiedString.contains("(") && modifiedString.contains(")") && modifiedString.indexOf('(') < modifiedString.indexOf(')')) {
//                modifiedString = modifiedString.substring(0, modifiedString.indexOf('(')) + modifiedString.substring(modifiedString.indexOf(')') + ")".length());
//            }
//            while (modifiedString.contains(" fc ")) {
//                modifiedString = Generic.quotedReplaceAll(modifiedString, " fc ", " ");
//            }
            while (modifiedString.contains("  ")) {
                modifiedString = Generic.quotedReplaceAll(modifiedString, "  ", " ");
            }

            modifiedString = modifiedString.toLowerCase().trim(); // before full aliases check; trim is important
            synchronized (fullAliasesMap) {
                if (fullAliasesMap.containsKey(modifiedString)) {
                    modifiedString = fullAliasesMap.get(modifiedString);
                }
            } // end synchronized
            synchronized (aliasesMap) {
                Set<String> keys = aliasesMap.keySet();
                for (String key : keys) {
                    if (modifiedString.contains(key)) {
                        modifiedString = Generic.quotedReplaceAll(modifiedString, key, aliasesMap.get(key));
                    }
                }
            } // end synchronized
            modifiedString = modifiedString.toLowerCase().trim(); // after all processing, before final checks

//            if (!Generic.isPureAscii(modifiedString)) {
//                logger.error("non standard character found in: {} original: {}", modifiedString, teamString);
//            }
//            final int length = modifiedString.length();
//            for (int i = 0; i < length; i++) {
//                final char c = modifiedString.charAt(i);
//                if ((c < 48 || c > 57) && (c < 97 || c > 122) && c != 32) {
//                    Formulas.logOnce(Generic.HOUR_LENGTH_MILLISECONDS, logger, LogLevel.ERROR, "forbidden char:{} code:{} in parseTeamString: {}", c, (int) c, teamString);
//                }
//            } // end for
            // logger.info("parseTeamString: {} to: {}", teamString, modifiedString);
            if (Statics.debugLevel.check(3, 197)) {
                logger.info("parseTeamString: {} to: {}", teamString, modifiedString);
            }
        } else {
            logger.error("null teamString in parseTeamString()");
        }

        return modifiedString;
    }

    public static double matchTeams(String firstString, String secondString) {
        double result;
        final TwoOrderedStrings twoOrderedStrings = new TwoOrderedStrings(firstString, secondString);
        final StampedDouble cachedResult = Formulas.matchesCache.get(twoOrderedStrings);

        if (cachedResult == null) {
            if (firstString == null || secondString == null) {
                result = 0;
            } else {
                String localFirst = firstString.trim().toLowerCase(), localSecond = secondString.trim().toLowerCase();

                if (localFirst.isEmpty() || localSecond.isEmpty()) {
                    result = 0;
                } else if (localFirst.equals(localSecond)) {
                    result = 1;
                } else if ((localSecond.length() > 3 && localFirst.contains(localSecond)) || (localFirst.length() > 3 && localSecond.contains(localFirst))) {
                    result = .98;
                } else {
                    localFirst = parseTeamString(localFirst);
                    localSecond = parseTeamString(localSecond);

                    if (localFirst.equals(localSecond)) {
                        result = .99;
                    } else if ((localSecond.length() > 3 && localFirst.contains(localSecond)) || (localFirst.length() > 3 && localSecond.contains(localFirst))) {
                        result = .97;
                    } else {
                        final double tempResult = Generic.stringMatchChance(localFirst, localSecond);
                        if (tempResult > Statics.highThreshold) {
                            result = tempResult;
                        } else {
                            localFirst = getCoreString(localFirst);
                            localSecond = getCoreString(localSecond);

                            if (localFirst.equals(localSecond)) {
                                result = .96;
                            } else if ((localSecond.length() > 3 && localFirst.contains(localSecond)) || (localFirst.length() > 3 && localSecond.contains(localFirst))) {
                                result = .95;
                            } else {
                                result = Generic.stringMatchChance(localFirst, localSecond);
                            }
                        } // end else
                    } // end else
                } // end else
            } // end else
            Formulas.matchesCache.put(twoOrderedStrings, new StampedDouble(result), true); // StampedDouble can be replaced, no reason to build update method
        } else {
            result = cachedResult.getValue();
        }
        return result;
    }

    public static boolean shouldAddInstruction(OrderPrice orderPrice, double limitOrderSize) {
        final boolean shouldAddInstruction;
        synchronized (Statics.executingOrdersMap) { // shouldAddInstruction check
            if (Statics.executingOrdersMap.containsKey(orderPrice)) {
                final double existingSize = Statics.executingOrdersMap.get(orderPrice);

                if (limitOrderSize - existingSize < .01) { // size - existingSize is the right condition
                    logger.info("order won't be executed again: {} {}", existingSize, limitOrderSize);
                    shouldAddInstruction = false;
                } else {
                    shouldAddInstruction = true;
                }
            } else {
                shouldAddInstruction = true;
            }
        } // end synchronized block
        return shouldAddInstruction;
    }

    public static void removeInstruction(OrderPrice orderPrice, double orderSize) {
        synchronized (Statics.executingOrdersMap) {
            final double existingSize;
            if (Statics.executingOrdersMap.containsKey(orderPrice)) {
                existingSize = Statics.executingOrdersMap.get(orderPrice);
            } else {
                logger.error("STRANGE order not found in Statics.executingOrdersMap for: {} in {}", Generic.objectToString(orderPrice),
                             Generic.objectToString(Statics.executingOrdersMap));
                existingSize = 0;
            }

            final double remainingSize = existingSize - orderSize;
            if (remainingSize < .01) {
                Statics.executingOrdersMap.remove(orderPrice);
                logger.info("removed orderPrice from Statics.executingOrdersMap");
            } else {
                Statics.executingOrdersMap.put(orderPrice, remainingSize);
                logger.warn("orderPrice diminished but not removed from Statics.executingOrdersMap: {} {} {}", orderSize, remainingSize, Generic.objectToString(orderPrice));
            }

            if (Statics.executingOrdersMap.size() > 0) {
                logger.warn("orders still exist in Statics.executingOrdersMap: {} {}", Statics.executingOrdersMap.size(), Generic.objectToString(Statics.executingOrdersMap));
            } else { // map empty
            }
        } // end synchronized block
    }

    public static void addInstruction(OrderPrice orderPrice, double orderSize, String marketId, PlaceInstruction placeInstruction) {
        synchronized (Statics.executingOrdersMap) { // shouldAddInstruction check
            if (Statics.executingOrdersMap.containsKey(orderPrice)) {
                final double existingSize = Statics.executingOrdersMap.get(orderPrice);
                final double replacingSize = existingSize + orderSize;
                Statics.executingOrdersMap.put(orderPrice, replacingSize);
                logger.info("executingOrdersMap replacing existingSize {} with {} for: {} {}", existingSize, replacingSize, marketId, Generic.objectToString(placeInstruction));
            } else {
                Statics.executingOrdersMap.put(orderPrice, orderSize);
                logger.info("will add placeInstruction: {} {}", marketId, Generic.objectToString(placeInstruction));
            }
        } // end synchronized block    
    }

    public static long getLastGetScraperEvents(AtomicLong lastGetScraperEvents) {
//        synchronized (lastGetScraperEvents) {
        return lastGetScraperEvents.get();
//        }
    }

    public static void setLastGetScraperEvents(AtomicLong lastGetScraperEvents, long newValue) {
//        synchronized (lastGetScraperEvents) {
        lastGetScraperEvents.set(newValue);
//        }
    }

    public static long addAndGetLastGetScraperEvents(AtomicLong lastGetScraperEvents, long addedValue) {
//        synchronized (lastGetScraperEvents) {
        return lastGetScraperEvents.addAndGet(addedValue);
//        }
    }

    public static void lastGetScraperEventsStamp(AtomicLong lastGetScraperEvents) {
//        synchronized (lastGetScraperEvents) {
        lastGetScraperEvents.set(System.currentTimeMillis());
//        }
    }

    public static void lastGetScraperEventsStamp(AtomicLong lastGetScraperEvents, long timeStamp) {
        long currentTime = System.currentTimeMillis();
        synchronized (lastGetScraperEvents) {
            if (currentTime - lastGetScraperEvents.get() >= timeStamp) {
                lastGetScraperEvents.set(currentTime + timeStamp);
            } else {
                lastGetScraperEvents.addAndGet(timeStamp);
            }
        } // end synchronized
    }

    @SuppressWarnings("unchecked")
    public static <T extends Ignorable> SynchronizedMap<?, T> getIgnorableMap(Class<T> clazz) {
        SynchronizedMap<?, T> returnValue;
        if (clazz == null) {
            logger.error("null clazz value in getIgnorableMap");
            returnValue = null;
        } else if (clazz.equals(MarketCatalogue.class)) {
            returnValue = (SynchronizedMap<?, T>) Statics.marketCataloguesMap;
        } else if (clazz.equals(Event.class)) {
            returnValue = (SynchronizedMap<?, T>) Statics.eventsMap;
        } else if (ScraperEvent.class.isAssignableFrom(clazz)) {
            returnValue = (SynchronizedMap<?, T>) getScraperEventsMap((Class<? extends ScraperEvent>) clazz);
        } else {
            logger.error("unsupported clazz {} in getIgnorableMap", clazz);
            returnValue = null;
        }
        return returnValue;
    }

    public static SynchronizedMap<Long, ? extends ScraperEvent> getScraperEventsMap(Class<? extends ScraperEvent> clazz) {
        SynchronizedMap<Long, ? extends ScraperEvent> returnValue;
        if (clazz == null) {
            logger.error("null clazz value in getScraperEventMap");
            returnValue = null;
        } else if (clazz.equals(BetradarEvent.class)) {
            returnValue = Statics.betradarEventsMap;
        } else if (clazz.equals(CoralEvent.class)) {
            returnValue = Statics.coralEventsMap;
        } else {
            logger.error("unknown clazz {} in getScraperEventMap", clazz);
            returnValue = null;
        }
        return returnValue;
    }

    public static ScraperThread getScraperThread(Class<? extends ScraperEvent> clazz) {
        ScraperThread returnValue;
        if (clazz == null) {
            logger.error("null clazz value in getScraperThread");
            returnValue = null;
        } else if (clazz.equals(BetradarEvent.class)) {
            returnValue = Statics.betradarScraperThread;
        } else if (clazz.equals(CoralEvent.class)) {
            returnValue = Statics.coralScraperThread;
        } else {
            logger.error("unknown clazz {} in getScraperThread", clazz);
            returnValue = null;
        }
        return returnValue;
    }

    public static <T extends Comparable<? super T>> T getMaxMultiple(List<T> list) {
        return getMaxMultiple(list, null, null, Statics.MIN_MATCHED);
    }

    public static <T extends Comparable<? super T>> T getMaxMultiple(List<T> list, Comparator<? super T> comparator) {
        return getMaxMultiple(list, null, comparator, Statics.MIN_MATCHED);
    }

    public static <T extends Comparable<? super T>> T getMaxMultiple(List<T> list, T defaultValue) {
        return getMaxMultiple(list, defaultValue, null, Statics.MIN_MATCHED);
    }

    public static <T extends Comparable<? super T>> T getMaxMultiple(List<T> list, T defaultValue, Comparator<? super T> comparator) {
        return getMaxMultiple(list, defaultValue, comparator, Statics.MIN_MATCHED);
    }

    public static <T extends Comparable<? super T>> T getMaxMultiple(List<T> list, T defaultValue, Comparator<? super T> comparator, int minNMultiple) {
        T returnValue;
        if (list == null) {
            returnValue = defaultValue;
        } else {
            Generic.collectionKeepMultiples(list, minNMultiple);
            if (comparator == null) {
                Collections.sort(list);
            } else {
                Collections.sort(list, comparator);
            }

            if (list.isEmpty()) {
                returnValue = defaultValue;
            } else {
                returnValue = list.get(list.size() - 1);
            }
        } // end else
        return returnValue;
    }

    public static String getEventIdOfMarketCatalogue(MarketCatalogue marketCatalogue) {
        String result;

        if (marketCatalogue != null) {
            final Event eventStump = marketCatalogue.getEventStump();
            if (eventStump == null) {
                logger.error("null event in marketCatalogue during getEventOfMarket: {}", Generic.objectToString(marketCatalogue));
                result = null;
            } else {
                result = eventStump.getId();
            }
        } else {
            logger.error("null marketCatalogue in Formulas.getEventIdOfMarketCatalogue");
            result = null;
        }

        return result;
    }

    public static String getEventIdOfMarketId(String marketId) {
        String result;

        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.get(marketId);
        if (marketCatalogue != null) {
            result = getEventIdOfMarketCatalogue(marketCatalogue);
        } else {
            logger.info("couldn't find marketId {} in Statics.marketCataloguesMap during getEventOfMarket", marketId);
            result = null;
        }

        return result;
    }

    public static Event getStoredEventOfMarketId(String marketId) {
        final String eventId = getEventIdOfMarketId(marketId);
        final Event event = Statics.eventsMap.get(eventId);
        return event;
    }

    public static Event getStoredEventOfMarketCatalogue(MarketCatalogue marketCatalogue) {
        final String eventId = getEventIdOfMarketCatalogue(marketCatalogue);
        final Event event = Statics.eventsMap.get(eventId);
        return event;
    }

    public static boolean isMarketType(MarketCatalogue marketCatalogue, String... types) {
        final boolean result;

        if (marketCatalogue != null && types != null) {
            final EventType eventType = marketCatalogue.getEventType();
            if (eventType != null) {
                final String eventTypeId = eventType.getId();
                if (eventTypeId != null) {
                    final List listTypes = Arrays.asList(types);
                    result = listTypes.contains(eventTypeId);
                } else {
                    logger.error("null eventType in isMarketType for: {} {}", Generic.objectToString(types), Generic.objectToString(marketCatalogue));
                    result = false;
                }
            } else {
                logger.error("null eventTypeId in isMarketType for: {} {}", Generic.objectToString(types), Generic.objectToString(marketCatalogue));
                result = false;
            }
        } else {
            logger.error("null arguments in isMarketType for: {} {}", Generic.objectToString(types), Generic.objectToString(marketCatalogue));
            result = false;
        }

        return result;
    }

    public static boolean isEachWayMarketType(String marketId) { // assumes the worst in case it doesn't find an answer
        final boolean isEachWay;

        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.get(marketId);
        if (marketCatalogue != null) {
            final MarketDescription marketDescription = marketCatalogue.getDescription();
            if (marketDescription == null) {
                logger.error("null marketDescription in marketCatalogue during isEachWayMarketType: {}", Generic.objectToString(marketCatalogue));
                isEachWay = true; // assume the worst
            } else {
                final String marketType = marketDescription.getMarketType();
                isEachWay = "EACH_WAY".equals(marketType);
            }
        } else {
            logger.error("couldn't find marketId {} in Statics.marketCataloguesMap during isEachWayMarketType", marketId);
            isEachWay = true;// assume the worst
        }

        return isEachWay;
    }
}
