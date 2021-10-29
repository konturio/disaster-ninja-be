package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.domain.AnalyticsTabProperties;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class AnalyticsTabPropertiesTest {

    @Autowired
    private AnalyticsTabProperties configuration;

    @Test
    public void test(){
        assertEquals(configuration.getFields().size(), 4);
    }

}