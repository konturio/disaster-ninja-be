package io.kontur.disasterninja.service.converter;

import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.dto.EventType;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;

import java.util.List;
import java.util.Map;

public class EventListEventDtoConverter {

    public static EventListDto convert(EventApiEventDto event) {
        EventListDto dto = new EventListDto();
        dto.setEventId(event.getEventId());

        dto.setEventName(eventName(event));
        dto.setDescription(event.getDescription());
        dto.setLocation(event.getLocation());
        dto.setEpisodeCount(event.getEpisodes().size());
        List<String> eventUrls = event.getUrls();
        dto.setExternalUrls(eventUrls != null ? List.copyOf(eventUrls) : List.of());

        dto.setSeverity(event.getSeverity());
        Map<String, Object> eventDetails = event.getEventDetails();
        if (eventDetails != null) {
            if (eventDetails.containsKey("population")) {
                dto.setAffectedPopulation(convertLong(eventDetails.get("population")));
            }
            if (eventDetails.containsKey("osmGapsPercentage")) {
                dto.setOsmGaps(convertLong(eventDetails.get("osmGapsPercentage")));
            }
            if (eventDetails.containsKey("populatedAreaKm2")) {
                dto.setSettledArea(convertDouble(event.getEventDetails().get("populatedAreaKm2")));
            }
            if (eventDetails.containsKey("loss")) {
                dto.setLoss(convertLong(event.getEventDetails().get("loss")));
            }
        }
        dto.setUpdatedAt(event.getUpdatedAt());

        dto.setBbox(event.getBbox());
        dto.setCentroid(event.getCentroid());

        return dto;
    }

    protected static Long convertLong(Object value) {
        if (value == null || "null".equals(String.valueOf(value))) {
            return 0L;
        } else {
            return Math.round(Double.parseDouble(String.valueOf(value)));
        }
    }

    protected static double convertDouble(Object value) {
        if (value == null || "null".equals(String.valueOf(value))) {
            return 0L;
        } else {
            return Double.parseDouble(String.valueOf(value));
        }
    }

    protected static String eventName(EventApiEventDto event) {
        EventType eventType;
        try {
            eventType = EventType.valueOf(event.getType());
        } catch (IllegalArgumentException ex) {
            eventType = EventType.OTHER;
        }
        String properName = event.getProperName();

        if (eventType == EventType.OTHER && properName != null) {
            return properName;
        }
        if (properName != null) {
            return eventType.getName() + " " + properName;
        }
        return eventType.getName();
    }
}
