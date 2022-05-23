package io.kontur.disasterninja.client;

import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class TestDependingOnUserAuth {

    @Mock
    protected SecurityContext securityContext;
    @MockBean
    protected KeycloakAuthorizationService keycloakAuthorizationService;

    protected void givenUserIsLoggedIn() {
        givenJwtTokenIs(getUserToken());
    }

    protected String getUserToken() {
        return "JwtTestToken";
    }

    protected void givenJwtTokenIs(String tokenValue) {
        Jwt jwt = Jwt.withTokenValue(tokenValue)
                .claim("some", "claim")
                .header(HttpHeaders.AUTHORIZATION, tokenValue)
                .build();

        Authentication authentication = new JwtAuthenticationToken(jwt);
        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    protected void givenUserIsNotAuthenticated() {
        securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
    }
}
