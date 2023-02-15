package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.UserMetricDto;
import io.kontur.disasterninja.util.AuthenticationUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@Validated
@RequestMapping("/rum")
public class MetricsController {

    private Histogram histogram;

    public MetricsController(MeterRegistry meterRegistry) {
        Histogram.Builder metricsBuilder = Histogram.build("real_user_monitoring", "RUM metrics")
                .labelNames("name", "appId", "isUserLoggedIn", "buildVersion")
                .buckets(100, 300, 500, 1_000, 2_000, 3_000, 4_000, 5_000, 10_000, 30_000, 60_000, 120_000, 300_000);

        if (meterRegistry instanceof PrometheusMeterRegistry) {
            CollectorRegistry collectorRegistry = ((PrometheusMeterRegistry) meterRegistry).getPrometheusRegistry();
            histogram = metricsBuilder.register(collectorRegistry);
        } else if (meterRegistry instanceof CompositeMeterRegistry compositeMeterRegistry) {
            compositeMeterRegistry.getRegistries().stream()
                    .filter(registry -> registry.getClass().equals(PrometheusMeterRegistry.class))
                    .findFirst()
                    .ifPresent(prometheusMeterRegistry -> {
                        CollectorRegistry collectorRegistry = ((PrometheusMeterRegistry) prometheusMeterRegistry)
                                .getPrometheusRegistry();
                        histogram = metricsBuilder.register(collectorRegistry);
                    });
        } else {
            histogram = metricsBuilder.create();
        }
    }

    @Operation(summary = "RUM metrics push gateway", tags = {"Metrics"})
    @PostMapping("/metrics")
    public ResponseEntity<?> writeMetrics(@RequestBody @Valid List<UserMetricDto> metrics) {
        if (CollectionUtils.isEmpty(metrics)) {
            return ResponseEntity.ok().build();
        }
        metrics.forEach(metric -> {
            String appId = metric.getAppId() != null ? metric.getAppId().toString() : "null";
            metric.setUserLoggedIn(AuthenticationUtil.isUserAuthenticated());
            histogram.labels(metric.getName(), appId, String.valueOf(metric.isUserLoggedIn()),
                    metric.getBuildVersion()).observe(metric.getValue());
        });
        return ResponseEntity.ok().build();
    }
}
