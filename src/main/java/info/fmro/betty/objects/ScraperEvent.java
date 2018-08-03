package info.fmro.betty.objects;

import info.fmro.betty.entities.Event;
import info.fmro.betty.enums.MatchStatus;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.Ignorable;
import info.fmro.shared.utility.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

public class ScraperEvent
        extends Ignorable
        implements Serializable, Comparable<ScraperEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ScraperEvent.class);
    private static final long serialVersionUID = 5045581708147478634L;
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    public static final long standardBlackListPeriod = Generic.MINUTE_LENGTH_MILLISECONDS * 5L, shortBlackListPeriod = Generic.MINUTE_LENGTH_MILLISECONDS * 2L,
            minimalBlackListPeriod = 10_000L;
    public final String objectId;
    private final long eventId;
    private long timeStamp, homeScoreTimeStamp, awayScoreTimeStamp, matchedTimeStamp; // score stamps for last time a valid & >=0 score was seen
    private String homeTeam, awayTeam, matchedEventId;
    private int homeScore = -1, awayScore = -1, homeHtScore = -1, awayHtScore = -1, minutesPlayed = -1, stoppageTime = -1, homeRedCards = -1, awayRedCards = -1;
    private MatchStatus matchStatus;
    private boolean htIgnored;

    public ScraperEvent(String objectId, long eventId) {
        this.objectId = objectId;
        this.eventId = eventId;
        this.timeStamp();
    }

    public ScraperEvent(String objectId, long eventId, long timeStamp) {
        this.objectId = objectId;
        this.eventId = eventId;
        this.setTimeStamp(timeStamp);
    }

    public synchronized String getObjectId() {
        return objectId;
    }

    public synchronized long getEventId() {
        return eventId;
    }

    //    public synchronized int setEventId(long eventId) {
