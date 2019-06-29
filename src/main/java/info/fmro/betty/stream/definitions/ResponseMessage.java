package info.fmro.betty.stream.definitions;

import java.io.Serializable;

// objects of this class are read from the stream
public class ResponseMessage
        implements Serializable {
    private static final long serialVersionUID = -3489834279217007833L;
    private Integer id; // Client generated unique id to link request with response (like json rpc)
    private ResponseOperationType op; // The operation type

    public ResponseMessage() {
    }

    public synchronized Integer getId() {
        return id;
    }

    public synchronized void setId(final Integer id) {
        this.id = id;
    }

    public synchronized ResponseOperationType getOp() {
        return op;
    }

    public synchronized void setOp(final ResponseOperationType op) {
        this.op = op;
    }
}
