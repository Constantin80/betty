package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.ChangeType;
import info.fmro.betty.stream.definitions.MarketChange;
import info.fmro.betty.stream.definitions.MarketChangeMessage;
import info.fmro.betty.stream.definitions.OrderChangeMessage;
import info.fmro.betty.stream.definitions.OrderMarketChange;
import info.fmro.betty.stream.definitions.SegmentType;

/**
 * Adapts market or order changes to a common change message
 * Created by mulveyj on 07/07/2016.
 */
public class ChangeMessageFactory {
    public static ChangeMessage<MarketChange> ToChangeMessage(int clientId, MarketChangeMessage message) {
        final ChangeMessage<MarketChange> change = new ChangeMessage<>(clientId);
        change.setId(message.getId());
        change.setPublishTime(message.getPt());
        change.setClk(message.getClk());
        change.setInitialClk(message.getInitialClk());
        change.setConflateMs(message.getConflateMs());
        change.setHeartbeatMs(message.getHeartbeatMs());

        change.setItems(message.getMc());

        SegmentType segmentType = SegmentType.NONE;
        if (message.getSegmentType() != null) {
            switch (message.getSegmentType()) {
                case SEG_START:
                    segmentType = SegmentType.SEG_START;
                    break;
                case SEG_END:
                    segmentType = SegmentType.SEG_END;
                    break;
                case SEG:
                    segmentType = SegmentType.SEG;
                    break;
            }
        }
        change.setSegmentType(segmentType);

        ChangeType changeType = ChangeType.UPDATE;
        if (message.getCt() != null) {
            switch (message.getCt()) {
                case HEARTBEAT:
                    changeType = ChangeType.HEARTBEAT;
                    break;
                case RESUB_DELTA:
                    changeType = ChangeType.RESUB_DELTA;
                    break;
                case SUB_IMAGE:
                    changeType = ChangeType.SUB_IMAGE;
                    break;
            }
        }
        change.setChangeType(changeType);

        return change;
    }

    public static ChangeMessage<OrderMarketChange> ToChangeMessage(int clientId, OrderChangeMessage message) {
        ChangeMessage<OrderMarketChange> change = new ChangeMessage<>(clientId);
        change.setId(message.getId());
        change.setPublishTime(message.getPt());
        change.setClk(message.getClk());
        change.setInitialClk(message.getInitialClk());
        change.setConflateMs(message.getConflateMs());
        change.setHeartbeatMs(message.getHeartbeatMs());

        change.setItems(message.getOc());

        SegmentType segmentType = SegmentType.NONE;
        if (message.getSegmentType() != null) {
            switch (message.getSegmentType()) {
                case SEG_START:
                    segmentType = SegmentType.SEG_START;
                    break;
                case SEG_END:
                    segmentType = SegmentType.SEG_END;
                    break;
                case SEG:
                    segmentType = SegmentType.SEG;
                    break;
            }
        }
        change.setSegmentType(segmentType);

        ChangeType changeType = ChangeType.UPDATE;
        if (message.getCt() != null) {
            switch (message.getCt()) {
                case HEARTBEAT:
                    changeType = ChangeType.HEARTBEAT;
                    break;
                case RESUB_DELTA:
                    changeType = ChangeType.RESUB_DELTA;
                    break;
                case SUB_IMAGE:
                    changeType = ChangeType.SUB_IMAGE;
                    break;
            }
        }
        change.setChangeType(changeType);

        return change;
    }
}
