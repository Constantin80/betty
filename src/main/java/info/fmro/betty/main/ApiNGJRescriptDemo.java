package info.fmro.betty.main;

import info.fmro.betty.entities.EventResult;
import info.fmro.betty.entities.MarketFilter;
import info.fmro.betty.objects.Statics;
import org.jetbrains.annotations.Contract;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@SuppressWarnings("UtilityClass")
final class ApiNGJRescriptDemo {
    private static final Logger logger = LoggerFactory.getLogger(ApiNGJRescriptDemo.class);
    //    public final static HashSet<String> eventTypeIdsSet = new HashSet<>(2, 0.75f);
    public static final MarketFilter marketFilter = new MarketFilter();

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

    static List<EventResult> getEventList(final String appKeyString) {
        final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        final List<EventResult> eventResultList = ApiNgRescriptOperations.listEvents(marketFilter, appKeyString, rescriptResponseHandler);

        if (Statics.debugLevel.check(3, 113)) {
            logger.info("eventResultList size: {}", eventResultList == null ? null : eventResultList.size());
        }

        return eventResultList;
    }
}
