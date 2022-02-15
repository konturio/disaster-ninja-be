package io.kontur.disasterninja.client;

import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

@RequiredArgsConstructor
public abstract class RestClientWithBearerAuth {

    private final KeycloakAuthorizationService authorizationService;

    protected HttpHeaders httpHeadersWithBearerAuth() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        headers.setBearerAuth(getUserTokenOrDefaultAccessToken());
        return headers;
    }

    private String getUserTokenOrDefaultAccessToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof BearerTokenAuthenticationToken userToken)
            || userToken.getToken() == null
            || userToken.getToken().isBlank()) {
            return authorizationService.getAccessToken();
        }

        return userToken.getToken();
    }
}
