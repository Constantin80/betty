package info.fmro.betty.safebet;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

class GetMarketBooksThread
        implements Runnable {
    private final List<String> marketIdsListSplit;
    private final QuickCheckThread quickCheckThread;

    @Contract(pure = true)
    GetMarketBooksThread(@NotNull final QuickCheckThread quickCheckThread, @NotNull final List<String> marketIdsListSplit) {
        this.marketIdsListSplit = new ArrayList<>(marketIdsListSplit);
        this.quickCheckThread = quickCheckThread;
    }

    @Override
    public void run() {
        this.quickCheckThread.singleGetMarketBooks(this.marketIdsListSplit);
    }
}
