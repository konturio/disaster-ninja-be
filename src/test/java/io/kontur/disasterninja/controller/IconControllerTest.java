package io.kontur.disasterninja.controller;

import static io.kontur.disasterninja.controller.IconController.DEFAULT_DOMAIN;
import static io.kontur.disasterninja.controller.IconController.SCHEMA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import io.kontur.disasterninja.dto.AppDto;
import io.kontur.disasterninja.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;

class IconControllerTest {
    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private IconController iconController;

    private MockMvc mockMvc;

    private final String EXPECTED_FAVICON_PATH = "/test/path/to/favicon.ico";
    private final String EXPECTED_REDIRECT_URL = SCHEMA + "://" + DEFAULT_DOMAIN + EXPECTED_FAVICON_PATH;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = standaloneSetup(iconController).build();
    }

    @Test
    public void whenXForwardedHostIsDisasterNinja_thenRedirectToFavicon() throws Exception {
        // Given
        givenUPSReturnsAppConfig(DEFAULT_DOMAIN);

        // When & Then
        mockMvc.perform(get("/favicon.ico").header("X-Forwarded-Host", DEFAULT_DOMAIN))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(EXPECTED_REDIRECT_URL));
    }

    @Test
    public void whenXForwardedHostIsNull_thenUseDefaultDomain() throws Exception {
        // Given
        givenUPSReturnsAppConfig(DEFAULT_DOMAIN);

        // When & Then
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(EXPECTED_REDIRECT_URL));
    }

    private void givenUPSReturnsAppConfig(String domain) {
        AppDto appDto = new AppDto();
        appDto.setFaviconUrl(EXPECTED_FAVICON_PATH);
        when(applicationService.getAppConfig(null, domain)).thenReturn(appDto);
    }
}