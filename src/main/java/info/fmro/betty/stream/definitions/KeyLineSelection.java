package info.fmro.betty.stream.definitions;

public class KeyLineSelection {
    private Double hc;
    private Long id;

    public KeyLineSelection() {
    }

    public synchronized Double getHc() {
        return hc;
    }

    public synchronized void setHc(Double hc) {
        this.hc = hc;
    }

    public synchronized Long getId() {
        return id;
    }

    public synchronized void setId(Long id) {
        this.id = id;
    }
}
