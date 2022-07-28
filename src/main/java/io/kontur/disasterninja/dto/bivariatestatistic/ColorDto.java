package io.kontur.disasterninja.dto.bivariatestatistic;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ColorDto {

    private String id;
    private String color;
}
