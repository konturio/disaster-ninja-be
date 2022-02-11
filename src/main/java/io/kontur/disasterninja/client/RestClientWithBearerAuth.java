package io.kontur.disasterninja.client;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

public abstract class RestClientWithBearerAuth {

    protected HttpHeaders httpHeadersWithBearerAuth() {
        HttpHeaders headers = new HttpHeaders();
        BearerTokenAuthenticationToken
            token = (BearerTokenAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        headers.setBearerAuth(token.getToken());
        return headers;
    }
}
