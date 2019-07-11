package info.fmro.betty.entities;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

class KeyLineDescription
        implements Serializable {
    private static final long serialVersionUID = -7685044775812063409L;
    @SuppressWarnings("unused")
    private List<KeyLineSelection> keyLine;

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final KeyLineDescription that = (KeyLineDescription) obj;
        //noinspection NonFinalFieldReferenceInEquals
        return Objects.equals(this.keyLine, that.keyLine);
    }

    @Override
    public synchronized int hashCode() {
        //noinspection NonFinalFieldReferencedInHashCode
        return Objects.hash(this.keyLine);
    }
}
