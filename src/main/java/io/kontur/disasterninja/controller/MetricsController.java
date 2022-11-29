package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.UserMetricDto;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.composite.CompositeMeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
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

    private Summary summary;

    public MetricsController(MeterRegistry meterRegistry) {
        Summary.Builder metricsBuilder = Summary.build("real_user_monitoring", "RUM metrics")
                .quantile(0.5, 0.01)
                .quantile(0.75, 0.01)
                .quantile(0.95, 0.005)
                .quantile(0.99, 0.005)
                .quantile(1, 0)
                .labelNames("name", "appId", "userId", "buildVersion")
                .maxAgeSeconds(120)
                .ageBuckets(1);

        if (meterRegistry instanceof PrometheusMeterRegistry) {
            CollectorRegistry collectorRegistry = ((PrometheusMeterRegistry) meterRegistry).getPrometheusRegistry();
            summary = metricsBuilder.register(collectorRegistry);
        } else if (meterRegistry instanceof CompositeMeterRegistry compositeMeterRegistry) {
            compositeMeterRegistry.getRegistries().stream()
                    .filter(registry -> registry.getClass().equals(PrometheusMeterRegistry.class))
                    .findFirst()
                    .ifPresent(prometheusMeterRegistry -> {
                        CollectorRegistry collectorRegistry = ((PrometheusMeterRegistry) prometheusMeterRegistry)
                                .getPrometheusRegistry();
                        summary = metricsBuilder.register(collectorRegistry);
                    });
        } else {
            summary = metricsBuilder.create();
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
            String userId = metric.getUserId() != null ? metric.getUserId() : "null";
            if (UserMetricDto.UserMetricDtoType.SUMMARY.equals(metric.getType())) {
                summary.labels(metric.getName(), appId, userId, metric.getBuildVersion()).observe(metric.getValue());
            }
        });
        return ResponseEntity.ok().build();
    }
}
