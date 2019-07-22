package info.fmro.betty.objects;

import info.fmro.betty.entities.Event;
import info.fmro.betty.entities.MarketBook;
import info.fmro.betty.entities.MarketCatalogue;
import info.fmro.betty.enums.ParsedMarketType;
import info.fmro.betty.logic.RulesManager;
import info.fmro.betty.logic.SafetyLimits;
import info.fmro.betty.safebet.BetradarEvent;
import info.fmro.betty.safebet.BetradarScraperThread;
import info.fmro.betty.safebet.CoralEvent;
import info.fmro.betty.safebet.CoralScraperThread;
import info.fmro.betty.safebet.SafeBet;
import info.fmro.betty.safebet.SafeBetsMap;
import info.fmro.betty.safebet.SafeRunner;
import info.fmro.betty.safebet.ScraperEvent;
import info.fmro.betty.safebet.ScraperPermanentThread;
import info.fmro.betty.stream.cache.market.MarketCache;
import info.fmro.betty.stream.cache.order.OrderCache;
import info.fmro.betty.threads.permanent.InputConnectionThread;
import info.fmro.betty.threads.permanent.InterfaceConnectionThread;
import info.fmro.betty.threads.permanent.LoggerThread;
import info.fmro.betty.threads.permanent.PendingOrdersThread;
import info.fmro.betty.threads.permanent.QuickCheckThread;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.SynchronizedMap;
import info.fmro.shared.utility.SynchronizedSafeSet;
import info.fmro.shared.utility.SynchronizedWriter;
import org.apache.commons.collections.comparators.NullComparator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"OverlyCoupledClass", "UtilityClass"})
public final class Statics {
    private static final Logger logger = LoggerFactory.getLogger(Statics.class);
    public static final boolean notPlacingOrders = true; // hard stop for order placing; true for testing, false enables order placing
    public static final boolean safeBetModuleActivated = false; // false for now, I won't be using this module for quite a while
    public static final boolean resetTestMarker = false; // when true, resets rulesManager testMarker variable, and then exits the program
    @SuppressWarnings("unchecked")
    public static final Comparator<Object> nullComparator = new NullComparator(false);
    public static final double threshold = .8d, highThreshold = .9d;
    public static final int SOCKET_CONNECT_TIMEOUT = 30_000, SOCKET_READ_TIMEOUT = 30_000, PAGE_GET_TIMEOUT = 120_000;
    public static final int ENCRYPTION_KEY = 0, DECRYPTION_KEY = 2, N_BEST = 100, N_ALL = 11, N_MARKET_BOOK_THREADS_LIMIT = 50, MIN_MATCHED = safeBetModuleActivated ? 2 : 0, SSL_PORT = 443, TEST_MARKER = 346562;
    public static final long DELAY_GET_MARKET_BOOKS = 200L, EXECUTOR_KEEP_ALIVE = 10_000L, DELAY_PRINT_AVERAGES = 20_000L, DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD = 1_000L, N_MARKET_BOOK_THREADS_INTERVAL = 2_000L,
            INITIAL_EVENT_SAFETY_PERIOD = Generic.MINUTE_LENGTH_MILLISECONDS, PROGRAM_START_TIME = System.currentTimeMillis(), MINIMUM_BAD_STUFF_HAPPENED_IGNORE = 2L * Generic.MINUTE_LENGTH_MILLISECONDS;
    public static final String SSO_HOST_RO = "identitysso-cert.betfair.ro", AUTH_URL = "https://" + SSO_HOST_RO + "/api/certlogin", KEY_STORE_TYPE = "pkcs12", PROJECT_PREFIX = "info.fmro";
    public static final String VARS_FILE_NAME = "input/vars.txt", STDOUT_FILE_NAME = "out.txt", STDERR_FILE_NAME = "err.txt", MATCHER_FILE_NAME = "matcher.txt", SAFE_BETS_FILE_NAME = "bets.txt", NEW_MARKET_FILE_NAME = "newMarket.txt",
            KEY_STORE_FILE_NAME = "input/client-2048.p12", KEY_STORE_PASSWORD = "", APING_URL = "https://api.betfair.com/exchange/betting/", RESCRIPT_SUFFIX = "rest/v1.0/", APPLICATION_JSON = "application/json", ACCOUNT_APING_URL =
            "https://api.betfair.com/exchange/account/", ALIASES_FILE_NAME = "input/aliases.txt", FULL_ALIASES_FILE_NAME = "input/aliasesFull.txt", LOGS_FOLDER_NAME = "logs", DATA_FOLDER_NAME = "data", STREAM_HOST = "stream-api.betfair.com",
            SETTINGS_FILE_NAME = Statics.DATA_FOLDER_NAME + "/rulesManager.txt", INTERFACE_KEY_STORE_FILE_NAME = "input/keystore";
    //    public static final List<Double> pricesList = List.of(1.01, 1.02,1.03,1.04,1.05,1.06,1.07,1.08,1.09,1.1,1.11,1.12,1.13,1.14 ...);
    @SuppressWarnings("PublicStaticCollectionField")
    public static final List<Integer> pricesList; // odds prices, multiplied by 100, to have them stored as int

