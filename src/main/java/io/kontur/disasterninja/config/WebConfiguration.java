package io.kontur.disasterninja.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.apollographql.apollo.ApolloClient;
import io.kontur.disasterninja.config.metrics.ParamLessRestTemplateExchangeTagsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTagsProvider;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;

import java.io.IOException;

@Configuration
public class WebConfiguration {

    @Bean
    public RestTemplate eventApiRestTemplate(RestTemplateBuilder builder,
                                             @Value("${kontur.platform.event-api.url}") String eventApiUrl) {
        return builder
                .rootUri(eventApiUrl)
                .build();
    }

    @Bean
    public RestTemplate kcApiRestTemplate(RestTemplateBuilder builder,
                                          @Value("${kontur.platform.kcApi.url}") String eventApiUrl) {
        return builder
            .rootUri(eventApiUrl)
            .build();
    }

    @Bean
    public RestTemplate insightsApiRestTemplate(RestTemplateBuilder builder,
                                                @Value("${kontur.platform.insightsApi.url}") String eventApiUrl) {
        return builder
            .rootUri(eventApiUrl)
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
        public GeoJSON deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectNode node = p.getCodec().readTree(p);
            if (node == null) {
                return null;
            }
            return GeoJSONFactory.create(node.toString());
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
    public ApolloClient insightsApiApolloClient(@Value("${kontur.platform.insightsApi.url}") String insightsApiUrl){
        return ApolloClient.builder()
                .serverUrl(insightsApiUrl+"/graphql")
                .build();
    }

}
