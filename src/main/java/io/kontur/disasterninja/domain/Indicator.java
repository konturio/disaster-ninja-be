package io.kontur.disasterninja.domain;

import java.util.List;

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
public class Indicator {
    private String name;
    private String label;
    private String emoji;
    private Integer maxZoom;
    private String description;
    private List<String> copyrights;
    private List<List<String>> direction; // I have no idea why the FE needs an array of arrays here
    private Unit unit;

    // FIXME: a static nested class is kinda dubious, but as a separate class it looks kinda even more stupid...
    @Data
    @Builder
    @Jacksonized
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Unit {
        private String id;
        private String shortName;
        private String longName;
    }
}
