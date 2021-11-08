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
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
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
    public void serializeDeserializeFromGeometryTest() {
        String url = "/layers/";

        String id = "123";
        Layer layer = testLayer(id, new FeatureCollection(null));
        Mockito.when(layerService.getList(any(), any())).thenReturn(List.of(layer));

        LayerSummaryInputDto input = new LayerSummaryInputDto(UUID.randomUUID(), new Point(new double[]{1, 0}));
        List<LayerSummaryDto> response = Arrays.asList(restTemplate.postForEntity(url, input, LayerSummaryDto[].class)
            .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).getId());
        Assertions.assertEquals(layer.getName(), response.get(0).getName());
        Assertions.assertEquals(layer.getDescription(), response.get(0).getDescription());
        Assertions.assertEquals(layer.getCategory(), LayerCategory.fromString(response.get(0).getCategory()));
        Assertions.assertEquals(layer.getGroup(), response.get(0).getGroup());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).getCopyrights());
        Assertions.assertEquals(layer.getLegend(), response.get(0).getLegend().toLegend());
    }

    @Test
    public void serializeDeserializeFromFeatureTest() {
        String url = "/layers/";

        String id = "123";
        Layer layer = testLayer(id, new FeatureCollection(null));
        Mockito.when(layerService.getList(any(), any())).thenReturn(List.of(layer));

        LayerSummaryInputDto input = new LayerSummaryInputDto(UUID.randomUUID(), new Feature(new Point(
            new double[]{1, 0}), new HashMap<>()));
        List<LayerSummaryDto> response = Arrays.asList(restTemplate.postForEntity(url, input, LayerSummaryDto[].class)
            .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).getId());
        Assertions.assertEquals(layer.getName(), response.get(0).getName());
        Assertions.assertEquals(layer.getDescription(), response.get(0).getDescription());
        Assertions.assertEquals(layer.getCategory(), LayerCategory.fromString(response.get(0).getCategory()));
        Assertions.assertEquals(layer.getGroup(), response.get(0).getGroup());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).getCopyrights());
        Assertions.assertEquals(layer.getLegend(), response.get(0).getLegend().toLegend());
    }

    @Test
    public void serializeDeserializeFromFeatureCollectionTest() {
        String url = "/layers/";

        String id = "123";
        Layer layer = testLayer(id, new FeatureCollection(null));
        Mockito.when(layerService.getList(any(), any())).thenReturn(List.of(layer));

        LayerSummaryInputDto input = new LayerSummaryInputDto(UUID.randomUUID(), new FeatureCollection(new Feature[]{
            new Feature(new Point(new double[]{1, 0}), new HashMap<>()),
            new Feature(new Point(new double[]{1, 0}), new HashMap<>())}));

        List<LayerSummaryDto> response = Arrays.asList(restTemplate.postForEntity(url, input, LayerSummaryDto[].class)
            .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).getId());
        Assertions.assertEquals(layer.getName(), response.get(0).getName());
        Assertions.assertEquals(layer.getDescription(), response.get(0).getDescription());
        Assertions.assertEquals(layer.getCategory(), LayerCategory.fromString(response.get(0).getCategory()));
        Assertions.assertEquals(layer.getGroup(), response.get(0).getGroup());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).getCopyrights());
        Assertions.assertEquals(layer.getLegend(), response.get(0).getLegend().toLegend());
    }

    private Layer testLayer(String id, FeatureCollection geoJSON) {
        LayerSource source = LayerSource.builder()
            .type(LayerSourceType.RASTER)
            .url("url-com.com")
            .tileSize(2d)
            .data(geoJSON).build();
        Legend legend = new Legend("some legend", LegendType.SIMPLE, new ArrayList<>(), new HashMap<>());
        Map<String, String> map = new HashMap<>();
        map.put("prop", "value");
        legend.getSteps().add(new LegendStep("param name", "param value", null, null, "step name",
            HEX, map));

        return Layer.builder()
            .id(id)
            .name("test name")
            .description("test description")
            .category(LayerCategory.BASE)
            .group("test group")
            .legend(legend)
            .copyrights(List.of("copyright text"))
            .maxZoom(10)
            .minZoom(1)
            .source(source)
            .build();
    }
}
