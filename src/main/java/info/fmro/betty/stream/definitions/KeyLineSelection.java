package info.fmro.betty.stream.definitions;

import java.io.Serializable;

// objects of this class are read from the stream
@SuppressWarnings("unused")
class KeyLineSelection
        implements Serializable {
    private static final long serialVersionUID = 3971286653165540247L;
    private Double hc;
    private Long id;

    public synchronized Double getHc() {
        return this.hc;
    }

    public synchronized void setHc(final Double hc) {
        this.hc = hc;
    }

    public synchronized Long getId() {
        return this.id;
    }

    public synchronized void setId(final Long id) {
        this.id = id;
    }
}
