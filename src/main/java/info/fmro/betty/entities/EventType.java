package info.fmro.betty.entities;

import org.jetbrains.annotations.Contract;

import java.io.Serializable;
import java.util.Objects;

public class EventType
        implements Serializable {
    private static final long serialVersionUID = -2158475045630703415L;
    private final String id;
    private final String name;

    @Contract(pure = true)
    public EventType(final String id, final String name) {
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
        final EventType other = (EventType) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return Objects.equals(this.name, other.name);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.id);
        hash = 71 * hash + Objects.hashCode(this.name);
        return hash;
    }
}
