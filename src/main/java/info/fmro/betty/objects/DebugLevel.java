package info.fmro.betty.objects;

import gnu.trove.TIntCollection;
import gnu.trove.set.hash.TIntHashSet;
import info.fmro.shared.utility.Generic;
import java.io.Serializable;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugLevel
        implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(DebugLevel.class);
    private static final long serialVersionUID = -2888763923758409582L;
    private int level;
    private TIntHashSet codesSet = new TIntHashSet(0);

    public synchronized int getLevel() {
        return level;
    }

    public synchronized void setLevel(int level) {
        this.level = level;
    }

    public synchronized TIntHashSet getCodesSet() {
        return new TIntHashSet(codesSet);
    }

    public synchronized void setCodesSet(Collection<? extends Integer> collection) {
        this.codesSet = new TIntHashSet(collection);
    }

    public synchronized void setCodesSet(TIntCollection collection) {
        this.codesSet = new TIntHashSet(collection);
    }

    public synchronized boolean add(int code) {
        return codesSet.add(code);
    }

    public synchronized boolean remove(int code) {
        return codesSet.remove(code);
    }

    public synchronized boolean contains(int code) {
        return codesSet.contains(code);
    }

    public synchronized void clear() {
        codesSet.clear();
    }

    public synchronized boolean check(int level, int code) {
        return this.level >= level || this.codesSet.contains(code);
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized void copyFrom(DebugLevel debugLevel) {
        if (!this.codesSet.isEmpty()) {
            logger.error("not empty set in DebugLevel copyFrom: {}", Generic.objectToString(this));
        }
//        this.level = debugLevel.level;
        this.setLevel(debugLevel.level);
//        this.codesSet.clear();
//        this.codesSet.addAll(debugLevel.codesSet);
        this.setCodesSet(debugLevel.codesSet);
    }
//    @Override
//    public synchronized String toString() {
//        return "level: " + level + " codesSet: " + Arrays.toString(codesSet.toArray());
//    }
}
