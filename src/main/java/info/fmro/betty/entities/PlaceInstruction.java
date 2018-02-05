package info.fmro.betty.entities;

import info.fmro.betty.enums.OrderType;
import info.fmro.betty.enums.Side;
import info.fmro.shared.utility.Generic;
import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlaceInstruction
        implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(PlaceInstruction.class);
    private static final long serialVersionUID = -1136840098955839521L;
    private OrderType orderType;
    private Long selectionId;
    private Double handicap;
    private Side side;
    private LimitOrder limitOrder;
    private LimitOnCloseOrder limitOnCloseOrder;
    private MarketOnCloseOrder marketOnCloseOrder;

    public PlaceInstruction() {
    }

    public synchronized double getPlacedAmount(boolean isEachWayMarket) {
        double amount;

        if (orderType == null) {
            logger.error("null orderType in PlaceInstruction getPlacedAmount for: {}", Generic.objectToString(this));
            amount = 0d; // initialized

            if (limitOrder != null) {
                amount += limitOrder.getLiability(side, isEachWayMarket);
            }
            if (limitOnCloseOrder != null) {
                final Double limitOnCloseOrderLiabilityObject = limitOnCloseOrder.getLiability();
                final double limitOnCloseOrderLiability = limitOnCloseOrderLiabilityObject == null ? 0d : limitOnCloseOrderLiabilityObject;
                amount += limitOnCloseOrderLiability;
            }
            if (marketOnCloseOrder != null) {
                final Double marketOnCloseOrderLiabilityObject = marketOnCloseOrder.getLiability();
                final double marketOnCloseOrderLiability = marketOnCloseOrderLiabilityObject == null ? 0d : marketOnCloseOrderLiabilityObject;
                amount += marketOnCloseOrderLiability;
            }
        } else if (orderType.equals(OrderType.LIMIT)) {
            amount = limitOrder.getLiability(side, isEachWayMarket);
        } else if (orderType.equals(OrderType.LIMIT_ON_CLOSE)) {
            final Double limitOnCloseOrderLiabilityObject = limitOnCloseOrder.getLiability();
            amount = limitOnCloseOrderLiabilityObject == null ? 0d : limitOnCloseOrderLiabilityObject;
        } else if (orderType.equals(OrderType.MARKET_ON_CLOSE)) {
            final Double marketOnCloseOrderLiabilityObject = marketOnCloseOrder.getLiability();
            amount = marketOnCloseOrderLiabilityObject == null ? 0d : marketOnCloseOrderLiabilityObject;
        } else { // unsupported OrderType
            logger.error("unsupported orderType {} in PlaceInstruction getPlacedAmount for: {}", orderType, Generic.objectToString(this));
            amount = 0d; // initialized

            if (limitOrder != null) {
                amount += limitOrder.getLiability(side, isEachWayMarket);
            }
            if (limitOnCloseOrder != null) {
                final Double limitOnCloseOrderLiabilityObject = limitOnCloseOrder.getLiability();
                final double limitOnCloseOrderLiability = limitOnCloseOrderLiabilityObject == null ? 0d : limitOnCloseOrderLiabilityObject;
                amount += limitOnCloseOrderLiability;
            }
            if (marketOnCloseOrder != null) {
                final Double marketOnCloseOrderLiabilityObject = marketOnCloseOrder.getLiability();
                final double marketOnCloseOrderLiability = marketOnCloseOrderLiabilityObject == null ? 0d : marketOnCloseOrderLiabilityObject;
                amount += marketOnCloseOrderLiability;
            }
        }

        return amount;
    }

    public synchronized OrderType getOrderType() {
        return orderType;
    }

    public synchronized void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public synchronized Long getSelectionId() {
        return selectionId;
    }

    public synchronized void setSelectionId(Long selectionId) {
        this.selectionId = selectionId;
    }

    public synchronized Double getHandicap() {
        return handicap;
    }

    public synchronized void setHandicap(Double handicap) {
        this.handicap = handicap;
    }

    public synchronized Side getSide() {
        return side;
    }

    public synchronized void setSide(Side side) {
        this.side = side;
    }

    public synchronized LimitOrder getLimitOrder() {
        return limitOrder;
    }

    public synchronized void setLimitOrder(LimitOrder limitOrder) {
        this.limitOrder = limitOrder;
    }

    public synchronized LimitOnCloseOrder getLimitOnCloseOrder() {
        return limitOnCloseOrder;
    }

    public synchronized void setLimitOnCloseOrder(LimitOnCloseOrder limitOnCloseOrder) {
        this.limitOnCloseOrder = limitOnCloseOrder;
    }

    public synchronized MarketOnCloseOrder getMarketOnCloseOrder() {
        return marketOnCloseOrder;
    }

    public synchronized void setMarketOnCloseOrder(MarketOnCloseOrder marketOnCloseOrder) {
        this.marketOnCloseOrder = marketOnCloseOrder;
    }
}
