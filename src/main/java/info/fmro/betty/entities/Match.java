package info.fmro.betty.entities;

import info.fmro.betty.enums.Side;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

public class Match
        implements Serializable {
    private static final long serialVersionUID = 5267424955233956792L;
    private String betId;
    private String matchId;
    private Side side;
    private Double price;
    private Double size;
    private Date matchDate;

    public Match() {
    }

    public Match(final String betId, final String matchId, final Side side, final Double price, final Double size, final Date matchDate) {
        this.betId = betId;
        this.matchId = matchId;
        this.side = side;
        this.price = price;
        this.size = size;
        this.matchDate = matchDate;
    }

    public synchronized String getBetId() {
        return betId;
    }

    //    public synchronized void setBetId(String betId) {
//        this.betId = betId;
//    }
    public synchronized String getMatchId() {
        return matchId;
    }

    //    public synchronized void setMatchId(String matchId) {
//        this.matchId = matchId;
//    }
    public synchronized Side getSide() {
        return side;
    }

    //    public synchronized void setSide(Side side) {
//        this.side = side;
//    }
    public synchronized Double getPrice() {
        return price;
    }

    //    public synchronized void setPrice(Double price) {
//        this.price = price;
//    }
    public synchronized Double getSize() {
        return size;
    }

    //    public synchronized void setSize(Double size) {
//        this.size = size;
//    }
    public synchronized Date getMatchDate() {
        return matchDate == null ? null : (Date) matchDate.clone();
    }

    //    public synchronized void setMatchDate(Date matchDate) {
//        this.matchDate = matchDate == null ? null : (Date) matchDate.clone();
//    }
    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
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
        final Match other = (Match) obj;
        if (!Objects.equals(this.betId, other.betId)) {
            return false;
        }
        if (!Objects.equals(this.matchId, other.matchId)) {
            return false;
        }
        if (this.side != other.side) {
            return false;
        }
        if (!Objects.equals(this.price, other.price)) {
            return false;
        }
        if (!Objects.equals(this.size, other.size)) {
            return false;
        }
        return Objects.equals(this.matchDate, other.matchDate);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.betId);
        hash = 97 * hash + Objects.hashCode(this.matchId);
        hash = 97 * hash + Objects.hashCode(this.side);
        hash = 97 * hash + Objects.hashCode(this.price);
        hash = 97 * hash + Objects.hashCode(this.size);
        hash = 97 * hash + Objects.hashCode(this.matchDate);
        return hash;
    }
}
