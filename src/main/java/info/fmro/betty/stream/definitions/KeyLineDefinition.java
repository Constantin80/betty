package info.fmro.betty.stream.definitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// objects of this class are read from the stream
public class KeyLineDefinition
        implements Serializable {
    private static final long serialVersionUID = 3111037495464828868L;
    private List<KeyLineSelection> kl;

    public KeyLineDefinition() {
    }

    public synchronized List<KeyLineSelection> getKl() {
        return kl == null ? null : new ArrayList<>(kl);
    }

    public synchronized void setKl(final List<KeyLineSelection> kl) {
        this.kl = kl == null ? null : new ArrayList<>(kl);
    }
}
