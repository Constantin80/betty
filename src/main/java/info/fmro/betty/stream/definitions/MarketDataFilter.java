package info.fmro.betty.stream.definitions;

import info.fmro.betty.stream.enums.FilterFlag;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class MarketDataFilter
        implements Serializable {
    private static final long serialVersionUID = -1848801841041872639L;
    @Nullable
    private Set<FilterFlag> fields; // A set of field filter flags
    private Integer ladderLevels; // For depth based ladders the number of levels to send (1 to 10)

    @Contract(pure = true)
    public MarketDataFilter() {
    }

    public MarketDataFilter(@NotNull final FilterFlag... flags) {
        this.fields = EnumSet.copyOf(Arrays.asList(flags));
    }

    public MarketDataFilter(final Integer ladderLevels, @NotNull final FilterFlag... flags) {
        this.ladderLevels = ladderLevels;
        this.fields = EnumSet.copyOf(Arrays.asList(flags));
    }

    @Nullable
    public synchronized Set<FilterFlag> getFields() {
        return this.fields == null ? null : EnumSet.copyOf(this.fields);
    }

    public synchronized void setFields(@NotNull final Collection<FilterFlag> fields) {
        this.fields = EnumSet.copyOf(fields);
    }

    public synchronized Integer getLadderLevels() {
        return this.ladderLevels;
    }

    public synchronized void setLadderLevels(final Integer ladderLevels) {
        this.ladderLevels = ladderLevels;
    }
}
