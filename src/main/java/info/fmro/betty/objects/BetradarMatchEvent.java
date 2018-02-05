package info.fmro.betty.objects;

import java.io.Serializable;
import java.util.Objects;

public class BetradarMatchEvent
        implements Serializable {

    private static final long serialVersionUID = 4657650910312201744L;
    private final int minute;
    private final String player, playerIn; // playerIn for substitutions

    public BetradarMatchEvent(int minute, String player, String playerIn) {
        this.minute = minute;
        this.player = player;
        this.playerIn = playerIn;
    }

    public synchronized int getMinute() {
        return minute;
    }

//    public synchronized void setMinute(int minute) {
//        this.minute = minute;
//    }
    public synchronized String getPlayer() {
        return player;
    }

//    public synchronized void setPlayer(String player) {
//        this.player = player;
//    }
    public synchronized String getPlayerIn() {
        return playerIn;
    }

//    public synchronized void setPlayerIn(String playerIn) {
//        this.playerIn = playerIn;
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
        final BetradarMatchEvent other = (BetradarMatchEvent) obj;
        if (this.minute != other.minute) {
            return false;
        }
        if (!Objects.equals(this.player, other.player)) {
            return false;
        }
        return Objects.equals(this.playerIn, other.playerIn);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 41 * hash + this.minute;
        hash = 41 * hash + Objects.hashCode(this.player);
        hash = 41 * hash + Objects.hashCode(this.playerIn);
        return hash;
    }
}
