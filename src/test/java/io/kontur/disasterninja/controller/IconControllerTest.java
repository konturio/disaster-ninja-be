package io.kontur.disasterninja.controller;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.dto.AppDto;
import io.kontur.disasterninja.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class IconControllerTest {
    @Mock
    private ApplicationService applicationService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private IconController iconController;

    private final ObjectMapper mapper = new ObjectMapper();
    private final String FAVICON_PACK_JSON = "{" +
            "  \"favicon.svg\": \"/active/static/favicon/favicon.svg\"," +
            "  \"favicon.ico\": \"/active/static/favicon/favicon.ico\"," +
            "  \"apple-touch-icon.png\": \"/active/static/favicon/apple-touch-icon.png\"," +
            "  \"icon-192x192.png\": \"/active/static/favicon/icon-192x192.png\"" +
            "}";
    private AppDto appDto = new AppDto();

    @BeforeEach
    public void setUp() throws JsonProcessingException {
        MockitoAnnotations.initMocks(this);
        appDto.setFaviconPack(mapper.readTree(FAVICON_PACK_JSON));
    }

    @Test
    public void testValidDomainWithKnownIconType() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-Host")).thenReturn("example.com");
        when(applicationService.getAppConfig(null, "example.com")).thenReturn(appDto);

        // When
        iconController.getAppSvgFavicon(request, response);

        // Then
        verify(response).sendRedirect("https://example.com/active/static/favicon/favicon.svg");
    }

    @Test
    public void testNoDomainPresent() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-Host")).thenReturn(null);

        // When
        iconController.getAppSvgFavicon(request, response);

        // Then
        verify(response).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testRequiredIconNotPresent() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-Host")).thenReturn("example.com");
        when(applicationService.getAppConfig(null, "example.com")).thenReturn(appDto);

        // When
        iconController.getAppPng512Favicon(request, response);

        // Then
        verify(response).sendError(SC_NOT_FOUND);
    }

    @Test
    public void testNoIconsPresent() throws Exception {
        // Given
        when(request.getHeader("X-Forwarded-Host")).thenReturn("example.com");
        when(applicationService.getAppConfig(null, "example.com")).thenReturn(new AppDto());

        // When
        iconController.getAppSvgFavicon(request, response);

        // Then
        verify(response).sendError(SC_NOT_FOUND);
    }
}