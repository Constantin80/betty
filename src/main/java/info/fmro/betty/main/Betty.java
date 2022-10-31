package info.fmro.betty.main;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.safebet.PlacedAmountsThread;
import info.fmro.betty.stream.ClientHandler;
import info.fmro.betty.threads.permanent.GetFundsThread;
import info.fmro.betty.threads.permanent.GetLiveMarketsThread;
import info.fmro.betty.threads.permanent.IdleConnectionMonitorThread;
import info.fmro.betty.threads.permanent.InputServerThread;
import info.fmro.betty.threads.permanent.InterfaceServerThread;
import info.fmro.betty.threads.permanent.MaintenanceThread;
import info.fmro.betty.threads.permanent.TimeJumpDetectorThread;
import info.fmro.betty.utility.BettyUncaughtExceptionHandler;
import info.fmro.shared.betapi.JsonConverter;
import info.fmro.shared.entities.SessionToken;
import info.fmro.shared.enums.ProgramName;
import info.fmro.shared.objects.SharedStatics;
import info.fmro.shared.utility.Generic;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("UtilityClass")
public final class Betty {
    private static final Logger logger = LoggerFactory.getLogger(Betty.class);

    @Contract(pure = true)
    private Betty() {
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "MethodWithMultipleReturnPoints"})
    public static void main(final String[] args) {
        SharedStatics.programName.set(ProgramName.SERVER);
//        System.out.println("fhadkflcnruw 12121 dsdasdase");
//        System.exit(0);
        Statics.standardStreamsList = Generic.replaceStandardStreams(Statics.STDOUT_FILE_NAME, Statics.STDERR_FILE_NAME, Statics.LOGS_FOLDER_NAME, !Statics.closeStandardStreamsNotInitialized);

        FileOutputStream outFileOutputStream = null, errFileOutputStream = null;
        PrintStream outPrintStream = null, errPrintStream = null;

        Thread.setDefaultUncaughtExceptionHandler(new BettyUncaughtExceptionHandler());
        Generic.changeDefaultCharset(Generic.UTF8_CHARSET);

        try {
//            new File(Statics.LOGS_FOLDER_NAME).mkdirs(); // moved to Generic.replaceStandardStreams

            outFileOutputStream = (FileOutputStream) Statics.standardStreamsList.get(0);
            outPrintStream = (PrintStream) Statics.standardStreamsList.get(1);
            errFileOutputStream = (FileOutputStream) Statics.standardStreamsList.get(2);
            errPrintStream = (PrintStream) Statics.standardStreamsList.get(3);

            if (Statics.safeBetModuleActivated) {
                //noinspection ResultOfMethodCallIgnored
                new File(Statics.betradarScraperThread.saveFolder).mkdirs();
                //noinspection ResultOfMethodCallIgnored
                new File(Statics.coralScraperThread.saveFolder).mkdirs();
            }
            //noinspection ResultOfMethodCallIgnored
            new File(Statics.DATA_FOLDER_NAME).mkdirs();

            int debugLevel = 0;
            for (final String arg : args) {
                if ("-help".equals(arg)) {
                    logger.info("Available options:\n  -help             prints this help screen\n" +
                                "  -debug=[0|1|2|3]  writes debug info to disk (0 is default and writes nothing, 3 writes all)\n" +
                                "  -denyBetting      denies placing of bets" +
                                "  -resetTestMarker      resets rulesManager settings and exits");
                    return;
                } else if ("-denyBetting".equals(arg)) {
                    SharedStatics.denyBetting.set(true);
                    logger.warn("betting denied");
                } else if ("-resetTestMarker".equals(arg)) {
                    SharedStatics.resetTestMarker.set(true);
                    logger.warn("rulesManager settings are being reset");
                } else if (arg.startsWith("-debug=")) {
                    debugLevel = Integer.parseInt(arg.substring(arg.indexOf("-debug=") + "-debug=".length()));
                } else {
                    logger.error("Bogus argument: {}", arg);
                }
            }
            Statics.debugLevel.setLevel(debugLevel);

            Generic.backupFiles(Statics.LOGS_FOLDER_NAME, Statics.SETTINGS_FILE_NAME); // backup before read, as read can, in some cases, overwrite the file

            if (VarsIO.readSettings()) { // settings have been read fine, program will run
            } else {
                logger.error("error reading settings, program will exit");
                return;
            }

            // if (Statics.debugger.getDebugLevel() >= 2) {
            //     Statics.debugger.addWriter(Statics.DEBUG_VARS_FILE_NAME, false, 2);
            //     if (Statics.debugger.getDebugLevel() >= 3) {
            //         Statics.debugger.addWriter(Statics.DEBUG_WEB_FILE_NAME, false, 3);
            //     } else { // debugLevel must be 2, no need to do anything else
            //     }
            // } else { // debugLevel < 2, no writer to open
            // }
            Generic.disableHTTPSValidation();
            Generic.turnOffHtmlUnitLogger();

            if (SharedStatics.notPlacingOrders) {
                logger.error("Statics.notPlacingOrders true. The program will not place orders.");
            } else { // regular run, nothing to be done
            }
            if (SharedStatics.reserveCanDecrease) {
                logger.error("Statics.reserveCanDecrease true. Should be false, except for testing. Reserve protection is affected.");
            } else { // regular run, nothing to be done
            }
            if (SharedStatics.noReserve) {
                logger.error("Statics.noReserve true. Should be false, except for testing. Reserve protection removed.");
            } else { // regular run, nothing to be done
            }
            if (Statics.connectingToBetfairServersDisabled) {
                logger.error("Statics.connectingToBetfairServersDisabled true. Communication with Betfair server will be stopped.");
            } else { // regular run, nothing to be done
            }

            VarsIO.readVarsFromFile(Statics.VARS_FILE_NAME);
            VarsIO.readObjectsFromFiles();

            Statics.matcherSynchronizedWriter.initialize(Statics.MATCHER_FILE_NAME, true);
            Statics.safeBetsSynchronizedWriter.initialize(Statics.SAFE_BETS_FILE_NAME, true);
            Statics.newMarketSynchronizedWriter.initialize(Statics.NEW_MARKET_FILE_NAME, true);

            if (Statics.programIsRunningMultiThreaded.getAndSet(true)) {
                logger.error("initial programMultithreaded state is true");
            }

            // threads only get started below this line

            final Thread loggerThread = new Thread(Statics.loggerThread);
            loggerThread.start();
            final InputServerThread inputServerThread = new InputServerThread();
            inputServerThread.start();
            final InterfaceServerThread interfaceServerThread = new InterfaceServerThread();
            interfaceServerThread.start();
            final MaintenanceThread maintenanceThread = new MaintenanceThread();
            maintenanceThread.start();
            final GetFundsThread getFundsThread = new GetFundsThread();
            getFundsThread.start();
            final GetLiveMarketsThread getLiveMarketsThread = new GetLiveMarketsThread();
            getLiveMarketsThread.start();
            final Thread betradarThread;
            if (Statics.safeBetModuleActivated) {
                betradarThread = new Thread(Statics.betradarScraperThread);
                betradarThread.start();
            } else {
                //noinspection InstantiatingAThreadWithDefaultRunMethod
                betradarThread = new Thread();
            }
            final Thread coralThread;
            if (Statics.safeBetModuleActivated) {
                coralThread = new Thread(Statics.coralScraperThread);
                coralThread.start();
            } else {
                //noinspection InstantiatingAThreadWithDefaultRunMethod
                coralThread = new Thread();
            }

            if (Statics.safeBetModuleActivated) {
                Statics.quickCheckThread.start();
            }

            final Thread pendingOrdersThread = new Thread(Statics.pendingOrdersThread);
            pendingOrdersThread.start();

            final Thread placedAmountsThread = new Thread(new PlacedAmountsThread());
            if (Statics.safeBetModuleActivated) {
                placedAmountsThread.start();
            }

            final Thread timeJumpDetectorThread = new Thread(new TimeJumpDetectorThread());
            timeJumpDetectorThread.start();
            final Thread idleConnectionMonitorThread = new Thread(new IdleConnectionMonitorThread(Statics.connManager));
            idleConnectionMonitorThread.start();

            final int streamClientsLength = ClientHandler.streamClients.length;
            if (Statics.connectingToBetfairServersDisabled) { // won't start streamClients
            } else {
                for (int i = 0; i < streamClientsLength; i++) {
                    ClientHandler.streamClients[i].start();
                }
            }

            final Thread rulesManagerThread = new Thread(Statics.rulesManagerThread);
            rulesManagerThread.start();
//            Statics.rulesManager.start();

            if (Statics.safeBetModuleActivated) {
                if (!BrowserVersion.BEST_SUPPORTED.equals(BrowserVersion.FIREFOX_78)) {
                    // sometimes this changes if I use a new version of HtmlUnit
                    logger.error("HtmlUnit BrowserVersion.BEST_SUPPORTED has changed, a review of BrowserVersion usage is necessary");
                }
            }

//            ClientCommands.traceOrders();
//            ClientCommands.traceMarkets();

            while (!SharedStatics.mustStop.get()) {
                //noinspection NestedTryStatement
                try {
                    if (Statics.mustSleep.get()) {
                        programSleeps(Statics.mustSleep, "betty main");
                    }
                    if (!SharedStatics.mustStop.get()) {

                        // this is where the work is placed
                        Generic.threadSleep(100);
                    }
                } catch (Throwable throwable) { // safety net inside the loop
                    logger.error("STRANGE ERROR inside Betty loop", throwable);
                }
            } // end while

            if (inputServerThread.isAlive()) {
                inputServerThread.closeSocket();
                logger.debug("joining inputServer thread");
                inputServerThread.join();
            }
            if (interfaceServerThread.isAlive()) {
                interfaceServerThread.closeSocket();
                logger.debug("joining interfaceServer thread");
                interfaceServerThread.join();
            }

            if (getFundsThread.isAlive()) {
                logger.debug("joining getFunds thread");
                getFundsThread.join();
            }
            if (getLiveMarketsThread.isAlive()) {
                logger.debug("joining getLiveMarkets thread");
                getLiveMarketsThread.join();
            }
            if (betradarThread.isAlive()) {
                logger.debug("joining betradarScraper thread");
                betradarThread.join();
            }
            if (coralThread.isAlive()) {
                logger.debug("joining coralScraper thread");
                coralThread.join();
            }
            if (Statics.safeBetModuleActivated) {
                if (Statics.quickCheckThread.isAlive()) {
                    logger.debug("joining quickCheck thread");
                    Statics.quickCheckThread.join();
                }
            }
            if (rulesManagerThread.isAlive()) {
                logger.debug("joining rulesManager thread");
                rulesManagerThread.join();
            }
            if (pendingOrdersThread.isAlive()) {
                logger.debug("joining pendingOrdersThread");
                pendingOrdersThread.join();
            }
            if (Statics.safeBetModuleActivated) {
                if (placedAmountsThread.isAlive()) {
                    logger.debug("joining placedAmountsThread");
                    placedAmountsThread.join();
                }
            }
            if (timeJumpDetectorThread.isAlive()) {
                logger.debug("joining timeJumpDetectorThread");
                timeJumpDetectorThread.join();
            }
            if (idleConnectionMonitorThread.isAlive()) {
                logger.debug("joining idleConnectionMonitorThread");
                idleConnectionMonitorThread.join();
            }
            if (maintenanceThread.isAlive()) {
                logger.debug("joining maintenance thread");
                maintenanceThread.join();
            }
            if (loggerThread.isAlive()) {
                logger.debug("joining logger thread");
                loggerThread.join();
            }
            for (int i = 0; i < streamClientsLength; i++) {
                if (ClientHandler.streamClients[i].isAlive()) {
                    logger.debug("joining streamClient {}", i);
                    ClientHandler.streamClients[i].join();
                }
            }

            SharedStatics.threadPoolExecutor.shutdown();
            Statics.threadPoolExecutorMarketBooks.shutdown();
            SharedStatics.threadPoolExecutorImportant.shutdown();

            if (!SharedStatics.threadPoolExecutor.awaitTermination(10L, TimeUnit.MINUTES)) {
                logger.error("threadPoolExecutor hanged: {}", SharedStatics.threadPoolExecutor.getActiveCount());
                final List<Runnable> runnableList = SharedStatics.threadPoolExecutor.shutdownNow();
                if (!runnableList.isEmpty()) {
                    logger.error("threadPoolExecutor not commenced: {}", runnableList.size());
                }
            }
            if (!Statics.threadPoolExecutorMarketBooks.awaitTermination(10L, TimeUnit.MINUTES)) {
                logger.error("threadPoolExecutorMarketBooks hanged: {}", Statics.threadPoolExecutorMarketBooks.getActiveCount());
                final List<Runnable> runnableList = Statics.threadPoolExecutorMarketBooks.shutdownNow();
                if (!runnableList.isEmpty()) {
                    logger.error("threadPoolExecutorMarketBooks not commenced: {}", runnableList.size());
                }
            }
            if (!SharedStatics.threadPoolExecutorImportant.awaitTermination(10L, TimeUnit.MINUTES)) {
                logger.error("threadPoolExecutorImportant hanged: {}", SharedStatics.threadPoolExecutorImportant.getActiveCount());
                final List<Runnable> runnableList = SharedStatics.threadPoolExecutorImportant.shutdownNow();
                if (!runnableList.isEmpty()) {
                    logger.error("threadPoolExecutorImportant not commenced: {}", runnableList.size());
                }
            }

            logger.debug("closing HTTP Client");
            Statics.client.close();
            logger.debug("closing connManager");
            Statics.connManager.close();
            logger.debug("connManager shutdown");
            Statics.connManager.shutdown();

            logger.debug("All threads finished");

            if (!Statics.programIsRunningMultiThreaded.getAndSet(false)) {
                logger.error("final programMultithreaded state is false");
            }

            VarsIO.writeObjectsToFiles();
            VarsIO.writeSettings();
            SharedStatics.alreadyPrintedMap.clear(); // also prints the important properties
        } catch (FileNotFoundException | NumberFormatException | InterruptedException exception) {
            logger.error("STRANGE ERROR inside Betty", exception);
        } catch (@SuppressWarnings("OverlyBroadCatchBlock") Throwable throwable) { // attempts to catch fatal errors
            logger.error("EVEN STRANGER ERROR inside Betty", throwable);
        } finally {
            logger.info("Program ends"); // after this point, streams are getting closed, so logging might no longer work

            Generic.closeStandardStreams();

            //noinspection ConstantConditions
            Generic.closeObjects(outPrintStream, outFileOutputStream, errPrintStream, errFileOutputStream, Statics.matcherSynchronizedWriter, Statics.safeBetsSynchronizedWriter, Statics.newMarketSynchronizedWriter);
        }
    }

    @SuppressWarnings({"OverlyLongMethod", "UnusedReturnValue"})
    public static boolean authenticate(final String authURL, @NotNull final AtomicReference<String> bu, @NotNull final AtomicReference<String> bp, final String keyStoreFileName, final String keyStorePassword, final String keyStoreType) {
        boolean success = false;
        final long beginTime = System.currentTimeMillis();
        final HttpClientBuilder httpClientBuilder = HttpClients.custom();
        CloseableHttpClient closeableHttpClient = null;

        try {
            final SSLContext sSLContext = SSLContext.getInstance("TLS");
            final KeyManager[] keyManagers = Generic.getKeyManagers(keyStoreFileName, keyStorePassword, keyStoreType);

            sSLContext.init(keyManagers, null, new SecureRandom());
            final LayeredConnectionSocketFactory sSLConnectionSocketFactory = new SSLConnectionSocketFactory(sSLContext, new DefaultHostnameVerifier());

            httpClientBuilder.setSSLSocketFactory(sSLConnectionSocketFactory);
            //noinspection resource
            closeableHttpClient = httpClientBuilder.build();
            final HttpPost httpPost = new HttpPost(authURL);
            final ArrayList<NameValuePair> nameValuePairArrayList = new ArrayList<>(2);

            final NameValuePair usernameBasicNameValuePair = new BasicNameValuePair("username", bu.get());
            final NameValuePair passwordBasicNameValuePair = new BasicNameValuePair("password", bp.get());
            nameValuePairArrayList.add(usernameBasicNameValuePair);
            nameValuePairArrayList.add(passwordBasicNameValuePair);

            final HttpEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairArrayList);
            httpPost.setEntity(urlEncodedFormEntity);
            httpPost.setHeader("X-Application", SharedStatics.appKey.get());

            logger.debug("authenticate executing request: {}", httpPost.getRequestLine());

            int whileCounter = 0;
            while (SharedStatics.needSessionToken.get() && !SharedStatics.mustStop.get()) {
                whileCounter++;
                CloseableHttpResponse closeableHttpResponse = null;

                //noinspection NestedTryStatement
                try {
                    //noinspection resource
                    closeableHttpResponse = closeableHttpClient.execute(httpPost);
                    final HttpEntity httpEntity = closeableHttpResponse.getEntity();

                    logger.debug("authenticate response status line: {}", closeableHttpResponse.getStatusLine());
                    if (httpEntity != null) {
                        final String responseString = EntityUtils.toString(httpEntity, Generic.UTF8_CHARSET);
                        logger.debug("authenticate responseString: {}", responseString);

                        @Nullable final SessionToken sessionToken;
                        if (responseString.contains("Internal error")) {
                            logger.error("Internal error string detected during authentication");
                            sessionToken = null;
                        } else {
                            sessionToken = JsonConverter.convertFromJson(responseString, SessionToken.class);
                        }
                        logger.info("sessionToken: {}", Generic.objectToString(sessionToken));

                        if (sessionToken != null && sessionToken.getSessionTokenLength() > 0) {
                            SharedStatics.sessionTokenObject.setSessionToken(sessionToken.getSessionToken());
//                            sessionTokenObject.timeStamp();
                            SharedStatics.needSessionToken.set(false);
                            success = true;
                        } else {
                            logger.error("authentication failed, sessionToken: {}", Generic.objectToString(sessionToken));
                            Generic.threadSleep(500L); // main sleep is further down
                        }
                    } else {
                        logger.error("authentication failed, httpEntity null, status line: {} timeStamp={}", closeableHttpResponse.getStatusLine(), System.currentTimeMillis());
                    }
                } catch (@SuppressWarnings("OverlyBroadCatchBlock") IOException iOException) {
                    logger.error("iOException in authenticate", iOException);
                } finally {
                    Generic.closeObject(closeableHttpResponse);
                }

                if (SharedStatics.needSessionToken.get() && !SharedStatics.mustStop.get()) {
                    final long amountToSleep = switch (whileCounter) {
                        case 0 -> {
                            logger.error("whileCounter is {} in authenticate method", whileCounter);
                            yield 10_000;
                        }
                        case 1 -> 1_000L;
                        case 2 -> 2_000L;
                        case 3 -> 5_000L;
                        case 4 -> 6_000L;
                        case 5 -> 8_000L;
                        case 6 -> 10_000L;
                        case 7 -> 12_000L;
                        case 8 -> 15_000L;
                        case 9 -> 20_000L;
                        default -> {
                            logger.error("problems while authenticating, whileCounter {}", whileCounter);
                            yield 30_000;
                        }
                    };
                    Generic.threadSleep(amountToSleep);
                } else { // successful authentication, nothing else to be done
                }
            } // end while
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            logger.error("unsupportedEncodingException in authenticate", unsupportedEncodingException);
            Generic.threadSleep(1000L);
        } catch (NoSuchAlgorithmException | KeyManagementException exception) {
            logger.error("STRANGE ERROR in authenticate", exception);
            Generic.threadSleep(1000L);
        } finally {
            Generic.closeObject(closeableHttpClient);
        }
        logger.info("authentication took {} ms", System.currentTimeMillis() - beginTime);

        return success;
    }

    public static void programSleeps(@NotNull final AtomicBoolean mustSleep, final String id) {
        if (mustSleep.get()) {
            logger.info("{} sleeping ...", id);
            while (mustSleep.get() && !SharedStatics.mustStop.get()) {
                Generic.threadSleep(100);
            }
            logger.info("{} woke up !", id);
        }
    }
}
