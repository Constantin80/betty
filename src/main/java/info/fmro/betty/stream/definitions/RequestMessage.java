package info.fmro.betty.stream.definitions;

public class RequestMessage {
    private Integer id; // Client generated unique id to link request with response (like json rpc)
    private RequestOperationType op; // The operation type

    public RequestMessage() {
    }

    public RequestMessage(RequestMessage other) {
        this.id = other.getId();
        this.op = other.getOp();
    }

    public synchronized Integer getId() {
        return id;
    }

    public synchronized void setId(Integer id) {
        this.id = id;
    }

    public synchronized RequestOperationType getOp() {
        return op;
    }

    public synchronized void setOp(RequestOperationType op) {
        this.op = op;
    }
}
