package io.kontur.disasterninja.dto;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.Legend;
import io.kontur.disasterninja.domain.LegendStep;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.domain.enums.LegendType;
import io.kontur.disasterninja.dto.layer.LayerSummaryDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryInputDto;
import io.kontur.disasterninja.service.layers.LayerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.wololo.geojson.Geometry;
import org.wololo.geojson.Point;

import java.util.*;

import static io.kontur.disasterninja.domain.enums.LayerStepShape.HEX;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DtoTest {

    @MockBean
    LayerService layerService;
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void serializeDeserializeTest() {
        String url = "http://localhost:" + port + "/api/layers/";

        String id = "123";
        double[] coords = new double[]{1, 0};
        Geometry geoJSON = new Point(coords);
        ArrayList<Layer> layers = new ArrayList<>();
        Layer layer = testLayer(id, geoJSON);
        layers.add(layer);
        Mockito.when(layerService.getList(any())).thenReturn(layers);

        LayerSummaryInputDto input = new LayerSummaryInputDto("event id", geoJSON);
        List<LayerSummaryDto> response = Arrays.asList(restTemplate.postForEntity(url, input, LayerSummaryDto[].class).getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).getId());
        Assertions.assertEquals(layer.getName(), response.get(0).getName());
        Assertions.assertEquals(layer.getDescription(), response.get(0).getDescription());
        Assertions.assertEquals(layer.getCategory(), LayerCategory.fromString(response.get(0).getCategory()));
        Assertions.assertEquals(layer.getGroup(), response.get(0).getGroup());
        Assertions.assertEquals(layer.getCopyright(), response.get(0).getCopyright());
        Assertions.assertEquals(layer.getLegend(), response.get(0).getLegend().toLegend());
    }

    private Layer testLayer(String id, Geometry geoJSON) {
        LayerSource source = new LayerSource(LayerSourceType.RASTER, "url-com.com", 2d, geoJSON);
        Legend legend = new Legend("some legend", LegendType.SIMPLE, new ArrayList<>());
        Map<String, String> map = new HashMap<>();
        map.put("prop", "value");
        legend.getSteps().add(new LegendStep("param name", "param value", "step name",
            HEX, map));

        return new Layer(id, "test name", "test desciption", LayerCategory.BASE, "tset group",
            legend, "copyright text", 10, 1, source);
    }
}
