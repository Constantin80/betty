package info.fmro.betty.stream.client;

import info.fmro.betty.stream.definitions.MarketFilter;
import info.fmro.betty.stream.definitions.MarketSubscriptionMessage;
import info.fmro.betty.stream.definitions.OrderSubscriptionMessage;
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

@Component
@Configuration
public class ClientCommands
        extends DefaultPromptProvider
        implements CommandMarker {
    private static final Logger logger = LoggerFactory.getLogger(ClientCommands.class);

    private ClientCommands() {
    }

    @CliCommand(value = "marketFirehose", help = "subscribes to all markets")
    public static MarketSubscriptionMessage marketFirehose(final Client client) {
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
        return createMarketSubscriptionMessage(client);
    }

    /**
     * Subscribe to the specified market ids. (starting the client if needed).
     */
    public static MarketSubscriptionMessage createMarketSubscriptionMessage(final Client client, final String... markets) {
        final MarketFilter marketFilter = new MarketFilter();
        marketFilter.setMarketIds(new HashSet<>(Arrays.asList(markets)));
        return createMarketSubscriptionMessage(client, marketFilter);
    }

    public static MarketSubscriptionMessage createMarketSubscriptionMessage(final Client client, final HashSet<String> markets) {
        final MarketFilter marketFilter = new MarketFilter();
        marketFilter.setMarketIds(markets);
        return createMarketSubscriptionMessage(client, marketFilter);
    }

    /**
     * Subscribe to the specified markets (matching your filter). (starting the client if needed).
     */
    public static MarketSubscriptionMessage createMarketSubscriptionMessage(final Client client, final MarketFilter marketFilter) {
        final MarketSubscriptionMessage marketSubscriptionMessage = new MarketSubscriptionMessage();
        marketSubscriptionMessage.setMarketFilter(marketFilter);
        return createMarketSubscriptionMessage(client, marketSubscriptionMessage);
    }

    // Explicit order subscription.
    public static MarketSubscriptionMessage createMarketSubscriptionMessage(final Client client, final MarketSubscriptionMessage message) {
        message.setConflateMs(client.conflateMs.get());
        message.setHeartbeatMs(client.heartbeatMs.get());
        message.setMarketDataFilter(client.marketDataFilter);
        return message;
//        final FutureResponse<StatusMessage> futureResponse = client.processor.marketSubscription(message);
//        waitFor(client, futureResponse);
        //        client.processor.marketSubscription(message);
    }

    /**
     * Subscribe to all orders.
     */
    public static OrderSubscriptionMessage createOrderSubscriptionMessage(final Client client) {
        return createOrderSubscriptionMessage(client, new OrderSubscriptionMessage());
    }

    /**
     * Explict order subscription.
     */
    public static OrderSubscriptionMessage createOrderSubscriptionMessage(final Client client, final OrderSubscriptionMessage message) {
        message.setConflateMs(client.conflateMs.get());
        message.setHeartbeatMs(client.heartbeatMs.get());
        return message;
//        final FutureResponse<StatusMessage> futureResponse = client.processor.orderSubscription(message);
//        waitFor(client, futureResponse);
//        client.processor.orderSubscription(message);
    }

    @CliCommand(value = "traceMessages", help = "trace Messages (Markets and Orders)")
    public static void traceMessages(@CliOption(key = {""}, mandatory = false, help = "truncate", unspecifiedDefaultValue = "200", specifiedDefaultValue = "200") final Client client, final int truncate) {
        client.processor.setTraceChangeTruncation(truncate);
    }
}
