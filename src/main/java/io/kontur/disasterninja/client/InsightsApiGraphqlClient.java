package io.kontur.disasterninja.client;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.graphql.BivariateLayerLegendQuery;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.SimpleTimer;
import io.prometheus.client.Summary;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.wololo.geojson.GeoJSON;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class InsightsApiGraphqlClient {

    private final ApolloClient apolloClient;
    private final static String SUCCESSFUL = "SUCCESSFUL";
    private final static String FAILED = "FAILED";
    private final Summary metrics;

    public InsightsApiGraphqlClient(ApolloClient apolloClient, CollectorRegistry collectorRegistry) {
        this.apolloClient = apolloClient;
        metrics = Summary.build()
            .labelNames("status")
            .name("http_client_insights_api_graphql_requests_seconds")
            .help("Requests with insights-api GraphQl.")
            .register(collectorRegistry);
    }

    public CompletableFuture<List<AnalyticsTabQuery.Function>> analyticsTabQuery(GeoJSON polygon, List<FunctionArgs> functionArgs) {
        CompletableFuture<List<AnalyticsTabQuery.Function>> future = new CompletableFuture<>();
        SimpleTimer timer = new SimpleTimer();
        apolloClient
            .query(new AnalyticsTabQuery(Input.optional(polygon), Input.optional(functionArgs)))
            .enqueue(new ApolloCall.Callback<>() {
                @Override
                public void onResponse(@NotNull Response<AnalyticsTabQuery.Data> response) {
                    metrics.labels(SUCCESSFUL).observe(timer.elapsedSeconds());

                    if (response.getData() != null && response.getData().polygonStatistic() != null &&
                        response.getData().polygonStatistic().analytics() != null &&
                        response.getData().polygonStatistic().analytics().functions() != null) {
                        future.complete(response.getData().polygonStatistic().analytics().functions());
                    }
                    future.complete(List.of());
                }

                @Override
                public void onFailure(@NotNull ApolloException e) {
                    metrics.labels(FAILED).observe(timer.elapsedSeconds());

                    future.completeExceptionally(new WebApplicationException("Exception when getting data from " +
                        "insights-api using apollo client", HttpStatus.BAD_GATEWAY));
                }
            });
        return future;
    }

    public CompletableFuture<List<BivariateLayerLegendQuery.Overlay>> getBivariateOverlays() {
        CompletableFuture<List<BivariateLayerLegendQuery.Overlay>> future = new CompletableFuture<>();
        SimpleTimer timer = new SimpleTimer();
        apolloClient
                .query(new BivariateLayerLegendQuery(Input.optional(null)))
                .enqueue(new ApolloCall.Callback<>() {
                    @Override
                    public void onResponse(@NotNull Response<BivariateLayerLegendQuery.Data> response) {
                        metrics.labels(SUCCESSFUL).observe(timer.elapsedSeconds());

                        if (response.getData() != null &&
                            response.getData().polygonStatistic() != null &&
                            response.getData().polygonStatistic().bivariateStatistic() != null) {
                            future.complete(response.getData().polygonStatistic().bivariateStatistic().overlays());
                        }
                        future.complete(List.of());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        metrics.labels(FAILED).observe(timer.elapsedSeconds());

                        future.completeExceptionally(new WebApplicationException("Exception when getting data from " +
                            "insights-api using apollo client", HttpStatus.BAD_GATEWAY));
                    }
                });
        return future;
    }
}
