package io.kontur.disasterninja.dto.bivariatestatistic;

import lombok.Data;

import java.util.List;

@Data
public class CombinationDto {

    private String color;
    private List<String> corner;
    private String color_comment;
}
