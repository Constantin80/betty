package info.fmro.betty.betapi;

import info.fmro.betty.objects.Statics;
import info.fmro.shared.betapi.ApiNgRescriptOperations;
import info.fmro.shared.betapi.RescriptResponseHandler;
import info.fmro.shared.entities.EventResult;
import info.fmro.shared.entities.MarketFilter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SuppressWarnings("UtilityClass")
public final class ApiNGJRescriptDemo {
    private static final Logger logger = LoggerFactory.getLogger(ApiNGJRescriptDemo.class);
    //    public final static HashSet<String> eventTypeIdsSet = new HashSet<>(2, 0.75f);
//    public static final MarketFilter marketFilter = new MarketFilter();

//    static {
//        eventTypeIdsSet.addAll(Arrays.asList(Statics.supportedEventTypes));
//
//        marketFilter.setInPlayOnly(true);
//        marketFilter.setTurnInPlayEnabled(true);
//        marketFilter.setEventTypeIds(eventTypeIdsSet);
//    }

    @Contract(pure = true)
    private ApiNGJRescriptDemo() {
    }

    public static List<EventResult> getEventList(@NotNull final MarketFilter marketFilter) {
        final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        final List<EventResult> eventResultList = ApiNgRescriptOperations.listEvents(marketFilter, rescriptResponseHandler, HttpUtil.sendPostRequestRescriptMethod);

        if (Statics.debugLevel.check(3, 113)) {
            logger.info("eventResultList size: {}", eventResultList == null ? null : eventResultList.size());
        }

        return eventResultList;
    }
}
