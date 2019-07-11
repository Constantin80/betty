package info.fmro.betty.entities;

class DeveloperAppVersion {
    private String owner; // The sportex user who owns the specific version of the application
    private Long versionId; // The unique Id of the application version
    private String version; // The version identifier string such as 1.0, 2.0. Unique for a given application.
    private String applicationKey; // The unqiue application key associated with this application version
    private Boolean delayData; // Indicates whether the data exposed by platform services as seen by this application key is delayed or realtime.
    private Boolean subscriptionRequired; // Indicates whether the application version needs explicit subscription
    private Boolean ownerManaged; // Indicates whether the application version needs explicit management by the software owner. A value of false indicates, this is a version meant for personal developer use.
    private Boolean active; // Indicates whether the application version is currently active
    private String vendorId; // Public unique string provided to the Vendor that they can use to pass to the Betfair API in order to identify themselves.
    private String vendorSecret; // Private unique string provided to the Vendor that they pass with certain calls to confirm their identity. Linked to a particular App Key.

    public synchronized String getOwner() {
        return this.owner;
    }

    public synchronized void setOwner(final String owner) {
        this.owner = owner;
    }

    public synchronized Long getVersionId() {
        return this.versionId;
    }

    public synchronized void setVersionId(final Long versionId) {
        this.versionId = versionId;
    }

    public synchronized String getVersion() {
        return this.version;
    }

    public synchronized void setVersion(final String version) {
        this.version = version;
    }

    public synchronized String getApplicationKey() {
        return this.applicationKey;
    }

    public synchronized void setApplicationKey(final String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public synchronized Boolean getDelayData() {
        return this.delayData;
    }

    public synchronized void setDelayData(final Boolean delayData) {
        this.delayData = delayData;
    }

    public synchronized Boolean getSubscriptionRequired() {
        return this.subscriptionRequired;
    }

    public synchronized void setSubscriptionRequired(final Boolean subscriptionRequired) {
        this.subscriptionRequired = subscriptionRequired;
    }

    public synchronized Boolean getOwnerManaged() {
        return this.ownerManaged;
    }

    public synchronized void setOwnerManaged(final Boolean ownerManaged) {
        this.ownerManaged = ownerManaged;
    }

    public synchronized Boolean getActive() {
        return this.active;
    }

    public synchronized void setActive(final Boolean active) {
        this.active = active;
    }

    public synchronized String getVendorId() {
        return this.vendorId;
    }

    public synchronized void setVendorId(final String vendorId) {
        this.vendorId = vendorId;
    }

    public synchronized String getVendorSecret() {
        return this.vendorSecret;
    }

    public synchronized void setVendorSecret(final String vendorSecret) {
        this.vendorSecret = vendorSecret;
    }
}
