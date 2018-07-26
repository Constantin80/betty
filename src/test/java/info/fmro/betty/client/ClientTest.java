// disabled for now, as there will be many modifications, and most methods or the entire class won't be used
//package info.fmro.betty.client;
//
//import com.jayway.awaitility.Awaitility;
//import com.jayway.awaitility.Duration;
//import info.fmro.betty.stream.client.Client;
//import info.fmro.betty.stream.protocol.InvalidCredentialException;
//import info.fmro.betty.stream.protocol.ConnectionException;
//import info.fmro.betty.stream.protocol.ConnectionStatus;
//import info.fmro.betty.stream.protocol.StatusException;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//
///**
// * Created by mulveyJ on 11/07/2016.
// */
//public class ClientTest
//        extends BaseTest {
//
//    private Client client;
//
//    @BeforeEach
//    public void beforeMethod() {
////        client = new Client("stream-api-integration.betfair.com", 443, getValidSessionProvider());
//        client = new Client("stream-api-integration.betfair.com", 443);
//    }
//
//    @AfterEach
//    public void afterMethod() {
//        client.stopClient();
//    }
//
//    @Test
//    public void testInvalidHost()
//            throws InvalidCredentialException, StatusException, ConnectionException {
////        Client invalidClient = new Client("www.betfair.com", 443, getValidSessionProvider());
//        Client invalidClient = new Client("www.betfair.com", 443);
//        invalidClient.setTimeout(100);
////        invalidClient.start();
//
//        assertThrows(ConnectionException.class, invalidClient::start, "exception expected");
//    }
//
//    @Test
//    public void testStartStop()
//            throws InvalidCredentialException, StatusException, ConnectionException {
//        assertEquals(client.getStatus(), ConnectionStatus.STOPPED, "stopped");
//        client.start();
//        assertEquals(client.getStatus(), ConnectionStatus.AUTHENTICATED, "authenticated");
//        client.stopClient();
//        assertEquals(client.getStatus(), ConnectionStatus.STOPPED, "stopped 2");
//    }
//
//    @Test
//    public void testStartHeartbeatStop()
//            throws InvalidCredentialException, StatusException, ConnectionException {
//        client.start();
//        client.heartbeat();
//        client.stopClient();
//    }
//
//    @Test
//    public void testReentrantStartStop()
//            throws InvalidCredentialException, StatusException, ConnectionException {
//        client.start();
//        assertEquals(client.getStatus(), ConnectionStatus.AUTHENTICATED, "authenticated");
//        client.heartbeat();
//        client.stopClient();
//        assertEquals(client.getStatus(), ConnectionStatus.STOPPED, "stopped");
//
//        client.start();
//        assertEquals(client.getStatus(), ConnectionStatus.AUTHENTICATED, "authenticated 2");
//        client.heartbeat();
//        client.stopClient();
//        assertEquals(client.getStatus(), ConnectionStatus.STOPPED, "stopped 2");
//    }
//
//    @Test
//    public void testDoubleStartStop()
//            throws InvalidCredentialException, StatusException, ConnectionException {
//        client.start();
//        client.start();
//        assertEquals(client.getStatus(), ConnectionStatus.AUTHENTICATED, "auth");
//        client.heartbeat();
//        client.stopClient();
//        client.stopClient();
//        assertEquals(client.getStatus(), ConnectionStatus.STOPPED, "stopped");
//    }
//
//    @Test
//    public void testDisconnectWithAutoReconnect()
//            throws InvalidCredentialException, StatusException, ConnectionException {
//        client.start();
//        assertEquals(client.getStatus(), ConnectionStatus.AUTHENTICATED, "auth");
//        client.heartbeat();
//
//        //socket disconnect
//        assertEquals(client.getDisconnectCounter(), 0, "0");
//        client.disconnect();
//
//        //retry until connected
//        Awaitility.await().catchUncaughtExceptions().atMost(Duration.ONE_MINUTE).until(() -> {
//            try {
//                client.heartbeat();
//                return true;
//            } catch (Throwable e) {
//                return false;
//            }
//        });
//        assertEquals(client.getStatus(), ConnectionStatus.AUTHENTICATED, "auth 2");
//        assertEquals(client.getDisconnectCounter(), 1, "1");
//    }
//}
