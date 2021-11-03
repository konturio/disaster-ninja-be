package io.kontur.disasterninja.client;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.wololo.geojson.GeoJSON;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class InsightsApiGraphqlClient {

    private final ApolloClient apolloClient;

    public CompletableFuture<List<AnalyticsTabQuery.Function>> analyticsTabQuery(GeoJSON polygon, List<FunctionArgs> functionArgs) {
        CompletableFuture<List<AnalyticsTabQuery.Function>> future = new CompletableFuture<>();
        apolloClient
                .query(new AnalyticsTabQuery(Input.optional(polygon), Input.optional(functionArgs)))
                .enqueue(new ApolloCall.Callback<AnalyticsTabQuery.Data>() {
                    @Override
                    public void onResponse(@NotNull Response<AnalyticsTabQuery.Data> response) {
                        future.complete(response.getData().polygonStatistic().analytics().functions());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        future.completeExceptionally(new WebApplicationException("Exception when getting data from insights-api using apollo client", HttpStatus.BAD_GATEWAY));
                    }
                });
        return future;
    }
}
