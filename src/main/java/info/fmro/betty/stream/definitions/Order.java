package info.fmro.betty.stream.definitions;

import java.io.Serializable;
import java.util.Date;

public class Order
        implements Serializable {
    private static final long serialVersionUID = 3021807768896649660L;
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

    public Order() {
    }

    public synchronized Date getLd() {
        return ld == null ? null : (Date) ld.clone();
    }

    public synchronized void setLd(Date ld) {
        this.ld = ld == null ? null : (Date) ld.clone();
    }

    public synchronized Date getMd() {
        return md == null ? null : (Date) md.clone();
    }

    public synchronized void setMd(Date md) {
        this.md = md == null ? null : (Date) md.clone();
    }

    public synchronized String getRac() {
        return rac;
    }

    public synchronized void setRac(String rac) {
        this.rac = rac;
    }

    public synchronized String getRc() {
        return rc;
    }

    public synchronized void setRc(String rc) {
        this.rc = rc;
    }

    public synchronized String getRfo() {
        return rfo;
    }

    public synchronized void setRfo(String rfo) {
        this.rfo = rfo;
    }

    public synchronized String getRfs() {
        return rfs;
    }

    public synchronized void setRfs(String rfs) {
        this.rfs = rfs;
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(String id) {
        this.id = id;
    }

    public synchronized OrderType getOt() {
        return ot;
    }

    public synchronized void setOt(OrderType ot) {
        this.ot = ot;
    }

    public synchronized OrderStatus getStatus() {
        return status;
    }

    public synchronized void setStatus(OrderStatus status) {
        this.status = status;
    }

    public synchronized PersistenceType getPt() {
        return pt;
    }

    public synchronized void setPt(PersistenceType pt) {
        this.pt = pt;
    }

    public synchronized Side getSide() {
        return side;
    }

    public synchronized void setSide(Side side) {
        this.side = side;
    }

    public synchronized Double getP() {
        return p;
    }

    public synchronized void setP(Double p) {
        this.p = p;
    }

    public synchronized Double getS() {
        return s;
    }

    public synchronized void setS(Double s) {
        this.s = s;
    }

    public synchronized Double getBsp() {
        return bsp;
    }

    public synchronized void setBsp(Double bsp) {
        this.bsp = bsp;
    }

    public synchronized Date getPd() {
        return pd == null ? null : (Date) pd.clone();
    }

    public synchronized void setPd(Date pd) {
        this.pd = pd == null ? null : (Date) pd.clone();
    }

    public synchronized Double getAvp() {
        return avp;
    }

    public synchronized void setAvp(Double avp) {
        this.avp = avp;
    }

    public synchronized Double getSm() {
        return sm;
    }

    public synchronized void setSm(Double sm) {
        this.sm = sm;
    }

    public synchronized Double getSr() {
        return sr;
    }

    public synchronized void setSr(Double sr) {
        this.sr = sr;
    }

    public synchronized Double getSl() {
        return sl;
    }

    public synchronized void setSl(Double sl) {
        this.sl = sl;
    }

    public synchronized Double getSc() {
        return sc;
    }

    public synchronized void setSc(Double sc) {
        this.sc = sc;
    }

    public synchronized Double getSv() {
        return sv;
    }

    public synchronized void setSv(Double sv) {
        this.sv = sv;
    }
}
