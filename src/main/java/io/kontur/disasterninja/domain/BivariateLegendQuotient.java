package io.kontur.disasterninja.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
@AllArgsConstructor
@NoArgsConstructor
public class BivariateLegendQuotient {

    private String name;
    private String label;
    private List<List<String>> direction;
    private Unit unit;
}
