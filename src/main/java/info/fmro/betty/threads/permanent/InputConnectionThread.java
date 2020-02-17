package info.fmro.betty.threads.permanent;

import info.fmro.betty.main.VarsIO;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.LaunchCommandThread;
import info.fmro.betty.utility.DebuggingMethods;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.enums.CommandType;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("OverlyComplexClass")
class InputConnectionThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InputConnectionThread.class);
    private final Socket socket;

    InputConnectionThread(final Socket socket) {
        super();
        this.socket = socket;
    }

    synchronized void closeSocket() {
        logger.info("closing InputConnectionThread socket");
        Generic.closeObjects(this.socket);
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod", "NestedTryStatement"})
    @Override
    public void run() {
        OutputStream outputStream = null;
        PrintWriter printWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            outputStream = this.socket.getOutputStream();
            //noinspection resource,IOResourceOpenedButNotSafelyClosed
            printWriter = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);
            inputStream = this.socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            //noinspection resource,IOResourceOpenedButNotSafelyClosed
            bufferedReader = new BufferedReader(inputStreamReader);

            String inputLine = bufferedReader.readLine();
            while (inputLine != null && !Statics.mustStop.get()) {
                String newOrder = null;
                inputLine = inputLine.toLowerCase(Locale.ENGLISH);
                if ("exit".equals(inputLine) || "quit".equals(inputLine)) {
                    printWriter.println("Bye");
                    break;
                } else if ("stop".equals(inputLine)) {
                    Statics.mustStop.set(true);
                    logger.info("Stop command executed");
                    printWriter.println("Stopping program");
                } else if ("sleep".equals(inputLine)) {
                    if (Statics.mustSleep.get()) {
                        printWriter.println("Program already sleeping");
                    } else {
                        Statics.mustSleep.set(true);
                        logger.info("Sleep command executed");
                        printWriter.println("Program starting sleep");
                    }
                } else if ("wake".equals(inputLine)) {
                    if (Statics.mustSleep.get()) {
                        Statics.mustSleep.set(false);
                        logger.info("Wake command executed");
                        printWriter.println("Waking the program up");
                    } else {
                        printWriter.println("Program already awake");
                    }
                } else if ("denyBetting".equals(inputLine)) {
                    if (Statics.denyBetting.get()) {
                        printWriter.println("denyBetting already active");
                    } else {
                        Statics.denyBetting.set(true);
                        logger.warn("denyBetting command executed");
                        printWriter.println("denyBetting activated");
                    }
                } else if ("startBetting".equals(inputLine)) {
                    if (Statics.denyBetting.get()) {
                        Statics.denyBetting.set(false);
                        logger.warn("startBetting command executed");
                        printWriter.println("denyBetting inactivated");
                    } else {
                        printWriter.println("denyBetting already inactive");
                    }
                } else if (inputLine.startsWith("savePage ".toLowerCase(Locale.ENGLISH))) {
                    final String command = inputLine.substring("savePage ".length()).trim();
                    final boolean success;
                    switch (command) {
                        case "betradar":
                            Statics.betradarScraperThread.mustSavePage.set(true);
                            success = true;
                            break;
                        case "coral":
                            Statics.coralScraperThread.mustSavePage.set(true);
                            success = true;
                            break;
                        default:
                            printWriter.println("unknown savePage command: " + command);
                            success = false;
                            break;
                    } // end switch
                    if (success) {
                        logger.info("savePage {} command executed", command);
                        printWriter.println("Will save " + command + " web page");
                    }
                } else if (inputLine.startsWith("refreshPage ".toLowerCase(Locale.ENGLISH))) {
                    final String command = inputLine.substring("refreshPage ".length()).trim();
                    final boolean success;
                    switch (command) {
                        case "betradar":
                            Statics.betradarScraperThread.mustRefreshPage.set(true);
                            success = true;
                            break;
                        case "coral":
                            Statics.coralScraperThread.mustRefreshPage.set(true);
                            success = true;
                            break;
                        default:
                            printWriter.println("unknown refreshPage command: " + command);
                            success = false;
                            break;
                    } // end switch
                    if (success) {
                        logger.info("refreshPage {} command executed", command);
                        printWriter.println("Will refresh " + command + " web page");
                    }
                } else if ("getNewSsoId".equals(inputLine)) {
                    Statics.needSessionToken.set(true);
                    logger.info("getNewSsoId command executed");
                    printWriter.println("Will get new ssoId");
                } else if ("writeObjects".equals(inputLine)) {
                    Statics.mustWriteObjects.set(true);
                    logger.info("writeObjects command executed");
                    printWriter.println("Will write objects to disk");
                } else if (inputLine.startsWith("rules ")) {
                    final String rulesString = inputLine.substring("rules ".length()).trim();
                    logger.info("rulesManager command executed: {}", rulesString);
                    printWriter.println("Will execute rulesManager command");

                    Statics.rulesManagerThread.rulesManager.executeCommand(rulesString, Statics.pendingOrdersThread, Statics.orderCache, Statics.safetyLimits.existingFunds, Statics.marketCataloguesMap, Statics.eventsMap,
                                                                           Statics.rulesManagerThread.rulesManager);
                } else if ("findMarketTypes".equals(inputLine)) {
                    printWriter.println("Will find new market types");
                    newOrder = "findMarketTypes";
                } else if (inputLine.startsWith("printCachedMarkets ".toLowerCase(Locale.ENGLISH))) {
                    String marketIds = inputLine.substring("printCachedMarkets ".length()).trim();
                    if (!marketIds.isEmpty() && marketIds.charAt(0) == '=') {
                        marketIds = marketIds.substring("=".length()).trim();
                    }
                    printWriter.println("Will print cached marketIds: " + marketIds);
                    DebuggingMethods.printCachedMarkets(marketIds);
                } else if ("printCachedOrders".equals(inputLine)) {
                    printWriter.println("Will print cached orders");
                    DebuggingMethods.printCachedOrders();
                } else if (inputLine.startsWith("printMarket ".toLowerCase(Locale.ENGLISH))) {
                    String marketId = inputLine.substring("printMarket ".length()).trim();
                    if (!marketId.isEmpty() && marketId.charAt(0) == '=') {
                        marketId = marketId.substring("=".length()).trim();
                    }
                    printWriter.println("Will print marketId: " + marketId);
                    newOrder = "market:" + marketId;
                } else if (inputLine.startsWith("printEvent ".toLowerCase(Locale.ENGLISH))) {
                    String eventId = inputLine.substring("printEvent ".length()).trim();
                    if (!eventId.isEmpty() && eventId.charAt(0) == '=') {
                        eventId = eventId.substring("=".length()).trim();
                    }
                    printWriter.println("Will print eventId: " + eventId);
                    newOrder = "event:" + eventId;
                } else if (inputLine.startsWith("printMarketType ".toLowerCase(Locale.ENGLISH))) {
                    String marketType = inputLine.substring("printMarketType ".length()).trim();
                    if (!marketType.isEmpty() && marketType.charAt(0) == '=') {
                        marketType = marketType.substring("=".length()).trim();
                    }
                    marketType = marketType.toUpperCase(Locale.ENGLISH); // they're always uppercase
                    printWriter.println("Will print marketType: " + marketType);
                    newOrder = "marketType:" + marketType;
                } else if ("weightTest".equals(inputLine)) {
                    printWriter.println("Will run weightTest");
                    newOrder = "weightTest";
                } else if (inputLine.startsWith("printMap ".toLowerCase(Locale.ENGLISH))) {
                    final String mapString = inputLine.substring("printMap ".length()).trim();
                    if (mapString.startsWith("betradarEventsMap ".toLowerCase(Locale.ENGLISH))) {
                        final String mapCommandString = mapString.substring("betradarEventsMap ".length()).trim();
                        if ("keys".equals(mapCommandString)) {
                            logger.info("betradarEventsMap keys: {}", Statics.betradarEventsMap.keySetCopy());
                        } else {
                            try {
                                logger.info("betradarEventsMap key {} : {}", mapCommandString, Generic.objectToString(Statics.betradarEventsMap.get(Long.valueOf(mapCommandString))));
                            } catch (NumberFormatException numberFormatException) {
                                logger.error("numberFormatException while parsing betradarEventsMap command: {}", mapCommandString, numberFormatException);
                            }
                        }
                        printWriter.println("Executed betradarEventsMap command");
                    } else if (mapString.startsWith("coralEventsMap ".toLowerCase(Locale.ENGLISH))) {
                        final String mapCommandString = mapString.substring("coralEventsMap ".length()).trim();
                        if ("keys".equals(mapCommandString)) {
                            logger.info("coralEventsMap keys: {}", Statics.coralEventsMap.keySetCopy());
                        } else {
                            try {
                                logger.info("coralEventsMap key {} : {}", mapCommandString, Generic.objectToString(Statics.coralEventsMap.get(Long.valueOf(mapCommandString))));
                            } catch (NumberFormatException numberFormatException) {
                                logger.error("numberFormatException while parsing coralEventsMap command: {}", mapCommandString, numberFormatException);
                            }
                        }
                        printWriter.println("Executed coralEventsMap command");
                    } else if (mapString.startsWith("eventsMap ".toLowerCase(Locale.ENGLISH))) {
                        final String mapCommandString = mapString.substring("eventsMap ".length()).trim();
                        if ("keys".equals(mapCommandString)) {
                            logger.info("eventsMap keys: {}", Statics.eventsMap.keySetCopy());
                        } else {
                            logger.info("eventsMap key {} : {}", mapCommandString, Generic.objectToString(Statics.eventsMap.get(mapCommandString)));
                        }
                        printWriter.println("Executed eventsMap command");
                    } else if (mapString.startsWith("marketCataloguesMap ".toLowerCase(Locale.ENGLISH))) {
                        final String mapCommandString = mapString.substring("marketCataloguesMap ".length()).trim();
                        if ("keys".equals(mapCommandString)) {
                            logger.info("marketCataloguesMap keys: {}", Statics.marketCataloguesMap.keySetCopy());
                        } else {
                            logger.info("marketCataloguesMap key {} : {}", mapCommandString, Generic.objectToString(Statics.marketCataloguesMap.get(mapCommandString)));
                        }
                        printWriter.println("Executed marketCataloguesMap command");
                        // } else if (mapString.startsWith("eventsinfomap ")) {
                        //     String mapCommandString = mapString.substring("eventsinfomap ".length()).trim();
                        //     if (mapCommandString.equals("keys")) {
                        //         logger.info("eventsInfoMap keys: {}", Statics.eventsInfoMap.keySet());
                        //     } else {
                        //         logger.info("eventsInfoMap key {} : {}", mapCommandString, Statics.eventsInfoMap.get(mapCommandString));
                        //     }
                        //     printWriter.println("Executed eventsInfoMap command");
                        // } else if (mapString.startsWith("interestingmarketsset")) {
                        // String mapCommandString = mapString.substring("interestingmarketsset ".length()).trim();
                        // if (mapCommandString.equals("keys")) {
                        // logger.info("interestingMarketsSet keys: {}", Statics.interestingMarketsSet);
                        // } else {
                        //     logger.info("interestingMarketsSet key {} : {}", mapCommandString, Statics.interestingMarketsSet.get(mapCommandString));
                        // }
                        // printWriter.println("Executed interestingMarketsSet command");
                    } else if (mapString.startsWith("safeMarketsMap ".toLowerCase(Locale.ENGLISH))) {
                        final String mapCommandString = mapString.substring("safeMarketsMap ".length()).trim();
                        if ("keys".equals(mapCommandString)) {
                            logger.info("safeMarketsMap keys: {}", Statics.safeMarketsMap.keySetCopy());
                        } else {
                            logger.info("safeMarketsMap key {} : {}", mapCommandString, Generic.objectToString(Statics.safeMarketsMap.get(mapCommandString)));
                        }
                        printWriter.println("Executed safeMarketsMap command");
                    } else if (mapString.startsWith("safeMarketBooksMap ".toLowerCase(Locale.ENGLISH))) {
                        final String mapCommandString = mapString.substring("safeMarketBooksMap ".length()).trim();
                        if ("keys".equals(mapCommandString)) {
                            logger.info("safeMarketBooksMap keys: {}", Statics.safeMarketBooksMap.keySetCopy());
                        } else {
                            logger.info("safeMarketBooksMap key {} : {}", mapCommandString, Generic.objectToString(Statics.safeMarketBooksMap.get(mapCommandString)));
                        }
                        printWriter.println("Executed safeMarketBooksMap command");
                    } else {
                        printWriter.println("Unknown mapString: " + mapString);
                    }
                } else if (inputLine.startsWith("debug ")) {
                    final String debugString = inputLine.substring("debug ".length()).trim();
                    if (debugString.startsWith("level ")) {
                        final String debugCommandString = debugString.substring("level ".length()).trim();
                        try {
                            final int newLevel = Integer.parseInt(debugCommandString);

                            if (newLevel >= 0) {
                                printWriter.println("new debug level: " + newLevel);
                                logger.info("debug level changed: {}", newLevel);
                                Statics.debugLevel.setLevel(newLevel);
                            } else {
                                printWriter.println("bad debug level: " + newLevel);
                            }
                        } catch (NumberFormatException numberFormatException) {
                            printWriter.println("numberFormatException: " + debugCommandString);
                            logger.error("numberFormatException for debug level command: {}", debugCommandString, numberFormatException);
                        }
                    } else if (debugString.startsWith("add ")) {
                        final String debugCommandString = debugString.substring("add ".length()).trim();
                        try {
                            final int code = Integer.parseInt(debugCommandString);

                            if (Statics.debugLevel.add(code)) {
                                printWriter.println("code added: " + code);
                                logger.info("debug code added: {}", code);
                            } else {
                                printWriter.println("code already existed: " + code);
                            }
                        } catch (NumberFormatException numberFormatException) {
                            printWriter.println("numberFormatException: " + debugCommandString);
                            logger.error("numberFormatException for debug add command: {}", debugCommandString, numberFormatException);
                        }
                    } else if (debugString.startsWith("remove ")) {
                        final String debugCommandString = debugString.substring("remove ".length()).trim();
                        try {
                            final int code = Integer.parseInt(debugCommandString);

                            if (Statics.debugLevel.remove(code)) {
                                printWriter.println("code removed: " + code);
                                logger.info("debug code removed: {}", code);
                            } else {
                                printWriter.println("code not present: " + code);
                            }
                        } catch (NumberFormatException numberFormatException) {
                            printWriter.println("numberFormatException: " + debugCommandString);
                            logger.error("numberFormatException for debug remove command: {}", debugCommandString, numberFormatException);
                        }
                    } else if ("clear".equals(debugString)) {
                        Statics.debugLevel.clear();
                        logger.info("debugLevel clear");
                        printWriter.println("Executed debugLevel clear command");
                    } else if ("print".equals(debugString)) {
                        logger.info("debugLevel: {}", Generic.objectToString(Statics.debugLevel));
                        printWriter.println("Executed debugLevel print command");
                    } else {
                        printWriter.println("Unknown debugString: " + debugString);
                    }
                } else if (inputLine.startsWith("alias ")) {
                    final String aliasCommandString = inputLine.substring("alias ".length()).trim();
                    String aliasFirst = null;
                    String aliasSecond = null;
                    final int indexSecondQuotes = aliasCommandString.indexOf('"', aliasCommandString.indexOf('"') + "\"".length());
                    if (!aliasCommandString.isEmpty() && aliasCommandString.charAt(0) == '\"' && indexSecondQuotes > 0) {
                        aliasFirst = aliasCommandString.substring(aliasCommandString.indexOf('"') + "\"".length(), indexSecondQuotes);
                        aliasSecond = aliasCommandString.substring(indexSecondQuotes + "\"".length()).trim();
                        if (!aliasSecond.isEmpty() && aliasSecond.charAt(0) == '\"' && aliasSecond.indexOf('"', aliasSecond.indexOf('"') + "\"".length()) > 0) {
                            aliasSecond = aliasSecond.substring(aliasSecond.indexOf('"') + "\"".length(), aliasSecond.indexOf('"', aliasSecond.indexOf('"') + "\"".length()));
                        }
                    } else if (aliasCommandString.contains(" ")) {
                        aliasFirst = aliasCommandString.substring(0, aliasCommandString.indexOf(' '));
                        aliasSecond = aliasCommandString.substring(aliasCommandString.indexOf(' ') + " ".length()).trim();
                        if (!aliasSecond.isEmpty() && aliasSecond.charAt(0) == '\"' && aliasSecond.indexOf('"', aliasSecond.indexOf('"') + "\"".length()) > 0) {
                            aliasSecond = aliasSecond.substring(aliasSecond.indexOf('"') + "\"".length(), aliasSecond.indexOf('"', aliasSecond.indexOf('"') + "\"".length()));
                        }
                    } else {
                        printWriter.println("unknown alias command: " + aliasCommandString);
                    }
                    if (aliasFirst != null && !aliasFirst.isEmpty() && !aliasSecond.isEmpty()) { // aliasSecond != null
                        final String printedString = MessageFormatter.arrayFormat("Will add alias \"{}\" to \"{}\"", new Object[]{aliasFirst, aliasSecond}).getMessage();
                        printWriter.println(printedString);
                        logger.info(printedString);
                        final String previousValue = Formulas.aliasesMap.put(aliasFirst, aliasSecond);
                        if (!Objects.equals(previousValue, aliasSecond)) {
                            Formulas.matchesCache.clear();
                        }
                        GetLiveMarketsThread.timedMapEventsCounter.set(GetLiveMarketsThread.timedMapEventsCounter.get() - GetLiveMarketsThread.timedMapEventsCounter.get() % 10 + 11); // will delay running checkAll again
                        Statics.timeStamps.setLastMapEventsToScraperEvents(System.currentTimeMillis() + Generic.MINUTE_LENGTH_MILLISECONDS);
                        if (Statics.safeBetModuleActivated) {
                            Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.mapEventsToScraperEvents, true));
                        }
                    }
                } else if (inputLine.startsWith("reserve ")) {
                    final String reserveCommandString = inputLine.substring("reserve ".length()).trim();
                    if (reserveCommandString.startsWith("get")) {
                        logger.info("current safety reserve is: {}", Statics.safetyLimits.existingFunds.getReserve());
                        printWriter.println("current safety reserve is: " + Statics.safetyLimits.existingFunds.getReserve());
                    } else if (reserveCommandString.startsWith("set ")) {
                        try {
                            final double newReserve = Double.parseDouble(reserveCommandString.substring("set ".length()).trim());

                            if (Statics.safetyLimits.existingFunds.setReserve(newReserve)) {
                                printWriter.println("have set new reserve: " + Statics.safetyLimits.existingFunds.getReserve());
                                logger.info("have set new reserve: {}", Statics.safetyLimits.existingFunds.getReserve());
                            } else {
                                printWriter.println("new reserve " + newReserve + " not set: " + Statics.safetyLimits.existingFunds.getReserve());
                            }
                        } catch (NumberFormatException numberFormatException) {
                            printWriter.println("numberFormatException: " + reserveCommandString);
                            logger.error("numberFormatException for reserveCommandString: {}", reserveCommandString, numberFormatException);
                        }
                    } else {
                        printWriter.println("Unknown reserve command: " + reserveCommandString);
                    }
                } else if ("cancelOrders".equals(inputLine)) {
                    printWriter.println("Will cancel all orders");
                    newOrder = "cancelOrders";
                } else if ("readAliases".equals(inputLine)) {
                    printWriter.println("Will read aliases from files");
                    logger.info("executing command to read aliases from files");
                    VarsIO.checkAliasesFile(Statics.ALIASES_FILE_NAME, Statics.aliasesTimeStamp, Formulas.aliasesMap);
                    VarsIO.checkAliasesFile(Statics.FULL_ALIASES_FILE_NAME, Statics.fullAliasesTimeStamp, Formulas.fullAliasesMap);
                } else if ("printAliases".equals(inputLine)) {
                    printWriter.println("Will print aliases maps");
                    logger.info("aliasesMap: {}", Generic.objectToString(Formulas.aliasesMap));
                    logger.info("fullAliasesMap: {}", Generic.objectToString(Formulas.fullAliasesMap));
                } else if ("safetyLimits".equals(inputLine)) {
                    printWriter.println("Will print placed amounts");
                    logger.info("safetyLimits: {}", Generic.objectToString(Statics.safetyLimits));
                } else if (inputLine.startsWith("addManagedRunner ".toLowerCase(Locale.ENGLISH))) {
                    final String addManagedRunnerCommandString = inputLine.substring("addManagedRunner ".length()).trim();
                    Statics.rulesManagerThread.rulesManager.addManagedRunnerCommands.add(addManagedRunnerCommandString);
                    Statics.rulesManagerThread.rulesManager.newAddManagedRunnerCommand.set(true);
                    printWriter.println("added managedRunnerCommandString: " + addManagedRunnerCommandString);
                    logger.info("adding newAddManagedRunnerCommand: {}", addManagedRunnerCommandString);
                } else if ("help".equals(inputLine)) {
                    printWriter.println("Commands list: stop sleep wake exit/quit savePage refreshPage getNewSsoId writeObjects findMarketTypes printMarket printEvent printMarketType weightTest printMap debug alias reserve cancelOrders readAliases " +
                                        "printAliases printCachedMarkets printCachedOrders safetyLimits addManagedRunner help");
                } else {
                    printWriter.println("Unknown command: " + inputLine + " . Use \"help\" for a commands list.");
                }

                if (newOrder != null) {
                    final String orderToSet;
                    final String existingOrder;
                    final String existingOrderAfter;
                    synchronized (Statics.orderToPrint) {
                        existingOrder = Statics.orderToPrint.get();
                        orderToSet = existingOrder == null ? newOrder : existingOrder + " " + newOrder;
                        existingOrderAfter = Statics.orderToPrint.getAndSet(orderToSet);
                        if (!Objects.equals(existingOrder, existingOrderAfter)) {
                            logger.error("existingOrder differs from existingOrderAfter: {} - {}", existingOrder, existingOrderAfter);
                        }
                    } // end synchronized block
                } // end if

                inputLine = bufferedReader.readLine();
            } // end while
        } catch (IOException iOException) {
            if (Statics.mustStop.get()) { // program is stopping and the socket has been closed from another thread
            } else {
                logger.error("IOException in InputConnection thread", iOException);
            }
        } finally {
            //noinspection ConstantConditions
            Generic.closeObjects(printWriter, outputStream, bufferedReader, inputStreamReader, inputStream, this.socket);
        }
        logger.info("reached the end of inputConnectionThread");
    }
}
