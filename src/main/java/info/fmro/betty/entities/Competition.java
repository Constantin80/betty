package info.fmro.betty.entities;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.Objects;

public class Competition
        implements Serializable {
    private static final long serialVersionUID = -3187113086865598702L;
    private final String id;
    private final String name;

    @Contract(pure = true)
    public Competition(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    public synchronized String getId() {
        return this.id;
    }

    public synchronized String getName() {
        return this.name;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Competition other = (Competition) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.name, other.name);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 3;
        hash = 13 * hash + Objects.hashCode(this.id);
        hash = 13 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
