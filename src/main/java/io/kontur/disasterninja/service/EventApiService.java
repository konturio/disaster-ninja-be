package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.dto.EventListEventDto;
import io.kontur.disasterninja.dto.EventType;
import io.kontur.disasterninja.dto.eventapi.EventDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class EventApiService {

    private final EventApiClient client;
    private final KeycloakAuthorizationService authorizationService;

    public EventApiService(EventApiClient client,
                           KeycloakAuthorizationService authorizationService) {
        this.client = client;
        this.authorizationService = authorizationService;
    }

    public List<EventListEventDto> getEvents() {
        String accessToken = authorizationService.getAccessToken();
        List<EventDto> events = client.getEvents(accessToken);
        List<EventListEventDto> eventList = convert(events);
        sort(eventList);
        return eventList;
    }

    private List<EventListEventDto> convert(List<EventDto> events) {
        List<EventListEventDto> result = new ArrayList<>(events.size());
        events.forEach(e -> {
            EventListEventDto dto = new EventListEventDto();
            dto.setEventId(e.getEventId());

            EventType eventType;
            try {
                eventType = EventType.valueOf(e.getEpisodes().get(0).getType());
            } catch (IllegalArgumentException ex) {
                eventType = EventType.OTHER;
            }

            dto.setEventName(eventType.getName()); //TODO add properName
            dto.setLocation(String.valueOf(e.getEventDetails().get("country"))); //TODO is it array?
            dto.setSeverity(e.getEpisodes().get(0).getSeverity());
            dto.setAffectedPopulation(convertLong(e.getEventDetails().get("population")));
            dto.setSettledArea(convertLong(e.getEventDetails().get("settledArea")));
            dto.setOsmGaps(convertLong(e.getEventDetails().get("osmGapsPercentage")));
            dto.setUpdatedAt(e.getUpdatedAt());
            result.add(dto);
        });
        return result;
    }

    /**
     * Events sequence:
     * -with humanitarian impact from events from high to low exposed ppl number
     * -for events with no humanitarian impact: by the last updates time
     */
    private void sort(List<EventListEventDto> eventList) {
        eventList.sort(Comparator.comparing(EventListEventDto::getAffectedPopulation)
                .thenComparing(EventListEventDto::getUpdatedAt).reversed());
    }

    private Long convertLong(Object value) {
        if (value == null || "null".equals(String.valueOf(value))) {
            return 0L;
        } else {
            return Math.round(Double.parseDouble(String.valueOf(value)));
        }
    }
}

