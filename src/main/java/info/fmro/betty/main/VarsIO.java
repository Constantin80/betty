package info.fmro.betty.main;

import info.fmro.betty.logic.SafetyLimits;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.safebet.BetradarEvent;
import info.fmro.betty.safebet.CoralEvent;
import info.fmro.betty.safebet.SafeRunner;
import info.fmro.betty.threads.permanent.GetLiveMarketsThread;
import info.fmro.betty.threads.permanent.RulesManagerThread;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.entities.Event;
import info.fmro.shared.entities.MarketBook;
import info.fmro.shared.entities.MarketCatalogue;
import info.fmro.shared.objects.SessionTokenObject;
import info.fmro.shared.objects.TimeStamps;
import info.fmro.shared.stream.objects.StreamSynchronizedMap;
import info.fmro.shared.utility.DebugLevel;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.SynchronizedMap;
import info.fmro.shared.utility.SynchronizedReader;
import info.fmro.shared.utility.SynchronizedSafeSet;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
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

@SuppressWarnings("UtilityClass")
public final class VarsIO {
    private static final Logger logger = LoggerFactory.getLogger(VarsIO.class);
    public static final AtomicLong writeSettingsCounter = new AtomicLong();

    @Contract(pure = true)
    private VarsIO() {
    }

    private static long fileLastModified(final String fileName) {
        return (new File(fileName)).lastModified();
    }

    @NotNull
    public static List<String> getUnescapedSplits(final CharSequence charSequence, final char marker) {
        return getUnescapedSplits(charSequence, marker, 0);
    }

    @NotNull
    private static List<String> getUnescapedSplits(final CharSequence charSequence, final char marker, final int nExpected) {
        // doesn't get empty strings
        final List<String> matchList = new ArrayList<>(nExpected);
        final Pattern regex = Pattern.compile("(?:\\\\.|[^" + marker + "\\\\]++)+");
        final Matcher regexMatcher = regex.matcher(charSequence);
        while (regexMatcher.find()) {
            matchList.add(regexMatcher.group());
        }
        return matchList;
    }

