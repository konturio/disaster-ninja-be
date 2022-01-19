package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventFeedDto;
import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.service.converter.EventDtoConverter;
import io.kontur.disasterninja.service.converter.EventListEventDtoConverter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class EventApiService {

    private final EventApiClient client;
    private final KeycloakAuthorizationService authorizationService;

    public EventApiService(EventApiClient client,
                           KeycloakAuthorizationService authorizationService) {
        this.client = client;
        this.authorizationService = authorizationService;
    }

    public List<EventListDto> getEvents(String feed) {
        String accessToken = authorizationService.getAccessToken();
        List<EventApiEventDto> events = client.getEvents(accessToken, feed);
        List<EventListDto> eventList = convertListOfEvents(events);
        sort(eventList);
        return eventList;
    }

    public EventDto getEvent(UUID eventId) { //todo will be removed. feed will be provided in all FE requests
        return getEvent(eventId, null);
    }

    public EventDto getEvent(UUID eventId, String feed) {
        String accessToken = authorizationService.getAccessToken();
        EventApiEventDto event = client.getEvent(eventId, accessToken, feed);
        if (event == null) {
            throw new WebApplicationException("Event " + eventId + " is not found", HttpStatus.NOT_FOUND);
        }
        return EventDtoConverter.convert(event);
    }

    public List<EventFeedDto> getUserFeeds(String userToken) {
        if (userToken == null || userToken.isBlank()) {
            return client.getUserFeeds(authorizationService.getAccessToken()); //default DN2 token - for public users
        }
        return client.getUserFeeds(userToken);
    }

    private List<EventListDto> convertListOfEvents(List<EventApiEventDto> events) {
        List<EventListDto> result = new ArrayList<>(events.size());
        events.forEach(event -> result.add(EventListEventDtoConverter.convert(event)));
        return result;
    }

    /**
     * Events sequence:
     * -with humanitarian impact from events from high to low exposed ppl number
     * -for events with no humanitarian impact: by the last updates time
     */
    private void sort(List<EventListDto> eventList) {
        eventList.sort(Comparator.comparing(EventListDto::getAffectedPopulation)
                .thenComparing(EventListDto::getUpdatedAt).reversed());
    }
}

