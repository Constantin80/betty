package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.enums.ChangeType;
import info.fmro.betty.stream.definitions.RequestMessage;
import info.fmro.betty.stream.enums.SegmentType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.StopWatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Generic subscription handler for change messages:
 * 1) Tracks clocks to facilitate resubscripiton
 * 2) Provides useful timings for initial image
 * 3) Supports the ability to re-combine segmented messages to retain event level atomicity
 * Created by mulveyj on 07/07/2016.
 */
public class SubscriptionHandler<S extends RequestMessage, C extends ChangeMessage<I>, I> {
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionHandler.class);
    private final int subscriptionId;
    private final S subscriptionMessage;
    private boolean isSubscribed;
    private final boolean isMergeSegments;
    @Nullable
    private List<I> mergedChanges;
    private final StopWatch ttfm;
    private final StopWatch ttlm;
    private int itemCount;
    private final CountDownLatch subscriptionComplete = new CountDownLatch(1);

    private Date lastPublishTime;
    private Date lastArrivalTime;
    private String initialClk;
    private String clk;
    private Long heartbeatMs;
    private Long conflationMs;

    public SubscriptionHandler(@NotNull final S subscriptionMessage, final boolean isMergeSegments) {
        this.subscriptionMessage = subscriptionMessage;
        this.isMergeSegments = isMergeSegments;
        this.isSubscribed = false;
        this.subscriptionId = subscriptionMessage.getId();
        this.ttfm = new StopWatch("ttfm");
        this.ttlm = new StopWatch("ttlm");
    }

    public synchronized int getSubscriptionId() {
        return this.subscriptionId;
    }

    public synchronized S getSubscriptionMessage() {
        return this.subscriptionMessage;
    }

    @SuppressWarnings("SuspiciousGetterSetter")
    public synchronized boolean isSubscribed() {
        return this.isSubscribed;
    }

    @Nullable
    public synchronized Date getLastPublishTime() {
        return this.lastPublishTime == null ? null : (Date) this.lastPublishTime.clone();
    }

    @Nullable
    public synchronized Date getLastArrivalTime() {
        return this.lastArrivalTime == null ? null : (Date) this.lastArrivalTime.clone();
    }

    public synchronized Long getHeartbeatMs() {
        return this.heartbeatMs;
    }

    public synchronized Long getConflationMs() {
        return this.conflationMs;
    }

    public synchronized String getInitialClk() {
        return this.initialClk;
    }

    public synchronized String getClk() {
        return this.clk;
    }

    public synchronized void cancel() {
        //unwind waiters
        this.subscriptionComplete.countDown();
    }

    @Nullable
    public synchronized C processChangeMessage(@NotNull final C changeMessage) {
        @Nullable C message = changeMessage;
        if (this.subscriptionId == message.getId()) {
            //Every message store timings
            this.lastPublishTime = message.getPublishTime();
            this.lastArrivalTime = message.getArrivalTime();
            if (message.isStartOfRecovery()) {
                //Start of recovery
                this.ttfm.stop();
                logger.info("{}: Start of image", this.subscriptionMessage.getOp());
            }

            if (message.getChangeType() == ChangeType.HEARTBEAT) {
                //Swallow heartbeats
                message = null;
            } else if (message.getSegmentType() != SegmentType.NONE && this.isMergeSegments) {
                //Segmented message and we're instructed to merge (which makes segments look atomic).
                message = MergeMessage(message);

                if (message != null) {
                    //store clocks
                    if (message.getInitialClk() != null) {
                        this.initialClk = message.getInitialClk();
                    }
                    if (message.getClk() != null) {
                        this.clk = message.getClk();
                    }

                    if (!this.isSubscribed) {
                        //During recovery
                        if (message.getItems() != null) {
                            this.itemCount += message.getItems().size();
                        }
                    }

                    if (message.isEndOfRecovery()) {
                        //End of recovery
                        this.isSubscribed = true;
                        this.heartbeatMs = message.getHeartbeatMs();
                        this.conflationMs = message.getConflateMs();
                        this.ttlm.stop();
                        logger.info("{}: End of image: type:{}, ttfm:{}, ttlm:{}, conflation:{}, heartbeat:{}, change.items:{}", this.subscriptionMessage.getOp(), message.getChangeType(), this.ttfm, this.ttlm, this.conflationMs, this.heartbeatMs,
                                    this.itemCount);

                        //unwind future
                        this.subscriptionComplete.countDown();
                    }
                }
            }
        } else {
            //previous subscription id - ignore
            message = null;
        }
        return message;
    }

    private synchronized C MergeMessage(@NotNull final C changeMessage) {
        //merge segmented messages so client sees atomic view across segments
        @Nullable C message = changeMessage;
        if (message.getSegmentType() == SegmentType.SEG_START) {
            //start merging
            this.mergedChanges = new ArrayList<>(2);
        }
        //accumulate
        if (this.mergedChanges != null) {
            final List<I> messageItems = message.getItems();
            if (messageItems != null) {
                this.mergedChanges.addAll(messageItems);
            } else { // no message items to add
            }
        } else { // can't accumulate
        }

        if (message.getSegmentType() == SegmentType.SEG_END) {
            //finish merging
            message.setSegmentType(SegmentType.NONE);
            message.setItems(this.mergedChanges);
            this.mergedChanges = null;
        } else {
            //swallow message as we're still merging
            message = null;
        }
        return message;
    }
}
