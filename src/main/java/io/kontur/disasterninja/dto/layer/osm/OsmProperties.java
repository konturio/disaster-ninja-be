package io.kontur.disasterninja.dto.layer.osm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class OsmProperties {
    public final OsmAttribution attribution;
    private final Boolean best;
    private final String category;
    @JsonProperty("country_code")
    private final String countryCode;
    private final String description;
    @JsonProperty("end_date")
    private final Integer endDate;
    private final String id;
    @JsonProperty("license_url")
    private final String licenseUrl;
    @JsonProperty("min_zoom")
    private final Double minZoom;
    @JsonProperty("max_zoom")
    private final Double maxZoom;
    private final String name;
    @JsonProperty("privacy_policy_url")
    private final String privaryPolicyUrl;
    @JsonProperty("start_date")
    private final Integer startDate;
    private final String type;
    private final String url;
}
