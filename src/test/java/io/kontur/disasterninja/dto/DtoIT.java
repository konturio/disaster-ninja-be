package io.kontur.disasterninja.dto;

import io.kontur.disasterninja.domain.Layer;
import io.kontur.disasterninja.domain.LayerSource;
import io.kontur.disasterninja.domain.LegendItem;
import io.kontur.disasterninja.domain.enums.LayerCategory;
import io.kontur.disasterninja.domain.enums.LayerSourceType;
import io.kontur.disasterninja.domain.enums.LegendItemType;
import io.kontur.disasterninja.dto.layer.LayerSummaryDto;
import io.kontur.disasterninja.dto.layer.LayerSummaryInputDto;
import io.kontur.disasterninja.dto.layer.LegendItemDto;
import io.kontur.disasterninja.service.LayerService;
import k2layers.api.model.GeometryGeoJSON;
import k2layers.api.model.PointGeoJSON;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DtoIT {

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
        List<BigDecimal> coords = new ArrayList<>();
        coords.add(BigDecimal.ONE);
        coords.add(BigDecimal.ZERO);
        GeometryGeoJSON geoJSON = new PointGeoJSON().coordinates(coords);
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
        Assertions.assertEquals(layer.getLegend(), response.get(0).getLegend().stream()
            .map(LegendItemDto::toLegendItem).collect(Collectors.toList()));
    }

    private Layer testLayer(String id, GeometryGeoJSON geoJSON) {
        LayerSource source = new LayerSource(LayerSourceType.RASTER, "url-com.com", 2d, geoJSON);
        List<LegendItem> legend = new ArrayList<>();
        legend.add(new LegendItem(LegendItemType.SIMPLE, "param name", "param value",
            "icon.png", "some name", "#ffffff", "#dddddd", "#cccccc"));
        return new Layer(id, "test name", "test desciption", LayerCategory.BASE, "tset group",
            legend, "copyright text", 10, 1, source);
    }
}
