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
public class StyleConfig {
    private String id;
    private Integer version;
    private List<StyleLayer> layers;
    private StyleColors colors;
}
