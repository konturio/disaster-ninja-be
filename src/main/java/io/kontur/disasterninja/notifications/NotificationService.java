package io.kontur.disasterninja.notifications;

import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NotificationService {

    private static final List<String> acceptableTypes = Arrays.asList("FLOOD", "EARTHQUAKE", "CYCLONE", "VOLCANO", "WILDFIRE");
    private static final ConcurrentHashMap<UUID, Long> lastNotifiedPopulation = new ConcurrentHashMap<>();

    public boolean isApplicable(EventApiEventDto event) {
        return isEventInPopulatedArea(event) && isEventTypeAppropriate(event) && isEventSeverityAppropriate(event);
    }

    public abstract void process(EventApiEventDto event, Map<String, Object> urbanPopulationProperties, Map<String, Double> analytics);

    protected static boolean hasPopulationIncreasedSignificantly(EventApiEventDto event) {
        Long currentPopulation = extractPopulation(event);
        if (currentPopulation == null) {
            return false;
        }
        Long previous = lastNotifiedPopulation.get(event.getEventId());
        if (previous == null) {
            return true;
        }
        BigDecimal threshold = new BigDecimal(previous).multiply(new BigDecimal("1.3"));
        return new BigDecimal(currentPopulation).compareTo(threshold) > 0;
    }

    protected static void recordPopulation(EventApiEventDto event) {
        Long currentPopulation = extractPopulation(event);
        if (currentPopulation != null) {
            lastNotifiedPopulation.put(event.getEventId(), currentPopulation);
        }
    }

    private static Long extractPopulation(EventApiEventDto event) {
        try {
            if (event.getEpisodes() == null || event.getEpisodes().isEmpty()) {
                return null;
            }
            Map<String, Object> details = event.getEpisodes().get(0).getEpisodeDetails();
            if (details == null) {
                return null;
            }
            Object value = details.get("population");
            if (value == null) {
                return null;
            }
            return new BigDecimal(String.valueOf(value)).longValue();
        } catch (Exception e) {
            return null;
        }
    }

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
