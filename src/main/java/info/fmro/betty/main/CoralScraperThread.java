package info.fmro.betty.main;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.ximpleware.AutoPilot;
import com.ximpleware.BookMark;
import com.ximpleware.NavException;
import com.ximpleware.VTDNav;
import com.ximpleware.XPathEvalException;
import com.ximpleware.XPathParseException;
import info.fmro.betty.entities.Event;
import info.fmro.betty.enums.MatchStatus;
import info.fmro.betty.objects.CoralEvent;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.utility.WebScraperMethods;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

public class CoralScraperThread
        extends ScraperThread {

    private static final Logger logger = LoggerFactory.getLogger(CoralScraperThread.class);
    public static final long pageRefreshPeriod = Generic.MINUTE_LENGTH_MILLISECONDS * 5L;
    public static final String symbol_160 = (new StringBuilder(1)).appendCodePoint(160).toString();
    private boolean shouldChangeCheckboxes;

    public CoralScraperThread() {
        super("coral", 500L, BrowserVersion.FIREFOX_52, 200, true);
    }

    public CoralEvent scrapeEventName(long startTime, BookMark bookMark) {
        CoralEvent scraperEvent;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            // <span id="listlive_evname_2200079">FC Ulisses Yerevan 2 v FC Alashkert 2</span>
//            autoPilot.selectXPath("./span[@id='listlive_evname_" + scraperEvent.getEventId() + "']");
            autoPilot.selectXPath(".//span[contains(@id, 'listlive_evname_')]");
            final int indexSpanEventName = autoPilot.evalXPath();

            if (indexSpanEventName >= 0) {
                final int indexIdAttributeVal = vtdNav.getAttrVal("id");
                if (indexIdAttributeVal >= 0) {
                    final String eventIdString = vtdNav.toString(indexIdAttributeVal);
                    try {
                        final long eventId = Long.valueOf(eventIdString.substring(eventIdString.indexOf("listlive_evname_") + "listlive_evname_".length()));
                        final int indexEventNameString = vtdNav.getText();
                        if (indexEventNameString >= 0) {
                            String eventName = vtdNav.toString(indexEventNameString);
                            eventName = eventName.trim();
                            scraperEvent = new CoralEvent(eventId, startTime);
                            scraperEvent.setEventName(eventName);
                        } else {
                            logger.error("{} eventNameString not found: {}", threadId, indexEventNameString);
                            scraperEvent = null;
                        }
                    } catch (NumberFormatException numberFormatException) {
                        logger.error("{} numberFormatException while getting eventId from: {}", threadId, eventIdString, numberFormatException);
                        scraperEvent = null;
                    }
                } else {
                    logger.error("{} indexIdAttributeVal not found in getScraperEvents", threadId);
                    scraperEvent = null;
                }
            } else {
//                if (Statics.debugLevel.check(2, 189)) {
                logger.error("{} indexEventNameSpan not found: {}", threadId, indexSpanEventName);
//                }
                scraperEvent = null;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot scrapeEventName", threadId, xPathParseException);
            scraperEvent = null;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot scrapeEventName", threadId, xPathEvalException);
            scraperEvent = null;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot scrapeEventName", threadId, navException);
            scraperEvent = null;
        }
        return scraperEvent;
    }

    public boolean scrapeScore(CoralEvent scraperEvent, BookMark bookMark, AtomicInteger knownCauseForFailure) {
        boolean success;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            // <div id="listlive_com_score_2200177" class="current-score  ">2 - 3</div>
            autoPilot.selectXPath(".//div[@id='listlive_com_score_" + scraperEvent.getEventId() + "']");
            final int indexDivScore = autoPilot.evalXPath();

            if (indexDivScore >= 0) {
                final int indexScoreString = vtdNav.getText();
                if (indexScoreString >= 0) {
                    String scoreString = vtdNav.toString(indexScoreString);
//                    final int nQuotes = StringUtils.countMatches(scoreString, "\"");
//                    if (nQuotes == 2) {
//                        scoreString = scoreString.substring(scoreString.indexOf('"') + "\"".length(), scoreString.lastIndexOf('"'));
                    scoreString = scoreString.trim();
                    if (scoreString.contains(" ")) {
                        final String scoreHomeString = scoreString.substring(0, scoreString.indexOf(' '));
                        final String scoreAwayString = scoreString.substring(scoreString.lastIndexOf(' ') + " ".length());
                        try {
                            final int scoreHome = Integer.parseInt(scoreHomeString);
                            scraperEvent.setHomeScore(scoreHome);
                            final int scoreAway = Integer.parseInt(scoreAwayString);
                            scraperEvent.setAwayScore(scoreAway);
                            success = true;
                        } catch (NumberFormatException numberFormatException) {
                            logger.error("{} numberFormatException in scrapeScore from: {}", threadId, scoreString, numberFormatException);
                            success = false;
                        }
                    } else {
                        if (scoreString.isEmpty()) {
                            if (Statics.debugLevel.check(2, 190)) {
                                logger.warn("{} scoreString empty", threadId);
                            }
                            knownCauseForFailure.addAndGet(1);
                        } else if (scoreString.equals("LIVE")) {
                            if (Statics.debugLevel.check(2, 198)) {
                                logger.info("{} scoreString equals LIVE", threadId);
                            }
                            knownCauseForFailure.addAndGet(2);
                        } else {
                            logger.error("{} scoreString has no space: {}", threadId, scoreString);
                        }
                        success = false;
                    } // end else
//                    } else {
//                        logger.error("{} scoreString has {} quotes: {}", threadId, nQuotes, scoreString);
//                        success = false;
//                    }
                } else {
                    logger.error("{} scoreString not found", threadId);
                    success = false;
                }
            } else {
                logger.error("{} DivScore not found", threadId);
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot scrapeScore", threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot scrapeScore", threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot scrapeScore", threadId, navException);
            success = false;
        }
        return success;
    }

    public boolean scrapeTime(CoralEvent scraperEvent, BookMark bookMark) {
        boolean success;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            // <span class="bip-hdr-time">184:58</span>
            autoPilot.selectXPath(".//span[@class='bip-hdr-time']");
            final int indexSpanTime = autoPilot.evalXPath();

            if (indexSpanTime >= 0) {
                final int indexTimeString = vtdNav.getText();
                if (indexTimeString >= 0) {
                    String timeString = vtdNav.toString(indexTimeString);
//                    final int nQuotes = StringUtils.countMatches(timeString, "\"");
//                    if (nQuotes == 2) {
//                        timeString = timeString.substring(timeString.indexOf('"') + "\"".length(), timeString.lastIndexOf('"'));
                    timeString = timeString.trim();
                    if (!timeString.isEmpty()) {
                        if (timeString.contains(":")) {
                            final String minutesString = timeString.substring(0, timeString.indexOf(':'));
                            final String secondsString = timeString.substring(timeString.lastIndexOf(':') + ":".length());
                            try {
                                final int minutes = Integer.parseInt(minutesString);
                                scraperEvent.setMinutesPlayed(minutes);
                                final int seconds = Integer.parseInt(secondsString);
                                scraperEvent.setSeconds(seconds);
                                success = true;
                            } catch (NumberFormatException numberFormatException) {
                                logger.error("{} numberFormatException in scrapeTime from: {}", threadId, timeString, numberFormatException);
                                success = false;
                            }
                        } else {
                            logger.error("{} timeString has no divider: {}", threadId, timeString);
                            success = false;
                        }
                    } else { // no match time information present
                        scraperEvent.setMinutesPlayed(-1);
                        scraperEvent.setSeconds(-1);
                        success = true;
                    }
//                    } else {
//                        logger.error("{} timeString has {} quotes: {}", threadId, nQuotes, timeString);
//                        success = false;
//                    }
                } else {
                    logger.error("{} timeString not found", threadId);
                    success = false;
                }
            } else {
//                if (Statics.debugLevel.check(2, 191)) {
                logger.error("{} SpanTime not found", threadId);
//                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot scrapeTime", threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot scrapeTime", threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot scrapeTime", threadId, navException);
            success = false;
        }
        return success;
    }

    public boolean scrapePeriod(CoralEvent scraperEvent, BookMark bookMark) {
        boolean success;
        bookMark.setCursorPosition();
        final VTDNav vtdNav = bookMark.getNav();
        final AutoPilot autoPilot = new AutoPilot(vtdNav);
//        autoPilot.selectElementNS("*", "*");

        try {
            // <span class="bip-hdr-period short">2H</span>
            autoPilot.selectXPath(".//span[@class='bip-hdr-period short']");
            final int indexSpanPeriod = autoPilot.evalXPath();

            if (indexSpanPeriod >= 0) {
                final int indexPeriodString = vtdNav.getText();
                if (indexPeriodString >= 0) {
                    String periodString = vtdNav.toString(indexPeriodString);
//                    final int nQuotes = StringUtils.countMatches(timeString, "\"");
//                    if (nQuotes == 2) {
//                        timeString = timeString.substring(timeString.indexOf('"') + "\"".length(), timeString.lastIndexOf('"'));
//                    final String symbol_160 = "\u00C2\u00A0";
//                    periodString = Generic.quotedReplaceAll(periodString, "Â ", ""); // removes special character 194+160, "non breaking space", which does happen in coral
                    periodString = Generic.quotedReplaceAll(periodString, symbol_160, ""); // removes special character 194+160, "non breaking space", which does happen in coral
                    periodString = periodString.trim();
                    if (!periodString.isEmpty()) {
                        MatchStatus matchStatus;
                        switch (periodString) {
                            case "":
                            case "&nbsp;":
//                                if (scraperEvent.getMinutesPlayed() == 0 && scraperEvent.getSeconds() == 0 && scraperEvent.getHomeScore() == 0 && scraperEvent.getAwayScore() == 0) {
//                                    // match hasn't started
//                                    matchStatus = MatchStatus.NOT_STARTED;
//                                } else {
//                                    matchStatus = MatchStatus.UNKNOWN;
//                                    logger.error("{} nbsp periodString for alreadyStarted in scrapePeriod: {} {}", threadId, periodString, Generic.objectToString(scraperEvent));
//
//                                    long currentTime = System.currentTimeMillis();
//                                    synchronized (lastTimedPageSave) { // synchronized not necessary for now, added just in case
//                                        if (currentTime - lastTimedPageSave.get() > Generic.MINUTE_LENGTH_MILLISECONDS * 5L) {
//                                            lastTimedPageSave.set(currentTime);
//                                            mustSavePage.set(true);
//                                        }
//                                    } // end synchronized
//                                }
                                matchStatus = null; // is accepted for Coral; however resetting a known status to null is not, and this is checked for in setMatchStatus
                                break;
//                            case "Abandoned":
//                                matchStatus = MatchStatus.ABANDONED;
//                                break;
//                            case "Postponed":
//                                matchStatus = MatchStatus.POSTPONED;
//                                break;
//                            case "Not started":
//                                matchStatus = MatchStatus.NOT_STARTED;
//                                break;
//                            case "Start delayed":
//                                matchStatus = MatchStatus.START_DELAYED;
//                                break;
                            case "1H":
                                matchStatus = MatchStatus.FIRST_HALF;
                                break;
                            case "HT":
                                matchStatus = MatchStatus.HALF_TIME;
                                break;
                            case "2H":
                                matchStatus = MatchStatus.SECOND_HALF;
                                break;
                            case "ET":
                                matchStatus = MatchStatus.OVERTIME;
                                break;
//                            case "Awaiting extra time":
//                                matchStatus = MatchStatus.AWAITING_ET;
//                                break;
//                            case "Overtime":
//                                matchStatus = MatchStatus.OVERTIME;
//                                break;
//                            case "1st extra":
//                                matchStatus = MatchStatus.FIRST_ET;
//                                break;
//                            case "Extra time halftime":
//                                matchStatus = MatchStatus.ET_HALF_TIME;
//                                break;
//                            case "2nd extra":
//                                matchStatus = MatchStatus.SECOND_ET;
//                                break;
//                            case "Awaiting penalties":
//                                matchStatus = MatchStatus.AWAITING_PEN;
//                                break;
//                            case "Penalties":
//                                matchStatus = MatchStatus.PENALTIES;
//                                break;
//                            case "AET":
//                                matchStatus = MatchStatus.AFTER_ET;
//                                break;
//                            case "AP":
//                                matchStatus = MatchStatus.AFTER_PEN;
//                                break;
//                            case "Ended":
//                                matchStatus = MatchStatus.ENDED;
//                                break;
//                            case "Interrupted":
//                                matchStatus = MatchStatus.INTERRUPTED;
//                                break;
//                            case "Cancelled":
//                                matchStatus = MatchStatus.CANCELLED;
//                                break;
                            default:
                                matchStatus = MatchStatus.UNKNOWN;
                                logger.error("{} unknown periodString in scrapePeriod: {} {} {}", threadId, periodString, Generic.getStringCodePointValues(periodString),
                                        Generic.objectToString(scraperEvent));

                                long currentTime = System.currentTimeMillis();
                                synchronized (lastTimedPageSave) { // synchronized not necessary for now, added just in case
                                    if (currentTime - lastTimedPageSave.get() > Generic.MINUTE_LENGTH_MILLISECONDS * 5L) {
                                        lastTimedPageSave.set(currentTime);
                                        mustSavePage.set(true);
                                    }
                                } // end synchronized
                                break;
                        } // end switch
                        scraperEvent.setMatchStatus(matchStatus);
                        success = true;
                    } else { // no period string present
//                        if (Statics.debugLevel.check(2, 192)) {
//                            logger.error("{} periodString empty", threadId);
//                        }
                        scraperEvent.setMatchStatus(null);
                        success = true;
                    }
//                    } else {
//                        logger.error("{} periodString has {} quotes: {}", threadId, nQuotes, timeString);
//                        success = false;
//                    }
                } else {
                    logger.error("{} periodString not found", threadId);
                    success = false;
                }
            } else {
//                if (Statics.debugLevel.check(2, 192)) {
                logger.error("{} SpanPeriod not found", threadId);
//                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot scrapePeriod", threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot scrapePeriod", threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot scrapePeriod", threadId, navException);
            success = false;
        }
        return success;
    }

    @Override
    public void getScraperEventsInner(long startTime, boolean fullRun, boolean checkAll, AutoPilot autoPilot, VTDNav vtdNav, AtomicInteger listSize,
            AtomicInteger scrapedEventsCounter)
            throws XPathParseException, XPathEvalException, NavException {
        if (checkAll) {
            if (startTime - this.lastPageGet.get() > CoralScraperThread.pageRefreshPeriod) { // refresh page once in a while
                this.mustRefreshPage.set(true);
            }

            autoPilot.selectXPath("//input[@class='other-filters' and @name='sportfilter' and @type='checkbox']");
            int trIndex;
            do {
                trIndex = autoPilot.evalXPath();
                if (trIndex >= 0) {
                    final int indexIdString = vtdNav.getAttrVal("id");
                    if (indexIdString >= 0) {
                        String idString = vtdNav.toString(indexIdString);
                        if (idString != null) {
                            idString = idString.trim();
                            if (!idString.isEmpty()) {
                                final int indexChecked = vtdNav.getAttrVal("checked");
                                if (idString.equals("football")) {
                                    if (indexChecked >= 0) { // is checked, nothing to be done
                                    } else {
                                        logger.info("{} indexChecked {} negative for {} , will change checkboxes", threadId, indexChecked, idString);
                                        this.shouldChangeCheckboxes = true;
                                    }
                                } else { // not football
                                    if (indexChecked >= 0) {
                                        logger.info("{} indexChecked {} for {} , will change checkboxes", threadId, indexChecked, idString);
                                        this.shouldChangeCheckboxes = true;
                                    } else { // not checked, nothing to be done
                                    }
                                } // end else
                            } else {
                                logger.error("{} empty idString", threadId);
                            }
                        } else {
                            logger.error("{} null idString", threadId);
                        }
                    } else {
                        logger.error("{} no idString", threadId);
                    }
                } else { // program will exit while, nothing to be done
                }
            } while (trIndex >= 0);
        } // end if checkAll

        // support for maintenance detection incomplete and unnecesary because of: check for htmlDivisionFootball and page refreshes every 15-20 minutes anyway
        // <meta name="DCSext.error" content="500 Error">
//        autoPilot.selectXPath("//meta[@name='DCSext.error' and @content='500 Error']");
//        final int indexMaintenance = autoPilot.evalXPath();
//        if (indexMaintenance >= 0) {
//            if (!mustRefreshPage.getAndSet(true)) { // will get printed once or twice (beginning always, end sometimes)
//                logger.info("{} maintenance dialog found", threadId);
//            }
//        } else { // no refresh needed
//        }
        // <div id="live_list_cat_types_16" class="matches">
        autoPilot.selectXPath("//div[@id='live_list_cat_types_16' and @class='matches']");
        final int indexDivisionFootball = autoPilot.evalXPath();
        if (indexDivisionFootball >= 0) {
            majorScrapingError.set(0); // page seems to be loaded fine

            // <div id="live_list_mkt_34675825" class="match first last suspended">
            autoPilot.selectXPath(".//div[contains(@class, 'match') and contains(@id, 'live_list_mkt_')]");

            final List<BookMark> trBookMarksList = new ArrayList<>(listSizeMaxValue.getValue());

            int trIndex, whileCounter = 0;
            do {
                trIndex = autoPilot.evalXPath();
                if (trIndex >= 0) {
                    whileCounter++;
                    final BookMark bookMark = new BookMark(vtdNav);
                    bookMark.recordCursorPosition();
                    trBookMarksList.add(bookMark);
                }
            } while (trIndex >= 0);
            listSizeMaxValue.setValue(whileCounter, startTime);

            listSize.set(trBookMarksList.size());
            final HashSet<Event> eventsAttachedToModifiedScraperEvents = new HashSet<>(0);
            final Set<CoralEvent> addedScraperEvents = new HashSet<>(0);
            final int listSizePrimitive = listSize.get();
            for (int i = 0; i < listSizePrimitive; i++) {
                final AtomicInteger knownCauseForFailure = new AtomicInteger();
                final BookMark bookMark = trBookMarksList.get(i);
//                bookMark.setCursorPosition();
//                final int indexIdAttributeVal = vtdNav.getAttrVal("id");
//                if (indexIdAttributeVal >= 0) {
//                    final String eventIdString = vtdNav.toString(indexIdAttributeVal);
//                    try {
                final CoralEvent scraperEvent = scrapeEventName(startTime, bookMark);
                final long eventId = scraperEvent == null ? -1 : scraperEvent.getEventId();
//                                Long.valueOf(eventIdString.substring(eventIdString.indexOf("live_list_mkt_") + "live_list_mkt_".length()));
//                if (eventId >= 0 && !BlackList.containsCoralId(eventId)) {
                if (eventId >= 0) {
//                            final CoralEvent scraperEvent = new CoralEvent(eventId, startTime);

//                            final boolean scrapeEventName = scrapeEventName(scraperEvent, bookMark);
//                    final boolean scrapeScore = scrapeScore(scraperEvent, bookMark, knownCauseForFailure);
//                    final boolean scrapeTime = scrapeTime(scraperEvent, bookMark);
//                    final boolean scrapePeriod = scrapePeriod(scraperEvent, bookMark);
                    if (scrapeScore(scraperEvent, bookMark, knownCauseForFailure) && scrapeTime(scraperEvent, bookMark) && scrapePeriod(scraperEvent, bookMark)) {
                        @SuppressWarnings("null") // null not possible because of eventId >= 0 check
                        final long scraperErrors = scraperEvent.errors();
                        if (scraperErrors <= 0L) {
                            scrapedEventsCounter.incrementAndGet();

                            final CoralEvent existingScraperEvent;
//                            synchronized (Statics.coralEventsMap) {
                            if (Statics.coralEventsMap.containsKey(eventId)) {
                                existingScraperEvent = Statics.coralEventsMap.get(eventId);
                            } else {
//                                            if (matchStatus != null && matchStatus.hasStarted()) {
//                                if (!BlackList.dummyScraperEvent.equals(BlackList.checkedPutScraper(eventId, scraperEvent))) {
                                existingScraperEvent = Statics.coralEventsMap.putIfAbsent(eventId, scraperEvent);
                                if (existingScraperEvent == null) { // event was added, no previous event existed, double check to avoid racing issues
//                                    existingScraperEvent = null;
                                    addedScraperEvents.add(scraperEvent);
                                    lastUpdatedScraperEvent.set(startTime); // event has been added
//                                                } else {
//                                                    logger.error("{} won't add blackListed scraperEvent: {}", threadId, eventId);
//                                                }
                                } else {
                                    logger.error("existingScraperEvent found during {} put double check: {} {}", threadId, Generic.objectToString(existingScraperEvent),
                                            Generic.objectToString(scraperEvent));
//                                    existingScraperEvent = inMapScraperEvent;
                                }
//                                            } else { // won't be added
//                                            }
                            } // end else
//                            } // end synchronized
                            if (existingScraperEvent != null) {
                                final String matchedEventId = existingScraperEvent.getMatchedEventId();
//                                scraperEvent.setMatchedEventId(matchedEventId); // placed before update, else error in update
                                existingScraperEvent.setSeconds(scraperEvent.getSeconds()); // placed before update, else update too often
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
                                            long timeSinceLastRemoved = startTime - Statics.eventsMap.getTimeStampRemoved();
                                            // will not remove; the error message should suffice
//                                            Statics.eventsMap.remove(matchedEventId);
//                                            Statics.coralEventsMap.remove(eventId);

                                            String printedString = MessageFormatter.arrayFormat(
                                                    "{} null event in map, timeSinceLastRemoved: {} for matchedEventId: {} of scraperEvent: {} {}",
                                                    new Object[]{threadId, timeSinceLastRemoved, matchedEventId, Generic.objectToString(scraperEvent),
                                                        Generic.objectToString(existingScraperEvent)}).getMessage();
                                            if (timeSinceLastRemoved < 1_000L) {
                                                logger.info("{} null event in map timeSinceLastRemoved {} for matchedEventId {} of scraperEventId {}", threadId,
                                                        timeSinceLastRemoved, matchedEventId, eventId);
                                            } else {
                                                logger.error(printedString);
                                            }
                                        }
                                    } else { // not matched scraper, no event to add for checking; no need to attempt matching again, only name is checked
                                    }
                                } // end if update
                                if (update >= 0) { // excludes update errors; in that case, scraper was already purged
                                    final long existingScraperErrors = existingScraperEvent.errors();
                                    if (existingScraperErrors > 0L) { // this sometimes, rarely, does happen
//                                        logger.error("{} check true scraperEvent updated into check false {} scraperEvent: {} {}", threadId, existingScraperErrors,
//                                                Generic.objectToString(scraperEvent), Generic.objectToString(existingScraperEvent));

                                        Generic.alreadyPrintedMap.logOnce(logger, LogLevel.ERROR, "{} check true scraperEvent updated into check false {} scraperEvent: {} {}",
                                                threadId, existingScraperErrors,
                                                Generic.objectToString(scraperEvent, "seconds", "classModifiers", "minutesPlayed", "stoppageTime", "Stamp"),
                                                Generic.objectToString(existingScraperEvent, "seconds", "classModifiers", "minutesPlayed", "stoppageTime", "Stamp"));

                                        // probably no need for removal, ignore is done when existingScraperEvent.errors() is invoked
//                                        BlackList.ignoreScraper(scraperEvent, 10_000L);
//                                        Statics.coralEventsMap.remove(eventId);
                                    }
                                } // end second if update 
                            } else { // no existing scraperEvent, nothing to be done about it
                            }

                            if (Statics.debugLevel.check(3, 193)) {
                                logger.info("{} scraperEvent parsed: {}", threadId, Generic.objectToString(scraperEvent));
                            }
                        } else {
                            final String scraperString = Generic.objectToString(scraperEvent, "seconds", "classModifiers", "minutesPlayed", "stoppageTime", "Stamp");

                            if (scraperErrors >= 100) {
//                                logger.error("{} scraperEvent scraperErrors: {} in getScraperEvents for: {}", threadId, scraperErrors, scraperString);
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
                                logger.error("STRANGE {} scraperErrors {} value for scraperEvent: {}", threadId, scraperErrors, Generic.objectToString(scraperEvent));
                            }
                        } // end else
                    } else {
                        if (knownCauseForFailure.get() > 0) {
                            if (Statics.debugLevel.check(2, 199)) {
                                logger.warn("{} scraperEvent not scraped properly: {}", threadId, Generic.objectToString(scraperEvent));
                            }
                        } else {
//                                if (Statics.debugLevel.check(2, 194)) {
                            logger.error("{} scraperEvent not scraped properly: {}", threadId, Generic.objectToString(scraperEvent));
//                                }
                        }
                    }
                } else { // blackListed or negative scraperId
                }
            } // end for

            Statics.coralEventsMap.timeStamp();

            final int sizeAdded = addedScraperEvents.size();
            if (sizeAdded > 0) {
                logger.info("{} getScraperEvents addedScraperEvents: {} launch: mapEventsToScraperEvents", threadId, sizeAdded);
                Statics.threadPoolExecutor.execute(new LaunchCommandThread("mapEventsToScraperEvents", addedScraperEvents, CoralEvent.class));
            }
            final int sizeEvents = eventsAttachedToModifiedScraperEvents.size();
            if (sizeEvents > 0) {
                logger.info("{} getScraperEvents toCheckEvents: {} launch: findSafeRunners", threadId, sizeEvents);
                Statics.threadPoolExecutor.execute(new LaunchCommandThread("findSafeRunners", eventsAttachedToModifiedScraperEvents));
            }
        } else {
            // support for htmlDivisionFootball not found not implemented because of: it happens normally when no football events live and page refreshes every 15-20 minutes anyway
//            logger.info("{} htmlDivisionFootball not found in getScraperEvents", threadId);
//            if (majorScrapingError.get() >= 1) {
//                majorScrapingError.set(0);
//                mustRefreshPage.set(true); // the page might not be loaded properly
//            } else {
//                majorScrapingError.incrementAndGet();
//            }
        }
    }

    @Override
    public String endSavePrefix() {
        String savePrefix;
        long currentTime = System.currentTimeMillis();
        synchronized (lastTimedPageSave) { // synchronized not necessary for now, added just in case
            if (currentTime - lastTimedPageSave.get() > Generic.MINUTE_LENGTH_MILLISECONDS * 60L) {
                lastTimedPageSave.set(currentTime);
                savePrefix = super.endSavePrefix();
            } else {
                savePrefix = null; // avoids saving
            }
        } // end synchronized

        return savePrefix;
    }

    @Override
    public HtmlPage getHtmlPage(WebClient webClient) {
        String savePrefix;
        long currentTime = System.currentTimeMillis();
        synchronized (lastTimedPageSave) { // synchronized not necessary for now, added just in case
            if (currentTime - lastTimedPageSave.get() > Generic.MINUTE_LENGTH_MILLISECONDS * 60L) {
                lastTimedPageSave.set(currentTime);
                savePrefix = SAVE_FOLDER + "/start";
            } else {
                savePrefix = null; // avoids saving
            }
        } // end synchronized

        HtmlPage htmlPage = WebScraperMethods.getPage(webClient, savePrefix, mustRefreshPage, mustSavePage, 25_000L, -1L, 2_000L,
                "http://sports.coral.co.uk/betinplay/list/now/football", threadId,
                "//a[@title='Decimal' and @id='site_pref_decimal']", // select decimal odds
                WebScraperMethods.ONLY_PRINT_INFO_IF_FAIL_PREFIX + "//img[@class='show' and @id='perform-show' and @alt='Show']", // hide liveStream
                "//h2[@class='col-block-title']"); // hides a lot of extra unimportant frames
        try {
            checkBoxes(webClient, htmlPage);
        } catch (IOException iOException) {
            logger.error("{} iOException while clicking checkBoxes in getHtmlPage", threadId, iOException);
        }
        WebScraperMethods.waitForScripts(webClient, 5_000L, threadId); // extra wait in case of coral, to have the page properly update

        return htmlPage;
    }

    @Override
    public void pageManipulation(WebClient webClient, HtmlPage htmlPage)
            throws IOException {
        // condition check for this is in the DOM parser
        if (this.shouldChangeCheckboxes) {
            this.shouldChangeCheckboxes = false; // if error and work not done, it will become true again during checkAll
            checkBoxes(webClient, htmlPage);

            if (this.shouldChangeCheckboxes) {
                logger.error("shouldChangeCheckboxes during {} pageManipulation; this should have been solved during initial page load", threadId);
            }
        } // end if shouldChangeCheckboxes
    }

    public void checkBoxes(WebClient webClient, HtmlPage htmlPage)
            throws IOException {
        if (htmlPage != null) {
            List<?> htmlCheckBoxInputs = htmlPage.getByXPath("//input[@class='other-filters' and @name='sportfilter' and @type='checkbox']");
            boolean clicked = false;
            for (Object object : htmlCheckBoxInputs) {
                HtmlCheckBoxInput htmlCheckBoxInput = (HtmlCheckBoxInput) object;
                String id = htmlCheckBoxInput.getAttribute("id");

                if (id.equals("football")) {
                    if (!htmlCheckBoxInput.isChecked()) {
                        htmlCheckBoxInput.click();
                        logger.info("htmlCheckBoxInput football: {}", htmlCheckBoxInput.asXml());
                        clicked = true;
                    } else { // nothing to be done
                    }
                } else if (htmlCheckBoxInput.isChecked()) {
                    htmlCheckBoxInput.click();
                    logger.info("htmlCheckBoxInput: {}", htmlCheckBoxInput.asXml());
                    clicked = true;
                } else { // nothing to be done
                }
            } // end for
            if (clicked) {
                WebScraperMethods.waitForScripts(webClient, 5_000L, threadId);
//            WebScraperMethods.savePage(htmlPage, mustSavePage, SAVE_FOLDER + "/afterClick", threadId);
            }
        } else {
            logger.error("null htmlPage in checkBoxes");
        }
    }
}
