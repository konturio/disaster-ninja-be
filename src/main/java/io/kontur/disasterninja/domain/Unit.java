package io.kontur.disasterninja.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude
public class Unit {

    private String id;
    private String shortName;
    private String longName;
}
