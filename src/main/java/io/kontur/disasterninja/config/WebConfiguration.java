package io.kontur.disasterninja.config;
import okio.Buffer;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import okhttp3.Interceptor;


import com.apollographql.apollo.ApolloClient;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.kontur.disasterninja.client.InsightsApiClient;
import io.kontur.disasterninja.client.InsightsApiClientDummy;
import io.kontur.disasterninja.client.InsightsApiGraphqlClient;
import io.kontur.disasterninja.client.InsightsApiGraphqlClientDummy;
import io.kontur.disasterninja.config.interceptor.HeaderInterceptor;
import io.kontur.disasterninja.config.metrics.ParamLessRestTemplateExchangeTagsProvider;
import io.kontur.disasterninja.controller.exception.WebApplicationException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.httpcomponents.PoolingHttpClientConnectionManagerMetricsBinder;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTagsProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.List;

@Configuration
public class WebConfiguration {

    @Bean
    public HttpClient httpClient(MeterRegistry meterRegistry) {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100);
        connectionManager.setDefaultMaxPerRoute(20);

        new PoolingHttpClientConnectionManagerMetricsBinder(connectionManager, "connection_pool")
                .bindTo(meterRegistry);

        return HttpClientBuilder.create()
                .setConnectionManager(connectionManager)
                .build();
    }

    @Bean
    public RestTemplate eventApiRestTemplate(RestTemplateBuilder builder, HttpClient httpClient,
                                             @Value("${kontur.platform.event-api.url}") String eventApiUrl,
                                             @Value("${kontur.platform.event-api.connectionTimeout}") Integer connectionTimeout,
                                             @Value("${kontur.platform.event-api.readTimeout}") Integer readTimeout) {
        return builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .rootUri(eventApiUrl)
                .setConnectTimeout(Duration.of(connectionTimeout, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(readTimeout, ChronoUnit.SECONDS))
                .build();
    }

    @Bean
    public RestTemplate kcApiRestTemplate(RestTemplateBuilder builder, HttpClient httpClient,
                                          @Value("${kontur.platform.kcApi.url}") String kcApiUrl,
                                          @Value("${kontur.platform.kcApi.connectionTimeout}") Integer connectionTimeout,
                                          @Value("${kontur.platform.kcApi.readTimeout}") Integer readTimeout) {
        return builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .rootUri(kcApiUrl)
                .setConnectTimeout(Duration.of(connectionTimeout, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(readTimeout, ChronoUnit.SECONDS))
                .build();
    }

    @Bean
    public RestTemplate layersApiRestTemplate(RestTemplateBuilder builder, HttpClient httpClient,
                                              @Value("${kontur.platform.layersApi.url}") String layersApiUrl,
                                              @Value("${kontur.platform.layersApi.connectionTimeout}") Integer connectionTimeout,
                                              @Value("${kontur.platform.layersApi.readTimeout}") Integer readTimeout) {
        return builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .rootUri(layersApiUrl)
                .setConnectTimeout(Duration.of(connectionTimeout, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(readTimeout, ChronoUnit.SECONDS))
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "kontur.platform.insightsApi.url")
    public RestTemplate insightsApiRestTemplate(RestTemplateBuilder builder, HttpClient httpClient,
                                                @Value("${kontur.platform.insightsApi.url}") String insightsApiUrl,
                                                @Value("${kontur.platform.insightsApi.connectionTimeout}") Integer connectionTimeout,
                                                @Value("${kontur.platform.insightsApi.readTimeout}") Integer readTimeout,
                                                ClientHttpRequestInterceptor insightsHeadersInterceptor) {
        return builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .rootUri(insightsApiUrl)
                .setConnectTimeout(Duration.of(connectionTimeout, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(readTimeout, ChronoUnit.SECONDS))
                .additionalInterceptors(insightsHeadersInterceptor)
                .build();
    }

    @Bean
    public RestTemplate authorizationRestTemplate(RestTemplateBuilder builder, HttpClient httpClient,
                                                  @Value("${kontur.platform.keycloak.url}") String keycloakUrl) {
        return builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .rootUri(keycloakUrl)
                .build();
    }

    @Bean
    public RestTemplate userProfileRestTemplate(RestTemplateBuilder builder, HttpClient httpClient,
                                                @Value("${kontur.platform.userProfileApi.url}") String userProfileApiUrl,
                                                @Value("${kontur.platform.userProfileApi.connectionTimeout}") Integer connectionTimeout,
                                                @Value("${kontur.platform.userProfileApi.readTimeout}") Integer readTimeout,
                                                ClientHttpRequestInterceptor userLanguageInterceptor) {
        return builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .rootUri(userProfileApiUrl)
                .setConnectTimeout(Duration.of(connectionTimeout, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(readTimeout, ChronoUnit.SECONDS))
                .additionalInterceptors(userLanguageInterceptor)
                .build();
    }

    @Bean
    public RestTemplate llmAnalyticsRestTemplate(RestTemplateBuilder builder, HttpClient httpClient,
                                                @Value("${kontur.platform.llmAnalyticsApi.url}") String llmAnalyticsApiUrl,
                                                @Value("${kontur.platform.llmAnalyticsApi.connectionTimeout}") Integer connectionTimeout,
                                                @Value("${kontur.platform.llmAnalyticsApi.readTimeout}") Integer readTimeout,
                                                ClientHttpRequestInterceptor userLanguageInterceptor) {
        return builder
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .rootUri(llmAnalyticsApiUrl)
                .setConnectTimeout(Duration.of(connectionTimeout, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(readTimeout, ChronoUnit.SECONDS))
                .additionalInterceptors(userLanguageInterceptor)
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
    @ConditionalOnProperty(name = "kontur.platform.insightsApi.url")
    public ApolloClient insightsApiApolloClient(@Value("${kontur.platform.insightsApi.url}") String insightsApiUrl,
                                                @Value("${graphql.apollo.maxIdleConnections}") Integer maxIdleConnections,
                                                @Value("${graphql.apollo.keepAliveDuration}") Integer keepAliveDuration,
                                                @Value("${graphql.apollo.connectionTimeout}") Integer connectionTimeout,
                                                @Value("${graphql.apollo.readTimeout}") Integer readTimeout) {
        return ApolloClient.builder()
                .serverUrl(insightsApiUrl + "/graphql")
                .okHttpClient(new OkHttpClient().newBuilder()
                        .connectTimeout(connectionTimeout, TimeUnit.SECONDS)
                        .readTimeout(readTimeout, TimeUnit.SECONDS)
                        .connectionPool(new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS))
                        .build())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(InsightsApiGraphqlClient.class)
    public InsightsApiGraphqlClient insightsApiGraphqlClientDummy() {
        return new InsightsApiGraphqlClientDummy();
    }

    @Bean
    @ConditionalOnMissingBean(InsightsApiClient.class)
    public InsightsApiClient insightsApiClientDummy() {
        return new InsightsApiClientDummy();
    }

    @Bean
    public ClientHttpRequestInterceptor insightsHeadersInterceptor() {
        return new HeaderInterceptor(List.of("If-None-Match", "If-Modified-Since"));
    }

    @Bean
    public ClientHttpRequestInterceptor userLanguageInterceptor() {
        return new HeaderInterceptor(List.of("User-Language"));
    }

}
