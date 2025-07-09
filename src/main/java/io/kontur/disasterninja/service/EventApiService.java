package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.EventApiClient;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.dto.EventDto;
import io.kontur.disasterninja.dto.EventEpisodeListDto;
import io.kontur.disasterninja.dto.EventFeedDto;
import io.kontur.disasterninja.dto.EventListDto;
import io.kontur.disasterninja.dto.layer.LayerDetailsDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryDto;
import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.service.layers.LocalLayerConfigService;
import io.kontur.disasterninja.service.layers.providers.EventShapeLayerProvider;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.service.converter.EventDtoConverter;
import io.kontur.disasterninja.service.converter.EventListEventDtoConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

@Service
public class EventApiService {

    private final EventApiClient client;

    private final int pageSize;
    private final LocalLayerConfigService layerConfigService;

    public EventApiService(EventApiClient client, LocalLayerConfigService layerConfigService,
                           @Value("${kontur.platform.event-api.pageSize}") int pageSize) {
        this.client = client;
        this.layerConfigService = layerConfigService;
        this.pageSize = pageSize;
    }

    public List<EventListDto> getEvents(String feed, List<BigDecimal> bbox) {
        OffsetDateTime after = OffsetDateTime.now().minusDays(4).truncatedTo(ChronoUnit.HOURS);
        Optional<EventApiClient.EventApiSearchEventResponse> eventsResponse = client.getEvents(feed, after, bbox,
                pageSize, EventApiClient.SortOrder.ASC);

        List<EventApiEventDto> events = new ArrayList<>();
        if (eventsResponse.isEmpty() || eventsResponse.get().getData().size() < pageSize) {
            //in case of the amount of events in the last 4 days is less than pageSize than gather the latest 1000 events
            events.addAll(client.getEvents(feed, null, bbox, pageSize, EventApiClient.SortOrder.DESC)
                    .orElse(new EventApiClient.EventApiSearchEventResponse())
                    .getData());
        } else {
            events.addAll(eventsResponse.get().getData());
            after = eventsResponse.get().getPageMetadata().getNextAfterValue();

            while (true) {
                eventsResponse = client.getEvents(feed, after, bbox, pageSize, EventApiClient.SortOrder.ASC);
                if (eventsResponse.isEmpty() || CollectionUtils.isEmpty(eventsResponse.get().getData())) {
                    break;
                }
                events.addAll(eventsResponse.get().getData());
                if (eventsResponse.get().getData().size() < pageSize) { //last page
                    break;
                }
                after = eventsResponse.get().getPageMetadata().getNextAfterValue();
            }
        }

        List<EventListDto> result = convertListOfEvents(events);
        sort(result);
        return result;
    }

    public EventDto getEvent(UUID eventId, String feed) {
        EventApiEventDto event = client.getEvent(eventId, feed, false);
        if (event == null) {
            throw new WebApplicationException("Event " + eventId + " is not found", HttpStatus.NOT_FOUND);
        }
        EventDto dto = EventDtoConverter.convert(event);

        Layer layer = Layer.builder()
                .eventType(dto.getEventType())
                .id(EventShapeLayerProvider.EVENT_SHAPE_LAYER_ID)
                .source(LayerSource.builder()
                        .type(io.kontur.disasterninja.domain.enums.LayerType.GEOJSON)
                        .data(dto.getGeojson())
                        .build())
                .eventIdRequiredForRetrieval(true)
                .build();

        layerConfigService.applyConfig(layer);
        EventDto.AssociatedLayer associatedLayer = new EventDto.AssociatedLayer();
        associatedLayer.setMapData(LayerDetailsDto.fromLayer(layer));
        associatedLayer.setLayerInfo(LayerSummaryDto.fromLayer(layer));
        dto.setAssociatedLayer(associatedLayer);
        return dto;
    }

    public List<EventEpisodeListDto> getEventEpisodes(UUID eventId, String feed) {
        EventApiEventDto event = client.getEvent(eventId, feed, true);
        if (event == null) {
            throw new WebApplicationException("Event " + eventId + " is not found", HttpStatus.NOT_FOUND);
        }
        if (CollectionUtils.isEmpty(event.getEpisodes())) {
            return Collections.emptyList();
        }
        return event.getEpisodes().stream()
                .map(EventDtoConverter::convertEventEpisode)
                .sorted(Comparator.comparing(EventEpisodeListDto::getUpdatedAt))
                .collect(Collectors.toList());
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