    public static final List<String> supportedEventTypes = List.of("1"); // "1" = soccer

    //    public static AtomicBoolean closeStandardStreams = new AtomicBoolean(true); // modified by reflection for tests
    public static final boolean closeStandardStreamsNotInitialized = false; // modified by reflection for tests; can't initialize, as that inlines the value and it can no longer be modified; no longer modified in tests, now I initialize it
    @SuppressWarnings({"StaticVariableMayNotBeInitialized", "PublicStaticCollectionField", "StaticNonFinalField"})
    public static ArrayList<? extends OutputStream> standardStreamsList;
    //    public static final ArrayList<? extends OutputStream> standardStreamsList = Generic.replaceStandardStreams(STDOUT_FILE_NAME, STDERR_FILE_NAME, LOGS_FOLDER_NAME, !closeStandardStreamsNotInitialized); // negated boolean, as it's not initialized
    public static final Set<Class<? extends ScraperEvent>> scraperEventSubclassesSet = Set.copyOf(Generic.getSubclasses(PROJECT_PREFIX, ScraperEvent.class));
    public static final Set<Class<? extends ScraperPermanentThread>> scraperThreadSubclassesSet = Set.copyOf(Generic.getSubclasses(PROJECT_PREFIX, ScraperPermanentThread.class));
    public static final AtomicBoolean mustStop = new AtomicBoolean(), mustSleep = new AtomicBoolean(), needSessionToken = new AtomicBoolean(), mustWriteObjects = new AtomicBoolean(), fundsQuickRun = new AtomicBoolean(), denyBetting = new AtomicBoolean(),
            programIsRunningMultiThreaded = new AtomicBoolean();
    public static final AtomicInteger inputServerPort = new AtomicInteger(), interfaceServerPort = new AtomicInteger();
    public static final AtomicReference<String> appKey = new AtomicReference<>(), delayedAppKey = new AtomicReference<>(), bu = new AtomicReference<>(), bp = new AtomicReference<>(), orderToPrint = new AtomicReference<>(),
            interfaceKeyStorePassword = new AtomicReference<>();
    public static final AtomicLong timeLastFundsOp = new AtomicLong(), timeLastSaveToDisk = new AtomicLong(), aliasesTimeStamp = new AtomicLong(), fullAliasesTimeStamp = new AtomicLong();
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<ServerSocket> inputServerSocketsSet = Collections.synchronizedSet(new HashSet<>(2));
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<ServerSocket> interfaceServerSocketsSet = Collections.synchronizedSet(new HashSet<>(2));
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<Socket> inputConnectionSocketsSet = Collections.synchronizedSet(new HashSet<>(2));
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<Socket> interfaceConnectionSocketsSet = Collections.synchronizedSet(new HashSet<>(2));
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<InputConnectionThread> inputConnectionThreadsSet = Collections.synchronizedSet(new HashSet<>(2));
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<InterfaceConnectionThread> interfaceConnectionThreadsSet = Collections.synchronizedSet(new HashSet<>(2));
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Map<OrderPrice, Double> executingOrdersMap = Collections.synchronizedMap(new HashMap<>(2));
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<String> marketTypes = Collections.synchronizedSet(new HashSet<>(256));
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<String> marketNullTypes = Collections.synchronizedSet(new HashSet<>(16));
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Set<ParsedMarketType> marketsUnderTesting = Collections.synchronizedSet(EnumSet.noneOf(ParsedMarketType.class));
    public static final SynchronizedWriter matcherSynchronizedWriter = new SynchronizedWriter(ENCRYPTION_KEY);
    public static final SynchronizedWriter safeBetsSynchronizedWriter = new SynchronizedWriter(ENCRYPTION_KEY);
    public static final SynchronizedWriter newMarketSynchronizedWriter = new SynchronizedWriter(ENCRYPTION_KEY);
    @SuppressWarnings("PublicStaticCollectionField")
    public static final LinkedBlockingQueue<Runnable> linkedBlockingQueue = new LinkedBlockingQueue<>();
    public static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(64, 64, EXECUTOR_KEEP_ALIVE, TimeUnit.MILLISECONDS, linkedBlockingQueue);
    @SuppressWarnings("PublicStaticCollectionField")
    public static final LinkedBlockingQueue<Runnable> linkedBlockingQueueMarketBooks = new LinkedBlockingQueue<>();
    public static final ThreadPoolExecutor threadPoolExecutorMarketBooks = new ThreadPoolExecutor(512, 512, EXECUTOR_KEEP_ALIVE, TimeUnit.MILLISECONDS, linkedBlockingQueueMarketBooks);
    @SuppressWarnings("PublicStaticCollectionField")
    public static final LinkedBlockingQueue<Runnable> linkedBlockingQueueImportant = new LinkedBlockingQueue<>();
    public static final ThreadPoolExecutor threadPoolExecutorImportant = new ThreadPoolExecutor(64, 64, EXECUTOR_KEEP_ALIVE, TimeUnit.MILLISECONDS, linkedBlockingQueueImportant);
    @SuppressWarnings({"resource", "IOResourceOpenedButNotSafelyClosed"})
    public static final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
    public static final RequestConfig fastConfig = RequestConfig.custom().setConnectionRequestTimeout(10_000).setConnectTimeout(1_000).setSocketTimeout(1_000).build();
    public static final RequestConfig accountsApiConfig = RequestConfig.custom().setConnectionRequestTimeout(10_000).setConnectTimeout(1_000).setSocketTimeout(2_000).build();
    public static final RequestConfig placingOrdersConfig = RequestConfig.custom().setConnectionRequestTimeout(10_000).setConnectTimeout(1_000).setSocketTimeout(20_000).build();
    public static final SocketConfig socketConfig = SocketConfig.custom().setSoKeepAlive(true).setSoReuseAddress(true).setTcpNoDelay(true).build();
    @SuppressWarnings("resource")
    public static final CloseableHttpClient client = HttpClients.custom().setDefaultSocketConfig(socketConfig).setDefaultRequestConfig(placingOrdersConfig).setConnectionManager(connManager).build();
    public static final BetradarScraperThread betradarScraperThread = new BetradarScraperThread();
    public static final CoralScraperThread coralScraperThread = new CoralScraperThread();
    public static final LoggerThread loggerThread = new LoggerThread();
    public static final RulesManager rulesManager = new RulesManager();
    public static final QuickCheckThread quickCheckThread = new QuickCheckThread();
    public static final PendingOrdersThread pendingOrdersThread = new PendingOrdersThread();

