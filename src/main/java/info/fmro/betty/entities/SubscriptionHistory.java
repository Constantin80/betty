package info.fmro.betty.entities;

import info.fmro.betty.enums.SubscriptionStatus;

import java.util.Date;

public class SubscriptionHistory {
    private String subscriptionToken; // Application key identifier
    private Date expiryDateTime; // Subscription Expiry date
    private Date expiredDateTime; // Subscription Expired date
    private Date createdDateTime; // Subscription Create date
    private Date activationDateTime; // Subscription Activation date
    private Date cancellationDateTime; // Subscription Cancelled date
    private SubscriptionStatus subscriptionStatus; // Subscription status
    private String clientReference; // Client reference

    public SubscriptionHistory() {
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

    public synchronized Date getCreatedDateTime() {
        return createdDateTime == null ? null : (Date) createdDateTime.clone();
    }

    public synchronized void setCreatedDateTime(final Date createdDateTime) {
        this.createdDateTime = createdDateTime == null ? null : (Date) createdDateTime.clone();
    }

    public synchronized Date getActivationDateTime() {
        return activationDateTime == null ? null : (Date) activationDateTime.clone();
    }

    public synchronized void setActivationDateTime(final Date activationDateTime) {
        this.activationDateTime = activationDateTime == null ? null : (Date) activationDateTime.clone();
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

    public synchronized String getClientReference() {
        return clientReference;
    }

    public synchronized void setClientReference(final String clientReference) {
        this.clientReference = clientReference;
    }
}
