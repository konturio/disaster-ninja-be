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
import lombok.RequiredArgsConstructor;

import static java.util.Comparator.*;

@Service
@RequiredArgsConstructor
public class EventApiService {
    private final EventApiClient client;

    public List<EventListDto> getEvents(String feed) {
        List<EventApiEventDto> events = client.getEvents(feed);
        List<EventListDto> eventList = convertListOfEvents(events);
        sort(eventList);
        return eventList;
    }

    public EventDto getEvent(UUID eventId, String feed) {
        EventApiEventDto event = client.getEvent(eventId, feed);
        if (event == null) {
            throw new WebApplicationException("Event " + eventId + " is not found", HttpStatus.NOT_FOUND);
        }
        return EventDtoConverter.convert(event);
    }

    public List<EventFeedDto> getUserFeeds() {
        return client.getUserFeeds();
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
        eventList.sort(Comparator.comparing(EventListDto::getAffectedPopulation, nullsFirst(naturalOrder()))
                .thenComparing(EventListDto::getUpdatedAt)
                .reversed());
    }
}

