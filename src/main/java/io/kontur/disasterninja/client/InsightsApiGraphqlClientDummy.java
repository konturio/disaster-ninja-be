package io.kontur.disasterninja.client;

import io.kontur.disasterninja.dto.BivariateStatisticDto;
import io.kontur.disasterninja.graphql.AdvancedAnalyticalPanelQuery;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.Geometry;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnMissingBean(InsightsApiGraphqlClient.class)
public class InsightsApiGraphqlClientDummy implements InsightsApiGraphqlClient {

    public CompletableFuture<List<AnalyticsTabQuery.Function>> analyticsTabQuery(GeoJSON polygon,
                                                                                 List<FunctionArgs> functionArgs) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    public CompletableFuture<FeatureCollection> humanitarianImpactQuery(Geometry polygon) {
        return CompletableFuture.completedFuture(new FeatureCollection(new Feature[0]));
    }

    public CompletableFuture<List<AdvancedAnalyticalPanelQuery.AdvancedAnalytic>> advancedAnalyticsPanelQuery(
            GeoJSON polygon) {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }

    public CompletableFuture<BivariateStatisticDto> getBivariateStatistic() {
        return CompletableFuture.completedFuture(new BivariateStatisticDto());
    }
}
