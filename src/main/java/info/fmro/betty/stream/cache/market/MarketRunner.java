package info.fmro.betty.stream.cache.market;

import info.fmro.betty.stream.cache.util.LevelPriceSizeLadder;
import info.fmro.betty.stream.cache.util.PriceSizeLadder;
import info.fmro.betty.stream.cache.util.RunnerId;
import info.fmro.betty.stream.definitions.RunnerChange;
import info.fmro.betty.stream.definitions.RunnerDefinition;

public class MarketRunner {
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
//    private double spn;
//    private double spf;
//    private double ltp;
//    private double tv;
    private RunnerDefinition runnerDefinition;
//    private MarketRunnerSnap snap;

    public MarketRunner(String marketId, RunnerId runnerId) {
        this.marketId = marketId;
        this.runnerId = runnerId;
    }

    synchronized void onPriceChange(boolean isImage, RunnerChange runnerChange) {
        //snap is invalid
//        snap = null;

//        MarketRunnerPrices newPrices = new MarketRunnerPrices();
//
//        newPrices.setAtl(atlPrices.onPriceChange(isImage, runnerChange.getAtl()));
//        newPrices.setAtb(atbPrices.onPriceChange(isImage, runnerChange.getAtb()));
//        newPrices.setTrd(trdPrices.onPriceChange(isImage, runnerChange.getTrd()));
//        newPrices.setSpb(spbPrices.onPriceChange(isImage, runnerChange.getSpb()));
//        newPrices.setSpl(splPrices.onPriceChange(isImage, runnerChange.getSpl()));
//
//        newPrices.setBatb(batbPrices.onPriceChange(isImage, runnerChange.getBatb()));
//        newPrices.setBatl(batlPrices.onPriceChange(isImage, runnerChange.getBatl()));
//        newPrices.setBdatb(bdatbPrices.onPriceChange(isImage, runnerChange.getBdatb()));
//        newPrices.setBdatl(bdatlPrices.onPriceChange(isImage, runnerChange.getBdatl()));
//
//        newPrices.setSpn(Utils.selectPrice(isImage, newPrices.getSpn(), runnerChange.getSpn()));
//        newPrices.setSpf(Utils.selectPrice(isImage, newPrices.getSpf(), runnerChange.getSpf()));
//        newPrices.setLtp(Utils.selectPrice(isImage, newPrices.getLtp(), runnerChange.getLtp()));
//        newPrices.setTv(Utils.selectPrice(isImage, newPrices.getTv(), runnerChange.getTv()));
        atlPrices.onPriceChange(isImage, runnerChange.getAtl());
        atbPrices.onPriceChange(isImage, runnerChange.getAtb());
        trdPrices.onPriceChange(isImage, runnerChange.getTrd());
        spbPrices.onPriceChange(isImage, runnerChange.getSpb());
        splPrices.onPriceChange(isImage, runnerChange.getSpl());

        batbPrices.onPriceChange(isImage, runnerChange.getBatb());
        batlPrices.onPriceChange(isImage, runnerChange.getBatl());
        bdatbPrices.onPriceChange(isImage, runnerChange.getBdatb());
        bdatlPrices.onPriceChange(isImage, runnerChange.getBdatl());

        //copy on write
//        marketRunnerPrices = newPrices;
    }

    synchronized void onRunnerDefinitionChange(RunnerDefinition runnerDefinition) {
        //snap is invalid
//        snap = null;
        this.runnerDefinition = runnerDefinition;
    }

    public synchronized RunnerId getRunnerId() {
        return runnerId;
    }

//    public synchronized MarketRunnerSnap getSnap() {
//        // takes or returns an existing immutable snap of the runner
//        if (snap == null) {
//            snap = new MarketRunnerSnap(getRunnerId(), runnerDefinition, marketRunnerPrices);
//        }
//        return snap;
//    }

//    @Override
//    public synchronized String toString() {
//        return "MarketRunner{" +
//               "runnerId=" + runnerId +
//               ", prices=" + marketRunnerPrices +
//               ", runnerDefinition=" + runnerDefinition +
//               '}';
//    }
}
