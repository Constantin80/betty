package info.fmro.betty.main;

import java.util.List;

public class GetMarketBooksThread
        implements Runnable {

    private final List<String> marketIdsListSplit;
    private final QuickCheckThread quickCheckThread;

    public GetMarketBooksThread(QuickCheckThread quickCheckThread, List<String> marketIdsListSplit) {
        this.marketIdsListSplit = marketIdsListSplit;
        this.quickCheckThread = quickCheckThread;
    }

    @Override
    public void run() {
        quickCheckThread.singleGetMarketBooks(marketIdsListSplit);
    }
}
