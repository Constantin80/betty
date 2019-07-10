package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.ChangeType;
import info.fmro.betty.stream.definitions.RequestMessage;
import info.fmro.betty.stream.definitions.SegmentType;
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

    public SubscriptionHandler(final S subscriptionMessage, final boolean isMergeSegments) {
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
    public synchronized C processChangeMessage(C changeMessage) {
        if (this.subscriptionId != changeMessage.getId()) {
            //previous subscription id - ignore
            return null;
        }

        //Every message store timings
        this.lastPublishTime = changeMessage.getPublishTime();
        this.lastArrivalTime = changeMessage.getArrivalTime();

        if (changeMessage.isStartOfRecovery()) {
            //Start of recovery
            this.ttfm.stop();
            logger.info("{}: Start of image", this.subscriptionMessage.getOp());
        }

        if (changeMessage.getChangeType() == ChangeType.HEARTBEAT) {
            //Swallow heartbeats
            changeMessage = null;
        } else if (changeMessage.getSegmentType() != SegmentType.NONE && this.isMergeSegments) {
            //Segmented message and we're instructed to merge (which makes segments look atomic).
            changeMessage = MergeMessage(changeMessage);
        }

        if (changeMessage != null) {
            //store clocks
            if (changeMessage.getInitialClk() != null) {
                this.initialClk = changeMessage.getInitialClk();
            }
            if (changeMessage.getClk() != null) {
                this.clk = changeMessage.getClk();
            }

            if (!this.isSubscribed) {
                //During recovery
                if (changeMessage.getItems() != null) {
                    this.itemCount += changeMessage.getItems().size();
                }
            }

            if (changeMessage.isEndOfRecovery()) {
                //End of recovery
                this.isSubscribed = true;
                this.heartbeatMs = changeMessage.getHeartbeatMs();
                this.conflationMs = changeMessage.getConflateMs();
                this.ttlm.stop();
                logger.info("{}: End of image: type:{}, ttfm:{}, ttlm:{}, conflation:{}, heartbeat:{}, change.items:{}",
                            this.subscriptionMessage.getOp(),
                            changeMessage.getChangeType(),
                            this.ttfm,
                            this.ttlm,
                            this.conflationMs,
                            this.heartbeatMs,
                            this.itemCount);

                //unwind future
                this.subscriptionComplete.countDown();
            }
        }
        return changeMessage;
    }

    private synchronized C MergeMessage(C changeMessage) {
        //merge segmented messages so client sees atomic view across segments
        if (changeMessage.getSegmentType() == SegmentType.SEG_START) {
            //start merging
            this.mergedChanges = new ArrayList<>();
        }
        //accumulate
        this.mergedChanges.addAll(changeMessage.getItems());

        if (changeMessage.getSegmentType() == SegmentType.SEG_END) {
            //finish merging
            changeMessage.setSegmentType(SegmentType.NONE);
            changeMessage.setItems(this.mergedChanges);
            this.mergedChanges = null;
        } else {
            //swallow message as we're still merging
            changeMessage = null;
        }
        return changeMessage;
    }
}
