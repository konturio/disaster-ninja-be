package io.kontur.disasterninja.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude
public class BivariateLegendAxisDescription {

    private String label;

    private List<BivariateLegendAxisStep> steps;

    private Double quality;

    private List<String> quotient;

    private List<String> parent;
}
