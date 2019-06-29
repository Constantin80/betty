package info.fmro.betty.stream.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// objects of this class are read from the stream
public class MarketChange
        implements Serializable {
    private static final long serialVersionUID = 14266379627847118L;
    private Boolean con; // Conflated - have more than a single change been combined (or null if not conflated)
    private String id; // Market Id - the id of the market
    private Boolean img; // Image - replace existing prices / data with the data supplied: it is not a delta (or null if delta)
    private MarketDefinition marketDefinition;
    private List<RunnerChange> rc; // Runner Changes - a list of changes to runners (or null if un-changed)
    private Double tv; // The total amount matched across the market. This value is truncated at 2dp (or null if un-changed)

    public MarketChange() {
    }

    public synchronized Boolean getCon() {
        return con;
    }

    public synchronized void setCon(final Boolean con) {
        this.con = con;
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(final String id) {
        this.id = id;
    }

    public synchronized Boolean getImg() {
        return img;
    }

    public synchronized void setImg(final Boolean img) {
        this.img = img;
    }

    public synchronized MarketDefinition getMarketDefinition() {
        return marketDefinition;
    }

    public synchronized void setMarketDefinition(final MarketDefinition marketDefinition) {
        this.marketDefinition = marketDefinition;
    }

    public synchronized List<RunnerChange> getRc() {
        return rc == null ? null : new ArrayList<>(rc);
    }

    public synchronized void setRc(final List<RunnerChange> rc) {
        this.rc = rc == null ? null : new ArrayList<>(rc);
    }

    public synchronized Double getTv() {
        return tv;
    }

    public synchronized void setTv(final Double tv) {
        this.tv = tv;
    }
}
