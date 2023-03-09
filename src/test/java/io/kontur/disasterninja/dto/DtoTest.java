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
import org.wololo.geojson.Feature;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.Point;

import java.util.*;

import static io.kontur.disasterninja.controller.LayerController.PATH_DETAILS;
import static io.kontur.disasterninja.controller.LayerController.PATH_SEARCH_GLOBAL;
import static io.kontur.disasterninja.domain.enums.LayerStepShape.HEX;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DtoTest {

    @MockBean
    LayerService layerService;
    @MockBean
    KeycloakAuthorizationService authorizationService;
    @Autowired
    private TestRestTemplate restTemplate;
    private final String SEARCH_URL = LayerController.PATH + PATH_SEARCH_GLOBAL;
    private final String DETAILS_URL = LayerController.PATH + PATH_DETAILS;

    @BeforeEach
    public void before() {
        Mockito.when(authorizationService.getAccessToken()).thenReturn("something");
    }

    @Test
    public void serializeDeserializeSummaryFromGeometryTest() {
        String id = "123";
        Layer layer = testLayer(id, new FeatureCollection(null));
        Mockito.when(layerService.getGlobalLayers(any())).thenReturn(List.of(layer));

        LayerSummarySearchDto
                input = new LayerSummarySearchDto(UUID.randomUUID(), UUID.randomUUID(), "some-feed",
                new Point(new double[]{1, 0}));
        List<LayerSummaryDto> response = Arrays.asList(
                restTemplate.postForEntity(SEARCH_URL, input, LayerSummaryDto[].class)
                        .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).id());
        Assertions.assertEquals(layer.isEventIdRequiredForRetrieval(), response.get(0).eventIdRequiredForRetrieval());
        Assertions.assertEquals(layer.getName(), response.get(0).name());
        Assertions.assertEquals(layer.getDescription(), response.get(0).description());
        Assertions.assertEquals(layer.getCategory(), response.get(0).category());
        Assertions.assertEquals(layer.getGroup(), response.get(0).group());
        Assertions.assertEquals(layer.isBoundaryRequiredForRetrieval(),
                response.get(0).boundaryRequiredForRetrieval());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).copyrights());
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

        Assertions.assertEquals(layer.getId(), response.get(0).id());
        Assertions.assertEquals(layer.getMinZoom(), response.get(0).minZoom());
        Assertions.assertEquals(layer.getMaxZoom(), response.get(0).maxZoom());

        Assertions.assertArrayEquals(
                ((Point) layer.getSource().getData().getFeatures()[0].getGeometry()).getCoordinates(),
                ((Point) ((FeatureCollection) response.get(0).source()
                        .data()).getFeatures()[0].getGeometry()).getCoordinates());

        Assertions.assertEquals(layer.getSource().getType(), response.get(0).source().type());
        Assertions.assertEquals(layer.getSource().getTileSize(), response.get(0).source().tileSize());
        Assertions.assertEquals(layer.getSource().getUrls(), response.get(0).source().urls());
    }

    @Test
    public void serializeDeserializeFromFeatureTest() {
        String id = "123";
        Layer layer = testLayer(id, new FeatureCollection(null));
        Mockito.when(layerService.getGlobalLayers(any())).thenReturn(List.of(layer));

        LayerSummarySearchDto input = new LayerSummarySearchDto(UUID.randomUUID(), UUID.randomUUID(),
                "some-feed",
                new Feature(new Point(
                        new double[]{1, 0}), new HashMap<>()));
        List<LayerSummaryDto> response = Arrays.asList(
                restTemplate.postForEntity(SEARCH_URL, input, LayerSummaryDto[].class)
                        .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).id());
        Assertions.assertEquals(layer.isEventIdRequiredForRetrieval(), response.get(0).eventIdRequiredForRetrieval());
        Assertions.assertEquals(layer.getName(), response.get(0).name());
        Assertions.assertEquals(layer.getDescription(), response.get(0).description());
        Assertions.assertEquals(layer.getCategory(), response.get(0).category());
        Assertions.assertEquals(layer.getGroup(), response.get(0).group());
        Assertions.assertEquals(layer.isBoundaryRequiredForRetrieval(),
                response.get(0).boundaryRequiredForRetrieval());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).copyrights());
    }

    @Test
    public void serializeDeserializeFromFeatureCollectionTest() {
        String id = "123";
        Layer layer = testLayer(id, new FeatureCollection(null));
        Mockito.when(layerService.getGlobalLayers(any())).thenReturn(List.of(layer));

        LayerSummarySearchDto input = new LayerSummarySearchDto(UUID.randomUUID(), null, null,
                null);

        List<LayerSummaryDto> response = Arrays.asList(
                restTemplate.postForEntity(SEARCH_URL, input, LayerSummaryDto[].class)
                        .getBody());

        Assertions.assertEquals(layer.getId(), response.get(0).id());
        Assertions.assertEquals(layer.isEventIdRequiredForRetrieval(), response.get(0).eventIdRequiredForRetrieval());
        Assertions.assertEquals(layer.getName(), response.get(0).name());
        Assertions.assertEquals(layer.getDescription(), response.get(0).description());
        Assertions.assertEquals(layer.getCategory(), response.get(0).category());
        Assertions.assertEquals(layer.getGroup(), response.get(0).group());
        Assertions.assertEquals(layer.isBoundaryRequiredForRetrieval(),
                response.get(0).boundaryRequiredForRetrieval());
        Assertions.assertEquals(layer.getCopyrights(), response.get(0).copyrights());
    }

    private Layer testLayer(String id, FeatureCollection geoJSON) {
        LayerSource source = LayerSource.builder()
                .type(LayerSourceType.RASTER)
                .urls(List.of("url-com.com"))
                .tileSize(2)
                .data(geoJSON).build();
        Legend legend = new Legend("legendName", LegendType.SIMPLE, null, new ArrayList<>(),
                new ArrayList<>(), new BivariateLegendAxes(), null);
        Map<String, Object> map = new HashMap<>();
        map.put("prop", "value");
        legend.getSteps().add(new LegendStep("param name", null, "param value",
                null, null, "step name", HEX, map, "source-layer", "",
                ""));

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
