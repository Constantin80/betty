package info.fmro.betty.threads;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.stream.objects.PoisonPill;
import info.fmro.shared.stream.objects.StreamObjectInterface;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class InterfaceConnectionWriterThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceConnectionWriterThread.class);
    private final ObjectOutputStream objectOutputStream;
    private final OutputStream outputStream;
    @SuppressWarnings("PackageVisibleField")
    final AtomicBoolean finished = new AtomicBoolean();
    @NotNull
    private final LinkedBlockingQueue<StreamObjectInterface> sendQueue;

    InterfaceConnectionWriterThread(@NotNull final Socket socket, @NotNull final LinkedBlockingQueue<StreamObjectInterface> sendQueue) {
        super();
        ObjectOutputStream tempObjectOutputStream = null;
        OutputStream tempOutputStream = null;
        try {
            tempOutputStream = socket.getOutputStream();
            //noinspection resource,IOResourceOpenedButNotSafelyClosed
            tempObjectOutputStream = new ObjectOutputStream(tempOutputStream);
        } catch (IOException e) {
            logger.error("IOException in InterfaceConnectionWriterThread constructor", e);
            this.finished.set(true);
        }
        this.outputStream = tempOutputStream;
        this.objectOutputStream = tempObjectOutputStream;
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.sendQueue = sendQueue;
    }

    private synchronized void sendObject(@NotNull final StreamObjectInterface object) {
        if (object instanceof PoisonPill) {
            logger.info("poison pill received by InterfaceConnectionWriterThread");
            this.finished.set(true);
        } else {
            // any runBeforeSend logic can be added here

            try {
                this.objectOutputStream.writeObject(object); // not synchronized, so when sending initialImage I'll send a copy of the main cache object
                this.objectOutputStream.flush();
            } catch (IOException e) {
                if (e.getClass().equals(SocketException.class) && (Statics.mustStop.get() || this.finished.get())) {
                    logger.info("IOException in InterfaceConnectionWriterThread.sendObject, thread ending: {}", e.toString());
                } else {
                    logger.error("IOException in InterfaceConnectionWriterThread.sendObject", e);
                    this.finished.set(true); // I should close socket after an exception
                }
            }
        }
    }

//    public void close() { // this is hard shutoff, probably best to not synchronize
//        logger.info("closing InterfaceConnectionWriterThread stream");
//        Generic.closeObjects(this.objectOutputStream, this.outputStream);
//    }

    @Override
    public void run() {
        Statics.marketCache.listOfQueues.registerQueue(this.sendQueue, Statics.marketCache);
        Statics.orderCache.listOfQueues.registerQueue(this.sendQueue, Statics.orderCache);
        Statics.rulesManagerThread.rulesManager.listOfQueues.registerQueue(this.sendQueue, Statics.rulesManagerThread.rulesManager);
        Statics.safetyLimits.existingFunds.listOfQueues.registerQueue(this.sendQueue, Statics.safetyLimits.existingFunds);
        Statics.marketCataloguesMap.listOfQueues.registerQueue(this.sendQueue, Statics.marketCataloguesMap);
        Statics.eventsMap.listOfQueues.registerQueue(this.sendQueue, Statics.eventsMap);
        //        this.sendObject(initialImage);

        while (!Statics.mustStop.get() && !this.finished.get()) {
            try {
                this.sendObject(this.sendQueue.take());
            } catch (InterruptedException e) {
                logger.error("InterruptedException in InterfaceConnectionWriterThread main loop", e);
            }
        } // end while
        Statics.marketCache.listOfQueues.removeQueue(this.sendQueue);
        Statics.orderCache.listOfQueues.removeQueue(this.sendQueue);
        Statics.rulesManagerThread.rulesManager.listOfQueues.removeQueue(this.sendQueue);
        Statics.safetyLimits.existingFunds.listOfQueues.removeQueue(this.sendQueue);
        Statics.marketCataloguesMap.listOfQueues.removeQueue(this.sendQueue);
        Statics.eventsMap.listOfQueues.removeQueue(this.sendQueue);
        Generic.closeObjects(this.objectOutputStream, this.outputStream);

        logger.info("InterfaceConnectionWriterThread ends");
    }
}
