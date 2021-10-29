package io.kontur.disasterninja.client;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import io.kontur.disasterninja.graphql.type.FunctionArgs;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import io.kontur.disasterninja.graphql.AnalyticsTabQuery;
import org.wololo.geojson.GeoJSON;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InsightsApiGraphqlClient {

    private final ApolloClient apolloClient;

    public void query(GeoJSON polygon, List<FunctionArgs> functionArgs){
        apolloClient
                .query(new AnalyticsTabQuery(Input.optional(polygon), Input.optional(functionArgs)))
                .enqueue(new ApolloCall.Callback<AnalyticsTabQuery.Data>(){

                    @Override
                    public void onResponse(@NotNull Response<AnalyticsTabQuery.Data> response) {
                        System.out.println("Result get(0) id = " + response.getData().polygonStatistic().analytics().functions().get(0).id());
                        System.out.println("Result get(0) id = " + response.getData().polygonStatistic().analytics().functions().get(0).result());
                    }

                    @Override
                    public void onFailure(@NotNull ApolloException e) {
                        System.out.println("ERROR");
                    }
                });
    }
}
