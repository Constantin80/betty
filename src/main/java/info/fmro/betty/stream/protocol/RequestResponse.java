package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.RequestMessage;
import info.fmro.betty.stream.definitions.StatusMessage;
import info.fmro.betty.stream.enums.RequestOperationType;
import info.fmro.betty.stream.enums.StatusCode;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class RequestResponse {
    private static final Logger logger = LoggerFactory.getLogger(RequestResponse.class);
    public static final long defaultExpirationPeriod = 30_000L;
    private final RequestMessage request;
    private final Consumer<RequestResponse> onSuccess;
    //    private final int id;
    @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized") // I couldn't find where it is accessed in unsynchronized context; it might be an IntelliJ inspection bug
    private StatusMessage statusMessage;
    private final long creationTime;

    public RequestResponse(final RequestMessage request, final Consumer<RequestResponse> onSuccess) {
        this.creationTime = System.currentTimeMillis();
//        this.id = id;
        this.request = request;
        this.onSuccess = onSuccess;
    }

    public synchronized void processStatusMessage(final StatusMessage newStatusMessage) {
        if (newStatusMessage == null) {
            logger.error("null statusMessage for task: {}", Generic.objectToString(this));
        } else {
            if (newStatusMessage.getStatusCode() == StatusCode.SUCCESS) {
                if (this.onSuccess != null) {
                    this.onSuccess.accept(this);
                } else { // onSuccess can be null, which means nothing should be done
                }
            }
            this.statusMessage = newStatusMessage;
            //        future.setResponse(statusMessage);
        }
    }

    @Contract(pure = true)
    private synchronized Consumer<RequestResponse> getOnSuccess() {
        return this.onSuccess;
    }

    public synchronized StatusMessage getStatusMessage() {
        return this.statusMessage;
    }

    public synchronized boolean isTaskSuccessful() {
//        final boolean result;
//
//        if (statusMessage == null) {
//            result = false;
//        } else {
//            result = statusMessage.getStatusCode() == StatusCode.SUCCESS;
//        }

        return isTaskFinished() && this.statusMessage.getStatusCode() == StatusCode.SUCCESS;
    }

    public synchronized boolean isTaskFinished() {
        return this.statusMessage != null;
    }

    public synchronized long getCreationTime() {
        return this.creationTime;
    }

    public synchronized boolean isExpired() {
        return isExpired(defaultExpirationPeriod);
    }

    private synchronized boolean isExpired(final long expirationPeriod) {
        final boolean isExpired;
        final long currentTime = System.currentTimeMillis();
        final long timeSinceCreation = currentTime - this.creationTime;
        isExpired = timeSinceCreation >= expirationPeriod;

        return isExpired;
    }

    public synchronized RequestMessage getRequest() {
        return this.request;
    }

    public synchronized int getId() {
        return this.request.getId();
    }

    public synchronized RequestOperationType getRequestOperationType() {
        return this.request.getOp();
    }
}
