package io.kontur.disasterninja.dto;

import lombok.Data;
import io.kontur.disasterninja.domain.SearchGroup;
import org.wololo.geojson.Feature;

import java.util.UUID;
import java.util.List;

@Data
public class SearchClickRequest {
    private UUID appId;
    private String query;
    private List<SearchGroup> searchResults;
    private Feature selectedFeature;
    private String selectedFeatureType;
}
