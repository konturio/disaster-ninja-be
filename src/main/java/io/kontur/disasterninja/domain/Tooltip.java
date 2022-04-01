package io.kontur.disasterninja.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tooltip {

    private String type;
    private String paramName;
}
