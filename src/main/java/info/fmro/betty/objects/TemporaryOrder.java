package info.fmro.betty.objects;

import info.fmro.betty.enums.TemporaryOrderType;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.definitions.Side;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class TemporaryOrder
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(TemporaryOrder.class);
    public static final long defaultTooOldPeriod = Generic.MINUTE_LENGTH_MILLISECONDS * 10L;
    private static final long serialVersionUID = -6977868264246613172L;
    private final TemporaryOrderType type;
    private final String marketId;
    private final RunnerId runnerId;
    private final Side side;
    private final double price, size;
    private final Double sizeReduction;
    private final long creationTime;
    private String betId;
    private long expirationTime;

    public TemporaryOrder(final String marketId, final RunnerId runnerId, final Side side, final double price, final double size) {
        this.type = TemporaryOrderType.PLACE;
        this.marketId = marketId;
        this.runnerId = runnerId;
        this.side = side;
        this.price = price;
        this.size = size;
        this.sizeReduction = null;
        this.creationTime = System.currentTimeMillis();
    }

    public TemporaryOrder(final String marketId, final RunnerId runnerId, final Side side, final double price, final double size, final String betId, final Double sizeReduction) {
        this.type = TemporaryOrderType.CANCEL;
        this.marketId = marketId;
        this.betId = betId;
        this.runnerId = runnerId;
        this.side = side;
        this.price = price;
        this.size = size;
        this.sizeReduction = sizeReduction;
        this.creationTime = System.currentTimeMillis();
    }

    public synchronized boolean runnerEquals(final String marketId, final RunnerId runnerId) {
        return marketId != null && marketId.equals(this.marketId) && runnerId != null && runnerId.equals(this.runnerId);
    }

    public synchronized TemporaryOrderType getType() {
        return type;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized RunnerId getRunnerId() {
        return runnerId;
    }

    public synchronized Side getSide() {
        return side;
    }

    public synchronized double getPrice() {
        return price;
    }

    public synchronized double getSize() {
        return size;
    }

    public synchronized Double getSizeReduction() {
        return sizeReduction;
    }

    public synchronized long getCreationTime() {
        return creationTime;
    }

    public synchronized String getBetId() {
        return betId;
    }

    public synchronized void setBetId(final String betId) {
        this.betId = betId;
    }

    public synchronized long getExpirationTime() {
        return expirationTime;
    }

    public synchronized void setExpirationTime(final long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public synchronized boolean isExpired() {
        final long currentTime = System.currentTimeMillis();
        return isExpired(currentTime);
    }

    public synchronized boolean isExpired(final long currentTime) {
        return this.expirationTime > 0L && currentTime >= this.expirationTime;
    }

    public synchronized boolean isTooOld() {
        final long currentTime = System.currentTimeMillis();
        return isTooOld(currentTime);
    }

    public synchronized boolean isTooOld(final long currentTime) {
        return currentTime >= this.creationTime + TemporaryOrder.defaultTooOldPeriod;
    }

    @Override
    public synchronized boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TemporaryOrder that = (TemporaryOrder) o;
        return Double.compare(that.price, price) == 0 &&
               Double.compare(that.size, size) == 0 &&
               type == that.type &&
               Objects.equals(marketId, that.marketId) &&
               Objects.equals(runnerId, that.runnerId) &&
               side == that.side &&
               Objects.equals(sizeReduction, that.sizeReduction) &&
               Objects.equals(betId, that.betId);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(type, marketId, runnerId, side, price, size, sizeReduction, betId);
    }
}
