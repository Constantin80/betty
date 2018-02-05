package info.fmro.betty.entities;

import java.io.Serializable;
import java.util.Objects;

public class Competition
        implements Serializable {

    private static final long serialVersionUID = -3187113086865598702L;
    private String id;
    private String name;

    public Competition() {
    }

    public Competition(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public synchronized String getId() {
        return id;
    }

//    public synchronized void setId(String id) {
//        this.id = id;
//    }
    public synchronized String getName() {
        return name;
    }

//    public synchronized void setName(String name) {
//        this.name = name;
//    }
    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(Object obj) {
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
