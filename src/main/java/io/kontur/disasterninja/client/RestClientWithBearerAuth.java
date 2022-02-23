package io.kontur.disasterninja.client;

import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
public abstract class RestClientWithBearerAuth {

    private final KeycloakAuthorizationService authorizationService;

    protected <T> HttpEntity<T> httpEntityWithUserBearerAuthIfPresent(T body) {
        return new HttpEntity<T>(body, httpHeadersWithUserBearerAuthIfPresent());
    }

    protected <T> HttpEntity<T> httpEntityWithUserOrDefaultBearerAuth(T body) {
        return new HttpEntity<T>(body, httpHeadersWithUserOrDefaultBearerAuth());
    }

    protected HttpHeaders httpHeadersWithUserBearerAuthIfPresent() {
        HttpHeaders headers = createHttpHeadersWithJsonMediaType();

        String userToken = getUserToken();
        if (userToken != null) {
            headers.setBearerAuth(userToken);
        }
        return headers;
    }

    protected HttpHeaders httpHeadersWithUserOrDefaultBearerAuth() {
        HttpHeaders headers = httpHeadersWithUserBearerAuthIfPresent();

        if (headers.get(AUTHORIZATION) == null) {
            headers.setBearerAuth(authorizationService.getAccessToken());
        }

        return headers;
    }

    private HttpHeaders createHttpHeadersWithJsonMediaType() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getUserToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof BearerTokenAuthenticationToken userToken
            && userToken.getToken() != null
            && !userToken.getToken().isBlank()) {
            return userToken.getToken();
        }
        return null;
    }
}
