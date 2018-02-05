package info.fmro.betty.enums;

public enum BetOutcome {

    WIN,
    LOSE,
    PLACE;
    // PLACE accounts for Each Way bets where the place portion of the bet won but the win portion lost. 
    // The profit/loss amount in this case could be positive or negative depending on the price matched at.
}