    public static final TimeStamps timeStamps = new TimeStamps();
    public static final DebugLevel debugLevel = new DebugLevel();
    public static final SessionTokenObject sessionTokenObject = new SessionTokenObject();
    public static final SafetyLimits safetyLimits = new SafetyLimits();
//    public static final BlackList blackList = new BlackList();
//    public static final PlacedAmounts placedAmounts = new PlacedAmounts();

    public static final SynchronizedMap<Long, BetradarEvent> betradarEventsMap = new SynchronizedMap<>(128); // <scraperId, BetradarEvent>
    public static final SynchronizedMap<Long, CoralEvent> coralEventsMap = new SynchronizedMap<>(128); // <scraperId, CoralEvent>
    public static final SynchronizedMap<String, Event> eventsMap = new SynchronizedMap<>(128); // <eventId, Event>
    // marketCataloguesMap contains interestingMarkets; markets that can be parsed and have at least MIN_MATCHED scraperEvents attached; added to in FindMarkets
    public static final SynchronizedMap<String, MarketCatalogue> marketCataloguesMap = new SynchronizedMap<>(128); // <marketId, MarketCatalogue>

    // safeMarketsMap contains the safeRunners and marketIds of their markets; these markets are being quickly rechecked; added to in FindSafeRunners
    public static final SynchronizedMap<String, SynchronizedSafeSet<SafeRunner>> safeMarketsMap = new SynchronizedMap<>(64); // <marketId, HashSet<SafeRunner>>
    // safeMarketsImportantMap contains marketIds of markets with safeRunners that have available amounts on them; these are rechecked very quicly, until the safeBet dissapears; added to in QuickCheckThread
    public static final SynchronizedMap<String, Long> safeMarketsImportantMap = new SynchronizedMap<>(); // <marketId, Long timestamp>
    // safeMarketBooksMap contains the marketBooks associated with the safeMarkets; of lesser importance so far; added to in QuickCheckThread
    public static final SynchronizedMap<String, MarketBook> safeMarketBooksMap = new SynchronizedMap<>(64); // <marketId, MarketBook>
    // safeBetsMap contains marketIds as keys and maps of SafeBet(available safeBets) to SafeBetStats(some time details about the safeBet) as values; added to in QuickCheckThread
    public static final SafeBetsMap<SafeBet> safeBetsMap = new SafeBetsMap<>(); // <marketId, <SafeBet, SafeBetStats>>
    public static final SynchronizedMap<String, Long> timedWarningsMap = new SynchronizedMap<>(); // String, timeStamp

