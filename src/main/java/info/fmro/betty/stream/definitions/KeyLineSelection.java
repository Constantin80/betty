package info.fmro.betty.stream.definitions;

import java.io.Serializable;

// objects of this class are read from the stream
public class KeyLineSelection
        implements Serializable {
    private static final long serialVersionUID = 3971286653165540247L;
    private Double hc;
    private Long id;

    public KeyLineSelection() {
    }

    public synchronized Double getHc() {
        return hc;
    }

    public synchronized void setHc(final Double hc) {
        this.hc = hc;
    }

    public synchronized Long getId() {
        return id;
    }

    public synchronized void setId(final Long id) {
        this.id = id;
    }
}
