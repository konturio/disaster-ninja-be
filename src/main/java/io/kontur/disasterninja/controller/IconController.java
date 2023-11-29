package io.kontur.disasterninja.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.kontur.disasterninja.dto.AppDto;
import io.kontur.disasterninja.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

@RequiredArgsConstructor
@Controller
public class IconController {

    private final static Logger LOG = LoggerFactory.getLogger(IconController.class);
    public final static String SCHEMA = "https";

    private final ApplicationService applicationService;

    @GetMapping(path = "/favicon.svg")
    public void getAppSvgFavicon(HttpServletRequest request, HttpServletResponse response) throws IOException {
        getAppIconByDomain(request, response, "favicon.svg");
    }

    @GetMapping(path = "/favicon.ico")
    public void getAppIcoFavicon(HttpServletRequest request, HttpServletResponse response) throws IOException {
        getAppIconByDomain(request, response, "favicon.ico");
    }

    @GetMapping(path = "/apple-touch-icon.png")
    public void getAppAppleFavicon(HttpServletRequest request, HttpServletResponse response) throws IOException {
        getAppIconByDomain(request, response, "apple-touch-icon.png");
    }

    @GetMapping(path = "/icon-192x192.png")
    public void getAppPng192Favicon(HttpServletRequest request, HttpServletResponse response) throws IOException {
        getAppIconByDomain(request, response, "icon-192x192.png");
    }

    @GetMapping(path = "/icon-512x512.png")
    public void getAppPng512Favicon(HttpServletRequest request, HttpServletResponse response) throws IOException {
        getAppIconByDomain(request, response, "icon-512x512.png");
    }

    private void getAppIconByDomain(HttpServletRequest request, HttpServletResponse response, String iconType) throws IOException {
        String domain = request.getHeader("X-Forwarded-Host");
        if (domain != null) {
            AppDto appDto = applicationService.getAppConfig(null, domain);
            try {
                URI baseUri = new URI(SCHEMA, domain, null, null);
                URI fullUri = baseUri.resolve(findFaviconPath(appDto.getFaviconPack(), iconType));
                URL targetUrl = fullUri.toURL();
                response.sendRedirect(targetUrl.toString());
                return;
            } catch (Exception e) {
                LOG.error(format("Error creating icon URL from X-Forwarded-Host %s", domain) + e.getMessage());
            }
        }
        response.sendError(SC_NOT_FOUND);
    }

    private String findFaviconPath(JsonNode faviconPack, String iconType) {
        if (faviconPack != null) {
            JsonNode iconNode = faviconPack.get(iconType);
            if (iconNode != null) {
                return iconNode.asText();
            }
        }
        throw new RuntimeException("Requested icon type is unknown: " + iconType);
    }
}
