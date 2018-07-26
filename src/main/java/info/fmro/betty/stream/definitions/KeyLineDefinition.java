package info.fmro.betty.stream.definitions;

import java.util.ArrayList;
import java.util.List;

public class KeyLineDefinition {
    private List<KeyLineSelection> kl;

    public KeyLineDefinition() {
    }

    public synchronized List<KeyLineSelection> getKl() {
        return kl == null ? null : new ArrayList<>(kl);
    }

    public synchronized void setKl(List<KeyLineSelection> kl) {
        this.kl = kl == null ? null : new ArrayList<>(kl);
    }
}
