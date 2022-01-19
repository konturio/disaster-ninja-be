package io.kontur.disasterninja.config;

import com.apollographql.apollo.ApolloClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.config.metrics.ParamLessRestTemplateExchangeTagsProvider;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTagsProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfiguration {

    @Bean
    public RestTemplate eventApiRestTemplate(RestTemplateBuilder builder,
                                             @Value("${kontur.platform.event-api.url}") String eventApiUrl,
                                             @Value("${kontur.platform.event-api.connectionTimeout}") Integer connectionTimeout,
                                             @Value("${kontur.platform.event-api.readTimeout}") Integer readTimeout) {
        return builder
            .rootUri(eventApiUrl)
            .setConnectTimeout(Duration.of(connectionTimeout, ChronoUnit.SECONDS))
            .setReadTimeout(Duration.of(readTimeout, ChronoUnit.SECONDS))
                .build();
    }

    @Bean
    public RestTemplate kcApiRestTemplate(RestTemplateBuilder builder,
                                          @Value("${kontur.platform.kcApi.url}") String eventApiUrl,
                                          @Value("${kontur.platform.kcApi.connectionTimeout}") Integer connectionTimeout,
                                          @Value("${kontur.platform.kcApi.readTimeout}") Integer readTimeout) {
        return builder
            .rootUri(eventApiUrl)
            .setConnectTimeout(Duration.of(connectionTimeout, ChronoUnit.SECONDS))
            .setReadTimeout(Duration.of(readTimeout, ChronoUnit.SECONDS))
            .build();
    }

    @Bean
    public RestTemplate insightsApiRestTemplate(RestTemplateBuilder builder,
                                                @Value("${kontur.platform.insightsApi.url}") String eventApiUrl,
                                                @Value("${kontur.platform.insightsApi.connectionTimeout}") Integer connectionTimeout,
                                                @Value("${kontur.platform.insightsApi.readTimeout}") Integer readTimeout) {
        return builder
            .rootUri(eventApiUrl)
            .setConnectTimeout(Duration.of(connectionTimeout, ChronoUnit.SECONDS))
            .setReadTimeout(Duration.of(readTimeout, ChronoUnit.SECONDS))
            .build();
    }

    @Bean
    public RestTemplate authorizationRestTemplate(RestTemplateBuilder builder,
                                                  @Value("${kontur.platform.keycloak.url}") String keycloakUrl) {
        return builder
                .rootUri(keycloakUrl)
                .build();
    }


    @Bean
    public RestTemplate userProfileRestTemplate(RestTemplateBuilder builder,
                                                @Value("${kontur.platform.userProfileApi.url}") String url,
                                                @Value("${kontur.platform.userProfileApi.connectionTimeout}") Integer connectionTimeout,
                                                @Value("${kontur.platform.userProfileApi.readTimeout}") Integer readTimeout) {
        return builder
            .rootUri(url)
            .setConnectTimeout(Duration.of(connectionTimeout, ChronoUnit.SECONDS))
            .setReadTimeout(Duration.of(readTimeout, ChronoUnit.SECONDS))
            .build();
    }

    @Bean
    public GeoJsonDeserializerCustomizer geoJsonDeserializerCustomizer() {
        return new GeoJsonDeserializerCustomizer();
    }

    @Bean
    @Primary
    public RestTemplateExchangeTagsProvider restTemplateExchangeTagsProvider() {
        return new ParamLessRestTemplateExchangeTagsProvider();
    }

    private static class GeoJSONDeserializer extends JsonDeserializer<GeoJSON> {
        @Override
        public GeoJSON deserialize(JsonParser p, DeserializationContext ctxt) {
            try {
                ObjectNode node = p.getCodec().readTree(p);
                if (node == null) {
                    return null;
                }
                return GeoJSONFactory.create(node.toString());
            } catch (Exception e) {
                throw new WebApplicationException("Can't parse GeoJSON", HttpStatus.BAD_REQUEST);
            }
        }
    }

    private static class GeoJsonDeserializerCustomizer implements Jackson2ObjectMapperBuilderCustomizer {
        @Override
        public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
            jacksonObjectMapperBuilder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            jacksonObjectMapperBuilder.serializationInclusion(JsonInclude.Include.NON_NULL);
            jacksonObjectMapperBuilder.deserializerByType(GeoJSON.class, new GeoJSONDeserializer());
        }
    }
    @Bean
    public ApolloClient insightsApiApolloClient(@Value("${kontur.platform.insightsApi.url}") String insightsApiUrl,
                                                @Value("${graphql.apollo.maxIdleConnections}") Integer maxIdleConnections,
                                                @Value("${graphql.apollo.keepAliveDuration}") Integer keepAliveDuration,
                                                @Value("${graphql.apollo.connectionTimeout}") Integer connectionTimeout,
                                                @Value("${graphql.apollo.readTimeout}") Integer readTimeout){
        return ApolloClient.builder()
                .serverUrl(insightsApiUrl+"/graphql")
                .okHttpClient(new OkHttpClient().newBuilder()
                        .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                        .readTimeout(readTimeout, TimeUnit.SECONDS)
                        .connectionPool(new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS))
                        .build())
                .build();
    }

}
