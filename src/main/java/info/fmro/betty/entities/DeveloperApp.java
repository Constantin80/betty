package info.fmro.betty.entities;

import java.util.ArrayList;
import java.util.List;

public class DeveloperApp {

    private String appName; // The unique name of the application
    private Long appId; // A unique id of this application
    private List<DeveloperAppVersion> appVersions; // The application versions (including application keys)

    public DeveloperApp() {
    }

    public synchronized String getAppName() {
        return appName;
    }

    public synchronized void setAppName(String appName) {
        this.appName = appName;
    }

    public synchronized Long getAppId() {
        return appId;
    }

    public synchronized void setAppId(Long appId) {
        this.appId = appId;
    }

    public synchronized List<DeveloperAppVersion> getAppVersions() {
        return appVersions == null ? null : new ArrayList<>(appVersions);
    }

    public synchronized void setAppVersions(List<DeveloperAppVersion> appVersions) {
        this.appVersions = appVersions == null ? null : new ArrayList<>(appVersions);
    }
}
