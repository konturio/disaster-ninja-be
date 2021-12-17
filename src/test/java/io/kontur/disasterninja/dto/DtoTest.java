package io.kontur.disasterninja.dto;

import io.kontur.disasterninja.domain.*;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.domain.enums.LegendType;
import io.kontur.disasterninja.dto.layer.LayerDetailsDto;
import io.kontur.disasterninja.dto.layer.LayerDetailsInputDto;
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
    public void serializeDeserializeSummaryFromGeometryTest() {
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
        Assertions.assertEquals(layer.isBoundaryRequiredForRetrieval(), response.get(0).isBoundaryRequiredForRetrieval());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).getCopyrights());
        Assertions.assertEquals(layer.getLegend(), response.get(0).getLegend().toLegend());
    }

    @Test
    public void serializeDeserializeDetailsFromGeometryTest() {
        String url = "/layers/details";

        String id = "123";
        Layer layer = testLayer(id, new FeatureCollection(new Feature[]{new Feature(new Point(new double[]{1d, 2d}), new HashMap<>())}));
        Mockito.when(layerService.get(any(), any(), any())).thenReturn(List.of(layer));

        LayerDetailsInputDto input = new LayerDetailsInputDto(new Point(new double[]{1, 0}), List.of(layer.getId()), UUID.randomUUID());
        List<LayerDetailsDto> response = Arrays.asList(restTemplate.postForEntity(url, input, LayerDetailsDto[].class)
            .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).getId());
        Assertions.assertEquals(layer.getMinZoom(), response.get(0).getMinZoom());
        Assertions.assertEquals(layer.getMaxZoom(), response.get(0).getMaxZoom());

        Assertions.assertArrayEquals(((Point) layer.getSource().getData().getFeatures()[0].getGeometry()).getCoordinates(),
            ((Point) ((FeatureCollection) response.get(0).getSource().getData()).getFeatures()[0].getGeometry()).getCoordinates());

        Assertions.assertEquals(layer.getSource().getType(), response.get(0).getSource().getType());
        Assertions.assertEquals(layer.getSource().getTileSize(), response.get(0).getSource().getTileSize());
        Assertions.assertEquals(layer.getSource().getUrls(), response.get(0).getSource().getUrls());
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
        Assertions.assertEquals(layer.isBoundaryRequiredForRetrieval(), response.get(0).isBoundaryRequiredForRetrieval());
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
        Assertions.assertEquals(layer.isBoundaryRequiredForRetrieval(), response.get(0).isBoundaryRequiredForRetrieval());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).getCopyrights());
        Assertions.assertEquals(layer.getLegend(), response.get(0).getLegend().toLegend());
    }

    @Test
    public void paramPatternTest() { //#8311
        String url = "/layers";
        String id = "123";

        final String PARAM_NAME = "param name";
        final String PARAM_PATTERN = "qwe(.+)";
        final String PARAM_VALUE = "qwe";
        final String OTHER_PARAM_VALUE = "asd";

        Feature feature1 = new Feature(new Point(new double[]{1d, 2d}),
            new HashMap<>());
        feature1.getProperties().put(PARAM_NAME, "qwe11"); //feature 1 matching step1 pattern
        Feature feature2 = new Feature(new Point(new double[]{2d, 3d}),
            new HashMap<>());
        feature2.getProperties().put(PARAM_NAME, "qwePPP"); //feature 2 matching step1 pattern
        Feature feature3 = new Feature(new Point(new double[]{2d, 3d}),
            new HashMap<>());
        feature3.getProperties().put(PARAM_NAME, "asd"); //feature matching step2 value

        FeatureCollection geoJSON = new FeatureCollection(new Feature[]{feature1, feature2, feature3});

        LayerSource source = LayerSource.builder()
            .type(LayerSourceType.RASTER)
            .urls(List.of("url-com.com"))
            .tileSize(2)
            .data(geoJSON).build();
        Legend legend = new Legend(LegendType.SIMPLE, null, new ArrayList<>(), new HashMap<>(),
            new BivariateLegendAxises());
        Map<String, Object> map = new HashMap<>();
        map.put("prop", "value");

        //pattern step (1)
        legend.getSteps()
            .add(new LegendStep(PARAM_NAME, PARAM_PATTERN, PARAM_VALUE,
            null, null, "step name",
            HEX, map, "source-layer"));
        //non-pattern step (2)
        legend.getSteps()
            .add(new LegendStep(PARAM_NAME, null, OTHER_PARAM_VALUE,
            null, null, "step name2",
            HEX, map, "source-layer"));

        Layer layerWithPattern = Layer.builder()
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

        Mockito.when(layerService.getList(any(), any())).thenReturn(List.of(layerWithPattern));
        LayerSummaryInputDto input = new LayerSummaryInputDto(UUID.randomUUID(), new Point(new double[]{1, 0}));
        List<LayerSummaryDto> response = Arrays.asList(restTemplate.postForEntity(url, input, LayerSummaryDto[].class)
            .getBody());

        Assertions.assertEquals(2, response.get(0).getLegend().getSteps().size());
        //both features (f1, f2) match the same pattern hence are merged into a single step
        Assertions.assertEquals(PARAM_VALUE, response.get(0).getLegend().getSteps().get(0).getParamValue());
        //f3 matches the second step
    }

    private Layer testLayer(String id, FeatureCollection geoJSON) {
        LayerSource source = LayerSource.builder()
            .type(LayerSourceType.RASTER)
            .urls(List.of("url-com.com"))
            .tileSize(2)
            .data(geoJSON).build();
        Legend legend = new Legend(LegendType.SIMPLE, null, new ArrayList<>(), new HashMap<>(), new BivariateLegendAxises());
        Map<String, Object> map = new HashMap<>();
        map.put("prop", "value");
        legend.getSteps().add(new LegendStep("param name", null, "param value", null, null, "step name",
            HEX, map, "source-layer"));

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
