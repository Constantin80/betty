package info.fmro.betty.stream;

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
        final boolean[] listWasModified = new boolean[nClients];
        int nRemovedMarkets = 0, nRemainingMarkets = 0;
        for (int i = 0; i < nClients; i++) {
            final ArrayList<String> list = lists[i];
//            modified[i] =
            final int initialListSize = list.size();
            list.retainAll(marketIds);
            final int finalListSize = list.size();
            final int nRemovedElementsFromThisList = initialListSize - finalListSize;
            nRemovedMarkets += nRemovedElementsFromThisList;
            nRemainingMarkets += finalListSize;
            sizes[i] = finalListSize;
            if (nRemovedElementsFromThisList > 0) {
                listWasModified[i] = true; // this will cause a new subscription message
            }

            marketIds.removeAll(list);
        }
        if (nRemovedMarkets > 0) {
            logger.info("nRemovedMarkets from stream: {} remaining: {}", nRemovedMarkets, nRemainingMarkets);
        } else if (nRemovedMarkets == 0) { // nothing was removed, no need to print anything
        } else { // nRemovedMarkets < 0
            logger.error("nRemovedMarkets has strange value: {} {} {}", nRemovedMarkets, marketIds.size(), nRemainingMarkets);
        }

        final ArrayList<String> marketsLeft = new ArrayList<>(marketIds);
        final int nMarketsAdded = marketsLeft.size();
        if (nMarketsAdded > 0) {
            logger.info("nMarketsAdded to stream: {} {}", nMarketsAdded, Generic.objectToString(marketsLeft));
        } else { // nMarketsAdded == 0, nothing was added, no need to print anything
        }

        for (int i = 0; i < nClients && !marketsLeft.isEmpty(); i++) {
            if (
//                    modified[i] ||
                    sizes[i] <= 900) {
                final ArrayList<String> list = lists[i];
                final int sizeLeft = 1_000 - sizes[i];
                final List<String> subList = marketsLeft.size() <= sizeLeft ? marketsLeft : marketsLeft.subList(0, sizeLeft - 1);
                list.addAll(subList);
                subList.clear();
                listWasModified[i] = true;
            } else { // won't add to this client
            }
        }

        int nModifiedLists = 0;
        for (int i = 0; i < nClients; i++) {
            if (listWasModified[i]) {
                streamClients[i].processor.setMarketsSet(lists[i]);
                nModifiedLists++;
            }
        }

        if (!marketsLeft.isEmpty()) {
            logger.error("markets still left in streamMarkets: {} {} {}", marketsLeft.size(), Statics.marketCataloguesMap.size(), Generic.objectToString(sizes));
        }

        if (ClientHandler.nMcmCommandsNeeded.get() == 0 && nModifiedLists > 0) {
            ClientHandler.nMcmCommandsNeeded.set(nModifiedLists);
        }
    }
}