    @SuppressWarnings("OverlyNestedMethod")
    private static boolean readAliases(final String fileName, final Map<? super String, String> syncMap) {
        boolean success;
        SynchronizedReader localSynchronizedReader = null;
        if (fileName != null) {
            try {
                final int mapCapacity = Generic.getCollectionCapacity(syncMap.size() + 1, 0.75f);
                final LinkedHashMap<String, String> newMap = new LinkedHashMap<>(mapCapacity, 0.75f);

                success = true;
                localSynchronizedReader = new SynchronizedReader(fileName);
                String fileLine = localSynchronizedReader.readLine();
//                synchronized (syncMap) {
//                newMap.clear(); // clear previous values
                while (fileLine != null) {
                    fileLine = fileLine.trim(); // only whole line is trimmed, not the individual aliases
                    fileLine = fileLine.toLowerCase(Locale.ENGLISH);
                    if (!fileLine.isEmpty() && fileLine.charAt(0) != '#') {
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
//                                syncMap.clear();
//                                success = false;
                        }
                    } else { // won't do anything with this line
                    }

                    fileLine = localSynchronizedReader.readLine();
                } // end while

                // apply toLowerCase() to all keys & values
                //noinspection SynchronizationOnLocalVariableOrMethodParameter
                synchronized (syncMap) {
                    final int mapSize = syncMap.size(), newMapSize = newMap.size();
                    if (newMapSize <= mapSize) {
                        logger.error("newMapSize {} smaller or equal to mapSize {} in aliases file {}", newMapSize, mapSize, fileName);
//                    newMap.clear();
                        success = false;
                    } else {
                        syncMap.clear();
                        final Set<Entry<String, String>> entries = newMap.entrySet();
                        for (final Entry<String, String> entry : entries) {
                            final String key = entry.getKey(), value = entry.getValue();
                            if (key == null || value == null) {
                                logger.error("STRANGE null key {} or value {} while reading aliases from: {}", key, value, fileName);
                                syncMap.clear(); // very bad error, clear syncMap
                                success = false;
                                break;
                            } else {
                                final String modifiedKey = key.toLowerCase(Locale.ENGLISH), modifiedValue = value.toLowerCase(Locale.ENGLISH);
                                final String previousValue = syncMap.put(modifiedKey, modifiedValue);
                                if (previousValue != null) {
                                    logger.error("double key after lowerCase in aliases file {}: key={} value={}", fileName, modifiedKey, modifiedValue);
                                }
                            }
                        } // end for
//                        newMap.clear();
//                        newMap.putAll(tempMap);
                    } // end else
                } // end synchronized
            } catch (FileNotFoundException e) {
                logger.error("FileNotFoundException inside readAliases from {}", fileName, e);
                success = false;
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

    public static boolean checkAliasesFile(final String fileName, @NotNull final AtomicLong fileTimeStamp, final Map<? super String, String> syncMap) {
        final boolean haveRead;
        final long newFileStamp = fileLastModified(fileName);
        if (newFileStamp > fileTimeStamp.get()) {
            haveRead = readAliases(fileName, syncMap);
//            if (haveRead) {
            fileTimeStamp.set(newFileStamp); // update fileStamp anyway, else program will keep trying and get the same error
//            }
        } else {
            haveRead = false;
        }
        if (haveRead) { // will run checkAll next
            GetLiveMarketsThread.timedMapEventsCounter.set(GetLiveMarketsThread.timedMapEventsCounter.get() - GetLiveMarketsThread.timedMapEventsCounter.get() % 10);
            Statics.timeStamps.setLastMapEventsToScraperEvents(System.currentTimeMillis());
            // Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.mapEventsToScraperEvents, true));
            Formulas.matchesCache.clear();
        }

        return haveRead;
    }

    public static void readVarsFromFile(final String fileName) {
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
                        //noinspection NestedTryStatement
                        try {
                            Statics.inputServerPort.set(Integer.parseInt(fileLine.substring(fileLine.indexOf("defaultInputServerPort=") + "defaultInputServerPort=".length())));
                        } catch (NumberFormatException numberFormatException) {
                            logger.error("NumberFormatException inside readVarsFromFile {}: {}", fileName, fileLine, numberFormatException);
                        }
                    } else if (fileLine.startsWith("defaultInterfaceServerPort=")) {
                        //noinspection NestedTryStatement
                        try {
                            Statics.interfaceServerPort.set(Integer.parseInt(fileLine.substring(fileLine.indexOf("defaultInterfaceServerPort=") + "defaultInterfaceServerPort=".length())));
                        } catch (NumberFormatException numberFormatException) {
                            logger.error("NumberFormatException2 inside readVarsFromFile {}: {}", fileName, fileLine, numberFormatException);
                        }
                    } else if (fileLine.startsWith("interfaceKeyPass=")) {
                        Statics.interfaceKeyStorePassword.set(Generic.backwardString(fileLine.substring(fileLine.indexOf("interfaceKeyPass=") + "interfaceKeyPass=".length())));
                    } else {
                        logger.error("Bogus line in vars file {}: {}", fileName, fileLine);
                    }
                    fileLine = localSynchronizedReader.readLine();
                } // end while
            } catch (FileNotFoundException e) {
                logger.error("FileNotFoundException inside readVarsFromFile {}", fileName, e);
            } catch (IOException iOException) {
                logger.error("IOException inside readVarsFromFile {}", fileName, iOException);
            } finally {
                Generic.closeObject(localSynchronizedReader);
            }
        } else {
            logger.error("null fileName passed as argument in readVarsFromFile");
        }
        logger.debug("have read vars from file");
    }

    public static void readObjectsFromFiles() {
        for (final Entry<String, String> entry : Statics.objectFileNamesMap.entrySet()) {
            final String key = entry.getKey();
            final Object objectFromFile = Generic.readObjectFromFile(entry.getValue());
            if (objectFromFile != null) {
                try {
                    //noinspection EnhancedSwitchMigration
                    switch (key) {
                        case "timeStamps":
                            final TimeStamps timeStamps = (TimeStamps) objectFromFile;
                            Statics.timeStamps.copyFrom(timeStamps);
                            break;
                        case "debugLevel":
                            final DebugLevel debugLevel = (DebugLevel) objectFromFile;
                            Statics.debugLevel.copyFrom(debugLevel);
                            break;
                        case "sessionTokenObject":
                            final SessionTokenObject sessionTokenObject = (SessionTokenObject) objectFromFile;
                            Statics.sessionTokenObject.copyFrom(sessionTokenObject);
                            break;
                        case "safetyLimits":
                            final SafetyLimits safetyLimits = (SafetyLimits) objectFromFile;
                            Statics.safetyLimits.copyFrom(safetyLimits);
                            break;
//                        case "blackList":
//                            final BlackList blackList = (BlackList) objectFromFile;
//                            BlackList.copyFrom(blackList);
//                            break;
//                        case "placedAmounts":
//                            final PlacedAmounts placedAmounts = (PlacedAmounts) objectFromFile;
//                            Statics.placedAmounts.copyFrom(placedAmounts);
//                            break;
                        case "betradarEventsMap":
                            @SuppressWarnings("unchecked") final SynchronizedMap<Long, BetradarEvent> betradarEventsMap = (SynchronizedMap<Long, BetradarEvent>) objectFromFile;
                            Statics.betradarEventsMap.copyFrom(betradarEventsMap);
                            break;
                        case "coralEventsMap":
                            @SuppressWarnings("unchecked") final SynchronizedMap<Long, CoralEvent> coralEventsMap = (SynchronizedMap<Long, CoralEvent>) objectFromFile;
                            Statics.coralEventsMap.copyFrom(coralEventsMap);
                            break;
                        case "eventsMap":
                            @SuppressWarnings("unchecked") final StreamSynchronizedMap<String, Event> eventsMap = (StreamSynchronizedMap<String, Event>) objectFromFile;
                            Statics.eventsMap.copyFrom(eventsMap);
                            break;
                        case "marketCataloguesMap":
                            @SuppressWarnings("unchecked") final StreamSynchronizedMap<String, MarketCatalogue> marketCataloguesMap = (StreamSynchronizedMap<String, MarketCatalogue>) objectFromFile;
                            Statics.marketCataloguesMap.copyFrom(marketCataloguesMap);
                            break;
                        case "safeMarketsMap":
                            @SuppressWarnings("unchecked") final SynchronizedMap<String, SynchronizedSafeSet<SafeRunner>> safeMarketsMap = (SynchronizedMap<String, SynchronizedSafeSet<SafeRunner>>) objectFromFile;
                            Statics.safeMarketsMap.copyFrom(safeMarketsMap);
                            break;
                        case "safeMarketBooksMap":
                            @SuppressWarnings("unchecked") final SynchronizedMap<String, MarketBook> safeMarketBooksMap = (SynchronizedMap<String, MarketBook>) objectFromFile;
                            Statics.safeMarketBooksMap.copyFrom(safeMarketBooksMap);
                            break;
//                        case "alreadyPrintedMap":
//                            final AlreadyPrintedMap alreadyPrintedMap = (AlreadyPrintedMap) objectFromFile;
//                            Generic.alreadyPrintedMap.copyFrom(alreadyPrintedMap);
//                            break;
                        case "timedWarningsMap":
                            @SuppressWarnings("unchecked") final SynchronizedMap<String, Long> timedWarningsMap = (SynchronizedMap<String, Long>) objectFromFile;
                            Statics.timedWarningsMap.copyFrom(timedWarningsMap);
                            break;
//                        case "ignorableDatabase":
//                            final IgnorableDatabase ignorableDatabase = (IgnorableDatabase) objectFromFile;
//                            Ignorable.database.copyFrom(ignorableDatabase);
//                            break;
//                        case "rulesManager":
//                            final RulesManager rulesManager = (RulesManager) objectFromFile;
//                            Statics.rulesManager.copyFrom(rulesManager);
//                            break;
//                        case "marketCache":
//                            @SuppressWarnings("unchecked") final MarketCache marketCache = (MarketCache) objectFromFile;
//                            Statics.offlineMarketCache.copyFrom(marketCache);
//                            break;
//                        case "orderCache":
//                            @SuppressWarnings("unchecked") final OrderCache orderCache = (OrderCache) objectFromFile;
//                            Statics.offlineOrderCache.copyFrom(orderCache);
//                            break;
                        default:
                            logger.error("unknown object in the fileNames map: {} {}", key, entry.getValue());
                            break;
                    } // end switch
                } catch (ClassCastException classCastException) { // the object class was probably changed recently
                    logger.error("classCastException while reading objects from files for: {}", key, classCastException);
                } catch (@SuppressWarnings("ProhibitedExceptionCaught") NullPointerException nullPointerException) { // the object class was probably changed recently; this is normally thrown from copyFrom methods
                    logger.error("nullPointerException while reading objects from files for: {}", key, nullPointerException);
                }
            } else {
                logger.warn("objectFromFile null for: {} {}", key, entry.getValue());
            }
        } // end for
//        Ignorable.database.syncIgnorableDatabase(Statics.programIsRunningMultiThreaded);

//        Ignorable.database.testIsGenerated();
        logger.debug("have read objects from files");
    }

    public static void writeObjectsToFiles() {
        Statics.timeStamps.lastObjectsSaveStamp(Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
        Statics.timeLastSaveToDisk.set(System.currentTimeMillis());
        for (final Entry<String, String> entry : Statics.objectFileNamesMap.entrySet()) {
            final String key = entry.getKey();
            //noinspection EnhancedSwitchMigration
            switch (key) {
                case "timeStamps":
                    Generic.synchronizedWriteObjectToFile(Statics.timeStamps, entry.getValue());
                    break;
                case "debugLevel":
                    Generic.synchronizedWriteObjectToFile(Statics.debugLevel, entry.getValue());
                    break;
                case "sessionTokenObject":
                    Generic.synchronizedWriteObjectToFile(Statics.sessionTokenObject, entry.getValue());
                    break;
                case "safetyLimits":
                    Generic.synchronizedWriteObjectToFile(Statics.safetyLimits, entry.getValue());
                    break;
//                case "blackList":
//                    Generic.synchronizedWriteObjectToFile(Statics.blackList, Statics.objectFileNamesMap.get(key));
//                    break;
//                case "placedAmounts":
//                    Generic.synchronizedWriteObjectToFile(Statics.placedAmounts, Statics.objectFileNamesMap.get(key));
//                    break;
                case "betradarEventsMap":
                    Generic.synchronizedWriteObjectToFile(Statics.betradarEventsMap, entry.getValue());
                    break;
                case "coralEventsMap":
                    Generic.synchronizedWriteObjectToFile(Statics.coralEventsMap, entry.getValue());
                    break;
                case "eventsMap":
                    Generic.synchronizedWriteObjectToFile(Statics.eventsMap, entry.getValue());
                    break;
                case "marketCataloguesMap":
                    Generic.synchronizedWriteObjectToFile(Statics.marketCataloguesMap, entry.getValue());
                    break;
                case "safeMarketsMap":
                    Generic.synchronizedWriteObjectToFile(Statics.safeMarketsMap, entry.getValue());
                    break;
                case "safeMarketBooksMap":
                    Generic.synchronizedWriteObjectToFile(Statics.safeMarketBooksMap, entry.getValue());
                    break;
//                case "alreadyPrintedMap":
//                    Generic.synchronizedWriteObjectToFile(Generic.alreadyPrintedMap, Statics.objectFileNamesMap.get(key));
//                    break;
                case "timedWarningsMap":
                    Generic.synchronizedWriteObjectToFile(Statics.timedWarningsMap, entry.getValue());
                    break;
//                case "ignorableDatabase":
//                    Generic.synchronizedWriteObjectToFile(Ignorable.database, Statics.objectFileNamesMap.get(key));
//                    break;
//                case "rulesManager":
//                    Generic.synchronizedWriteObjectToFile(Statics.rulesManager, Statics.objectFileNamesMap.get(key));
//                    break;
//                case "marketCache":
//                    Generic.synchronizedWriteObjectToFile(Statics.marketCache, Statics.objectFileNamesMap.get(key));
//                    break;
//                case "orderCache":
//                    Generic.synchronizedWriteObjectToFile(Statics.orderCache, Statics.objectFileNamesMap.get(key));
//                    break;
                default:
                    logger.error("unknown key in the fileNames map: {} {}", key, entry.getValue());
                    break;
            } // end switch
        } // end for
//        logger.info("have written objects to files");
    }

    public static void writeSettings() {
        Statics.timeStamps.lastSettingsSaveStamp(Generic.MINUTE_LENGTH_MILLISECONDS);
        Statics.rulesManagerThread.rulesManager.rulesHaveChanged.set(false);
        Generic.synchronizedWriteObjectToFile(Statics.rulesManagerThread, Statics.SETTINGS_FILE_NAME);
        writeSettingsCounter.incrementAndGet();
    }

    static boolean readSettings() {
        final boolean readSuccessful;
        final Object objectFromFile = Generic.readObjectFromFile(Statics.SETTINGS_FILE_NAME);
        if (objectFromFile != null) {
            final RulesManagerThread rulesManager = (RulesManagerThread) objectFromFile;
            readSuccessful = Statics.rulesManagerThread.copyFrom(rulesManager);
        } else {
            readSuccessful = false;
            logger.error("objectFromFile null in readSettings");
            if (Statics.resetTestMarker) {
                Statics.rulesManagerThread.copyFrom(new RulesManagerThread()); // I need copyFrom to run for reset to work
            }
        }

        return readSuccessful;
    }
//    public static void readPropertiesFromFiles() {
//        final File folder = new File(Statics.PROPERTIES_FOLDER_NAME);
//        final File[] fileNames = folder.listFiles();
//        if (fileNames == null) {
//            logger.error("null fileNames in readPropertiesFromFiles");
//        } else {
//            for (File file : fileNames) {
//                if (file.isDirectory()) {
//                    logger.error("subdirectory found in readPropertiesFromFiles");
//                } else {
//                    readPropertiesFile(file);
//                }
//            } // end for
//        }
//    }
//
//    public static void readPropertiesFile(File file) {
//        if (file == null) {
//            logger.error("null file in readPropertiesFile");
//        } else {
//            final String fileName = file.getName();
//            final Properties properties = new Properties();
//            try {
//                final FileInputStream fileInputStream = new FileInputStream(file);
//                try {
//                    properties.load(fileInputStream);
//                    try {
//                        final Enumeration<?> enumeration = properties.propertyNames();
//                        while (enumeration.hasMoreElements()) {
//                            final String key = (String) enumeration.nextElement();
//                            final String value = properties.getProperty(key);
//                            switch (key) {
//                                case "":
//                                    -
//                                    break;
//                                default:
//                                    logger.error("unknown key in readPropertiesFile enumeration: {} {} {}", key, value, fileName);
//                                    break;
//                            }
//                        } // end while
//                    } catch (ClassCastException e) {
//                        logger.error("ClassCastException in readPropertiesFile for: {} {}", fileName, Generic.objectToString(properties));
//                    }
//                } catch (IOException e) {
//                    logger.error("IOException in readPropertiesFile for: {}", fileName);
//                }
//            } catch (FileNotFoundException e) {
//                logger.error("FileNotFoundException in readPropertiesFile for: {}", fileName);
//            }
//        }
//    }
}
