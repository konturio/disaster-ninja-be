package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

public abstract class NotificationService {

    private static final List<String> acceptableTypes = Arrays.asList("FLOOD", "EARTHQUAKE", "CYCLONE", "VOLCANO", "WILDFIRE");

    public boolean isApplicable(EventApiEventDto event) {
        return isEventInPopulatedArea(event) && isEventTypeAppropriate(event) && isEventSeverityAppropriate(event);
    }

    public abstract void process(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics);

    private boolean isEventInPopulatedArea(EventApiEventDto event) {
        if (event.getEpisodes().get(0).getEpisodeDetails() == null) {
            return false;
        }
        String population = String.valueOf(event.getEpisodes().get(0).getEpisodeDetails().get("population"));
        return population != null &&
                new BigDecimal(population).compareTo(new BigDecimal(500)) >= 0;
    }

    /**
     * Hotfix for Industrial heats being wildfires #7985
     */
    private boolean isEventTypeAppropriate(EventApiEventDto eventApiEventDto) {
        return acceptableTypes.contains(eventApiEventDto.getType());
    }

    private boolean isEventSeverityAppropriate(EventApiEventDto event) {
        return StringUtils.isBlank(event.getName()) || !event.getName().startsWith("Green");
    }
}
