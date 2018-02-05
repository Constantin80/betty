package info.fmro.betty.entities;

public class DeveloperAppVersion {

    private String owner; // The sportex user who owns the specific version of the application
    private Long versionId; // The unique Id of the application version
    private String version; // The version identifier string such as 1.0, 2.0. Unique for a given application.
    private String applicationKey; // The unqiue application key associated with this application version
    private Boolean delayData; // Indicates whether the data exposed by platform services as seen by this application key is delayed or realtime.
    private Boolean subscriptionRequired; // Indicates whether the application version needs explicit subscription
    private Boolean ownerManaged; // Indicates whether the application version needs explicit management by the software owner. A value of false indicates, this is a version meant 
    // for personal developer use.
    private Boolean active; // Indicates whether the application version is currently active

    public DeveloperAppVersion() {
    }

    public synchronized String getOwner() {
        return owner;
    }

    public synchronized void setOwner(String owner) {
        this.owner = owner;
    }

    public synchronized Long getVersionId() {
        return versionId;
    }

    public synchronized void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public synchronized String getVersion() {
        return version;
    }

    public synchronized void setVersion(String version) {
        this.version = version;
    }

    public synchronized String getApplicationKey() {
        return applicationKey;
    }

    public synchronized void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public synchronized Boolean isDelayData() {
        return delayData;
    }

    public synchronized void setDelayData(Boolean delayData) {
        this.delayData = delayData;
    }

    public synchronized Boolean isSubscriptionRequired() {
        return subscriptionRequired;
    }

    public synchronized void setSubscriptionRequired(Boolean subscriptionRequired) {
        this.subscriptionRequired = subscriptionRequired;
    }

    public synchronized Boolean isOwnerManaged() {
        return ownerManaged;
    }

    public synchronized void setOwnerManaged(Boolean ownerManaged) {
        this.ownerManaged = ownerManaged;
    }

    public synchronized Boolean isActive() {
        return active;
    }

    public synchronized void setActive(Boolean active) {
        this.active = active;
    }
}
