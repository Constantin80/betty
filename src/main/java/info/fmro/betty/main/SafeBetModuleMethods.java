package info.fmro.betty.main;

import info.fmro.betty.entities.EventResult;
import info.fmro.betty.objects.Statics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SafeBetModuleMethods {
    private static final Logger logger = LoggerFactory.getLogger(SafeBetModuleMethods.class);

    public static List<EventResult> getLiveEventResultList(String appKeyString) {
//        final HashSet<String> eventTypeIdsSet = new HashSet<>(2, 0.75f);
//        eventTypeIdsSet.add("1"); // soccer
//
//        final MarketFilter marketFilter = new MarketFilter();
//        marketFilter.setInPlayOnly(true);
//        marketFilter.setTurnInPlayEnabled(true);
//        marketFilter.setEventTypeIds(eventTypeIdsSet);

        final RescriptResponseHandler rescriptResponseHandler = new RescriptResponseHandler();
        final List<EventResult> eventResultList = ApiNgRescriptOperations.listEvents(ApiNGJRescriptDemo.marketFilter, appKeyString, rescriptResponseHandler);

        if (Statics.debugLevel.check(3, 113)) {
            logger.info("eventResultList size: {}", eventResultList == null ? null : eventResultList.size());
        }

        return eventResultList;
    }
}
