package info.fmro.betty.stream.definitions;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// objects of this class are read from the stream
public class KeyLineDefinition
        implements Serializable {
    private static final long serialVersionUID = 3111037495464828868L;
    @Nullable
    private List<KeyLineSelection> kl;

    @Nullable
    public synchronized List<KeyLineSelection> getKl() {
        return this.kl == null ? null : new ArrayList<>(this.kl);
    }

    public synchronized void setKl(final List<? extends KeyLineSelection> kl) {
        this.kl = kl == null ? null : new ArrayList<>(kl);
    }
}
