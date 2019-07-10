package info.fmro.betty.stream.cache.util;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LevelPriceSizeLadder
        implements Serializable {
    private static final long serialVersionUID = 990070832400710390L;
    private final Map<Integer, LevelPriceSize> levelToPriceSize = new TreeMap<>();

    public synchronized void onPriceChange(final boolean isImage, final Iterable<? extends List<Double>> prices) {
        if (isImage) {
            //image is replace
            this.levelToPriceSize.clear();
        }

        if (prices != null) {
            //changes to apply
            for (final List<Double> price : prices) {
                final LevelPriceSize levelPriceSize = new LevelPriceSize(price);
                this.levelToPriceSize.put(levelPriceSize.getLevel(), levelPriceSize);
            }
        }
    }
}
