package info.fmro.betty.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StartingPrices
        implements Serializable {

    private static final long serialVersionUID = -2616193533595542784L;
    private Double nearPrice;
    private Double farPrice;
    private List<PriceSize> backStakeTaken;
    private List<PriceSize> layLiabilityTaken;
    private Double actualSP;

    public StartingPrices() {
    }

    public StartingPrices(Double nearPrice, Double farPrice, List<PriceSize> backStakeTaken, List<PriceSize> layLiabilityTaken, Double actualSP) {
        this.nearPrice = nearPrice;
        this.farPrice = farPrice;
        this.backStakeTaken = backStakeTaken;
        this.layLiabilityTaken = layLiabilityTaken;
        this.actualSP = actualSP;
    }

    public synchronized Double getNearPrice() {
        return nearPrice;
    }

//    public synchronized void setNearPrice(Double nearPrice) {
//        this.nearPrice = nearPrice;
//    }
    public synchronized Double getFarPrice() {
        return farPrice;
    }

//    public synchronized void setFarPrice(Double farPrice) {
//        this.farPrice = farPrice;
//    }
    public synchronized List<PriceSize> getBackStakeTaken() {
        return backStakeTaken == null ? null : new ArrayList<>(backStakeTaken);
    }

//    public synchronized void setBackStakeTaken(List<PriceSize> backStakeTaken) {
//        this.backStakeTaken = backStakeTaken == null ? null : new ArrayList<>(backStakeTaken);
//    }
    public synchronized List<PriceSize> getLayLiabilityTaken() {
        return layLiabilityTaken == null ? null : new ArrayList<>(layLiabilityTaken);
    }

//    public synchronized void setLayLiabilityTaken(List<PriceSize> layLiabilityTaken) {
//        this.layLiabilityTaken = layLiabilityTaken == null ? null : new ArrayList<>(layLiabilityTaken);
//    }
    public synchronized Double getActualSP() {
        return actualSP;
    }

//    public synchronized void setActualSP(Double actualSP) {
//        this.actualSP = actualSP;
//    }
    @Override
    public synchronized int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.nearPrice);
        hash = 97 * hash + Objects.hashCode(this.farPrice);
        hash = 97 * hash + Objects.hashCode(this.backStakeTaken);
        hash = 97 * hash + Objects.hashCode(this.layLiabilityTaken);
        hash = 97 * hash + Objects.hashCode(this.actualSP);
        return hash;
    }

    @Override
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    public synchronized boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StartingPrices other = (StartingPrices) obj;
        if (!Objects.equals(this.nearPrice, other.nearPrice)) {
            return false;
        }
        if (!Objects.equals(this.farPrice, other.farPrice)) {
            return false;
        }
        if (!Objects.equals(this.backStakeTaken, other.backStakeTaken)) {
            return false;
        }
        if (!Objects.equals(this.layLiabilityTaken, other.layLiabilityTaken)) {
            return false;
        }
        return Objects.equals(this.actualSP, other.actualSP);
    }
}
