package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.bivariatestatistic.BivariateStatisticDto;
import io.kontur.disasterninja.graphql.AdvancedAnalyticalPanelQuery;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.graphql.type.AdvancedAnalyticsRequest;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.Geometry;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface InsightsApiGraphqlClient {

    CompletableFuture<List<AnalyticsTabQuery.Function>> analyticsTabQuery(GeoJSON polygon,
                                                                          List<FunctionArgs> functionArgs);

    CompletableFuture<FeatureCollection> humanitarianImpactQuery(Geometry polygon);

    CompletableFuture<List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic>> advancedAnalyticsPanelQuery(
            GeoJSON argPolygon, List<AdvancedAnalyticsRequest> argRequest);

    CompletableFuture<BivariateStatisticDto> getBivariateStatistic();

    CompletableFuture<BivariateStatisticDto> getBivariateMatrix(GeoJSON geoJSON, List<List<String>> importantLayers);
}
