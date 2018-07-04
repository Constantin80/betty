package info.fmro.betty.main;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.ximpleware.AutoPilot;
import com.ximpleware.BookMark;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import info.fmro.betty.entities.Event;
import info.fmro.betty.enums.MatchStatus;
import info.fmro.betty.objects.BetradarEvent;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.WebScraperMethods;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BetradarScraperThread
        extends ScraperThread {

    private static final Logger logger = LoggerFactory.getLogger(BetradarScraperThread.class);
    public static final String selectFootballXPath = "//span[@class='item' and starts-with(@onclick,\"SRLive.trigger('treefilter:select', { _id: '1'\")]",
            orderByKickoffXPath = "//span[@class=' last ' and @data-eventid='kickoff' and starts-with(@onclick,\"SRLive.trigger('\")]",
            tickerZeroXPath = "//span[@class=' first ' and @data-eventid='0' and starts-with(@onclick,\"SRLive.trigger('eventticker_limit:list_item_internal_select\")]";

    private final AtomicBoolean mustSelectFootball = new AtomicBoolean(), mustOrderByKickoff = new AtomicBoolean(), mustTickerZero = new AtomicBoolean();
    private final AtomicLong timeLastPageManipulation = new AtomicLong();

    public BetradarScraperThread() {
        super("betradar", 500L, BrowserVersion.FIREFOX_52, 50, false);
    }

    public boolean scrapeStartTime(BetradarEvent scraperEvent, BookMark bookMark) {
        boolean success;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            autoPilot.selectXPath("./td[@class='time']");
            final int indexDataCellTime = autoPilot.evalXPath();
            if (indexDataCellTime >= 0) {
                final int indexTextTime = vtdNav.getText();
                if (indexTextTime >= 0) {
                    String startTimeString = vtdNav.toString(indexTextTime);
                    startTimeString = startTimeString.trim();
                    try {
                        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                        simpleDateFormat.setTimeZone(Generic.UTC_TIMEZONE);
                        final Date startTime = simpleDateFormat.parse(startTimeString); // only sets hour and minute

                        final Calendar calendar = Calendar.getInstance();
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        calendar.setTimeInMillis(calendar.getTimeInMillis() + startTime.getTime());

                        // logger.info("" + calendar.getTimeInMillis());
                        final long currentTime = System.currentTimeMillis(), calendarTimeMillis = calendar.getTimeInMillis();
                        if (currentTime - calendarTimeMillis >= Generic.HOUR_LENGTH_MILLISECONDS * 6) {
                            calendar.setTimeInMillis(calendarTimeMillis + Generic.DAY_LENGTH_MILLISECONDS);
                        }

                        startTime.setTime(calendar.getTimeInMillis());
                        scraperEvent.setStartTime(startTime);
                        // logger.info("" + scraperEvent.getStartTime().getTime());
                        success = true;
                    } catch (ParseException parseException) {
                        logger.error("{} parseException while converting time", threadId, parseException);
                        success = false;
                    }
                } else {
                    if (Statics.debugLevel.check(2, 145)) {
                        logger.error("{} Time text not found", threadId);
                    }
                    success = false;
                }
            } else {
                if (Statics.debugLevel.check(2, 130)) {
                    logger.error("{} Time not found", threadId);
                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot Time", threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot Time", threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot Time", threadId, navException);
            success = false;
        }
        return success;
    }

    public boolean scrapeHomeTeam(BetradarEvent scraperEvent, BookMark bookMark) {
        boolean success;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            autoPilot.selectXPath("./td[contains(@class, 'hometeam')]");
            final int indexDataCellHome = autoPilot.evalXPath();
            if (indexDataCellHome >= 0) {
                final int indexTextHome = vtdNav.getText();
                String homeTeam = null;
                if (indexTextHome >= 0) {
                    homeTeam = vtdNav.toString(indexTextHome);
                    homeTeam = homeTeam.trim();
                    scraperEvent.setHomeTeam(homeTeam);
                    success = true;
                } else {
                    if (Statics.debugLevel.check(2, 143)) {
                        logger.error("{} Home text not found", threadId);
                    }
                    success = false;
                }

                autoPilot.selectXPath("./span[contains(@class, 'red')]");

                int indexSpan, counterCards = 0;
                do {
                    indexSpan = autoPilot.evalXPath();
                    if (indexSpan >= 0) {
                        counterCards++;
                    }
                } while (indexSpan >= 0);
                scraperEvent.setHomeRedCards(counterCards);
                if (counterCards > 0) {
                    Generic.alreadyPrintedMap.logOnce(Statics.debugLevel.check(2, 127), logger, LogLevel.INFO, "red cards for {}: {}", homeTeam, counterCards);
                }
            } else {
                if (Statics.debugLevel.check(2, 131)) {
                    logger.error("{} HomeTeam not found", threadId);
                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot HomeTeam", threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot HomeTeam", threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot HomeTeam", threadId, navException);
            success = false;
        }
        return success;
    }

    public boolean scrapeAwayTeam(BetradarEvent scraperEvent, BookMark bookMark) {
        boolean success;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            autoPilot.selectXPath("./td[contains(@class, 'awayteam')]");
            final int indexDataCellAway = autoPilot.evalXPath();
            if (indexDataCellAway >= 0) {
                final int indexTextAway = vtdNav.getText();
                String awayTeam = null;
                if (indexTextAway >= 0) {
                    awayTeam = vtdNav.toString(indexTextAway);
                    awayTeam = awayTeam.trim();
                    scraperEvent.setAwayTeam(awayTeam);
                    success = true;
                } else {
                    if (Statics.debugLevel.check(2, 142)) {
                        logger.error("{} Away text not found", threadId);
                    }
                    success = false;
                }

                autoPilot.selectXPath("./span[contains(@class, 'red')]");

                int indexSpan, counterCards = 0;
                do {
                    indexSpan = autoPilot.evalXPath();
                    if (indexSpan >= 0) {
                        counterCards++;
                    }
                } while (indexSpan >= 0);
                scraperEvent.setAwayRedCards(counterCards);
                if (counterCards > 0) {
                    Generic.alreadyPrintedMap.logOnce(Statics.debugLevel.check(2, 128), logger, LogLevel.INFO, "red cards for {}: {}", awayTeam, counterCards);
                }
            } else {
                if (Statics.debugLevel.check(2, 132)) {
                    logger.error("{} AwayTeam not found", threadId);
                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot awayTeam", threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot awayTeam", threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot awayTeam", threadId, navException);
            success = false;
        }
        return success;
    }

    public boolean scrapeScore(BetradarEvent scraperEvent, BookMark bookMark) {
        boolean success;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            autoPilot.selectXPath("./td[@class='score' or (starts-with(@class, 'score') and contains(@class, 'recent'))]");
            final int indexDataCellScore = autoPilot.evalXPath();
            if (indexDataCellScore >= 0) {
                BookMark bookMarkCellScore = new BookMark(vtdNav);
                bookMarkCellScore.recordCursorPosition();

                autoPilot.selectXPath("./span");

                int indexSpan;
                final List<BookMark> scoreBookMarksList = new ArrayList<>(2);
                do {
                    indexSpan = autoPilot.evalXPath();
                    if (indexSpan >= 0) {
                        final BookMark scoreBookMark = new BookMark(vtdNav);
                        scoreBookMark.recordCursorPosition();
                        final int indexText = vtdNav.getText();
                        if (indexText >= 0) {
                            scoreBookMarksList.add(scoreBookMark);
                        } else {
                            logger.error("{} no text for Score Span listSize: {}", threadId, scoreBookMarksList.size());
                        }
                    }
                } while (indexSpan >= 0);
                final int listSize = scoreBookMarksList.size();
                if (listSize == 2) {
                    scoreBookMarksList.get(0).setCursorPosition();
                    final int indexHome = vtdNav.getText();
                    String homeScoreString = vtdNav.toString(indexHome);

                    scoreBookMarksList.get(1).setCursorPosition();
                    final int indexAway = vtdNav.getText();
                    String awayScoreString = vtdNav.toString(indexAway);

                    if (homeScoreString != null && awayScoreString != null) {
                        homeScoreString = homeScoreString.trim();
                        awayScoreString = awayScoreString.trim();
                        int homeScore, awayScore;
                        try {
                            homeScore = Integer.parseInt(homeScoreString);
                            scraperEvent.setHomeScore(homeScore);
                            awayScore = Integer.parseInt(awayScoreString);
                            scraperEvent.setAwayScore(awayScore);
                            success = true;
                        } catch (NumberFormatException numberFormatException) {
                            if (Statics.debugLevel.check(2, 148)) {
                                logger.error("{} numberFormatException while getting score from: {} {}", threadId, homeScoreString, awayScoreString, numberFormatException);
                            }
                            success = false;
                        }
                    } else {
                        logger.error("{} homeScoreString {} or awayScoreString {} are null", threadId, homeScoreString, awayScoreString);
                        success = false;
                    }
                } else {
                    if (listSize != 0) {
                        if (Statics.debugLevel.check(2, 147)) {
                            logger.error("{} Spancounter bad in Score: {}", threadId, listSize);
                        }
                        success = false;
                    } else {
                        bookMarkCellScore.setCursorPosition();
                        final int indexCellScoreText = vtdNav.getText();
                        if (indexCellScoreText < 0) {
                            if (Statics.debugLevel.check(2, 140)) {
                                logger.error("{} no CellScoreText found", threadId);
                            }
                            success = false;
                        } else {
                            final String textContent = vtdNav.toString(indexCellScoreText);
                            if (!textContent.contains("-:-")) {
                                logger.error("{} textContent bad for Score: {}", threadId, textContent);
                                success = false;
                            } else { // match hasn't started, thus no score
                                success = true; // it's normal
                            }
                        }
                    }
                }
            } else {
                if (Statics.debugLevel.check(2, 133)) {
                    logger.error("{} Score not found", threadId);
                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot Score", threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot Score", threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot Score", threadId, navException);
            success = false;
        }
        return success;
    }

    public boolean scrapeHtScore(BetradarEvent scraperEvent, BookMark bookMark) {
        boolean success;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            autoPilot.selectXPath("./td[@class='ht']");
            final int indexDataCellHt = autoPilot.evalXPath();
            if (indexDataCellHt >= 0) {
                autoPilot.selectXPath("./span");

                int indexSpan;
                final List<BookMark> htBookMarksList = new ArrayList<>(2);
                do {
                    indexSpan = autoPilot.evalXPath();
                    if (indexSpan >= 0) {
                        final BookMark htBookMark = new BookMark(vtdNav);
                        htBookMark.recordCursorPosition();
                        int indexText = vtdNav.getText();
                        if (indexText >= 0) {
                            htBookMarksList.add(htBookMark);
                        } else {
                            logger.error("{} no text for Ht Span listSize: {}", threadId, htBookMarksList.size());
                        }
                    }
                } while (indexSpan >= 0);
                final int listSize = htBookMarksList.size();
                if (listSize == 2) {
                    htBookMarksList.get(0).setCursorPosition();
                    final int indexHome = vtdNav.getText();
                    String homeScoreString = vtdNav.toString(indexHome);

                    htBookMarksList.get(1).setCursorPosition();
                    final int indexAway = vtdNav.getText();
                    String awayScoreString = vtdNav.toString(indexAway);

                    if (homeScoreString != null && awayScoreString != null) {
                        homeScoreString = homeScoreString.trim();
                        awayScoreString = awayScoreString.trim();
                        int homeScore, awayScore;
                        try {
                            homeScore = Integer.parseInt(homeScoreString);
                            scraperEvent.setHomeHtScore(homeScore);
                            awayScore = Integer.parseInt(awayScoreString);
                            scraperEvent.setAwayHtScore(awayScore);

                            scraperEvent.adjustStartTimeMatchPlayed(); // event is or has just been live, so for events that started in the previous day and continue this day
                            success = true;
                        } catch (NumberFormatException numberFormatException) {
                            if (Statics.debugLevel.check(2, 149)) {
                                logger.error("{} numberFormatException while getting ht score from: {} {}", threadId, homeScoreString, awayScoreString, numberFormatException);
                            }
                            success = false;
                        }
                    } else {
                        logger.error("{} homeScoreString {} or awayScoreString {} are null at ht", threadId, homeScoreString, awayScoreString);
                        success = false;
                    }
                } else {
                    if (listSize != 0) {
                        if (Statics.debugLevel.check(2, 146)) {
                            logger.error("{} Spancounter bad in HtScore: {}", threadId, listSize);
                        }
                        success = false;
                    } else {
                        success = true; // normal behaviour
                    }
                }
            } else {
                if (Statics.debugLevel.check(2, 134)) {
                    logger.error("{} HtScore not found", threadId);
                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot HtScore", threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot HtScore", threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot HtScore", threadId, navException);
            success = false;
        }
        return success;
    }

    public boolean scrapeMatchStatus(BetradarEvent scraperEvent, BookMark bookMark) {
        boolean success;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            autoPilot.selectXPath("./td[@class='status' or (starts-with(@class, 'status') and contains(@class, 'recent'))]");
            final int indexDataCellStatus = autoPilot.evalXPath();
            if (indexDataCellStatus >= 0) {
                autoPilot.selectXPath("./span");
                final int indexSpan = autoPilot.evalXPath();
                if (indexSpan >= 0) {
                    final int indexStatusString = vtdNav.getText();
                    if (indexStatusString >= 0) {
                        String statusString = vtdNav.toString(indexStatusString);
                        if (statusString != null) {
                            statusString = statusString.trim();
                            if (!statusString.isEmpty()) {
                                MatchStatus matchStatus;
                                switch (statusString) {
                                    case "Abandoned":
                                        matchStatus = MatchStatus.ABANDONED;
                                        break;
                                    case "Postponed":
                                        matchStatus = MatchStatus.POSTPONED;
                                        break;
                                    case "Not started":
                                        matchStatus = MatchStatus.NOT_STARTED;
                                        break;
                                    case "Start delayed":
                                        matchStatus = MatchStatus.START_DELAYED;
                                        break;
                                    case "1st half":
                                        matchStatus = MatchStatus.FIRST_HALF;
                                        break;
                                    case "Half Time":
                                        matchStatus = MatchStatus.HALF_TIME;
                                        break;
                                    case "2nd half":
                                        matchStatus = MatchStatus.SECOND_HALF;
                                        break;
                                    case "Awaiting extra time":
                                        matchStatus = MatchStatus.AWAITING_ET;
                                        break;
                                    case "Overtime":
                                        matchStatus = MatchStatus.OVERTIME;
                                        break;
                                    case "1st extra":
                                        matchStatus = MatchStatus.FIRST_ET;
                                        break;
                                    case "Extra time halftime":
                                        matchStatus = MatchStatus.ET_HALF_TIME;
                                        break;
                                    case "2nd extra":
                                        matchStatus = MatchStatus.SECOND_ET;
                                        break;
                                    case "Awaiting penalties":
                                        matchStatus = MatchStatus.AWAITING_PEN;
                                        break;
                                    case "Penalties":
                                        matchStatus = MatchStatus.PENALTIES;
                                        break;
                                    case "AET":
                                        matchStatus = MatchStatus.AFTER_ET;
                                        break;
                                    case "AP":
                                        matchStatus = MatchStatus.AFTER_PEN;
                                        break;
                                    case "Ended":
                                        matchStatus = MatchStatus.ENDED;
                                        break;
                                    case "Interrupted":
                                        matchStatus = MatchStatus.INTERRUPTED;
                                        break;
                                    case "Cancelled":
                                        matchStatus = MatchStatus.CANCELLED;
                                        break;
                                    default:
                                        matchStatus = MatchStatus.UNKNOWN;
                                        logger.error("{} unknown statusString in scrapeMatchStatus: {} {}", threadId, statusString, Generic.objectToString(scraperEvent));

                                        long currentTime = System.currentTimeMillis();
                                        synchronized (lastTimedPageSave) { // synchronized not necessary for now, added just in case
                                            if (currentTime - lastTimedPageSave.get() > Generic.MINUTE_LENGTH_MILLISECONDS * 30L) {
                                                lastTimedPageSave.set(currentTime);
                                                mustSavePage.set(true);
                                            }
                                        } // end synchronized
                                        break;
                                } // end switch
                                scraperEvent.setMatchStatus(matchStatus);
                                success = true;
                            } else {
                                if (Statics.debugLevel.check(2, 141)) {
                                    logger.error("{} statusString empty", threadId);
                                }
                                success = false;
                            }
                        } else {
                            logger.error("{} statusString null", threadId);
                            success = false;
                        }
                    } else {
                        logger.error("{} statusString not found in MatchStatus", threadId);
                        success = false;
                    }
                } else {
                    if (Statics.debugLevel.check(2, 144)) {
                        logger.error("{} Span not found in MatchStatus", threadId);
                    }
                    success = false;
                }
            } else {
                if (Statics.debugLevel.check(2, 135)) {
                    logger.error("{} MatchStatus not found", threadId);
                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot MatchStatus", threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot MatchStatus", threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot MatchStatus", threadId, navException);
            success = false;
        }
        return success;
    }

    public boolean scrapeMinutesPlayed(BetradarEvent scraperEvent, BookMark bookMark) {
        boolean success;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            autoPilot.selectXPath("./td[@class='minutesplayed']");
            final int indexDataCellMinutes = autoPilot.evalXPath();

            if (indexDataCellMinutes >= 0) {
                final int indexMinutesString = vtdNav.getText();
                if (indexMinutesString >= 0) {
                    String minutesString = vtdNav.toString(indexMinutesString);
                    minutesString = minutesString.trim();
                    if (minutesString.contains("'")) {
                        minutesString = minutesString.substring(0, minutesString.indexOf('\''));
                        int stoppage;
                        try {
                            if (minutesString.contains("+")) {
                                final String stoppageString = minutesString.substring(minutesString.indexOf('+') + "+".length());
                                stoppage = Integer.parseInt(stoppageString);
                                minutesString = minutesString.substring(0, minutesString.indexOf('+'));
                            } else {
                                stoppage = -1;
                            }

                            scraperEvent.setStoppageTime(stoppage);
                            final int minutes = Integer.parseInt(minutesString);
                            scraperEvent.setMinutesPlayed(minutes);
                            success = true;
                        } catch (NumberFormatException numberFormatException) {
                            logger.error("{} numberFormatException getting played time from: {}", threadId, minutesString, numberFormatException);
                            success = false;
                        }
                    } else { // no minutes played
                        success = true;
                    }
                } else { // no minutes played; seems to have no text and end up on this branch
                    // logger.error("minutesString not found: {}", indexMinutesString);
                    success = true; // although minutes not found, this is normal behaviour
                }
            } else {
                if (Statics.debugLevel.check(2, 136)) {
                    logger.error("{} DataCellMinutes not found", threadId);
                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot MinutesPlayed", threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot MinutesPlayed", threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot MinutesPlayed", threadId, navException);
            success = false;
        }
        return success;
    }

    @Override
    public void pageManipulation(WebClient webClient, HtmlPage htmlPage) {
        if (mustSelectFootball.getAndSet(false)) {
            timeLastPageManipulation.set(System.currentTimeMillis());
            WebScraperMethods.clickElements(webClient, mustRefreshPage, htmlPage, 5_000L, selectFootballXPath, threadId, SAVE_FOLDER + "/click", mustSavePage);
        }
        if (mustOrderByKickoff.getAndSet(false)) {
            timeLastPageManipulation.set(System.currentTimeMillis());
            WebScraperMethods.clickElements(webClient, mustRefreshPage, htmlPage, 5_000L, orderByKickoffXPath, threadId, SAVE_FOLDER + "/click", mustSavePage);
        }
        if (mustTickerZero.getAndSet(false)) {
            timeLastPageManipulation.set(System.currentTimeMillis());
            WebScraperMethods.clickElements(webClient, mustRefreshPage, htmlPage, 5_000L, tickerZeroXPath, threadId, SAVE_FOLDER + "/click", mustSavePage);
        }

        final long timeStamp = System.currentTimeMillis();
        if (timeStamp - timeLastPageManipulation.get() > 30L * Generic.MINUTE_LENGTH_MILLISECONDS) {
            timeLastPageManipulation.set(timeStamp);
            // attempts to avoid need for refresh
            WebScraperMethods.clickElements(webClient, mustRefreshPage, htmlPage, 5_000L, tickerZeroXPath, threadId, SAVE_FOLDER + "/click", mustSavePage);
        }
    }

    @Override
    public void getScraperEventsInner(long startTime, boolean fullRun, boolean checkAll, AutoPilot autoPilot, VTDNav vtdNav, AtomicInteger listSize,
                                      AtomicInteger scrapedEventsCounter)
            throws XPathParseException, XPathEvalException, NavException {

        final boolean checkForErrors = (timedScraperCounter.get() - 1) % 10 == 0;

        if (checkForErrors) {
            // refresh dialog errors
            autoPilot.selectXPath("//div[@id='srlive-inactivity-dialog' and @style='display: block;']");
            final int indexRefresh = autoPilot.evalXPath();
            if (indexRefresh >= 0) {
                // Formulas.logOnce(Generic.MINUTE_LENGTH_MILLISECONDS * 5L, logger, LogLevel.INFO, "{} refresh dialog found", threadId);
                if (!mustRefreshPage.getAndSet(true)) {
                    logger.info("{} refresh dialog found", threadId);
                }
            } else { // no refresh needed
            }

//            <li class="tree-item active expanded live  tree-type-sports" id="tree-_sid-1">
            autoPilot.selectXPath("//li[@class='tree-item active expanded live  tree-type-sports' and @id='tree-_sid-1']");
            final int indexFootballSelected = autoPilot.evalXPath();
            if (indexFootballSelected < 0) {
                logger.error("indexFootballSelected not found");
                mustSelectFootball.set(true);
            } else { // football selected, bnothing to be done
            }

//            <li class="srlive-list-item-kickoff active  last ">
            autoPilot.selectXPath("//li[contains(@class, 'srlive-list-item-kickoff active')]");
            final int indexOrderByKickoff = autoPilot.evalXPath();
            if (indexOrderByKickoff < 0) {
                logger.error("indexOrderByKickoff not found");
                mustOrderByKickoff.set(true);
            } else { // order by kickoff selected, bnothing to be done
            }

//            <li class="srlive-list-item-0 active  first ">
            autoPilot.selectXPath("//li[contains(@class, 'srlive-list-item-0 active')]");
            final int indexTickerZero = autoPilot.evalXPath();
            if (indexTickerZero < 0) {
                logger.error("indexTickerZero not found");
                mustTickerZero.set(true);
            } else { // ticker rero selected, bnothing to be done
            }
        } else {
            // not checking for errors during this iteration, nothing to be done
        }

        autoPilot.selectXPath("//div[contains(@class, 'sport_1 ')]");
        final int indexDivisionFootball = autoPilot.evalXPath();
        if (indexDivisionFootball >= 0) {
            majorScrapingError.set(0); // page seems to be loaded fine
            autoPilot.selectXPath(".//tr[@class and contains(@id, 'match-')]");

            int expectedLiveEvents = liveMaxValue.getValue();
            if (fullRun) {
                expectedLiveEvents++; // get 1 extra event; in case something started between checkAll runs
            }

            int listCapacity;
            if (checkAll) {
                listCapacity = listSizeMaxValue.getValue();
            } else {
                listCapacity = expectedLiveEvents;
            }
            final List<BookMark> trBookMarksList = new ArrayList<>(listCapacity);

            int trIndex, whileCounter = 0;
            do {
                trIndex = autoPilot.evalXPath();
                if (trIndex >= 0 && (checkAll || whileCounter < listCapacity)) {
                    whileCounter++;
                    final BookMark bookMark = new BookMark(vtdNav);
                    bookMark.recordCursorPosition();
                    trBookMarksList.add(bookMark);
                }
            } while (trIndex >= 0 && (checkAll || whileCounter < listCapacity));

            listSize.set(trBookMarksList.size());
            final HashSet<Event> eventsAttachedToModifiedScraperEvents = new HashSet<>(0);
            final Set<BetradarEvent> addedScraperEvents = new HashSet<>(0);
            int nLiveEvents = 0, counterNotStarted = 0;
            // for (int i = 0; (i < listSize) && (counterNotStarted == 0 || checkAll); i++) {
            final int listSizePrimitive = listSize.get();
            for (int i = 0; i < listSizePrimitive; i++) {
                final BookMark bookMark = trBookMarksList.get(i);
                bookMark.setCursorPosition();
                final int indexIdAttributeVal = vtdNav.getAttrVal("id");
                if (indexIdAttributeVal >= 0) {
                    final String eventIdString = vtdNav.toString(indexIdAttributeVal);
                    try {
                        final long eventId = Long.valueOf(eventIdString.substring(eventIdString.indexOf("match-") + "match-".length()));
//                        if (!BlackList.containsBetradarId(eventId)) {
                        final int indexClassAttributeVal = vtdNav.getAttrVal("class");
                        if (indexClassAttributeVal >= 0) {
                            final String classModifiers = vtdNav.toString(indexClassAttributeVal);
                            // match filterable  status_result status_postponed status_next
                            // match filterable  status_upcoming status_liveodds status_next
                            // match filterable  status_live status_livenow status_liveodds status_next
                            // match filterable  status_live status_livenow status_next status_recentChange
                            // match filterable  status_live status_livenow status_liveodds status_next status_recentChange
                            // match filterable  status_result status_recentlyended status_liveodds status_next status_recentChange
                            final boolean shouldScrape = fullRun || checkAll || classModifiers.contains("status_recentChange");

                            if (shouldScrape) {
                                final BetradarEvent scraperEvent = new BetradarEvent(eventId, startTime);
//                                    scraperEvent.timeStamp();
                                scraperEvent.setClassModifiers(classModifiers);

                                final boolean scrapedStartTime = scrapeStartTime(scraperEvent, bookMark);
                                final boolean scrapedHomeTeam = scrapeHomeTeam(scraperEvent, bookMark);
                                final boolean scrapedAwayTeam = scrapeAwayTeam(scraperEvent, bookMark);
                                final boolean scrapedScore = scrapeScore(scraperEvent, bookMark);
                                final boolean scrapedHtScore = scrapeHtScore(scraperEvent, bookMark);
                                final boolean scrapedMatchStatus = scrapeMatchStatus(scraperEvent, bookMark);
                                final boolean scrapedMinutesPlayed = scrapeMinutesPlayed(scraperEvent, bookMark);

                                MatchStatus matchStatus = scraperEvent.getMatchStatus();
                                if (matchStatus == null) { // likely not scraped properly; nothing to be done; error messages are printed in other places
                                } else if (matchStatus.hasStarted()) {
                                    nLiveEvents = i + 1 + counterNotStarted; // will get a few extra but ensures I get all; i+1 because it starts from 0
                                    if (counterNotStarted > 0) {
                                        if (Statics.debugLevel.check(2, 195)) {
                                            Generic.alreadyPrintedMap.logOnce(logger, LogLevel.INFO, "{} found started game after not started: counterNotStarted={} {} id:{}",
                                                                              threadId, counterNotStarted, matchStatus.name(), eventId);
                                        }
//                                                            lastFoundStartedAfterNotStarted.set(startTime);
                                    }
                                } else {
                                    // no longer necessary; nLiveEvents gets updated on the other if branch
//                                                if (counterNotStarted == 0) {
//                                                    nLiveEvents = i; // it's actually i+1-1; +1 because it starts from 0 and -1 because it relates to previous value
//                                                } else { // nLiveEvents doesn't get modified; nothing to be done
//                                                }
                                    counterNotStarted++;
                                }

                                if (scrapedStartTime && scrapedHomeTeam && scrapedAwayTeam && scrapedScore && scrapedHtScore && scrapedMatchStatus && scrapedMinutesPlayed) {
                                    final long scraperErrors = scraperEvent.errors();
                                    if (scraperErrors <= 0L) {
                                        scrapedEventsCounter.incrementAndGet();

                                        BetradarEvent existingScraperEvent;
//                                            synchronized (Statics.betradarEventsMap) {
                                        if (Statics.betradarEventsMap.containsKey(eventId)) {
                                            existingScraperEvent = Statics.betradarEventsMap.get(eventId);
                                            if (matchStatus != null && matchStatus.hasStarted()) { // all good, nothing to be done here
                                            } else if (matchStatus == null) {
                                                logger.error("{} matchStatus null; this else branch shouldn't be entered: {} {}", threadId, scrapedMatchStatus,
                                                             Generic.objectToString(scraperEvent));
                                            } else { // !matchStatus.hasStarted()
                                                if (existingScraperEvent != null) {
                                                    logger.error("{} !hasStarted exists in map: {} {}", threadId, Generic.objectToString(existingScraperEvent, Generic.objectToString(scraperEvent)));
                                                    scraperEvent.setIgnored(Generic.MINUTE_LENGTH_MILLISECONDS * 2L);
//                                                    MaintenanceThread.removeScraper(scraperEvent);
                                                } else { // existingScraperEvent null & betradarEventsMap.containsKey(eventId)
                                                    long timeSinceLastRemoved = startTime - Statics.betradarEventsMap.getTimeStampRemoved();
                                                    Statics.betradarEventsMap.removeValueAll(null);

                                                    String printedString = MessageFormatter.arrayFormat(
                                                            "{} null betradarEvent in map, timeSinceLastRemoved: {} for eventId: {} of scraperEvent: {} {}",
                                                            new Object[]{threadId, timeSinceLastRemoved, eventId, Generic.objectToString(scraperEvent),
                                                                         Generic.objectToString(existingScraperEvent)}).getMessage();
//                                                    if (timeSinceLastRemoved < 1_000L) {
//                                                        logger.info("{} null betradarEvent in map timeSinceLastRemoved {} for eventId {}", threadId, timeSinceLastRemoved,
//                                                                eventId);
//                                                    } else {
                                                    logger.error(printedString);
//                                                    }
                                                }

                                                existingScraperEvent = null; // has just been removed
                                            } // end else
                                        } else {
                                            if (matchStatus != null && matchStatus.hasStarted()) {
//                                                if (!BlackList.dummyScraperEvent.equals(BlackList.checkedPutScraper(eventId, scraperEvent))) {
                                                existingScraperEvent = Statics.betradarEventsMap.putIfAbsent(eventId, scraperEvent);
                                                if (existingScraperEvent == null) { // event was added, no previous event existed, double check to avoid racing issues
//                                                    existingScraperEvent = null;
                                                    addedScraperEvents.add(scraperEvent);
                                                    lastUpdatedScraperEvent.set(startTime); // event has been added
//                                                } else {
//                                                    logger.error("{} won't add blackListed scraperEvent: {}", threadId, eventId);
//                                                }
                                                } else {
                                                    logger.error("existingScraperEvent found during {} put double check: {} {}", threadId,
                                                                 Generic.objectToString(existingScraperEvent), Generic.objectToString(scraperEvent));
//                                                    existingScraperEvent = inMapScraperEvent;
                                                }
                                            } else { // won't be added
                                                existingScraperEvent = null;
                                            }
                                        } // end else
//                                            } // end synchronized
                                        if (existingScraperEvent != null) {
                                            final String matchedEventId = existingScraperEvent.getMatchedEventId();
//                                            scraperEvent.setMatchedEventId(matchedEventId); // placed before update, else error in update
                                            final int update = existingScraperEvent.update(scraperEvent);
                                            if (update > 0) {
                                                lastUpdatedScraperEvent.set(startTime); // updated scraperEvent, not necessarely attached to a betfair event
                                                if (matchedEventId != null) {
                                                    final Event event = Statics.eventsMap.get(matchedEventId);
                                                    if (event != null) {
                                                        if (!event.isIgnored()) {
                                                            eventsAttachedToModifiedScraperEvents.add(event);
                                                        } else { // won't do anything about ignored events
                                                        }
                                                    } else {
                                                        final long timeSinceLastRemoved = startTime - Statics.eventsMap.getTimeStampRemoved();
                                                        // won't remove here; error message should suffice, and removing might cause problems
//                                                        Statics.eventsMap.remove(matchedEventId);
//                                                        Statics.betradarEventsMap.remove(eventId);

                                                        final String printedString = MessageFormatter.arrayFormat(
                                                                "{} null event in map or id not found, timeSinceLastRemoved: {} for matchedEventId: {} of scraperEvent: {} {}",
                                                                new Object[]{threadId, timeSinceLastRemoved, matchedEventId, Generic.objectToString(scraperEvent),
                                                                             Generic.objectToString(existingScraperEvent)}).getMessage();
                                                        if (timeSinceLastRemoved < 1_000L) {
                                                            logger.info("{} null event in map or id not found, timeSinceLastRemoved {} for matchedEventId {} of scraperEventId {}",
                                                                        threadId, timeSinceLastRemoved, matchedEventId, eventId);
                                                        } else {
                                                            logger.error(printedString);
                                                        }
                                                    }
                                                } else { // not matched scraper, no event to add for checking; no need to attempt matching again, only name is checked
                                                }
                                            }
                                            if (update >= 0) { // excludes update errors; in that case, scraper was already purged
                                                final long existingScraperErrors = existingScraperEvent.errors();
                                                if (existingScraperErrors > 0L) { // this sometimes, rarely, does happen
//                                                    logger.error("{} check true scraperEvent updated into check false scraperEvent: {} {}", threadId,
//                                                            Generic.objectToString(scraperEvent), Generic.objectToString(existingScraperEvent));

                                                    Generic.alreadyPrintedMap.logOnce(logger, LogLevel.ERROR,
                                                                                      "{} check true scraperEvent updated into check false {} scraperEvent: {} {}", threadId, existingScraperErrors,
                                                                                      Generic.objectToString(scraperEvent, "seconds", "classModifiers", "minutesPlayed", "stoppageTime", "Stamp"),
                                                                                      Generic.objectToString(existingScraperEvent, "seconds", "classModifiers", "minutesPlayed", "stoppageTime", "Stamp"));

                                                    // probably no need for removal, ignore is done when existingScraperEvent.errors() is invoked
//                                                    Statics.betradarEventsMap.remove(eventId);
                                                }
                                            }
                                        } else { // no existing scraperEvent, nothing to be done about it
                                        }

                                        if (Statics.debugLevel.check(3, 108)) {
                                            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                            final String formattedDate = scraperEvent.getHomeTeam() + "/" + scraperEvent.getAwayTeam() + ": " +
                                                                         scraperEvent.getStartTime().getTime() + " " + simpleDateFormat.format(scraperEvent.getStartTime());
                                            logger.info("{} scraperEvent parsed: {} {}", threadId, formattedDate, Generic.objectToString(scraperEvent));
                                        }
                                    } else {
                                        final String scraperString = Generic.objectToString(scraperEvent, "seconds", "classModifiers", "minutesPlayed", "stoppageTime", "Stamp");

                                        if (scraperErrors >= 100) {
//                                            logger.error("{} scraperEvent scraperErrors: {} in getScraperEvents for: {}", threadId, scraperErrors, scraperString);
                                            Generic.alreadyPrintedMap.logOnce(5L * Generic.MINUTE_LENGTH_MILLISECONDS, logger, LogLevel.ERROR,
                                                                              "{} scraperEvent scraperErrors: {} in getScraperEvents for: {}", threadId, scraperErrors, scraperString);
                                        } else if (scraperErrors >= 1) {
                                            final long currentTime = System.currentTimeMillis();

                                            if (Statics.timedWarningsMap.containsKey(scraperString)) {
                                                final long existingTime = Statics.timedWarningsMap.get(scraperString);
                                                final long lapsedTime = currentTime - existingTime;

                                                if (lapsedTime > Generic.MINUTE_LENGTH_MILLISECONDS) { // temporary errors allowed for at most 1 minute
                                                    logger.error("{} scraperEvent minor but lasting {} ms scraperErrors: {} in getScraperEvents for: {}", threadId,
                                                                 lapsedTime, scraperErrors, scraperString);
                                                } else { // might be a normal temporary error; not enough time has lapsed
                                                }
                                            } else { // first time this particular error is found
                                                Statics.timedWarningsMap.put(scraperString, currentTime);
                                            }
                                        } else {
                                            logger.error("STRANGE {} scraperErrors {} value for scraperEvent: {}", threadId, scraperErrors,
                                                         Generic.objectToString(scraperEvent));
                                        }
                                    } // end else
                                } else {
                                    if (Statics.debugLevel.check(2, 137)) {
                                        logger.error("{} scraperEvent not scraped properly: {} {} {} {} {} {} {} {}", threadId, Generic.objectToString(scraperEvent),
                                                     scrapedStartTime, scrapedHomeTeam, scrapedAwayTeam, scrapedScore, scrapedHtScore, scrapedMatchStatus,
                                                     scrapedMinutesPlayed);
                                    }
                                }
                            } else { // event not changed recently and not fullRun
                            }
                        } else {
                            logger.error("{} indexClassAttributeVal not found in getScraperEvents", threadId);
                        }
//                        } else { // blackListed scraperId
//                        }
                    } catch (NumberFormatException numberFormatException) {
                        logger.error("{} numberFormatException while getting eventId from: {}", threadId, eventIdString, numberFormatException);
                    }
                } else {
                    logger.error("{} indexIdAttributeVal not found in getScraperEvents", threadId);
                }
            } // end for

            listSizeMaxValue.setValue(listSize.get(), startTime);
            liveMaxValue.setValue(nLiveEvents, startTime);
            Statics.betradarEventsMap.timeStamp();

            final int sizeAdded = addedScraperEvents.size();
            if (sizeAdded > 0) {
                logger.info("{} getScraperEvents addedScraperEvents: {} launch: mapEventsToScraperEvents", threadId, sizeAdded);
                Statics.threadPoolExecutor.execute(new LaunchCommandThread("mapEventsToScraperEvents", addedScraperEvents, BetradarEvent.class));
            }
            final int sizeEvents = eventsAttachedToModifiedScraperEvents.size();
            if (sizeEvents > 0) {
                logger.info("{} getScraperEvents toCheckEvents: {} launch: findSafeRunners", threadId, sizeEvents);
                Statics.threadPoolExecutor.execute(new LaunchCommandThread("findSafeRunners", eventsAttachedToModifiedScraperEvents));
            }

            if (!mustRefreshPage.get()) {
                // lastUpdatedScraperEvent refresh support is designed to detect errors; it's not supposed to be a reliable way of refreshing
                long timeSinceLastUpdate = startTime - lastUpdatedScraperEvent.get();
                if (nLiveEvents > 0) {
                    if (timeSinceLastUpdate > Generic.MINUTE_LENGTH_MILLISECONDS * 3L) {
                        if (timeSinceLastUpdate > Generic.MINUTE_LENGTH_MILLISECONDS * 20L) { // upto 15 minutes without update during "Half Time"
                            logger.error("started events exist and no updated {} events for {}ms", threadId, timeSinceLastUpdate);
                            mustRefreshPage.set(true);
                        } else {
                            long maxTimeBetweenUpdates = Generic.MINUTE_LENGTH_MILLISECONDS * 20L;
                            Collection<BetradarEvent> valuesCopy = Statics.betradarEventsMap.valuesCopy();
                            for (BetradarEvent betradarEvent : valuesCopy) {
                                if (betradarEvent != null) {
                                    MatchStatus matchStatus = betradarEvent.getMatchStatus();
                                    if (matchStatus != null) {
                                        switch (matchStatus) {
                                            case PENALTIES:
                                                maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 3L);
                                                break;
                                            case FIRST_HALF:
                                                if (betradarEvent.minutesAre(45)) {
                                                    maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
                                                } else {
                                                    maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 3L);
                                                }
                                                break;
                                            case SECOND_HALF:
                                                if (betradarEvent.minutesAre(90)) {
                                                    maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
                                                } else {
                                                    maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 3L);
                                                }
                                                break;
                                            case FIRST_ET:
                                                if (betradarEvent.minutesAre(105)) {
                                                    maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
                                                } else {
                                                    maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 3L);
                                                }
                                                break;
                                            case SECOND_ET:
                                                if (betradarEvent.minutesAre(120)) {
                                                    maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
                                                } else {
                                                    maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 3L);
                                                }
                                                break;
                                            case ET_HALF_TIME:
                                                maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 10L);
                                                break;
                                            case AWAITING_ET:
                                            case AWAITING_PEN:
                                            case AFTER_ET:
                                            case AFTER_PEN:
                                            case ENDED:
                                                maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 15L);
                                                break;
                                            case HALF_TIME:
                                            case OVERTIME: // time between updates is actually even longer, but I'll keep the max
                                            case INTERRUPTED: // time between updates is actually even longer, but I'll keep the max
                                            case ABANDONED: // time between updates is actually even longer, but I'll keep the max
                                                // maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 20L);
                                                break;
                                            default:
                                                // maxTimeBetweenUpdates = Math.min(maxTimeBetweenUpdates, Generic.MINUTE_LENGTH_MILLISECONDS * 20L);
                                                logger.error("STRANGE matchstatus {} in Statics.{}EventsMap during matchStatus check for: {}", matchStatus.name(), threadId, Generic.objectToString(betradarEvent));
                                                betradarEvent.setIgnored(Generic.MINUTE_LENGTH_MILLISECONDS * 15L);
//                                                MaintenanceThread.removeScraper(betradarEvent);
//                                                Statics.betradarEventsMap.removeValueAll(betradarEvent);
                                                break;
                                        } // end switch
                                        if (maxTimeBetweenUpdates == Generic.MINUTE_LENGTH_MILLISECONDS * 3L) {
                                            break;
                                        } else if (maxTimeBetweenUpdates > Generic.MINUTE_LENGTH_MILLISECONDS * 3L) { // nothing to be done, for continues
                                        } else if (maxTimeBetweenUpdates < Generic.MINUTE_LENGTH_MILLISECONDS * 3L) {
                                            logger.error("maxTimeBetweenUpdates too small: {}", maxTimeBetweenUpdates);
                                            break;
                                        }
                                    } else { // matchstatus == null
                                        logger.error("null matchStatus in Statics.{}EventsMap while checking matchStatus for: {}", threadId, Generic.objectToString(betradarEvent));
                                        betradarEvent.setIgnored(Generic.MINUTE_LENGTH_MILLISECONDS * 2L);
//                                        MaintenanceThread.removeScraper(betradarEvent);
//                                        Statics.betradarEventsMap.removeValueAll(betradarEvent);
                                    }
                                } else { // betradarEvent == null
                                    logger.error("null value in Statics.{}EventsMap while checking matchStatus", threadId);
                                    Statics.betradarEventsMap.removeValueAll(null); // null removal
                                }
                            } // end for
                            if (timeSinceLastUpdate > maxTimeBetweenUpdates) {
                                logger.error("started events exist and no updated {} events for {}ms max:{}ms", threadId, timeSinceLastUpdate, maxTimeBetweenUpdates);
                                mustRefreshPage.set(true);
                            }
                        } // end else
                    } else { // too little time passed
                    }
                } else { // program will enter on this branch when no updated events exist (nLiveEvents won't update); time < 20 minutes can activate during half_time
                    long timeSinceLastPageGet = startTime - lastPageGet.get();
                    if (timeSinceLastUpdate > Generic.MINUTE_LENGTH_MILLISECONDS * 30L ||
                        (timeSinceLastUpdate > Generic.MINUTE_LENGTH_MILLISECONDS * 20L && timeSinceLastPageGet > Generic.MINUTE_LENGTH_MILLISECONDS * 30L)) {
                        logger.warn("no started events exist and no updated {} events for {}ms", threadId, timeSinceLastUpdate);
                        mustRefreshPage.set(true);
                    } else { // too little time passed or page get too recent
                    }
                }
            } else { // mustRefresh already true, no need to recheck
            }
        } else {
            logger.info("{} htmlDivisionFootball not found in getScraperEvents", threadId);
            if (majorScrapingError.get() >= 1) {
                majorScrapingError.set(0);
                mustRefreshPage.set(true); // the page might not be loaded properly
            } else {
                majorScrapingError.incrementAndGet();
            }
        }
    }

    @Override
    public HtmlPage getHtmlPage(WebClient webClient) {
        HtmlPage htmlPage = WebScraperMethods.getPage(webClient, SAVE_FOLDER + "/start", mustRefreshPage, mustSavePage,
                                                      "http://livescore.betradar.com/ls/livescore/?/betfair/en/page", threadId, selectFootballXPath, orderByKickoffXPath, tickerZeroXPath);
        //                onclick="SRLive.trigger('treefilter:select', { _id: '1', selected: { type: 'sports', property: '_sid', value: '1' }})"
        //                onclick="SRLive.trigger('treefilter:select', { _id: '1', selected: { type: 'sports', property: '_sid', value: '1' }})"
        //                onclick="SRLive.trigger('treefilter:select', { _id: '1', selected: { type: 'sports', property: '_sid', value: '1' }})"
        //                onclick="SRLive.trigger('treefilter:select', { _id: '5', selected: { type: 'sports', property: '_sid', value: '5' }})"

        boolean hasError = false;

        if (htmlPage != null) {
            boolean indexFootballSelected;
            // "tree-item active expanded tree-type-sports"
            // "tree-item active expanded live  tree-type-sports"
//            HtmlListItem htmlListItem = htmlPage.getFirstByXPath("//li[@class='tree-item active expanded live  tree-type-sports' and @id='tree-_sid-1']");
            HtmlListItem htmlListItem =
                    htmlPage.getFirstByXPath("//li[starts-with(@class, 'tree-item active expanded ') and ends-with(@class, ' tree-type-sports') and @id='tree-_sid-1']");
            if (htmlListItem == null) {
                logger.warn("indexFootballSelected not found");
                indexFootballSelected = false;
                hasError = true;
            } else {
                indexFootballSelected = true;
            }

            boolean indexOrderByKickoff;
            htmlListItem = htmlPage.getFirstByXPath("//li[contains(@class, 'srlive-list-item-kickoff active')]");
            if (htmlListItem == null) {
                logger.warn("indexOrderByKickoff not found");
                indexOrderByKickoff = false;
                hasError = true;
            } else {
                indexOrderByKickoff = true;
            }

            boolean indexTickerZero;
            htmlListItem = htmlPage.getFirstByXPath("//li[contains(@class, 'srlive-list-item-0 active')]");
            if (htmlListItem == null) {
                logger.warn("indexTickerZero not found");
                indexTickerZero = false;
                hasError = true;
            } else {
                indexTickerZero = true;
            }

            if (hasError) {
                logger.warn("error found in {} getHtmlPage: {} {} {}", threadId, indexFootballSelected, indexOrderByKickoff, indexTickerZero);
                if (!indexFootballSelected) {
                    WebScraperMethods.clickElements(webClient, mustRefreshPage, htmlPage, 5_000L, selectFootballXPath, threadId, SAVE_FOLDER + "/click", mustSavePage);
                }
                if (!indexOrderByKickoff) {
                    WebScraperMethods.clickElements(webClient, mustRefreshPage, htmlPage, 5_000L, orderByKickoffXPath, threadId, SAVE_FOLDER + "/click", mustSavePage);
                }
                if (!indexTickerZero) {
                    WebScraperMethods.clickElements(webClient, mustRefreshPage, htmlPage, 5_000L, tickerZeroXPath, threadId, SAVE_FOLDER + "/click", mustSavePage);
                }

                hasError = false;
                htmlListItem = htmlPage.getFirstByXPath("//li[@class='tree-item active expanded live  tree-type-sports' and @id='tree-_sid-1']");
                if (htmlListItem == null) {
                    logger.warn("indexFootballSelected not found second time");
                    hasError = true;
                }

                htmlListItem = htmlPage.getFirstByXPath("//li[contains(@class, 'srlive-list-item-kickoff active')]");
                if (htmlListItem == null) {
                    logger.warn("indexOrderByKickoff not found second time");
                    hasError = true;
                }

                htmlListItem = htmlPage.getFirstByXPath("//li[contains(@class, 'srlive-list-item-0 active')]");
                if (htmlListItem == null) {
                    logger.warn("indexTickerZero not found second time");
                    hasError = true;
                }
            }
        } else {
            logger.error("null htmlPage in {} getHtmlPage", threadId);
            hasError = true;
        }

        if (hasError) {
            logger.warn("hasError in {} getHtmlPage, returning null page to retry", threadId);
            htmlPage = null; // returning null causes client close and another attempt
        } else {
            timeLastPageManipulation.set(System.currentTimeMillis());
        }

        return htmlPage;
    }
}
