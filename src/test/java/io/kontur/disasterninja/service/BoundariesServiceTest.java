package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.KcApiClient;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wololo.geojson.*;

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
                {
                  "type": "Feature",
                  "geometry": {
                    "type": "Point",
                    "coordinates": [-3.6749267578125, 41.166249339092]
                  },
                  "properties": {
                  }
                }
                """;
        Geometry geometry = ((Feature) GeoJSONFactory.create(geoJsonString)).getGeometry();
        Point point = new Point(new double[]{-3.6749267578125, 41.166249339092});

        ArgumentCaptor<GeoJSON> geoJSONArgumentCaptor = ArgumentCaptor.forClass(GeoJSON.class);

        when(geometryTransformer.getGeometryFromGeoJson(geoJSONArgumentCaptor.capture())).thenReturn(geometry);
        when(geometryTransformer.getPointFromGeometry(geometry)).thenReturn(point);
        when(kcApiClient.getCollectionItemsByPoint(point, "bounds")).thenReturn(Lists.newArrayList());

        //when
        boundariesService.getBoundaries(geoJsonString);

        //then
        GeoJSON geoJSONCaptorValue = geoJSONArgumentCaptor.getValue();
        assertEquals(GeoJSONFactory.create(geoJsonString).toString(), geoJSONCaptorValue.toString());

        verify(geometryTransformer).getGeometryFromGeoJson(any(GeoJSON.class));
        verify(geometryTransformer).getPointFromGeometry(geometry);
        verify(kcApiClient).getCollectionItemsByPoint(point, "bounds");

    }

}