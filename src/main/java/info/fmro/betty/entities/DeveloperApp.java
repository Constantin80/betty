package info.fmro.betty.entities;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class DeveloperApp {
    private String appName; // The unique name of the application
    private Long appId; // A unique id of this application
    @Nullable
    private List<DeveloperAppVersion> appVersions; // The application versions (including application keys)

    public synchronized String getAppName() {
        return this.appName;
    }

    public synchronized void setAppName(final String appName) {
        this.appName = appName;
    }

    public synchronized Long getAppId() {
        return this.appId;
    }

    public synchronized void setAppId(final Long appId) {
        this.appId = appId;
    }

    @Nullable
    public synchronized List<DeveloperAppVersion> getAppVersions() {
        return this.appVersions == null ? null : new ArrayList<>(this.appVersions);
    }

    public synchronized void setAppVersions(final List<? extends DeveloperAppVersion> appVersions) {
        this.appVersions = appVersions == null ? null : new ArrayList<>(appVersions);
    }
}
