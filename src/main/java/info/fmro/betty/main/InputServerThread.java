package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class InputServerThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InputServerThread.class);

    @Override
    public void run() {
        ServerSocket serverSocket = null;
        try {
            //noinspection resource,IOResourceOpenedButNotSafelyClosed,SocketOpenedButNotSafelyClosed
            serverSocket = new ServerSocket(Statics.inputServerPort.get());
        } catch (IOException iOException) {
            logger.error("IOException in InputServer", iOException);
            try {
                //noinspection resource,IOResourceOpenedButNotSafelyClosed,SocketOpenedButNotSafelyClosed
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
                    final Socket socket = serverSocket.accept();
                    if ("127.0.0.1".equals(socket.getInetAddress().getHostAddress())) {
                        Statics.inputConnectionSocketsSet.add(socket);
                        final InputConnectionThread inputConnectionThread = new InputConnectionThread(socket);
                        Statics.inputConnectionThreadsSet.add(inputConnectionThread);
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
            Statics.inputServerSocketsSet.remove(serverSocket);
            logger.info("closing input server socket");
            Generic.closeObject(serverSocket);
        } else {
            logger.error("STRANGE serverSocket null in InputServer thread, timeStamp={}", System.currentTimeMillis());
        }
    }
}
