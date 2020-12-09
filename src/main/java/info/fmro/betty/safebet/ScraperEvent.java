package info.fmro.betty.safebet;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.LaunchCommandThread;
import info.fmro.betty.threads.permanent.MaintenanceThread;
import info.fmro.betty.utility.Formulas;
import info.fmro.shared.entities.Event;
import info.fmro.shared.enums.MatchStatus;
import info.fmro.shared.objects.SharedStatics;
import info.fmro.shared.stream.objects.ScraperEventInterface;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.Ignorable;
import info.fmro.shared.utility.LogLevel;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass"})
public class ScraperEvent
        extends Ignorable
        implements ScraperEventInterface, Serializable, Comparable<ScraperEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ScraperEvent.class);
    @Serial
    private static final long serialVersionUID = 5045581708147478634L;
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    public static final long standardBlackListPeriod = Generic.MINUTE_LENGTH_MILLISECONDS * 5L, shortBlackListPeriod = Generic.MINUTE_LENGTH_MILLISECONDS << 1, minimalBlackListPeriod = 10_000L;
    public final String objectId;
    private final long eventId;
    @SuppressWarnings("RedundantFieldInitialization")
    private long timeStamp = 0L;
    private long homeScoreTimeStamp, awayScoreTimeStamp, matchedTimeStamp; // score stamps for last time a valid & >=0 score was seen
    private String homeTeam, awayTeam, matchedEventId;
    private int homeScore = -1, awayScore = -1, homeHtScore = -1, awayHtScore = -1, minutesPlayed = -1, stoppageTime = -1, homeRedCards = -1, awayRedCards = -1;
    private MatchStatus matchStatus;
    private boolean htIgnored;

    public ScraperEvent(final String objectId, final long eventId) {
        super();
        this.objectId = objectId;
        this.eventId = eventId;
        this.timeStamp();
    }

    ScraperEvent(final String objectId, final long eventId, final long timeStamp) {
        super();
        this.objectId = objectId;
        this.eventId = eventId;
        this.setTimeStamp(timeStamp);
    }

    public synchronized String getObjectId() {
        return this.objectId;
    }

    public synchronized long getEventId() {
        return this.eventId;
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
    synchronized boolean isHtIgnored() {
        return this.htIgnored;
    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized int ignoreHt() {
        return setHtIgnored(true);
    }

    public synchronized int setHtIgnored(final boolean newHtIgnored) {
        final int modified;
        if (this.htIgnored == newHtIgnored) {
            modified = 0;
        } else if (!this.htIgnored) {
            this.htIgnored = newHtIgnored;
            modified = 1;
        } else { // this.htIgnored true and htIgnored false
            logger.error("{} attempted to reset htIgnored in setHtIgnored for: {}", this.objectId, Generic.objectToString(this));
            modified = 0;
        }

        return modified;
    }

    public synchronized int resetHtIgnored() {
        final int modified = this.htIgnored ? 1 : 0;
        this.htIgnored = false;

        return modified;
    }

    public synchronized String getHomeTeam() {
        return this.homeTeam;
    }

    public synchronized int setHomeTeam(final String newHomeTeam) {
        final int modified;
        if (this.homeTeam == null) {
            if (newHomeTeam == null) {
                modified = 0;
            } else {
                this.homeTeam = newHomeTeam;
                modified = 1;
            }
        } else if (this.homeTeam.equals(newHomeTeam)) {
            modified = 0;
        } else {
            if (Formulas.matchTeams(this.homeTeam, newHomeTeam) > Statics.highThreshold) {
                this.setIgnored(shortBlackListPeriod);
//                modified = -10000; // blackListed
            } else {
                this.setIgnored(standardBlackListPeriod);
//                modified = -10000; // blackListed
            }
            logger.error("{} change homeTeam {} to {} in scraperEvent: {}", this.objectId, this.homeTeam, newHomeTeam, Generic.objectToString(this));

            this.homeTeam = newHomeTeam; // modification goes ahead
            modified = 1;
        }
        return modified;
    }

    public synchronized String getAwayTeam() {
        return this.awayTeam;
    }

    public synchronized int setAwayTeam(final String newAwayTeam) {
        final int modified;
        if (this.awayTeam == null) {
            if (newAwayTeam == null) {
                modified = 0;
            } else {
                this.awayTeam = newAwayTeam;
                modified = 1;
            }
        } else if (this.awayTeam.equals(newAwayTeam)) {
            modified = 0;
        } else {
            if (Formulas.matchTeams(this.awayTeam, newAwayTeam) > Statics.highThreshold) {
                this.setIgnored(shortBlackListPeriod);
//                modified = -10000; // blackListed
            } else {
                this.setIgnored(standardBlackListPeriod);
//                modified = -10000; // blackListed
            }
            logger.error("{} change awayTeam {} to {} in scraperEvent: {}", this.objectId, this.awayTeam, newAwayTeam, Generic.objectToString(this));

            this.awayTeam = newAwayTeam; // modification goes ahead
            modified = 1;
        }
        return modified;
    }

    public synchronized int getHomeRedCards() {
        return this.homeRedCards;
    }

    synchronized int setHomeRedCards(final int newHomeRedCards) {
        final int modified;
        if (this.homeRedCards == newHomeRedCards) {
            modified = 0;
        } else if (this.homeRedCards < newHomeRedCards) {
            this.homeRedCards = newHomeRedCards;
            modified = 1;
        } else {
            this.setIgnored(standardBlackListPeriod);
            logger.error("{} change homeRedCards {} to {} in scraperEvent: {}", this.objectId, this.homeRedCards, newHomeRedCards, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.homeRedCards = newHomeRedCards; // modification goes ahead
            modified = 1;
        }
        return modified;
    }

    public synchronized int getAwayRedCards() {
        return this.awayRedCards;
    }

    synchronized int setAwayRedCards(final int newAwayRedCards) {
        final int modified;
        if (this.awayRedCards == newAwayRedCards) {
            modified = 0;
        } else if (this.awayRedCards < newAwayRedCards) {
            this.awayRedCards = newAwayRedCards;
            modified = 1;
        } else {
            this.setIgnored(standardBlackListPeriod);
            logger.error("{} change awayRedCards {} to {} in scraperEvent: {}", this.objectId, this.awayRedCards, newAwayRedCards, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.awayRedCards = newAwayRedCards; // modification goes ahead
            modified = 1;
        }
        return modified;
    }

    public synchronized int getHomeScore() {
        return this.homeScore;
    }

    public synchronized int setHomeScore(final int newHomeScore) {
        return setHomeScore(newHomeScore, isPenalties());
    }

    private synchronized int setHomeScore(final int newHomeScore, final boolean isPenalties) {
        final int modified;
        final long currentTime = System.currentTimeMillis();
        final long timeLapsed = currentTime - this.getHomeScoreTimeStamp();

        if (this.homeScore == newHomeScore) {
            if (newHomeScore >= 0) {
                this.setHomeScoreTimeStamp(currentTime);
            } else { // default score, won't stamp
            }
            modified = 0;
        } else if (newHomeScore == -1) {
            logger.error("{} attempted to reset homeScore {} in scraperEvent: {}", this.objectId, this.homeScore, Generic.objectToString(this));
            modified = 0; // ignored reset
        } else {
            final int addedGoals = newHomeScore - this.homeScore;
            if (addedGoals > 0) {
                if (addedGoals > 1) {
                    if (timeLapsed < 15_000L * addedGoals && !isPenalties) {
                        // more than 1 goal per 15 seconds; this error does happen fairly often, as scores are sometimes not updated immediately
                        this.setIgnored(standardBlackListPeriod, currentTime);
                        logger.error("{} change homeScore {} to {} after {} ms in scraperEvent: {}", this.objectId, this.homeScore, newHomeScore, timeLapsed, Generic.objectToString(this));
//                        modified = -10000; // blackListed

                    }
                } else { // addedGoals == 1
                }
                this.homeScore = newHomeScore;
                modified = 1;
            } else if (newHomeScore >= 0 && addedGoals == -1) { // score taken back by 1 goal; happens rarely due to scrapeSite score errors
                this.setIgnored(shortBlackListPeriod, currentTime);
                logger.error("REVIEW {} homeScore {} reduced to {} in scraperEvent: {}", this.objectId, this.homeScore, newHomeScore, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.homeScore = newHomeScore;
                modified = 1;
            } else {
                this.setIgnored(standardBlackListPeriod, currentTime);
                logger.error("{} change homeScore {} to {} in scraperEvent: {}", this.objectId, this.homeScore, newHomeScore, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.homeScore = newHomeScore;
                modified = 1;
            }
        } // end else

        if (modified > 0) {
            this.setHomeScoreTimeStamp(currentTime);
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized long getHomeScoreTimeStamp() {
        return this.homeScoreTimeStamp;
    }

    public synchronized void setHomeScoreTimeStamp(final long homeScoreTimeStamp) {
        this.homeScoreTimeStamp = homeScoreTimeStamp;
    }

    public synchronized int getAwayScore() {
        return this.awayScore;
    }

    public synchronized int setAwayScore(final int newAwayScore) {
        return setAwayScore(newAwayScore, isPenalties());
    }

    private synchronized int setAwayScore(final int newAwayScore, final boolean isPenalties) {
        final int modified;
        final long currentTime = System.currentTimeMillis();
        final long timeLapsed = currentTime - this.getAwayScoreTimeStamp();

        if (this.awayScore == newAwayScore) {
            if (newAwayScore >= 0) {
                this.setAwayScoreTimeStamp(currentTime);
            } else { // default score, won't stamp
            }
            modified = 0;
        } else if (newAwayScore == -1) {
            logger.error("{} attempted to reset awayScore {} in scraperEvent: {}", this.objectId, this.awayScore, Generic.objectToString(this));
            modified = 0; // ignored reset
        } else {
            final int addedGoals = newAwayScore - this.awayScore;
            if (addedGoals > 0) {
                if (addedGoals > 1) {
                    if (timeLapsed < 15_000L * addedGoals && !isPenalties) {
                        // more than 1 goal per 15 seconds; this error does happen fairly often, as scores are sometimes not updated immediately
                        this.setIgnored(standardBlackListPeriod, currentTime);
                        logger.error("{} change awayScore {} to {} after {} ms in scraperEvent: {}", this.objectId, this.awayScore, newAwayScore, timeLapsed, Generic.objectToString(this));
//                        modified = -10000; // blackListed

                    }
                } else { // addedGoals == 1
                }
                this.awayScore = newAwayScore;
                modified = 1;
            } else if (newAwayScore >= 0 && addedGoals == -1) { // score taken back by 1 goal; happens rarely due to scrapeSite score errors
                this.setIgnored(shortBlackListPeriod, currentTime);
                logger.error("REVIEW {} awayScore {} reduced to {} in scraperEvent: {}", this.objectId, this.awayScore, newAwayScore, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.awayScore = newAwayScore;
                modified = 1;
            } else {
                this.setIgnored(standardBlackListPeriod, currentTime);
                logger.error("{} attempted to change awayScore {} to {} in scraperEvent: {}", this.objectId, this.awayScore, newAwayScore, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.awayScore = newAwayScore;
                modified = 1;
            }
        } // end else

        if (modified > 0) {
            this.setAwayScoreTimeStamp(currentTime);
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized long getAwayScoreTimeStamp() {
        return this.awayScoreTimeStamp;
    }

    public synchronized void setAwayScoreTimeStamp(final long awayScoreTimeStamp) {
        this.awayScoreTimeStamp = awayScoreTimeStamp;
    }

    public synchronized int getHomeHtScore() {
        return isHtIgnored() ? -1 : this.homeHtScore;
    }

    public synchronized int setHomeHtScore(final int newHomeHtScore) {
        return setHomeHtScore(newHomeHtScore, false);
    }

    synchronized int setHomeHtScore(final int newHomeHtScore, @SuppressWarnings("unused") final boolean allowedReset) {
        final int modified;
        if (this.homeHtScore == newHomeHtScore) {
            modified = 0;
        } else if (this.homeHtScore == -1) {
            this.homeHtScore = newHomeHtScore;
            modified = 1;
        } else if (newHomeHtScore == -1) {
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
            logger.error("{} change homeHtScore {} to {} in scraperEvent, will ignoredHt: {}", this.objectId, this.homeHtScore, newHomeHtScore, Generic.objectToString(this));
            this.homeHtScore = newHomeHtScore; // after logging the error message
            modified = 1;
//            modified = -10000; // blackListed
        }
        return modified;
    }

    public synchronized int getAwayHtScore() {
        return isHtIgnored() ? -1 : this.awayHtScore;
    }

    public synchronized int setAwayHtScore(final int newAwayHtScore) {
        return setAwayHtScore(newAwayHtScore, false);
    }

    synchronized int setAwayHtScore(final int newAwayHtScore, @SuppressWarnings("unused") final boolean allowedReset) {
        final int modified;
        if (this.awayHtScore == newAwayHtScore) {
            modified = 0;
        } else if (this.awayHtScore == -1) {
            this.awayHtScore = newAwayHtScore;
            modified = 1;
        } else if (newAwayHtScore == -1) {
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
            logger.error("{} change awayHtScore {} to {} in scraperEvent, will ignoredHt: {}", this.objectId, this.awayHtScore, newAwayHtScore, Generic.objectToString(this));
            this.awayHtScore = newAwayHtScore; // after logging the error message
            modified = 1;
//            modified = -10000; // blackListed
        }
        return modified;
    }

    public synchronized MatchStatus getMatchStatus() {
        return this.matchStatus;
    }

    public synchronized int setMatchStatus(final MatchStatus newMatchStatus) {
        return setMatchStatus(newMatchStatus, false);
    }

    synchronized int setMatchStatus(final MatchStatus newMatchStatus, final boolean resetToNullIgnored) {
        final int modified;
        if (this.matchStatus == null) {
            if (newMatchStatus == null) {
                modified = 0;
            } else {
                this.matchStatus = newMatchStatus;
                modified = 1;
            }
        } else if (this.matchStatus == newMatchStatus) {
            modified = 0;
        } else if (newMatchStatus == null) {
            if (resetToNullIgnored) { // this seems to happen in case of coral just before the match disappears, but not only then
                final String printedString = MessageFormatter.arrayFormat("{} reset attempt matchStatus to {} from {} for: {} {}/{}", new Object[]{this.objectId, newMatchStatus, this.matchStatus, this.eventId, this.homeTeam, this.awayTeam}).getMessage();

                if ("coral".equals(this.objectId)) {
                    SharedStatics.alreadyPrintedMap.logOnce(Statics.debugLevel.check(2, 210), Generic.MINUTE_LENGTH_MILLISECONDS * 5L, logger, LogLevel.INFO, printedString);
                } else {
                    logger.error(printedString);
                }
//                logger.info("{} reset attempt matchStatus to {} from {} for: {} {}/{}", objectId, matchStatus, this.matchStatus, this.eventId, this.homeTeam, this.awayTeam);
//                this.matchStatus = matchStatus;
                modified = 0;
            } else {
                this.setIgnored(shortBlackListPeriod);
                logger.error("{} reset matchStatus {} to {} in scraperEvent: {}", this.objectId, this.matchStatus, newMatchStatus, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.matchStatus = newMatchStatus;
                modified = 1;
            }
        } else if (newMatchStatus == MatchStatus.NOT_STARTED || newMatchStatus == MatchStatus.POSTPONED) {
            this.setIgnored(shortBlackListPeriod);
            logger.error("{} reset matchStatus {} to {} in scraperEvent: {}", this.objectId, this.matchStatus, newMatchStatus, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.matchStatus = newMatchStatus;
            modified = 1;
        } else if (this.matchStatus.ordinal() < MatchStatus.FIRST_HALF.ordinal() || this.matchStatus.ordinal() > MatchStatus.ENDED.ordinal() ||
                   this.matchStatus.ordinal() < newMatchStatus.ordinal()) {
            this.matchStatus = newMatchStatus;
            modified = 1;
        } else if (this.matchStatus.ordinal() - newMatchStatus.ordinal() == 1) {
            this.setIgnored(shortBlackListPeriod);
            logger.error("REVIEW {} matchStatus {} reduced to {} in scraperEvent: {}", this.objectId, this.matchStatus, newMatchStatus, Generic.objectToString(this));
//            this.matchStatus = matchStatus; // matchStatus taken back by 1 setting; does happen rarely
//            modified = 1;
//            modified = -10000; // blackListed

            this.matchStatus = newMatchStatus;
            modified = 1;
        } else {
            this.setIgnored(standardBlackListPeriod);
            logger.error("{} change matchStatus {} to {} in scraperEvent: {}", this.objectId, this.matchStatus, newMatchStatus, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.matchStatus = newMatchStatus;
            modified = 1;
        }

        return modified;
    }

    public synchronized int getMinutesPlayed() {
        return this.minutesPlayed;
    }

    synchronized int setMinutesPlayedUnckeched(final int newMinutesPlayed) {
        final int modified;
        if (this.minutesPlayed == newMinutesPlayed) {
            modified = 0;
        } else {
            this.minutesPlayed = newMinutesPlayed;
            modified = 1;
        }
        return modified;
    }

    public synchronized int setMinutesPlayed(final int newMinutesPlayed) {
        final int modified;
        if (this.minutesPlayed == newMinutesPlayed) {
            modified = 0;
        } else if (this.minutesPlayed < newMinutesPlayed || newMinutesPlayed == -1) { // minutesPlayed can reset when match is over
            this.minutesPlayed = newMinutesPlayed;
            modified = 1;
        } else if (this.minutesPlayed <= 1) { // this.minutesPlayed > minutesPlayed && minutesPlayed != -1
            this.setIgnored(minimalBlackListPeriod);
            logger.error("{} outOfRange change minutesPlayed {} to {} in scraperEvent: {}", this.objectId, this.minutesPlayed, newMinutesPlayed, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.minutesPlayed = newMinutesPlayed;
            modified = 1;
        } else if (newMinutesPlayed > 0 && this.minutesPlayed - newMinutesPlayed == 1) {
            this.minutesPlayed = newMinutesPlayed; // minutesPlayed taken back by 1; happens sometimes
            modified = 1;
        } else if ((this.minutesPlayed < 45 && newMinutesPlayed >= 1) || (this.minutesPlayed > 46 && this.minutesPlayed < 90 && newMinutesPlayed >= 46) ||
                   (this.minutesPlayed > 91 && this.minutesPlayed < 105 && newMinutesPlayed >= 91) || (this.minutesPlayed > 106 && this.minutesPlayed < 120 && newMinutesPlayed >= 106)) { // this.minutesPlayed > 1
            logger.warn("REVIEW {} minutesPlayed {} reduced to {} in scraperEvent: {}", this.objectId, this.minutesPlayed, newMinutesPlayed, Generic.objectToString(this));
            this.minutesPlayed = newMinutesPlayed; // minutesPlayed taken back by larger amount, but in the same period; happens sometimes
            modified = 1;
        } else if ((this.minutesPlayed == 45 && newMinutesPlayed >= 1) || (this.minutesPlayed == 90 && newMinutesPlayed >= 46) || (this.minutesPlayed == 105 && newMinutesPlayed >= 91) || (this.minutesPlayed == 120 && newMinutesPlayed >= 106)) {
            this.setIgnored(minimalBlackListPeriod);
            logger.error("{} change breakLike minutesPlayed {} to {} in scraperEvent: {}", this.objectId, this.minutesPlayed, newMinutesPlayed, Generic.objectToString(this));
            // this.minutesPlayed = minutesPlayed; // minutesPlayed taken back by larger amount, but in the same period or during break
            // modified = 1;
//            modified = -10000; // blackListed

            this.minutesPlayed = newMinutesPlayed;
            modified = 1;
        } else {
            this.setIgnored(minimalBlackListPeriod);
            logger.error("{} change differentPeriod minutesPlayed {} to {} in scraperEvent: {}", this.objectId, this.minutesPlayed, newMinutesPlayed, Generic.objectToString(this));
//            modified = -10000; // blackListed

            this.minutesPlayed = newMinutesPlayed;
            modified = 1;
        }
        return modified;
    }

    public synchronized int getStoppageTime() {
        return this.stoppageTime;
    }

    synchronized int setStoppageTime(final int newStoppageTime) {
        final int modified;
        if (this.stoppageTime == newStoppageTime) {
            modified = 0;
        } else if (this.stoppageTime < newStoppageTime || newStoppageTime == -1) { // stoppageTime will reset when periods or match are over
            this.stoppageTime = newStoppageTime;
            modified = 1;
        } else if (newStoppageTime > 0 && this.stoppageTime - newStoppageTime == 1) {
            this.stoppageTime = newStoppageTime; // stoppageTime taken back by 1; might happen sometimes
            modified = 1;
        } else {
//            BlackList.ignoreScraper(this, minimalBlackListPeriod);
//            modified = -10000; // blackListed
            logger.error("REVIEW {} stoppageTime {} reduced to {} in scraperEvent: {}", this.objectId, this.stoppageTime, newStoppageTime, Generic.objectToString(this));
            this.stoppageTime = newStoppageTime; // stoppageTime taken back by larger amount; happens sometimes
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

    public synchronized boolean isTooOldForMatching(final long currentTime) {
        final long timeSinceUpdate = currentTime - this.getTimeStamp();
        return timeSinceUpdate > Generic.HOUR_LENGTH_MILLISECONDS;
    }

    public synchronized int setIgnored(final long period) {
        final long currentTime = System.currentTimeMillis();
        return setIgnored(period, currentTime);
    }

    protected synchronized int setIgnored(final long period, final long startTime) {
        final int modified = super.setIgnored(period, startTime);
        if (modified > 0) {
            if (this.matchedEventId != null) {
                final Event event = Statics.eventsMap.get(this.matchedEventId);
                if (event != null) {
                    event.ignoredScrapersCheck(MaintenanceThread.removeFromSecondaryMapsMethod, LaunchCommandThread.constructorLinkedHashSetLongMarket, Statics.safeBetModuleActivated, Statics.MIN_MATCHED, Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD,
                                               Statics.marketCataloguesMap, Formulas.getScraperEventsMapMethod, Formulas.getIgnorableMapMethod, LaunchCommandThread.constructorHashSetLongEvent);
                    if (event.isIgnored()) { // event got ignored, standard behavior, nothing to be done
                    } else {
                        logger.info("event still not ignored after ScraperEvent ignored: {} {} {}", this.eventId, event.getId(),
                                    event.getNValidScraperEventIds(MaintenanceThread.removeFromSecondaryMapsMethod, LaunchCommandThread.constructorLinkedHashSetLongMarket, Statics.safeBetModuleActivated, Statics.MIN_MATCHED,
                                                                   Statics.DEFAULT_REMOVE_OR_BAN_SAFETY_PERIOD, Statics.marketCataloguesMap, Formulas.getScraperEventsMapMethod, Formulas.getIgnorableMapMethod,
                                                                   LaunchCommandThread.constructorHashSetLongEvent));
                        final long eventIgnorePeriod = Math.min(period, Statics.MINIMUM_BAD_STUFF_HAPPENED_IGNORE); // if period is smaller than default minimum, it will be used
                        event.setIgnored(eventIgnorePeriod, startTime, MaintenanceThread.removeFromSecondaryMapsMethod, LaunchCommandThread.constructorLinkedHashSetLongMarket, Statics.safeBetModuleActivated, Statics.marketCataloguesMap,
                                         LaunchCommandThread.constructorHashSetLongEvent);
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
        return this.timeStamp;
    }

    public final synchronized int setTimeStamp(final long newTimeStamp) {
        final int modified;
        if (newTimeStamp > this.timeStamp) {
            this.timeStamp = newTimeStamp;
            modified = 1;
        } else {
            modified = 0;
        }
        return modified;
    }

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
        return this.matchedTimeStamp;
    }

    public synchronized int setMatchedTimeStamp(final long newTimeStamp) {
        final int modified;
        if (newTimeStamp > this.matchedTimeStamp) {
            this.matchedTimeStamp = newTimeStamp;
            modified = 1;
        } else {
            modified = 0;
        }
        return modified;
    }

    @SuppressWarnings("UnusedReturnValue")
    private synchronized int matchedTimeStamp() {
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
        return this.matchedEventId;
    }

    public synchronized int setMatchedEventId(final String newMatchedEventId) {
        final int modified;
        if (this.matchedEventId == null) {
            if (newMatchedEventId == null) {
                modified = 0; // values are both null
            } else {
                this.matchedEventId = newMatchedEventId;
                modified = 1;
            }
        } else if (this.matchedEventId.equals(newMatchedEventId)) {
            modified = 0; // values are equal
        } else {
            this.setIgnored(shortBlackListPeriod);
            logger.error("REVIEW {} changing matched event from {} to {} for scraperEvent: {}", this.objectId, this.matchedEventId, newMatchedEventId, Generic.objectToString(this));
            modified = -10_000; // blackListed; this won't pass
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

    private synchronized boolean isPenalties() {
        return this.getMatchStatus() == MatchStatus.PENALTIES;
    }

    private synchronized boolean homeScoreExists() {
        return this.getHomeScore() >= 0;
    }

    private synchronized boolean awayScoreExists() {
        return this.getAwayScore() >= 0;
    }

    @Contract(pure = true)
    private synchronized boolean homeScoreTimeStampExists() {
        return this.getHomeScoreTimeStamp() > 0;
    }

    @Contract(pure = true)
    private synchronized boolean awayScoreTimeStampExists() {
        return this.getAwayScoreTimeStamp() > 0;
    }

    private synchronized boolean timeStampExists() {
        return this.getTimeStamp() > 0;
    }

    private synchronized boolean homeHtScoreExists() {
        return this.getHomeHtScore() >= 0;
    }

    private synchronized boolean awayHtScoreExists() {
        return this.getAwayHtScore() >= 0;
    }

    synchronized boolean minutesPlayedExists() {
        return this.getMinutesPlayed() >= 0;
    }

    synchronized boolean stoppageTimeExists() {
        return this.getStoppageTime() >= 0;
    }

    private synchronized boolean onlyOneScoreExists() {
        return (homeScoreExists() && !awayScoreExists()) || (!homeScoreExists() && awayScoreExists());
    }

    private synchronized boolean onlyOneHtScoreExists() {
        return (homeHtScoreExists() && !awayHtScoreExists()) || (!homeHtScoreExists() && awayHtScoreExists());
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    synchronized boolean bothScoreExists() {
        return homeScoreExists() && awayScoreExists();
    }

    synchronized boolean bothHtScoreExists() {
        return homeHtScoreExists() && awayHtScoreExists();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    synchronized boolean bothHtScoreExistsAndNotHtIgnored() {
        return bothHtScoreExists() && !isHtIgnored();
    }

    synchronized boolean anyScoreExists() {
        return homeScoreExists() || awayScoreExists();
    }

    synchronized boolean anyHtScoreExists() {
        return homeHtScoreExists() || awayHtScoreExists();
    }

    synchronized boolean minutesExistsAndNot(final int minute) {
        return minutesPlayedExists() && minutesNot(minute);
    }

    synchronized boolean minutesExistsAndNot(final int... minutes) {
        return minutesPlayedExists() && minutesNot(minutes);
    }

    private synchronized boolean minutesNot(final int minute) {
        return this.getMinutesPlayed() != minute;
    }

    private synchronized boolean minutesNot(final int... minutes) {
        boolean returnVar = true;
        if (minutes != null && minutes.length > 0) {
            final int length = minutes.length;
            for (int i = 0; i < length && returnVar; i++) { // returnVar == true
                returnVar = minutesNot(minutes[i]);
            }
        } else { // nothing to be done, will return initial value
        }
        return returnVar;
    }

    synchronized boolean minutesExistsAndNotInterval(final int minuteBegin, final int minuteEnd) {
        return minutesPlayedExists() && !minutesInInterval(minuteBegin, minuteEnd);
    }

    private synchronized boolean minutesInInterval(final int minuteBegin, final int minuteEnd) {
        final int localMinutesPlayed = this.getMinutesPlayed();
        return localMinutesPlayed >= minuteBegin && localMinutesPlayed <= minuteEnd;
    }

    synchronized boolean minutesOrStoppageExists() {
        return minutesPlayedExists() || stoppageTimeExists();
    }

    synchronized boolean stoppageExistsOrMinutesAre(final int minute) {
        return stoppageTimeExists() || minutesAre(minute);
    }

    synchronized boolean stoppageExistsOrMinutesAre(final int... minutes) {
        return stoppageTimeExists() || minutesAre(minutes);
    }

    synchronized boolean minutesAre(final int minute) {
        return this.getMinutesPlayed() == minute;
    }

    private synchronized boolean minutesAre(final int... minutes) {
        boolean returnVar = false;
        if (minutes != null && minutes.length > 0) {
            final int length = minutes.length;
            for (int i = 0; i < length && !returnVar; i++) { // returnVar == false
                returnVar = minutesAre(minutes[i]);
            }
        } else { // nothing to be done, will return initial value
        }
        return returnVar;
    }

    public synchronized int update(@NotNull final ScraperEvent scraperEvent) {
        int modified;
        if (this == scraperEvent) {
            logger.error("{} update from same object in ScraperEvent.update: {}", this.objectId, Generic.objectToString(this));
            modified = 0;
        } else if (this.getEventId() != scraperEvent.getEventId()) {
            logger.error("{} mismatch eventId in ScraperEvent.update: {} {}", this.objectId, Generic.objectToString(this), Generic.objectToString(scraperEvent));
            modified = 0;
        } else {
            final long thatTimeStamp = scraperEvent.getTimeStamp();
            final long thatHomeScoreTimeStamp = scraperEvent.getHomeScoreTimeStamp();
            final long thatAwayScoreTimeStamp = scraperEvent.getAwayScoreTimeStamp();

            if (this.timeStamp > thatTimeStamp || this.homeScoreTimeStamp > thatHomeScoreTimeStamp || this.awayScoreTimeStamp > thatAwayScoreTimeStamp) {
                final long currentTime = System.currentTimeMillis();
                if (this.timeStamp > currentTime || this.homeScoreTimeStamp > currentTime || this.awayScoreTimeStamp > currentTime) { // clock jump
                    logger.error("{} scraper clock jump in the past of at least {} {} {} ms detected", this.objectId, this.timeStamp - currentTime, this.homeScoreTimeStamp - currentTime, this.awayScoreTimeStamp - currentTime);
                    this.setTimeStamp(currentTime); // to eliminate the timeJump error
                    this.setHomeScoreTimeStamp(currentTime); // to eliminate the timeJump error
                    this.setAwayScoreTimeStamp(currentTime); // to eliminate the timeJump error
                    // won't update the object further, as I have no guarantees on the time ordering
                    modified = 0;
                } else {
                    final long maxDifference = Math.max(Math.max(this.homeScoreTimeStamp - thatHomeScoreTimeStamp, this.awayScoreTimeStamp - thatAwayScoreTimeStamp), this.timeStamp - thatTimeStamp);
                    if (maxDifference > 1_000_000L) { // huge difference, likely a score was reset and the update time is 0 now
                        this.setIgnored(shortBlackListPeriod, currentTime);
                        logger.error("{} score reset detected in ScraperEvent.update: {} {}", this.objectId, Generic.objectToString(this), Generic.objectToString(scraperEvent));
                        modified = -10000; // blackListed
                    } else {
                        logger.error("{} attempt to update from older object {} {} {} ScraperEvent.update: {} {}", this.objectId, this.timeStamp - thatTimeStamp, this.homeScoreTimeStamp - thatHomeScoreTimeStamp,
                                     this.awayScoreTimeStamp - thatAwayScoreTimeStamp, Generic.objectToString(this), Generic.objectToString(scraperEvent));
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

    synchronized int innerUpdate(@NotNull final ScraperEvent scraperEvent) {
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
        final AtomicLong blackListPeriod = new AtomicLong();

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
        if (anyScoreExists() && Math.abs(this.homeScoreTimeStamp - this.awayScoreTimeStamp) > 1_000L) {
            errors += 1_600L;
            blackListPeriod.set(Math.max(blackListPeriod.get(), Generic.MINUTE_LENGTH_MILLISECONDS));
        }
        if (this.homeTeam == null || this.homeTeam.isEmpty()) {
            errors += 3_200L;
        }
        if (this.awayTeam == null || this.awayTeam.isEmpty()) {
            errors += 6_400L;
        }
        if (this.homeScore < this.homeHtScore) {
            errors += 12_800L;
        }
        if (this.awayScore < this.awayHtScore) {
            errors += 25_600L;
        }
        if (stoppageTimeExists() && minutesNot(45, 90, 105, 120)) {
            errors += 51_200L;
            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
        }

        errors += this.innerErrors(blackListPeriod);

        if (blackListPeriod.get() > 0L && errors % 100L == 0L && Generic.isPowerOfTwo(errors / 100L)) { // applied for single error only, else standardPeriod
            this.setIgnored(blackListPeriod.get());
        } else if (errors >= 100L) {
            this.setIgnored(standardBlackListPeriod);
        } else { // no blackList, at most minor errors
        }

        return errors;
    }

    synchronized long innerErrors(final AtomicLong blackListPeriod) {
        return 0L; // dummy method, normally overriden
    }

    public synchronized boolean hasStarted() {
        final boolean hasStarted;
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
        // end else
        // in some cases matchStatus == null can be an acceptable value, when no matchstatus info exists
        hasStarted = this.matchStatus == null ? errors() < 100L && anyScoreExists() : this.matchStatus.hasStarted();

        return hasStarted;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public int compareTo(@NotNull final ScraperEvent o) {
        //noinspection ConstantConditions
        if (o == null) {
            return AFTER;
        }
        if (this == o) {
            return EQUAL;
        }

        if (this.getClass() != o.getClass()) {
            return this.getClass().hashCode() < o.getClass().hashCode() ? BEFORE : AFTER;
        }
        if (this.eventId != o.eventId) {
            return this.eventId < o.eventId ? BEFORE : AFTER;
        }

        return EQUAL;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final ScraperEvent that = (ScraperEvent) obj;
        return this.eventId == that.eventId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.eventId);
    }
}
