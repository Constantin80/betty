package info.fmro.betty.main;

import info.fmro.betty.objects.Statics;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FindSafeRunners {

    private static final Logger logger = LoggerFactory.getLogger(FindSafeRunners.class);
    @Rule
    @SuppressWarnings("PublicField")
    public TestRule watchman = new TestWatcher() {
        @Override
        public void starting(Description description) {
            logger.info("{} being run...", description.getMethodName());
        }
    };

    @Test
    public void testMarketSafeRunners() {
        // ANYTIME_SCORE
        for (int homeScore = 0; homeScore < 10; homeScore++) {
            for (int awayScore = 0; awayScore < 10; awayScore++) {
                int foundSafeRunners = 0;

                final int additionalBack;
                if (homeScore == 0 || awayScore == 0) {
                    additionalBack = Math.abs(Math.min(homeScore, 3) - Math.min(awayScore, 3)) - 1;
                } else {
                    additionalBack = 0;
                }
                final int additionalLay;
                if (additionalBack != 0) {
                    additionalLay = 0;
                } else if (homeScore >= 4 || awayScore >= 4) {
                    additionalLay = -1;
                } else {
                    additionalLay = 0;
                }
                final int nSafeRunners = 1 + additionalBack + additionalLay +
                        Math.min(homeScore, 4) * (3 - Math.min(awayScore, 3)) + Math.min(awayScore, 4) * (3 - Math.min(homeScore, 3));

                if (homeScore == 0 && awayScore >= 1) {
                    foundSafeRunners++;
                }
                if (homeScore > 0 && awayScore < 1) {
                    foundSafeRunners++;
                }

                if (homeScore == 0 && awayScore >= 2) {
                    foundSafeRunners++;
                }
                if (homeScore > 0 && awayScore < 2) {
                    foundSafeRunners++;
                }
                if (homeScore == 0 && awayScore >= 3) {
                    foundSafeRunners++;
                }
                if (homeScore > 0 && awayScore < 3) {
                    foundSafeRunners++;
                }
                if (homeScore >= 1 && awayScore == 0) {
                    foundSafeRunners++;
                }
                if (homeScore < 1 && awayScore > 0) {
                    foundSafeRunners++;
                }
                if (homeScore == 1 && awayScore == 1) {
                    foundSafeRunners++;
                }
                if ((homeScore < 1 && awayScore > 1) || (homeScore > 1 && awayScore < 1)) {
                    foundSafeRunners++;
                }
                if (homeScore == 1 && awayScore == 2) {
                    foundSafeRunners++;
                }
                if ((homeScore < 1 && awayScore > 2) || (homeScore > 1 && awayScore < 2)) {
                    foundSafeRunners++;
                }
                if (homeScore == 1 && awayScore == 3) {
                    foundSafeRunners++;
                }
                if ((homeScore < 1 && awayScore > 3) || (homeScore > 1 && awayScore < 3)) {
                    foundSafeRunners++;
                }
                if (homeScore >= 2 && awayScore == 0) {
                    foundSafeRunners++;
                }
                if (homeScore < 2 && awayScore > 0) {
                    foundSafeRunners++;
                }
                if (homeScore == 2 && awayScore == 1) {
                    foundSafeRunners++;
                }
                if ((homeScore < 2 && awayScore > 1) || (homeScore > 2 && awayScore < 1)) {
                    foundSafeRunners++;
                }
                if (homeScore == 2 && awayScore == 2) {
                    foundSafeRunners++;
                }
                if ((homeScore < 2 && awayScore > 2) || (homeScore > 2 && awayScore < 2)) {
                    foundSafeRunners++;
                }
                if (homeScore == 2 && awayScore == 3) {
                    foundSafeRunners++;
                }
                if ((homeScore < 2 && awayScore > 3) || (homeScore > 2 && awayScore < 3)) {
                    foundSafeRunners++;
                }
                if (homeScore >= 3 && awayScore == 0) {
                    foundSafeRunners++;
                }
                if (homeScore < 3 && awayScore > 0) {
                    foundSafeRunners++;
                }
                if (homeScore == 3 && awayScore == 1) {
                    foundSafeRunners++;
                }
                if ((homeScore < 3 && awayScore > 1) || (homeScore > 3 && awayScore < 1)) {
                    foundSafeRunners++;
                }
                if (homeScore == 3 && awayScore == 2) {
                    foundSafeRunners++;
                }
                if ((homeScore < 3 && awayScore > 2) || (homeScore > 3 && awayScore < 2)) {
                    foundSafeRunners++;
                }
                if (homeScore == 3 && awayScore == 3) {
                    foundSafeRunners++;
                }
                if ((homeScore < 3 && awayScore > 3) || (homeScore > 3 && awayScore < 3)) {
                    foundSafeRunners++;
                }

                assertEquals(homeScore + " - " + awayScore, nSafeRunners, foundSafeRunners);
            } // end for awayScore
        } // end for homeScore
    }
}
