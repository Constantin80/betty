package info.fmro.betty.safebet;

import info.fmro.shared.enums.MatchStatus;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings("ClassTooDeepInInheritanceTree")
public class CoralEvent
        extends ScraperEvent
        implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(CoralEvent.class);
    private static final long serialVersionUID = -1908827012627039399L;
    private String eventName;
    private int seconds = -1;

    //    public CoralEvent(long eventId) {
//        super("coral", eventId);
//    }
    public CoralEvent(final long eventId, final long timeStamp) {
        super("coral", eventId, timeStamp);
    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized int parseName() {
        final int modified;
        if (this.eventName.contains(" v ")) {
            final int homeModified = this.setHomeTeam(this.eventName.substring(0, this.eventName.indexOf(" v ")).trim());
            final int awayModified = this.setAwayTeam(this.eventName.substring(this.eventName.indexOf(" v ") + " v ".length()).trim());
            modified = homeModified + awayModified;
        } else {
            Generic.alreadyPrintedMap.logOnce(logger, LogLevel.WARN, "unknown coralEvent name home/away separator for: {}", this.eventName);

            // won't allow null to be set
            // homeName = null;
            // awayName = null;
            modified = 0;
        }

        return modified;
    }

    @Contract(pure = true)
    private synchronized String getEventName() {
        return this.eventName;
    }

    public synchronized int setEventName(final String newEventName) {
        final int modified;
        if (this.eventName == null) {
            if (newEventName == null) {
                modified = 0;
            } else {
                this.eventName = newEventName;
                modified = 1;
            }
        } else if (this.eventName.equals(newEventName)) {
            modified = 0;
        } else {
            if (Formulas.matchTeams(this.eventName, newEventName) > Statics.highThreshold) {
                this.setIgnored(minimalBlackListPeriod); // avoid filling logs
//                modified = -10000; // blackListed

            } else {
                this.setIgnored(standardBlackListPeriod);
//                modified = -10000; // blackListed

            }
            this.eventName = newEventName;
            modified = 1;
            logger.error("change eventName {} to {} in coralEvent: {}", this.eventName, newEventName, Generic.objectToString(this));
        }
        if (modified > 0) {
            this.parseName();
        }
        return modified;
    }

    @Override
    public synchronized int setMinutesPlayed(final int newMinutesPlayed) {
        final int modified;
        final int existingMinutesPlayed = this.getMinutesPlayed();
        if (existingMinutesPlayed == newMinutesPlayed) {
            modified = 0;
        } else if (newMinutesPlayed == -1) {
            logger.info("{} reset attempt minutesPlayed to {} from {} for: {} {}/{}", this.objectId, newMinutesPlayed, existingMinutesPlayed, this.getEventId(), this.getHomeTeam(), this.getAwayTeam());
            modified = 0;
        } else if (existingMinutesPlayed < newMinutesPlayed) {
            modified = this.setMinutesPlayedUnckeched(newMinutesPlayed);
        } else if (existingMinutesPlayed <= 0) { // this.minutesPlayed > minutesPlayed && minutesPlayed != -1
            this.setIgnored(minimalBlackListPeriod);
            logger.error("{} outOfRange change minutesPlayed {} to {} in scraperEvent: {}", this.objectId, existingMinutesPlayed, newMinutesPlayed, Generic.objectToString(this));
//            modified = -10000; // blackListed

            modified = this.setMinutesPlayedUnckeched(newMinutesPlayed);
        } else if (newMinutesPlayed > 0 && existingMinutesPlayed - newMinutesPlayed == 1) {
            modified = this.setMinutesPlayedUnckeched(newMinutesPlayed); // minutesPlayed taken back by 1; happens sometimes
        } else if ((newMinutesPlayed == 120 && this.getMatchStatus() == MatchStatus.AWAITING_PEN) || (newMinutesPlayed >= 105 && this.getMatchStatus() == MatchStatus.SECOND_ET) ||
                   (newMinutesPlayed == 105 && this.getMatchStatus() == MatchStatus.ET_HALF_TIME) || (newMinutesPlayed >= 90 && this.getMatchStatus() == MatchStatus.FIRST_ET) ||
                   (newMinutesPlayed == 90 && this.getMatchStatus() == MatchStatus.AWAITING_ET) || (newMinutesPlayed >= 45 && this.getMatchStatus() == MatchStatus.SECOND_HALF) ||
                   (newMinutesPlayed == 45 && this.getMatchStatus() == MatchStatus.HALF_TIME) || (newMinutesPlayed >= 0 && this.getMatchStatus() == MatchStatus.FIRST_HALF)) {
            logger.warn("REVIEW {} minutesPlayed {} reduced to {} in scraperEvent: {}", this.objectId, existingMinutesPlayed, newMinutesPlayed, Generic.objectToString(this));
            modified = this.setMinutesPlayedUnckeched(newMinutesPlayed); // minutesPlayed taken back by larger amount, but in the same period; happens sometimes
        } else {
            this.setIgnored(minimalBlackListPeriod);
            logger.error("{} change differentPeriod minutesPlayed {} to {} in scraperEvent: {}", this.objectId, existingMinutesPlayed, newMinutesPlayed, Generic.objectToString(this));
//            modified = -10000; // blackListed

            modified = this.setMinutesPlayedUnckeched(newMinutesPlayed);
        }
        return modified;
    }

    public synchronized int getSeconds() {
        return this.seconds;
    }

    public synchronized int setSeconds(final int newSeconds) {
        final int modified;
        if (this.seconds == newSeconds) {
            modified = 0;
        } else if (newSeconds == -1) {
            logger.info("{} reset attempt seconds to {} from {} for: {} {}/{}", this.objectId, newSeconds, this.seconds, this.getEventId(), this.getHomeTeam(), this.getAwayTeam());
            modified = 0;
        } else {
            this.seconds = newSeconds;
            modified = 1;
        }
        return modified;
    }

    @Override
    public synchronized int setMatchStatus(final MatchStatus newMatchStatus) {
        int modified = 0;
        modified += setMatchStatus(newMatchStatus, true);
        if (modified > 0 && newMatchStatus == MatchStatus.HALF_TIME) {
            modified += setHomeHtScore(getHomeScore());
            modified += setAwayHtScore(getAwayScore());
        }

        return modified;
    }

    @Override
    public synchronized int innerUpdate(@NotNull final ScraperEvent scraperEvent) {
        int modified = super.innerUpdate(scraperEvent);
        final CoralEvent coralEvent = (CoralEvent) scraperEvent;

        modified += this.setEventName(coralEvent.getEventName());
        modified += this.setSeconds(coralEvent.getSeconds());

        return modified;
    }

    @Override
    public synchronized long innerErrors(final AtomicLong blackListPeriod) {
        long errors = super.innerErrors(blackListPeriod);
        final int minutesPlayed = this.getMinutesPlayed();
        final int homeScore = this.getHomeScore();
        final int awayScore = this.getAwayScore();
        final int homeHtScore = this.getHomeHtScore();
        final int awayHtScore = this.getAwayHtScore();
        if ((minutesPlayed >= 0) == (this.seconds < 0)) {
            errors += 102400L;
            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
        }
        if (!bothScoreExists()) {
            errors += 204800L;
            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
        }
        final MatchStatus matchStatus = this.getMatchStatus();
        if (matchStatus == null) { // sometimes the period is not known
        } else {
            switch (matchStatus) {
                case START_DELAYED:
                case POSTPONED:
                case ABANDONED:
                case CANCELLED:
                case AWAITING_ET:
                case FIRST_ET:
                case ET_HALF_TIME:
                case SECOND_ET:
                case AWAITING_PEN:
                case PENALTIES:
                case AFTER_ET:
                case AFTER_PEN:
                case ENDED:
                case INTERRUPTED:
                    errors += 409600L; // unsupported status
                    break;
                case NOT_STARTED:
                    if (this.getHomeScore() != 0 || this.getAwayScore() != 0 || anyHtScoreExists() || this.getHomeRedCards() > 0 || this.getAwayRedCards() > 0 || stoppageTimeExists() || !minutesAre(0) || this.getSeconds() != 0) {
                        errors += 13107200L;
                    }
                    break;
                case FIRST_HALF:
                    // no condition; existance of scores is checked before this switch
                    break;
                case HALF_TIME:
                    if (!bothScoreExists() || (!this.isHtIgnored() && (!bothHtScoreExists() || homeScore != homeHtScore || awayScore != awayHtScore))) {
                        errors += 819200L;
                    }
                    break;
                case SECOND_HALF:
                    // it does actually happen
//                    if (!bothHtScoreExists()) {
//                        errors += 1638400L;
//                    }
                    break;
                case OVERTIME:
                    // no condition I can think of
                    break;
                case UNKNOWN:
                    logger.error("matchStatus {} in coralEvent check for: {}", matchStatus, Generic.objectToString(this));
                    errors += 3276800L;
                    break;
                default:
                    logger.error("unsupported matchStatus {} in coralEvent check for: {}", matchStatus, Generic.objectToString(this));
                    errors += 6553600L;
                    break;
            } // end switch
        } // end else
        return errors;
    }
}
