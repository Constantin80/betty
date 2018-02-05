package info.fmro.betty.main;

import info.fmro.betty.entities.EventResult;
import info.fmro.betty.entities.MarketFilter;
import info.fmro.betty.objects.Statics;
import java.util.HashSet;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiNGJRescriptDemo {

    private static final Logger logger = LoggerFactory.getLogger(ApiNGJRescriptDemo.class);

    private ApiNGJRescriptDemo() {
    }

    public static List<EventResult> getLiveEventResultList(String appKeyString) {
        HashSet<String> eventTypeIdsSet = new HashSet<>(2, 0.75f);
        eventTypeIdsSet.add("1"); // soccer

        MarketFilter marketFilter = new MarketFilter();
        marketFilter.setInPlayOnly(true);
        marketFilter.setTurnInPlayEnabled(true);
        marketFilter.setEventTypeIds(eventTypeIdsSet);

        RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        List<EventResult> eventResultList = ApiNgRescriptOperations.listEvents(marketFilter, appKeyString, rescriptResponseHandler);

        if (Statics.debugLevel.check(3, 113)) {
            logger.info("eventResultList size: {}", eventResultList == null ? null : eventResultList.size());
        }

        return eventResultList;
    }
}
