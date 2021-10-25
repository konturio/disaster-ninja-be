package io.kontur.disasterninja.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.kontur.disasterninja.config.metrics.ParamLessRestTemplateExchangeTagsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTagsProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfiguration {

    public WebConfiguration(@Autowired ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

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
    @Primary
    public RestTemplateExchangeTagsProvider restTemplateExchangeTagsProvider() {
        return new ParamLessRestTemplateExchangeTagsProvider();
    }

}
