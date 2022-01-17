package io.kontur.disasterninja.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.kontur.disasterninja.dto.layerapi.Layer;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.wololo.geojson.Feature;
import org.wololo.geojson.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LayersApiClient {

    private static final String LAYERS_SEARCH_URI = "/collections/search";
    private static final String LAYERS_FEATURES_SEARCH_URI = "/collections/%s/items/search";
    private static final String GET_LAYER_URI = "/collections/%s";

    private final RestTemplate layersApiRestTemplate;
    @Value("${kontur.platform.kcApi.pageSize}")
    private int pageSize;

    public LayersApiClient(RestTemplate layersApiRestTemplate) {
        this.layersApiRestTemplate = layersApiRestTemplate;
    }

    public List<Layer> getLayers(Geometry geoJson) {
        List<Layer> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        body.put("geometry", geoJson);
        body.put("limit", pageSize);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiLayersCollection> response = layersApiRestTemplate
                    .exchange(LAYERS_SEARCH_URI, HttpMethod.POST, new HttpEntity<>(body, headers),
                            new ParameterizedTypeReference<>() {});

            ApiLayersCollection responseBody = response.getBody();
            if (responseBody == null || CollectionUtils.isEmpty(responseBody.getLayers())) {
                break;
            }

            result.addAll(responseBody.getLayers());
            if (result.size() == responseBody.getNumberMatched()) {
                break;
            }
        }

        return result;
    }

    public List<Feature> getLayersFeatures(Geometry geoJson, String layerId) {
        Assert.notNull(layerId, "Layer ID should not be null");

        List<Feature> result = new ArrayList<>();

        Map<String, Object> body = new HashMap<>();
        body.put("geometry", geoJson);
        body.put("limit", pageSize);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        while (true) {
            body.put("offset", result.size());

            ResponseEntity<ApiLayersFeaturesCollection> response = layersApiRestTemplate
                    .exchange(String.format(LAYERS_FEATURES_SEARCH_URI, layerId), HttpMethod.POST,
                            new HttpEntity<>(body, headers),
                            new ParameterizedTypeReference<>() {});

            ApiLayersFeaturesCollection responseBody = response.getBody();
            if (responseBody == null || CollectionUtils.isEmpty(responseBody.getFeatures())) {
                break;
            }

            result.addAll(responseBody.getFeatures());
            if (result.size() == responseBody.getNumberMatched()) {
                break;
            }
        }

        return result;
    }

    public Layer getLayer(String layerId) {
        ResponseEntity<Layer> response = layersApiRestTemplate
                .exchange(String.format(GET_LAYER_URI, layerId), HttpMethod.GET, new HttpEntity<>(null, null),
                        new ParameterizedTypeReference<>() {});
        return response.getBody();
    }

    @Getter
    private static class ApiLayersCollection {

        private final List<Layer> layers;
        private final int numberMatched;
        private final int numberReturned;

        @JsonCreator
        public ApiLayersCollection(@JsonProperty("collections") List<Layer> layers,
                                   @JsonProperty("numberReturned") int numberReturned,
                                   @JsonProperty("numberMatched") int numberMatched) {
            this.layers = layers;
            this.numberMatched = numberMatched;
            this.numberReturned = numberReturned;
        }
    }

    @Getter
    private static class ApiLayersFeaturesCollection {

        private final List<Feature> features;
        private final int numberMatched;
        private final int numberReturned;

        @JsonCreator
        public ApiLayersFeaturesCollection(@JsonProperty("features") List<Feature> layers,
                                           @JsonProperty("numberReturned") int numberReturned,
                                           @JsonProperty("numberMatched") int numberMatched) {
            this.features = layers;
            this.numberMatched = numberMatched;
            this.numberReturned = numberReturned;
        }
    }
}
