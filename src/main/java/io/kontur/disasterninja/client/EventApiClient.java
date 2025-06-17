package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kontur.disasterninja.dto.EventFeedDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import io.micrometer.core.annotation.Timed;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class EventApiClient extends RestClientWithBearerAuth {

    private static final Logger LOG = LoggerFactory.getLogger(EventApiClient.class);
    private static final String EVENT_API_EVENT_LIST_URI = "/v1/?feed={feed}&severities=EXTREME,SEVERE,MODERATE&limit={limit}";
    private static final String EVENT_API_EVENT_ID_URI = "/v1/event?feed={feed}&eventId={event}";
    private static final String EVENT_API_USER_FEEDS_URI = "/v1/user_feeds";

    private final RestTemplate restTemplate;

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

    public Optional<EventApiSearchEventResponse> getEvents(String eventApiFeed, OffsetDateTime after, List<BigDecimal> bbox, int pageSize, SortOrder sortOrder) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(EVENT_API_EVENT_LIST_URI);
        uriBuilder.queryParam("episodeFilterType", "NONE");
        uriBuilder.queryParam("sortOrder", Objects.requireNonNullElse(sortOrder, "ASC"));

        if (after != null) {
            uriBuilder.queryParam("after", after.atZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        }
        if (!CollectionUtils.isEmpty(bbox)) {
            uriBuilder.queryParam("bbox", bbox);
        }
        ResponseEntity<EventApiSearchEventResponse> response = restTemplate
                .exchange(uriBuilder.build(eventApiFeed, pageSize).toString(), HttpMethod.GET, httpEntityWithUserOrDefaultBearerAuth(null),
                        new ParameterizedTypeReference<>() {
                        });
        if (!response.getStatusCode().is2xxSuccessful()) {
            LOG.info("Received {} response from eventapi. Request uri: {}", response.getStatusCode(), uriBuilder.build(eventApiFeed, pageSize));
        }
        if (response.getStatusCode() == HttpStatus.NO_CONTENT ||
                response.getBody() == null || response.getBody().data == null ||
                response.getBody().data.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(response.getBody());
    }

    public List<EventApiEventDto> getLatestEvents(List<String> acceptableTypes, String feedName, int limit) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(EVENT_API_EVENT_LIST_URI);

        uriBuilder.queryParam("sortOrder", "DESC");
        uriBuilder.queryParam("episodeFilterType", "LATEST");
        if (!CollectionUtils.isEmpty(acceptableTypes)) {
            uriBuilder.queryParam("types", acceptableTypes);
        }
        ResponseEntity<EventApiSearchEventResponse> response = restTemplate
                .exchange(uriBuilder.build(feedName, limit).toString(), HttpMethod.GET, httpEntityWithUserOrDefaultBearerAuth(null),
                        new ParameterizedTypeReference<>() {
                        });
        if (!response.getStatusCode().is2xxSuccessful()) {
            LOG.info("Received {} response from eventapi. Request uri: {}", response.getStatusCode(), uriBuilder.build(feedName, limit));
        }
        if (response.getStatusCode() == HttpStatus.NO_CONTENT ||
                response.getBody() == null || response.getBody().data == null ||
                response.getBody().data.isEmpty()) {
            return new ArrayList<>();
        }
        return response.getBody().data;
    }

    public Optional<EventApiSearchEventResponse> getEventsBySeverities(String eventApiFeed,
                                                                     OffsetDateTime after,
                                                                     List<String> severities,
                                                                     int limit,
                                                                     SortOrder sortOrder) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/v1/");
        uriBuilder.queryParam("feed", eventApiFeed);
        uriBuilder.queryParam("limit", limit);
        uriBuilder.queryParam("episodeFilterType", "NONE");
        uriBuilder.queryParam("sortOrder", Objects.requireNonNullElse(sortOrder, "ASC"));

        if (after != null) {
            uriBuilder.queryParam("after", after.atZoneSameInstant(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ISO_ZONED_DATE_TIME));
        }
        if (!CollectionUtils.isEmpty(severities)) {
            uriBuilder.queryParam("severities", String.join(",", severities));
        }

        ResponseEntity<EventApiSearchEventResponse> response = restTemplate
                .exchange(uriBuilder.build().toString(), HttpMethod.GET,
                        httpEntityWithUserOrDefaultBearerAuth(null),
                        new ParameterizedTypeReference<>() {
                        });

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOG.info("Received {} response from eventapi. Request uri: {}", response.getStatusCode(), uriBuilder);
        }
        if (response.getStatusCode() == HttpStatus.NO_CONTENT ||
                response.getBody() == null || response.getBody().data == null ||
                response.getBody().data.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(response.getBody());
    }

    @Timed(value = "events.getEvent", histogram = true)
    public EventApiEventDto getEvent(UUID eventId, String eventApiFeed, boolean includeEpisodes) {
        if (eventId == null) {
            return null;
        }
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(EVENT_API_EVENT_ID_URI);
        if (includeEpisodes) {
            uriBuilder.queryParam("episodeFilterType", "ANY");
        } else {
            uriBuilder.queryParam("episodeFilterType", "NONE");
        }
        ResponseEntity<EventApiEventDto> response = restTemplate
                .exchange(uriBuilder.build(eventApiFeed, eventId).toString(), HttpMethod.GET, httpEntityWithUserOrDefaultBearerAuth(null),
                        new ParameterizedTypeReference<>() {
                        });
        if (!response.getStatusCode().is2xxSuccessful()) {
            LOG.info("Received {} response from eventapi. Request uri: {}", response.getStatusCode(), uriBuilder);
        }

        return response.getBody();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class EventApiSearchEventResponse {

        private List<EventApiEventDto> data = new ArrayList<>();
        private PageMetadata pageMetadata = new PageMetadata();
    }

    @Data
    public static class PageMetadata {

        private OffsetDateTime nextAfterValue;
    }

    public enum SortOrder {
        ASC,
        DESC
    }
}
