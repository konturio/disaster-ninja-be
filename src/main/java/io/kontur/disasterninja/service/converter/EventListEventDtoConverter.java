package io.kontur.disasterninja.service.converter;

import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.dto.EventType;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;

import java.util.Map;

public class EventListEventDtoConverter {

    public static EventListDto convert(EventApiEventDto event) {
        EventListDto dto = new EventListDto();
        dto.setEventId(event.getEventId());

        EventType eventType;
        try {
            eventType = EventType.valueOf(event.getEpisodes().get(0).getType());
        } catch (IllegalArgumentException ex) {
            eventType = EventType.OTHER;
        }

        dto.setEventName(eventType.getName()); //TODO add properName
        dto.setSeverity(event.getEpisodes().get(0).getSeverity());
        Map<String, Object> eventDetails = event.getEventDetails();
        if (eventDetails != null) {
            dto.setLocation(String.valueOf(eventDetails.get("country"))); //TODO is it array?
            dto.setAffectedPopulation(convertLong(eventDetails.get("population")));
            dto.setSettledArea(convertLong(eventDetails.get("settledArea")));
            dto.setOsmGaps(convertLong(eventDetails.get("osmGapsPercentage")));
        } else {
            dto.setAffectedPopulation(0L);
            dto.setSettledArea(0L);
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
}
