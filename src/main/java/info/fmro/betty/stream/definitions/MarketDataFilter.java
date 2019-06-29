package info.fmro.betty.stream.definitions;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MarketDataFilter
        implements Serializable {
    private static final long serialVersionUID = -1848801841041872639L;
    private Set<FilterFlag> fields; // A set of field filter flags
    private Integer ladderLevels; // For depth based ladders the number of levels to send (1 to 10)

    public MarketDataFilter() {
    }

    public MarketDataFilter(final FilterFlag... flags) {
        fields = new HashSet<>(Arrays.asList(flags));
    }

    public MarketDataFilter(final Integer ladderLevels, final FilterFlag... flags) {
        this.ladderLevels = ladderLevels;
        fields = new HashSet<>(Arrays.asList(flags));
    }

    public synchronized Set<FilterFlag> getFields() {
        return fields == null ? null : new HashSet<>(fields);
    }

    public synchronized void setFields(final Set<FilterFlag> fields) {
        this.fields = fields == null ? null : new HashSet<>(fields);
    }

    public synchronized Integer getLadderLevels() {
        return ladderLevels;
    }

    public synchronized void setLadderLevels(final Integer ladderLevels) {
        this.ladderLevels = ladderLevels;
    }
}
