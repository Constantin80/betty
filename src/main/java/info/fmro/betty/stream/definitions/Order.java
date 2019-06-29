package info.fmro.betty.stream.definitions;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

// objects of this class are read from the stream
public class Order
        implements Serializable { // amounts are in account currency (EUR)
    private static final long serialVersionUID = 3021807768896649660L;
    private static final Logger logger = LoggerFactory.getLogger(Order.class);
    private Double avp; // Average Price Matched - the average price the order was matched at (null if the order is not matched).
    //                     This value is not meaningful for activity on Line markets and is not guaranteed to be returned or maintained for these markets.
    private Double bsp; // BSP Liability - the BSP liability of the order (null if the order is not a BSP order)
    private String id; // Bet Id - the id of the order
    private Date ld; // Lapsed Date - the date the order was lapsed (null if the order is not lapsed)
    private Date md; //  Matched Date - the date the order was matched (null if the order is not matched)
    private OrderType ot; // Order Type - the type of the order (L = LIMIT, MOC = MARKET_ON_CLOSE, LOC = LIMIT_ON_CLOSE)
    private Double p; // Price - the original placed price of the order. Line markets operate at even-money odds of 2.0. However, price for these markets refers to the line positions available as defined by the markets min-max range and interval steps
    private Date pd; // Placed Date - the date the order was placed
    private PersistenceType pt; // Persistence Type - whether the order will persist at in play or not (L = LAPSE, P = PERSIST, MOC = Market On Close)
    private String rac; // Regulator Auth Code - the auth code returned by the regulator
    private String rc; // Regulator Code - the regulator of the order
    private String rfo; // Order Reference - the customer's order reference for this order (empty string if one was not set)
    private String rfs; // Strategy Reference - the customer's strategy reference for this order (empty string if one was not set)
    private Double s; // Size - the original placed size of the order
    private Double sc; // Size Cancelled - the amount of the order that has been cancelled
    private Side side; // Side - the side of the order. For Line markets a 'B' bet refers to a SELL line and an 'L' bet refers to a BUY line.
    private Double sl; // Size Lapsed - the amount of the order that has been lapsed
    private Double sm; // Size Matched - the amount of the order that has been matched
    private Double sr; // Size Remaining - the amount of the order that is remaining unmatched
    private OrderStatus status; // Status - the status of the order (E = EXECUTABLE, EC = EXECUTION_COMPLETE)
    private Double sv; // Size Voided - the amount of the order that has been voided
    private double backExposure, layExposure, backProfit, layProfit;

    public Order() {
    }

    public synchronized void calculateExposureAndProfit() {
        if (p == null || p <= 1d || sr == null || status == null || side == null) {
            logger.error("null or bogus fields in Order calculateExposureAndProfit: {} {} {} {} {}", p, sr, status, side, Generic.objectToString(this));
            backExposure = 0d;
            layExposure = 0d;
            backProfit = 0d;
            layProfit = 0d;
        } else if (status == OrderStatus.EC) { // execution complete, nothing should be remaining
            backExposure = 0d;
            layExposure = 0d;
            backProfit = 0d;
            layProfit = 0d;
        } else {
            switch (side) {
                case B:
                    backExposure = sr;
                    backProfit = Formulas.layExposure(p, sr);
                    layExposure = 0d;
                    layProfit = 0d;
                    break;
                case L:
                    backExposure = 0d;
                    backProfit = 0d;
                    layExposure = Formulas.layExposure(p, sr);
                    layProfit = sr;
                    break;
                default:
                    logger.error("strange side in Order calculateExposureAndProfit: {} {}", side, Generic.objectToString(this));
                    backExposure = 0d;
                    layExposure = 0d;
                    backProfit = 0d;
                    layProfit = 0d;
                    break;
            }
        }
        if (backExposure != 0d && layExposure != 0d) { // this condition should always be false, only one type of exposure should exist
            logger.error("strange exposure in Order calculateExposureAndProfit: {} {} {}", backExposure, layExposure, Generic.objectToString(this));
        }
    }

    public synchronized boolean cancelOrder(final String marketId, final RunnerId runnerId) { // full cancel
        return cancelOrder(marketId, runnerId, null);
    }

    public synchronized boolean cancelOrder(final String marketId, final RunnerId runnerId, final Double sizeReduction) {
        if (this.p == null || this.side == null || this.id == null) {
            logger.error("null variables during cancelOrder for: {} {} {} {}", this.p, this.side, this.id, Generic.objectToString(this));
            if (this.p == null) { // avoids exception when converting to primitive
                this.p = 1.01d;
            } else { // proper price exists, nothing to be done
            }
        } else { // no error, nothing to be done, method will continue
        }

        final double sizeRemaining = this.sr == null ? 0d : this.sr;
        return Statics.ordersThread.addCancelOrder(marketId, runnerId, this.side, this.p, sizeRemaining, this.id, sizeReduction);
    }

    public synchronized double removeBackExposure(final String marketId, final RunnerId runnerId, final double excessExposure) {
        final double exposureReduction;
        if (this.p == null || this.side == null || this.id == null) {
            logger.error("null variables during removeBackExposure for: {} {} {} {}", this.p, this.side, this.id, Generic.objectToString(this));
            exposureReduction = 0d;
        } else if (Objects.equals(this.side, Side.B)) {
            final double sizeRemaining = this.sr == null ? 0d : this.sr;
            final Double sizeReduction;
            if (excessExposure >= sizeRemaining) {
                sizeReduction = null;
                if (Statics.ordersThread.addCancelOrder(marketId, runnerId, this.side, this.p, sizeRemaining, this.id, sizeReduction)) {
                    exposureReduction = sizeRemaining;
                } else {
                    exposureReduction = 0d;
                }
            } else {
                sizeReduction = excessExposure;
                if (Statics.ordersThread.addCancelOrder(marketId, runnerId, this.side, this.p, sizeRemaining, this.id, sizeReduction)) {
                    exposureReduction = excessExposure;
                } else {
                    exposureReduction = 0d;
                }
            }
        } else {
            logger.error("wrong side in removeBackExposure for: {} {} {} {}", marketId, runnerId, excessExposure, Generic.objectToString(this));
            exposureReduction = 0d;
        }
        return excessExposure - exposureReduction;
    }

    public synchronized double removeLayExposure(final String marketId, final RunnerId runnerId, final double excessExposure) {
        final double exposureReduction;
        if (this.p == null || this.side == null || this.id == null) {
            logger.error("null variables during removeLayExposure for: {} {} {} {}", this.p, this.side, this.id, Generic.objectToString(this));
            exposureReduction = 0d;
        } else if (Objects.equals(this.side, Side.L)) {
            final double sizeRemaining = this.sr == null ? 0d : this.sr;
            final Double sizeReduction;
            if (excessExposure >= Formulas.layExposure(this.p, sizeRemaining)) {
                sizeReduction = null;
                if (Statics.ordersThread.addCancelOrder(marketId, runnerId, this.side, this.p, sizeRemaining, this.id, sizeReduction)) {
                    exposureReduction = Formulas.layExposure(this.p, sizeRemaining);
                } else {
                    exposureReduction = 0d;
                }
            } else {
                sizeReduction = excessExposure / (this.p - 1d);
                if (Statics.ordersThread.addCancelOrder(marketId, runnerId, this.side, this.p, sizeRemaining, this.id, sizeReduction)) {
                    exposureReduction = excessExposure;
                } else {
                    exposureReduction = 0d;
                }
            }
        } else {
            logger.error("wrong side in removeLayExposure for: {} {} {} {}", marketId, runnerId, excessExposure, Generic.objectToString(this));
            exposureReduction = 0d;
        }
        return excessExposure - exposureReduction;
    }

    public synchronized double getBackExposure() {
        return backExposure;
    }

    public synchronized double getLayExposure() {
        return layExposure;
    }

    public synchronized double getBackProfit() {
        return backProfit;
    }

    public synchronized double getLayProfit() {
        return layProfit;
    }

    public synchronized Date getLd() {
        return ld == null ? null : (Date) ld.clone();
    }

    public synchronized void setLd(final Date ld) {
        this.ld = ld == null ? null : (Date) ld.clone();
    }

    public synchronized Date getMd() {
        return md == null ? null : (Date) md.clone();
    }

    public synchronized void setMd(final Date md) {
        this.md = md == null ? null : (Date) md.clone();
    }

    public synchronized String getRac() {
        return rac;
    }

    public synchronized void setRac(final String rac) {
        this.rac = rac;
    }

    public synchronized String getRc() {
        return rc;
    }

    public synchronized void setRc(final String rc) {
        this.rc = rc;
    }

    public synchronized String getRfo() {
        return rfo;
    }

    public synchronized void setRfo(final String rfo) {
        this.rfo = rfo;
    }

    public synchronized String getRfs() {
        return rfs;
    }

    public synchronized void setRfs(final String rfs) {
        this.rfs = rfs;
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(final String id) {
        this.id = id;
    }

    public synchronized OrderType getOt() {
        return ot;
    }

    public synchronized void setOt(final OrderType ot) {
        this.ot = ot;
    }

    public synchronized OrderStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(final OrderStatus status) {
        this.status = status;
    }

    public synchronized PersistenceType getPt() {
        return pt;
    }

    public synchronized void setPt(final PersistenceType pt) {
        this.pt = pt;
    }

    public synchronized Side getSide() {
        return side;
    }

    public synchronized void setSide(final Side side) {
        this.side = side;
    }

    public synchronized Double getP() {
        return p;
    }

    public synchronized void setP(final Double p) {
        this.p = p;
    }

    public synchronized Double getS() {
        return s;
    }

    public synchronized void setS(final Double s) {
        this.s = s;
    }

    public synchronized Double getBsp() {
        return bsp;
    }

    public synchronized void setBsp(final Double bsp) {
        this.bsp = bsp;
    }

    public synchronized Date getPd() {
        return pd == null ? null : (Date) pd.clone();
    }

    public synchronized void setPd(final Date pd) {
        this.pd = pd == null ? null : (Date) pd.clone();
    }

    public synchronized Double getAvp() {
        return avp;
    }

    public synchronized void setAvp(final Double avp) {
        this.avp = avp;
    }

    public synchronized Double getSm() {
        return sm;
    }

    public synchronized void setSm(final Double sm) {
        this.sm = sm;
    }

    public synchronized Double getSr() {
        return sr;
    }

    public synchronized void setSr(final Double sr) {
        this.sr = sr;
    }

    public synchronized Double getSl() {
        return sl;
    }

    public synchronized void setSl(final Double sl) {
        this.sl = sl;
    }

    public synchronized Double getSc() {
        return sc;
    }

    public synchronized void setSc(final Double sc) {
        this.sc = sc;
    }

    public synchronized Double getSv() {
        return sv;
    }

    public synchronized void setSv(final Double sv) {
        this.sv = sv;
    }
}
