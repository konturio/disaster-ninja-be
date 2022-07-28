package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.InsightsApiGraphqlClientImpl;
import io.kontur.disasterninja.dto.bivariatestatistic.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wololo.geojson.GeoJSONFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BivariateMatrixServiceTest {

    @Mock
    private InsightsApiGraphqlClientImpl insightsApiGraphqlClient;

    @InjectMocks
    private BivariateMatrixService bivariateMatrixService;

    @Test
    public void getDataForBivariateMatrix() {
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","id":{},"geometry":{"type":"Polygon","coordinates":[[[13.348388671875,-9.275622176792098],[19.215087890624996,-9.275622176792098],[19.215087890624996,-6.937332868878443],[13.348388671875,-6.937332868878443],[13.348388671875,-9.275622176792098]]]},"properties":{}}]}
                  """;

        var geoJSON = GeoJSONFactory.create(geoJsonString);

        List<List<String>> importantLayers = List.of(
                List.of("count", "area_km2"),
                List.of("building_count", "area_km2"),
                List.of("highway_length", "area_km2"));

        OverlayDto overlayDto = new OverlayDto()
                .setName("Kontur OpenStreetMap Quantity")
                .setDescription("This map shows relative distribution of OpenStreetMap objects and Population. Last updated 2022-07-25T17:59:52Z")
                .setX(null)
                .setY(null)
                .setColors(List.of(new ColorDto()
                        .setId("A1")
                        .setColor("rgb(232,232,157)")))
                .setOrder(1);

        List<OverlayDto> overlays = List.of(overlayDto);

        BivariateStatisticDto bivariateStatisticDto = new BivariateStatisticDto(
                overlays, List.of(), List.of(), new MetaDto(), List.of(), new ColorsDto());


        when(insightsApiGraphqlClient.getBivariateMatrix(geoJSON, importantLayers))
                .thenReturn(CompletableFuture.completedFuture(bivariateStatisticDto));

        BivariateMatrixRequestDto requestDto = new BivariateMatrixRequestDto(geoJSON, importantLayers);

        BivariateStatisticDto result = bivariateMatrixService.getDataForBivariateMatrix(requestDto);

        verify(insightsApiGraphqlClient).getBivariateMatrix(geoJSON, importantLayers);
        assertEquals(overlayDto, result.getOverlays().get(0));
    }
}