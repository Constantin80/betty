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
    private TIntHashSet codesSet = new TIntHashSet(0);

    public synchronized int getLevel() {
        return level;
    }

    public synchronized void setLevel(final int level) {
        this.level = level;
    }

    public synchronized boolean add(final int code) {
        return codesSet.add(code);
    }

    public synchronized boolean remove(final int code) {
        return codesSet.remove(code);
    }

    public synchronized boolean contains(final int code) {
        return codesSet.contains(code);
    }

    public synchronized void clear() {
        codesSet.clear();
    }

    public synchronized boolean check(final int level, final int code) {
        return this.level >= level || this.codesSet.contains(code);
    }

    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized void copyFrom(final DebugLevel debugLevel) {
        if (!this.codesSet.isEmpty()) {
            logger.error("not empty set in DebugLevel copyFrom: {}", Generic.objectToString(this));
        }

        if (debugLevel == null) {
            logger.error("null debugLevel in copyFrom for: {}", Generic.objectToString(this));
        } else {
            this.setLevel(debugLevel.level);

            this.codesSet.clear();
            if (debugLevel.codesSet != null) {
                this.codesSet.addAll(debugLevel.codesSet);
            } else {
                logger.error("null codesSet in DebugLevel copyFrom: {}", Generic.objectToString(debugLevel));
            }
        }
    }
}
