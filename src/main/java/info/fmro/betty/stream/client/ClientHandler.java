package info.fmro.betty.stream.client;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.utility.Generic;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("UtilityClass")
public final class ClientHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    // errorMessage=You have exceeded your max connection limit which is: 10 connection(s).You currently have: 11 active connection(s).
    // this errorMessage seem to trigger much later than 11 connections; triggers with 70, not with 68; started 23, sleep 3 min, start 5 more, didn't trigger
    public static final int nClients = 32;
    @SuppressWarnings("PublicStaticArrayField")
    public static final Client[] streamClients = new Client[nClients];
    @SuppressWarnings("unchecked")
    private static final ArrayList<String>[] lists = (ArrayList<String>[]) new ArrayList<?>[nClients];
    @SuppressWarnings("PublicStaticCollectionField")
    public static final Collection<Integer> modifiedLists = new ConcurrentSkipListSet<>();
    public static final AtomicInteger threadsWithOcmCommandReceived = new AtomicInteger();
    public static final AtomicInteger threadsWithMcmCommandReceived = new AtomicInteger();
    public static final AtomicInteger nMcmCommandsNeeded = new AtomicInteger();

    static {
        final int length = streamClients.length;
        for (int i = 0; i < length; i++) {
            streamClients[i] = new Client(i, Statics.STREAM_HOST, Statics.SSL_PORT);
        }

        for (int i = 0; i < nClients; i++) {
            lists[i] = new ArrayList<>(1_000);
        }
    }

    @Contract(pure = true)
    private ClientHandler() {
    }

    public static void streamAllMarkets() {
//        final Set<String> marketsSet = Statics.marketCataloguesMap.keySetCopy();
        final Set<String> marketsSet = Statics.rulesManagerThread.rulesManager.markets.keySetCopy();
        streamMarkets(marketsSet);
    }

    private static void streamMarkets(final Collection<String> marketIds) {
        if (!modifiedLists.isEmpty()) {
            logger.info("following clients have internally modified lists: {}", Generic.objectToString(modifiedLists));
            for (final Integer i : modifiedLists) {
                final Set<String> marketsSet = streamClients[i].processor.getMarketsSet();
                final ArrayList<String> list = lists[i];
                list.clear();
                list.addAll(marketsSet);
            }
            modifiedLists.clear();
        }

        final int[] sizes = new int[nClients];
        final boolean[] modified = new boolean[nClients];
        for (int i = 0; i < nClients; i++) {
            final ArrayList<String> list = lists[i];
//            modified[i] =
            list.retainAll(marketIds);
            sizes[i] = list.size();

            marketIds.removeAll(list);
        }

        final ArrayList<String> marketsLeft = new ArrayList<>(marketIds);
        for (int i = 0; i < nClients && !marketsLeft.isEmpty(); i++) {
            if (
//                    modified[i] ||
                    sizes[i] <= 900) {
                final ArrayList<String> list = lists[i];
                final int sizeLeft = 1_000 - sizes[i];
                final List<String> subList = marketsLeft.size() <= sizeLeft ? marketsLeft : marketsLeft.subList(0, sizeLeft - 1);
                list.addAll(subList);
                subList.clear();
                modified[i] = true;
            } else { // won't add to this client
            }
        }

        int nModified = 0;
        for (int i = 0; i < nClients; i++) {
            if (modified[i]) {
                streamClients[i].processor.setMarketsSet(lists[i]);
                nModified++;
            }
        }

        if (!marketsLeft.isEmpty()) {
            logger.error("markets still left in streamMarkets: {} {} {}", marketsLeft.size(), Statics.marketCataloguesMap.size(), Generic.objectToString(sizes));
        }

        if (ClientHandler.nMcmCommandsNeeded.get() == 0 && nModified > 0) {
            ClientHandler.nMcmCommandsNeeded.set(nModified);
        }
    }
}
