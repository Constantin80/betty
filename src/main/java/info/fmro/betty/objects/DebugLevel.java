package info.fmro.betty.objects;

import gnu.trove.set.hash.TIntHashSet;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class DebugLevel
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(DebugLevel.class);
    private static final long serialVersionUID = -2888763923758409582L;
    private int level;
    private final TIntHashSet codesSet = new TIntHashSet(0);

    public synchronized int getLevel() {
        return this.level;
    }

    public synchronized void setLevel(final int level) {
        this.level = level;
    }

    public synchronized boolean add(final int code) {
        return this.codesSet.add(code);
    }

    public synchronized boolean remove(final int code) {
        return this.codesSet.remove(code);
    }

    public synchronized boolean contains(final int code) {
        return this.codesSet.contains(code);
    }

    public synchronized void clear() {
        this.codesSet.clear();
    }

    public synchronized boolean check(final int levelToCheck, final int code) {
        return this.level >= levelToCheck || this.codesSet.contains(code);
    }

    public synchronized void copyFrom(final DebugLevel debugLevel) {
        if (!this.codesSet.isEmpty()) {
            logger.error("not empty set in DebugLevel copyFrom: {}", Generic.objectToString(this));
        }

        if (debugLevel == null) {
            logger.error("null debugLevel in copyFrom for: {}", Generic.objectToString(this));
        } else {
            this.setLevel(debugLevel.level);

            this.codesSet.clear();
            this.codesSet.addAll(debugLevel.codesSet);
        }
    }
}
