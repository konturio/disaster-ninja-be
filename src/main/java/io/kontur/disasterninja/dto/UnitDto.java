package io.kontur.disasterninja.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude
public class UnitDto {

    private String id;
    private String shortName;
    private String longName;
}
