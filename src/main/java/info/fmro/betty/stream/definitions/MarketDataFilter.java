package info.fmro.betty.stream.definitions;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MarketDataFilter
        implements Serializable {
    private static final long serialVersionUID = -1848801841041872639L;
    @Nullable
    private Set<FilterFlag> fields; // A set of field filter flags
    private Integer ladderLevels; // For depth based ladders the number of levels to send (1 to 10)

    public MarketDataFilter() {
    }

    public MarketDataFilter(final FilterFlag... flags) {
        this.fields = new HashSet<>(Arrays.asList(flags));
    }

    public MarketDataFilter(final Integer ladderLevels, final FilterFlag... flags) {
        this.ladderLevels = ladderLevels;
        this.fields = new HashSet<>(Arrays.asList(flags));
    }

    @Nullable
    public synchronized Set<FilterFlag> getFields() {
        return this.fields == null ? null : new HashSet<>(this.fields);
    }

    public synchronized void setFields(final Set<FilterFlag> fields) {
        this.fields = fields == null ? null : new HashSet<>(fields);
    }

    public synchronized Integer getLadderLevels() {
        return this.ladderLevels;
    }

    public synchronized void setLadderLevels(final Integer ladderLevels) {
        this.ladderLevels = ladderLevels;
    }
}
