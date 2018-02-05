package info.fmro.betty.objects;

import info.fmro.shared.utility.LogLevel;
import info.fmro.betty.enums.MatchStatus;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.AlreadyPrintedMap;
import info.fmro.shared.utility.Generic;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public CoralEvent(long eventId, long timeStamp) {
        super("coral", eventId, timeStamp);
    }

    public synchronized int parseName() {
        int modified;
        if (eventName.contains(" v ")) {
            int homeModified = this.setHomeTeam(eventName.substring(0, eventName.indexOf(" v ")).trim());
            int awayModified = this.setAwayTeam(eventName.substring(eventName.indexOf(" v ") + " v ".length()).trim());
            modified = homeModified + awayModified;
        } else {
            Generic.alreadyPrintedMap.logOnce(logger, LogLevel.WARN, "unknown coralEvent name home/away separator for: {}", eventName);

            // won't allow null to be set
            // homeName = null;
            // awayName = null;
            modified = 0;
        }

        return modified;
    }

    public synchronized String getEventName() {
        return eventName;
    }

    public synchronized int setEventName(String eventName) {
        final int modified;
        if (this.eventName == null) {
            if (eventName == null) {
                modified = 0;
            } else {
                this.eventName = eventName;
                modified = 1;
            }
        } else if (this.eventName.equals(eventName)) {
            modified = 0;
        } else {
            if (Formulas.matchTeams(this.eventName, eventName) > Statics.highThreshold) {
                BlackList.ignoreScraper(this, minimalBlackListPeriod); // avoid filling logs
//                modified = -10000; // blackListed

                this.eventName = eventName;
                modified = 1;
            } else {
                BlackList.ignoreScraper(this, standardBlackListPeriod);
//                modified = -10000; // blackListed

                this.eventName = eventName;
                modified = 1;
            }
            logger.error("change eventName {} to {} in coralEvent: {}", this.eventName, eventName, Generic.objectToString(this));
        }
        if (modified > 0) {
            this.parseName();
        }
        return modified;
    }

    @Override
    public synchronized int setMinutesPlayed(int minutesPlayed) {
        final int modified;
        final int existingMinutesPlayed = this.getMinutesPlayed();
        if (existingMinutesPlayed == minutesPlayed) {
            modified = 0;
        } else if (minutesPlayed == -1) {
            logger.info("{} reset attempt mainutesPlayed to {} from {} for: {} {}/{}", objectId, minutesPlayed, existingMinutesPlayed, this.getEventId(), this.getHomeTeam(),
                    this.getAwayTeam());
            modified = 0;
        } else if (existingMinutesPlayed < minutesPlayed) {
            modified = this.setMinutesPlayedUnckeched(minutesPlayed);
        } else if (existingMinutesPlayed <= 0) { // this.minutesPlayed > minutesPlayed && minutesPlayed != -1
            BlackList.ignoreScraper(this, minimalBlackListPeriod);
            logger.error("{} outOfRange change minutesPlayed {} to {} in scraperEvent: {}", objectId, existingMinutesPlayed, minutesPlayed, Generic.objectToString(this));
//            modified = -10000; // blackListed

            modified = this.setMinutesPlayedUnckeched(minutesPlayed);
        } else if (minutesPlayed > 0 && existingMinutesPlayed - minutesPlayed == 1) {
            modified = this.setMinutesPlayedUnckeched(minutesPlayed); // minutesPlayed taken back by 1; happens sometimes
        } else if ((minutesPlayed == 120 && this.getMatchStatus() == MatchStatus.AWAITING_PEN) || (minutesPlayed >= 105 && this.getMatchStatus() == MatchStatus.SECOND_ET) ||
                (minutesPlayed == 105 && this.getMatchStatus() == MatchStatus.ET_HALF_TIME) || (minutesPlayed >= 90 && this.getMatchStatus() == MatchStatus.FIRST_ET) ||
                (minutesPlayed == 90 && this.getMatchStatus() == MatchStatus.AWAITING_ET) || (minutesPlayed >= 45 && this.getMatchStatus() == MatchStatus.SECOND_HALF) ||
                (minutesPlayed == 45 && this.getMatchStatus() == MatchStatus.HALF_TIME) || (minutesPlayed >= 0 && this.getMatchStatus() == MatchStatus.FIRST_HALF)) {
            logger.warn("REVIEW {} minutesPlayed {} reduced to {} in scraperEvent: {}", objectId, existingMinutesPlayed, minutesPlayed, Generic.objectToString(this));
            modified = this.setMinutesPlayedUnckeched(minutesPlayed); // minutesPlayed taken back by larger amount, but in the same period; happens sometimes
        } else {
            BlackList.ignoreScraper(this, minimalBlackListPeriod);
            logger.error("{} change differentPeriod minutesPlayed {} to {} in scraperEvent: {}", objectId, existingMinutesPlayed, minutesPlayed, Generic.objectToString(this));
//            modified = -10000; // blackListed

            modified = this.setMinutesPlayedUnckeched(minutesPlayed);
        }
        return modified;
    }

    public synchronized int getSeconds() {
        return seconds;
    }

    public synchronized int setSeconds(int seconds) {
        int modified;
        if (this.seconds == seconds) {
            modified = 0;
        } else if (seconds == -1) {
            logger.info("{} reset attempt seconds to {} from {} for: {} {}/{}", objectId, seconds, this.seconds, this.getEventId(), this.getHomeTeam(), this.getAwayTeam());
            modified = 0;
        } else {
            this.seconds = seconds;
            modified = 1;
        }
        return modified;
    }

    @Override
    public synchronized int setMatchStatus(MatchStatus matchStatus) {
        int modified = 0;
        modified += super.setMatchStatus(matchStatus, true);
        if (modified > 0 && matchStatus == MatchStatus.HALF_TIME) {
            modified += setHomeHtScore(getHomeScore());
            modified += setAwayHtScore(getAwayScore());
        }

        return modified;
    }

    @Override
    public synchronized int innerUpdate(ScraperEvent scraperEvent) {
        int modified = super.innerUpdate(scraperEvent);
        CoralEvent coralEvent = (CoralEvent) scraperEvent;

        modified += this.setEventName(coralEvent.getEventName());
        modified += this.setSeconds(coralEvent.getSeconds());

        return modified;
    }

    @Override
    public synchronized long innerErrors(AtomicLong blackListPeriod) {
        long errors = super.innerErrors(blackListPeriod);
        int minutesPlayed = this.getMinutesPlayed(), homeScore = this.getHomeScore(), awayScore = this.getAwayScore(), homeHtScore = this.getHomeHtScore(),
                awayHtScore = this.getAwayHtScore();
        if ((minutesPlayed >= 0 && this.seconds < 0) || (minutesPlayed < 0 && this.seconds >= 0)) {
            errors += 102400L;
            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
        }
        if (!bothScoreExists()) {
            errors += 204800L;
            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
        }
        MatchStatus matchStatus = this.getMatchStatus();
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
                    if (this.getHomeScore() != 0 || this.getAwayScore() != 0 || anyHtScoreExists() || this.getHomeRedCards() > 0 || this.getAwayRedCards() > 0 ||
                            stoppageTimeExists() || !minutesAre(0) || this.getSeconds() != 0) {
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
