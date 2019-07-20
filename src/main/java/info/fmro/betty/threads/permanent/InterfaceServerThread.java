package info.fmro.betty.threads.permanent;

import info.fmro.betty.main.Betty;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class InterfaceServerThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceServerThread.class);

    @Override
    public void run() {
        final KeyManager[] keyManagers = Betty.getKeyManagers(Statics.INTERFACE_KEY_STORE_FILE_NAME, Statics.interfaceKeyStorePassword.get(), Statics.KEY_STORE_TYPE);
        SSLContext sSLContext = null;
        try {
            sSLContext = SSLContext.getInstance("TLS");
            sSLContext.init(keyManagers, null, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.error("SSLContext exception in InterfaceServer", e);
        }

        final SSLServerSocketFactory serverSocketFactory = sSLContext != null ? sSLContext.getServerSocketFactory() : null;
        SSLServerSocket serverSocket = null;
        if (serverSocketFactory != null) {
            try {
                serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(Statics.interfaceServerPort.get());
            } catch (IOException iOException) {
                logger.error("IOException in InterfaceServer", iOException);
                try {
                    serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(0);
                } catch (IOException innerIOException) {
                    logger.error("IOException in InterfaceServer inner", innerIOException);
                }
            }
        } else {
            logger.error("STRANGE serverSocketFactory null in InterfaceServer thread, timeStamp={}", System.currentTimeMillis());
        }

        if (serverSocket != null) {
            serverSocket.setNeedClientAuth(true);
            Statics.interfaceServerSocketsSet.add(serverSocket);
            Statics.interfaceServerPort.set(serverSocket.getLocalPort());
            logger.info("interfaceServerPort={}", Statics.interfaceServerPort.get());

            while (!Statics.mustStop.get()) {
                try {
                    final Socket socket = serverSocket.accept();
                    // todo ssl auth check test

                    Statics.interfaceConnectionSocketsSet.add(socket);
                    final InterfaceConnectionThread interfaceConnectionThread = new InterfaceConnectionThread(socket);
                    Statics.interfaceConnectionThreadsSet.add(interfaceConnectionThread);
                    interfaceConnectionThread.start();
                } catch (IOException iOException) {
                    if (Statics.mustStop.get()) { // program is stopping and the socket has been closed from another thread
                    } else {
                        logger.error("IOException in InterfaceServer socket accept", iOException);
                    }
                } catch (Throwable throwable) {
                    logger.error("STRANGE throwable in InterfaceServer socket accept", throwable);
                }
            } // end while
            Statics.interfaceServerSocketsSet.remove(serverSocket);
            logger.info("closing interface server socket");
            Generic.closeObject(serverSocket);
        } else {
            logger.error("STRANGE serverSocket null in InterfaceServer thread, timeStamp={}", System.currentTimeMillis());
        }
    }
}
