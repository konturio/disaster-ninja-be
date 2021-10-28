package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kontur.disasterninja.dto.eventapi.EventApiEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
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
        String then = OffsetDateTime.now()
            .minusDays(4)
            .truncatedTo(ChronoUnit.SECONDS)
            .atZoneSameInstant(ZoneOffset.UTC)
            .toString();

        String uri = String.format(EVENT_API_EVENT_LIST_URI, eventApiFeed, then);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<EventApiSearchEventResponse> response = restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(null, headers), new ParameterizedTypeReference<>() {
                });

        if (response.getBody() == null) {
            return List.of();
        }
        return response.getBody().data;
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
    }
}
