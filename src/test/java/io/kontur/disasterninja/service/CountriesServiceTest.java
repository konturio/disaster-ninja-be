package io.kontur.disasterninja.service;

import io.kontur.disasterninja.client.LayersApiClient;
import io.kontur.disasterninja.domain.DtoFeatureProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.wololo.geojson.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CountriesServiceTest {

    @Mock
    private LayersApiClient layersApiClient;

    @Mock
    private GeometryTransformer geometryTransformer;

    @InjectMocks
    private CountriesService countriesService;

    @Test
    public void getAffectedCountriesTest() {
        String geoJsonString = """
                {
                  "type":"Feature",
                  "geometry":{"type":"Polygon","coordinates":[[[0,0],[1,0],[1,1],[0,1],[0,0]]]},
                  "properties":{}
                }
                """;
        GeoJSON geoJSON = GeoJSONFactory.create(geoJsonString);
        Geometry geometry = ((Feature) geoJSON).getGeometry();

        ArgumentCaptor<Geometry> geometryArgumentCaptor = ArgumentCaptor.forClass(Geometry.class);

        Feature f1 = new Feature(new Point(new double[]{0,0}), Map.of(DtoFeatureProperties.COUNTRY_CODE, "USA"));
        Feature f2 = new Feature(new Point(new double[]{1,1}), Map.of(DtoFeatureProperties.COUNTRY_CODE, "CAN"));

        when(geometryTransformer.getGeometryFromGeoJson(any())).thenReturn(geometry);
        when(layersApiClient.getCollectionFeatures(geometryArgumentCaptor.capture(), eq("konturBoundaries"), isNull()))
                .thenReturn(List.of(f1, f2));

        Set<String> result = countriesService.getAffectedCountries(geoJSON);

        assertEquals(Set.of("USA", "CAN"), result);
        assertEquals(geometry, geometryArgumentCaptor.getValue());

        verify(geometryTransformer).getGeometryFromGeoJson(any(GeoJSON.class));
        verify(layersApiClient).getCollectionFeatures(geometry, "konturBoundaries", null);
    }
}
