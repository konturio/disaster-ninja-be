package io.kontur.disasterninja.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class StyleColorParameters {
    @JsonProperty("fill-color")
    private List<List<Object>> fillColor;
    @JsonProperty("fill-opacity")
    private Double fillOpacity;
    @JsonProperty("fill-antialias")
    private Boolean fillAntialias;
    // sentiments
    private String bad;
    private String good;
    private List<Midpoint> midpoints;

    @Data
    @Builder
    @Jacksonized
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Midpoint {
        private Double value;
        private String color;
    }
}
