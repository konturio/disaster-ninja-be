package io.kontur.disasterninja.service.converter;

import io.kontur.disasterninja.dto.EventListEventDto;
import io.kontur.disasterninja.dto.EventType;
import io.kontur.disasterninja.dto.eventapi.EventDto;

public class EventListEventDtoConverter {

    public static EventListEventDto convert(EventDto event) {
        EventListEventDto dto = new EventListEventDto();
        dto.setEventId(event.getEventId());

        EventType eventType;
        try {
            eventType = EventType.valueOf(event.getEpisodes().get(0).getType());
        } catch (IllegalArgumentException ex) {
            eventType = EventType.OTHER;
        }

        dto.setEventName(eventType.getName()); //TODO add properName
        dto.setLocation(String.valueOf(event.getEventDetails().get("country"))); //TODO is it array?
        dto.setSeverity(event.getEpisodes().get(0).getSeverity());
        dto.setAffectedPopulation(convertLong(event.getEventDetails().get("population")));
        dto.setSettledArea(convertLong(event.getEventDetails().get("settledArea")));
        dto.setOsmGaps(convertLong(event.getEventDetails().get("osmGapsPercentage")));
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
