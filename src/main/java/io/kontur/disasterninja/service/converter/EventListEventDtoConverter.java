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
        dto.setLocations(event.getLocation());
        List<String> eventUrls = event.getUrls();
        dto.setExternalUrls(eventUrls != null ? List.copyOf(eventUrls) : List.of());

        dto.setSeverity(event.getEpisodes().get(0).getSeverity());
        Map<String, Object> eventDetails = event.getEventDetails();
        if (eventDetails != null) {
            dto.setAffectedPopulation(convertLong(eventDetails.get("population")));
            dto.setOsmGaps(convertLong(eventDetails.get("osmGapsPercentage")));
            dto.setSettledArea(convertDouble(event.getEventDetails().get("populatedAreaKm2")));
        } else {
            dto.setAffectedPopulation(0L);
            dto.setSettledArea(0d);
            dto.setOsmGaps(0L);
        }
        dto.setUpdatedAt(event.getUpdatedAt());

        return dto;
    }

    private static Long convertLong(Object value) {
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
            eventType = EventType.valueOf(event.getEpisodes().get(0).getType());
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
