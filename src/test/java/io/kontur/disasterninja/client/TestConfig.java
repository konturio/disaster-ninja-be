package io.kontur.disasterninja.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@TestConfiguration
public class TestConfig {

    @Bean
    @Qualifier("eventApiRestTemplate")
    RestTemplate restTemplate1() {
        return new RestTemplate();
    }

    @Bean
    @Qualifier("kcApiRestTemplate")
    RestTemplate restTemplate2() {
        return new RestTemplate();
    }

    @Bean
    @Qualifier("insightsApiRestTemplate")
    RestTemplate restTemplate3() {
        return new RestTemplate();
    }
}
