package io.kontur.disasterninja.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfiguration {

    public WebConfiguration(@Autowired ObjectMapper objectMapper) {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Bean
    @Qualifier("eventApiRestTemplate")
    public RestTemplate eventApiRestTemplate(RestTemplateBuilder builder,
                                             @Value("${kontur.platform.event-api.url}") String eventApiUrl) {
        return builder
                .rootUri(eventApiUrl)
                .build();
    }

    @Bean
    @Qualifier("kcApiRestTemplate")
    public RestTemplate kcApiRestTemplate(RestTemplateBuilder builder,
                                          @Value("${kontur.platform.kcApi.url}") String eventApiUrl) {
        return builder
            .rootUri(eventApiUrl)
            .build();
    }

    @Bean
    @Qualifier("insightsApiRestTemplate")
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

}
