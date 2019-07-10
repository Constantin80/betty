package info.fmro.betty.entities;

import info.fmro.betty.enums.SubscriptionStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Date;

@SuppressWarnings("unused")
public class SubscriptionHistory {
    private String subscriptionToken; // Application key identifier
    @Nullable
    private Date expiryDateTime; // Subscription Expiry date
    @Nullable
    private Date expiredDateTime; // Subscription Expired date
    @Nullable
    private Date createdDateTime; // Subscription Create date
    @Nullable
    private Date activationDateTime; // Subscription Activation date
    @Nullable
    private Date cancellationDateTime; // Subscription Cancelled date
    private SubscriptionStatus subscriptionStatus; // Subscription status
    private String clientReference; // Client reference

    public synchronized String getSubscriptionToken() {
        return this.subscriptionToken;
    }

    public synchronized void setSubscriptionToken(final String subscriptionToken) {
        this.subscriptionToken = subscriptionToken;
    }

    @Nullable
    public synchronized Date getExpiryDateTime() {
        return this.expiryDateTime == null ? null : (Date) this.expiryDateTime.clone();
    }

    public synchronized void setExpiryDateTime(final Date expiryDateTime) {
        this.expiryDateTime = expiryDateTime == null ? null : (Date) expiryDateTime.clone();
    }

    @Nullable
    public synchronized Date getExpiredDateTime() {
        return this.expiredDateTime == null ? null : (Date) this.expiredDateTime.clone();
    }

    public synchronized void setExpiredDateTime(final Date expiredDateTime) {
        this.expiredDateTime = expiredDateTime == null ? null : (Date) expiredDateTime.clone();
    }

    @Nullable
    public synchronized Date getCreatedDateTime() {
        return this.createdDateTime == null ? null : (Date) this.createdDateTime.clone();
    }

    public synchronized void setCreatedDateTime(final Date createdDateTime) {
        this.createdDateTime = createdDateTime == null ? null : (Date) createdDateTime.clone();
    }

    @Nullable
    public synchronized Date getActivationDateTime() {
        return this.activationDateTime == null ? null : (Date) this.activationDateTime.clone();
    }

    public synchronized void setActivationDateTime(final Date activationDateTime) {
        this.activationDateTime = activationDateTime == null ? null : (Date) activationDateTime.clone();
    }

    @Nullable
    public synchronized Date getCancellationDateTime() {
        return this.cancellationDateTime == null ? null : (Date) this.cancellationDateTime.clone();
    }

    public synchronized void setCancellationDateTime(final Date cancellationDateTime) {
        this.cancellationDateTime = cancellationDateTime == null ? null : (Date) cancellationDateTime.clone();
    }

    public synchronized SubscriptionStatus getSubscriptionStatus() {
        return this.subscriptionStatus;
    }

    public synchronized void setSubscriptionStatus(final SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public synchronized String getClientReference() {
        return this.clientReference;
    }

    public synchronized void setClientReference(final String clientReference) {
        this.clientReference = clientReference;
    }
}
