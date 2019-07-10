package info.fmro.betty.stream.definitions;

import java.io.Serializable;

// objects of this class are read from the stream
public class PriceLadderDefinition
        implements Serializable {
    private static final long serialVersionUID = 6910728478048295440L;
    private PriceLadderType type;

    public synchronized PriceLadderType getType() {
        return this.type;
    }
}
