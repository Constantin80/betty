package info.fmro.betty.objects;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AverageLogger {

    private static final Logger logger = LoggerFactory.getLogger(AverageLogger.class);
    private static final DecimalFormat decimalFormat = new DecimalFormat("#0.0#");
    private static final RoundingMode roundingMode = RoundingMode.DOWN;
    private final AverageLoggerInterface loggerInterface;
    private final String standardPattern, errorPattern;
    private final int nLists;
    private final ArrayList<ArrayList<Long>> lists;

    public AverageLogger(final AverageLoggerInterface loggerInterface, final String standardPattern, final String errorPattern, final int nLists) {
        this.loggerInterface = loggerInterface;
        this.standardPattern = standardPattern;
        this.errorPattern = errorPattern;
        this.nLists = nLists;
        this.lists = new ArrayList<>(this.nLists);
        for (int i = 0; i < this.nLists; i++) {
            this.lists.add(new ArrayList<>(0));
        }
    }

    static {
        decimalFormat.setRoundingMode(roundingMode);
    }

    public synchronized void addRecords(final long... records) {
        for (int i = 0; i < this.nLists; i++) {
            this.lists.get(i).add(records[i]);
        }
    }

    public synchronized void printRecords() {
        final int nRecords = lists.get(0).size();
        int expectedRuns = loggerInterface.getExpectedRuns();

        if (nRecords > 0) {
            Object[] objects = new Object[2 + 2 * this.nLists];
            objects[0] = nRecords;
            objects[1] = expectedRuns;
            for (int i = 0; i < this.nLists; i++) {
                long max = Long.MIN_VALUE;
                double average = 0d;
                ArrayList<Long> currentList = lists.get(i);
                for (long value : currentList) {
                    average += value;
                    if (value > max) {
                        max = value;
                    }
                } // end for
                average /= (double) nRecords;
                String averageString = decimalFormat.format(average);

                objects[2 + 2 * i] = averageString;
                objects[2 + 2 * i + 1] = max;

                currentList.clear();
            } // end for

            logger.info(standardPattern, objects);
        } else {
            logger.info(errorPattern, nRecords, expectedRuns);
        }
    }
}
