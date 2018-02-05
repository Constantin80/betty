package info.fmro.betty.objects;

import info.fmro.betty.enums.MatchStatus;
import info.fmro.shared.utility.Generic;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetradarEvent
        extends ScraperEvent
        implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(BetradarEvent.class);
    private static final long serialVersionUID = -6590032889145745147L;
    private Date startTime;
    private String classModifiers;
    // private ScraperMatchFacts matchFacts; // removed support for now; might be added later

//    public BetradarEvent(long eventId) {
//        super("betradar", eventId);
//    }
    public BetradarEvent(long eventId, long timeStamp) {
        super("betradar", eventId, timeStamp);
    }

    public synchronized Date getStartTime() {
        return startTime == null ? null : (Date) startTime.clone();
    }

    public synchronized int setStartTime(Date startTime) {
        final int modified;
        if (startTime == null) {
            if (this.startTime == null) {
                modified = 0;
            } else {
                BlackList.ignoreScraper(this, minimalBlackListPeriod); // avoid filling logs
                logger.error("reset startTime {} to null in betradarEvent: {}", this.startTime, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.startTime = (Date) startTime.clone();
                modified = 1;
            }
        } else if (this.startTime == null) {
            this.startTime = (Date) startTime.clone();
            modified = 1;
        } else if (this.startTime.equals(startTime)) {
            modified = 0;
        } else {
            final long timeDifference = Math.abs(this.startTime.getTime() - startTime.getTime());
            if (timeDifference == Generic.HOUR_LENGTH_MILLISECONDS * 2L) { // 2 hour difference happens, likely due to some server related issue(happens in regular browser too)
                this.startTime = (Date) startTime.clone();
                modified = 1;
            } else if (timeDifference == Generic.DAY_LENGTH_MILLISECONDS || timeDifference == Generic.HOUR_LENGTH_MILLISECONDS * 22L) { // happens sometimes, when the day changes
                logger.warn("changing startTime {} to {} in betradarEvent: {}/{}", this.startTime, startTime, this.getHomeTeam(), this.getAwayTeam());
                this.startTime = (Date) startTime.clone();
                modified = 1;
            } else {
                BlackList.ignoreScraper(this, minimalBlackListPeriod); // avoid filling logs
                logger.error("change startTime {} to {} in betradarEvent: {}", this.startTime, startTime, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.startTime = (Date) startTime.clone();
                modified = 1;
            }
        }

        // this.startTime = startTime == null ? null : (Date) startTime.clone();
        return modified;
    }

    public synchronized String getClassModifiers() {
        return classModifiers;
    }

    public synchronized int setClassModifiers(String classModifiers) {
        final int modified;
        if (classModifiers == null) {
            if (this.classModifiers == null) {
                modified = 0;
            } else {
                BlackList.ignoreScraper(this, minimalBlackListPeriod); // avoid filling logs
                logger.error("reset classModifiers {} to null in betradarEvent: {}", this.classModifiers, Generic.objectToString(this));
//                modified = -10000; // blackListed

                this.classModifiers = classModifiers;
                modified = 1;
            }
        } else if (classModifiers.equals(this.classModifiers)) {
            modified = 0;
        } else {
            this.classModifiers = classModifiers;
            modified = 1;
        }

        //this.classModifiers = new String(classModifiers);
        return modified;
    }

    @Override
    public synchronized int setHomeHtScore(int homeHtScore) {
        return super.setHomeHtScore(homeHtScore, true);
    }

    @Override
    public synchronized int setAwayHtScore(int awayHtScore) {
        return super.setAwayHtScore(awayHtScore, true);
    }

    // public synchronized ScraperMatchFacts getMatchFacts() {
    //     return matchFacts;
    // }
    // public synchronized void setMatchFacts(ScraperMatchFacts matchFacts) {
    //     this.matchFacts = matchFacts;
    // }
    public synchronized int adjustStartTimeMatchPlayed() { // for events that started in the previous day and continue this day
        int modified;
        final long currentTime = System.currentTimeMillis(), startTimeMillis = this.startTime.getTime();

        if (startTimeMillis - currentTime >= Generic.HOUR_LENGTH_MILLISECONDS * 4) {
            this.startTime.setTime(startTimeMillis - Generic.DAY_LENGTH_MILLISECONDS);
            modified = 1;
        } else {
            modified = 0;
        }

        return modified;
    }

    public synchronized boolean recentChange() {
        return this.classModifiers.contains("status_recentChange");
    }

    @Override
    public synchronized int innerUpdate(ScraperEvent scraperEvent) {
        int modified = super.innerUpdate(scraperEvent);
        BetradarEvent betradarEvent = (BetradarEvent) scraperEvent;

        modified += this.setStartTime(betradarEvent.getStartTime());
        modified += this.setClassModifiers(betradarEvent.getClassModifiers());
        // ScraperMatchFacts thatMatchFacts = betradarEvent.getMatchFacts();
        // if (thatMatchFacts != null && !thatMatchFacts.equals(this.matchFacts)) {
        //     this.matchFacts = thatMatchFacts;
        //     modified++;
        // }

        return modified;
    }

    @Override
    public synchronized long innerErrors(AtomicLong blackListPeriod) {
        long errors = super.innerErrors(blackListPeriod);
        int minutesPlayed = this.getMinutesPlayed(), homeScore = this.getHomeScore(), awayScore = this.getAwayScore(), homeHtScore = this.getHomeHtScore(),
                awayHtScore = this.getAwayHtScore(), homeRedCards = this.getHomeRedCards(), awayRedCards = this.getAwayRedCards();
        if (minutesPlayed > 120) {
            errors += 102400L;
            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
        }
        if (homeRedCards < 0) {
            errors += 204800L;
        }
        if (awayRedCards < 0) {
            errors += 409600L;
        }
        if (this.startTime == null) {
            errors += 819200L;
        }
        if (this.classModifiers == null) {
            errors += 1638400L;
        }
        MatchStatus matchStatus = this.getMatchStatus();
        if (matchStatus == null) {
            errors += 3276800L;
        } else {
            switch (matchStatus) {
                case NOT_STARTED:
                case START_DELAYED:
                    if (anyScoreExists() || anyHtScoreExists() || homeRedCards > 0 || awayRedCards > 0 || minutesOrStoppageExists()) {
                        errors += 6553600L;
                    }
                    break;
                case POSTPONED:
//                    if (anyScoreExists()) {
//                        errors += 1717986918400L;
//                    } else 
                    if (!recentChange() && minutesOrStoppageExists()) {
                        errors += 1L;
                    }
                    break;
                case ABANDONED:
                case CANCELLED:
//                    if (onlyOneScoreExists() || onlyOneHtScoreExists() || minutesPlayed > 120 || (minutesPlayed >= 46 && !bothHtScoreExists())) {
                    if (minutesPlayed > 120 || (minutesPlayed >= 46 && !bothHtScoreExistsAndNotHtIgnored())) {
                        errors += 429496729600L;
                    } else if (!recentChange() && minutesOrStoppageExists()) {
                        errors += 1L;
                    }
                    break;
                case FIRST_HALF:
                    if (recentChange() && minutesAre(1) && !anyScoreExists()) {
                        errors += 1L;
                    } else if (!bothScoreExists() || anyHtScoreExists() || minutesExistsAndNotInterval(1, 45)) {
                        errors += 13107200L;
                    } else if (!recentChange() && !minutesPlayedExists()) {
                        errors += 1L;
                    }
                    break;
                case HALF_TIME:
                    if (!bothScoreExists() || (!this.isHtIgnored() && (!bothHtScoreExists() || homeScore != homeHtScore || awayScore != awayHtScore)) || minutesExistsAndNot(45)) {
                        if (recentChange() && !anyHtScoreExists()) {
                            errors += 1L;
                        } else {
                            if (!bothScoreExists() || (!this.isHtIgnored() && (!bothHtScoreExists() || homeScore != homeHtScore || awayScore != awayHtScore))) {
                                // remains standard here
                            } else if (minutesPlayed < 45) {
                                blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
                            }
                            errors += 26214400L;
                        }
                    } else if (!recentChange() && stoppageExistsOrMinutesAre(45)) {
                        errors += 1L;
                    }
                    break;
                case SECOND_HALF:
                    if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored() || minutesExistsAndNotInterval(46, 90)) {
                        if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored()) { // remains standard here
                        } else if (minutesAre(45)) {
                            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
                        }
                        errors += 52428800L;
                    } else if (!recentChange() && !minutesPlayedExists()) {
                        errors += 1L;
                    }
                    break;
                case AWAITING_ET:
                    if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored() || minutesExistsAndNot(90)) {
                        if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored()) { // remains standard here
                        } else if (minutesPlayed < 90) {
                            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
                        }
                        errors += 104857600L;
                    } else if (!recentChange() && stoppageExistsOrMinutesAre(90)) {
                        errors += 1L;
                    }
                    break;
                case OVERTIME:
                    if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored() || minutesExistsAndNot(90)) {
                        if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored()) { // remains standard here
                        } else if (minutesPlayed < 90) {
                            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
                        }
                        errors += 858993459200L;
                    } else if (!recentChange() && stoppageExistsOrMinutesAre(90)) {
                        errors += 1L;
                    }
                    break;
                case FIRST_ET:
                    if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored() || minutesExistsAndNotInterval(91, 105)) {
                        if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored()) { // remains standard here
                        } else if (minutesAre(90)) {
                            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
                        }
                        errors += 209715200L;
                    } else if (!recentChange() && !minutesPlayedExists()) {
                        errors += 1L;
                    }
                    break;
                case ET_HALF_TIME:
                    if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored() || minutesExistsAndNot(105)) {
                        if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored()) { // remains standard here
                        } else if (minutesPlayed < 105) {
                            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
                        }
                        errors += 419430400L;
                    } else if (!recentChange() && stoppageExistsOrMinutesAre(105)) {
                        errors += 1L;
                    }
                    break;
                case SECOND_ET:
                    if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored() || minutesExistsAndNotInterval(106, 120)) {
                        if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored()) { // remains standard here
                        } else if (minutesAre(105)) {
                            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
                        }
                        errors += 838860800L;
                    } else if (!recentChange() && !minutesPlayedExists()) {
                        errors += 1L;
                    }
                    break;
                case AWAITING_PEN:
                    if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored() || minutesExistsAndNot(120, 90)) {
                        if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored()) { // remains standard here
                        } else if (minutesPlayed < 90) {
                            blackListPeriod.set(Math.max(blackListPeriod.get(), shortBlackListPeriod));
                        }
                        errors += 1677721600L;
                    } else if (!recentChange() && stoppageExistsOrMinutesAre(90, 120)) {
                        errors += 1L;
                    }
                    break;
                case PENALTIES:
                    if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored()) {
                        errors += 3355443200L;
                    } else if (!recentChange() && minutesOrStoppageExists()) {
                        errors += 1L;
                    }
                    break;
                case AFTER_ET:
