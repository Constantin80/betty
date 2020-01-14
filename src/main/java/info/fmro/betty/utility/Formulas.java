package info.fmro.betty.utility;

import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.entities.PlaceInstruction;
import info.fmro.betty.main.VarsIO;
import info.fmro.shared.objects.OrderPrice;
import info.fmro.shared.objects.StampedDouble;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.objects.TwoOrderedStrings;
import info.fmro.betty.safebet.BetradarEvent;
import info.fmro.betty.safebet.CoralEvent;
import info.fmro.betty.safebet.ScraperEvent;
import info.fmro.betty.safebet.ScraperPermanentThread;
import info.fmro.shared.entities.EventType;
import info.fmro.shared.entities.MarketDescription;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.Ignorable;
import info.fmro.shared.utility.LogLevel;
import info.fmro.shared.utility.SynchronizedMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass", "UtilityClass"})
public final class Formulas {
    private static final Logger logger = LoggerFactory.getLogger(Formulas.class);
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Map<String, String> aliasesMap = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f));
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Map<String, String> fullAliasesMap = Collections.synchronizedMap(new LinkedHashMap<>(4, 0.75f));
    public static final SynchronizedMap<TwoOrderedStrings, StampedDouble> matchesCache = new SynchronizedMap<>();

    @Contract(pure = true)
    private Formulas() {
    }

    static {
        VarsIO.checkAliasesFile(Statics.ALIASES_FILE_NAME, Statics.aliasesTimeStamp, Formulas.aliasesMap);
        VarsIO.checkAliasesFile(Statics.FULL_ALIASES_FILE_NAME, Statics.fullAliasesTimeStamp, Formulas.fullAliasesMap);
    }

    public static String removeStrangeChars(final String teamString) {
        String modifiedString = null;
        if (teamString != null) {
            modifiedString = teamString.toLowerCase(Locale.ENGLISH).trim();
            synchronized (info.fmro.shared.utility.Formulas.charactersMap) {
                for (final Map.Entry<String, String> entry : info.fmro.shared.utility.Formulas.charactersMap.entrySet()) {
                    final String key = entry.getKey();
                    while (modifiedString.contains(key)) {
                        modifiedString = Generic.quotedReplaceAll(modifiedString, key, entry.getValue());
                    }
                }
            } // end synchronized
        } else {
            logger.error("null teamString in removeStrangeChars()");
        }

        return modifiedString;
    }

    public static boolean containsStrangeChars(final String teamString) {
        boolean result = false;
        if (teamString != null) {
            synchronized (info.fmro.shared.utility.Formulas.charactersMap) {
                final Set<String> keys = info.fmro.shared.utility.Formulas.charactersMap.keySet();
                for (final String key : keys) {
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

    @SuppressWarnings({"OverlyComplexMethod", "SpellCheckingInspection"})
    public static String parseTeamString(final String teamString) {
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
            if (modifiedString.startsWith("depor ")) {
                modifiedString = "deportivo " + modifiedString.substring("dep ".length());
            }

            while (modifiedString.contains("(") && modifiedString.contains(")") && modifiedString.indexOf('(') < modifiedString.indexOf(')')) {
                modifiedString = modifiedString.substring(0, modifiedString.indexOf('(')) + modifiedString.substring(modifiedString.indexOf(')') + ")".length());
            }
            if (modifiedString.contains("(") && modifiedString.indexOf('(') >= modifiedString.length() - 2) { // an extremely rare error, with an "(" left close to the end of the teamName
                modifiedString = modifiedString.substring(0, modifiedString.indexOf('('));
            }
            if (modifiedString.contains(" (")) { // an extremely rare error: atletico nacional (col
                modifiedString = modifiedString.substring(0, modifiedString.indexOf(" (")) + " " + modifiedString.substring(modifiedString.indexOf(" (") + " (".length());
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

            modifiedString = modifiedString.toLowerCase(Locale.ENGLISH).trim(); // before full aliases check; trim is important
            synchronized (fullAliasesMap) {
                if (fullAliasesMap.containsKey(modifiedString)) {
                    modifiedString = fullAliasesMap.get(modifiedString);
                }
            } // end synchronized
            synchronized (aliasesMap) {
                for (final Map.Entry<String, String> entry : aliasesMap.entrySet()) {
                    final String key = entry.getKey();
                    if (modifiedString.contains(key)) {
                        modifiedString = Generic.quotedReplaceAll(modifiedString, key, entry.getValue());
                    }
                }
            } // end synchronized
            modifiedString = modifiedString.toLowerCase(Locale.ENGLISH).trim(); // after all processing, before final checks

//            if (!Generic.isPureAscii(modifiedString)) {
//                logger.error("non standard character found in: {} original: {}", modifiedString, teamString);
//            }
            final int length = modifiedString.length();
            for (int i = 0; i < length; i++) {
                final char c = modifiedString.charAt(i);
                if ((c < 48 || c > 57) && (c < 97 || c > 122) && c != 32) {
                    Generic.alreadyPrintedMap.logOnce(Generic.HOUR_LENGTH_MILLISECONDS, Formulas.logger, LogLevel.ERROR, "forbidden char:{} code:{} in parseTeamString: {}", c, (int) c, teamString);
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

    @SuppressWarnings({"OverlyLongMethod", "SpellCheckingInspection"})
    private static String getCoreString(final String teamString) {
        String modifiedString = null;
        if (teamString != null) {
            modifiedString = teamString.toLowerCase(Locale.ENGLISH).trim(); // before initial processing

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

            modifiedString = modifiedString.toLowerCase(Locale.ENGLISH).trim(); // before full aliases check; trim is important
            synchronized (fullAliasesMap) {
                if (fullAliasesMap.containsKey(modifiedString)) {
                    modifiedString = fullAliasesMap.get(modifiedString);
                }
            } // end synchronized
            synchronized (aliasesMap) {
                for (final Map.Entry<String, String> entry : aliasesMap.entrySet()) {
                    final String key = entry.getKey();
                    if (modifiedString.contains(key)) {
                        modifiedString = Generic.quotedReplaceAll(modifiedString, key, entry.getValue());
                    }
                }
            } // end synchronized
            modifiedString = modifiedString.toLowerCase(Locale.ENGLISH).trim(); // after all processing, before final checks

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

    @SuppressWarnings("OverlyNestedMethod")
    public static double matchTeams(final String firstString, final String secondString) {
        double result;
        final TwoOrderedStrings twoOrderedStrings = new TwoOrderedStrings(firstString, secondString);
        final StampedDouble cachedResult = Formulas.matchesCache.get(twoOrderedStrings);

        if (cachedResult == null) {
            if (firstString == null || secondString == null) {
                result = 0;
            } else {
                String localFirst = firstString.trim().toLowerCase(Locale.ENGLISH), localSecond = secondString.trim().toLowerCase(Locale.ENGLISH);

                if (localFirst.isEmpty() || localSecond.isEmpty()) {
                    result = 0;
                } else if (localFirst.equals(localSecond)) {
                    result = 1;
                } else if ((localSecond.length() > 3 && localFirst.contains(localSecond)) || (localFirst.length() > 3 && localSecond.contains(localFirst))) {
                    result = .98;

                    localFirst = parseTeamString(localFirst);
                    localSecond = parseTeamString(localSecond);
                    if (localFirst.equals(localSecond)) {
                        result = .99; // an increase
                    } else { // verified on next branch
                    }
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

    public static boolean shouldAddInstruction(final OrderPrice orderPrice, final double limitOrderSize) {
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

    public static void removeInstruction(final OrderPrice orderPrice, final double orderSize) {
        synchronized (Statics.executingOrdersMap) {
            final double existingSize;
            if (Statics.executingOrdersMap.containsKey(orderPrice)) {
                existingSize = Statics.executingOrdersMap.get(orderPrice);
            } else {
                logger.error("STRANGE order not found in Statics.executingOrdersMap for: {} in {}", Generic.objectToString(orderPrice), Generic.objectToString(Statics.executingOrdersMap));
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

            if (Statics.executingOrdersMap.isEmpty()) { // map empty
            } else {
                logger.warn("orders still exist in Statics.executingOrdersMap: {} {}", Statics.executingOrdersMap.size(), Generic.objectToString(Statics.executingOrdersMap));
            }
        } // end synchronized block
    }

    public static void addInstruction(final OrderPrice orderPrice, final double orderSize, final String marketId, final PlaceInstruction placeInstruction) {
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

    public static long getLastGetScraperEvents(@NotNull final AtomicLong lastGetScraperEvents) {
//        synchronized (lastGetScraperEvents) {
        return lastGetScraperEvents.get();
//        }
    }

    public static void setLastGetScraperEvents(@NotNull final AtomicLong lastGetScraperEvents, final long newValue) {
//        synchronized (lastGetScraperEvents) {
        lastGetScraperEvents.set(newValue);
//        }
    }

    public static long addAndGetLastGetScraperEvents(@NotNull final AtomicLong lastGetScraperEvents, final long addedValue) {
//        synchronized (lastGetScraperEvents) {
        return lastGetScraperEvents.addAndGet(addedValue);
//        }
    }

    public static void lastGetScraperEventsStamp(@NotNull final AtomicLong lastGetScraperEvents) {
//        synchronized (lastGetScraperEvents) {
        lastGetScraperEvents.set(System.currentTimeMillis());
//        }
    }

    public static void lastGetScraperEventsStamp(@NotNull final AtomicLong lastGetScraperEvents, final long timeStamp) {
        final long currentTime = System.currentTimeMillis();
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lastGetScraperEvents) {
            if (currentTime - lastGetScraperEvents.get() >= timeStamp) {
                lastGetScraperEvents.set(currentTime + timeStamp);
            } else {
                lastGetScraperEvents.addAndGet(timeStamp);
            }
        } // end synchronized
    }

    @SuppressWarnings("unchecked")
    public static <T extends Ignorable> SynchronizedMap<?, T> getIgnorableMap(final Class<T> clazz) {
        @Nullable final SynchronizedMap<?, T> returnValue;
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

    public static SynchronizedMap<Long, ? extends ScraperEvent> getScraperEventsMap(final Class<? extends ScraperEvent> clazz) {
        @Nullable final SynchronizedMap<Long, ? extends ScraperEvent> returnValue;
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

    public static ScraperPermanentThread getScraperThread(final Class<? extends ScraperEvent> clazz) {
        @Nullable final ScraperPermanentThread returnValue;
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

    public static <T extends Comparable<? super T>> T getMaxMultiple(final List<T> list) {
        return getMaxMultiple(list, null, null, Statics.MIN_MATCHED);
    }

    public static <T extends Comparable<? super T>> T getMaxMultiple(final List<T> list, final Comparator<? super T> comparator) {
        return getMaxMultiple(list, null, comparator, Statics.MIN_MATCHED);
    }

    public static <T extends Comparable<? super T>> T getMaxMultiple(final List<? extends T> list, final T defaultValue) {
        return getMaxMultiple(list, defaultValue, null, Statics.MIN_MATCHED);
    }

    public static <T extends Comparable<? super T>> T getMaxMultiple(final List<? extends T> list, final T defaultValue, final Comparator<? super T> comparator) {
        return getMaxMultiple(list, defaultValue, comparator, Statics.MIN_MATCHED);
    }

    public static <T extends Comparable<? super T>> T getMaxMultiple(final List<? extends T> list, final T defaultValue, final Comparator<? super T> comparator, final int minNMultiple) {
        final T returnValue;
        if (list == null) {
            returnValue = defaultValue;
        } else {
            Generic.collectionKeepMultiples(list, minNMultiple);
            if (comparator == null) {
                Collections.sort(list);
            } else {
                list.sort(comparator);
            }

            returnValue = list.isEmpty() ? defaultValue : list.get(list.size() - 1);
        } // end else
        return returnValue;
    }

    private static String getEventIdOfMarketCatalogue(final MarketCatalogue marketCatalogue) {
        @Nullable final String result;

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

    public static String getEventIdOfMarketId(final String marketId) {
        @Nullable final String result;

        final MarketCatalogue marketCatalogue = Statics.marketCataloguesMap.get(marketId);
        if (marketCatalogue != null) {
            result = getEventIdOfMarketCatalogue(marketCatalogue);
        } else {
            logger.info("couldn't find marketId {} in Statics.marketCataloguesMap during getEventOfMarket", marketId);
            result = null;
        }

        return result;
    }

    public static Event getStoredEventOfMarketId(final String marketId) {
        final String eventId = getEventIdOfMarketId(marketId);
        return Statics.eventsMap.get(eventId);
    }

    public static Event getStoredEventOfMarketCatalogue(final MarketCatalogue marketCatalogue) {
        final String eventId = getEventIdOfMarketCatalogue(marketCatalogue);
        return Statics.eventsMap.get(eventId);
    }

    public static boolean isMarketType(final MarketCatalogue marketCatalogue, final Collection<String> typesList) {
        final boolean result;

        if (marketCatalogue != null && typesList != null) {
            final EventType eventType = marketCatalogue.getEventType();
            if (eventType != null) {
                final String eventTypeId = eventType.getId();
                if (eventTypeId != null) {
                    result = typesList.contains(eventTypeId);
                } else {
                    logger.error("null eventType in isMarketType listArg for: {} {}", Generic.objectToString(typesList), Generic.objectToString(marketCatalogue));
                    result = false;
                }
            } else {
                logger.error("null eventTypeId in isMarketType listArg for: {} {}", Generic.objectToString(typesList), Generic.objectToString(marketCatalogue));
                result = false;
            }
        } else {
            logger.error("null arguments in isMarketType listArg for: {} {}", Generic.objectToString(typesList), Generic.objectToString(marketCatalogue));
            result = false;
        }

        return result;
    }

    public static boolean isMarketType(final MarketCatalogue marketCatalogue, final String... types) {
        return isMarketType(marketCatalogue, types == null ? null : Arrays.asList(types));
    }

    public static boolean isEachWayMarketType(final String marketId) { // assumes the worst in case it doesn't find an answer
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
