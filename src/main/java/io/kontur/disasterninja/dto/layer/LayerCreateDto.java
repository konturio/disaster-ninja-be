package io.kontur.disasterninja.dto.layer;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LayerCreateDto extends LayerUpdateDto {

    private String id;
}