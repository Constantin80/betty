package info.fmro.betty.stream.protocol;

import info.fmro.betty.stream.definitions.RequestMessage;
import info.fmro.betty.stream.definitions.StatusCode;
import info.fmro.betty.stream.definitions.StatusMessage;

import java.util.function.Consumer;

/**
 * Created by mulveyj on 07/07/2016.
 */
public class RequestResponse {
    private final FutureResponse<StatusMessage> future = new FutureResponse<>();
    private final RequestMessage request;
    private final Consumer<RequestResponse> onSuccess;
    private final int id;

    public RequestResponse(int id, RequestMessage request, Consumer<RequestResponse> onSuccess) {
        this.id = id;
        this.request = request;
        this.onSuccess = onSuccess;
    }

    public synchronized void processStatusMessage(StatusMessage statusMessage) {
        if (statusMessage.getStatusCode() == StatusCode.SUCCESS) {
            if (onSuccess != null) {
                onSuccess.accept(this);
            }
        }
        future.setResponse(statusMessage);
    }

    public synchronized FutureResponse<StatusMessage> getFuture() {
        return future;
    }

    public synchronized RequestMessage getRequest() {
        return request;
    }

    public synchronized int getId() {
        return id;
    }

    public synchronized void setException(Exception e) {
        future.setException(e);
    }
}
