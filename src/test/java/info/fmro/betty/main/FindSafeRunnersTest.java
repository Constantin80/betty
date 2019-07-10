package info.fmro.betty.main;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FindSafeRunnersTest {
    @SuppressWarnings({"OverlyComplexMethod", "OverlyLongMethod"})
    @Test
    void marketSafeRunners() {
        // ANYTIME_SCORE
        for (int homeScore = 0; homeScore < 10; homeScore++) {
            for (int awayScore = 0; awayScore < 10; awayScore++) {
                int foundSafeRunners = 0;

                final int additionalBack = homeScore == 0 || awayScore == 0 ? Math.abs(Math.min(homeScore, 3) - Math.min(awayScore, 3)) - 1 : 0;
                final int additionalLay;
                if (additionalBack != 0) {
                    additionalLay = 0;
                } else if (homeScore >= 4 || awayScore >= 4) {
                    additionalLay = -1;
                } else {
                    additionalLay = 0;
                }
                final int nSafeRunners = 1 + additionalBack + additionalLay + Math.min(homeScore, 4) * (3 - Math.min(awayScore, 3)) + Math.min(awayScore, 4) * (3 - Math.min(homeScore, 3));

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

                assertEquals(nSafeRunners, foundSafeRunners, homeScore + " - " + awayScore);
            } // end for awayScore
        } // end for homeScore
    }
}
