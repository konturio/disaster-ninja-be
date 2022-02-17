package io.kontur.disasterninja.client;

import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserProfileClient extends RestClientWithBearerAuth {
    private static final Logger LOG = LoggerFactory.getLogger(UserProfileClient.class);
    private static final String USER_PROFILE_API_USER_FEED_URL = "/features/user_feed";
    private final RestTemplate userProfileRestTemplate;

    public UserProfileClient(RestTemplate userProfileRestTemplate,
                             KeycloakAuthorizationService authorizationService) {
        super(authorizationService);
        this.userProfileRestTemplate = userProfileRestTemplate;
    }

    public String getUserDefaultFeed() {
        ResponseEntity<String> response = userProfileRestTemplate
            .exchange(USER_PROFILE_API_USER_FEED_URL, HttpMethod.GET, httpEntityWithUserBearerAuthIfPresent(null),
                new ParameterizedTypeReference<>() {
                });

        String feed = response.getBody();
        LOG.info("Get user default feed from UPS: {}", feed);
        return feed;
    }
}
