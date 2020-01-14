package info.fmro.betty.safebet;

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
import info.fmro.shared.enums.CommandType;
import info.fmro.shared.enums.MatchStatus;
import info.fmro.betty.objects.Statics;
import info.fmro.betty.threads.LaunchCommandThread;
import info.fmro.betty.utility.WebScraperMethods;
import info.fmro.shared.utility.Generic;
import info.fmro.shared.utility.LogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings({"OverlyComplexClass", "SpellCheckingInspection"})
public class CoralScraperThread
        extends ScraperPermanentThread {
    private static final Logger logger = LoggerFactory.getLogger(CoralScraperThread.class);
    public static final long pageRefreshPeriod = Generic.MINUTE_LENGTH_MILLISECONDS * 5L;
    public static final String symbol_160 = (new StringBuilder(1)).appendCodePoint(160).toString();
    private boolean shouldChangeCheckboxes;

    public CoralScraperThread() {
        super("coral", 500L, BrowserVersion.FIREFOX_60, 200, true);
    }

    @SuppressWarnings("NestedTryStatement")
    private CoralEvent scrapeEventName(final long startTime, @NotNull final BookMark bookMark) {
        @Nullable CoralEvent scraperEvent;
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
                            logger.error("{} eventNameString not found: {}", this.threadId, indexEventNameString);
                            scraperEvent = null;
                        }
                    } catch (NumberFormatException numberFormatException) {
                        logger.error("{} numberFormatException while getting eventId from: {}", this.threadId, eventIdString, numberFormatException);
                        scraperEvent = null;
                    }
                } else {
                    logger.error("{} indexIdAttributeVal not found in getScraperEvents", this.threadId);
                    scraperEvent = null;
                }
            } else {
//                if (Statics.debugLevel.check(2, 189)) {
                logger.error("{} indexEventNameSpan not found: {}", this.threadId, indexSpanEventName);
//                }
                scraperEvent = null;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot scrapeEventName", this.threadId, xPathParseException);
            scraperEvent = null;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot scrapeEventName", this.threadId, xPathEvalException);
            scraperEvent = null;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot scrapeEventName", this.threadId, navException);
            scraperEvent = null;
        }
        return scraperEvent;
    }

    @SuppressWarnings({"OverlyNestedMethod", "NestedTryStatement"})
    private boolean scrapeScore(@NotNull final CoralEvent scraperEvent, @NotNull final BookMark bookMark, final AtomicInteger knownCauseForFailure) {
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
                            logger.error("{} numberFormatException in scrapeScore from: {}", this.threadId, scoreString, numberFormatException);
                            success = false;
                        }
                    } else {
                        if (scoreString.isEmpty()) {
                            if (Statics.debugLevel.check(2, 190)) {
                                logger.warn("{} scoreString empty", this.threadId);
                            }
                            knownCauseForFailure.addAndGet(1);
                        } else if ("LIVE".equals(scoreString)) {
                            if (Statics.debugLevel.check(2, 198)) {
                                logger.info("{} scoreString equals LIVE", this.threadId);
                            }
                            knownCauseForFailure.addAndGet(2);
                        } else {
                            logger.error("{} scoreString has no space: {}", this.threadId, scoreString);
                        }
                        success = false;
                    } // end else
//                    } else {
//                        logger.error("{} scoreString has {} quotes: {}", threadId, nQuotes, scoreString);
//                        success = false;
//                    }
                } else {
                    logger.error("{} scoreString not found", this.threadId);
                    success = false;
                }
            } else {
                logger.error("{} DivScore not found", this.threadId);
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot scrapeScore", this.threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot scrapeScore", this.threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot scrapeScore", this.threadId, navException);
            success = false;
        }
        return success;
    }

    @SuppressWarnings({"OverlyNestedMethod", "NestedTryStatement"})
    private boolean scrapeTime(final CoralEvent scraperEvent, @NotNull final BookMark bookMark) {
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
                    if (timeString.isEmpty()) { // no match time information present
                        scraperEvent.setMinutesPlayed(-1);
                        scraperEvent.setSeconds(-1);
                        success = true;
                    } else {
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
                                logger.error("{} numberFormatException in scrapeTime from: {}", this.threadId, timeString, numberFormatException);
                                success = false;
                            }
                        } else {
                            logger.error("{} timeString has no divider: {}", this.threadId, timeString);
                            success = false;
                        }
                    }
