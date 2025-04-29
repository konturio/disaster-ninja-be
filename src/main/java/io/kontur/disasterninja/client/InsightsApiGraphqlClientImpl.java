package io.kontur.disasterninja.client;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.apollographql.apollo.exception.ApolloHttpException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.domain.BivariateLegendAxisDescription;
import io.kontur.disasterninja.domain.Transformation;
import io.kontur.disasterninja.dto.bivariatematrix.*;
import io.kontur.disasterninja.graphql.*;
import io.kontur.disasterninja.graphql.type.AdvancedAnalyticsRequest;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import io.kontur.disasterninja.mapper.AxisListMapper;
import io.kontur.disasterninja.mapper.BivariateStatisticMapper;
import io.kontur.disasterninja.mapper.TransformationListMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.SimpleTimer;
import io.prometheus.client.Summary;
import org.jetbrains.annotations.NotNull;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnProperty(name = "kontur.platform.insightsApi.url")
public class InsightsApiGraphqlClientImpl implements InsightsApiGraphqlClient {

    private final ApolloClient apolloClient;
    private final BivariateStatisticMapper mapper = Mappers.getMapper(BivariateStatisticMapper.class);
    private final AxisListMapper axisMapper = Mappers.getMapper(AxisListMapper.class);
    private final TransformationListMapper transformationMapper = Mappers.getMapper(TransformationListMapper.class);
    private final static String SUCCESS = "SUCCESS";
    private final static String FAILED = "FAILED";
    private final Summary metrics; //todo use histogram once we know which buckets to use

    public InsightsApiGraphqlClientImpl(ApolloClient apolloClient, MeterRegistry meterRegistry) {
        this.apolloClient = apolloClient;
        Summary.Builder metricsBuilder = Summary.build()
                .quantile(0.5, 0.01)
                .quantile(1, 0.01) //max
                .labelNames("outcome", "status")
                .name("http_client_insights_api_graphql_requests_seconds")
                .help("Requests with insights-api GraphQl.")
                .maxAgeSeconds(120) //same as in micrometer (for spring restTemplates)
                .ageBuckets(1); //same as in micrometer (for spring restTemplates)

        if (meterRegistry instanceof PrometheusMeterRegistry) {
            CollectorRegistry collectorRegistry = ((PrometheusMeterRegistry) meterRegistry).getPrometheusRegistry();
            metrics = metricsBuilder.register(collectorRegistry);
        } else {
            metrics = metricsBuilder.create();
        }
    }

