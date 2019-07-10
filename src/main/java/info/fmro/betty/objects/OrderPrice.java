package info.fmro.betty.objects;

import info.fmro.betty.enums.Side;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OrderPrice
        implements Comparable<OrderPrice> {
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private final String marketId;
    private final long selectionId;
    private final Side side;
    private final double price;

    @Contract(pure = true)
    public OrderPrice(final String marketId, final long selectionId, final Side side, final double price) {
        this.marketId = marketId;
        this.selectionId = selectionId;
        this.side = side;
        this.price = price;
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    public synchronized long getSelectionId() {
        return this.selectionId;
    }

    public synchronized Side getSide() {
        return this.side;
    }

    public synchronized double getPrice() {
        return this.price;
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
        final OrderPrice that = (OrderPrice) obj;
        return this.selectionId == that.selectionId &&
               Double.compare(that.price, this.price) == 0 &&
               Objects.equals(this.marketId, that.marketId) &&
               this.side == that.side;
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.marketId, this.selectionId, this.side, this.price);
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public synchronized int compareTo(@NotNull final OrderPrice o) {
        //noinspection ConstantConditions
        if (o == null) {
            return AFTER;
        }
        if (this == o) {
            return EQUAL;
        }

        if (this.getClass() != o.getClass()) {
            return this.getClass().hashCode() < o.getClass().hashCode() ? BEFORE : AFTER;
        }
        if (this.selectionId != o.selectionId) {
            return this.selectionId < o.selectionId ? BEFORE : AFTER;
        }
        if (!Objects.equals(this.marketId, o.marketId)) {
            if (this.marketId == null) {
                return BEFORE;
            }
            if (o.marketId == null) {
                return AFTER;
            }
            return this.marketId.compareTo(o.marketId);
        }
        //noinspection FloatingPointEquality
        if (this.price != o.price) {
            return this.price < o.price ? BEFORE : AFTER;
        }
        if (this.side != o.side) {
            if (this.side == null) {
                return BEFORE;
            }
            if (o.side == null) {
                return AFTER;
            }
            return this.side.compareTo(o.side);
        }

        return EQUAL;
    }
}
