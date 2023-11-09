package io.kontur.disasterninja.controller;

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
import java.net.URISyntaxException;
import java.net.URL;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;

@RequiredArgsConstructor
@Controller
public class IconController {

    private final static Logger LOG = LoggerFactory.getLogger(IconController.class);
    public final static String DEFAULT_DOMAIN = "disaster.ninja";
    public final static String SCHEMA = "https";

    private final ApplicationService applicationService;

    @GetMapping(path = "/favicon.ico")
    public void getAppIcon(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String xForwardedHost = request.getHeader("X-Forwarded-Host");
        String domain = isBlank(xForwardedHost) ? DEFAULT_DOMAIN : xForwardedHost;
        AppDto appDto = applicationService.getAppConfig(null, domain);
        try {
            URI baseUri = new URI(SCHEMA, domain, null, null);
            URI fullUri = baseUri.resolve(appDto.getFaviconUrl());
            URL targetUrl = fullUri.toURL();
            response.sendRedirect(targetUrl.toString());
        } catch (URISyntaxException e) {
            LOG.error(format("Error creating icon URL from X-Forwarded-Host %s", xForwardedHost) + e.getMessage());
        }
    }
}
