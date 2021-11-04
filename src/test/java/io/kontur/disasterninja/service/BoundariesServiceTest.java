package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.KcApiClient;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wololo.geojson.FeatureCollection;
import org.wololo.geojson.GeoJSON;
import org.wololo.geojson.GeoJSONFactory;
import org.wololo.geojson.Geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoundariesServiceTest {

    @Mock
    private KcApiClient kcApiClient;

    @Mock
    private GeometryTransformer geometryTransformer;

    @InjectMocks
    private BoundariesService boundariesService;

    @Test
    public void getBoundariesTest(){
        //given
        String geoJsonString = """
                {"type":"FeatureCollection","features":[{"type":"Feature","properties":{},"geometry":{"type":"Polygon","coordinates":[[[27.2021484375,54.13347814286039],[26.9989013671875,53.82335438174398],[27.79541015625,53.70321053273598],[27.960205078125,53.90110181472825],[28.004150390625,54.081951104880396],[27.6470947265625,54.21707306629117],[27.2021484375,54.13347814286039]]]}}]}";
                """;
        Geometry geometry = ((FeatureCollection) GeoJSONFactory.create(geoJsonString)).getFeatures()[0].getGeometry();

        ArgumentCaptor<GeoJSON> geoJSONArgumentCaptor = ArgumentCaptor.forClass(GeoJSON.class);

        when(geometryTransformer.getGeometryFromGeoJson(geoJSONArgumentCaptor.capture())).thenReturn(geometry);
        when(kcApiClient.getCollectionItemsByGeometry(geometry, "bounds")).thenReturn(Lists.newArrayList());

        //when
        boundariesService.getBoundaries(geoJsonString);

        //then
        GeoJSON geoJSONCaptorValue = geoJSONArgumentCaptor.getValue();
        assertEquals(GeoJSONFactory.create(geoJsonString).toString(), geoJSONCaptorValue.toString());

        verify(geometryTransformer).getGeometryFromGeoJson(any(GeoJSON.class));
        verify(kcApiClient).getCollectionItemsByGeometry(geometry, "bounds");

    }

}