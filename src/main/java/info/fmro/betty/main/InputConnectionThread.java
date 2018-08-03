package info.fmro.betty.main;

import info.fmro.betty.enums.CommandType;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
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
import java.util.Objects;

public class InputConnectionThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InputConnectionThread.class);
    private final Socket socket;

    InputConnectionThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        OutputStream outputStream = null;
        PrintWriter printWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

        try {
            outputStream = socket.getOutputStream();
            printWriter = new PrintWriter(outputStream, true);
            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String inputLine = bufferedReader.readLine();
            while (inputLine != null && !Statics.mustStop.get()) {
                String newOrder = null;
                inputLine = inputLine.toLowerCase();
                if (inputLine.equals("exit") || inputLine.equals("quit")) {
                    printWriter.println("Bye");
                    break;
                } else if (inputLine.equals("stop")) {
                    Statics.mustStop.set(true);
                    logger.info("Stop command executed");
                    printWriter.println("Stopping program");
                } else if (inputLine.equals("sleep")) {
                    if (Statics.mustSleep.get()) {
                        printWriter.println("Program already sleeping");
                    } else {
                        Statics.mustSleep.set(true);
                        logger.info("Sleep command executed");
                        printWriter.println("Program starting sleep");
                    }
                } else if (inputLine.equals("wake")) {
                    if (!Statics.mustSleep.get()) {
                        printWriter.println("Program already awake");
                    } else {
                        Statics.mustSleep.set(false);
                        logger.info("Wake command executed");
                        printWriter.println("Waking the program up");
                    }
                } else if (inputLine.equals("denyBetting")) {
                    if (Statics.denyBetting.get()) {
                        printWriter.println("denyBetting already active");
                    } else {
                        Statics.denyBetting.set(true);
                        logger.warn("denyBetting command executed");
                        printWriter.println("denyBetting activated");
                    }
                } else if (inputLine.equals("startBetting")) {
                    if (!Statics.denyBetting.get()) {
                        printWriter.println("denyBetting already inactive");
                    } else {
                        Statics.denyBetting.set(false);
                        logger.warn("startBetting command executed");
                        printWriter.println("denyBetting inactivated");
                    }
                } else if (inputLine.startsWith("savepage ")) {
                    String command = inputLine.substring("savepage ".length()).trim();
                    boolean success;
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
                            printWriter.println("unknown savepage command: " + command);
                            success = false;
                            break;
                    } // end switch
                    if (success) {
                        logger.info("Savepage {} command executed", command);
                        printWriter.println("Will save " + command + " web page");
                    }
                } else if (inputLine.startsWith("refreshpage ")) {
                    String command = inputLine.substring("refreshpage ".length()).trim();
                    boolean success;
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
                            printWriter.println("unknown refreshpage command: " + command);
                            success = false;
                            break;
                    } // end switch
                    if (success) {
                        logger.info("Refreshpage {} command executed", command);
                        printWriter.println("Will refresh " + command + " web page");
                    }
                } else if (inputLine.equals("getnewssoid")) {
                    Statics.needSessionToken.set(true);
                    logger.info("Getnewssoid command executed");
                    printWriter.println("Will get new ssoid");
                } else if (inputLine.equals("writeobjects")) {
                    Statics.mustWriteObjects.set(true);
                    logger.info("Writeobjects command executed");
                    printWriter.println("Will write objects to disk");
                } else if (inputLine.equals("findmarkettypes")) {
                    printWriter.println("Will find new market types");
                    newOrder = "findmarkettypes";
                } else if (inputLine.startsWith("printmarket ")) {
                    String marketId = inputLine.substring("printmarket ".length()).trim();
                    if (marketId.startsWith("=")) {
                        marketId = marketId.substring("=".length()).trim();
                    }
                    printWriter.println("Will print marketId: " + marketId);
                    newOrder = "market:" + marketId;
                } else if (inputLine.startsWith("printevent ")) {
                    String eventId = inputLine.substring("printevent ".length()).trim();
                    if (eventId.startsWith("=")) {
                        eventId = eventId.substring("=".length()).trim();
                    }
                    printWriter.println("Will print eventId: " + eventId);
                    newOrder = "event:" + eventId;
                } else if (inputLine.startsWith("printmarkettype ")) {
                    String marketType = inputLine.substring("printmarkettype ".length()).trim();
                    if (marketType.startsWith("=")) {
                        marketType = marketType.substring("=".length()).trim();
                    }
                    marketType = marketType.toUpperCase(); // they're always uppercase
                    printWriter.println("Will print marketType: " + marketType);
                    newOrder = "markettype:" + marketType;
                } else if (inputLine.equals("weighttest")) {
                    printWriter.println("Will run weightTest");
                    newOrder = "weighttest";
                } else if (inputLine.startsWith("printmap ")) {
                    String mapString = inputLine.substring("printmap ".length()).trim();
                    if (mapString.startsWith("betradareventsmap ")) {
                        String mapCommandString = mapString.substring("betradareventsmap ".length()).trim();
                        if (mapCommandString.equals("keys")) {
                            logger.info("betradarEventsMap keys: {}", Statics.betradarEventsMap.keySetCopy());
                        } else {
                            try {
                                logger.info("betradarEventsMap key {} : {}", mapCommandString, Generic.objectToString(Statics.betradarEventsMap.get(Long.valueOf(mapCommandString))));
                            } catch (NumberFormatException numberFormatException) {
                                logger.error("numberFormatException while parsing betradarEventsMap command: {}", mapCommandString, numberFormatException);
                            }
                        }
                        printWriter.println("Executed betradarEventsMap command");
                    } else if (mapString.startsWith("coraleventsmap ")) {
                        String mapCommandString = mapString.substring("coraleventsmap ".length()).trim();
                        if (mapCommandString.equals("keys")) {
                            logger.info("coralEventsMap keys: {}", Statics.coralEventsMap.keySetCopy());
                        } else {
                            try {
                                logger.info("coralEventsMap key {} : {}", mapCommandString, Generic.objectToString(Statics.coralEventsMap.get(Long.valueOf(mapCommandString))));
                            } catch (NumberFormatException numberFormatException) {
                                logger.error("numberFormatException while parsing coralEventsMap command: {}", mapCommandString, numberFormatException);
                            }
                        }
                        printWriter.println("Executed coralEventsMap command");
                    } else if (mapString.startsWith("eventsmap ")) {
                        String mapCommandString = mapString.substring("eventsmap ".length()).trim();
                        if (mapCommandString.equals("keys")) {
                            logger.info("eventsMap keys: {}", Statics.eventsMap.keySetCopy());
                        } else {
                            logger.info("eventsMap key {} : {}", mapCommandString, Generic.objectToString(Statics.eventsMap.get(mapCommandString)));
                        }
                        printWriter.println("Executed eventsMap command");
                    } else if (mapString.startsWith("marketcataloguesmap ")) {
                        String mapCommandString = mapString.substring("marketcataloguesmap ".length()).trim();
                        if (mapCommandString.equals("keys")) {
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
                    } else if (mapString.startsWith("safemarketsmap ")) {
                        String mapCommandString = mapString.substring("safemarketsmap ".length()).trim();
                        if (mapCommandString.equals("keys")) {
                            logger.info("safeMarketsMap keys: {}", Statics.safeMarketsMap.keySetCopy());
                        } else {
                            logger.info("safeMarketsMap key {} : {}", mapCommandString, Generic.objectToString(Statics.safeMarketsMap.get(mapCommandString)));
                        }
                        printWriter.println("Executed safeMarketsMap command");
                    } else if (mapString.startsWith("safemarketbooksmap ")) {
                        String mapCommandString = mapString.substring("safemarketbooksmap ".length()).trim();
                        if (mapCommandString.equals("keys")) {
                            logger.info("safeMarketBooksMap keys: {}", Statics.safeMarketBooksMap.keySetCopy());
                        } else {
                            logger.info("safeMarketBooksMap key {} : {}", mapCommandString, Generic.objectToString(Statics.safeMarketBooksMap.get(mapCommandString)));
                        }
                        printWriter.println("Executed safeMarketBooksMap command");
                    } else {
                        printWriter.println("Unknown mapString: " + mapString);
                    }
                } else if (inputLine.startsWith("debug ")) {
                    String debugString = inputLine.substring("debug ".length()).trim();
                    if (debugString.startsWith("level ")) {
                        String debugCommandString = debugString.substring("level ".length()).trim();
                        try {
                            int newLevel = Integer.parseInt(debugCommandString);

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
                        String debugCommandString = debugString.substring("add ".length()).trim();
                        try {
                            int code = Integer.parseInt(debugCommandString);

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
                        String debugCommandString = debugString.substring("remove ".length()).trim();
                        try {
                            int code = Integer.parseInt(debugCommandString);

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
                    } else if (debugString.equals("clear")) {
                        Statics.debugLevel.clear();
                        logger.info("debugLevel clear");
                        printWriter.println("Executed debugLevel clear command");
                    } else if (debugString.equals("print")) {
                        logger.info("debugLevel: {}", Generic.objectToString(Statics.debugLevel));
                        printWriter.println("Executed debugLevel print command");
                    } else {
                        printWriter.println("Unknown debugString: " + debugString);
                    }
                } else if (inputLine.startsWith("alias ")) {
                    String aliasCommandString = inputLine.substring("alias ".length()).trim(), aliasFirst = null, aliasSecond = null;
                    if (aliasCommandString.startsWith("\"") && aliasCommandString.indexOf('"', aliasCommandString.indexOf('"') + "\"".length()) > 0) {
                        aliasFirst = aliasCommandString.substring(aliasCommandString.indexOf('"') + "\"".length(),
                                                                  aliasCommandString.indexOf('"', aliasCommandString.indexOf('"') + "\"".length()));
                        aliasSecond = aliasCommandString.substring(aliasCommandString.indexOf('"', aliasCommandString.indexOf('"') + "\"".length()) + "\"".length()).trim();
                        if (aliasSecond.startsWith("\"") && aliasSecond.indexOf('"', aliasSecond.indexOf('"') + "\"".length()) > 0) {
                            aliasSecond = aliasSecond.substring(aliasSecond.indexOf('"') + "\"".length(), aliasSecond.indexOf('"', aliasSecond.indexOf('"') + "\"".length()));
                        }
                    } else if (aliasCommandString.contains(" ")) {
                        aliasFirst = aliasCommandString.substring(0, aliasCommandString.indexOf(' '));
                        aliasSecond = aliasCommandString.substring(aliasCommandString.indexOf(' ') + " ".length()).trim();
                        if (aliasSecond.startsWith("\"") && aliasSecond.indexOf('"', aliasSecond.indexOf('"') + "\"".length()) > 0) {
                            aliasSecond = aliasSecond.substring(aliasSecond.indexOf('"') + "\"".length(), aliasSecond.indexOf('"', aliasSecond.indexOf('"') + "\"".length()));
                        }
                    } else {
                        printWriter.println("unknown alias command: " + aliasCommandString);
                    }
                    if (aliasFirst != null && aliasSecond != null && !aliasFirst.isEmpty() && !aliasSecond.isEmpty()) {
                        String printedString = MessageFormatter.arrayFormat("Will add alias \"{}\" to \"{}\"", new Object[]{aliasFirst, aliasSecond}).getMessage();
                        printWriter.println(printedString);
                        logger.info(printedString);
                        String previousValue = Formulas.aliasesMap.put(aliasFirst, aliasSecond);
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
                    String reserveCommandString = inputLine.substring("reserve ".length()).trim();
                    if (reserveCommandString.startsWith("get")) {
                        logger.info("current safety reserve is: {}", Statics.safetyLimits.getReserve());
                        printWriter.println("current safety reserve is: " + Statics.safetyLimits.getReserve());
                    } else if (reserveCommandString.startsWith("set ")) {
                        try {
                            double newReserve = Double.parseDouble(reserveCommandString.substring("set ".length()).trim());

                            if (Statics.safetyLimits.setReserve(newReserve)) {
                                printWriter.println("have set new reserve: " + Statics.safetyLimits.getReserve());
                                logger.info("have set new reserve: {}", Statics.safetyLimits.getReserve());
                            } else {
                                printWriter.println("new reserve " + newReserve + " not set: " + Statics.safetyLimits.getReserve());
                            }
                        } catch (NumberFormatException numberFormatException) {
                            printWriter.println("numberFormatException: " + reserveCommandString);
                            logger.error("numberFormatException for reserveCommandString: {}", reserveCommandString, numberFormatException);
                        }
                    } else {
                        printWriter.println("Unknown reserve command: " + reserveCommandString);
                    }
                } else if (inputLine.equals("cancelorders")) {
                    printWriter.println("Will cancel all orders");
                    newOrder = "cancelorders";
                } else if (inputLine.equals("readaliases")) {
                    printWriter.println("Will read aliases from files");
                    logger.info("executing command to read aliases from files");
                    VarsIO.checkAliasesFile(Statics.ALIASES_FILE_NAME, Statics.aliasesTimeStamp, Formulas.aliasesMap);
                    VarsIO.checkAliasesFile(Statics.FULL_ALIASES_FILE_NAME, Statics.fullAliasesTimeStamp, Formulas.fullAliasesMap);
                } else if (inputLine.equals("printaliases")) {
                    printWriter.println("Will print aliases maps");
                    logger.info("aliasesMap: {}", Generic.objectToString(Formulas.aliasesMap));
                    logger.info("fullAliasesMap: {}", Generic.objectToString(Formulas.fullAliasesMap));
                } else if (inputLine.equals("safetylimits")) {
                    printWriter.println("Will print placed amounts");
                    logger.info("safetylimits: {}", Generic.objectToString(Statics.safetyLimits));
                } else if (inputLine.equals("help")) {
                    printWriter.println("Commands list: stop sleep wake exit/quit savepage refreshpage getnewssoid writeobjects findmarkettypes printmarket printevent " +
                                        "printmarkettype weighttest printmap debug alias reserve cancelorders readaliases printaliases safetylimits help");
                } else {
                    printWriter.println("Unknown command: " + inputLine + " . Use \"help\" for a commands list.");
                }

                if (newOrder != null) {
                    String orderToSet, existingOrder, existingOrderAfter;
                    synchronized (Statics.orderToPrint) {
                        existingOrder = Statics.orderToPrint.get();

                        if (existingOrder == null) {
                            orderToSet = newOrder;
                        } else {
                            orderToSet = existingOrder + " " + newOrder;
                        }
                        existingOrderAfter = Statics.orderToPrint.getAndSet(orderToSet);
                        if ((existingOrder == null && existingOrderAfter != null) || (existingOrder != null && !existingOrder.equals(existingOrderAfter))) {
                            logger.error("existingOrder differs from existingOrderAfter: {} - {}", existingOrder, existingOrderAfter);
                        }
                    } // end synchronized block
                } // end if

                inputLine = bufferedReader.readLine();
            } // end while
        } catch (IOException iOException) {
            if (!Statics.mustStop.get()) {
                logger.error("IOException in InputConnection thread", iOException);
            } else { // program is stopping and the socket has been closed from another thread
            }
        } finally {
//            logger.info("closing input connection socket");
            Generic.closeObjects(printWriter, outputStream, bufferedReader, inputStreamReader, inputStream, socket);
//            logger.info("input connection socket was closed");

            Statics.inputConnectionSocketsSet.remove(socket);
            Statics.inputConnectionThreadsSet.remove(this);
        }
        logger.info("reached the end of inputConnectionThread");
    }
}
