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
public class StyleLayer {
    private List<String> axis;
    private List<Integer> range;
    private List<String> sentiment;
    private Integer coefficient;
    private String transformationFunction;
    private String normalization;
}
