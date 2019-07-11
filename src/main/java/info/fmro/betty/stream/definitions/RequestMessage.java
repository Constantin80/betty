package info.fmro.betty.stream.definitions;

import info.fmro.betty.stream.enums.RequestOperationType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

public class RequestMessage
        implements Serializable {
    private static final long serialVersionUID = 6566614737849166532L;
    private Integer id; // Client generated unique id to link request with response (like json rpc)
    private RequestOperationType op; // The operation type

    @Contract(pure = true)
    RequestMessage() {
    }

    public RequestMessage(@NotNull final RequestMessage other) {
        this.id = other.getId();
        this.op = other.getOp();
    }

    public synchronized Integer getId() {
        return this.id;
    }

    public synchronized void setId(final Integer id) {
        this.id = id;
    }

    public synchronized RequestOperationType getOp() {
        return this.op;
    }

    public synchronized void setOp(final RequestOperationType op) {
        this.op = op;
    }
}
