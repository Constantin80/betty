package info.fmro.betty.entities;

import info.fmro.betty.enums.SubscriptionStatus;

import java.util.Date;

public class SubscriptionTokenInfo {
    private String subscriptionToken; // Application key identifier
    private Date activatedDateTime; // Subscription Activated date
    private Date expiryDateTime; // Subscription Expiry date
    private Date expiredDateTime; // Subscription Expired date
    private Date cancellationDateTime; // Subscription Cancelled date
    private SubscriptionStatus subscriptionStatus; // Subscription status

    public SubscriptionTokenInfo() {
    }

    public synchronized String getSubscriptionToken() {
        return subscriptionToken;
    }

    public synchronized void setSubscriptionToken(final String subscriptionToken) {
        this.subscriptionToken = subscriptionToken;
    }

    public synchronized Date getExpiryDateTime() {
        return expiryDateTime == null ? null : (Date) expiryDateTime.clone();
    }

    public synchronized void setExpiryDateTime(final Date expiryDateTime) {
        this.expiryDateTime = expiryDateTime == null ? null : (Date) expiryDateTime.clone();
    }

    public synchronized Date getExpiredDateTime() {
        return expiredDateTime == null ? null : (Date) expiredDateTime.clone();
    }

    public synchronized void setExpiredDateTime(final Date expiredDateTime) {
        this.expiredDateTime = expiredDateTime == null ? null : (Date) expiredDateTime.clone();
    }

    public synchronized Date getActivatedDateTime() {
        return activatedDateTime == null ? null : (Date) activatedDateTime.clone();
    }

    public synchronized void setActivatedDateTime(final Date activatedDateTime) {
        this.activatedDateTime = activatedDateTime == null ? null : (Date) activatedDateTime.clone();
    }

    public synchronized Date getCancellationDateTime() {
        return cancellationDateTime == null ? null : (Date) cancellationDateTime.clone();
    }

    public synchronized void setCancellationDateTime(final Date cancellationDateTime) {
        this.cancellationDateTime = cancellationDateTime == null ? null : (Date) cancellationDateTime.clone();
    }

    public synchronized SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public synchronized void setSubscriptionStatus(final SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }
}
