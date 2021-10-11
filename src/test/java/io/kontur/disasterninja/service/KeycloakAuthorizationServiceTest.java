package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.KeycloakClient;
import io.kontur.disasterninja.dto.TokenResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@ExtendWith(MockitoExtension.class)
class KeycloakAuthorizationServiceTest {

    @Mock
    KeycloakClient keycloakClient;

    @InjectMocks
    KeycloakAuthorizationService service;

    @BeforeEach
    public void resetServiceCache() {
        ReflectionTestUtils.setField(service, "tokenResponse", null);
        ReflectionTestUtils.setField(service, "tokenExpiration", null);
    }

    @Test
    public void testGetAccessToken() {
        //given
        when(keycloakClient.getToken()).thenReturn(new TokenResponse("accessToken", 10_000L));

        //when
        String token = service.getAccessToken();
        service.getAccessToken();

        //then
        assertEquals("accessToken", token);
    }

    @Test
    public void testGetAccessTokenFromCache() {
        //given
        when(keycloakClient.getToken()).thenReturn(new TokenResponse("accessToken", 100_000L));

        //when
        service.getAccessToken();
        service.getAccessToken();

        //then
        verify(keycloakClient, times(1)).getToken();
    }

    @Test
    public void testReturnOldTokenOnError() {
        //given
        when(keycloakClient.getToken()).thenReturn(new TokenResponse("accessToken", 10L));
        service.getAccessToken();
        when(keycloakClient.getToken()).thenThrow(new HttpServerErrorException(SERVICE_UNAVAILABLE));

        //when
        String token = service.getAccessToken();

        //then
        assertEquals("accessToken", token);
    }

}