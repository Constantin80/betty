package info.fmro.betty.entities;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class KeyLineDescription
        implements Serializable {
    private static final long serialVersionUID = -7685044775812063409L;
    private List<KeyLineSelection> keyLine;

    public KeyLineDescription() {
    }

    @Override
    public synchronized boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        KeyLineDescription that = (KeyLineDescription) o;
        return Objects.equals(keyLine, that.keyLine);
    }

    @Override
    public synchronized int hashCode() {
        return Objects.hash(keyLine);
    }
}
