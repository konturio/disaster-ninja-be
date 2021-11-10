package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EventApiClient {

    private static final String EVENT_API_EVENT_LIST_URI = "/v1/?feed=%s&severities=EXTREME,SEVERE,MODERATE&after=%s&episodeFilterType=LATEST&limit=1000&sortOrder=ASC";
    private static final String EVENT_API_EVENT_ID_URI = "/v1/event?feed=%s&eventId=%s";

    private final RestTemplate restTemplate;

    @Value("${kontur.platform.event-api.feed}")
    private String eventApiFeed;

    public EventApiClient(RestTemplate eventApiRestTemplate) {
        this.restTemplate = eventApiRestTemplate;
    }

    public List<EventApiEventDto> getEvents(String jwtToken) {
        OffsetDateTime after = OffsetDateTime.now()
            .minusDays(4);

        List<EventApiEventDto> result = new ArrayList<>();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        while (true) {
            String uri = String.format(EVENT_API_EVENT_LIST_URI, eventApiFeed, after.truncatedTo(ChronoUnit.MINUTES)
                .atZoneSameInstant(ZoneOffset.UTC));

            ResponseEntity<EventApiSearchEventResponse> response = restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(null, headers), new ParameterizedTypeReference<>() {
                });
            if (response.getStatusCode() == HttpStatus.NO_CONTENT ||
                response.getBody() == null || response.getBody().data == null ||
                response.getBody().data.isEmpty() ||
                after.equals(response.getBody().pageMetadata.nextAfterValue)) { //todo workaround as can't get 204
                break;
            }
            result.addAll(response.getBody().data);
            after = response.getBody().pageMetadata.nextAfterValue;
        }
        return result;
    }

    public EventApiEventDto getEvent(UUID eventId, String jwtToken) {
        if (eventId == null) {
            return null;
        }
        String uri = String.format(EVENT_API_EVENT_ID_URI, eventApiFeed, eventId);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<EventApiEventDto> response = restTemplate
            .exchange(uri, HttpMethod.GET, new HttpEntity<>(null, headers), new ParameterizedTypeReference<>() {
            });

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