//        final int modified;
//        if (this.eventId == eventId) {
//            modified = 0;
//        } else if (this.eventId == -1) {
//            this.eventId = eventId;
//            modified = 1;
//        } else {
//            BlackList.ignoreScraper(this, shortBlackListPeriod);
//
//            // trick to purge the eventId from the argument too
//            long previousEventId = this.eventId;
//            this.eventId = eventId;
//            BlackList.ignoreScraper(this, shortBlackListPeriod);
//            this.eventId = previousEventId;
//
//            logger.error("{} attempted to change eventId {} to {} in scraperEvent: {}", objectId, this.eventId, eventId, Generic.objectToString(this));
//            modified = -10000; // blackListed
//        }
//        return modified;
//    }
    public synchronized boolean isHtIgnored() {
        return htIgnored;
    }

    public synchronized int ignoreHt() {
        return setHtIgnored(true);
    }

    public synchronized int setHtIgnored(boolean htIgnored) {
        final int modified;
        if (this.htIgnored == htIgnored) {
            modified = 0;
        } else if (!this.htIgnored) {
            this.htIgnored = htIgnored;
            modified = 1;
        } else { // this.htIgnored true and htIgnored false
            logger.error("{} attempted to reset htIgnored in setHtIgnored for: {}", objectId, Generic.objectToString(this));
            modified = 0;
        }

        return modified;
    }

    public synchronized int resetHtIgnored() {
        final int modified;
        if (this.htIgnored) {
            modified = 1;
        } else {
            modified = 0;
        }
        this.htIgnored = false;

        return modified;
    }

    public synchronized String getHomeTeam() {
        return homeTeam;
    }

    public synchronized int setHomeTeam(String homeTeam) {
        final int modified;
        if (this.homeTeam == null) {
            if (homeTeam == null) {
                modified = 0;
            } else {
                this.homeTeam = homeTeam;
                modified = 1;
            }
        } else if (this.homeTeam.equals(homeTeam)) {
            modified = 0;
        } else {
            if (Formulas.matchTeams(this.homeTeam, homeTeam) > Statics.highThreshold) {
                this.setIgnored(shortBlackListPeriod);
//                modified = -10000; // blackListed
            } else {
                this.setIgnored(standardBlackListPeriod);
//                modified = -10000; // blackListed
            }
            logger.error("{} change homeTeam {} to {} in scraperEvent: {}", objectId, this.homeTeam, homeTeam, Generic.objectToString(this));

            this.homeTeam = homeTeam; // modification goes ahead
            modified = 1;
        }
        return modified;
    }

    public synchronized String getAwayTeam() {
        return awayTeam;
    }

    public synchronized int setAwayTeam(String awayTeam) {
        final int modified;
        if (this.awayTeam == null) {
            if (awayTeam == null) {
                modified = 0;
            } else {
                this.awayTeam = awayTeam;
                modified = 1;
            }
        } else if (this.awayTeam.equals(awayTeam)) {
            modified = 0;
        } else {
            if (Formulas.matchTeams(this.awayTeam, awayTeam) > Statics.highThreshold) {
                this.setIgnored(shortBlackListPeriod);
//                modified = -10000; // blackListed
            } else {
                this.setIgnored(standardBlackListPeriod);
//                modified = -10000; // blackListed
            }
            logger.error("{} change awayTeam {} to {} in scraperEvent: {}", objectId, this.awayTeam, awayTeam, Generic.objectToString(this));

            this.awayTeam = awayTeam; // modification goes ahead
            modified = 1;
        }
        return modified;
    }

    public synchronized int getHomeRedCards() {
        return homeRedCards;
    }

    public synchronized int setHomeRedCards(int homeRedCards) {
        final int modified;
        if (this.homeRedCards == homeRedCards) {
            modified = 0;
        } else if (this.homeRedCards < homeRedCards) {
            this.homeRedCards = homeRedCards;
            modified = 1;
        } else {
            this.setIgnored(standardBlackListPeriod);
            logger.error("{} change homeRedCards {} to {} in scraperEvent: {}", objectId, this.homeRedCards, homeRedCards, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.homeRedCards = homeRedCards; // modification goes ahead
            modified = 1;
        }
        return modified;
    }

    public synchronized int getAwayRedCards() {
        return awayRedCards;
    }

    public synchronized int setAwayRedCards(int awayRedCards) {
        final int modified;
        if (this.awayRedCards == awayRedCards) {
            modified = 0;
        } else if (this.awayRedCards < awayRedCards) {
            this.awayRedCards = awayRedCards;
            modified = 1;
        } else {
            this.setIgnored(standardBlackListPeriod);
            logger.error("{} change awayRedCards {} to {} in scraperEvent: {}", objectId, this.awayRedCards, awayRedCards, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.awayRedCards = awayRedCards; // modification goes ahead
            modified = 1;
        }
        return modified;
    }

    public synchronized int getHomeScore() {
        return homeScore;
    }

    public synchronized int setHomeScore(int homeScore) {
        return setHomeScore(homeScore, isPenalties());
    }

    public synchronized int setHomeScore(int homeScore, boolean isPenalties) {
        final int modified;
        final long currentTime = System.currentTimeMillis();
        final long timeLapsed = currentTime - this.getHomeScoreTimeStamp();

        if (this.homeScore == homeScore) {
            if (homeScore >= 0) {
                this.setHomeScoreTimeStamp(currentTime);
            } else { // default score, won't stamp
            }
            modified = 0;
        } else if (homeScore == -1) {
            logger.error("{} attempted to reset homeScore {} in scraperEvent: {}", objectId, this.homeScore, Generic.objectToString(this));
            modified = 0; // ignored reset
        } else {
            final int addedGoals = homeScore - this.homeScore;
            if (addedGoals > 0) {
                if (addedGoals > 1) {
                    if (timeLapsed < 15_000L * addedGoals && !isPenalties) {
                        // more than 1 goal per 15 seconds; this error does happen fairly often, as scores are sometimes not updated immediately
                        this.setIgnored(standardBlackListPeriod, currentTime);
                        logger.error("{} change homeScore {} to {} after {} ms in scraperEvent: {}", objectId, this.homeScore, homeScore, timeLapsed, Generic.objectToString(this));
//                        modified = -10000; // blackListed

                        this.homeScore = homeScore;
                        modified = 1;
                    } else {
                        this.homeScore = homeScore;
                        modified = 1;
                    }
                } else { // addedGoals == 1
                    this.homeScore = homeScore;
                    modified = 1;
                }
            } else if (homeScore >= 0 && addedGoals == -1) { // score taken back by 1 goal; happens rarely due to scrapeSite score errors
                this.setIgnored(shortBlackListPeriod, currentTime);
                logger.error("REVIEW {} homeScore {} reduced to {} in scraperEvent: {}", objectId, this.homeScore, homeScore, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.homeScore = homeScore;
                modified = 1;
            } else {
                this.setIgnored(standardBlackListPeriod, currentTime);
                logger.error("{} change homeScore {} to {} in scraperEvent: {}", objectId, this.homeScore, homeScore, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.homeScore = homeScore;
                modified = 1;
            }
        } // end else

        if (modified > 0) {
            this.setHomeScoreTimeStamp(currentTime);
        }
        return modified;
    }

    public synchronized long getHomeScoreTimeStamp() {
        return homeScoreTimeStamp;
    }

    public synchronized void setHomeScoreTimeStamp(long homeScoreTimeStamp) {
        this.homeScoreTimeStamp = homeScoreTimeStamp;
    }

    public synchronized int getAwayScore() {
        return awayScore;
    }

    public synchronized int setAwayScore(int awayScore) {
        return setAwayScore(awayScore, isPenalties());
    }

    public synchronized int setAwayScore(int awayScore, boolean isPenalties) {
        final int modified;
        final long currentTime = System.currentTimeMillis();
        final long timeLapsed = currentTime - this.getAwayScoreTimeStamp();

        if (this.awayScore == awayScore) {
            if (awayScore >= 0) {
                this.setAwayScoreTimeStamp(currentTime);
            } else { // default score, won't stamp
            }
            modified = 0;
        } else if (awayScore == -1) {
            logger.error("{} attempted to reset awayScore {} in scraperEvent: {}", objectId, this.awayScore, Generic.objectToString(this));
            modified = 0; // ignored reset
        } else {
            final int addedGoals = awayScore - this.awayScore;
            if (addedGoals > 0) {
                if (addedGoals > 1) {
                    if (timeLapsed < 15000L * addedGoals && !isPenalties) {
                        // more than 1 goal per 15 seconds; this error does happen fairly often, as scores are sometimes not updated immediately
                        this.setIgnored(standardBlackListPeriod, currentTime);
                        logger.error("{} change awayScore {} to {} after {} ms in scraperEvent: {}", objectId, this.awayScore, awayScore, timeLapsed, Generic.objectToString(this));
//                        modified = -10000; // blackListed

                        this.awayScore = awayScore;
                        modified = 1;
                    } else {
                        this.awayScore = awayScore;
                        modified = 1;
                    }
                } else { // addedGoals == 1
                    this.awayScore = awayScore;
                    modified = 1;
                }
            } else if (awayScore >= 0 && addedGoals == -1) { // score taken back by 1 goal; happens rarely due to scrapeSite score errors
                this.setIgnored(shortBlackListPeriod, currentTime);
                logger.error("REVIEW {} awayScore {} reduced to {} in scraperEvent: {}", objectId, this.awayScore, awayScore, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.awayScore = awayScore;
                modified = 1;
            } else {
                this.setIgnored(standardBlackListPeriod, currentTime);
                logger.error("{} attempted to change awayScore {} to {} in scraperEvent: {}", objectId, this.awayScore, awayScore, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.awayScore = awayScore;
                modified = 1;
            }
        } // end else

        if (modified > 0) {
            this.setAwayScoreTimeStamp(currentTime);
        }
        return modified;
    }

    public synchronized long getAwayScoreTimeStamp() {
        return awayScoreTimeStamp;
    }

    public synchronized void setAwayScoreTimeStamp(long awayScoreTimeStamp) {
        this.awayScoreTimeStamp = awayScoreTimeStamp;
    }

    public synchronized int getHomeHtScore() {
        final int result;
        if (isHtIgnored()) {
            result = -1;
        } else {
            result = this.homeHtScore;
        }
        return result;
    }

    public synchronized int setHomeHtScore(int homeHtScore) {
        return setHomeHtScore(homeHtScore, false);
    }

    public synchronized int setHomeHtScore(int homeHtScore, boolean allowedReset) {
        final int modified;
        if (this.homeHtScore == homeHtScore) {
            modified = 0;
        } else if (this.homeHtScore == -1) {
            this.homeHtScore = homeHtScore;
            modified = 1;
        } else if (homeHtScore == -1) {
//            if (allowedReset) { // reset of ht score is allowed for betradar in case of AFTER_ET & AFTER_PEN
//                this.homeHtScore = homeHtScore;
//                modified = 1;
//            } else { // no error, just ignored reset
//                modified = 0;
//            }
            // allowedReset support removed, htScore will just stay there
            modified = 0;
        } else {
            this.setIgnored(standardBlackListPeriod);
            this.ignoreHt();
            logger.error("{} change homeHtScore {} to {} in scraperEvent, will ignoredHt: {}", objectId, this.homeHtScore, homeHtScore, Generic.objectToString(this));
            this.homeHtScore = homeHtScore; // after logging the error message
            modified = 1;
//            modified = -10000; // blackListed
        }
        return modified;
    }

    public synchronized int getAwayHtScore() {
        final int result;
        if (isHtIgnored()) {
            result = -1;
        } else {
            result = this.awayHtScore;
        }
        return result;
    }

    public synchronized int setAwayHtScore(int awayHtScore) {
        return setAwayHtScore(awayHtScore, false);
    }

    public synchronized int setAwayHtScore(int awayHtScore, boolean allowedReset) {
        final int modified;
        if (this.awayHtScore == awayHtScore) {
            modified = 0;
        } else if (this.awayHtScore == -1) {
            this.awayHtScore = awayHtScore;
            modified = 1;
        } else if (awayHtScore == -1) {
//            if (allowedReset) { // reset of ht score is allowed for betradar in case of AFTER_ET & AFTER_PEN
//                this.awayHtScore = awayHtScore;
//                modified = 1;
//            } else { // no error, just ignored reset
//                modified = 0;
//            }
            // allowedReset support removed, htScore will just stay there
            modified = 0;
        } else {
            this.setIgnored(standardBlackListPeriod);
            this.ignoreHt();
            logger.error("{} change awayHtScore {} to {} in scraperEvent, will ignoredHt: {}", objectId, this.awayHtScore, awayHtScore, Generic.objectToString(this));
            this.awayHtScore = awayHtScore; // after logging the error message
            modified = 1;
//            modified = -10000; // blackListed
        }
        return modified;
    }

    public synchronized MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public synchronized int setMatchStatus(MatchStatus matchStatus) {
        return setMatchStatus(matchStatus, false);
    }

    public synchronized int setMatchStatus(MatchStatus matchStatus, boolean resetToNullIgnored) {
        final int modified;
        if (this.matchStatus == null) {
            if (matchStatus == null) {
                modified = 0;
            } else {
                this.matchStatus = matchStatus;
                modified = 1;
            }
        } else if (this.matchStatus.equals(matchStatus)) {
            modified = 0;
        } else if (matchStatus == null) {
            if (resetToNullIgnored) { // this seems to happen in case of coral just before the match disappears, but not only then
                final String printedString = MessageFormatter.arrayFormat("{} reset attempt matchStatus to {} from {} for: {} {}/{}",
                                                                          new Object[]{objectId, matchStatus, this.matchStatus, this.eventId, this.homeTeam, this.awayTeam}).getMessage();

                if ("coral".equals(objectId)) {
                    Generic.alreadyPrintedMap.logOnce(Statics.debugLevel.check(2, 210), Generic.MINUTE_LENGTH_MILLISECONDS * 5L, logger, LogLevel.INFO, printedString);
                } else {
                    logger.error(printedString);
                }
//                logger.info("{} reset attempt matchStatus to {} from {} for: {} {}/{}", objectId, matchStatus, this.matchStatus, this.eventId, this.homeTeam, this.awayTeam);
//                this.matchStatus = matchStatus;
                modified = 0;
            } else {
                this.setIgnored(shortBlackListPeriod);
                logger.error("{} reset matchStatus {} to {} in scraperEvent: {}", objectId, this.matchStatus, matchStatus, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.matchStatus = matchStatus;
                modified = 1;
            }
        } else if (matchStatus == MatchStatus.NOT_STARTED || matchStatus == MatchStatus.POSTPONED) {
            this.setIgnored(shortBlackListPeriod);
            logger.error("{} reset matchStatus {} to {} in scraperEvent: {}", objectId, this.matchStatus, matchStatus, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.matchStatus = matchStatus;
            modified = 1;
        } else if (this.matchStatus.ordinal() < MatchStatus.FIRST_HALF.ordinal() || this.matchStatus.ordinal() > MatchStatus.ENDED.ordinal() ||
                   this.matchStatus.ordinal() < matchStatus.ordinal()) {
            this.matchStatus = matchStatus;
            modified = 1;
        } else if (this.matchStatus.ordinal() - matchStatus.ordinal() == 1) {
            this.setIgnored(shortBlackListPeriod);
            logger.error("REVIEW {} matchStatus {} reduced to {} in scraperEvent: {}", objectId, this.matchStatus, matchStatus, Generic.objectToString(this));
//            this.matchStatus = matchStatus; // matchStatus taken back by 1 setting; does happen rarely
//            modified = 1;
//            modified = -10000; // blackListed

            this.matchStatus = matchStatus;
            modified = 1;
        } else {
            this.setIgnored(standardBlackListPeriod);
            logger.error("{} change matchStatus {} to {} in scraperEvent: {}", objectId, this.matchStatus, matchStatus, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.matchStatus = matchStatus;
            modified = 1;
        }

        return modified;
    }

    public synchronized int getMinutesPlayed() {
        return minutesPlayed;
    }

    public synchronized int setMinutesPlayedUnckeched(int minutesPlayed) {
        final int modified;
        if (this.minutesPlayed == minutesPlayed) {
            modified = 0;
        } else {
            this.minutesPlayed = minutesPlayed;
            modified = 1;
        }
        return modified;
    }

    public synchronized int setMinutesPlayed(int minutesPlayed) {
        final int modified;
        if (this.minutesPlayed == minutesPlayed) {
            modified = 0;
        } else if (this.minutesPlayed < minutesPlayed || minutesPlayed == -1) { // minutesPlayed can reset when match is over
            this.minutesPlayed = minutesPlayed;
            modified = 1;
        } else if (this.minutesPlayed <= 1) { // this.minutesPlayed > minutesPlayed && minutesPlayed != -1
            this.setIgnored(minimalBlackListPeriod);
            logger.error("{} outOfRange change minutesPlayed {} to {} in scraperEvent: {}", objectId, this.minutesPlayed, minutesPlayed, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.minutesPlayed = minutesPlayed;
            modified = 1;
        } else if (minutesPlayed > 0 && this.minutesPlayed - minutesPlayed == 1) {
            this.minutesPlayed = minutesPlayed; // minutesPlayed taken back by 1; happens sometimes
            modified = 1;
        } else if ((this.minutesPlayed > 1 && this.minutesPlayed < 45 && minutesPlayed >= 1) ||
                   (this.minutesPlayed > 46 && this.minutesPlayed < 90 && minutesPlayed >= 46) ||
                   (this.minutesPlayed > 91 && this.minutesPlayed < 105 && minutesPlayed >= 91) ||
                   (this.minutesPlayed > 106 && this.minutesPlayed < 120 && minutesPlayed >= 106)) {
            logger.warn("REVIEW {} minutesPlayed {} reduced to {} in scraperEvent: {}", objectId, this.minutesPlayed, minutesPlayed, Generic.objectToString(this));
            this.minutesPlayed = minutesPlayed; // minutesPlayed taken back by larger amount, but in the same period; happens sometimes
            modified = 1;
        } else if ((this.minutesPlayed == 45 && minutesPlayed >= 1) ||
                   (this.minutesPlayed == 90 && minutesPlayed >= 46) ||
                   (this.minutesPlayed == 105 && minutesPlayed >= 91) ||
                   (this.minutesPlayed == 120 && minutesPlayed >= 106)) {
            this.setIgnored(minimalBlackListPeriod);
            logger.error("{} change breakLike minutesPlayed {} to {} in scraperEvent: {}", objectId, this.minutesPlayed, minutesPlayed, Generic.objectToString(this));
            // this.minutesPlayed = minutesPlayed; // minutesPlayed taken back by larger amount, but in the same period or during break
            // modified = 1;
//            modified = -10000; // blackListed

            this.minutesPlayed = minutesPlayed;
            modified = 1;
        } else {
            this.setIgnored(minimalBlackListPeriod);
            logger.error("{} change differentPeriod minutesPlayed {} to {} in scraperEvent: {}", objectId, this.minutesPlayed, minutesPlayed, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.minutesPlayed = minutesPlayed;
            modified = 1;
        }
        return modified;
    }

    public synchronized int getStoppageTime() {
        return stoppageTime;
    }

    public synchronized int setStoppageTime(int stoppageTime) {
        final int modified;
        if (this.stoppageTime == stoppageTime) {
            modified = 0;
        } else if (this.stoppageTime < stoppageTime || stoppageTime == -1) { // stoppageTime will reset when periods or match are over
            this.stoppageTime = stoppageTime;
            modified = 1;
        } else if (stoppageTime > 0 && this.stoppageTime - stoppageTime == 1) {
            this.stoppageTime = stoppageTime; // stoppageTime taken back by 1; might happen sometimes
            modified = 1;
        } else {
//            BlackList.ignoreScraper(this, minimalBlackListPeriod);
//            modified = -10000; // blackListed
            logger.error("REVIEW {} stoppageTime {} reduced to {} in scraperEvent: {}", objectId, this.stoppageTime, stoppageTime, Generic.objectToString(this));
            this.stoppageTime = stoppageTime; // stoppageTime taken back by larger amount; happens sometimes
            modified = 1;
        }
        return modified;
    }

    // ignored scrapers are still used for matching, and I don't want that
//    @Override
//    public synchronized boolean isIgnored(long currentTime) {
//        boolean isIgnored = super.isIgnored(currentTime);
//        if (!isIgnored) {
//            final long timeSinceUpdate = currentTime - this.getTimeStamp();
//            isIgnored = timeSinceUpdate > Generic.HOUR_LENGTH_MILLISECONDS;
//            logger.warn("scraperEvent ignored due to being obsolete: {}", timeSinceUpdate);
//        }
//
//        return isIgnored;
//    }
    public synchronized boolean isTooOldForMatching() {
        final long currentTime = System.currentTimeMillis();
        return isTooOldForMatching(currentTime);
    }

    public synchronized boolean isTooOldForMatching(long currentTime) {
        final long timeSinceUpdate = currentTime - this.getTimeStamp();
        return timeSinceUpdate > Generic.HOUR_LENGTH_MILLISECONDS;
    }

    @Override
    public synchronized int setIgnored(long period) {
        final long currentTime = System.currentTimeMillis();
        return setIgnored(period, currentTime);
    }

    @Override
    public synchronized int setIgnored(long period, long currentTime) {
        final int modified = super.setIgnored(period, currentTime);

        if (modified > 0) {
            if (this.matchedEventId != null) {
                final Event event = Statics.eventsMap.get(this.matchedEventId);
                if (event != null) {
                    event.ignoredScrapersCheck();
                    if (!event.isIgnored()) {
                        logger.info("event still not ignored after ScraperEvent ignored: {} {} {}", this.eventId, event.getId(), event.getNValidScraperEventIds());
                        final long eventIgnorePeriod = Math.min(period, Statics.MINIMUM_BAD_STUFF_HAPPENED_IGNORE); // if period is smaller than default minimum, it will be used
                        event.setIgnored(eventIgnorePeriod, currentTime);
                    } else { // event got ignored, standard behavior, nothing to be done
                    }

                    // delayed starting of threads might no longer be necessary
//                    final long realCurrentTime = System.currentTimeMillis();
//                    final long realPeriod = period + currentTime - realCurrentTime + 500L; // 500ms added to account for clock errors
//                    final HashSet<Event> eventsSet = new HashSet<>(2);
//                    eventsSet.add(event);
//
//                    logger.info("ignoreScraper {} toCheckEvent: {} delay: {} launch: findMarkets findSafeRunners", this.eventId, matchedEventId, realPeriod);
//                    Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findMarkets, eventsSet, realPeriod));
//                    Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findSafeRunners, eventsSet, realPeriod));
                } else { // error message is printed in checkEventNMatched, nothing to be done
                    logger.error("no event in map for matchedEventId {} in ScraperEvent.setIgnored for: {}", this.matchedEventId, Generic.objectToString(this));
                }
            } else { // no event attached, nothing to be done
            }
        } else { // ignored was not modified, likely nothing to be done
        }

        return modified;
    }

    public synchronized long getTimeStamp() {
        return timeStamp;
    }

    @SuppressWarnings("FinalMethod")
    public final synchronized int setTimeStamp(long timeStamp) {
        final int modified;
        if (timeStamp > this.timeStamp) {
            this.timeStamp = timeStamp;
            modified = 1;
        } else {
            modified = 0;
        }
        return modified;
    }

    @SuppressWarnings("FinalMethod")
    public final synchronized int timeStamp() {
        final int modified;
        final long currentTime = System.currentTimeMillis();
        if (currentTime > this.timeStamp) {
            this.timeStamp = currentTime;
            modified = 1;
        } else {
            modified = 0;
        }
        return modified;
    }

    public synchronized long getMatchedTimeStamp() {
        return matchedTimeStamp;
    }

    public synchronized int setMatchedTimeStamp(long timeStamp) {
        final int modified;
        if (timeStamp > this.matchedTimeStamp) {
            this.matchedTimeStamp = timeStamp;
            modified = 1;
        } else {
            modified = 0;
        }
        return modified;
    }

    public synchronized int matchedTimeStamp() {
        final int modified;
        final long currentTime = System.currentTimeMillis();
        if (currentTime > this.matchedTimeStamp) {
            this.matchedTimeStamp = currentTime;
            modified = 1;
        } else {
            modified = 0;
        }
        return modified;
    }

    public synchronized String getMatchedEventId() {
        return matchedEventId;
    }

    public synchronized int setMatchedEventId(String matchedEventId) {
        final int modified;
        if (this.matchedEventId == null) {
            if (matchedEventId == null) {
                modified = 0; // values are both null
            } else {
                this.matchedEventId = matchedEventId;
                modified = 1;
            }
        } else if (this.matchedEventId.equals(matchedEventId)) {
            modified = 0; // values are equal
        } else {
            this.setIgnored(shortBlackListPeriod);
            logger.error("REVIEW {} changing matched event from {} to {} for scraperEvent: {}", objectId, this.matchedEventId, matchedEventId, Generic.objectToString(this));
            modified = -10000; // blackListed; this won't pass
        }

        if (modified > 0) {
            matchedTimeStamp();
        }

        return modified;
    }

//    public synchronized int resetMatchedEventId() {
//        final int modified;
//        if (this.matchedEventId == null) {
//            BlackList.ignoreScraper(this, shortBlackListPeriod);
//            logger.error("REVIEW {} trying to reset already null matchedEventId {} for scraperEvent: {}", objectId, this.matchedEventId, Generic.objectToString(this));
//            modified = -10000; // blackListed
//        } else {
//            this.matchedEventId = null;
//            modified = 1;
//        }
//
//        if (modified > 0) {
//            matchedTimeStamp();
//        }
//
//        return modified;
//    }

    public synchronized boolean isPenalties() {
        return this.getMatchStatus() == MatchStatus.PENALTIES;
    }

    public synchronized boolean homeScoreExists() {
        return this.getHomeScore() >= 0;
    }

    public synchronized boolean awayScoreExists() {
        return this.getAwayScore() >= 0;
    }

    public synchronized boolean homeScoreTimeStampExists() {
        return this.getHomeScoreTimeStamp() > 0;
    }

    public synchronized boolean awayScoreTimeStampExists() {
        return this.getAwayScoreTimeStamp() > 0;
    }

    public synchronized boolean timeStampExists() {
        return this.getTimeStamp() > 0;
    }

    public synchronized boolean homeHtScoreExists() {
        return this.getHomeHtScore() >= 0;
    }

    public synchronized boolean awayHtScoreExists() {
        return this.getAwayHtScore() >= 0;
    }

    public synchronized boolean minutesPlayedExists() {
        return this.getMinutesPlayed() >= 0;
    }

    public synchronized boolean stoppageTimeExists() {
        return this.getStoppageTime() >= 0;
    }

    public synchronized boolean onlyOneScoreExists() {
        return (homeScoreExists() && !awayScoreExists()) || (!homeScoreExists() && awayScoreExists());
    }

    public synchronized boolean onlyOneHtScoreExists() {
        return (homeHtScoreExists() && !awayHtScoreExists()) || (!homeHtScoreExists() && awayHtScoreExists());
    }

    public synchronized boolean bothScoreExists() {
        return homeScoreExists() && awayScoreExists();
    }

    public synchronized boolean bothHtScoreExists() {
        return homeHtScoreExists() && awayHtScoreExists();
    }

    public synchronized boolean bothHtScoreExistsAndNotHtIgnored() {
        return bothHtScoreExists() && !isHtIgnored();
    }

    public synchronized boolean anyScoreExists() {
        return homeScoreExists() || awayScoreExists();
    }

    public synchronized boolean anyHtScoreExists() {
        return homeHtScoreExists() || awayHtScoreExists();
    }

    public synchronized boolean minutesExistsAndNot(int minute) {
        return minutesPlayedExists() && minutesNot(minute);
    }

    public synchronized boolean minutesExistsAndNot(int... minutes) {
        return minutesPlayedExists() && minutesNot(minutes);
    }

    public synchronized boolean minutesNot(int minute) {
        return this.getMinutesPlayed() != minute;
    }

    public synchronized boolean minutesNot(int... minutes) {
        boolean returnVar = true;
        if (minutes != null && minutes.length > 0) {
            int length = minutes.length;
            for (int i = 0; i < length && returnVar; i++) {
                returnVar = returnVar && minutesNot(minutes[i]);
            }
        } else { // nothing to be done, will return initial value
        }
        return returnVar;
    }

    public synchronized boolean minutesExistsAndNotInterval(int minuteBegin, int minuteEnd) {
        return minutesPlayedExists() && !minutesInInterval(minuteBegin, minuteEnd);
    }

    public synchronized boolean minutesInInterval(int minuteBegin, int minuteEnd) {
        final int localMinutesPlayed = this.getMinutesPlayed();
        return localMinutesPlayed >= minuteBegin && localMinutesPlayed <= minuteEnd;
    }

    public synchronized boolean minutesOrStoppageExists() {
        return minutesPlayedExists() || stoppageTimeExists();
    }

    public synchronized boolean stoppageExistsOrMinutesAre(int minute) {
        return stoppageTimeExists() || minutesAre(minute);
    }

    public synchronized boolean stoppageExistsOrMinutesAre(int... minutes) {
        return stoppageTimeExists() || minutesAre(minutes);
    }

    public synchronized boolean minutesAre(int minute) {
        return this.getMinutesPlayed() == minute;
    }

    public synchronized boolean minutesAre(int... minutes) {
        boolean returnVar = false;
        if (minutes != null && minutes.length > 0) {
            int length = minutes.length;
            for (int i = 0; i < length && !returnVar; i++) {
                returnVar = returnVar || minutesAre(minutes[i]);
            }
        } else { // nothing to be done, will return initial value
        }
        return returnVar;
    }

    public synchronized int update(ScraperEvent scraperEvent) {
        int modified;
        if (this == scraperEvent) {
            logger.error("{} update from same object in ScraperEvent.update: {}", objectId, Generic.objectToString(this));
            modified = 0;
        } else if (this.getEventId() != scraperEvent.getEventId()) {
            logger.error("{} mismatch eventId in ScraperEvent.update: {} {}", objectId, Generic.objectToString(this), Generic.objectToString(scraperEvent));
            modified = 0;
        } else {
            long thatTimeStamp = scraperEvent.getTimeStamp();
            long thatHomeScoreTimeStamp = scraperEvent.getHomeScoreTimeStamp();
            long thatAwayScoreTimeStamp = scraperEvent.getAwayScoreTimeStamp();

            if (this.timeStamp > thatTimeStamp || this.homeScoreTimeStamp > thatHomeScoreTimeStamp || this.awayScoreTimeStamp > thatAwayScoreTimeStamp) {
                final long currentTime = System.currentTimeMillis();
                if (this.timeStamp > currentTime || this.homeScoreTimeStamp > currentTime || this.awayScoreTimeStamp > currentTime) { // clock jump
                    logger.error("{} scraper clock jump in the past of at least {} {} {} ms detected", objectId, this.timeStamp - currentTime,
                                 this.homeScoreTimeStamp - currentTime, this.awayScoreTimeStamp - currentTime);
                    this.setTimeStamp(currentTime); // to eliminate the timeJump error
                    this.setHomeScoreTimeStamp(currentTime); // to eliminate the timeJump error
                    this.setAwayScoreTimeStamp(currentTime); // to eliminate the timeJump error
                    // won't update the object further, as I have no guarantees on the time ordering
                    modified = 0;
                } else {
                    long maxDifference = Math.max(Math.max(this.homeScoreTimeStamp - thatHomeScoreTimeStamp, this.awayScoreTimeStamp - thatAwayScoreTimeStamp),
                                                  this.timeStamp - thatTimeStamp);
                    if (maxDifference > 1_000_000L) { // huge difference, likely a score was reset and the update time is 0 now
                        this.setIgnored(shortBlackListPeriod, currentTime);
                        logger.error("{} score reset detected in ScraperEvent.update: {} {}", objectId, Generic.objectToString(this), Generic.objectToString(scraperEvent));
                        modified = -10000; // blackListed
                    } else {
                        logger.error("{} attempt to update from older object {} {} {} ScraperEvent.update: {} {}", objectId, this.timeStamp - thatTimeStamp,
                                     this.homeScoreTimeStamp - thatHomeScoreTimeStamp, this.awayScoreTimeStamp - thatAwayScoreTimeStamp, Generic.objectToString(this),
                                     Generic.objectToString(scraperEvent));
                        modified = 0;
                    }
                } // end else
            } else {
                modified = 0; // initialized
                this.setTimeStamp(thatTimeStamp); // this doesn't count as modification

                modified += this.innerUpdate(scraperEvent);

                // this must be placed after the score update, as the scoreTimeStamps are updated there as well, with currentTime
                this.setHomeScoreTimeStamp(thatHomeScoreTimeStamp); // this doesn't count as modification
                this.setAwayScoreTimeStamp(thatAwayScoreTimeStamp); // this doesn't count as modification
            } // end else
        } // end else
        return modified;
    }

    public synchronized int innerUpdate(ScraperEvent scraperEvent) {
        int modified = 0;

        modified += this.setHomeTeam(scraperEvent.getHomeTeam());
        modified += this.setAwayTeam(scraperEvent.getAwayTeam());
        modified += this.setHomeScore(scraperEvent.getHomeScore());
        modified += this.setAwayScore(scraperEvent.getAwayScore());
        modified += this.setHomeHtScore(scraperEvent.getHomeHtScore());
        modified += this.setAwayHtScore(scraperEvent.getAwayHtScore());
        modified += this.setMinutesPlayed(scraperEvent.getMinutesPlayed());
        modified += this.setStoppageTime(scraperEvent.getStoppageTime());

        // updating the matchedEventId won't be done at all, as this can cause issues due to racing condition, even with the current matchStamp implementation
//        final String newMatchedEventId = scraperEvent.getMatchedEventId();
//        if (newMatchedEventId != null) {
//            modified += this.setMatchedEventId(newMatchedEventId);
//        } else { // not allowing setting matchedEventId to null in update
//        }
        modified += this.setHomeRedCards(scraperEvent.getHomeRedCards());
        modified += this.setAwayRedCards(scraperEvent.getAwayRedCards());
        modified += this.setMatchStatus(scraperEvent.getMatchStatus());
        if (!this.isHtIgnored()) {
            modified += this.setHtIgnored(scraperEvent.isHtIgnored());
        }
        modified += this.updateIgnorable(scraperEvent);

        return modified;
    }

    public synchronized long errors() {
        long errors = 0L;
        AtomicLong blackListPeriod = new AtomicLong();

        if (this.getEventId() < 0 || onlyOneScoreExists() || onlyOneHtScoreExists()) {
            errors += 100L;
        }
        if (!timeStampExists()) {
            errors += 200L;
        }
        if (!homeScoreTimeStampExists() && homeScoreExists()) {
            errors += 400L;
        }
        if (!awayScoreTimeStampExists() && awayScoreExists()) {
            errors += 800L;
        }
        if (anyScoreExists() && Math.abs(homeScoreTimeStamp - awayScoreTimeStamp) > 1_000L) {
            errors += 1600L;
            blackListPeriod.set(Math.max(blackListPeriod.get(), Generic.MINUTE_LENGTH_MILLISECONDS));
        }
        if (homeTeam == null || homeTeam.isEmpty()) {
            errors += 3200L;
        }
        if (awayTeam == null || awayTeam.isEmpty()) {
            errors += 6400L;
        }
        if (this.homeScore < this.homeHtScore) {
            errors += 12800L;
        }
        if (this.awayScore < this.awayHtScore) {
            errors += 25600L;
        }
        if (stoppageTimeExists() && minutesNot(45, 90, 105, 120)) {
            errors += 51200L;
            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
        }

        errors += this.innerErrors(blackListPeriod);

        if (blackListPeriod.get() > 0L && errors % 100 == 0 && Generic.isPowerOfTwo(errors / 100)) { // applied for single error only, else standardPeriod
            this.setIgnored(blackListPeriod.get());
        } else if (errors >= 100L) {
            this.setIgnored(standardBlackListPeriod);
        } else { // no blackList, at most minor errors
        }

        return errors;
    }

    public synchronized long innerErrors(AtomicLong blackListPeriod) {
        return 0L; // dummy method, normally overriden
    }

    public synchronized boolean hasStarted() {
        boolean hasStarted;

        if (this.matchStatus == null) {
            // in some cases matchStatus == null can be an acceptable value, when no matchstatus info exists
            hasStarted = errors() < 100L && anyScoreExists();
//            hasStarted = false;
        } else {
            hasStarted = this.matchStatus.hasStarted();

//            switch (this.matchStatus) {
//                case NOT_STARTED:
//                case START_DELAYED:
//                case POSTPONED:
//                case ABANDONED:
//                case CANCELLED:
//                    hasStarted = false;
//                    break;
//                case FIRST_HALF:
//                case HALF_TIME:
//                case SECOND_HALF:
//                case AWAITING_ET:
//                case OVERTIME:
//                case FIRST_ET:
//                case ET_HALF_TIME:
//                case SECOND_ET:
//                case AWAITING_PEN:
//                case PENALTIES:
//                case AFTER_ET:
//                case AFTER_PEN:
//                case ENDED:
//                case INTERRUPTED:
//                    hasStarted = true;
//                    break;
//                case UNKNOWN:
//                    hasStarted = false;
//                    break;
//                default:
//                    logger.error("{} unsupported matchStatus {} in scraperEvent check for: {}", objectId, this.matchStatus, Generic.objectToString(this));
//                    hasStarted = false;
//                    break;
//            } // end switch
        } // end else

        return hasStarted;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(ScraperEvent other) {
        if (other == null) {
            return AFTER;
        }
        if (this == other) {
            return EQUAL;
        }

        if (this.getClass() != other.getClass()) {
            if (this.getClass().hashCode() < other.getClass().hashCode()) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (this.eventId != other.eventId) {
            if (this.eventId < other.eventId) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }

        return EQUAL;
    }

    @Override
    public synchronized int hashCode() {
        int hash = 5;
        hash = 71 * hash + (int) (this.eventId ^ (this.eventId >>> 32));
        return hash;
    }

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
        final ScraperEvent other = (ScraperEvent) obj;
        return this.eventId == other.eventId;
    }
}
