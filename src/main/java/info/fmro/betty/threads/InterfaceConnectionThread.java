package info.fmro.betty.threads;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.stream.objects.PoisonPill;
import info.fmro.shared.stream.objects.StreamObjectInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class InterfaceConnectionThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceConnectionThread.class);
    private final Socket socket;
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    private final InterfaceConnectionWriterThread writerThread;
    private final LinkedBlockingQueue<StreamObjectInterface> sendQueue = new LinkedBlockingQueue<>();

    public InterfaceConnectionThread(final Socket socket) {
        super();
        this.socket = socket;
        this.writerThread = new InterfaceConnectionWriterThread(socket, this.sendQueue);
    }

//    public synchronized void sendObject(@NotNull final StreamObjectInterface object) {
//        this.sendQueue.add(object);
//    }

    public synchronized void closeSocket() {
        logger.info("closing InterfaceConnectionThread socket");
        Generic.closeObjects(this.socket);
    }

    @Override
    public void run() {
        this.writerThread.start();
        ObjectInputStream objectInputStream = null;
        try {
            //noinspection resource,IOResourceOpenedButNotSafelyClosed
            objectInputStream = new ObjectInputStream(this.socket.getInputStream());

            Object receivedObject;
            do {
                receivedObject = objectInputStream.readObject();
                if (receivedObject instanceof StreamObjectInterface) {
                    final StreamObjectInterface receivedCommand = (StreamObjectInterface) receivedObject;
                    receivedCommand.runAfterReceive();
                } else if (receivedObject == null) { // nothing to be done, will reach end of loop and exit loop
                } else {
                    logger.error("unknown type of object in interfaceConnection stream: {} {}", receivedObject.getClass(), Generic.objectToString(receivedObject));
                }
            } while (receivedObject != null && !Statics.mustStop.get() && !this.writerThread.finished.get());
        } catch (IOException iOException) {
            if (Statics.mustStop.get()) { // program is stopping and the socket has been closed from another thread
            } else {
                logger.error("IOException in interfaceConnection thread", iOException);
            }
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException in interfaceConnection thread", e);
        } finally {
            //noinspection ConstantConditions
            Generic.closeObjects(objectInputStream, this.socket);
            this.writerThread.finished.set(true);
        }

        if (this.writerThread.isAlive()) {
            this.sendQueue.add(new PoisonPill());
//            logger.info("joining writerThread thread");
            try {
                this.writerThread.join();
            } catch (InterruptedException e) {
                logger.error("InterruptedException in interfaceConnection thread end", e);
            }
        }
        logger.info("reached the end of interfaceConnectionThread");
    }
}
