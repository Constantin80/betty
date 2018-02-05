package info.fmro.betty.entities;

import java.util.HashMap;
import java.util.Map;

public class RunnerDescription {

    private String runnerName;
    private Map<String, String> metadata;

    public RunnerDescription() {
    }

    public synchronized String getRunnerName() {
        return runnerName;
    }

    public synchronized void setRunnerName(String runnerName) {
        this.runnerName = runnerName;
    }

    public synchronized Map<String, String> getMetadata() {
        return metadata == null ? null : new HashMap<>(metadata);
    }

    public synchronized void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata == null ? null : new HashMap<>(metadata);
    }
}
