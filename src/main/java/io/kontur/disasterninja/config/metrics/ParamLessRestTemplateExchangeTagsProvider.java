package io.kontur.disasterninja.config.metrics;

import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTags;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTagsProvider;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

import java.util.Arrays;

public class ParamLessRestTemplateExchangeTagsProvider implements RestTemplateExchangeTagsProvider {

    @Override
    public Iterable<Tag> getTags(String urlTemplate, HttpRequest request, ClientHttpResponse response) {
        return Arrays.asList(RestTemplateExchangeTags.method(request), getUriTag(request),
                RestTemplateExchangeTags.status(response), RestTemplateExchangeTags.clientName(request),
                RestTemplateExchangeTags.outcome(response));
    }

    private Tag getUriTag(HttpRequest request) {
        return Tag.of("uri", request.getURI().getPath());
    }
}
