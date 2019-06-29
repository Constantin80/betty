package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.RequestMessage;
import info.fmro.betty.stream.definitions.RequestOperationType;
import info.fmro.betty.stream.definitions.StatusCode;
import info.fmro.betty.stream.definitions.StatusMessage;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Created by mulveyj on 07/07/2016.
 */
public class RequestResponse {
    //    private final FutureResponse<StatusMessage> future = new FutureResponse<>();
    private static final Logger logger = LoggerFactory.getLogger(RequestResponse.class);
    public static final long defaultExpirationPeriod = 30_000L;
    private final RequestMessage request;
    private final Consumer<RequestResponse> onSuccess;
    //    private final int id;
    private StatusMessage statusMessage;
    private final long creationTime;

    public RequestResponse(final RequestMessage request, final Consumer<RequestResponse> onSuccess) {
        creationTime = System.currentTimeMillis();
//        this.id = id;
        this.request = request;
        this.onSuccess = onSuccess;
    }

    public RequestResponse(final RequestResponse other) {
        creationTime = System.currentTimeMillis();
//        this.id = other.getId();
        this.request = other.getRequest();
        this.onSuccess = other.getOnSuccess();
    }

    public synchronized void processStatusMessage(final StatusMessage statusMessage) {
        if (statusMessage == null) {
            logger.error("null statusMessage for task: {}", Generic.objectToString(this));
        } else {
            if (statusMessage.getStatusCode() == StatusCode.SUCCESS) {
                if (onSuccess != null) {
                    onSuccess.accept(this);
                } else { // onSuccess can be null, which means nothing should be done
                }
            }
            this.statusMessage = statusMessage;
            //        future.setResponse(statusMessage);
        }
    }

    public synchronized Consumer<RequestResponse> getOnSuccess() {
        return onSuccess;
    }

    public synchronized StatusMessage getStatusMessage() {
        return statusMessage;
    }

    public synchronized boolean isTaskSuccessful() {
//        final boolean result;
//
//        if (statusMessage == null) {
//            result = false;
//        } else {
//            result = statusMessage.getStatusCode() == StatusCode.SUCCESS;
//        }

        return isTaskFinished() && statusMessage.getStatusCode() == StatusCode.SUCCESS;
    }

    public synchronized boolean isTaskFinished() {
        return statusMessage != null;
    }

    public synchronized long getCreationTime() {
        return creationTime;
    }

    public synchronized boolean isExpired() {
        return isExpired(defaultExpirationPeriod);
    }

    public synchronized boolean isExpired(final long expirationPeriod) {
        final boolean isExpired;
        final long currentTime = System.currentTimeMillis();
        final long timeSinceCreation = currentTime - this.creationTime;
        isExpired = timeSinceCreation >= expirationPeriod;

        return isExpired;
    }

//    public synchronized FutureResponse<StatusMessage> getFuture() {
//        return future;
//    }

    public synchronized RequestMessage getRequest() {
        return request;
    }

    public synchronized int getId() {
        return this.request.getId();
    }

    public synchronized RequestOperationType getRequestOperationType() {
        return this.request.getOp();
    }

//    public synchronized void setException(Exception e) {
//        future.setException(e);
//    }
}
