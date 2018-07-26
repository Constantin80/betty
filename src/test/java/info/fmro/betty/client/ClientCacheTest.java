// disabled for now, as there will be many modifications, and most methods or the entire class won't be used
//package info.fmro.betty.client;
//
//import info.fmro.betty.stream.client.Client;
//import info.fmro.betty.stream.ClientCache;
//import info.fmro.betty.stream.protocol.InvalidCredentialException;
//import info.fmro.betty.stream.protocol.ConnectionException;
//import info.fmro.betty.stream.protocol.StatusException;
//import org.junit.jupiter.api.Test;
//
///**
// * Created by mulveyj on 08/07/2016.
// */
//public class ClientCacheTest
//        extends BaseTest {
//
//    @Test
//    public void testUserStory()
//            throws InvalidCredentialException, StatusException, ConnectionException, InterruptedException {
//        //1: Create a session provider
//
//        //2: Create a client
//        Client client = new Client("stream-api-integration.betfair.com", 443);
//
//        //3: Create a cache
//        ClientCache cache = new ClientCache(client);
//
//        //4: Setup order subscription
//        //Register for change events
////        cache.getOrderCache().OrderMarketChanged +=
////                (sender, arg) => Console.WriteLine("Order:" + arg.Snap);
//        //Subscribe to orders
//        cache.subscribeOrders();
//
//        //5: Setup market subscription
//        //Register for change events
//        cache.getMarketCache().addMarketChangeListener((e) -> System.out.println("Market:" + e.getSnap()));
////        cache.MarketCache.MarketChanged +=
////                (sender, arg) => Console.WriteLine("Market:" + arg.Snap);
//        //Subscribe to markets (use a valid market id or filter)
//        cache.subscribeMarkets("1.125499232");
//
//        Thread.sleep(5000); //pause for a bit
//    }
//}