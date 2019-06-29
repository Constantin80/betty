package info.fmro.betty.objects;

import info.fmro.betty.enums.Side;
import java.util.Objects;

public class OrderPrice
        implements Comparable<OrderPrice> {

    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private final String marketId;
    private final long selectionId;
    private final Side side;
    private final double price;

    public OrderPrice(final String marketId, final long selectionId, final Side side, final double price) {
        this.marketId = marketId;
        this.selectionId = selectionId;
        this.side = side;
        this.price = price;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized long getSelectionId() {
        return selectionId;
    }

    public synchronized Side getSide() {
        return side;
    }

    public synchronized double getPrice() {
        return price;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(final OrderPrice other) {
        if (other == null) {
            return AFTER;
        }
        if (this == other) {
            return EQUAL;
        }

        if (this.getClass() != other.getClass()) {
            if (this.getClass().hashCode() < other.getClass().hashCode()) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (this.selectionId != other.selectionId) {
            if (this.selectionId < other.selectionId) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (!Objects.equals(this.marketId, other.marketId)) {
            if (this.marketId == null) {
                return BEFORE;
            }
            if (other.marketId == null) {
                return AFTER;
            }
            return this.marketId.compareTo(other.marketId);
        }
        if (this.price != other.price) {
            if (this.price < other.price) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (!Objects.equals(this.side, other.side)) {
            if (this.side == null) {
                return BEFORE;
            }
            if (other.side == null) {
                return AFTER;
            }
            return this.side.compareTo(other.side);
        }

        return EQUAL;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final OrderPrice other = (OrderPrice) obj;
        if (!Objects.equals(this.marketId, other.marketId)) {
            return false;
        }
        if (this.selectionId != other.selectionId) {
            return false;
        }
        if (this.side != other.side) {
            return false;
        }
        return Double.doubleToLongBits(this.price) == Double.doubleToLongBits(other.price);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.marketId);
        hash = 67 * hash + (int) (this.selectionId ^ (this.selectionId >>> 32));
        hash = 67 * hash + Objects.hashCode(this.side);
        hash = 67 * hash + (int) (Double.doubleToLongBits(this.price) ^ (Double.doubleToLongBits(this.price) >>> 32));
        return hash;
    }
}
