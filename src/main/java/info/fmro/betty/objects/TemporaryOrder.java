package info.fmro.betty.objects;

import info.fmro.betty.enums.TemporaryOrderType;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.enums.Side;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class TemporaryOrder
        implements Serializable {
    public static final long defaultTooOldPeriod = Generic.MINUTE_LENGTH_MILLISECONDS * 10L;
    private static final long serialVersionUID = -6977868264246613172L;
    private final TemporaryOrderType type;
    private final String marketId;
    private final RunnerId runnerId;
    private final Side side;
    private final double price, size;
    @Nullable
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

    @SuppressWarnings("ConstructorWithTooManyParameters")
    public TemporaryOrder(final String marketId, final RunnerId runnerId, final Side side, final double price, final double size, final String betId, @Nullable final Double sizeReduction) {
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

    public synchronized boolean runnerEquals(final String marketIdToCheck, final RunnerId runnerIdToCheck) {
        return marketIdToCheck != null && marketIdToCheck.equals(this.marketId) && runnerIdToCheck != null && runnerIdToCheck.equals(this.runnerId);
    }

    public synchronized TemporaryOrderType getType() {
        return this.type;
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    public synchronized RunnerId getRunnerId() {
        return this.runnerId;
    }

    public synchronized Side getSide() {
        return this.side;
    }

    public synchronized double getPrice() {
        return this.price;
    }

    public synchronized double getSize() {
        return this.size;
    }

    @Nullable
    public synchronized Double getSizeReduction() {
        return this.sizeReduction;
    }

    public synchronized long getCreationTime() {
        return this.creationTime;
    }

    public synchronized String getBetId() {
        return this.betId;
    }

    public synchronized void setBetId(final String betId) {
        this.betId = betId;
    }

    public synchronized long getExpirationTime() {
        return this.expirationTime;
    }

    public synchronized void setExpirationTime(final long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public synchronized boolean isExpired() {
        final long currentTime = System.currentTimeMillis();
        return isExpired(currentTime);
    }

    @Contract(pure = true)
    private synchronized boolean isExpired(final long currentTime) {
        return this.expirationTime > 0L && currentTime >= this.expirationTime;
    }

    public synchronized boolean isTooOld() {
        final long currentTime = System.currentTimeMillis();
        return isTooOld(currentTime);
    }

    public synchronized boolean isTooOld(final long currentTime) {
        return currentTime >= this.creationTime + TemporaryOrder.defaultTooOldPeriod;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final TemporaryOrder that = (TemporaryOrder) obj;
        return Double.compare(that.price, this.price) == 0 &&
               Double.compare(that.size, this.size) == 0 &&
               this.type == that.type &&
               Objects.equals(this.marketId, that.marketId) &&
               Objects.equals(this.runnerId, that.runnerId) &&
               this.side == that.side &&
               Objects.equals(this.sizeReduction, that.sizeReduction);
        // && Objects.equals(this.betId, that.betId);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.type, this.marketId, this.runnerId, this.side, this.price, this.size, this.sizeReduction); // , this.betId
    }
}
