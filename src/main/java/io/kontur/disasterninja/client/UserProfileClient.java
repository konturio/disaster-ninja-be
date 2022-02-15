package io.kontur.disasterninja.client;

import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserProfileClient extends RestClientWithBearerAuth {
    private static final String USER_PROFILE_API_USER_FEED_URL = "/features/user_feed";
    private final RestTemplate userProfileRestTemplate;

    public UserProfileClient(RestTemplate userProfileRestTemplate,
                             KeycloakAuthorizationService authorizationService) {
        super(authorizationService);
        this.userProfileRestTemplate = userProfileRestTemplate;
    }

    public String getUserDefaultFeed() {
        ResponseEntity<String> response = userProfileRestTemplate
            .exchange(USER_PROFILE_API_USER_FEED_URL, HttpMethod.GET, new HttpEntity<>(null, httpHeadersWithBearerAuth()),
                new ParameterizedTypeReference<>() {
                });

        return response.getBody();
    }
}
