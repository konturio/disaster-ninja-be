package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kontur.disasterninja.dto.EventFeedDto;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import io.micrometer.core.annotation.Timed;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
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
    private static final String EVENT_API_EVENT_LIST_URI = "/v1/?feed=%s&severities=EXTREME,SEVERE,MODERATE&episodeFilterType=LATEST&limit=%s";
    private static final String EVENT_API_EVENT_ID_URI = "/v1/event?feed=%s&eventId=%s";
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
        String uri = String.format(EVENT_API_EVENT_LIST_URI, eventApiFeed, pageSize);
        if (sortOrder == null) {
            uri += "&sortOrder=ASC";
        } else {
            uri += "&sortOrder=" + sortOrder;
        }

        if (after != null) {
            uri += "&after=" + after.atZoneSameInstant(ZoneOffset.UTC)
                    .truncatedTo(ChronoUnit.MILLIS).format(DateTimeFormatter.ISO_ZONED_DATE_TIME);
        }
        if (!CollectionUtils.isEmpty(bbox)) {
            uri += "&bbox=" + StringUtils.join(bbox, ",");
        }
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
            return Optional.empty();
        }
        return Optional.ofNullable(response.getBody());
    }

    public List<EventApiEventDto> getLatestEvents(List<String> acceptableTypes, String feedName, int limit) {
        String uri = String.format(EVENT_API_EVENT_LIST_URI, feedName, limit);
        uri += "&sortOrder=DESC";
        if (!CollectionUtils.isEmpty(acceptableTypes)) {
            uri += "&types=" + String.join(",", acceptableTypes);
        }
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
            return new ArrayList<>();
        }
        return response.getBody().data;
    }

    @Timed(percentiles = {0.5, 0.75, 0.9, 0.99})
    public EventApiEventDto getEvent(UUID eventId, String eventApiFeed) {
        if (eventId == null) {
            return null;
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
