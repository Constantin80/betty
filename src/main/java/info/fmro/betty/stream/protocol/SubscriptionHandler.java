package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.ChangeType;
import info.fmro.betty.stream.definitions.RequestMessage;
import info.fmro.betty.stream.definitions.SegmentType;
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
    private boolean isMergeSegments;
    private List<I> mergedChanges;
    private StopWatch ttfm;
    private StopWatch ttlm;
    private int itemCount;
    private CountDownLatch subscriptionComplete = new CountDownLatch(1);

    private Date lastPublishTime;
    private Date lastArrivalTime;
    private String initialClk;
    private String clk;
    private Long heartbeatMs;
    private Long conflationMs;

    public SubscriptionHandler(S subscriptionMessage, boolean isMergeSegments) {
        this.subscriptionMessage = subscriptionMessage;
        this.isMergeSegments = isMergeSegments;
        isSubscribed = false;
        subscriptionId = subscriptionMessage.getId();
        ttfm = new StopWatch("ttfm");
        ttlm = new StopWatch("ttlm");
    }

    public synchronized S getSubscriptionMessage() {
        return subscriptionMessage;
    }

    public synchronized boolean isSubscribed() {
        return isSubscribed;
    }

    public synchronized Date getLastPublishTime() {
        return lastPublishTime == null ? null : (Date) lastPublishTime.clone();
    }

    public synchronized Date getLastArrivalTime() {
        return lastArrivalTime == null ? null : (Date) lastArrivalTime.clone();
    }

    public synchronized Long getHeartbeatMs() {
        return heartbeatMs;
    }

    public synchronized Long getConflationMs() {
        return conflationMs;
    }

    public synchronized String getInitialClk() {
        return initialClk;
    }

    public synchronized String getClk() {
        return clk;
    }

    public synchronized void cancel() {
        //unwind waiters
        subscriptionComplete.countDown();
    }

    public synchronized C processChangeMessage(C changeMessage) {
        if (subscriptionId != changeMessage.getId()) {
            //previous subscription id - ignore
            return null;
        }

        //Every message store timings
        lastPublishTime = changeMessage.getPublishTime();
        lastArrivalTime = changeMessage.getArrivalTime();

        if (changeMessage.isStartOfRecovery()) {
            //Start of recovery
            ttfm.stop();
            logger.info("{}: Start of image", subscriptionMessage.getOp());
        }

        if (changeMessage.getChangeType() == ChangeType.HEARTBEAT) {
            //Swallow heartbeats
            changeMessage = null;
        } else if (changeMessage.getSegmentType() != SegmentType.NONE && isMergeSegments) {
            //Segmented message and we're instructed to merge (which makes segments look atomic).
            changeMessage = MergeMessage(changeMessage);
        }

        if (changeMessage != null) {
            //store clocks
            if (changeMessage.getInitialClk() != null) {
                initialClk = changeMessage.getInitialClk();
            }
            if (changeMessage.getClk() != null) {
                clk = changeMessage.getClk();
            }

            if (!isSubscribed) {
                //During recovery
                if (changeMessage.getItems() != null) {
                    itemCount += changeMessage.getItems().size();
                }
            }

            if (changeMessage.isEndOfRecovery()) {
                //End of recovery
                isSubscribed = true;
                heartbeatMs = changeMessage.getHeartbeatMs();
                conflationMs = changeMessage.getConflateMs();
                ttlm.stop();
                logger.info("{}: End of image: type:{}, ttfm:{}, ttlm:{}, conflation:{}, heartbeat:{}, change.items:{}",
                            subscriptionMessage.getOp(),
                            changeMessage.getChangeType(),
                            ttfm,
                            ttlm,
                            conflationMs,
                            heartbeatMs,
                            itemCount);

                //unwind future
                subscriptionComplete.countDown();
            }
        }
        return changeMessage;
    }

    private synchronized C MergeMessage(C changeMessage) {
        //merge segmented messages so client sees atomic view across segments
        if (changeMessage.getSegmentType() == SegmentType.SEG_START) {
            //start merging
            mergedChanges = new ArrayList<>();
        }
        //accumulate
        mergedChanges.addAll(changeMessage.getItems());

        if (changeMessage.getSegmentType() == SegmentType.SEG_END) {
            //finish merging
            changeMessage.setSegmentType(SegmentType.NONE);
            changeMessage.setItems(mergedChanges);
            mergedChanges = null;
        } else {
            //swallow message as we're still merging
            changeMessage = null;
        }
        return changeMessage;
    }
}