package info.fmro.betty.entities;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RunnerCatalog
        implements Serializable {
    private static final long serialVersionUID = 8076707042221620993L;
    private Long selectionId;
    private String runnerName;
    private Double handicap;
    private Integer sortPriority;
    private Map<String, String> metadata;

    @Contract(pure = true)
    public RunnerCatalog() {
    }

    @Contract(pure = true)
    public RunnerCatalog(final Long selectionId, final String runnerName, final Double handicap, final Integer sortPriority, @NotNull final Map<String, String> metadata) {
        this.selectionId = selectionId;
        this.runnerName = runnerName;
        this.handicap = handicap;
        this.sortPriority = sortPriority;
        this.metadata = new HashMap<>(metadata);
    }

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

    public synchronized String getRunnerName() {
        return this.runnerName;
    }

    public synchronized Double getHandicap() {
        return this.handicap;
    }

    public synchronized Integer getSortPriority() {
        return this.sortPriority;
    }

    @Nullable
    public synchronized Map<String, String> getMetadata() {
        return this.metadata == null ? null : new HashMap<>(this.metadata);
    }

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RunnerCatalog other = (RunnerCatalog) obj;
        if (!Objects.equals(this.selectionId, other.selectionId)) {
            return false;
        }
        return Objects.equals(this.handicap, other.handicap);
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.selectionId);
        hash = 97 * hash + Objects.hashCode(this.handicap);
        return hash;
    }
}
