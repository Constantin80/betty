package info.fmro.betty.entities;

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

    public RunnerCatalog() {
    }

    public RunnerCatalog(Long selectionId, String runnerName, Double handicap, Integer sortPriority, Map<String, String> metadata) {
        this.selectionId = selectionId;
        this.runnerName = runnerName;
        this.handicap = handicap;
        this.sortPriority = sortPriority;
        this.metadata = metadata;
    }

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

//    public synchronized void setSelectionId(Long selectionId) {
//        this.selectionId = selectionId;
//    }
    public synchronized String getRunnerName() {
        return this.runnerName;
    }

//    public synchronized void setRunnerName(String runnerName) {
//        this.runnerName = runnerName;
//    }
    public synchronized Double getHandicap() {
        return this.handicap;
    }

//    public synchronized void setHandicap(Double handicap) {
//        this.handicap = handicap;
//    }
    public synchronized Integer getSortPriority() {
        return this.sortPriority;
    }

//    public synchronized void setSortPriority(Integer sortPriority) {
//        this.sortPriority = sortPriority;
//    }
    public synchronized Map<String, String> getMetadata() {
        return metadata == null ? null : new HashMap<>(metadata);
    }

//    public synchronized void setMetadata(Map<String, String> metadata) {
//        this.metadata = metadata == null ? null : new HashMap<>(metadata);
//    }
    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(Object obj) {
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
        if (!Objects.equals(this.runnerName, other.runnerName)) {
            return false;
        }
        if (!Objects.equals(this.handicap, other.handicap)) {
            return false;
        }
        if (!Objects.equals(this.sortPriority, other.sortPriority)) {
            return false;
        }
        return Objects.equals(this.metadata, other.metadata);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.selectionId);
        hash = 97 * hash + Objects.hashCode(this.runnerName);
        hash = 97 * hash + Objects.hashCode(this.handicap);
        hash = 97 * hash + Objects.hashCode(this.sortPriority);
        hash = 97 * hash + Objects.hashCode(this.metadata);
        return hash;
    }
}
