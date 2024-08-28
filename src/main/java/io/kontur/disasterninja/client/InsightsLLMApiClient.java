package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.AnalyticsRequestDto;
import io.kontur.disasterninja.dto.LLMAnalyticsDto;
import io.kontur.disasterninja.dto.SearchDto;
import io.kontur.disasterninja.dto.SearchClickRequest;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.springframework.http.HttpMethod.*;

@Component
public class InsightsLLMApiClient extends RestClientWithBearerAuth {

    private static final Logger LOG = LoggerFactory.getLogger(InsightsLLMApiClient.class);

    private final RestTemplate llmAnalyticsRestTemplate;

    public InsightsLLMApiClient(RestTemplate llmAnalyticsRestTemplate,
                                KeycloakAuthorizationService authorizationService) {
        super(authorizationService);
        this.llmAnalyticsRestTemplate = llmAnalyticsRestTemplate;
    }

    public LLMAnalyticsDto getLLMAnalytics(AnalyticsRequestDto llmAnalyticsRequest) {
        ResponseEntity<LLMAnalyticsDto> response = llmAnalyticsRestTemplate
                .exchange("/llm-analytics", POST, httpEntityWithUserBearerAuthIfPresent(llmAnalyticsRequest),
                        new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public SearchDto search(UUID appId, String query) {
        ResponseEntity<SearchDto> response = llmAnalyticsRestTemplate
                .exchange(String.format("/search?appId=%s&query=%s", appId, query), GET,
                        httpEntityWithUserBearerAuthIfPresent(null),
                        new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    public ResponseEntity<?> logSearchClick(SearchClickRequest searchClickRequest) {
        return llmAnalyticsRestTemplate
                .exchange("/search/click", POST,
                        httpEntityWithUserBearerAuthIfPresent(searchClickRequest),
                        new ParameterizedTypeReference<>() {});
    }
}
