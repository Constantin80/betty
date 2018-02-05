package info.fmro.betty.objects;

import info.fmro.betty.enums.ParsedMarketType;
import info.fmro.betty.enums.ParsedRunnerType;
import info.fmro.shared.utility.Generic;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParsedMarket
        implements Serializable, Comparable<ParsedMarket> {

    private static final Logger logger = LoggerFactory.getLogger(ParsedMarket.class);
    public static final int BEFORE = -1, EQUAL = 0, AFTER = 1;
    private static final long serialVersionUID = -2616558897137266131L;
    private final String marketId;
    private final ParsedMarketType parsedMarketType;
    private final HashSet<ParsedRunner> parsedRunnersSet;

//    public ParsedMarket(String marketId) {
//        this.marketId = marketId;
//    }
//
//    public ParsedMarket(String marketId, ParsedMarketType parsedMarketType) {
//        this.marketId = marketId;
//        this.parsedMarketType = parsedMarketType;
//    }
    public ParsedMarket(String marketId, ParsedMarketType parsedMarketType, HashSet<ParsedRunner> parsedRunnersSet) {
        this.marketId = marketId;
        this.parsedMarketType = parsedMarketType;
        this.parsedRunnersSet = new HashSet<>(parsedRunnersSet);
    }

    public synchronized String getMarketId() {
        return marketId;
    }

    public synchronized ParsedMarketType getParsedMarketType() {
        return parsedMarketType;
    }

//    public synchronized void setParsedMarketType(ParsedMarketType parsedMarketType) {
//        this.parsedMarketType = parsedMarketType;
//    }
    public synchronized HashSet<ParsedRunner> getParsedRunnersSet() {
        return new HashSet<>(parsedRunnersSet);
    }

//    public synchronized void setParsedRunnersSet(HashSet<ParsedRunner> parsedRunnersSet) {
//        this.parsedRunnersSet = parsedRunnersSet == null ? null : new HashSet<>(parsedRunnersSet);
//    }
    public synchronized boolean checkSameTypeRunners() {
        boolean allDifferent;
        int capacity = Generic.getCollectionCapacity(this.parsedRunnersSet.size());
        HashSet<ParsedRunnerType> parsedRunnerTypesSet = new HashSet<>(capacity);

        for (ParsedRunner parsedRunner : this.parsedRunnersSet) {
            parsedRunnerTypesSet.add(parsedRunner.getParsedRunnerType());
        }
        if (parsedRunnerTypesSet.size() != this.parsedRunnersSet.size()) {
            logger.error("same type ParsedRunner(s) detected {} {} {}: {}", parsedRunnerTypesSet.size(), this.parsedRunnersSet.size(), Generic.objectToString(parsedRunnerTypesSet),
                    Generic.objectToString(this));
            allDifferent = false;
        } else {
            allDifferent = true;
        }

        return allDifferent;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized int compareTo(ParsedMarket other) {
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
        if (!Objects.equals(this.parsedMarketType, other.parsedMarketType)) {
            if (this.parsedMarketType == null) {
                return BEFORE;
            }
            if (other.parsedMarketType == null) {
                return AFTER;
            }
            return this.parsedMarketType.compareTo(other.parsedMarketType);
        }
        if (!Objects.equals(this.parsedRunnersSet, other.parsedRunnersSet)) {
            if (this.parsedRunnersSet == null) {
                return BEFORE;
            }
            if (other.parsedRunnersSet == null) {
                return AFTER;
            }
//            return this.parsedRunnersSet.compareTo(other.parsedRunnersSet);
            return BEFORE; // very primitive, hopefully it won't cause bugs; implementing compareTo for sets is not easy and it would likely consume too much resources
        }

        return EQUAL;
    }

    @Override
    @SuppressWarnings(value = "AccessingNonPublicFieldOfAnotherObject")
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
        final ParsedMarket other = (ParsedMarket) obj;
        if (!Objects.equals(this.marketId, other.marketId)) {
            return false;
        }
        if (this.parsedMarketType != other.parsedMarketType) {
            return false;
        }
        return Objects.equals(this.parsedRunnersSet, other.parsedRunnersSet);
    }

    @Override
    public synchronized int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.marketId);
        hash = 97 * hash + Objects.hashCode(this.parsedMarketType);
        hash = 97 * hash + Objects.hashCode(this.parsedRunnersSet);
        return hash;
    }
}
