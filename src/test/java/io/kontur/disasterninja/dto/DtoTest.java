package io.kontur.disasterninja.dto;

import io.kontur.disasterninja.controller.LayerController;
import io.kontur.disasterninja.domain.*;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.domain.enums.LegendType;
import io.kontur.disasterninja.dto.layer.LayerDetailsDto;
import io.kontur.disasterninja.dto.layer.LayerDetailsSearchDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryDto;
import io.kontur.disasterninja.dto.layer.LayerSummarySearchDto;
import io.kontur.disasterninja.service.KeycloakAuthorizationService;
import io.kontur.disasterninja.service.layers.LayerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

import static io.kontur.disasterninja.controller.LayerController.PATH_DETAILS;
import static io.kontur.disasterninja.controller.LayerController.PATH_SEARCH;
import static io.kontur.disasterninja.domain.enums.LayerStepShape.HEX;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DtoTest {

    @MockBean
    LayerService layerService;
    @MockBean
    KeycloakAuthorizationService authorizationService;
    @LocalServerPort
    private int port;
    @Autowired
    private TestRestTemplate restTemplate;
    private final String SEARCH_URL = LayerController.PATH + PATH_SEARCH;
    private final String DETAILS_URL = LayerController.PATH + PATH_DETAILS;

    @BeforeEach
    public void before() {
        Mockito.when(authorizationService.getAccessToken()).thenReturn("something");
    }

    @Test
    public void serializeLayerWithEmptyCollections() {
        Layer layer = Layer.builder()
                .legend(Legend.builder().build())
                .build();
        layer.getLegend().setSteps(null);
        layer.getLegend().setColors(null);

        LayerSummaryDto summaryDto = LayerSummaryDto.fromLayer(layer);
        assertNull(summaryDto.getLegend().getSteps());
        assertNull(summaryDto.getLegend().getColors());
    }

    @Test
    public void serializeDeserializeSummaryFromGeometryTest() {
        String id = "123";
        Layer layer = testLayer(id, new FeatureCollection(null));
        Mockito.when(layerService.getList(any())).thenReturn(List.of(layer));

        LayerSummarySearchDto
                input = new LayerSummarySearchDto(UUID.randomUUID(), UUID.randomUUID(), "some-feed",
                new Point(new double[]{1, 0}));
        List<LayerSummaryDto> response = Arrays.asList(
                restTemplate.postForEntity(SEARCH_URL, input, LayerSummaryDto[].class)
                        .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).getId());
        Assertions.assertEquals(layer.isEventIdRequiredForRetrieval(), response.get(0).isEventIdRequiredForRetrieval());
        Assertions.assertEquals(layer.getName(), response.get(0).getName());
        Assertions.assertEquals(layer.getDescription(), response.get(0).getDescription());
        Assertions.assertEquals(layer.getCategory(), response.get(0).getCategory());
        Assertions.assertEquals(layer.getGroup(), response.get(0).getGroup());
        Assertions.assertEquals(layer.isBoundaryRequiredForRetrieval(),
                response.get(0).isBoundaryRequiredForRetrieval());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).getCopyrights());
        Assertions.assertEquals(layer.getLegend(), response.get(0).getLegend());
    }

    @Test
    public void serializeDeserializeDetailsFromGeometryTest() {
        String id = "123";
        Layer layer = testLayer(id,
                new FeatureCollection(new Feature[]{new Feature(new Point(new double[]{1d, 2d}), new HashMap<>())}));
        Mockito.when(layerService.get(any(), any(), any())).thenReturn(List.of(layer));

        LayerDetailsSearchDto input = new LayerDetailsSearchDto(new Point(new double[]{1, 0}), List.of(layer.getId()),
                List.of(), UUID.randomUUID(), UUID.randomUUID(), "some-feed");
        List<LayerDetailsDto> response = Arrays.asList(
                restTemplate.postForEntity(DETAILS_URL, input, LayerDetailsDto[].class)
                        .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).getId());
        Assertions.assertEquals(layer.getMinZoom(), response.get(0).getMinZoom());
        Assertions.assertEquals(layer.getMaxZoom(), response.get(0).getMaxZoom());

        Assertions.assertArrayEquals(
                ((Point) layer.getSource().getData().getFeatures()[0].getGeometry()).getCoordinates(),
                ((Point) ((FeatureCollection) response.get(0).getSource()
                        .getData()).getFeatures()[0].getGeometry()).getCoordinates());

        Assertions.assertEquals(layer.getSource().getType(), response.get(0).getSource().getType());
        Assertions.assertEquals(layer.getSource().getTileSize(), response.get(0).getSource().getTileSize());
        Assertions.assertEquals(layer.getSource().getUrls(), response.get(0).getSource().getUrls());
    }

    @Test
    public void serializeDeserializeFromFeatureTest() {
        String id = "123";
        Layer layer = testLayer(id, new FeatureCollection(null));
        Mockito.when(layerService.getList(any())).thenReturn(List.of(layer));

        LayerSummarySearchDto input = new LayerSummarySearchDto(UUID.randomUUID(), UUID.randomUUID(), "some-feed",
                new Feature(new Point(
                        new double[]{1, 0}), new HashMap<>()));
        List<LayerSummaryDto> response = Arrays.asList(
                restTemplate.postForEntity(SEARCH_URL, input, LayerSummaryDto[].class)
                        .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).getId());
        Assertions.assertEquals(layer.isEventIdRequiredForRetrieval(), response.get(0).isEventIdRequiredForRetrieval());
        Assertions.assertEquals(layer.getName(), response.get(0).getName());
        Assertions.assertEquals(layer.getDescription(), response.get(0).getDescription());
        Assertions.assertEquals(layer.getCategory(), response.get(0).getCategory());
        Assertions.assertEquals(layer.getGroup(), response.get(0).getGroup());
        Assertions.assertEquals(layer.isBoundaryRequiredForRetrieval(),
                response.get(0).isBoundaryRequiredForRetrieval());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).getCopyrights());
        Assertions.assertEquals(layer.getLegend(), response.get(0).getLegend());
    }

    @Test
    public void serializeDeserializeFromFeatureCollectionTest() {
        String id = "123";
        Layer layer = testLayer(id, new FeatureCollection(null));
        Mockito.when(layerService.getList(any())).thenReturn(List.of(layer));

        LayerSummarySearchDto input = new LayerSummarySearchDto(UUID.randomUUID(), UUID.randomUUID(), "some-feed",
                new FeatureCollection(new Feature[]{
                        new Feature(new Point(new double[]{1, 0}), new HashMap<>()),
                        new Feature(new Point(new double[]{1, 0}), new HashMap<>())}));

        List<LayerSummaryDto> response = Arrays.asList(
                restTemplate.postForEntity(SEARCH_URL, input, LayerSummaryDto[].class)
                        .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).getId());
        Assertions.assertEquals(layer.isEventIdRequiredForRetrieval(), response.get(0).isEventIdRequiredForRetrieval());
        Assertions.assertEquals(layer.getName(), response.get(0).getName());
        Assertions.assertEquals(layer.getDescription(), response.get(0).getDescription());
        Assertions.assertEquals(layer.getCategory(), response.get(0).getCategory());
        Assertions.assertEquals(layer.getGroup(), response.get(0).getGroup());
        Assertions.assertEquals(layer.isBoundaryRequiredForRetrieval(),
                response.get(0).isBoundaryRequiredForRetrieval());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).getCopyrights());
        Assertions.assertEquals(layer.getLegend(), response.get(0).getLegend());
    }

    @Test
    public void paramPatternTest() { //#8311
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
        Legend legend = new Legend("legendName", LegendType.SIMPLE, null, new ArrayList<>(),
                new ArrayList<>(), new BivariateLegendAxes(), null);
        Map<String, Object> map = new HashMap<>();
        map.put("prop", "value");

        //pattern step (1)
        legend.getSteps()
                .add(new LegendStep(PARAM_NAME, PARAM_PATTERN, PARAM_VALUE,
                        null, null, "step name",
                        HEX, map, "source-layer", "", ""));
        //non-pattern step (2)
        legend.getSteps()
                .add(new LegendStep(PARAM_NAME, null, OTHER_PARAM_VALUE,
                        null, null, "step name2",
                        HEX, map, "source-layer", "", ""));

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

        Mockito.when(layerService.getList(any())).thenReturn(List.of(layerWithPattern));
        LayerSummarySearchDto
                input = new LayerSummarySearchDto(UUID.randomUUID(), UUID.randomUUID(), "some-feed",
                new Point(new double[]{1, 0}));
        List<LayerSummaryDto> response = Arrays.asList(
                restTemplate.postForEntity(SEARCH_URL, input, LayerSummaryDto[].class)
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
        Legend legend = new Legend("legendName", LegendType.SIMPLE, null, new ArrayList<>(), new ArrayList<>(),
                new BivariateLegendAxes(), null);
        Map<String, Object> map = new HashMap<>();
        map.put("prop", "value");
        legend.getSteps().add(new LegendStep("param name", null, "param value", null, null, "step name",
                HEX, map, "source-layer", "", ""));

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
