package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kontur.disasterninja.dto.EventFeedDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EventApiClient extends RestClientWithBearerAuth {

    private static final Logger LOG = LoggerFactory.getLogger(EventApiClient.class);
    private static final String EVENT_API_EVENT_LIST_URI = "/v1/?feed=%s&severities=EXTREME,SEVERE,MODERATE&after=%s&episodeFilterType=LATEST&limit=%s&sortOrder=ASC";
    private static final String EVENT_API_EVENT_ID_URI = "/v1/event?feed=%s&eventId=%s";
    private static final String EVENT_API_USER_FEEDS_URI = "/v1/user_feeds";

    private final RestTemplate restTemplate;

    @Value("${kontur.platform.event-api.feed}")
    private String defaultEventApiFeed; //todo will be removed - as this param will be provided in all FE requests
    @Value("${kontur.platform.event-api.pageSize}")
    private int pageSize;

    public EventApiClient(RestTemplate eventApiRestTemplate, KeycloakAuthorizationService authorizationService) {
        super(authorizationService);
        this.restTemplate = eventApiRestTemplate;
    }

    public List<EventFeedDto> getUserFeeds() {
        ResponseEntity<List<EventFeedDto>> response = restTemplate
            .exchange(EVENT_API_USER_FEEDS_URI, HttpMethod.GET, httpEntityWithUserOrDefaultBearerAuth(null),
                new ParameterizedTypeReference<>() {
            });

        return Optional.ofNullable(response.getBody()).orElseGet(List::of);
    }

    public List<EventApiEventDto> getEvents(String eventApiFeed) {
        if (eventApiFeed == null) {
            eventApiFeed = defaultEventApiFeed;
        }
        OffsetDateTime after = OffsetDateTime.now()
            .minusDays(4);

        List<EventApiEventDto> result = new ArrayList<>();

        while (true) {
            String uri = String.format(EVENT_API_EVENT_LIST_URI, eventApiFeed, after.atZoneSameInstant(ZoneOffset.UTC)
                .truncatedTo(ChronoUnit.MILLIS).format(DateTimeFormatter.ISO_ZONED_DATE_TIME), pageSize);
            ResponseEntity<EventApiSearchEventResponse> response = restTemplate
                .exchange(uri, HttpMethod.GET, httpEntityWithUserOrDefaultBearerAuth(null),
                    new ParameterizedTypeReference<>() {
                });
            if (!response.getStatusCode().is2xxSuccessful()) {
                LOG.info("Received {} response from eventapi. Request uri: {}", response.getStatusCode(), uri);
            }
            if (response.getStatusCode() == HttpStatus.NO_CONTENT ||
                response.getBody() == null || response.getBody().data == null ||
                response.getBody().data.isEmpty()) {
                break;
            }
            result.addAll(response.getBody().data);
            if (response.getBody().data.size() < pageSize) { //last page
                break;
            }
            after = response.getBody().pageMetadata.nextAfterValue;
        }
        return result;
    }

    public EventApiEventDto getEvent(UUID eventId, String eventApiFeed) {
        if (eventId == null) {
            return null;
        }
        if (eventApiFeed == null) {
            eventApiFeed = defaultEventApiFeed;
        }
        String uri = String.format(EVENT_API_EVENT_ID_URI, eventApiFeed, eventId);
        ResponseEntity<EventApiEventDto> response = restTemplate
            .exchange(uri, HttpMethod.GET, httpEntityWithUserOrDefaultBearerAuth(null),
                new ParameterizedTypeReference<>() {
            });
        if (!response.getStatusCode().is2xxSuccessful()) {
            LOG.info("Received {} response from eventapi. Request uri: {}", response.getStatusCode(), uri);
        }

        return response.getBody();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EventApiSearchEventResponse {

        public List<EventApiEventDto> data;
        public PageMetadata pageMetadata;
    }

    @Data
    private static class PageMetadata {
        public OffsetDateTime nextAfterValue;
    }
}
