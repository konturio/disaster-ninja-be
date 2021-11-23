package io.kontur.disasterninja;

import io.prometheus.client.CollectorRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class DisasterNinjaBeApplicationTests {
    @MockBean
    CollectorRegistry collectorRegistry;

    @Test
    void contextLoads() {
    }

}
