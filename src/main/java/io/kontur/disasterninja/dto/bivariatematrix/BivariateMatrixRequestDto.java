package io.kontur.disasterninja.dto.bivariatematrix;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.wololo.geojson.GeoJSON;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BivariateMatrixRequestDto {

    private GeoJSON geoJSON;
    private List<List<String>> importantLayers;
}
