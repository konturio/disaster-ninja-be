package io.kontur.disasterninja.client;

import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.BearerTokenAuthenticationToken;

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

    protected void givenJwtTokenIs(String jwt) {
        Authentication authentication = new BearerTokenAuthenticationToken(jwt);
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
