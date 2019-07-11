package info.fmro.betty.stream.definitions;

import info.fmro.betty.stream.enums.Side;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// objects of this class are read from the stream
public class OrderRunnerChange
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(OrderRunnerChange.class);
    private static final long serialVersionUID = -7515170337276827530L;
    private Boolean fullImage;
    private Double hc; // Handicap - the handicap of the runner (selection) (null if not applicable)
    private Long id; // Selection Id - the id of the runner (selection)
    @Nullable
    private List<List<Double>> mb; // Matched Backs - matched amounts by distinct matched price on the Back side for this runner (selection)
    @Nullable
    private List<List<Double>> ml; // Matched Lays - matched amounts by distinct matched price on the Lay side for this runner (selection)
    @Nullable
    private Map<String, StrategyMatchChange> smc; // Strategy Matches - Matched Backs and Matched Lays grouped by strategy reference (customerStrategyRef)
    @Nullable
    private List<Order> uo; // Unmatched Orders - orders on this runner (selection) that are not fully matched

    @SuppressWarnings("OverlyNestedMethod")
    public synchronized double getMatchedSize(final Side side, final double price) {
        double matchedSize = 0d;
        @Nullable final List<List<Double>> chosenList;
        if (side == Side.B) {
            chosenList = this.mb;
        } else if (side == Side.L) {
            chosenList = this.ml;
        } else {
            logger.error("unknown Side in getMatchedSize for: {} {}", side, price); // matchedSize remains 0d
            chosenList = null;
        }

        if (chosenList != null) {
            for (final List<Double> priceSizeList : chosenList) {
                if (priceSizeList != null) {
                    final int listSize = priceSizeList.size();
                    if (listSize == 2) {
                        final Double foundPrice = priceSizeList.get(0);
                        //noinspection FloatingPointEquality
                        if (foundPrice != null && foundPrice == price) {
                            final Double foundSize = priceSizeList.get(0);
                            if (foundSize != null) {
                                matchedSize = foundSize;
                                break;
                            } else {
                                logger.error("foundSize null in getMatchedSize for: {} {} {}", foundPrice, Generic.objectToString(priceSizeList), Generic.objectToString(this)); // matchedSize remains 0d
                            }
                        } else { // matchedSize remains 0d
                            if (foundPrice == null) {
                                logger.error("foundPrice null in getMatchedSize for: {} {}", Generic.objectToString(priceSizeList), Generic.objectToString(this)); // matchedSize remains 0d
                            } else { // no error, but proper price not found, nothing to be done
                            }
                        }
                    } else {
                        logger.error("wrong size {} for priceSizeList getMatchedSize for: {} {}", listSize, Generic.objectToString(priceSizeList), Generic.objectToString(this)); // matchedSize remains 0d
                    }
                } else {
                    logger.error("null priceSizeList in getMatchedSize for: {} {} {}", side, price, Generic.objectToString(this)); // matchedSize remains 0d
                }
            } // end for
        } else { // matchedSize remains 0d, nothing to be done
        }

        return matchedSize;
    }

    public synchronized Order getUnmatchedOrder(final String betId) {
        Order returnValue = null;
        if (betId != null) {
            if (this.uo != null) {
                for (final Order order : this.uo) {
                    final String orderBetId = order.getId();
                    if (betId.equals(orderBetId)) {
                        returnValue = order;
                        break;
                    } else { // not returnValue yet, returnValue stays null, nothing to be done
                    }
                } // end for
            } else { // returnValue stays null, nothing to be done
            }
        } else {
            logger.error("null betId in getUnmatchedOrder"); // returnValue stays null
        }

        return returnValue;
    }

    public synchronized Boolean getFullImage() {
        return this.fullImage;
    }

    public synchronized void setFullImage(final Boolean fullImage) {
        this.fullImage = fullImage;
    }

    public synchronized Double getHc() {
        return this.hc;
    }

    public synchronized void setHc(final Double hc) {
        this.hc = hc;
    }

    public synchronized Long getId() {
        return this.id;
    }

    public synchronized void setId(final Long id) {
        this.id = id;
    }

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

    @Nullable
    public synchronized Map<String, StrategyMatchChange> getSmc() {
        return this.smc == null ? null : new HashMap<>(this.smc);
    }

    public synchronized void setSmc(final Map<String, ? extends StrategyMatchChange> smc) {
        this.smc = smc == null ? null : new HashMap<>(smc);
    }

    @Nullable
    public synchronized List<Order> getUo() {
        return this.uo == null ? null : new ArrayList<>(this.uo);
    }

    public synchronized void setUo(final List<? extends Order> uo) {
        this.uo = uo == null ? null : new ArrayList<>(uo);
    }
}
