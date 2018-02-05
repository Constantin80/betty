package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InputServerThread
        extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(InputServerThread.class);

    @Override
    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(Statics.inputServerPort.get());
        } catch (IOException iOException) {
            logger.error("IOException in InputServer", iOException);
            try {
                serverSocket = new ServerSocket(0);
            } catch (IOException innerIOException) {
                logger.error("IOException in InputServer inner", innerIOException);
            }
        }

        if (serverSocket != null) {
            Statics.inputServerSocketsSet.add(serverSocket);
            Statics.inputServerPort.set(serverSocket.getLocalPort());
            logger.info("inputServerPort={}", Statics.inputServerPort.get());

            while (!Statics.mustStop.get()) {
                try {
                    Socket socket = serverSocket.accept();
                    if (socket.getInetAddress().getHostAddress().equals("127.0.0.1")) {
                        Statics.inputConnectionSocketsSet.add(socket);
                        InputConnectionThread inputConnectionThread = new InputConnectionThread(socket);
                        Statics.inputConnectionThreadsSet.add(inputConnectionThread);
                        inputConnectionThread.start();
                    } else {
                        logger.error("rejected connection attempt from {}", socket.getInetAddress().getHostAddress());
                        socket.close();
                    }
                } catch (IOException iOException) {
                    if (!Statics.mustStop.get()) {
                        logger.error("IOException in InputServer socket accept", iOException);
                    } else { // program is stopping and the socket has been closed from another thread
                    }
                } catch (Throwable throwable) {
                    logger.error("STRANGE throwable in InputServer socket accept", throwable);
                }
            } // end while
            Statics.inputServerSocketsSet.remove(serverSocket);
            logger.info("closing input server socket");
            Generic.closeObject(serverSocket);
        } else {
            logger.error("STRANGE serverSocket null in InputServer thread, timeStamp={}", System.currentTimeMillis());
        }
    }
}
