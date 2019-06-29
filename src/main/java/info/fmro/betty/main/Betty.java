package info.fmro.betty.main;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import info.fmro.betty.entities.SessionToken;
import info.fmro.betty.objects.SessionTokenObject;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.client.ClientHandler;
import info.fmro.betty.utility.UncaughtExceptionHandler;
import info.fmro.shared.utility.Generic;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Betty {
    private static final Logger logger = LoggerFactory.getLogger(Betty.class);

    private Betty() {
    }

    public static void main(final String[] args) {
        Statics.standardStreamsList = Generic.replaceStandardStreams(Statics.STDOUT_FILE_NAME, Statics.STDERR_FILE_NAME, Statics.LOGS_FOLDER_NAME, !Statics.closeStandardStreamsNotInitialized);

        FileOutputStream outFileOutputStream = null, errFileOutputStream = null;
        PrintStream outPrintStream = null, errPrintStream = null;

        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        Generic.changeDefaultCharset(Generic.UTF8_CHARSET);

        try {
//            new File(Statics.LOGS_FOLDER_NAME).mkdirs(); // moved to Generic.replaceStandardStreams

            outFileOutputStream = (FileOutputStream) Statics.standardStreamsList.get(0);
            outPrintStream = (PrintStream) Statics.standardStreamsList.get(1);
            errFileOutputStream = (FileOutputStream) Statics.standardStreamsList.get(2);
            errPrintStream = (PrintStream) Statics.standardStreamsList.get(3);

            if (Statics.safeBetModuleActivated) {
                new File(Statics.betradarScraperThread.SAVE_FOLDER).mkdirs();
                new File(Statics.coralScraperThread.SAVE_FOLDER).mkdirs();
            }
            new File(Statics.DATA_FOLDER_NAME).mkdirs();

            int debugLevel = 0;
            for (final String arg : args) {
                if (arg.equals("-help")) {
                    logger.info("Available options:\n  -help             prints this help screen\n" +
                                "  -debug=[0|1|2|3]  writes debug info to disk (0 is default and writes nothing, 3 writes all)\n" +
                                "  -denyBetting      denies placing of bets");
                    return;
                } else if (arg.equals("-denyBetting")) {
                    Statics.denyBetting.set(true);
                    logger.warn("betting denied");
                } else if (arg.startsWith("-debug=")) {
                    debugLevel = Integer.parseInt(arg.substring(arg.indexOf("-debug=") + "-debug=".length()));
                } else {
                    logger.error("Bogus argument: {}", arg);
                }
            }
            Statics.debugLevel.setLevel(debugLevel);

            Generic.backupFiles(Statics.LOGS_FOLDER_NAME, Statics.SETTINGS_FILE_NAME); // backup before read, as read can, in some cases, overwrite the file

            if (!VarsIO.readSettings()) {
                logger.error("error reading settings, program will exit");
                return;
            } else { // settings have been read fine, program will run
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

            if (Statics.notPlacingOrders) {
                logger.error("Statics.notPlacingOrders true. The program will not place orders.");
            } else { // regular run, nothing ot be done
            }

            VarsIO.readVarsFromFile(Statics.VARS_FILE_NAME);
            VarsIO.readObjectsFromFiles();

            Statics.matcherSynchronizedWriter.initialize(Statics.MATCHER_FILE_NAME, true);
            Statics.safebetsSynchronizedWriter.initialize(Statics.SAFEBETS_FILE_NAME, true);
            Statics.newMarketSynchronizedWriter.initialize(Statics.NEWMARKET_FILE_NAME, true);

            if (Statics.programIsRunningMultiThreaded.getAndSet(true)) {
                logger.error("initial programMultithreaded state is true");
            }

            // threads only get started below this line

            final Thread loggerThread = new Thread(Statics.loggerThread);
            loggerThread.start();
            final InputServerThread inputServerThread = new InputServerThread();
            inputServerThread.start();
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
                betradarThread = new Thread();
            }
            final Thread coralThread;
            if (Statics.safeBetModuleActivated) {
                coralThread = new Thread(Statics.coralScraperThread);
                coralThread.start();
            } else {
                coralThread = new Thread();
            }
            Statics.quickCheckThread.start();
            final Thread cancelOrdersThread = new Thread(Statics.ordersThread);
            cancelOrdersThread.start();
            final Thread placedAmountsThread = new Thread(new PlacedAmountsThread());
            placedAmountsThread.start();
            final Thread timeJumpDetectorThread = new Thread(new TimeJumpDetectorThread());
            timeJumpDetectorThread.start();
            final Thread idleConnectionMonitorThread = new Thread(new IdleConnectionMonitorThread(Statics.connManager));
            idleConnectionMonitorThread.start();
            for (int i = 0; i < ClientHandler.streamClients.length; i++) {
                ClientHandler.streamClients[i].start();
            }
            Statics.rulesManager.start();

            if (Statics.safeBetModuleActivated) {
                if (!BrowserVersion.BEST_SUPPORTED.equals(BrowserVersion.FIREFOX_52)) {
                    // sometimes this changes if I use a new version of HtmlUnit
                    logger.error("HtmlUnit BrowserVersion.BEST_SUPPORTED has changed, a review of BrowserVersion usage is necessary");
                }
            }

//            ClientCommands.traceOrders();
//            ClientCommands.traceMarkets();

            while (!Statics.mustStop.get()) {
                try {
                    if (Statics.mustSleep.get()) {
                        programSleeps(Statics.mustSleep, Statics.mustStop, "betty main");
                    }
                    if (!Statics.mustStop.get()) {

                        // this is where the work is placed
                        Generic.threadSleep(100);
                    }
                } catch (Throwable throwable) { // safety net inside the loop
                    logger.error("STRANGE ERROR inside Betty loop", throwable);
                }
            } // end while

            synchronized (Statics.inputConnectionSocketsSet) {
                for (final Socket socket : Statics.inputConnectionSocketsSet) {
                    Generic.closeObject(socket);
                }
            } // end synchronized
            synchronized (Statics.inputServerSocketsSet) {
                for (final ServerSocket serverSocket : Statics.inputServerSocketsSet) {
                    Generic.closeObject(serverSocket);
                }
            } // end synchronized
            if (inputServerThread.isAlive()) {
                logger.info("joining inputServer thread");
                inputServerThread.join();
            }

            final Set<InputConnectionThread> inputConnectionThreadsSetCopy;
            synchronized (Statics.inputConnectionThreadsSet) {
                inputConnectionThreadsSetCopy = new HashSet<>(Statics.inputConnectionThreadsSet);
            } // end synchronized
            for (InputConnectionThread inputConnectionThread : inputConnectionThreadsSetCopy) {
                if (inputConnectionThread.isAlive()) {
                    logger.info("joining inputConnection");
                    inputConnectionThread.join();
                }
            } // end for

            if (getFundsThread.isAlive()) {
                logger.info("joining getFunds thread");
                getFundsThread.join();
            }
            if (getLiveMarketsThread.isAlive()) {
                logger.info("joining getLiveMarkets thread");
                getLiveMarketsThread.join();
            }
            if (betradarThread.isAlive()) {
                logger.info("joining betradarScraper thread");
                betradarThread.join();
            }
            if (coralThread.isAlive()) {
                logger.info("joining coralScraper thread");
                coralThread.join();
            }
            if (Statics.quickCheckThread.isAlive()) {
                logger.info("joining quickCheck thread");
                Statics.quickCheckThread.join();
            }
            if (Statics.rulesManager.isAlive()) {
                logger.info("joining rulesManager thread");
                Statics.rulesManager.join();
            }
            if (cancelOrdersThread.isAlive()) {
                logger.info("joining cancelOrdersThread");
                cancelOrdersThread.join();
            }
            if (placedAmountsThread.isAlive()) {
                logger.info("joining placedAmountsThread");
                placedAmountsThread.join();
            }
            if (timeJumpDetectorThread.isAlive()) {
                logger.info("joining timeJumpDetectorThread");
                timeJumpDetectorThread.join();
            }
            if (idleConnectionMonitorThread.isAlive()) {
                logger.info("joining idleConnectionMonitorThread");
                idleConnectionMonitorThread.join();
            }
            if (maintenanceThread.isAlive()) {
                logger.info("joining maintenance thread");
                maintenanceThread.join();
            }
            if (loggerThread.isAlive()) {
                logger.info("joining logger thread");
                loggerThread.join();
            }
            for (int i = 0; i < ClientHandler.streamClients.length; i++) {
                if (ClientHandler.streamClients[i].isAlive()) {
                    logger.info("joining streamClient {}", i);
                    ClientHandler.streamClients[i].join();
                }
            }

            Statics.threadPoolExecutor.shutdown();
            Statics.threadPoolExecutorMarketBooks.shutdown();
            Statics.threadPoolExecutorImportant.shutdown();

            if (!Statics.threadPoolExecutor.awaitTermination(10L, TimeUnit.MINUTES)) {
                logger.error("threadPoolExecutor hanged: {}", Statics.threadPoolExecutor.getActiveCount());
                final List<Runnable> runnableList = Statics.threadPoolExecutor.shutdownNow();
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
            if (!Statics.threadPoolExecutorImportant.awaitTermination(10L, TimeUnit.MINUTES)) {
                logger.error("threadPoolExecutorImportant hanged: {}", Statics.threadPoolExecutorImportant.getActiveCount());
                final List<Runnable> runnableList = Statics.threadPoolExecutorImportant.shutdownNow();
                if (!runnableList.isEmpty()) {
                    logger.error("threadPoolExecutorImportant not commenced: {}", runnableList.size());
                }
            }

            logger.info("closing HTTP Client");
            Statics.client.close();
            logger.info("closing connManager");
            Statics.connManager.close();
            logger.info("connManager shutdown");
            Statics.connManager.shutdown();

            logger.info("All threads finished");

            if (!Statics.programIsRunningMultiThreaded.getAndSet(false)) {
                logger.error("final programMultithreaded state is false");
            }

            VarsIO.writeObjectsToFiles();
            VarsIO.writeSettings();
            Generic.alreadyPrintedMap.clear(); // also prints the important properties
        } catch (FileNotFoundException | NumberFormatException | InterruptedException exception) {
            logger.error("STRANGE ERROR inside Betty", exception);
        } catch (Throwable throwable) { // attempts to catch fatal errors
            logger.error("EVEN STRANGER ERROR inside Betty", throwable);
        } finally {
            logger.info("Program ends"); // after this point, streams are getting closed, so logging might no longer work

            Generic.closeStandardStreams();

            Generic.closeObjects(outPrintStream, outFileOutputStream, errPrintStream, errFileOutputStream, Statics.matcherSynchronizedWriter, Statics.safebetsSynchronizedWriter, Statics.newMarketSynchronizedWriter);
        }
    }

    public static boolean authenticate(final String authURL, final AtomicReference<String> bu, final AtomicReference<String> bp, final SessionTokenObject sessionTokenObject, final String keyStoreFileName, final String keyStorePassword,
                                       final String keyStoreType, final AtomicReference<String> appKey) {
        boolean success = false;
        long beginTime = System.currentTimeMillis();
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        CloseableHttpClient closeableHttpClient = null;

        try {
            SSLContext sSLContext = SSLContext.getInstance("TLS");
            KeyManager[] keyManagers = getKeyManagers(keyStoreFileName, keyStorePassword, keyStoreType);

            sSLContext.init(keyManagers, null, new SecureRandom());
            SSLConnectionSocketFactory sSLConnectionSocketFactory = new SSLConnectionSocketFactory(sSLContext, new DefaultHostnameVerifier());

            httpClientBuilder.setSSLSocketFactory(sSLConnectionSocketFactory);
            closeableHttpClient = httpClientBuilder.build();
            HttpPost httpPost = new HttpPost(authURL);
            ArrayList<NameValuePair> nameValuePairArrayList = new ArrayList<>(2);

            BasicNameValuePair usernameBasicNameValuePair = new BasicNameValuePair("username", bu.get()), passwordBasicNameValuePair = new BasicNameValuePair("password", bp.get());
            nameValuePairArrayList.add(usernameBasicNameValuePair);
            nameValuePairArrayList.add(passwordBasicNameValuePair);

            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairArrayList);
            httpPost.setEntity(urlEncodedFormEntity);
            httpPost.setHeader("X-Application", appKey.get());

            logger.info("authenticate executing request: {}", httpPost.getRequestLine());

            int whileCounter = 0;
            while (Statics.needSessionToken.get() && !Statics.mustStop.get()) {
                whileCounter++;
                CloseableHttpResponse closeableHttpResponse = null;

                try {
                    closeableHttpResponse = closeableHttpClient.execute(httpPost);
                    final HttpEntity httpEntity = closeableHttpResponse.getEntity();

                    logger.info("authenticate response status line: {}", closeableHttpResponse.getStatusLine());
                    if (httpEntity != null) {
                        String responseString = EntityUtils.toString(httpEntity, Generic.UTF8_CHARSET);
                        logger.info("authenticate responseString: {}", responseString);

                        SessionToken sessionToken;
                        if (responseString.contains("Internal error")) {
                            logger.error("Internal error string detected during authentication");
                            sessionToken = null;
                        } else {
                            sessionToken = JsonConverter.convertFromJson(responseString, SessionToken.class);
                        }
                        logger.info("sessionToken: {}", Generic.objectToString(sessionToken));

                        if (sessionToken != null && sessionToken.getSessionTokenLength() > 0) {
                            sessionTokenObject.setSessionToken(sessionToken.getSessionToken());
//                            sessionTokenObject.timeStamp();
                            Statics.needSessionToken.set(false);
                            success = true;
                        } else {
                            logger.error("authentication failed, sessionToken: {}", Generic.objectToString(sessionToken));
                            Generic.threadSleep(500L); // main sleep is further down
                        }
                    } else {
                        logger.error("authentication failed, httpEntity null, status line: {} timeStamp={}", closeableHttpResponse.getStatusLine(), System.currentTimeMillis());
                    }
                } catch (IOException iOException) {
                    logger.error("iOException in authenticate", iOException);
                } finally {
                    Generic.closeObject(closeableHttpResponse);
                }

                if (Statics.needSessionToken.get() && !Statics.mustStop.get()) {
                    final long amountToSleep;
                    switch (whileCounter) {
                        case 0:
                            logger.error("whileCounter is {} in authenticate method", whileCounter);
                            amountToSleep = 10_000L;
                            break;
                        case 1:
                            amountToSleep = 1_000L;
                            break;
                        case 2:
                            amountToSleep = 2_000L;
                            break;
                        case 3:
                            amountToSleep = 5_000L;
                            break;
                        case 4:
                            amountToSleep = 6_000L;
                            break;
                        case 5:
                            amountToSleep = 8_000L;
                            break;
                        case 6:
                            amountToSleep = 10_000L;
                            break;
                        case 7:
                            amountToSleep = 12_000L;
                            break;
                        case 8:
                            amountToSleep = 15_000L;
                            break;
                        case 9:
                            amountToSleep = 20_000L;
                            break;
                        default:
                            logger.error("problems while authenticating, whileCounter {}", whileCounter);
                            amountToSleep = 30_000L;
                            break;
                    }
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

    public static KeyManager[] getKeyManagers(final String keyStoreFileName, final String keyStorePassword, final String keyStoreType) {
        File keyFile = new File(keyStoreFileName);
        FileInputStream keyStoreFileInputStream = null;
        KeyManagerFactory keyManagerFactory = null;

        try {
            keyStoreFileInputStream = new FileInputStream(keyFile);

            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(keyStoreFileInputStream, keyStorePassword.toCharArray());
            keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException | UnrecoverableKeyException exception) {
            logger.error("STRANGE ERROR inside getKeyManagers", exception);
        } finally {
            Generic.closeObject(keyStoreFileInputStream);
        }

        return keyManagerFactory == null ? null : keyManagerFactory.getKeyManagers();
    }

    public static void programSleeps(final AtomicBoolean mustSleep, final AtomicBoolean mustStop, final String id) {
        if (mustSleep.get()) {
            logger.info("{} sleeping ...", id);
            while (mustSleep.get() && !mustStop.get()) {
                Generic.threadSleep(100);
            }
            logger.info("{} woke up !", id);
        }
    }
}
