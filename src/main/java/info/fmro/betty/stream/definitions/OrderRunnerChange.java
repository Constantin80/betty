package info.fmro.betty.stream.definitions;

import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OrderRunnerChange {
    private static final Logger logger = LoggerFactory.getLogger(OrderRunnerChange.class);
    private Boolean fullImage;
    private Double hc; // Handicap - the handicap of the runner (selection) (null if not applicable)
    private Long id; // Selection Id - the id of the runner (selection)
    private List<List<Double>> mb; // Matched Backs - matched amounts by distinct matched price on the Back side for this runner (selection)
    private List<List<Double>> ml; // Matched Lays - matched amounts by distinct matched price on the Lay side for this runner (selection)
    private StrategyMatchChange smc; // Strategy Matches - Matched Backs and Matched Lays grouped by strategy reference
    private List<Order> uo; // Unmatched Orders - orders on this runner (selection) that are not fully matched

    public OrderRunnerChange() {
    }

    public synchronized Boolean getFullImage() {
        return fullImage;
    }

    public synchronized void setFullImage(Boolean fullImage) {
        this.fullImage = fullImage;
    }

    public synchronized Double getHc() {
        return hc;
    }

    public synchronized void setHc(Double hc) {
        this.hc = hc;
    }

    public synchronized Long getId() {
        return id;
    }

    public synchronized void setId(Long id) {
        this.id = id;
    }

    public synchronized List<List<Double>> getMb() {
        final List<List<Double>> result;

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

    public synchronized void setMb(List<List<Double>> mb) {
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
        final List<List<Double>> result;

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

    public synchronized void setMl(List<List<Double>> ml) {
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

    public synchronized StrategyMatchChange getSmc() {
        return smc;
    }

    public synchronized void setSmc(StrategyMatchChange smc) {
        this.smc = smc;
    }

    public synchronized List<Order> getUo() {
        return uo == null ? null : new ArrayList<>(uo);
    }

    public synchronized void setUo(List<Order> uo) {
        this.uo = uo == null ? null : new ArrayList<>(uo);
    }
}
