package info.fmro.betty.main;

import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.MarketBook;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.objects.BetradarEvent;
import info.fmro.betty.objects.CoralEvent;
import info.fmro.betty.objects.DebugLevel;
import info.fmro.betty.objects.SafeRunner;
import info.fmro.betty.objects.SafetyLimits;
import info.fmro.betty.objects.SessionTokenObject;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.objects.TimeStamps;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.SynchronizedMap;
import info.fmro.shared.utility.SynchronizedReader;
import info.fmro.shared.utility.SynchronizedSafeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VarsIO {

    private static final Logger logger = LoggerFactory.getLogger(VarsIO.class);

    private VarsIO() {
    }

    public static long fileLastModified(String fileName) {
        return (new File(fileName)).lastModified();
    }

    public static List<String> getUnescapedSplits(String string, char marker) {
        return getUnescapedSplits(string, marker, 0);
    }

    public static List<String> getUnescapedSplits(String string, char marker, int nExpected) {
        // doesn't get empty strings
        List<String> matchList = new ArrayList<>(nExpected);
        Pattern regex = Pattern.compile("(?:\\\\.|[^" + marker + "\\\\]++)+");
        Matcher regexMatcher = regex.matcher(string);
        while (regexMatcher.find()) {
            matchList.add(regexMatcher.group());
        }
        return matchList;
    }

    public static boolean readAliases(String fileName, Map<String, String> map) {
        boolean success;
        SynchronizedReader localSynchronizedReader = null;

        if (fileName != null) {
            try {
                final int mapCapacity = Generic.getCollectionCapacity(map.size() + 1, 0.75f);
                final LinkedHashMap<String, String> newMap = new LinkedHashMap<>(mapCapacity, 0.75f);

                success = true;
                localSynchronizedReader = new SynchronizedReader(fileName);
                String fileLine = localSynchronizedReader.readLine();
//                synchronized (map) {
//                newMap.clear(); // clear previous values
                while (fileLine != null) {
                    fileLine = fileLine.trim(); // only whole line is trimmed, not the individual aliases
                    fileLine = fileLine.toLowerCase(Locale.ENGLISH);
                    if (!fileLine.isEmpty() && !fileLine.startsWith("#")) {
                        final List<String> stringList = getUnescapedSplits(fileLine, '"', 3);
                        final int size = stringList.size();
                        if (size == 2 || size == 3) {
                            final String newKey = stringList.get(0);
                            final boolean newKeyIsStrange = Formulas.containsStrangeChars(newKey);
                            if (newKeyIsStrange) {
                                logger.error("strange key in aliases file {}: {}", fileName, fileLine);
                            }
                            final String newValue = stringList.get(size - 1);
                            final boolean newValueIsStrange = Formulas.containsStrangeChars(newValue);
                            if (newValueIsStrange) {
                                logger.error("strange value in aliases file {}: {}", fileName, fileLine);
                            }

                            final String previousValue = newMap.put(newKey, newValue);
                            if (previousValue != null) {
                                logger.error("double key in aliases file {}: {}", fileName, fileLine);
                            }
                            if (newMap.containsValue(newKey)) {
                                logger.error("key already contained in values in aliases file {}: {}", fileName, fileLine);
                            }
                        } else {
                            logger.error("Bogus line in aliases file {}: {} listSize={} list:{}", fileName, fileLine, size, Generic.objectToString(stringList));
//                                map.clear();
//                                success = false;
                        }
                    } else { // won't do anything with this line
                    }

                    fileLine = localSynchronizedReader.readLine();
                } // end while

                // apply toLowerCase() to all keys & values
                synchronized (map) {
                    final int mapSize = map.size(), newMapSize = newMap.size();

                    if (newMapSize <= mapSize) {
                        logger.error("newMapSize {} smaller or equal to mapSize {} in aliases file {}", newMapSize, mapSize, fileName);
//                    newMap.clear();
                        success = false;
                    } else {
                        map.clear();
                        final Set<Entry<String, String>> entries = newMap.entrySet();
                        for (Entry<String, String> entry : entries) {
                            final String key = entry.getKey(), value = entry.getValue();
                            if (key == null || value == null) {
                                logger.error("STRANGE null key {} or value {} while reading aliases from: {}", key, value, fileName);
                                map.clear(); // very bad error, clear map
                                success = false;
                                break;
                            } else {
                                final String modifiedKey = key.toLowerCase(Locale.ENGLISH), modifiedValue = value.toLowerCase(Locale.ENGLISH);
                                final String previousValue = map.put(modifiedKey, modifiedValue);
                                if (previousValue != null) {
                                    logger.error("double key after lowerCase in aliases file {}: key={} value={}", fileName, modifiedKey, modifiedValue);
                                }
                            }
                        } // end for
//                        newMap.clear();
//                        newMap.putAll(tempMap);
                    } // end else
                } // end synchronized
            } catch (IOException iOException) {
                logger.error("IOException inside readAliases from {}", fileName, iOException);
                success = false;
            } finally {
                Generic.closeObject(localSynchronizedReader);
            }
        } else {
            logger.error("null fileName passed as argument in readAliases");
            success = false;
        }
        if (success) {
            logger.info("have updated aliases from {}", fileName);
        }

        return success;
    }

    public static boolean checkAliasesFile(String fileName, AtomicLong fileTimeStamp, Map<String, String> map) {
        boolean haveRead;
        long newFileStamp = fileLastModified(fileName);
        if (newFileStamp > fileTimeStamp.get()) {
            haveRead = readAliases(fileName, map);
//            if (haveRead) {
            fileTimeStamp.set(newFileStamp); // update fileStamp anyway, else program will keep trying and get the same error
//            }
        } else {
            haveRead = false;
        }
        if (haveRead) { // will run checkAll next
            GetLiveMarketsThread.timedMapEventsCounter.set(GetLiveMarketsThread.timedMapEventsCounter.get() - GetLiveMarketsThread.timedMapEventsCounter.get() % 10);
            Statics.timeStamps.setLastMapEventsToScraperEvents(System.currentTimeMillis());
            // Statics.threadPoolExecutor.execute(new LaunchCommandThread("mapEventsToScraperEvents", true));
            Formulas.matchesCache.clear();
        }

        return haveRead;
    }

    public static void readVarsFromFile(String fileName) {
        SynchronizedReader localSynchronizedReader = null;

        if (fileName != null) {
            try {
                localSynchronizedReader = new SynchronizedReader(fileName, Statics.DECRYPTION_KEY);

                String fileLine = localSynchronizedReader.readLine();
                while (fileLine != null) {
                    if (fileLine.startsWith("appKey=")) {
                        Statics.appKey.set(fileLine.substring(fileLine.indexOf("appKey=") + "appKey=".length()));
                    } else if (fileLine.startsWith("delayedAppKey=")) {
                        Statics.delayedAppKey.set(fileLine.substring(fileLine.indexOf("delayedAppKey=") + "delayedAppKey=".length()));
                    } else if (fileLine.startsWith("bu=")) {
                        Statics.bu.set(Generic.backwardString(fileLine.substring(fileLine.indexOf("bu=") + "bu=".length())));
                    } else if (fileLine.startsWith("bp=")) {
                        Statics.bp.set(Generic.backwardString(fileLine.substring(fileLine.indexOf("bp=") + "bp=".length())));
                    } else if (fileLine.startsWith("defaultInputServerPort=")) {
                        try {
                            Statics.inputServerPort.set(Integer.parseInt(fileLine.substring(fileLine.indexOf("defaultInputServerPort=") + "defaultInputServerPort=".length())));
                        } catch (NumberFormatException numberFormatException) {
                            logger.error("NumberFormatException inside readVarsFromFile {}: {}", fileName, fileLine, numberFormatException);
                        }
                    } else {
                        logger.error("Bogus line in vars file {}: {}", fileName, fileLine);
                    }

                    fileLine = localSynchronizedReader.readLine();
                } // end while
            } catch (IOException iOException) {
                logger.error("IOException inside readVarsFromFile {}", fileName, iOException);
            } finally {
                Generic.closeObject(localSynchronizedReader);
            }
        } else {
            logger.error("null fileName passed as argument in readVarsFromFile");
        }
        logger.info("have read vars from file");
    }

    public static void readObjectsFromFiles() {
        Set<String> keySet = Statics.objectFileNamesMap.keySet();
        for (String key : keySet) {
            Object objectFromFile = Generic.readObjectFromFile(Statics.objectFileNamesMap.get(key));
            if (objectFromFile != null) {
                try {
                    switch (key) {
                        case "timeStamps":
                            TimeStamps timeStamps = (TimeStamps) objectFromFile;
                            Statics.timeStamps.copyFrom(timeStamps);
                            break;
                        case "debugLevel":
                            DebugLevel debugLevel = (DebugLevel) objectFromFile;
                            Statics.debugLevel.copyFrom(debugLevel);
                            break;
                        case "sessionTokenObject":
                            SessionTokenObject sessionTokenObject = (SessionTokenObject) objectFromFile;
                            Statics.sessionTokenObject.copyFrom(sessionTokenObject);
                            break;
                        case "safetyLimits":
                            SafetyLimits safetyLimits = (SafetyLimits) objectFromFile;
                            Statics.safetyLimits.copyFrom(safetyLimits);
                            break;
//                        case "blackList":
//                            BlackList blackList = (BlackList) objectFromFile;
//                            BlackList.copyFrom(blackList);
//                            break;
//                        case "placedAmounts":
//                            PlacedAmounts placedAmounts = (PlacedAmounts) objectFromFile;
//                            Statics.placedAmounts.copyFrom(placedAmounts);
//                            break;
                        case "betradarEventsMap":
                            @SuppressWarnings("unchecked") SynchronizedMap<Long, BetradarEvent> betradarEventsMap = (SynchronizedMap<Long, BetradarEvent>) objectFromFile;
                            Statics.betradarEventsMap.copyFrom(betradarEventsMap);
                            break;
                        case "coralEventsMap":
                            @SuppressWarnings("unchecked") SynchronizedMap<Long, CoralEvent> coralEventsMap = (SynchronizedMap<Long, CoralEvent>) objectFromFile;
                            Statics.coralEventsMap.copyFrom(coralEventsMap);
                            break;
                        case "eventsMap":
                            @SuppressWarnings("unchecked") SynchronizedMap<String, Event> eventsMap = (SynchronizedMap<String, Event>) objectFromFile;
                            Statics.eventsMap.copyFrom(eventsMap);
                            break;
                        case "marketCataloguesMap":
                            @SuppressWarnings("unchecked") SynchronizedMap<String, MarketCatalogue> marketCataloguesMap = (SynchronizedMap<String, MarketCatalogue>) objectFromFile;
                            Statics.marketCataloguesMap.copyFrom(marketCataloguesMap);
                            break;
                        case "safeMarketsMap":
                            @SuppressWarnings("unchecked") SynchronizedMap<String, SynchronizedSafeSet<SafeRunner>> safeMarketsMap =
                                    (SynchronizedMap<String, SynchronizedSafeSet<SafeRunner>>) objectFromFile;
                            Statics.safeMarketsMap.copyFrom(safeMarketsMap);
                            break;
                        case "safeMarketBooksMap":
                            @SuppressWarnings("unchecked") SynchronizedMap<String, MarketBook> safeMarketBooksMap = (SynchronizedMap<String, MarketBook>) objectFromFile;
                            Statics.safeMarketBooksMap.copyFrom(safeMarketBooksMap);
                            break;
//                        case "alreadyPrintedMap":
//                            @SuppressWarnings("unchecked") AlreadyPrintedMap alreadyPrintedMap = (AlreadyPrintedMap) objectFromFile;
//                            Generic.alreadyPrintedMap.copyFrom(alreadyPrintedMap);
//                            break;
                        case "timedWarningsMap":
                            @SuppressWarnings("unchecked") SynchronizedMap<String, Long> timedWarningsMap = (SynchronizedMap<String, Long>) objectFromFile;
                            Statics.timedWarningsMap.copyFrom(timedWarningsMap);
                            break;
//                        case "ignorableDatabase":
//                            @SuppressWarnings("unchecked") IgnorableDatabase ignorableDatabase = (IgnorableDatabase) objectFromFile;
//                            Ignorable.database.copyFrom(ignorableDatabase);
//                            break;
                        default:
                            logger.error("unknown object in the fileNames map: {} {}", key, Statics.objectFileNamesMap.get(key));
                            break;
                    } // end switch
                } catch (ClassCastException classCastException) { // the object class was probably changed recently
                    logger.error("classCastException while reading objects from files for: {}", key, classCastException);
                } catch (NullPointerException nullPointerException) { // the object class was probably changed recently; this is normally thrown from copyFrom methods
                    logger.error("nullPointerException while reading objects from files for: {}", key, nullPointerException);
                }
            } else {
                logger.warn("objectFromFile null for: {} {}", key, Statics.objectFileNamesMap.get(key));
            }
        } // end for
//        Ignorable.database.syncIgnorableDatabase(Statics.programIsRunningMultiThreaded);

//        Ignorable.database.testIsGenerated();
        logger.info("have read objects from files");
    }

    public static void writeObjectsToFiles() {
        Statics.timeStamps.lastObjectsSaveStamp(Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
        Statics.timeLastSaveToDisk.set(System.currentTimeMillis());
        Set<String> keyset = Statics.objectFileNamesMap.keySet();
        for (String key : keyset) {
            switch (key) {
                case "timeStamps":
                    Generic.synchronizedWriteObjectToFile(Statics.timeStamps, Statics.objectFileNamesMap.get(key));
                    break;
                case "debugLevel":
                    Generic.synchronizedWriteObjectToFile(Statics.debugLevel, Statics.objectFileNamesMap.get(key));
                    break;
                case "sessionTokenObject":
                    Generic.synchronizedWriteObjectToFile(Statics.sessionTokenObject, Statics.objectFileNamesMap.get(key));
                    break;
                case "safetyLimits":
                    Generic.synchronizedWriteObjectToFile(Statics.safetyLimits, Statics.objectFileNamesMap.get(key));
                    break;
//                case "blackList":
//                    Generic.synchronizedWriteObjectToFile(Statics.blackList, Statics.objectFileNamesMap.get(key));
//                    break;
//                case "placedAmounts":
//                    Generic.synchronizedWriteObjectToFile(Statics.placedAmounts, Statics.objectFileNamesMap.get(key));
//                    break;
                case "betradarEventsMap":
                    Generic.synchronizedWriteObjectToFile(Statics.betradarEventsMap, Statics.objectFileNamesMap.get(key));
                    break;
                case "coralEventsMap":
                    Generic.synchronizedWriteObjectToFile(Statics.coralEventsMap, Statics.objectFileNamesMap.get(key));
                    break;
                case "eventsMap":
                    Generic.synchronizedWriteObjectToFile(Statics.eventsMap, Statics.objectFileNamesMap.get(key));
                    break;
                case "marketCataloguesMap":
                    Generic.synchronizedWriteObjectToFile(Statics.marketCataloguesMap, Statics.objectFileNamesMap.get(key));
                    break;
                case "safeMarketsMap":
                    Generic.synchronizedWriteObjectToFile(Statics.safeMarketsMap, Statics.objectFileNamesMap.get(key));
                    break;
                case "safeMarketBooksMap":
                    Generic.synchronizedWriteObjectToFile(Statics.safeMarketBooksMap, Statics.objectFileNamesMap.get(key));
                    break;
//                case "alreadyPrintedMap":
//                    Generic.synchronizedWriteObjectToFile(Generic.alreadyPrintedMap, Statics.objectFileNamesMap.get(key));
//                    break;
                case "timedWarningsMap":
                    Generic.synchronizedWriteObjectToFile(Statics.timedWarningsMap, Statics.objectFileNamesMap.get(key));
                    break;
//                case "ignorableDatabase":
//                    Generic.synchronizedWriteObjectToFile(Ignorable.database, Statics.objectFileNamesMap.get(key));
//                    break;
                default:
                    logger.error("unknown key in the fileNames map: {} {}", key, Statics.objectFileNamesMap.get(key));
                    break;
            } // end switch
        } // end for
        logger.info("have written objects to files");
    }
}
