package io.kontur.disasterninja.client;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ConditionalOnProperty(name = "kontur.platform.insightsApi.url")
public class InsightsApiClientImpl implements InsightsApiClient {

    private static final String INSIGHTS_API_TILE_MVT_URI = "/tiles/bivariate/v1/%s/%s/%s.mvt?indicatorsClass=%s";

    private final RestTemplate insightsApiRestTemplate;

    private final static String AUTHENTICATED = "AUTHENTICATED";

    private final static String NOT_AUTHENTICATED = "NOT_AUTHENTICATED";

    private final Counter metrics;

    public InsightsApiClientImpl(RestTemplate insightsApiRestTemplate, MeterRegistry meterRegistry) {
        this.insightsApiRestTemplate = insightsApiRestTemplate;
        Counter.Builder metricsBuilder = Counter.build()
                .labelNames("authenticated")
                .name("http_server_authenticated_requests")
                .help("Requests divided by auth token.");

        if (meterRegistry instanceof PrometheusMeterRegistry) {
            CollectorRegistry collectorRegistry = ((PrometheusMeterRegistry) meterRegistry).getPrometheusRegistry();
            metrics = metricsBuilder.register(collectorRegistry);
        } else {
            metrics = metricsBuilder.create();
        }
    }

    @Override
    public byte[] getBivariateTileMvt(Integer z, Integer x, Integer y, String indicatorsClass) {
        if ("all".equals(indicatorsClass)) {
            metrics.labels(AUTHENTICATED).inc();
        } else {
            metrics.labels(NOT_AUTHENTICATED).inc();
        }
        return insightsApiRestTemplate.getForEntity(String.format(INSIGHTS_API_TILE_MVT_URI, z, x, y, indicatorsClass), byte[].class)
                .getBody();
    }
}
