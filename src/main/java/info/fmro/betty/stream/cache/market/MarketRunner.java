package info.fmro.betty.stream.cache.market;

import info.fmro.betty.objects.Statics;
import info.fmro.betty.stream.cache.util.LevelPriceSizeLadder;
import info.fmro.betty.stream.cache.util.PriceSizeLadder;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.cache.util.Utils;
import info.fmro.betty.stream.definitions.Order;
import info.fmro.betty.stream.definitions.RunnerChange;
import info.fmro.betty.stream.definitions.RunnerDefinition;
import info.fmro.betty.stream.definitions.Side;
import info.fmro.shared.utility.Generic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class MarketRunner
        implements Serializable { // amounts are in underlying currency (GBP), but the only way to get amounts from PriceSize object is with getSizeEUR method
    private static final Logger logger = LoggerFactory.getLogger(MarketRunner.class);
    private static final long serialVersionUID = -7071355306184374342L;
    //    private final Market market;
    private final String marketId;
    private final RunnerId runnerId;

    // Level / Depth Based Ladders
//    private MarketRunnerPrices marketRunnerPrices = new MarketRunnerPrices();
    private PriceSizeLadder atlPrices = PriceSizeLadder.newLay(); // available to lay
    private PriceSizeLadder atbPrices = PriceSizeLadder.newBack(); // available to back
    private PriceSizeLadder trdPrices = PriceSizeLadder.newLay(); // traded
    private PriceSizeLadder spbPrices = PriceSizeLadder.newBack();
    private PriceSizeLadder splPrices = PriceSizeLadder.newLay();

    // Full depth Ladders
    private LevelPriceSizeLadder batbPrices = new LevelPriceSizeLadder();
    private LevelPriceSizeLadder batlPrices = new LevelPriceSizeLadder();
    private LevelPriceSizeLadder bdatbPrices = new LevelPriceSizeLadder();
    private LevelPriceSizeLadder bdatlPrices = new LevelPriceSizeLadder();

    // special prices
    private double spn; // starting price near projected
    private double spf; // starting price far
    private double ltp; // last traded price
    private double tv; //total value traded
    private RunnerDefinition runnerDefinition;

    public MarketRunner(final String marketId, final RunnerId runnerId) {
        this.marketId = marketId;
        this.runnerId = runnerId;
    }

    synchronized void onPriceChange(final boolean isImage, final RunnerChange runnerChange) {
        atlPrices.onPriceChange(isImage, runnerChange.getAtl());
        atbPrices.onPriceChange(isImage, runnerChange.getAtb());
        trdPrices.onPriceChange(isImage, runnerChange.getTrd());
        spbPrices.onPriceChange(isImage, runnerChange.getSpb());
        splPrices.onPriceChange(isImage, runnerChange.getSpl());

        batbPrices.onPriceChange(isImage, runnerChange.getBatb());
        batlPrices.onPriceChange(isImage, runnerChange.getBatl());
        bdatbPrices.onPriceChange(isImage, runnerChange.getBdatb());
        bdatlPrices.onPriceChange(isImage, runnerChange.getBdatl());

        this.setSpn(Utils.selectPrice(isImage, this.getSpn(), runnerChange.getSpn()));
        this.setSpf(Utils.selectPrice(isImage, this.getSpf(), runnerChange.getSpf()));
        this.setLtp(Utils.selectPrice(isImage, this.getLtp(), runnerChange.getLtp()));
        this.setTv(Utils.selectPrice(isImage, this.getTv(), runnerChange.getTv()));
    }

    synchronized void onRunnerDefinitionChange(final RunnerDefinition runnerDefinition) {
        this.runnerDefinition = runnerDefinition;
    }

    public synchronized double getBestAvailableLayPrice(final Map<String, Order> unmatchedOrders, final double calculatedLimit) {
        final PriceSizeLadder modifiedPrices = this.atlPrices.copy();
        if (unmatchedOrders == null) { // normal case, and nothing to be done
        } else {
            for (Order order : unmatchedOrders.values()) {
                final Side side = order.getSide();
                if (side == null) {
                    logger.error("null side in getBestAvailableLayPrice for order: {}", Generic.objectToString(order));
                } else if (side.equals(Side.B)) {
                    final Double price = order.getP(), sizeRemaining = order.getSr();
                    final double sizeRemainingPrimitive = sizeRemaining == null ? 0d : sizeRemaining;
                    modifiedPrices.removeAmountEUR(price, sizeRemainingPrimitive);
                } else { // uninteresting side, nothing to be done
                }
            }
        }
        return modifiedPrices.getBestPrice(calculatedLimit);
    }

    public synchronized double getBestAvailableBackPrice(final Map<String, Order> unmatchedOrders, final double calculatedLimit) {
        final PriceSizeLadder modifiedPrices = this.atbPrices.copy();
        if (unmatchedOrders == null) { // normal case, and nothing to be done
        } else {
            for (Order order : unmatchedOrders.values()) {
                final Side side = order.getSide();
                if (side == null) {
                    logger.error("null side in getBestAvailableBackPrice for order: {}", Generic.objectToString(order));
                } else if (side.equals(Side.L)) {
                    final Double price = order.getP(), sizeRemaining = order.getSr();
                    final double sizeRemainingPrimitive = sizeRemaining == null ? 0d : sizeRemaining;
                    modifiedPrices.removeAmountEUR(price, sizeRemainingPrimitive);
                } else { // uninteresting side, nothing to be done
                }
            }
        }
        return modifiedPrices.getBestPrice(calculatedLimit);
    }

    public synchronized RunnerId getRunnerId() {
        return runnerId;
    }

    public synchronized double getSpn() {
        return spn;
    }

    public synchronized void setSpn(final double spn) {
        this.spn = spn;
    }

    public synchronized double getSpf() {
        return spf;
    }

    public synchronized void setSpf(final double spf) {
        this.spf = spf;
    }

    public synchronized double getLtp() {
        return ltp;
    }

    public synchronized void setLtp(final double ltp) {
        this.ltp = ltp;
    }

    private synchronized double getTv() {
        return tv;
    }

    public synchronized double getTvEUR() {
        return getTv() * Statics.safetyLimits.currencyRate.get();
    }

    public synchronized void setTv(final double tv) {
        this.tv = tv;
    }

    public synchronized TreeMap<Double, Double> getAvailableToLay() {
        return this.atlPrices.getSimpleTreeMap();
    }

    public synchronized TreeMap<Double, Double> getAvailableToBack() {
        return this.atbPrices.getSimpleTreeMap();
    }
}
