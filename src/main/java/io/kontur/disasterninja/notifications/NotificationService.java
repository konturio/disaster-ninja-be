package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

public abstract class NotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

    private static final List<String> acceptableTypes = Arrays.asList("FLOOD", "EARTHQUAKE", "CYCLONE", "VOLCANO", "WILDFIRE");

    public boolean isApplicable(EventApiEventDto event) {
        boolean inPopulatedArea = isEventInPopulatedArea(event);
        boolean typeOk = isEventTypeAppropriate(event);
        boolean severityOk = isEventSeverityAppropriate(event);

        if (!inPopulatedArea) {
            LOG.info("Event {} is not in populated area or below population threshold", event.getEventId());
        }
        if (!typeOk) {
            LOG.info("Event {} has unsupported type {}", event.getEventId(), event.getType());
        }
        if (!severityOk) {
            LOG.info("Event {} filtered out due to severity", event.getEventId());
        }

        return inPopulatedArea && typeOk && severityOk;
    }

    public abstract void process(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics);

    public abstract String getEventApiFeed();

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
