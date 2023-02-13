package io.kontur.disasterninja.client;

import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@RequiredArgsConstructor
public abstract class RestClientWithBearerAuth {

    private final KeycloakAuthorizationService authorizationService;

    protected <T> HttpEntity<T> httpEntityWithUserBearerAuthIfPresentAndNoCacheHeader(T body) {
        HttpHeaders headers = httpHeadersWithUserBearerAuthIfPresent();
        headers.setCacheControl(CacheControl.noCache());
        return new HttpEntity<T>(body, headers);
    }

    protected <T> HttpEntity<T> httpEntityWithUserOrDefaultBearerAuth(T body) {
        return new HttpEntity<T>(body, httpHeadersWithUserOrDefaultBearerAuth());
    }

    protected <T> HttpEntity<T> httpEntityWithDefaultBearerAuth(T body) {
        return new HttpEntity<T>(body, httpHeadersWithDefaultBearerAuth());
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

    protected HttpHeaders httpHeadersWithDefaultBearerAuth() {
        HttpHeaders headers = createHttpHeadersWithJsonMediaType();
        headers.setBearerAuth(authorizationService.getAccessToken());
        return headers;
    }

    private HttpHeaders createHttpHeadersWithJsonMediaType() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getUserToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof BearerTokenAuthenticationToken bearerToken) {
            return bearerToken.getToken() == null ? null :
                    bearerToken.getToken();
        } else if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return jwtToken.getToken() == null ? null
                    : jwtToken.getToken().getTokenValue();
        }
        return null;
    }
}
