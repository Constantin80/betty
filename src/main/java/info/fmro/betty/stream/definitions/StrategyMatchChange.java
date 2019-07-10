package info.fmro.betty.stream.definitions;

import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// objects of this class are read from the stream
public class StrategyMatchChange
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(StrategyMatchChange.class);
    private static final long serialVersionUID = -3731593485034448217L;
    @Nullable
    private List<List<Double>> mb; // Matched Backs - matched amounts by distinct matched price on the Back side for this strategy
    @Nullable
    private List<List<Double>> ml; // Matched Lays - matched amounts by distinct matched price on the Lay side for this strategy

    public synchronized List<List<Double>> getMb() {
        @Nullable final List<List<Double>> result;

        if (this.mb == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.mb.size());
            for (final List<Double> list : this.mb) {
                if (list == null) {
                    logger.error("null element found in mb during getMb for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setMb(final Collection<? extends List<Double>> mb) {
        if (mb == null) {
            this.mb = null;
        } else {
            this.mb = new ArrayList<>(mb.size());
            for (final List<Double> list : mb) {
                if (list == null) {
                    logger.error("null element found in mb during setMb for: {}", Generic.objectToString(mb));
                    this.mb.add(null);
                } else {
                    this.mb.add(new ArrayList<>(list));
                }
            }
        }
    }

    public synchronized List<List<Double>> getMl() {
        @Nullable final List<List<Double>> result;

        if (this.ml == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.ml.size());
            for (final List<Double> list : this.ml) {
                if (list == null) {
                    logger.error("null element found in ml during getMl for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setMl(final Collection<? extends List<Double>> ml) {
        if (ml == null) {
            this.ml = null;
        } else {
            this.ml = new ArrayList<>(ml.size());
            for (final List<Double> list : ml) {
                if (list == null) {
                    logger.error("null element found in ml during setMl for: {}", Generic.objectToString(ml));
                    this.ml.add(null);
                } else {
                    this.ml.add(new ArrayList<>(list));
                }
            }
        }
    }
}
