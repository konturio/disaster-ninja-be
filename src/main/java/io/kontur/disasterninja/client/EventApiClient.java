package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.kontur.disasterninja.dto.eventapi.EventDto;
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
import java.util.Collections;
import java.util.List;

@Component
public class EventApiClient {

    private static final String EVENT_API_EVENT_LIST_URI = "/v1/?feed=%s&severities=EXTREME,SEVERE,MODERATE&after=%s&episodeFilterType=LATEST&limit=1000&sortOrder=ASC";

    private final RestTemplate restTemplate;

    @Value("${kontur.platform.event-api.feed}")
    private String eventApiFeed;

    public EventApiClient(RestTemplate eventApiRestTemplate) {
        this.restTemplate = eventApiRestTemplate;
    }

    public List<EventDto> getEvents(String jwtToken) {
        String then = OffsetDateTime.now()
                .minusDays(4)
                .truncatedTo(ChronoUnit.SECONDS)
                .atZoneSameInstant(ZoneOffset.UTC)
                .toString();

        String uri = String.format(EVENT_API_EVENT_LIST_URI, eventApiFeed, then);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);

        ResponseEntity<EventApiEventDto> response = restTemplate
                .exchange(uri, HttpMethod.GET, new HttpEntity<>(null, headers), new ParameterizedTypeReference<>() {
                });

        if (response.getBody() == null) {
            return Collections.emptyList();
        }
        return response.getBody().data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EventApiEventDto {

        public List<EventDto> data;
    }
}