//                    if (!bothScoreExists() || onlyOneHtScoreExists()) {
                    if (!bothScoreExists()) {
                        errors += 6710886400L;
                    } else if (!recentChange() && minutesOrStoppageExists()) {
                        errors += 1L;
                    }
                    break;
                case AFTER_PEN:
//                    if (!bothScoreExists() || onlyOneHtScoreExists()) {
                    if (!bothScoreExists()) {
                        errors += 13421772800L;
                    } else if (!recentChange() && minutesOrStoppageExists()) {
                        errors += 1L;
                    }
                    break;
                case ENDED:
                    if (!bothScoreExists() || !bothHtScoreExistsAndNotHtIgnored()) {
                        errors += 26843545600L;
                    } else if (!recentChange() && minutesOrStoppageExists()) {
                        errors += 1L;
                    }
                    break;
                case INTERRUPTED:
//                    if (!bothScoreExists() || onlyOneHtScoreExists() || minutesPlayed > 120 || (minutesPlayed >= 46 && !bothHtScoreExists())) {
                    if (!bothScoreExists() || minutesPlayed > 120 || (minutesPlayed >= 46 && !bothHtScoreExistsAndNotHtIgnored())) {
                        errors += 53687091200L;
                    } else if (!recentChange() && minutesOrStoppageExists()) {
                        errors += 1L;
                    }
                    break;
                case UNKNOWN:
                    logger.error("matchStatus {} in betradarEvent check for: {}", matchStatus, Generic.objectToString(this));
                    errors += 107374182400L;
                    break;
                default:
                    logger.error("unsupported matchStatus {} in betradarEvent check for: {}", matchStatus, Generic.objectToString(this));
                    errors += 214748364800L;
                    break;
            } // end switch
        } // end else
        return errors;
    }
}
