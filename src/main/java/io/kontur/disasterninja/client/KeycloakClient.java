package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.TokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
public class KeycloakClient {

    private final RestTemplate restTemplate;

    @Value("${kontur.platform.keycloak.realm}")
    private String keycloakRealm;
    @Value("${kontur.platform.keycloak.clientId}")
    private String keycloakClientId;
    @Value("${kontur.platform.keycloak.username}")
    private String keycloakUsername;
    @Value("${kontur.platform.keycloak.password}")
    private String keycloakPassword;

    public KeycloakClient(RestTemplate authorizationRestTemplate) {
        this.restTemplate = authorizationRestTemplate;
    }

    public TokenResponse getToken() {
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("client_id", keycloakClientId);
        params.add("username", keycloakUsername);
        params.add("password", keycloakPassword);
        params.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);


        ResponseEntity<TokenResponse> response = restTemplate
                .exchange(String.format("/realms/%s/protocol/openid-connect/token", keycloakRealm),
                        HttpMethod.POST, new HttpEntity<>(params, headers), new ParameterizedTypeReference<>() {
                        });

        return Objects.requireNonNull(response.getBody());
    }
}
