package info.fmro.betty.stream.cache.util;

import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Objects;

public class RunnerId
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(RunnerId.class);
    private static final long serialVersionUID = -4753325449542276275L;
    private final Long selectionId; // the id of the runner
    private final Double handicap; // the handicap of the runner (null if not applicable)

    public RunnerId(final Long selectionId, final Double handicap) {
        if (selectionId == null) {
            logger.error("null selectionId when creating RunnerId: {} {}", selectionId, handicap);
        } else { // no error message, constructor continues normally
        }
        this.selectionId = selectionId;
        this.handicap = handicap;
    }

    public synchronized Long getSelectionId() {
        return this.selectionId;
    }

    public synchronized Double getHandicap() {
        return this.handicap;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final RunnerId runnerId = (RunnerId) obj;
        return Objects.equals(this.selectionId, runnerId.selectionId) &&
               Objects.equals(this.handicap, runnerId.handicap);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(this.selectionId, this.handicap);
    }

    @Override
    public synchronized String toString() {
        return "RunnerId{" +
               "selectionId=" + this.selectionId +
               ", handicap=" + this.handicap +
               '}';
    }
}
