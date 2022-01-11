package io.kontur.disasterninja.client;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import io.kontur.disasterninja.dto.BivariateStatisticDto;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.SimpleTimer;
import io.prometheus.client.Summary;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.wololo.geojson.GeoJSON;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class InsightsApiGraphqlClient {

    private final ApolloClient apolloClient;
    private final static String SUCCESS = "SUCCESS";
    private final static String FAILED = "FAILED";
    private final Summary metrics; //todo use histogram once we know which buckets to use

    public InsightsApiGraphqlClient(ApolloClient apolloClient, MeterRegistry meterRegistry) {
        this.apolloClient = apolloClient;
        Summary.Builder metricsBuilder = Summary.build()
                .quantile(0.5, 0.01)
                .quantile(1, 0.01) //max
                .labelNames("outcome")
                .name("http_client_insights_api_graphql_requests_seconds")
                .help("Requests with insights-api GraphQl.")
                .maxAgeSeconds(120) //same as in micrometer (for spring restTemplates)
                .ageBuckets(1); //same as in micrometer (for spring restTemplates)

        if (meterRegistry instanceof PrometheusMeterRegistry) {
            CollectorRegistry collectorRegistry = ((PrometheusMeterRegistry) meterRegistry).getPrometheusRegistry();
            metrics = metricsBuilder.register(collectorRegistry);
        } else {
            //for tests only
            metrics = metricsBuilder.create();
        }
    }

    public CompletableFuture<List<AnalyticsTabQuery.Function>> analyticsTabQuery(GeoJSON polygon, List<FunctionArgs> functionArgs) {
        CompletableFuture<List<AnalyticsTabQuery.Function>> future = new CompletableFuture<>();
        SimpleTimer timer = new SimpleTimer();
        apolloClient
                .query(new AnalyticsTabQuery(Input.optional(polygon), Input.optional(functionArgs)))
                .enqueue(new ApolloCall.Callback<>() {
                    @Override
                    public void onResponse(@NotNull Response<AnalyticsTabQuery.Data> response) {
                        metrics.labels(SUCCESS).observe(timer.elapsedSeconds());

                        if (response.getData() != null && response.getData().polygonStatistic() != null &&
                                response.getData().polygonStatistic().analytics() != null &&
                                response.getData().polygonStatistic().analytics().functions() != null) {
                            future.complete(response.getData().polygonStatistic().analytics().functions());
                        }
                        future.complete(List.of());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        observeMetricsAndCompleteExceptionally(e, future, timer);
                    }
                });
        return future;
    }

    public CompletableFuture<BivariateStatisticDto> getBivariateStatistic() {
        CompletableFuture<BivariateStatisticDto> future = new CompletableFuture<>();
        SimpleTimer timer = new SimpleTimer();
        apolloClient
                .query(new BivariateLayerLegendQuery(Input.optional(null)))
                .enqueue(new ApolloCall.Callback<>() {
                    @Override
                    public void onResponse(@NotNull Response<BivariateLayerLegendQuery.Data> response) {
                        metrics.labels(SUCCESS).observe(timer.elapsedSeconds());

                        if (response.getData() != null &&
                                response.getData().polygonStatistic() != null &&
                                response.getData().polygonStatistic().bivariateStatistic() != null &&
                                response.getData().polygonStatistic().bivariateStatistic().overlays() != null &&
                                response.getData().polygonStatistic().bivariateStatistic().indicators() != null) {
                            BivariateStatisticDto bivariateStatisticDto = BivariateStatisticDto.builder()
                                    .overlays(response.getData().polygonStatistic().bivariateStatistic().overlays())
                                    .indicators(response.getData().polygonStatistic().bivariateStatistic().indicators())
                                    .build();
                            future.complete(bivariateStatisticDto);
                        }
                        future.complete(new BivariateStatisticDto());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        observeMetricsAndCompleteExceptionally(e, future, timer);
                    }
                });
        return future;
    }

    public <T> void observeMetricsAndCompleteExceptionally(@NotNull ApolloException e,
                                                                  @NotNull CompletableFuture<T> future,
                                                                  @NotNull SimpleTimer timer) {
        metrics.labels(FAILED).observe(timer.elapsedSeconds());
        future.completeExceptionally(e);
    }
}
