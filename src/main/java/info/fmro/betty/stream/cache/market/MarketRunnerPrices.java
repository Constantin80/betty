package info.fmro.betty.stream.cache.market;

import info.fmro.betty.entities.PriceSize;
import info.fmro.betty.stream.cache.util.LevelPriceSize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarketRunnerPrices {
    private List<PriceSize> atl = Collections.emptyList();
    private List<PriceSize> atb = Collections.emptyList();
    private List<PriceSize> trd = Collections.emptyList();
    private List<PriceSize> spb = Collections.emptyList();
    private List<PriceSize> spl = Collections.emptyList();

    private List<LevelPriceSize> batb = Collections.emptyList();
    private List<LevelPriceSize> batl = Collections.emptyList();
    private List<LevelPriceSize> bdatb = Collections.emptyList();
    private List<LevelPriceSize> bdatl = Collections.emptyList();

    private double ltp;
    private double spn;
    private double spf;
    private double tv;

    public synchronized List<PriceSize> getAtl() {
        return atl == null ? null : new ArrayList<>(atl);
    }

    public synchronized List<PriceSize> getAtb() {
        return atb == null ? null : new ArrayList<>(atb);
    }

    public synchronized List<PriceSize> getTrd() {
        return trd == null ? null : new ArrayList<>(trd);
    }

    public synchronized List<PriceSize> getSpb() {
        return spb == null ? null : new ArrayList<>(spb);
    }

    public synchronized List<PriceSize> getSpl() {
        return spl == null ? null : new ArrayList<>(spl);
    }

    public synchronized List<LevelPriceSize> getBatb() {
        return batb == null ? null : new ArrayList<>(batb);
    }

    public synchronized List<LevelPriceSize> getBatl() {
        return batl == null ? null : new ArrayList<>(batl);
    }

    public synchronized List<LevelPriceSize> getBdatb() {
        return bdatb == null ? null : new ArrayList<>(bdatb);
    }

    public synchronized List<LevelPriceSize> getBdatl() {
        return bdatl == null ? null : new ArrayList<>(bdatl);
    }

    public synchronized double getLtp() {
        return ltp;
    }

    public synchronized double getSpn() {
        return spn;
    }

    public synchronized double getSpf() {
        return spf;
    }

    public synchronized double getTv() {
        return tv;
    }

    public synchronized void setLtp(double ltp) {
        this.ltp = ltp;
    }

    public synchronized void setSpn(double spn) {
        this.spn = spn;
    }

    public synchronized void setSpf(double spf) {
        this.spf = spf;
    }

    public synchronized void setTv(double tv) {
        this.tv = tv;
    }

    @Override
    public synchronized String toString() {
        return "MarketRunnerPrices{" +
               "atl=" + atl +
               ", atb=" + atb +
               ", trd=" + trd +
               ", spb=" + spb +
               ", spl=" + spl +
               ", batb=" + batb +
               ", batl=" + batl +
               ", bdatb=" + bdatb +
               ", bdatl=" + bdatl +
               ", ltp=" + ltp +
               ", spn=" + spn +
               ", spf=" + spf +
               ", tv=" + tv +
               '}';
    }

    public void setAtl(List<PriceSize> atl) {
        this.atl = atl == null ? null : new ArrayList<>(atl);
    }

    public void setAtb(List<PriceSize> atb) {
        this.atb = atb == null ? null : new ArrayList<>(atb);
    }

    public void setTrd(List<PriceSize> trd) {
        this.trd = trd == null ? null : new ArrayList<>(trd);
    }

    public void setSpb(List<PriceSize> spb) {
        this.spb = spb == null ? null : new ArrayList<>(spb);
    }

    public void setSpl(List<PriceSize> spl) {
        this.spl = spl == null ? null : new ArrayList<>(spl);
    }

    public void setBatb(List<LevelPriceSize> batb) {
        this.batb = batb == null ? null : new ArrayList<>(batb);
    }

    public void setBatl(List<LevelPriceSize> batl) {
        this.batl = batl == null ? null : new ArrayList<>(batl);
    }

    public void setBdatb(List<LevelPriceSize> bdatb) {
        this.bdatb = bdatb == null ? null : new ArrayList<>(bdatb);
    }

    public void setBdatl(List<LevelPriceSize> bdatl) {
        this.bdatl = bdatl == null ? null : new ArrayList<>(bdatl);
    }
}
