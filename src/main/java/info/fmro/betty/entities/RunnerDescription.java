package info.fmro.betty.entities;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class RunnerDescription {
    private String runnerName;
    @Nullable
    private Map<String, String> metadata;

    public synchronized String getRunnerName() {
        return this.runnerName;
    }

    public synchronized void setRunnerName(final String runnerName) {
        this.runnerName = runnerName;
    }

    @Nullable
    public synchronized Map<String, String> getMetadata() {
        return this.metadata == null ? null : new HashMap<>(this.metadata);
    }

    public synchronized void setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata == null ? null : new HashMap<>(metadata);
    }
}
