package info.fmro.betty.stream.definitions;

public class ResponseMessage {
    private Integer id; // Client generated unique id to link request with response (like json rpc)
    private ResponseOperationType op; // The operation type

    public ResponseMessage() {
    }

    public synchronized Integer getId() {
        return id;
    }

    public synchronized void setId(Integer id) {
        this.id = id;
    }

    public synchronized ResponseOperationType getOp() {
        return op;
    }

    public synchronized void setOp(ResponseOperationType op) {
        this.op = op;
    }
}
