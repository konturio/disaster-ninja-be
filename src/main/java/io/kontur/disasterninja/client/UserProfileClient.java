package io.kontur.disasterninja.client;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserProfileClient extends RestClientWithBearerAuth {
    private static final String USER_PROFILE_API_USER_FEED_URL = "/features/user_feed";
    private final RestTemplate userProfileRestTemplate;

    public String getUserDefaultFeed() {
        ResponseEntity<String> response = userProfileRestTemplate
            .exchange(USER_PROFILE_API_USER_FEED_URL, HttpMethod.GET, new HttpEntity<>(null, httpHeadersWithBearerAuth()),
                new ParameterizedTypeReference<>() {
                });

        return response.getBody();
    }
}