    public CompletableFuture<List<AnalyticsTabQuery.Function>> analyticsTabQuery(GeoJSON polygon,
                                                                                 List<FunctionArgs> functionArgs) {
        CompletableFuture<List<AnalyticsTabQuery.Function>> future = new CompletableFuture<>();
        SimpleTimer timer = new SimpleTimer();
        apolloClient
                .query(new AnalyticsTabQuery(Input.optional(polygon), Input.optional(functionArgs)))
                .enqueue(new ApolloCall.Callback<>() {
                    @Override
                    public void onResponse(@NotNull Response<AnalyticsTabQuery.Data> response) {
                        metrics.labels(SUCCESS, "200").observe(timer.elapsedSeconds());

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

    public CompletableFuture<FeatureCollection> humanitarianImpactQuery(Geometry polygon) {
        CompletableFuture<FeatureCollection> future = new CompletableFuture<>();
        SimpleTimer timer = new SimpleTimer();
        apolloClient
                .query(new HumanitarianImpactQuery(Input.optional(polygon)))
                .enqueue(new ApolloCall.Callback<>() {
                    @Override
                    public void onResponse(@NotNull Response<HumanitarianImpactQuery.Data> response) {
                        metrics.labels(SUCCESS, "200").observe(timer.elapsedSeconds());

                        if (response.getData() != null && response.getData().polygonStatistic() != null &&
                                response.getData().polygonStatistic().analytics() != null &&
                                response.getData().polygonStatistic().analytics().humanitarianImpact() != null) {
                            String json = response.getData().polygonStatistic().analytics().humanitarianImpact();
                            try {
                                future.complete(new ObjectMapper().readValue(json, FeatureCollection.class));
                            } catch (JsonProcessingException e) {
                                throw new WebApplicationException("Unable to convert response",
                                        HttpStatus.INTERNAL_SERVER_ERROR);
                            }
                        }
                        future.complete(new FeatureCollection(new Feature[0]));
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        observeMetricsAndCompleteExceptionally(e, future, timer);
                    }
                });
        return future;
    }

    public CompletableFuture<List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic>> advancedAnalyticsPanelQuery(
            GeoJSON argPolygon, List<AdvancedAnalyticsRequest> argRequest) {
        CompletableFuture<List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic>> future = new CompletableFuture<>();
        SimpleTimer timer = new SimpleTimer();
        apolloClient
                .query(new AdvancedAnalyticalPanelQuery(Input.optional(argPolygon), Input.optional(argRequest)))
                .enqueue(new ApolloCall.Callback<>() {
                    @Override
                    public void onResponse(@NotNull Response<AdvancedAnalyticalPanelQuery.Data> response) {
                        metrics.labels(SUCCESS, "200").observe(timer.elapsedSeconds());

                        if (response.getData() != null && response.getData().polygonStatistic() != null &&
                                response.getData().polygonStatistic().analytics() != null &&
                                response.getData().polygonStatistic().analytics().advancedAnalytics() != null) {
                            future.complete(response.getData().polygonStatistic().analytics().advancedAnalytics());
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

    public CompletableFuture<List<Transformation>> getTransformationList(String numerator, String denominator) {
        CompletableFuture<List<Transformation>> future = new CompletableFuture<>();
        SimpleTimer timer = new SimpleTimer();
        apolloClient
                .query(new TransformationListQuery(
                            Input.optional(numerator == null ? null : numerator),
                            Input.optional(denominator == null ? null : denominator)))
                .enqueue(new ApolloCall.Callback<>() {
                    @Override
                    public void onResponse(@NotNull Response<TransformationListQuery.Data> response) {
                        metrics.labels(SUCCESS, "200").observe(timer.elapsedSeconds());

                        if (response.getData() != null && response.getData().getTransformations() != null
                                && response.getData().getTransformations().transformation() != null) {
                            future.complete(
                                    transformationMapper.transformationListQueryTransformationListToTransformationList(
                                            response.getData().getTransformations().transformation()));
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

    public CompletableFuture<List<BivariateLegendAxisDescription>> getAxisList() {
        CompletableFuture<List<BivariateLegendAxisDescription>> future = new CompletableFuture<>();
        SimpleTimer timer = new SimpleTimer();
        apolloClient
                .query(new AxisListQuery())
                .enqueue(new ApolloCall.Callback<>() {
                    @Override
                    public void onResponse(@NotNull Response<AxisListQuery.Data> response) {
                        metrics.labels(SUCCESS, "200").observe(timer.elapsedSeconds());

                        if (response.getData() != null && response.getData().getAxes() != null
                                && response.getData().getAxes().axis() != null) {
                            future.complete(
                                    axisMapper.axisListQueryAxisListToBivariateLegendAxisDescriptionList(
                                            response.getData().getAxes().axis()));
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
                        metrics.labels(SUCCESS, "200").observe(timer.elapsedSeconds());

                        if (response.getData() != null &&
                                response.getData().polygonStatistic() != null &&
                                response.getData().polygonStatistic().bivariateStatistic() != null &&
                                response.getData().polygonStatistic().bivariateStatistic().indicators() != null) {
                            BivariateStatisticDto bivariateStatisticDto = BivariateStatisticDto.builder()
                                    .indicators(mapper.bivariateLayerLegendQueryIndicatorListToIndicatorDtoList(
                                            response.getData().polygonStatistic().bivariateStatistic().indicators()))
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

    @Override
    public CompletableFuture<BivariateMatrixDto> getBivariateMatrix(GeoJSON geoJSON, List<List<String>> importantLayers) {
        CompletableFuture<BivariateMatrixDto> future = new CompletableFuture<>();
        SimpleTimer timer = new SimpleTimer();
        apolloClient
                .query(new BivariateMatrixQuery(Input.optional(geoJSON), Input.optional(importantLayers)))
                .enqueue(new ApolloCall.Callback<>() {
                    @Override
                    public void onResponse(@NotNull Response<BivariateMatrixQuery.Data> response) {
                        metrics.labels(SUCCESS, "200").observe(timer.elapsedSeconds());

                        if (response.getData() != null &&
                                response.getData().polygonStatistic() != null &&
                                response.getData().polygonStatistic().bivariateStatistic() != null) {
                            BivariateMatrixDto bivariateMatrixDto = new BivariateMatrixDto(
                                    new BivariateMatrixGraphqlResponseDataDto(
                                            new PolygonStatisticDto(BivariateStatisticDto.builder()
                                                    .axis(mapper.bivariateMatrixQueryAxisListToBivariateLegendAxisDescriptionList(
                                                            response.getData().polygonStatistic().bivariateStatistic().axis()))
                                                    .meta(mapper.bivariateMatrixQueryMetaToMetaDto(
                                                            response.getData().polygonStatistic().bivariateStatistic().meta()))
                                                    .indicators(mapper.bivariateMatrixQueryIndicatorListToIndicatorDtoList(
                                                            response.getData().polygonStatistic().bivariateStatistic().indicators()))
                                                    .correlationRates(mapper.bivariateMatrixQueryCorrelationRateListToCorrelationRateDtoList(
                                                            response.getData().polygonStatistic().bivariateStatistic().correlationRates()))
                                                    .colors(mapper.bivariateMatrixQueryColorsToColorsDto(
                                                            response.getData().polygonStatistic().bivariateStatistic().colors()))
                                                    .build())));
                            future.complete(bivariateMatrixDto);
                        }
                        future.complete(new BivariateMatrixDto());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        observeMetricsAndCompleteExceptionally(e, future, timer);
                    }
                });
        return future;
    }

    private <T> void observeMetricsAndCompleteExceptionally(@NotNull ApolloException e,
                                                            @NotNull CompletableFuture<T> future,
                                                            @NotNull SimpleTimer timer) {
        int status = 500;
        if (e instanceof ApolloHttpException) {
            status = ((ApolloHttpException) e).code();
        }
        metrics.labels(FAILED, String.valueOf(status)).observe(timer.elapsedSeconds());
        future.completeExceptionally(e);
    }
}
