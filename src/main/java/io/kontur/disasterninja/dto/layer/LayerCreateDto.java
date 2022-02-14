package io.kontur.disasterninja.dto.layer;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LayerCreateDto extends LayerUpdateDto {
    @NotNull
    @NotEmpty
    @Pattern(regexp = "[\\w]*")
    private String id;
}