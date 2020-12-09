package info.fmro.betty.safebet;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

class BetradarMatchEvent
        implements Serializable {
    @Serial
    private static final long serialVersionUID = 4657650910312201744L;
    private final int minute;
    private final String player, playerIn; // playerIn for substitutions

    @SuppressWarnings("unused")
    BetradarMatchEvent(final int minute, final String player, final String playerIn) {
        this.minute = minute;
        this.player = player;
        this.playerIn = playerIn;
    }

    public int getMinute() {
        return this.minute;
    }

    public String getPlayer() {
        return this.player;
    }

    public String getPlayerIn() {
        return this.playerIn;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final BetradarMatchEvent that = (BetradarMatchEvent) obj;
        return this.minute == that.minute &&
               Objects.equals(this.player, that.player) &&
               Objects.equals(this.playerIn, that.playerIn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.minute, this.player, this.playerIn);
    }
}
