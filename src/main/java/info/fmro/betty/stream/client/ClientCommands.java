package info.fmro.betty.stream.client;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.cache.market.Market;
import info.fmro.betty.stream.cache.order.OrderMarket;
import info.fmro.betty.stream.cache.util.Utils;
import info.fmro.betty.stream.definitions.ErrorCode;
import info.fmro.betty.stream.definitions.MarketFilter;
import info.fmro.betty.stream.definitions.MarketSubscriptionMessage;
import info.fmro.betty.stream.definitions.OrderSubscriptionMessage;
import info.fmro.betty.stream.definitions.StatusCode;
import info.fmro.betty.stream.definitions.StatusMessage;
import info.fmro.betty.stream.protocol.FutureResponse;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.plugin.support.DefaultPromptProvider;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Configuration
public class ClientCommands
        extends DefaultPromptProvider
        implements CommandMarker {
    private static final Logger logger = LoggerFactory.getLogger(ClientCommands.class);

    private ClientCommands() {
    }

    @CliCommand(value = "marketFirehose", help = "subscribes to all markets")
    public static void marketFirehose(Client client) {
//        1=Soccer  10k markets
//        2=Tennis  6k markets
//        3=Golf    everything else has very few markets
//        4=Cricket
//        5=Rugby Union
//        6=Boxing
//        7=Horse Racing
//        8=Motor Sport
//        9=Special Bets
//        19=Politics
//        1477=Rugby League
//        3503=Darts
//        4339=Greyhound Racing
//        6231=Financial Bets
//        6422=Snooker
//        6423=American Football
//        7511=Baseball
//        7522=Basketball
//        72382=Pool
//        99817=Volleyball
//        451485=Winter Sports
//        468328=Handball
//        998918=Bowls
//        998919=Bandy
        subscribeMarkets(client);
    }

    /**
     * Subscribe to the specified market ids. (starting the client if needed).
     */
    public static void subscribeMarkets(Client client, String... markets) {
        final MarketFilter marketFilter = new MarketFilter();
        marketFilter.setMarketIds(new HashSet<>(Arrays.asList(markets)));
//        marketFilter.setEventTypeIds("3");
//        marketFilter.setMarketTypes("HALF_TIME_FULL_TIME",
//                                    "CORRECT_SCORE",
//                                    "ANYTIME_SCORE",
//                                    "HALF_TIME_SCORE",
//                                    "TOTAL_GOALS"
//                                    );
        subscribeMarkets(client, marketFilter);

//        final MarketFilter marketFilter2 = new MarketFilter();
//        marketFilter2.setMarketIds(new HashSet<>(Arrays.asList(markets)));
////        marketFilter.setEventTypeIds("3");
////        marketFilter2.setMarketTypes(
////                "EXACT_GOALS",
////                "TEAM_TOTAL_GOALS_A",
////                "TEAM_TOTAL_GOALS_B",
////                "CORRECT_SCORE2_A",
////                                    "CORRECT_SCORE2_B",
////                                    "WINNING_MARGIN",
////                                    "HALF_WITH_MOST_GOALS",
////                                    "METHOD_OF_VICTORY",
////                                    "MATCH_ODDS_AND_OU_25",
////                                    "MATCH_ODDS_AND_OU_35",
////                                    "MATCH_ODDS_AND_BTTS",
////                                    "ET_CORRECT_SCORE",
////                                    "TO_SCORE_BOTH_HALVES_A",
////                                    "TO_SCORE_BOTH_HALVES_B");
//        subscribeMarkets(marketFilter2);
    }

    /**
     * Subscribe to the specified markets (matching your filter). (starting the client if needed).
     */
    public static void subscribeMarkets(Client client, MarketFilter marketFilter) {
        final MarketSubscriptionMessage marketSubscriptionMessage = new MarketSubscriptionMessage();
        marketSubscriptionMessage.setMarketFilter(marketFilter);
        subscribeMarkets(client, marketSubscriptionMessage);
    }

    // Explicit order subscription.
    public static void subscribeMarkets(Client client, MarketSubscriptionMessage message) {
        message.setConflateMs(client.conflateMs.get());
        message.setHeartbeatMs(client.heartbeatMs.get());
        message.setMarketDataFilter(client.marketDataFilter);

        final FutureResponse<StatusMessage> futureResponse = client.processor.marketSubscription(message);
        waitFor(client, futureResponse);
    }

    /**
     * Subscribe to all orders.
     */
    public static void subscribeOrders(Client client) {
        subscribeOrders(client, new OrderSubscriptionMessage());
    }

    /**
     * Explict order subscription.
     */
    public static void subscribeOrders(Client client, OrderSubscriptionMessage message) {
        message.setConflateMs(client.conflateMs.get());
        message.setHeartbeatMs(client.heartbeatMs.get());

        final FutureResponse<StatusMessage> futureResponse = client.processor.orderSubscription(message);
        waitFor(client, futureResponse);
    }

    @CliCommand(value = "listMarkets", help = "lists the cached markets")
    public static void listMarkets() {
        for (Market market : Statics.marketCache.getMarkets()) {
            Utils.printMarket(market.getSnap());
        }
    }

    @CliCommand(value = "listOrders", help = "lists the cached orders")
    public static void listOrders() {
        for (OrderMarket orderMarket : Statics.orderCache.getOrderMarkets()) {
            Utils.printOrderMarket(orderMarket.getOrderMarketSnap());
        }
    }

    @CliCommand(value = "traceMarkets", help = "trace Markets")
    public static void traceMarkets() {
        Statics.marketCache.traceMarkets();
    }

    @CliCommand(value = "traceOrders", help = "trace Orders")
    public static void traceOrders() {
        Statics.orderCache.traceOrders();
    }

    @CliCommand(value = "traceMessages", help = "trace Messages (Markets and Orders)")
    public static void traceMessages(@CliOption(key = {""}, mandatory = false, help = "truncate", unspecifiedDefaultValue = "200", specifiedDefaultValue = "200") Client client, int truncate) {
        client.processor.setTraceChangeTruncation(truncate);
    }

    public static FutureResponse<StatusMessage> waitFor(Client client, FutureResponse<StatusMessage> task) {
        StatusMessage statusMessage = null;
        FutureResponse<StatusMessage> result = null;

        try {
            statusMessage = task.get(client.timeout, TimeUnit.MILLISECONDS);
            if (statusMessage != null) { //server responded
                if (statusMessage.getStatusCode() == StatusCode.SUCCESS) {
                    result = task;
                } else { //status error
                    if (statusMessage.getErrorCode() == ErrorCode.INVALID_SESSION_INFORMATION) {
                        logger.info("INVALID_SESSION_INFORMATION, needSessionToken: {}", Generic.objectToString(statusMessage));
                        Statics.needSessionToken.set(true);
                    } else {
                        logger.error("StatusCode.FAILURE in streamClient: {}", Generic.objectToString(statusMessage));
                    }
                }
            } else {
                logger.error("statusMessage null in streamClient: {}", Generic.objectToString(task, "backtrace"));
            }
        } catch (InterruptedException e) {
            logger.error("InterruptedException in streamClient: {}", Generic.objectToString(task, "backtrace"), e);
        } catch (ExecutionException e) {
            logger.error("ExecutionException in streamClient: {}", Generic.objectToString(task, "backtrace"), e);
        } catch (CancellationException e) {
            logger.error("CancellationException in streamClient: {}", Generic.objectToString(task, "backtrace"), e);
        } catch (TimeoutException e) {
            logger.error("TimeoutException in streamClient: {}", Generic.objectToString(task, "backtrace"), e);
        }

        if (statusMessage == null || (statusMessage.getStatusCode() == StatusCode.FAILURE && statusMessage.getErrorCode() != ErrorCode.SUBSCRIPTION_LIMIT_EXCEEDED)) {
            logger.error("failure in waitFor: {} {}", Generic.objectToString(statusMessage), Generic.objectToString(task, "backtrace"));
//            disconnect();
            client.setStreamError(true);
        }

        return result;
    }
}
