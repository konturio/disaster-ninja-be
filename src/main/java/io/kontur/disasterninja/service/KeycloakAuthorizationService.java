package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.KeycloakClient;
import io.kontur.disasterninja.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Instant;

@Service
public class KeycloakAuthorizationService {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakAuthorizationService.class);
    private static volatile TokenResponse tokenResponse = null;
    private static volatile Instant tokenExpiration = null;

    private final KeycloakClient keycloakClient;

    public KeycloakAuthorizationService(KeycloakClient keycloakClient) {
        this.keycloakClient = keycloakClient;
    }

    public String getAccessToken() {
        if (tokenResponse == null || tokenExpiration == null || tokenExpiration.isBefore(Instant.now())) {
            try {
                synchronized (this) {
                    if (tokenResponse == null || tokenExpiration == null || tokenExpiration.isBefore(Instant.now())) {
                        TokenResponse token = keycloakClient.getToken();
                        saveToken(token);
                    }
                }
            } catch (RestClientException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
        return tokenResponse.getAccessToken();
    }

    private void saveToken(TokenResponse token) {
        Instant expiresAt = Instant.now().plusSeconds(token.getExpiresIn());
        tokenResponse = token;
        tokenExpiration = expiresAt.minusSeconds(30);
    }

}
