package info.fmro.betty.main;

import info.fmro.betty.entities.EventResult;
import info.fmro.betty.entities.MarketFilter;
import info.fmro.betty.objects.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ApiNGJRescriptDemo {

    private static final Logger logger = LoggerFactory.getLogger(ApiNGJRescriptDemo.class);
    public final static HashSet<String> eventTypeIdsSet = new HashSet<>(2, 0.75f);
    public final static MarketFilter marketFilter = new MarketFilter();

    static {
//        eventTypeIdsSet.add("1"); // soccer
        eventTypeIdsSet.addAll(Arrays.asList(Statics.supportedEventTypes));

        marketFilter.setInPlayOnly(true);
        marketFilter.setTurnInPlayEnabled(true);
        marketFilter.setEventTypeIds(eventTypeIdsSet);
    }

    private ApiNGJRescriptDemo() {
    }

    public static List<EventResult> getLiveEventResultList(String appKeyString) {
//        final HashSet<String> eventTypeIdsSet = new HashSet<>(2, 0.75f);
//        eventTypeIdsSet.add("1"); // soccer
//
//        final MarketFilter marketFilter = new MarketFilter();
//        marketFilter.setInPlayOnly(true);
//        marketFilter.setTurnInPlayEnabled(true);
//        marketFilter.setEventTypeIds(eventTypeIdsSet);

        final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        final List<EventResult> eventResultList = ApiNgRescriptOperations.listEvents(marketFilter, appKeyString, rescriptResponseHandler);

        if (Statics.debugLevel.check(3, 113)) {
            logger.info("eventResultList size: {}", eventResultList == null ? null : eventResultList.size());
        }

        return eventResultList;
    }
}
