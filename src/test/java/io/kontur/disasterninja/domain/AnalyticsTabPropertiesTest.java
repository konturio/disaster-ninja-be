package io.kontur.disasterninja.domain;

import io.kontur.disasterninja.dto.AnalyticsField;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
class AnalyticsTabPropertiesTest {

    @Autowired
    private AnalyticsTabProperties configuration;

    @Test
    public void test() {
        assertEquals(4, configuration.getFields().size());
    }

    @Test
    public void populationTest(){
        List<AnalyticsField> fieldList = configuration.getFields().stream()
                .filter(field -> field.getName().equals("Population")).toList();
        assertEquals(1, fieldList.size());
        assertEquals(2, fieldList.get(0).getFunctions().size());
    }

    @Test
    public void osmGapsTest(){
        List<AnalyticsField> fieldList = configuration.getFields().stream()
                .filter(field -> field.getName().equals("OSM Gaps")).toList();
        assertEquals(1, fieldList.size());
        assertEquals(3, fieldList.get(0).getFunctions().size());
    }

    @Test
    public void noBuildingsTest(){
        List<AnalyticsField> fieldList = configuration.getFields().stream()
                .filter(field -> field.getName().equals("No buildings")).toList();
        assertEquals(1, fieldList.size());
        assertEquals(3, fieldList.get(0).getFunctions().size());
    }

    @Test
    public void noRoadsTest(){
        List<AnalyticsField> fieldList = configuration.getFields().stream()
                .filter(field -> field.getName().equals("No roads")).toList();
        assertEquals(1, fieldList.size());
        assertEquals(3, fieldList.get(0).getFunctions().size());
    }

}