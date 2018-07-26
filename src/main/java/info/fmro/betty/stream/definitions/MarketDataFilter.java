package info.fmro.betty.stream.definitions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MarketDataFilter {
    private Set<FilterFlag> fields; // A set of field filter flags
    private Integer ladderLevels; // For depth based ladders the number of levels to send (1 to 10)

    public MarketDataFilter() {
    }

    public MarketDataFilter(FilterFlag... flags) {
        fields = new HashSet<>(Arrays.asList(flags));
    }

    public MarketDataFilter(Integer ladderLevels, FilterFlag... flags) {
        this.ladderLevels = ladderLevels;
        fields = new HashSet<>(Arrays.asList(flags));
    }

    public synchronized Set<FilterFlag> getFields() {
        return fields == null ? null : new HashSet<>(fields);
    }

    public synchronized void setFields(Set<FilterFlag> fields) {
        this.fields = fields == null ? null : new HashSet<>(fields);
    }

    public synchronized Integer getLadderLevels() {
        return ladderLevels;
    }

    public synchronized void setLadderLevels(Integer ladderLevels) {
        this.ladderLevels = ladderLevels;
    }
}