    public static final SubclassMaps<ScraperEvent> scraperEventMaps = new SubclassMaps<>(scraperEventSubclassesSet);

    public static final MarketCache marketCache = new MarketCache(); // , offlineMarketCache = new MarketCache();
    public static final OrderCache orderCache = new OrderCache(); // , offlineOrderCache = new OrderCache();

    private Statics() {
    }

//    public Statics(boolean dontCloseStandardStreams) {
//        Statics.closeStandardStreamsNotInitialized = dontCloseStandardStreams;
//    }

    static { // initialize priceList
        final Collection<Integer> localPricesList = new ArrayList<>(350);
        int counter = 101;
        do {
            localPricesList.add(counter);
            final int step;
            if (counter < 200) {
                step = 1;
            } else if (counter < 300) {
                step = 2;
            } else if (counter < 400) {
                step = 5;
            } else if (counter < 600) {
                step = 10;
            } else if (counter < 1_000) {
                step = 20;
            } else if (counter < 2_000) {
                step = 50;
            } else if (counter < 3_000) {
                step = 100;
            } else if (counter < 5_000) {
                step = 200;
            } else if (counter < 10_000) {
                step = 500;
            } else {
                step = 1_000;
            }

            counter += step;
        } while (counter <= 100_000);

        pricesList = List.copyOf(localPricesList);
    }

    static {
        connManager.setMaxTotal(32768);
        connManager.setDefaultMaxPerRoute(16384);
    }

    static {
        threadPoolExecutor.allowCoreThreadTimeOut(true);
        threadPoolExecutorMarketBooks.allowCoreThreadTimeOut(true);
        threadPoolExecutorImportant.allowCoreThreadTimeOut(true);
    }

    private static final Map<String, String> privateObjectFileNamesMap = new LinkedHashMap<>(32);

    static { // map used in two methods in VarsIO class
        privateObjectFileNamesMap.put("timeStamps", Statics.DATA_FOLDER_NAME + "/timeStamps.txt");
        privateObjectFileNamesMap.put("debugLevel", Statics.DATA_FOLDER_NAME + "/debugLevel.txt");
        privateObjectFileNamesMap.put("sessionTokenObject", Statics.DATA_FOLDER_NAME + "/sessionToken.txt");
        privateObjectFileNamesMap.put("safetyLimits", Statics.DATA_FOLDER_NAME + "/safetyLimits.txt");
//        privateObjectFileNamesMap.put("blackList", Statics.DATA_FOLDER_NAME + "/blacklist.txt");
//        privateObjectFileNamesMap.put("placedAmounts", Statics.DATA_FOLDER_NAME + "/placedAmounts.txt");

        privateObjectFileNamesMap.put("betradarEventsMap", Statics.DATA_FOLDER_NAME + "/betradarEvents.txt");
        privateObjectFileNamesMap.put("coralEventsMap", Statics.DATA_FOLDER_NAME + "/coralEvents.txt");
        privateObjectFileNamesMap.put("eventsMap", Statics.DATA_FOLDER_NAME + "/events.txt");
        privateObjectFileNamesMap.put("marketCataloguesMap", Statics.DATA_FOLDER_NAME + "/marketCatalogues.txt");
        privateObjectFileNamesMap.put("safeMarketsMap", Statics.DATA_FOLDER_NAME + "/safeMarkets.txt");
        privateObjectFileNamesMap.put("safeMarketBooksMap", Statics.DATA_FOLDER_NAME + "/safeMarketBooks.txt");

//        privateObjectFileNamesMap.put("alreadyPrintedMap", Statics.DATA_FOLDER_NAME + "/alreadyprinted.txt");
        privateObjectFileNamesMap.put("timedWarningsMap", Statics.DATA_FOLDER_NAME + "/timedWarnings.txt");
//        privateObjectFileNamesMap.put("ignorableDatabase", Statics.DATA_FOLDER_NAME + "/ignorabledatabase.txt");
//        privateObjectFileNamesMap.put("rulesManager", Statics.DATA_FOLDER_NAME + "/rulesManager.txt");

//        privateObjectFileNamesMap.put("marketCache", Statics.DATA_FOLDER_NAME + "/marketcache.txt");
//        privateObjectFileNamesMap.put("orderCache", Statics.DATA_FOLDER_NAME + "/ordercache.txt");
    }

    public static final Map<String, String> objectFileNamesMap = Collections.unmodifiableMap(privateObjectFileNamesMap);

