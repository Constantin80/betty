package info.fmro.betty.entities;

import info.fmro.betty.enums.MarketStatus;
import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

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
    private Boolean inplay;
    private Integer numberOfWinners;
    private Integer numberOfRunners;
    private Integer numberOfActiveRunners;
    private Date lastMatchTime;
    private Double totalMatched;
    private Double totalAvailable;
    private Boolean crossMatching;
    private Boolean runnersVoidable;
    private Long version;
    private List<Runner> runners;
    private long timeStamp;

    //    public MarketBook() {
//    }
    public MarketBook(String marketId) {
        this.marketId = marketId;
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized Boolean getIsMarketDataDelayed() {
        return isMarketDataDelayed;
    }

    public synchronized int setIsMarketDataDelayed(Boolean isMarketDataDelayed) {
        final int modified;
        if (this.isMarketDataDelayed == null) {
            if (isMarketDataDelayed == null) {
                modified = 0;
            } else {
                this.isMarketDataDelayed = isMarketDataDelayed;
                modified = 1;
            }
        } else if (this.isMarketDataDelayed.equals(isMarketDataDelayed)) {
            modified = 0;
        } else {
            this.isMarketDataDelayed = isMarketDataDelayed;
            modified = 1;
        }
        return modified;
    }

    public synchronized MarketStatus getStatus() {
        return status;
    }

    public synchronized int setStatus(MarketStatus status) {
        final int modified;
        if (this.status == null) {
            if (status == null) {
                modified = 0;
            } else {
                this.status = status;
                modified = 1;
            }
        } else if (this.status.equals(status)) {
            modified = 0;
        } else {
            this.status = status;
            modified = 1;
        }
        return modified;
    }

    public synchronized Integer getBetDelay() {
        return betDelay;
    }

    public synchronized int setBetDelay(Integer betDelay) {
        final int modified;
        if (this.betDelay == null) {
            if (betDelay == null) {
                modified = 0;
            } else {
                this.betDelay = betDelay;
                modified = 1;
            }
        } else if (this.betDelay.equals(betDelay)) {
            modified = 0;
        } else {
            this.betDelay = betDelay;
            modified = 1;
        }
        return modified;
    }

    public synchronized Boolean getBspReconciled() {
        return bspReconciled;
    }

    public synchronized int setBspReconciled(Boolean bspReconciled) {
        final int modified;
        if (this.bspReconciled == null) {
            if (bspReconciled == null) {
                modified = 0;
            } else {
                this.bspReconciled = bspReconciled;
                modified = 1;
            }
        } else if (this.bspReconciled.equals(bspReconciled)) {
            modified = 0;
        } else {
            this.bspReconciled = bspReconciled;
            modified = 1;
        }
        return modified;
    }

    public synchronized Boolean getComplete() {
        return complete;
    }

    public synchronized int setComplete(Boolean complete) {
        final int modified;
        if (this.complete == null) {
            if (complete == null) {
                modified = 0;
            } else {
                this.complete = complete;
                modified = 1;
            }
        } else if (this.complete.equals(complete)) {
            modified = 0;
        } else {
            this.complete = complete;
            modified = 1;
        }
        return modified;
    }

    public synchronized Boolean getInplay() {
        return inplay;
    }

    public synchronized int setInplay(Boolean inplay) {
        final int modified;
        if (this.inplay == null) {
            if (inplay == null) {
                modified = 0;
            } else {
                this.inplay = inplay;
                modified = 1;
            }
        } else if (this.inplay.equals(inplay)) {
            modified = 0;
        } else {
            this.inplay = inplay;
            modified = 1;
        }
        return modified;
    }

    public synchronized Integer getNumberOfWinners() {
        return numberOfWinners;
    }

    public synchronized int setNumberOfWinners(Integer numberOfWinners) {
        final int modified;
        if (this.numberOfWinners == null) {
            if (numberOfWinners == null) {
                modified = 0;
            } else {
                this.numberOfWinners = numberOfWinners;
                modified = 1;
            }
        } else if (this.numberOfWinners.equals(numberOfWinners)) {
            modified = 0;
        } else {
            this.numberOfWinners = numberOfWinners;
            modified = 1;
        }
        return modified;
    }

    public synchronized Integer getNumberOfRunners() {
        return numberOfRunners;
    }

    public synchronized int setNumberOfRunners(Integer numberOfRunners) {
        final int modified;
        if (this.numberOfRunners == null) {
            if (numberOfRunners == null) {
                modified = 0;
            } else {
                this.numberOfRunners = numberOfRunners;
                modified = 1;
            }
        } else if (this.numberOfRunners.equals(numberOfRunners)) {
            modified = 0;
        } else {
            this.numberOfRunners = numberOfRunners;
            modified = 1;
        }
        return modified;
    }

    public synchronized Integer getNumberOfActiveRunners() {
        return numberOfActiveRunners;
    }

    public synchronized int setNumberOfActiveRunners(Integer numberOfActiveRunners) {
        final int modified;
        if (this.numberOfActiveRunners == null) {
            if (numberOfActiveRunners == null) {
                modified = 0;
            } else {
                this.numberOfActiveRunners = numberOfActiveRunners;
                modified = 1;
            }
        } else if (this.numberOfActiveRunners.equals(numberOfActiveRunners)) {
            modified = 0;
        } else {
            this.numberOfActiveRunners = numberOfActiveRunners;
            modified = 1;
        }
        return modified;
    }

    public synchronized Date getLastMatchTime() {
        return lastMatchTime == null ? null : (Date) lastMatchTime.clone();
    }

    public synchronized int setLastMatchTime(Date lastMatchTime) {
        final int modified;
        if (this.lastMatchTime == null) {
            if (lastMatchTime == null) {
                modified = 0;
            } else {
                this.lastMatchTime = (Date) lastMatchTime.clone();
                modified = 1;
            }
        } else if (this.lastMatchTime.equals(lastMatchTime)) {
            modified = 0;
        } else {
            this.lastMatchTime = lastMatchTime == null ? null : (Date) lastMatchTime.clone();
            modified = 1;
        }
        return modified;
    }

    public synchronized Double getTotalMatched() {
        return totalMatched;
    }

    public synchronized int setTotalMatched(Double totalMatched) {
        final int modified;
        if (this.totalMatched == null) {
            if (totalMatched == null) {
                modified = 0;
            } else {
                this.totalMatched = totalMatched;
                modified = 1;
            }
        } else if (this.totalMatched.equals(totalMatched)) {
            modified = 0;
        } else {
            this.totalMatched = totalMatched;
            modified = 1;
        }
        return modified;
    }

    public synchronized Double getTotalAvailable() {
        return totalAvailable;
    }

    public synchronized int setTotalAvailable(Double totalAvailable) {
        final int modified;
        if (this.totalAvailable == null) {
            if (totalAvailable == null) {
                modified = 0;
            } else {
                this.totalAvailable = totalAvailable;
                modified = 1;
            }
        } else if (this.totalAvailable.equals(totalAvailable)) {
            modified = 0;
        } else {
            this.totalAvailable = totalAvailable;
            modified = 1;
        }
        return modified;
    }

    public synchronized Boolean getCrossMatching() {
        return crossMatching;
    }

    public synchronized int setCrossMatching(Boolean crossMatching) {
        final int modified;
        if (this.crossMatching == null) {
            if (crossMatching == null) {
                modified = 0;
            } else {
                this.crossMatching = crossMatching;
                modified = 1;
            }
        } else if (this.crossMatching.equals(crossMatching)) {
            modified = 0;
        } else {
            this.crossMatching = crossMatching;
            modified = 1;
        }
        return modified;
    }

    public synchronized Boolean getRunnersVoidable() {
        return runnersVoidable;
    }

    public synchronized int setRunnersVoidable(Boolean runnersVoidable) {
        final int modified;
        if (this.runnersVoidable == null) {
            if (runnersVoidable == null) {
                modified = 0;
            } else {
                this.runnersVoidable = runnersVoidable;
                modified = 1;
            }
        } else if (this.runnersVoidable.equals(runnersVoidable)) {
            modified = 0;
        } else {
            this.runnersVoidable = runnersVoidable;
            modified = 1;
        }
        return modified;
    }

    public synchronized Long getVersion() {
        return version;
    }

    public synchronized int setVersion(Long version) {
        final int modified;
        if (this.version == null) {
            if (version == null) {
                modified = 0;
            } else {
                this.version = version;
                modified = 1;
            }
        } else if (this.version.equals(version)) {
            modified = 0;
        } else {
            this.version = version;
            modified = 1;
        }
        return modified;
    }

    public synchronized List<Runner> getRunners() {
        return runners == null ? null : new ArrayList<>(runners);
    }

    public synchronized int setRunners(List<Runner> runners) {
        final int modified;
        if (this.runners == null) {
            if (runners == null) {
                modified = 0;
            } else {
                this.runners = new ArrayList<>(runners);
                modified = 1;
            }
        } else if (this.runners.equals(runners)) {
            modified = 0;
        } else {
            this.runners = runners == null ? null : new ArrayList<>(runners);
            modified = 1;
        }
        return modified;
    }

    public synchronized long getTimeStamp() {
        return timeStamp;
    }

    public synchronized void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public synchronized void timeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    public synchronized int update(MarketBook marketBook) {
        int modified;
        if (this == marketBook) {
            logger.error("update from same object in MarketBook.update: {}", Generic.objectToString(this));
            modified = 0;
        } else if (this.marketId == null ? marketBook.getMarketId() != null : !this.marketId.equals(marketBook.getMarketId())) {
            logger.error("mismatch marketId in MarketBook.update: {} {}", Generic.objectToString(this), Generic.objectToString(marketBook));
            modified = 0;
        } else {
            long thatTimeStamp = marketBook.getTimeStamp();
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
            }
        }
        return modified;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(MarketBook other) {
        if (other == null) {
            return AFTER;
        }
        if (this == other) {
            return EQUAL;
        }

        if (this.getClass() != other.getClass()) {
            if (this.getClass().hashCode() < other.getClass().hashCode()) {
                return BEFORE;
            } else {
                return AFTER;
            }
        }
        if (!Objects.equals(this.marketId, other.marketId)) {
            if (this.marketId == null) {
                return BEFORE;
            }
            if (other.marketId == null) {
                return AFTER;
            }
            return this.marketId.compareTo(other.marketId);
        }

        return EQUAL;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(Object obj) {
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
