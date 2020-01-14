package info.fmro.betty.entities;

import info.fmro.shared.entities.KeyLineDescription;
import info.fmro.shared.entities.Runner;
import info.fmro.shared.enums.MarketStatus;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass"})
public class MarketBook
        implements Serializable, Comparable<MarketBook> {
    private static final Logger logger = LoggerFactory.getLogger(MarketBook.class);
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = -690691726819586172L;
    private final String marketId;
    private Boolean isMarketDataDelayed;
    private MarketStatus status;
    private Integer betDelay;
    private Boolean bspReconciled;
    private Boolean complete;
    @SuppressWarnings("SpellCheckingInspection")
    private Boolean inplay;
    private Integer numberOfWinners;
    private Integer numberOfRunners;
    private Integer numberOfActiveRunners;
    @Nullable
    private Date lastMatchTime;
    private Double totalMatched;
    private Double totalAvailable;
    private Boolean crossMatching;
    private Boolean runnersVoidable;
    private Long version;
    @Nullable
    private ArrayList<Runner> runners;
    private KeyLineDescription keyLineDescription;
    private long timeStamp;

    @Contract(pure = true)
    public MarketBook(final String marketId) {
        this.marketId = marketId;
    }

    public synchronized String getMarketId() {
        return this.marketId;
    }

    @Contract(pure = true)
    private synchronized Boolean getIsMarketDataDelayed() {
        return this.isMarketDataDelayed;
    }

    private synchronized int setIsMarketDataDelayed(final Boolean newIsMarketDataDelayed) {
        final int modified;
        if (this.isMarketDataDelayed == null) {
            if (newIsMarketDataDelayed == null) {
                modified = 0;
            } else {
                this.isMarketDataDelayed = newIsMarketDataDelayed;
                modified = 1;
            }
        } else if (this.isMarketDataDelayed.equals(newIsMarketDataDelayed)) {
            modified = 0;
        } else {
            this.isMarketDataDelayed = newIsMarketDataDelayed;
            modified = 1;
        }
        return modified;
    }

    public synchronized MarketStatus getStatus() {
        return this.status;
    }

    private synchronized int setStatus(final MarketStatus newStatus) {
        final int modified;
        if (this.status == null) {
            if (newStatus == null) {
                modified = 0;
            } else {
                this.status = newStatus;
                modified = 1;
            }
        } else if (this.status == newStatus) {
            modified = 0;
        } else {
            this.status = newStatus;
            modified = 1;
        }
        return modified;
    }

    public synchronized Integer getBetDelay() {
        return this.betDelay;
    }

    private synchronized int setBetDelay(final Integer newBetDelay) {
        final int modified;
        if (this.betDelay == null) {
            if (newBetDelay == null) {
                modified = 0;
            } else {
                this.betDelay = newBetDelay;
                modified = 1;
            }
        } else if (this.betDelay.equals(newBetDelay)) {
            modified = 0;
        } else {
            this.betDelay = newBetDelay;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized Boolean getBspReconciled() {
        return this.bspReconciled;
    }

    private synchronized int setBspReconciled(final Boolean newBspReconciled) {
        final int modified;
        if (this.bspReconciled == null) {
            if (newBspReconciled == null) {
                modified = 0;
            } else {
                this.bspReconciled = newBspReconciled;
                modified = 1;
            }
        } else if (this.bspReconciled.equals(newBspReconciled)) {
            modified = 0;
        } else {
            this.bspReconciled = newBspReconciled;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized Boolean getComplete() {
        return this.complete;
    }

    private synchronized int setComplete(final Boolean newComplete) {
        final int modified;
        if (this.complete == null) {
            if (newComplete == null) {
                modified = 0;
            } else {
                this.complete = newComplete;
                modified = 1;
            }
        } else if (this.complete.equals(newComplete)) {
            modified = 0;
        } else {
            this.complete = newComplete;
            modified = 1;
        }
        return modified;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public synchronized Boolean getInplay() {
        return this.inplay;
    }

    @SuppressWarnings("SpellCheckingInspection")
    private synchronized int setInplay(final Boolean newInplay) {
        final int modified;
        if (this.inplay == null) {
            if (newInplay == null) {
                modified = 0;
            } else {
                this.inplay = newInplay;
                modified = 1;
            }
        } else if (this.inplay.equals(newInplay)) {
            modified = 0;
        } else {
            this.inplay = newInplay;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized Integer getNumberOfWinners() {
        return this.numberOfWinners;
    }

    private synchronized int setNumberOfWinners(final Integer newNumberOfWinners) {
        final int modified;
        if (this.numberOfWinners == null) {
            if (newNumberOfWinners == null) {
                modified = 0;
            } else {
                this.numberOfWinners = newNumberOfWinners;
                modified = 1;
            }
        } else if (this.numberOfWinners.equals(newNumberOfWinners)) {
            modified = 0;
        } else {
            this.numberOfWinners = newNumberOfWinners;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized Integer getNumberOfRunners() {
        return this.numberOfRunners;
    }

    private synchronized int setNumberOfRunners(final Integer newNumberOfRunners) {
        final int modified;
        if (this.numberOfRunners == null) {
            if (newNumberOfRunners == null) {
                modified = 0;
            } else {
                this.numberOfRunners = newNumberOfRunners;
                modified = 1;
            }
        } else if (this.numberOfRunners.equals(newNumberOfRunners)) {
            modified = 0;
        } else {
            this.numberOfRunners = newNumberOfRunners;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized Integer getNumberOfActiveRunners() {
        return this.numberOfActiveRunners;
    }

    private synchronized int setNumberOfActiveRunners(final Integer newNumberOfActiveRunners) {
        final int modified;
        if (this.numberOfActiveRunners == null) {
            if (newNumberOfActiveRunners == null) {
                modified = 0;
            } else {
                this.numberOfActiveRunners = newNumberOfActiveRunners;
                modified = 1;
            }
        } else if (this.numberOfActiveRunners.equals(newNumberOfActiveRunners)) {
            modified = 0;
        } else {
            this.numberOfActiveRunners = newNumberOfActiveRunners;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    @Nullable
    private synchronized Date getLastMatchTime() {
        return this.lastMatchTime == null ? null : (Date) this.lastMatchTime.clone();
    }

    private synchronized int setLastMatchTime(final Date newLastMatchTime) {
        final int modified;
        if (this.lastMatchTime == null) {
            if (newLastMatchTime == null) {
                modified = 0;
            } else {
                this.lastMatchTime = (Date) newLastMatchTime.clone();
                modified = 1;
            }
        } else if (this.lastMatchTime.equals(newLastMatchTime)) {
            modified = 0;
        } else {
            this.lastMatchTime = newLastMatchTime == null ? null : (Date) newLastMatchTime.clone();
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized Double getTotalMatched() {
        return this.totalMatched;
    }

    private synchronized int setTotalMatched(final Double newTotalMatched) {
        final int modified;
        if (this.totalMatched == null) {
            if (newTotalMatched == null) {
                modified = 0;
            } else {
                this.totalMatched = newTotalMatched;
                modified = 1;
            }
        } else if (this.totalMatched.equals(newTotalMatched)) {
            modified = 0;
        } else {
            this.totalMatched = newTotalMatched;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized Double getTotalAvailable() {
        return this.totalAvailable;
    }

    private synchronized int setTotalAvailable(final Double newTotalAvailable) {
        final int modified;
        if (this.totalAvailable == null) {
            if (newTotalAvailable == null) {
                modified = 0;
            } else {
                this.totalAvailable = newTotalAvailable;
                modified = 1;
            }
        } else if (this.totalAvailable.equals(newTotalAvailable)) {
            modified = 0;
        } else {
            this.totalAvailable = newTotalAvailable;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized Boolean getCrossMatching() {
        return this.crossMatching;
    }

    private synchronized int setCrossMatching(final Boolean newCrossMatching) {
        final int modified;
        if (this.crossMatching == null) {
            if (newCrossMatching == null) {
                modified = 0;
            } else {
                this.crossMatching = newCrossMatching;
                modified = 1;
            }
        } else if (this.crossMatching.equals(newCrossMatching)) {
            modified = 0;
        } else {
            this.crossMatching = newCrossMatching;
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized Boolean getRunnersVoidable() {
        return this.runnersVoidable;
    }

    private synchronized int setRunnersVoidable(final Boolean newRunnersVoidable) {
        final int modified;
        if (this.runnersVoidable == null) {
            if (newRunnersVoidable == null) {
                modified = 0;
            } else {
                this.runnersVoidable = newRunnersVoidable;
                modified = 1;
            }
        } else if (this.runnersVoidable.equals(newRunnersVoidable)) {
            modified = 0;
        } else {
            this.runnersVoidable = newRunnersVoidable;
            modified = 1;
        }
        return modified;
    }

    private synchronized Long getVersion() {
        return this.version;
    }

    private synchronized int setVersion(final Long newVersion) {
        final int modified;
        if (this.version == null) {
            if (newVersion == null) {
                modified = 0;
            } else {
                this.version = newVersion;
                modified = 1;
            }
        } else if (this.version.equals(newVersion)) {
            modified = 0;
        } else {
            this.version = newVersion;
            modified = 1;
        }
        return modified;
    }

    @Nullable
    public synchronized List<Runner> getRunners() {
        return this.runners == null ? null : new ArrayList<>(this.runners);
    }

    private synchronized int setRunners(final List<? extends Runner> newRunners) {
        final int modified;
        if (this.runners == null) {
            if (newRunners == null) {
                modified = 0;
            } else {
                this.runners = new ArrayList<>(newRunners);
                modified = 1;
            }
        } else if (this.runners.equals(newRunners)) {
            modified = 0;
        } else {
            this.runners = newRunners == null ? null : new ArrayList<>(newRunners);
            modified = 1;
        }
        return modified;
    }

    @Contract(pure = true)
    private synchronized KeyLineDescription getKeyLineDescription() {
        return this.keyLineDescription;
    }

    private synchronized int setKeyLineDescription(final KeyLineDescription newKeyLineDescription) {
        final int modified;

        if (this.keyLineDescription == null) {
            if (newKeyLineDescription == null) {
                modified = 0;
            } else {
                this.keyLineDescription = newKeyLineDescription;
                modified = 1;
            }
        } else if (this.keyLineDescription.equals(newKeyLineDescription)) {
            modified = 0;
        } else {
            this.keyLineDescription = newKeyLineDescription;
            modified = 1;
        }

        return modified;
    }

    public synchronized long getTimeStamp() {
        return this.timeStamp;
    }

    public synchronized void setTimeStamp(final long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public synchronized void timeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    public synchronized int update(final MarketBook marketBook) {
        int modified;
        if (this == marketBook) {
            logger.error("update from same object in MarketBook.update: {}", Generic.objectToString(this));
            modified = 0;
        } else if (this.marketId == null ? marketBook.getMarketId() != null : !this.marketId.equals(marketBook.getMarketId())) {
            logger.error("mismatch marketId in MarketBook.update: {} {}", Generic.objectToString(this), Generic.objectToString(marketBook));
            modified = 0;
        } else {
            final long thatTimeStamp = marketBook.getTimeStamp();
            if (this.timeStamp > thatTimeStamp) {
                final long currentTime = System.currentTimeMillis();
                if (this.timeStamp > currentTime) { // clock jump
                    logger.error("clock jump in the past of at least {} ms detected", this.timeStamp - currentTime);
                    this.timeStamp = currentTime; // won't update the object further, as I have no guarantees on the time ordering
                } else {
                    final long timeSinceLastDiskSave = this.timeStamp - Statics.timeLastSaveToDisk.get();
                    if (timeSinceLastDiskSave > 5_000L) {
                        final long timeDifference = this.timeStamp - thatTimeStamp;
                        if (timeDifference > 2_000L) {
                            logger.error("attempt to update MarketBook from older by {} ms object: {}", timeDifference, this.marketId);
                        } else { // happens due to concurrent threads and high processor load; no need to print error message
                        }
                    } else { // objects were written to disk recently, resulting in lag; no error message will be printed
                    }
                } // end else
                modified = 0;
            } else {
                modified = 0; // initialized
                this.timeStamp = thatTimeStamp; // this doesn't count as modification

                modified += this.setIsMarketDataDelayed(marketBook.getIsMarketDataDelayed());
                modified += this.setStatus(marketBook.getStatus());
                modified += this.setBetDelay(marketBook.getBetDelay());
                modified += this.setBspReconciled(marketBook.getBspReconciled());
                modified += this.setComplete(marketBook.getComplete());
                modified += this.setInplay(marketBook.getInplay());
                modified += this.setNumberOfWinners(marketBook.getNumberOfWinners());
                modified += this.setNumberOfRunners(marketBook.getNumberOfRunners());
                modified += this.setNumberOfActiveRunners(marketBook.getNumberOfActiveRunners());
                modified += this.setLastMatchTime(marketBook.getLastMatchTime());
                modified += this.setTotalMatched(marketBook.getTotalMatched());
                modified += this.setTotalAvailable(marketBook.getTotalAvailable());
                modified += this.setCrossMatching(marketBook.getCrossMatching());
                modified += this.setRunnersVoidable(marketBook.getRunnersVoidable());
                modified += this.setVersion(marketBook.getVersion());
                modified += this.setRunners(marketBook.getRunners());
                modified += this.setKeyLineDescription(marketBook.getKeyLineDescription());
            }
        }
        return modified;
    }

    @SuppressWarnings("MethodWithMultipleReturnPoints")
    @Override
    public synchronized int compareTo(@NotNull final MarketBook o) {
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
        if (!Objects.equals(this.marketId, o.marketId)) {
            if (this.marketId == null) {
                return BEFORE;
            }
            if (o.marketId == null) {
                return AFTER;
            }
            return this.marketId.compareTo(o.marketId);
        }

        return EQUAL;
    }

    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MarketBook other = (MarketBook) obj;
        return Objects.equals(this.marketId, other.marketId);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 13 * hash + Objects.hashCode(this.marketId);
        return hash;
    }
}