//                    } else {
//                        logger.error("{} timeString has {} quotes: {}", threadId, nQuotes, timeString);
//                        success = false;
//                    }
                } else {
                    logger.error("{} timeString not found", this.threadId);
                    success = false;
                }
            } else {
//                if (Statics.debugLevel.check(2, 191)) {
                logger.error("{} SpanTime not found", this.threadId);
//                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot scrapeTime", this.threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot scrapeTime", this.threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot scrapeTime", this.threadId, navException);
            success = false;
        }
        return success;
    }

    @SuppressWarnings("OverlyNestedMethod")
    private boolean scrapePeriod(final CoralEvent scraperEvent, @NotNull final BookMark bookMark) {
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
                    if (periodString.isEmpty()) { // no period string present
                        scraperEvent.setMatchStatus(null);
                    } else {
                        @Nullable final MatchStatus matchStatus;
                        switch (periodString) {
                            case "&nbsp;":
                                matchStatus = null; // is accepted for Coral; however resetting a known status to null is not, and this is checked for in setMatchStatus
                                break;
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
                            default:
                                matchStatus = MatchStatus.UNKNOWN;
                                logger.error("{} unknown periodString in scrapePeriod: {} {} {}", this.threadId, periodString, Generic.getStringCodePointValues(periodString), Generic.objectToString(scraperEvent));

                                final long currentTime = System.currentTimeMillis();
                                synchronized (this.lastTimedPageSave) { // synchronized not necessary for now, added just in case
                                    if (currentTime - this.lastTimedPageSave.get() > Generic.MINUTE_LENGTH_MILLISECONDS * 5L) {
                                        this.lastTimedPageSave.set(currentTime);
                                        this.mustSavePage.set(true);
                                    }
                                } // end synchronized
                                break;
                        } // end switch
                        scraperEvent.setMatchStatus(matchStatus);
                    }

                    success = true;
                } else {
                    logger.error("{} periodString not found", this.threadId);
                    success = false;
                }
            } else {
//                if (Statics.debugLevel.check(2, 192)) {
                logger.error("{} SpanPeriod not found", this.threadId);
//                }
                success = false;
            }
        } catch (XPathParseException xPathParseException) {
            logger.error("{} xPathParseException in autoPilot scrapePeriod", this.threadId, xPathParseException);
            success = false;
        } catch (XPathEvalException xPathEvalException) {
            logger.error("{} xPathEvalException in autoPilot scrapePeriod", this.threadId, xPathEvalException);
            success = false;
        } catch (NavException navException) {
            logger.error("{} navException in autoPilot scrapePeriod", this.threadId, navException);
            success = false;
        }
        return success;
    }

    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod", "OverlyNestedMethod"})
    @Override
    public void getScraperEventsInner(final long startTime, final boolean fullRun, final boolean checkAll, final AutoPilot autoPilot, final VTDNav vtdNav, final AtomicInteger listSize, final AtomicInteger scrapedEventsCounter)
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
                            if (idString.isEmpty()) {
                                logger.error("{} empty idString", this.threadId);
                            } else {
                                final int indexChecked = vtdNav.getAttrVal("checked");
                                if ("football".equals(idString)) {
                                    if (indexChecked >= 0) { // is checked, nothing to be done
                                    } else {
                                        logger.info("{} indexChecked {} negative for {} , will change checkboxes", this.threadId, indexChecked, idString);
                                        this.shouldChangeCheckboxes = true;
                                    }
                                } else { // not football
                                    if (indexChecked >= 0) {
                                        logger.info("{} indexChecked {} for {} , will change checkboxes", this.threadId, indexChecked, idString);
                                        this.shouldChangeCheckboxes = true;
                                    } else { // not checked, nothing to be done
                                    }
                                } // end else
                            }
                        } else {
                            logger.error("{} null idString", this.threadId);
                        }
                    } else {
                        logger.error("{} no idString", this.threadId);
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
            this.majorScrapingError.set(0); // page seems to be loaded fine

            // <div id="live_list_mkt_34675825" class="match first last suspended">
            autoPilot.selectXPath(".//div[contains(@class, 'match') and contains(@id, 'live_list_mkt_')]");

            final List<BookMark> trBookMarksList = new ArrayList<>(this.listSizeMaxValue.getValue());

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
            this.listSizeMaxValue.setValue(whileCounter, startTime);

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
                        // @SuppressWarnings("null") // null not possible because of eventId >= 0 check
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
                                    this.lastUpdatedScraperEvent.set(startTime); // event has been added
//                                                } else {
//                                                    logger.error("{} won't add blackListed scraperEvent: {}", threadId, eventId);
//                                                }
                                } else {
                                    logger.error("existingScraperEvent found during {} put double check: {} {}", this.threadId, Generic.objectToString(existingScraperEvent),
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
                                    this.lastUpdatedScraperEvent.set(startTime); // updated scraperEvent, not necessarely attached to a betfair event
                                    if (matchedEventId != null) {
                                        final Event event = Statics.eventsMap.get(matchedEventId);
                                        if (event != null) {
                                            if (event.isIgnored()) { // won't do anything about ignored events
                                            } else {
                                                eventsAttachedToModifiedScraperEvents.add(event);
                                            }
                                        } else {
                                            final long timeSinceLastRemoved = startTime - Statics.eventsMap.getTimeStampRemoved();
                                            // will not remove; the error message should suffice
//                                            Statics.eventsMap.remove(matchedEventId);
//                                            Statics.coralEventsMap.remove(eventId);

                                            final String printedString = MessageFormatter.arrayFormat(
                                                    "{} null event in map, timeSinceLastRemoved: {} for matchedEventId: {} of scraperEvent: {} {}",
                                                    new Object[]{this.threadId, timeSinceLastRemoved, matchedEventId, Generic.objectToString(scraperEvent),
                                                                 Generic.objectToString(existingScraperEvent)}).getMessage();
                                            if (timeSinceLastRemoved < 1_000L) {
                                                logger.info("{} null event in map timeSinceLastRemoved {} for matchedEventId {} of scraperEventId {}", this.threadId,
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
                                                                          this.threadId, existingScraperErrors,
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
                                logger.info("{} scraperEvent parsed: {}", this.threadId, Generic.objectToString(scraperEvent));
                            }
                        } else {
                            final String scraperString = Generic.objectToString(scraperEvent, "seconds", "classModifiers", "minutesPlayed", "stoppageTime", "Stamp");

                            if (scraperErrors >= 100) {
//                                logger.error("{} scraperEvent scraperErrors: {} in getScraperEvents for: {}", threadId, scraperErrors, scraperString);
                                Generic.alreadyPrintedMap.logOnce(5L * Generic.MINUTE_LENGTH_MILLISECONDS, logger, LogLevel.ERROR, "{} scraperEvent scraperErrors: {} in getScraperEvents for: {}", this.threadId, scraperErrors, scraperString);
                            } else { // scraperErrors >= 1
                                final long currentTime = System.currentTimeMillis();

                                if (Statics.timedWarningsMap.containsKey(scraperString)) {
                                    final long existingTime = Statics.timedWarningsMap.get(scraperString);
                                    final long lapsedTime = currentTime - existingTime;

                                    if (lapsedTime > Generic.MINUTE_LENGTH_MILLISECONDS) { // temporary errors allowed for at most 1 minute
                                        logger.error("{} scraperEvent minor but lasting {} ms scraperErrors: {} in getScraperEvents for: {}", this.threadId,
                                                     lapsedTime, scraperErrors, scraperString);
                                    } else { // might be a normal temporary error; not enough time has lapsed
                                    }
                                } else { // first time this particular error is found
                                    Statics.timedWarningsMap.put(scraperString, currentTime);
                                }
                            }
                        } // end else
                    } else {
                        if (knownCauseForFailure.get() > 0) {
                            if (Statics.debugLevel.check(2, 199)) {
                                logger.warn("{} scraperEvent not scraped properly: {}", this.threadId, Generic.objectToString(scraperEvent));
                            }
                        } else {
//                                if (Statics.debugLevel.check(2, 194)) {
                            logger.error("{} scraperEvent not scraped properly: {}", this.threadId, Generic.objectToString(scraperEvent));
//                                }
                        }
                    }
                } else { // blackListed or negative scraperId
                }
            } // end for

            Statics.coralEventsMap.timeStamp();

            final int sizeAdded = addedScraperEvents.size();
            if (sizeAdded > 0) {
                logger.info("{} getScraperEvents addedScraperEvents: {} launch: mapEventsToScraperEvents", this.threadId, sizeAdded);
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.mapEventsToScraperEvents, addedScraperEvents, CoralEvent.class));
            }
            final int sizeEvents = eventsAttachedToModifiedScraperEvents.size();
            if (sizeEvents > 0) {
                logger.info("{} getScraperEvents toCheckEvents: {} launch: findSafeRunners", this.threadId, sizeEvents);
                Statics.threadPoolExecutor.execute(new LaunchCommandThread(CommandType.findSafeRunners, eventsAttachedToModifiedScraperEvents));
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
        @Nullable final String savePrefix;
        final long currentTime = System.currentTimeMillis();
        synchronized (this.lastTimedPageSave) { // synchronized not necessary for now, added just in case
            if (currentTime - this.lastTimedPageSave.get() > Generic.MINUTE_LENGTH_MILLISECONDS * 60L) {
                this.lastTimedPageSave.set(currentTime);
                savePrefix = super.endSavePrefix();
            } else {
                savePrefix = null; // avoids saving
            }
        } // end synchronized

        return savePrefix;
    }

    @Override
    public HtmlPage getHtmlPage(final WebClient webClient) {
        @Nullable final String savePrefix;
        final long currentTime = System.currentTimeMillis();
        synchronized (this.lastTimedPageSave) { // synchronized not necessary for now, added just in case
            if (currentTime - this.lastTimedPageSave.get() > Generic.MINUTE_LENGTH_MILLISECONDS * 60L) {
                this.lastTimedPageSave.set(currentTime);
                savePrefix = this.saveFolder + "/start";
            } else {
                savePrefix = null; // avoids saving
            }
        } // end synchronized

        final HtmlPage htmlPage = WebScraperMethods.getPage(webClient, savePrefix, this.mustRefreshPage, this.mustSavePage, 25_000L, -1L, 2_000L,
                                                            "http://sports.coral.co.uk/betinplay/list/now/football", this.threadId,
                                                            "//a[@title='Decimal' and @id='site_pref_decimal']", // select decimal odds
                                                            WebScraperMethods.ONLY_PRINT_INFO_IF_FAIL_PREFIX + "//img[@class='show' and @id='perform-show' and @alt='Show']", // hide liveStream
                                                            "//h2[@class='col-block-title']"); // hides a lot of extra unimportant frames
        try {
            checkBoxes(webClient, htmlPage);
        } catch (IOException iOException) {
            logger.error("{} iOException while clicking checkBoxes in getHtmlPage", this.threadId, iOException);
        }
        WebScraperMethods.waitForScripts(webClient, 5_000L, this.threadId); // extra wait in case of coral, to have the page properly update

        return htmlPage;
    }

    @Override
    public void pageManipulation(final WebClient webClient, final HtmlPage htmlPage)
            throws IOException {
        // condition check for this is in the DOM parser
        if (this.shouldChangeCheckboxes) {
            this.shouldChangeCheckboxes = false; // if error and work not done, it will become true again during checkAll
            checkBoxes(webClient, htmlPage);

            if (this.shouldChangeCheckboxes) {
                logger.error("shouldChangeCheckboxes during {} pageManipulation; this should have been solved during initial page load", this.threadId);
            }
        } // end if shouldChangeCheckboxes
    }

    private void checkBoxes(final WebClient webClient, final HtmlPage htmlPage)
            throws IOException {
        if (htmlPage != null) {
            final List<?> htmlCheckBoxInputs = htmlPage.getByXPath("//input[@class='other-filters' and @name='sportfilter' and @type='checkbox']");
            boolean clicked = false;
            for (final Object object : htmlCheckBoxInputs) {
                final HtmlCheckBoxInput htmlCheckBoxInput = (HtmlCheckBoxInput) object;
                final String id = htmlCheckBoxInput.getAttribute("id");

                if ("football".equals(id)) {
                    if (htmlCheckBoxInput.isChecked()) { // nothing to be done
                    } else {
                        htmlCheckBoxInput.click();
                        logger.info("htmlCheckBoxInput football: {}", htmlCheckBoxInput.asXml());
                        clicked = true;
                    }
                } else if (htmlCheckBoxInput.isChecked()) {
                    htmlCheckBoxInput.click();
                    logger.info("htmlCheckBoxInput: {}", htmlCheckBoxInput.asXml());
                    clicked = true;
                } else { // nothing to be done
                }
            } // end for
            if (clicked) {
                WebScraperMethods.waitForScripts(webClient, 5_000L, this.threadId);
//            WebScraperMethods.savePage(htmlPage, mustSavePage, SAVE_FOLDER + "/afterClick", threadId);
            }
        } else {
            logger.error("null htmlPage in checkBoxes");
        }
    }
}
