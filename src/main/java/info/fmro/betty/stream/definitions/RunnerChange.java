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
@SuppressWarnings("OverlyComplexClass")
public class RunnerChange
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(RunnerChange.class);
    private static final long serialVersionUID = -3034311716396095048L;
    @Nullable
    private List<List<Double>> atb; // Available To Back - PriceVol tuple delta of price changes (0 vol is remove)
    @Nullable
    private List<List<Double>> atl; // Available To Lay - PriceVol tuple delta of price changes (0 vol is remove)
    @Nullable
    private List<List<Double>> batb; // Best Available To Back - LevelPriceVol triple delta of price changes, keyed by level (0 vol is remove)
    @Nullable
    private List<List<Double>> batl; // Best Available To Lay - LevelPriceVol triple delta of price changes, keyed by level (0 vol is remove)
    @Nullable
    private List<List<Double>> bdatb; // Best Display Available To Back (includes virtual prices)- LevelPriceVol triple delta of price changes, keyed by level (0 vol is remove)
    @Nullable
    private List<List<Double>> bdatl; // Best Display Available To Lay (includes virtual prices)- LevelPriceVol triple delta of price changes, keyed by level (0 vol is remove)
    private Double hc; // Handicap - the handicap of the runner (selection) (null if not applicable)
    private Long id; // Selection Id - the id of the runner (selection)
    private Double ltp; // Last Traded Price - The last traded price (or null if un-changed)
    @Nullable
    private List<List<Double>> spb; // Starting Price Back - PriceVol tuple delta of price changes (0 vol is remove)
    private Double spf; // Starting Price Far - The far starting price (or null if un-changed)
    @Nullable
    private List<List<Double>> spl; // Starting Price Lay - PriceVol tuple delta of price changes (0 vol is remove)
    private Double spn; // Starting Price Near - The far starting price (or null if un-changed)
    @Nullable
    private List<List<Double>> trd; // Traded - PriceVol tuple delta of price changes (0 vol is remove)
    private Double tv; // The total amount matched. This value is truncated at 2dp.

    public synchronized List<List<Double>> getAtb() {
        @Nullable final List<List<Double>> result;

        if (this.atb == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.atb.size());
            for (final List<Double> list : this.atb) {
                if (list == null) {
                    logger.error("null element found in atb during get for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setAtb(final Collection<? extends List<Double>> atb) {
        if (atb == null) {
            this.atb = null;
        } else {
            this.atb = new ArrayList<>(atb.size());
            for (final List<Double> list : atb) {
                if (list == null) {
                    logger.error("null element found in atb during set for: {}", Generic.objectToString(atb));
                    this.atb.add(null);
                } else {
                    this.atb.add(new ArrayList<>(list));
                }
            }
        }
    }

    public synchronized List<List<Double>> getAtl() {
        @Nullable final List<List<Double>> result;

        if (this.atl == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.atl.size());
            for (final List<Double> list : this.atl) {
                if (list == null) {
                    logger.error("null element found in atl during get for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setAtl(final Collection<? extends List<Double>> atl) {
        if (atl == null) {
            this.atl = null;
        } else {
            this.atl = new ArrayList<>(atl.size());
            for (final List<Double> list : atl) {
                if (list == null) {
                    logger.error("null element found in atl during set for: {}", Generic.objectToString(atl));
                    this.atl.add(null);
                } else {
                    this.atl.add(new ArrayList<>(list));
                }
            }
        }
    }

    public synchronized List<List<Double>> getBatb() {
        @Nullable final List<List<Double>> result;

        if (this.batb == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.batb.size());
            for (final List<Double> list : this.batb) {
                if (list == null) {
                    logger.error("null element found in batb during get for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setBatb(final Collection<? extends List<Double>> batb) {
        if (batb == null) {
            this.batb = null;
        } else {
            this.batb = new ArrayList<>(batb.size());
            for (final List<Double> list : batb) {
                if (list == null) {
                    logger.error("null element found in batb during set for: {}", Generic.objectToString(batb));
                    this.batb.add(null);
                } else {
                    this.batb.add(new ArrayList<>(list));
                }
            }
        }
    }

    public synchronized List<List<Double>> getBatl() {
        @Nullable final List<List<Double>> result;

        if (this.batl == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.batl.size());
            for (final List<Double> list : this.batl) {
                if (list == null) {
                    logger.error("null element found in batl during get for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setBatl(final Collection<? extends List<Double>> batl) {
        if (batl == null) {
            this.batl = null;
        } else {
            this.batl = new ArrayList<>(batl.size());
            for (final List<Double> list : batl) {
                if (list == null) {
                    logger.error("null element found in batl during set for: {}", Generic.objectToString(batl));
                    this.batl.add(null);
                } else {
                    this.batl.add(new ArrayList<>(list));
                }
            }
        }
    }

    public synchronized List<List<Double>> getBdatb() {
        @Nullable final List<List<Double>> result;

        if (this.bdatb == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.bdatb.size());
            for (final List<Double> list : this.bdatb) {
                if (list == null) {
                    logger.error("null element found in bdatb during get for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setBdatb(final Collection<? extends List<Double>> bdatb) {
        if (bdatb == null) {
            this.bdatb = null;
        } else {
            this.bdatb = new ArrayList<>(bdatb.size());
            for (final List<Double> list : bdatb) {
                if (list == null) {
                    logger.error("null element found in bdatb during set for: {}", Generic.objectToString(bdatb));
                    this.bdatb.add(null);
                } else {
                    this.bdatb.add(new ArrayList<>(list));
                }
            }
        }
    }

    public synchronized List<List<Double>> getBdatl() {
        @Nullable final List<List<Double>> result;

        if (this.bdatl == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.bdatl.size());
            for (final List<Double> list : this.bdatl) {
                if (list == null) {
                    logger.error("null element found in bdatl during get for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setBdatl(final Collection<? extends List<Double>> bdatl) {
        if (bdatl == null) {
            this.bdatl = null;
        } else {
            this.bdatl = new ArrayList<>(bdatl.size());
            for (final List<Double> list : bdatl) {
                if (list == null) {
                    logger.error("null element found in bdatl during set for: {}", Generic.objectToString(bdatl));
                    this.bdatl.add(null);
                } else {
                    this.bdatl.add(new ArrayList<>(list));
                }
            }
        }
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

    public synchronized Double getLtp() {
        return this.ltp;
    }

    public synchronized void setLtp(final Double ltp) {
        this.ltp = ltp;
    }

    public synchronized List<List<Double>> getSpb() {
        @Nullable final List<List<Double>> result;

        if (this.spb == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.spb.size());
            for (final List<Double> list : this.spb) {
                if (list == null) {
                    logger.error("null element found in spb during get for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setSpb(final Collection<? extends List<Double>> spb) {
        if (spb == null) {
            this.spb = null;
        } else {
            this.spb = new ArrayList<>(spb.size());
            for (final List<Double> list : spb) {
                if (list == null) {
                    logger.error("null element found in spb during set for: {}", Generic.objectToString(spb));
                    this.spb.add(null);
                } else {
                    this.spb.add(new ArrayList<>(list));
                }
            }
        }
    }

    public synchronized Double getSpf() {
        return this.spf;
    }

    public synchronized void setSpf(final Double spf) {
        this.spf = spf;
    }

    public synchronized List<List<Double>> getSpl() {
        @Nullable final List<List<Double>> result;

        if (this.spl == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.spl.size());
            for (final List<Double> list : this.spl) {
                if (list == null) {
                    logger.error("null element found in spl during get for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setSpl(final Collection<? extends List<Double>> spl) {
        if (spl == null) {
            this.spl = null;
        } else {
            this.spl = new ArrayList<>(spl.size());
            for (final List<Double> list : spl) {
                if (list == null) {
                    logger.error("null element found in spl during set for: {}", Generic.objectToString(spl));
                    this.spl.add(null);
                } else {
                    this.spl.add(new ArrayList<>(list));
                }
            }
        }
    }

    public synchronized Double getSpn() {
        return this.spn;
    }

    public synchronized void setSpn(final Double spn) {
        this.spn = spn;
    }

    public synchronized List<List<Double>> getTrd() {
        @Nullable final List<List<Double>> result;

        if (this.trd == null) {
            result = null;
        } else {
            result = new ArrayList<>(this.trd.size());
            for (final List<Double> list : this.trd) {
                if (list == null) {
                    logger.error("null element found in trd during get for: {}", Generic.objectToString(this));
                    result.add(null);
                } else {
                    result.add(new ArrayList<>(list));
                }
            }
        }

        return result;
    }

    public synchronized void setTrd(final Collection<? extends List<Double>> trd) {
        if (trd == null) {
            this.trd = null;
        } else {
            this.trd = new ArrayList<>(trd.size());
            for (final List<Double> list : trd) {
                if (list == null) {
                    logger.error("null element found in trd during set for: {}", Generic.objectToString(trd));
                    this.trd.add(null);
                } else {
                    this.trd.add(new ArrayList<>(list));
                }
            }
        }
    }

    public synchronized Double getTv() {
        return this.tv;
    }

    public synchronized void setTv(final Double tv) {
        this.tv = tv;
    }
}
