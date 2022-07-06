package io.kontur.disasterninja.controller;

import io.kontur.disasterninja.dto.UserMetricDto;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Summary;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class MetricsController {

    private final Summary summary;

    public MetricsController(MeterRegistry meterRegistry) {
        Summary.Builder metricsBuilder = Summary.build("real_user_monitoring", "RUM metrics")
                .quantile(0.5, 0.01)
                .quantile(0.75, 0.01)
                .quantile(0.95, 0.005)
                .quantile(0.99, 0.005)
                .quantile(1, 0)
                .labelNames("name", "appId", "userId")
                .maxAgeSeconds(120)
                .ageBuckets(1);

        if (meterRegistry instanceof PrometheusMeterRegistry) {
            CollectorRegistry collectorRegistry = ((PrometheusMeterRegistry) meterRegistry).getPrometheusRegistry();
            summary = metricsBuilder.register(collectorRegistry);
        } else {
            summary = metricsBuilder.create();
        }
    }

    @Operation(summary = "RUM metrics push gateway", tags = {"Metrics"})
    @PostMapping("/metrics")
    public ResponseEntity<?> writeMetrics(@RequestBody List<UserMetricDto> metrics) {
        if (CollectionUtils.isEmpty(metrics)) {
            return ResponseEntity.ok().build();
        }
        metrics.forEach(metric -> {
            if (metric.getType().equals(UserMetricDto.UserMetricDtoType.summary)) {
                summary.labels(metric.getName(), metric.getAppId().toString(), metric.getUserId().toString())
                        .observe(metric.getValue());
            }
        });
        return ResponseEntity.ok().build();
    }

}
