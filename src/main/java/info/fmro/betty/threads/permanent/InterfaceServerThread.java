package info.fmro.betty.threads.permanent;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.InterfaceConnectionThread;
import info.fmro.shared.objects.SharedStatics;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InterfaceServerThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceServerThread.class);
    private final Set<InterfaceConnectionThread> interfaceConnectionThreadsSet = Collections.synchronizedSet(new HashSet<>(2));
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    private SSLServerSocket serverSocket;

//    // not synchronized, likely not needed
//    public void sendObject(@NotNull final StreamObjectInterface object) { // sends to all connections
//        synchronized (this.interfaceConnectionThreadsSet) {
//            for (@NotNull final InterfaceConnectionThread interfaceConnectionThread : this.interfaceConnectionThreadsSet) {
//                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.sendObject, interfaceConnectionThread, object));
//            }
//        }
//    }

    public void closeSocket() { // this is hard shutoff, probably best to not synchronize
        logger.debug("closing InterfaceServerThread socket");
        Generic.closeObject(this.serverSocket);
    }

    @Override
    public void run() {
        final KeyManager[] keyManagers = Generic.getKeyManagers(Statics.INTERFACE_KEY_STORE_FILE_NAME, Statics.interfaceKeyStorePassword.get(), Statics.KEY_STORE_TYPE);
        SSLContext sSLContext = null;
        try {
            sSLContext = SSLContext.getInstance("TLS");
            sSLContext.init(keyManagers, Generic.getCustomTrustManager(Statics.INTERFACE_KEY_STORE_FILE_NAME, Statics.interfaceKeyStorePassword.get()), new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            logger.error("SSLContext exception in InterfaceServer", e);
        }

        final SSLServerSocketFactory serverSocketFactory = sSLContext != null ? sSLContext.getServerSocketFactory() : null;
        if (serverSocketFactory != null) {
            try {
                this.serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(Statics.interfaceServerPort.get());
            } catch (IOException iOException) {
                logger.error("IOException in InterfaceServer", iOException);
                try {
                    this.serverSocket = (SSLServerSocket) serverSocketFactory.createServerSocket(0);
                } catch (IOException innerIOException) {
                    logger.error("IOException in InterfaceServer inner", innerIOException);
                }
            }
        } else {
            logger.error("STRANGE serverSocketFactory null in InterfaceServer thread, timeStamp={}", System.currentTimeMillis());
        }

        if (this.serverSocket != null) {
            this.serverSocket.setNeedClientAuth(true);
            Statics.interfaceServerPort.set(this.serverSocket.getLocalPort());
            logger.debug("interfaceServerPort={}", Statics.interfaceServerPort.get());

            while (!SharedStatics.mustStop.get()) {
                try {
                    final Socket socket = this.serverSocket.accept();
                    final InterfaceConnectionThread interfaceConnectionThread = new InterfaceConnectionThread(socket);
                    this.interfaceConnectionThreadsSet.add(interfaceConnectionThread);
                    interfaceConnectionThread.start();
                } catch (IOException iOException) {
                    if (SharedStatics.mustStop.get()) { // program is stopping and the socket has been closed from another thread
                    } else {
                        logger.error("IOException in InterfaceServer socket accept", iOException);
                    }
                } catch (Throwable throwable) {
                    logger.error("STRANGE throwable in InterfaceServer socket accept", throwable);
                }
            } // end while
            logger.debug("closing interface server socket");
            Generic.closeObject(this.serverSocket);
        } else {
            logger.error("STRANGE serverSocket null in InterfaceServer thread, timeStamp={}", System.currentTimeMillis());
        }

        final Set<InterfaceConnectionThread> interfaceConnectionThreadsSetCopy;
        synchronized (this.interfaceConnectionThreadsSet) {
            interfaceConnectionThreadsSetCopy = new HashSet<>(this.interfaceConnectionThreadsSet);
        } // end synchronized
        for (final InterfaceConnectionThread interfaceConnectionThread : interfaceConnectionThreadsSetCopy) {
            if (interfaceConnectionThread.isAlive()) {
                interfaceConnectionThread.closeSocket();
//                logger.info("joining interfaceConnection");
                try {
                    interfaceConnectionThread.join();
                } catch (InterruptedException e) {
                    logger.error("InterruptedException during interfaceConnection join: ", e);
                }
            }
        } // end for
        logger.debug("InterfaceServerThread ends");
    }
}