    static { // marketTypes set
        // plenty of markets with null marketType; some are interesting; can't be retreived by marketType; I need support and a new Set<String> for getting them by name
        // get by name from MarketCatalogue when the type in MarketDescription is null
        // first group has priority
        marketTypes.add("HALF_TIME_FULL_TIME"); // half time/full time results, each with 3 possibilities, 9 possibilities total; very interesting
        marketTypes.add("CORRECT_SCORE"); // correct score after 2nd half, normally upto 3-3, closed when score>3; very interesting
        marketTypes.add("ANYTIME_SCORE"); // correct score during both halves, at any time, normally upto 3-3, 0-0 excluded, might be closed when score>3; very interesting
        marketTypes.add("HALF_TIME_SCORE"); // correct score after 1st half, normally upto 2-2, closed when score>2; very interesting
        marketTypes.add("TOTAL_GOALS"); // total goals, after 2nd half, from "1 or more" upto "7 or more"; very interesting
        marketTypes.add("EXACT_GOALS"); // total number of goals, from "0" to "7+"; very interesting
        marketTypes.add("TEAM_TOTAL_GOALS"); // 2 markets; total goals, after 2nd half, from "1 or more" upto "7 or more"; very interesting
        marketTypes.add("CORRECT_SCORE2"); // 2 markets(4 now with score_3); normally scores 4-0 upto 7-2, after 2nd half, closed when weaker>2 or stronger>7; very interesting
        marketTypes.add("WINNING_MARGIN"); // winning margin options +1 to +4, draw and no goals, after 2nd half; very interesting "No Goals" option
        marketTypes.add("HALF_WITH_MOST_GOALS"); // 1st_Half/2nd_Half/Tie, goals scored; very interesting
        marketTypes.add("METHOD_OF_VICTORY"); // method of victory (90 minutes/ET/penalties) 3*2=6 options; very interesting
        marketTypes.add("MATCH_ODDS_AND_OU_25"); // was in the other set; match odds and over/under 2.5 goals combination, 6 options total, after 2nd half; very interesting
        marketTypes.add("MATCH_ODDS_AND_OU_35"); // was in the other set; match odds and over/under 3.5 goals combination, 6 options total, after 2nd half; very interesting
        marketTypes.add("MATCH_ODDS_AND_BTTS"); // match odds and both teams score combination, 6 options total, after 2nd half; very interesting
        marketTypes.add("LAST_TEAM_TO_SCORE"); // last team to score, after 2nd half, with "No Goal" option; very interesting
        // next are considered for testing
        marketTypes.add("TO_SCORE_BOTH_HALVES");// 2 markets; team scores in both 1st & 2nd half; might get closed when impossible
        marketTypes.add("WIN_HALF"); // 2 markets; team wins 1st or 2nd half; only goals scored in that half count; might get closed when impossible
        marketTypes.add("GOAL_BOTH_HALVES"); // goals scored in 1st half & 2nd half; might get closed when impossible
        marketTypes.add("TEAM_A_WIN_TO_NIL"); // this team scores and the other team scores no goals, after 2nd half; probably closed when imopossible
        marketTypes.add("TEAM_B_WIN_TO_NIL"); // this team scores and the other team scores no goals, after 2nd half; probably closed when imopossible
        marketTypes.add("WIN_BOTH_HALVES"); // 2 markets; team wins 1st and 2nd half; probably closed when imopossible
        marketTypes.add("CLEAN_SHEET"); // 2 markets; team not receiving goal, after 2nd half; probably closed when imopossible
        marketTypes.add("BOTH_TEAMS_TO_SCORE"); // both teams to score, after 2nd half; seems to get closed when impossible
        marketTypes.add("FIRST_HALF_GOALS_05"); // over/under 0.5 goals, after 1st half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("FIRST_HALF_GOALS_15"); // over/under 1.5 goals, after 1st half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("FIRST_HALF_GOALS_25"); // over/under 2.5 goals, after 1st half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("SECOND_HALF_GOALS_05"); // over/under 0.5 goals, only 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("SECOND_HALF_GOALS_15"); // over/under 1.5 goals, only 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("OVER_UNDER_05"); // over/under 0.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("OVER_UNDER_15"); // over/under 1.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("OVER_UNDER_25"); // over/under 2.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("OVER_UNDER_35"); // over/under 3.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("OVER_UNDER_45"); // over/under 4.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("OVER_UNDER_55"); // over/under 5.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("OVER_UNDER_65"); // over/under 6.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("OVER_UNDER_75"); // over/under 7.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("OVER_UNDER_85"); // over/under 8.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("OVER_UNDER"); // over/under goals, after 2nd half; many markets, I've seen for 9.5-14.5; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("TEAM_A_OVER_UNDER_05"); // home team over/under 0.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("TEAM_A_OVER_UNDER_15"); // home team over/under 1.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("TEAM_A_OVER_UNDER_25"); // home team over/under 2.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("TEAM_B_OVER_UNDER_05"); // away team over/under 0.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("TEAM_B_OVER_UNDER_15"); // away team over/under 1.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("TEAM_B_OVER_UNDER_25"); // away team over/under 2.5 goals, after 2nd half; normally gets suspended when goal scored and closed when impossible
        // next are not considered
        marketTypes.add("ET_CORRECT_SCORE"); // correct score, upto 2-2, only ET; very interesting, but harder to code
        marketTypes.add("ET_BTTS"); // both teams to score, only ET; seems to get closed when impossible
        marketTypes.add("ET_OU_GOALS_05"); // over/under 0.5 goals, only ET; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("ET_OU_GOALS_15"); // over/under 1.5 goals, only ET; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("ET_OU_GOALS_25"); // over/under 2.5 goals, only ET; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("ET_OU_GOALS_35"); // over/under 3.5 goals, only ET; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("ET_FH_OU_GOALS_05"); // over/under 0.5 goals, only ET 1st Half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("ET_FH_OU_GOALS_15"); // over/under 1.5 goals, only ET 1st Half; normally gets suspended when goal scored and closed when impossible
        marketTypes.add("SENDING_OFF"); // any player shown red card, after 2nd half; only players on the pitch count; probably closed when imopossible, hard to know if player on pitch
        marketTypes.add("LAST_GOALSCORER"); // player to score last, excluding those who take no part in match, excluding own goals, after 2nd half; hard to code
        marketTypes.add("CORNER_ODDS"); // total corners, after 2nd half; I've seen "9 or less", "10 - 12", "13 or more" options; interesting, but hard to code
        marketTypes.add("FIRST_GOAL_SCORER"); // player who scores first, from long list, after 2nd half; hard to code
        marketTypes.add("NEXT_GOALSCORER"); // player who scores next(separate markets for 2nd, 3rd, 4th, ..., 9th), from long list, after 2nd half; hard to code
        marketTypes.add("RACE_TO_2_GOALS"); // fist team to score 2 goals, 3 options, after 2nd half; fairly hard & probably closed when imopossible
        marketTypes.add("RACE_TO_3_GOALS"); // fist team to score 3 goals, 3 options, after 2nd half; fairly hard & probably closed when imopossible
        marketTypes.add("TO_SCORE"); // named player, from long list, to score, after 2nd half; OGs don't count, players who didn't play voided; hard
        marketTypes.add("TO_SCORE_2_OR_MORE"); // player to score 2+ goals, after 2nd half; OGs don't count, players who didn't play voided; hard
        marketTypes.add("TO_SCORE_HATTRICK"); // named player, from long list, to score 3+ times, after 2nd half; OGs don't count, players who didn't play voided; hard
        marketTypes.add("FIRST_GOAL_ODDS"); // minute interval when first goal is scored, or no goal, after 2nd half; hard & probably closed when imopossible
        marketTypes.add("HAT_TRICKED_SCORED"); // 3+ goals by same player, after 2nd half; own goals don't count; hard & likely not live & probably closed when imopossible
        marketTypes.add("PENALTY_TAKEN"); // penalty awarded, after 2nd half; hard & likely not live & probably closed when imopossible
        marketTypes.add("OVER_UNDER_25_CARDS"); // number of total cards over/under 2.5, after 2nd half; hard & probably closed when imopossible
        marketTypes.add("OVER_UNDER_35_CARDS"); // number of total cards over/under 2.5, after 2nd half; hard & probably closed when imopossible
        marketTypes.add("OVER_UNDER_45_CARDS"); // number of total cards over/under 4.5, after 2nd half; hard & probably closed when imopossible
        marketTypes.add("OVER_UNDER_65_CARDS"); // number of total cards over/under 6.5, after 2nd half; hard & probably closed when imopossible
        marketTypes.add("SHOWN_A_CARD"); // huge list, players who may be shown a card, after 2nd half; only those who played count, rest will be voided; hard & not live
        marketTypes.add("BOOKING_ODDS"); // 10 yellow, 25 for red, 10+25 for yellow+yellow, after 2nd half; only cards on pitch; hard & likely not live
        marketTypes.add("SCORE_CAST"); // correct first player who scores and final score combination, from long list, after 2nd half; hard & not live
        marketTypes.add("WINCAST"); // 2 markets(anytime/first goal); player to score (anytime/first)/match odds combination, after 2nd half; anytime is impossible; hard & not live
        marketTypes.add("FIRST_HALF_CORNERS"); // corners after 1st half, I've seen over/under 4.5 options; hard to code and might get closed when impossible
        marketTypes.add("OVER_UNDER_55_CORNR"); // over/under 5.5 corners, after 2nd half; hard to code and might get closed when impossible
        marketTypes.add("OVER_UNDER_85_CORNR"); // over/under 8.5 corners, after 2nd half; hard to code and might get closed when impossible
        marketTypes.add("OVER_UNDER_105_CORNR"); // over/under 10.5 corners, after 2nd half; hard to code and might get closed when impossible
        marketTypes.add("OVER_UNDER_135_CORNR"); // over/under 13.5 corners, after 2nd half; hard to code and might get closed when impossible
        marketTypes.add("FIRST_CORNER"); // 3 options match odds for first corner, after 2nd half; hard to code and might get closed when impossible
        marketTypes.add("MATCH_BET"); // Season Match Bets, 2 options, which team will finish higher in the league; league table positions are not going to be implemented
        marketTypes.add("ACCA"); // accumulator bet on a selection of more than 1 team winning; more than 1 event is involved, too complicated to implement for now and it's very rare
        marketTypes.add("ALT_TOTAL_GOALS"); // Goal Lines, asian handicap double line betting; this market is mostly a combination of over/under markets, so there's little reason to use it
        // next are never impossible
        marketTypes.add("NEXT_GOAL"); // team that scores next goal, 3 options; when a goal is scored, market is closed/settled and opened again; never impossible
        marketTypes.add("ET_NEXT_GOAL"); // team that scores next goal, during ET, 3 options; when a goald is scored, market is closed/settled and opened again; never impossible
        marketTypes.add("MATCH_ODDS"); // home_win/away_win/draw, after 2nd half; never impossible
        marketTypes.add("MATCH_ODDS_UNMANAGED"); // home_win/away_win/draw, after 2nd half, unmanaged; never impossible
        marketTypes.add("HALF_TIME"); // 3 option match odds, after 1st half; never impossible
        marketTypes.add("2ND_HALF_MATCH_ODDS"); // 3 option match odds, only goals scored in 2nd half; never impossible
        marketTypes.add("EXTRA_TIME"); // 3 option match odds, only ET; never impossible
        marketTypes.add("ET_HALF_TIME"); // 3 option match odds, only ET 1st half; never impossible
        marketTypes.add("DRAW_NO_BET"); // home_win/away_win, after 2nd half; never impossible
        marketTypes.add("TEAM_A_1"); // match odds with +1 handicap, after 2nd half; never impossible
        marketTypes.add("TEAM_B_1"); // match odds with +1 handicap, after 2nd half; never impossible
        marketTypes.add("TEAM_A_2"); // match odds with +2 handicap, after 2nd half; never impossible
        marketTypes.add("TEAM_B_2"); // match odds with +2 handicap, after 2nd half; never impossible
        marketTypes.add("TEAM_A_3"); // match odds with +3 handicap, after 2nd half; never impossible
        marketTypes.add("TEAM_B_3"); // match odds with +3 handicap, after 2nd half; never impossible
        marketTypes.add("HANDICAP"); // many markets; match odds with handicap (I've seen +4 - +9), after 2nd half; never impossible
        marketTypes.add("ASIAN_HANDICAP"); // huge market, only home_win/away_win options, after 2nd half, with a lot of asian handicap options; never impossible
        marketTypes.add("TO_QUALIFY"); // two options, the team that qualifies after the match; never impossible
        marketTypes.add("TO_REACH_SEMIS"); // two options, the team that qualifies after the match into semifinals; never impossible
        marketTypes.add("DOUBLE_CHANCE"); // 3 match odds options, home+away/home+draw/away+draw, after 2nd half; never impossible
        marketTypes.add("ODD_OR_EVEN"); // total number of goals scored, after 2nd half; zero is even; never impossible
        marketTypes.add("BOOKING_MATCH_BET"); // 3 option match odds, 10 points yellow, 25 red, 10+25 yellow+yellow, after 2nd half; only cards on pitch; never impossible
        marketTypes.add("WIN_FROM_BEHIND"); // 2 markets; teams wins after being behind, after 2nd half; never impossible
        marketTypes.add("CORNER_MATCH_BET"); // 3 option match odds for most corners, after 2nd half; never impossible
        marketTypes.add("SPECIAL"); // various markets; related to leagues/competitions, not individual matches
        marketTypes.add("UNDIFFERENTIATED"); // various markets; related to leagues/competitions, not individual matches
        marketTypes.add("UNUSED"); // various markets; related to leagues/competitions, not individual matches
        marketTypes.add("WINNER"); // winners teams; related to leagues/competitions, not individual matches
        marketTypes.add("TOP_GOALSCORER"); // top goalscorer; related to leagues/competitions, not individual matches
        marketTypes.add("ROCK_BOTTOM"); // tema to finish the competition last; related to leagues/competitions, not individual matches
        marketTypes.add("SPECIALS_NEXT_MGR"); // next manager markets; related to teams, not individual matches
        marketTypes.add("TOP_N_FINISH"); // top (usually 10) finish teams in a league; related to leagues, not individual matches
        marketTypes.add("DAILY_SPECIALS"); // special markets (ex: 3pm specials, for matches starting at 3pm in Premiership); related to groups of games, not individual matches
        marketTypes.add("DAILY_GOALS"); // daily goals for a certain competition (ex: Europa League); related to competitions, not individual matches
        marketTypes.add("TO_REACH_FINAL"); // to reach final for a certain competition (ex: Champions League); related to competitions, not individual matches
        marketTypes.add("AH_ODDS_MARKET"); //home_win/away_win options, +/- 0.5,1,1.5 handicap, after 2nd half; never impossible
    }

