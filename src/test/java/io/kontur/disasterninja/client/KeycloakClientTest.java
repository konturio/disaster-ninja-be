package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;

import static io.kontur.disasterninja.util.TestUtil.readFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest(KeycloakClient.class)
@AutoConfigureWebClient(registerRestTemplate = true)
class KeycloakClientTest {

    @Autowired
    private KeycloakClient client;
    @Autowired
    private MockRestServiceServer server;

    @Test
    public void testGetAccessToken() throws IOException {
        //given
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", "event-api");
        formData.add("username", "testUsername");
        formData.add("password", "testPassword");
        formData.add("grant_type", "password");

        server.expect(ExpectedCount.once(), requestTo("/realms/testRealm/protocol/openid-connect/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().formData(formData))
                .andRespond(withSuccess(readFile(this, "KeycloakClientTest.testGetAccessToken.response.json"),
                        MediaType.APPLICATION_JSON));

        //when
        TokenResponse token = client.getToken();

        //then
        assertEquals("TestAuthToken", token.getAccessToken());
        assertEquals(3600L, token.getExpiresIn());
    }
}