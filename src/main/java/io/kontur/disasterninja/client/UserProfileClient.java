package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.AppDto;
import io.kontur.disasterninja.dto.AppSummaryDto;
import io.kontur.disasterninja.dto.FeatureDto;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpMethod.*;

@Component
public class UserProfileClient extends RestClientWithBearerAuth {
    private static final String FEATURES_USER_FEED_URL = "/features/user_feed";
    private static final String FEATURES_URL = "/features";
    private static final String APP_URL = "/apps";
    private static final String APP_URL_WITH_ID = "/apps/{id}";
    private final RestTemplate userProfileRestTemplate;

    public UserProfileClient(RestTemplate userProfileRestTemplate,
                             KeycloakAuthorizationService authorizationService) {
        super(authorizationService);
        this.userProfileRestTemplate = userProfileRestTemplate;
    }

    public String getUserDefaultFeed() {
        ResponseEntity<String> response = userProfileRestTemplate
            .exchange(FEATURES_USER_FEED_URL, HttpMethod.GET, httpEntityWithUserBearerAuthIfPresent(null),
                new ParameterizedTypeReference<>() {
                });

        return response.getBody();
    }

    public List<FeatureDto> getUserAppFeatures(UUID appId) {
        String url;
        if (appId != null) {
            url = FEATURES_URL + "?appId=" + appId;
        } else {
            url = FEATURES_URL;
        }

        ResponseEntity<List<FeatureDto>> response = userProfileRestTemplate
            .exchange(url, HttpMethod.GET, httpEntityWithUserBearerAuthIfPresent(null),
                new ParameterizedTypeReference<>() {
                }, appId);

        return response.getBody();
    }

    public List<AppSummaryDto> getAppsList() {
        ResponseEntity<List<AppSummaryDto>> response = userProfileRestTemplate
            .exchange(APP_URL, HttpMethod.GET, httpEntityWithUserBearerAuthIfPresent(null),
                new ParameterizedTypeReference<>() {
                });

        return response.getBody();
    }

    public AppDto getApp(UUID id) {
        return sendAppRequest(APP_URL_WITH_ID, id, GET, null).getBody();
    }

    public AppDto createApp(AppDto input) {
        return sendAppRequest(APP_URL, null, POST, input).getBody();
    }

    public AppDto updateApp(UUID id, AppDto update) {
        return sendAppRequest(APP_URL_WITH_ID, id, PUT, update).getBody();
    }

    public ResponseEntity<?> deleteApp(UUID id) {
        return sendAppRequest(APP_URL_WITH_ID, id, DELETE, null);
    }

    private ResponseEntity<AppDto> sendAppRequest(String url, Object queryParam, HttpMethod method, AppDto body) {
        return userProfileRestTemplate
            .exchange(url, method, httpEntityWithUserBearerAuthIfPresent(body),
                new ParameterizedTypeReference<>() {
                }, queryParam);
    }
}
