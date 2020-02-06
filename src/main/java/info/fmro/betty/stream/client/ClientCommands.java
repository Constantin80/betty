package info.fmro.betty.stream.client;

import info.fmro.shared.stream.definitions.MarketFilter;
import info.fmro.shared.stream.definitions.MarketSubscriptionMessage;
import info.fmro.shared.stream.definitions.OrderSubscriptionMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("UtilityClass")
final class ClientCommands {
    private ClientCommands() {
        super();
    }

    @NotNull
    @SuppressWarnings("unused")
    public static MarketSubscriptionMessage marketFireHose(final Client client) {
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

    // Subscribe to the specified market ids. (starting the client if needed).
    @NotNull
    private static MarketSubscriptionMessage createMarketSubscriptionMessage(final Client client, final String... markets) {
        final MarketFilter marketFilter = new MarketFilter();
        marketFilter.setMarketIds(new HashSet<>(Arrays.asList(markets)));
        return createMarketSubscriptionMessage(client, marketFilter);
    }

    @NotNull
    static MarketSubscriptionMessage createMarketSubscriptionMessage(final Client client, final Set<String> markets) {
        final MarketFilter marketFilter = new MarketFilter();
        marketFilter.setMarketIds(markets);
        return createMarketSubscriptionMessage(client, marketFilter);
    }

    // Subscribe to the specified markets (matching your filter). (starting the client if needed).
    @NotNull
    private static MarketSubscriptionMessage createMarketSubscriptionMessage(final Client client, final MarketFilter marketFilter) {
        final MarketSubscriptionMessage marketSubscriptionMessage = new MarketSubscriptionMessage();
        marketSubscriptionMessage.setMarketFilter(marketFilter);
        return createMarketSubscriptionMessage(client, marketSubscriptionMessage);
    }

    // Explicit order subscription.
    @NotNull
    @Contract("_, _ -> param2")
    private static MarketSubscriptionMessage createMarketSubscriptionMessage(@NotNull final Client client, @NotNull final MarketSubscriptionMessage message) {
        message.setConflateMs(client.conflateMs.get());
        message.setHeartbeatMs(client.heartbeatMs.get());
        message.setMarketDataFilter(client.marketDataFilter);
        return message;
    }

    // Subscribe to all orders.
    @NotNull
    static OrderSubscriptionMessage createOrderSubscriptionMessage(final Client client) {
        return createOrderSubscriptionMessage(client, new OrderSubscriptionMessage());
    }

    // Explict order subscription.
    @NotNull
    @Contract("_, _ -> param2")
    private static OrderSubscriptionMessage createOrderSubscriptionMessage(@NotNull final Client client, @NotNull final OrderSubscriptionMessage message) {
        message.setConflateMs(client.conflateMs.get());
        message.setHeartbeatMs(client.heartbeatMs.get());
        return message;
    }
}
