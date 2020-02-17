package info.fmro.betty.threads.permanent;

import info.fmro.betty.main.Betty;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.objects.LoggerThreadInterface;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class LoggerThread
        implements LoggerThreadInterface, Runnable {
    private static final Logger logger = LoggerFactory.getLogger(LoggerThread.class);
    //    @NotNull
//    public static final Method addLogEntryMethod = Objects.requireNonNull(Generic.getMethod(LoggerThread.class, "addLogEntry", String.class, long.class));
    public static final long DEFAULT_TIME_OUT_TO_PRINT = 1_000L;
    private final HashMap<String, ArrayList<Long>> logEntries = new HashMap<>(2);
    private final Map<String, Long> logEntriesExpiration = new HashMap<>(2);

    public synchronized void addLogEntry(final String messageFormat, final long timeDifference) {
        final ArrayList<Long> list;
        if (this.logEntries.containsKey(messageFormat)) {
            list = this.logEntries.get(messageFormat);
        } else {
            list = new ArrayList<>(32);
            this.logEntries.put(messageFormat, list);
        }
        list.add(timeDifference);

        this.logEntriesExpiration.put(messageFormat, System.currentTimeMillis() + DEFAULT_TIME_OUT_TO_PRINT);
    }

    private synchronized void printEntry(final String messageFormat) {
        final ArrayList<Long> list = this.logEntries.remove(messageFormat);
        // logEntriesExpiration entry gets removed outside this method, using the Iterator
//        logEntriesExpiration.remove(messageFormat);
        if (list == null) {
            logger.error("null list in LoggerThread.printEntry for: {}", messageFormat);
        } else {
//            Collections.sort(list);
            final int nEntries = list.size();
            final long lowest = Collections.min(list);
            final long highest = Collections.max(list);
            final long sum = list.stream().mapToLong(Long::longValue).sum();
            final long average = Math.round((double) sum / nEntries);
            final String valuesString = "(nEntries:" + nEntries + " lowest:" + lowest + " average:" + average + " highest:" + highest + ")";
            final String formattedString = MessageFormatter.arrayFormat(messageFormat, new Object[]{valuesString}).getMessage();

            logger.info("loggerThread {}ms after last input: {}", DEFAULT_TIME_OUT_TO_PRINT, formattedString);
        }
    }

    private synchronized long checkEntries() {
        long timeToSleep = 0L;

        if (this.logEntriesExpiration.isEmpty()) { // nothing to be done, there are no entries
        } else {
            final long currentTime = System.currentTimeMillis();
            final Set<Entry<String, Long>> entrySet = this.logEntriesExpiration.entrySet();
            final Iterator<Entry<String, Long>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                final Entry<String, Long> entry = iterator.next();
                final long timeToExecute = entry.getValue();
                if (timeToExecute < currentTime) {
                    final String messageFormat = entry.getKey();
                    printEntry(messageFormat);
                    iterator.remove();
                } else {
                    final long timeLeft = timeToExecute - currentTime;
                    timeToSleep = Math.max(timeToSleep, timeLeft);
                }
            } // end while
        }
        if (timeToSleep == 0L) {
            timeToSleep = LoggerThread.DEFAULT_TIME_OUT_TO_PRINT;
        }
        return timeToSleep;
    }

    private synchronized void finalPrintEntries() {
        if (this.logEntriesExpiration.isEmpty()) { // nothing to be done, there are no entries
        } else {
            final long currentTime = System.currentTimeMillis();
            final Set<Entry<String, Long>> entrySet = this.logEntriesExpiration.entrySet();
            final Iterator<Entry<String, Long>> iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                final Entry<String, Long> entry = iterator.next();
                final long timeToExecute = entry.getValue();
                final long timeLeft = timeToExecute - currentTime; // can be negative
                if (timeLeft < -1_000L) {
                    logger.error("LoggerThread.finalPrintEntries, printing entry with negative timeLeft {}ms", timeLeft);
                } else {
                    logger.warn("LoggerThread.finalPrintEntries, printing entry with timeLeft {}ms", timeLeft);
                }

                final String messageFormat = entry.getKey();
                printEntry(messageFormat);
                iterator.remove();
            } // end while
        }
    }

    @Override
    public void run() {
        while (!Statics.mustStop.get()) {
            try {
                if (Statics.mustSleep.get()) {
                    Betty.programSleeps(Statics.mustSleep, Statics.mustStop, "logger thread");
                }

                final long timeToSleep = this.checkEntries();
                Generic.threadSleepSegmented(timeToSleep, 100L, Statics.mustStop);
            } catch (Throwable throwable) {
                logger.error("STRANGE ERROR inside LoggerThread loop", throwable);
            }
        } // end while
        this.finalPrintEntries();

        logger.info("LoggerThread ends");
    }
}
