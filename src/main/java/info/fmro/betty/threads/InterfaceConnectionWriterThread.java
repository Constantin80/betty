package info.fmro.betty.threads;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.stream.objects.PoisonPill;
import info.fmro.shared.stream.objects.StreamObjectInterface;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class InterfaceConnectionWriterThread
        extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(InterfaceConnectionWriterThread.class);
    private final ObjectOutputStream objectOutputStream;
    @SuppressWarnings("PackageVisibleField")
    final AtomicBoolean finished = new AtomicBoolean();
    private final @NotNull LinkedBlockingQueue<StreamObjectInterface> sendQueue;

    InterfaceConnectionWriterThread(@NotNull final Socket socket, @NotNull final LinkedBlockingQueue<StreamObjectInterface> sendQueue) {
        super();
        ObjectOutputStream tempObjectOutputStream = null;
        try {
            //noinspection resource,IOResourceOpenedButNotSafelyClosed
            tempObjectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            logger.error("IOException in InterfaceConnectionWriterThread constructor", e);
            this.finished.set(true);
        }
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
                logger.error("IOException in InterfaceConnectionWriterThread.sendObject", e);
            }
        }
    }

    @Override
    public void run() {
        Statics.marketCache.listOfQueues.registerQueue(this.sendQueue, Statics.marketCache);
        Statics.orderCache.listOfQueues.registerQueue(this.sendQueue, Statics.orderCache);
        Statics.rulesManagerThread.rulesManager.listOfQueues.registerQueue(this.sendQueue, Statics.rulesManagerThread.rulesManager);
        Statics.safetyLimits.existingFunds.listOfQueues.registerQueue(this.sendQueue, Statics.safetyLimits.existingFunds);
        Statics.marketCataloguesMap.listOfQueues.registerQueue(this.sendQueue, Statics.marketCataloguesMap);
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

        logger.info("InterfaceConnectionWriterThread ends");
    }
}
