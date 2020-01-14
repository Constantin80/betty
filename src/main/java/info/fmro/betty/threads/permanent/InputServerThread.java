package info.fmro.betty.threads.permanent;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class InputServerThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InputServerThread.class);
    private final Set<InputConnectionThread> inputConnectionThreadsSet = Collections.synchronizedSet(new HashSet<>(2));
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    private ServerSocket serverSocket;

    public synchronized void closeSocket() {
        logger.info("closing InputServerThread socket");
        Generic.closeObjects(this.serverSocket);
    }

    @Override
    public void run() {
        try {
            this.serverSocket = new ServerSocket(Statics.inputServerPort.get());
        } catch (IOException iOException) {
            logger.error("IOException in InputServer", iOException);
            try {
                this.serverSocket = new ServerSocket(0);
            } catch (IOException innerIOException) {
                logger.error("IOException in InputServer inner", innerIOException);
            }
        }

        if (this.serverSocket != null) {
            Statics.inputServerPort.set(this.serverSocket.getLocalPort());
            logger.info("inputServerPort={}", Statics.inputServerPort.get());

            while (!Statics.mustStop.get()) {
                try {
                    final Socket socket = this.serverSocket.accept();
                    if ("127.0.0.1".equals(socket.getInetAddress().getHostAddress())) {
                        final InputConnectionThread inputConnectionThread = new InputConnectionThread(socket);
                        this.inputConnectionThreadsSet.add(inputConnectionThread);
                        inputConnectionThread.start();
                    } else {
                        logger.error("rejected connection attempt from {}", socket.getInetAddress().getHostAddress());
                        socket.close();
                    }
                } catch (IOException iOException) {
                    if (Statics.mustStop.get()) { // program is stopping and the socket has been closed from another thread
                    } else {
                        logger.error("IOException in InputServer socket accept", iOException);
                    }
                } catch (Throwable throwable) {
                    logger.error("STRANGE throwable in InputServer socket accept", throwable);
                }
            } // end while
            logger.info("closing input server socket");
            Generic.closeObject(this.serverSocket);
        } else {
            logger.error("STRANGE serverSocket null in InputServer thread, timeStamp={}", System.currentTimeMillis());
        }

        final Set<InputConnectionThread> inputConnectionThreadsSetCopy;
        synchronized (this.inputConnectionThreadsSet) {
            inputConnectionThreadsSetCopy = new HashSet<>(this.inputConnectionThreadsSet);
        } // end synchronized
        for (final InputConnectionThread inputConnectionThread : inputConnectionThreadsSetCopy) {
            if (inputConnectionThread.isAlive()) {
                inputConnectionThread.closeSocket();
//                logger.info("joining inputConnection");
                try {
                    inputConnectionThread.join();
                } catch (InterruptedException e) {
                    logger.error("InterruptedException during inputConnection join: ", e);
                }
            }
        } // end for
    }
}
