package info.fmro.betty.threads.permanent;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.StreamObjectInterface;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class InterfaceConnectionThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceConnectionThread.class);
    private final Socket socket;
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
    private ObjectOutputStream objectOutputStream;
    private final AtomicBoolean outputStreamIsInitialized = new AtomicBoolean();

    InterfaceConnectionThread(final Socket socket) {
        super();
        this.socket = socket;
    }

    public synchronized void sendObject(@NotNull final StreamObjectInterface object, long counter) { // todo I need to order the objects being sent, else I can get erratic out of order behaviour
        object.runBeforeSend();
        while (!Statics.mustStop.get() && !this.outputStreamIsInitialized.get()) {
            logger.error("waiting for outputStream to be initialized in InterfaceConnectionThread");
            Generic.threadSleepSegmented(1_000L, 50L, Statics.mustStop, this.outputStreamIsInitialized);
        }
        try {
            this.objectOutputStream.writeObject(object);
            this.objectOutputStream.flush();
        } catch (IOException e) {
            logger.error("IOException in InterfaceConnectionThread.sendObject", e);
        }
    }

    @Override
    public void run() {
        ObjectInputStream objectInputStream = null;
        try {
            this.objectOutputStream = new ObjectOutputStream(this.socket.getOutputStream());
            this.objectOutputStream.writeObject(initialImage); // todo initialImage will send a counter for objects send, that is unique for the entire program, and each sendObject command will increment the counter
            this.objectOutputStream.flush();
            this.outputStreamIsInitialized.set(true);
            //noinspection resource,IOResourceOpenedButNotSafelyClosed
            objectInputStream = new ObjectInputStream(this.socket.getInputStream());

            Object receivedObject = objectInputStream.readObject();
            while (receivedObject != null && !Statics.mustStop.get()) {
                if (receivedObject instanceof StreamObjectInterface) {
                    final StreamObjectInterface receivedCommand = (StreamObjectInterface) receivedObject;
                    receivedCommand.runAfterReceive();
                } else {
                    logger.error("unknown type of object in interfaceConnection stream: {} {}", receivedObject.getClass(), Generic.objectToString(receivedObject));
                }

                receivedObject = objectInputStream.readObject();
            }
        } catch (IOException iOException) {
            if (Statics.mustStop.get()) { // program is stopping and the socket has been closed from another thread
            } else {
                logger.error("IOException in interfaceConnection thread", iOException);
            }
        } catch (ClassNotFoundException e) {
            logger.error("ClassNotFoundException in interfaceConnection thread", e);
        } finally {
            this.outputStreamIsInitialized.set(false);
            Generic.closeObjects(this.objectOutputStream, objectInputStream, this.socket);
            Statics.interfaceConnectionSocketsSet.remove(this.socket);
            Statics.interfaceConnectionThreadsSet.remove(this);
        }
        logger.info("reached the end of interfaceConnectionThread");
    }
}
