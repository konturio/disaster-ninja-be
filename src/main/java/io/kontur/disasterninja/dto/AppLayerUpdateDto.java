package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppLayerUpdateDto {

    private String layerId;
    private Boolean isDefault;
    private ObjectNode legendStyle;
}
