package info.fmro.betty.entities;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StartingPrices
        implements Serializable {
    private static final long serialVersionUID = -2616193533595542784L;
    private Double nearPrice;
    private Double farPrice;
    private List<? extends PriceSize> backStakeTaken;
    private List<? extends PriceSize> layLiabilityTaken;
    private Double actualSP;

    @Contract(pure = true)
    public StartingPrices() {
    }

    @Contract(pure = true)
    public StartingPrices(final Double nearPrice, final Double farPrice, @NotNull final List<? extends PriceSize> backStakeTaken, @NotNull final List<? extends PriceSize> layLiabilityTaken, final Double actualSP) {
        this.nearPrice = nearPrice;
        this.farPrice = farPrice;
        this.backStakeTaken = new ArrayList<>(backStakeTaken);
        this.layLiabilityTaken = new ArrayList<>(layLiabilityTaken);
        this.actualSP = actualSP;
    }

    public synchronized Double getNearPrice() {
        return this.nearPrice;
    }

    public synchronized Double getFarPrice() {
        return this.farPrice;
    }

    @Nullable
    public synchronized List<PriceSize> getBackStakeTaken() {
        return this.backStakeTaken == null ? null : new ArrayList<>(this.backStakeTaken);
    }

    @Nullable
    public synchronized List<PriceSize> getLayLiabilityTaken() {
        return this.layLiabilityTaken == null ? null : new ArrayList<>(this.layLiabilityTaken);
    }

    public synchronized Double getActualSP() {
        return this.actualSP;
    }

    @SuppressWarnings("NonFinalFieldReferencedInHashCode")
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

    @SuppressWarnings("NonFinalFieldReferenceInEquals")
    @Contract(value = "null -> false", pure = true)
    @Override
    public synchronized boolean equals(final Object obj) {
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