    static { // marketNullTypes Set; markets that have null marketType, so I have to rely on marketName
        // first group has priority
        marketNullTypes.add("Second Half Correct Score"); // correct score, upto 2-2, only 2nd half; very interesting
        // marketNullTypes.add("Last Team to Score"); // last team to score, after 2nd half, with "No Goal" option; very interesting
        // marketNullTypes.add("Match Odds and Both teams to Score"); // match odds and both teams score combination, 6 options total, after 2nd half; very interesting
        // marketNullTypes.add("Match Odds and Over/Under 2.5 goals"); // match odds and over/under 2.5 goals combination, 6 options total, after 2nd half; very interesting
        marketNullTypes.add("Half Time Score 2"); // 2 markets presumably; correct score after 1st half, I've only seen 1 with scores 3-0 - 7-1; very interesting
        // marketNullTypes.add("Correct Score 3 Home"); // 2 markets(presumably Away exists); scores 8-0 upto 11-2, after 2nd half; very interesting; included in CORRECT_SCORE2
        // next are considered for testing

        // next are not considered
//        marketNullTypes.add("Next Goalscorer - 2nd Goal"); // player who scores 2nd, from long list, after 2nd half; hard to code
//        marketNullTypes.add("Next Goalscorer - 3rd Goal"); // player who scores 3rd, from long list, after 2nd half; hard to code
//        marketNullTypes.add("Next Goalscorer - 4th Goal"); // player who scores 4th, from long list, after 2nd half; hard to code
//        marketNullTypes.add("Next Goalscorer - 5th Goal"); // player who scores 5th, from long list, after 2nd half; hard to code
//        marketNullTypes.add("Next Goalscorer - 6th Goal"); // player who scores 6th, from long list, after 2nd half; hard to code
//        marketNullTypes.add("Next Goalscorer - 7th Goal"); // player who scores 7th, from long list, after 2nd half; hard to code
//        marketNullTypes.add("Next Goalscorer - 8th Goal"); // player who scores 8th, from long list, after 2nd half; hard to code
//        marketNullTypes.add("Next Goalscorer - 9th Goal"); // player who scores 9th, from long list, after 2nd half; hard to code
        marketNullTypes.add("ET - First Goalscorer"); // player who scores first in ET, from long list, after ET; hard to code
        // marketNullTypes.add("Cards Over/Under 2.5"); // number of total cards over/under 2.5, after 2nd half; hard & probably closed when imopossible
        // marketNullTypes.add("Corners Over/Under 8.5"); // over/under 8.5 corners, after 2nd half; hard to code and might get closed when impossible
        // marketNullTypes.add("To Score 2 Goals or more"); // player to score 2+ goals, after 2nd half; OGs don't count, players who didn't play voided; hard & might not be live
        marketNullTypes.add("First Carded Player"); // player who get a card first, from long list, after 2nd half; hard to code
        // next are never impossible
//        marketNullTypes.add("Asian Handicap -0.5"); //home_win/away_win options, -0.5 handicap for home, after 2nd half; never impossible
//        marketNullTypes.add("Asian Handicap +0.5"); //home_win/away_win options, +0.5 handicap for home, after 2nd half; never impossible
//        marketNullTypes.add("Asian Handicap -1.0"); //home_win/away_win options, -1.0 handicap for home, after 2nd half; never impossible
//        marketNullTypes.add("Asian Handicap +1.0"); //home_win/away_win options, +1.0 handicap for home, after 2nd half; never impossible
//        marketNullTypes.add("Asian Handicap -1.5"); //home_win/away_win options, -1.5 handicap for home, after 2nd half; never impossible
//        marketNullTypes.add("Asian Handicap +1.5"); //home_win/away_win options, +1.5 handicap for home, after 2nd half; never impossible
    }

    static {
//        marketsUnderTesting.add(ParsedMarketType.EXACT_GOALS);

        if (!marketsUnderTesting.isEmpty()) {
            logger.error("marketsUnderTesting not empty: {}", Generic.objectToString(marketsUnderTesting));
        }
    }
}
