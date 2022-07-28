package io.kontur.disasterninja.dto.bivariatestatistic;

import lombok.Data;

import java.util.List;

@Data
public class ColorsDto {

    private String fallback;
    private List<CombinationDto> combinations;
}
