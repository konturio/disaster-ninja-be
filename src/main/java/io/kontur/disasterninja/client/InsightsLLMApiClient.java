package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.AnalyticsRequestDto;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

    public ResponseEntity<Object> getLLMAnalytics(AnalyticsRequestDto llmAnalyticsRequest) {
            return llmAnalyticsRestTemplate
                    .exchange("/llm-analytics", POST, httpEntityWithUserBearerAuthIfPresent(llmAnalyticsRequest),
                            new ParameterizedTypeReference<>() {
                            });
    }
}
