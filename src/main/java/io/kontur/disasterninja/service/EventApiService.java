package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.dto.EventListEventDto;
import io.kontur.disasterninja.dto.eventapi.EventDto;
import io.kontur.disasterninja.service.converter.EventListEventDtoConverter;
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
        events.forEach(event -> result.add(EventListEventDtoConverter.convert(event)));
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
}

